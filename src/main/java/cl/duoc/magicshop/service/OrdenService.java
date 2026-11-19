package cl.duoc.magicshop.service;

import cl.duoc.magicshop.dto.OrdenDtos.ItemRequest;
import cl.duoc.magicshop.dto.OrdenDtos.OrdenRequest;
import cl.duoc.magicshop.entity.Cliente;
import cl.duoc.magicshop.entity.Despacho;
import cl.duoc.magicshop.entity.Orden;
import cl.duoc.magicshop.entity.OrdenItem;
import cl.duoc.magicshop.entity.Producto;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.repository.OrdenRepository;
import cl.duoc.magicshop.repository.ProductoRepository;
import cl.duoc.magicshop.util.Catalogo;
import cl.duoc.magicshop.util.Filtros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class OrdenService {

    private static final Set<String> CAMPOS_FILTRO = Set.of("estado", "usuarioId", "numero");
    private static final Set<String> CAMPOS_ORDEN = Set.of("fecha", "total");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("America/Santiago"));

    private final OrdenRepository ordenes;
    private final ProductoRepository productos;
    private final MtgClient mtg;

    public OrdenService(OrdenRepository ordenes, ProductoRepository productos, MtgClient mtg) {
        this.ordenes = ordenes;
        this.productos = productos;
        this.mtg = mtg;
    }

    @Transactional(readOnly = true)
    public List<Orden> listar(Map<String, String> params) {
        return ordenes.findAll(Filtros.<Orden>desde(params, CAMPOS_FILTRO), Filtros.ordenar(params, CAMPOS_ORDEN));
    }

    @Transactional(readOnly = true)
    public Orden obtener(String id) {
        return ordenes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Orden no encontrada."));
    }

    @Transactional
    public Orden crear(OrdenRequest solicitud, Long usuarioId) {
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (ItemRequest item : solicitud.items()) {
            cantidades.merge(item.id(), item.cantidad(), Integer::sum);
        }

        Map<String, Producto> bloqueados = new LinkedHashMap<>();
        for (String id : new TreeSet<>(cantidades.keySet())) {
            bloqueados.put(id, productos.findByIdForUpdate(id).orElseGet(() -> crearDesdeMtg(id)));
        }

        Instant ahora = Instant.now();
        Orden orden = new Orden();
        orden.setId(UUID.randomUUID().toString());
        orden.setNumero(FORMATO_FECHA.format(ahora) + String.format("%04d", ahora.toEpochMilli() % 10000));
        orden.setFecha(ahora);
        orden.setUsuarioId(usuarioId);
        orden.setCliente(aCliente(solicitud));
        orden.setDespacho(aDespacho(solicitud));

        List<String> sinStock = new ArrayList<>();
        int total = 0;
        for (Map.Entry<String, Integer> entrada : cantidades.entrySet()) {
            Producto producto = bloqueados.get(entrada.getKey());
            int cantidad = entrada.getValue();
            if (Boolean.TRUE.equals(producto.getEliminado()) || producto.getStock() < cantidad) {
                sinStock.add(producto.getNombre());
            }
            int precioUnitario = Catalogo.precioFinal(producto);
            OrdenItem item = new OrdenItem();
            item.setProductoId(producto.getId());
            item.setNombre(producto.getNombre());
            item.setImagen(producto.getImagen());
            item.setCategoria(producto.getCategoria());
            item.setPrecioUnitario(precioUnitario);
            item.setCantidad(cantidad);
            item.setSubtotal(precioUnitario * cantidad);
            orden.agregarItem(item);
            total += item.getSubtotal();
        }
        orden.setTotal(total);

        String motivo = "";
        if (!sinStock.isEmpty()) {
            motivo = "Stock insuficiente para: " + String.join(", ", sinStock) + ".";
        } else if (Boolean.TRUE.equals(solicitud.simularRechazo())) {
            motivo = "El medio de pago fue rechazado por el banco.";
        }
        orden.setMotivo(motivo);
        orden.setEstado(motivo.isEmpty() ? Orden.PAGADA : Orden.RECHAZADA);

        if (Orden.PAGADA.equals(orden.getEstado())) {
            for (Map.Entry<String, Integer> entrada : cantidades.entrySet()) {
                Producto producto = bloqueados.get(entrada.getKey());
                producto.setStock(producto.getStock() - entrada.getValue());
                productos.save(producto);
            }
        }
        return ordenes.save(orden);
    }

    private Producto crearDesdeMtg(String id) {
        Producto carta = mtg.obtenerCarta(id)
                .orElseThrow(() -> ApiException.noEncontrado("La carta " + id + " no existe."));
        return productos.save(carta);
    }

    private static Cliente aCliente(OrdenRequest solicitud) {
        Cliente cliente = new Cliente();
        cliente.setNombre(solicitud.cliente().nombre().trim());
        cliente.setApellidos(solicitud.cliente().apellidos().trim());
        cliente.setEmail(solicitud.cliente().email().trim().toLowerCase());
        return cliente;
    }

    private static Despacho aDespacho(OrdenRequest solicitud) {
        Despacho despacho = new Despacho();
        despacho.setCalle(solicitud.despacho().calle().trim());
        despacho.setDepartamento(solicitud.despacho().departamento());
        despacho.setRegion(solicitud.despacho().region());
        despacho.setComuna(solicitud.despacho().comuna());
        despacho.setIndicaciones(solicitud.despacho().indicaciones());
        return despacho;
    }
}
