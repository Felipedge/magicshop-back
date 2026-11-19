package cl.duoc.magicshop.service;

import cl.duoc.magicshop.dto.CatalogoDtos.ProductoRequest;
import cl.duoc.magicshop.entity.Producto;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.repository.ProductoRepository;
import cl.duoc.magicshop.util.Filtros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductoService {

    private static final Set<String> CAMPOS_FILTRO = Set.of("id", "categoria", "rareza", "eliminado", "stock", "oferta", "precio");
    private static final Set<String> CAMPOS_ORDEN = Set.of("nombre", "precio", "stock", "oferta", "categoria");

    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar(Map<String, String> params) {
        return productos.findAll(Filtros.<Producto>desde(params, CAMPOS_FILTRO), Filtros.ordenar(params, CAMPOS_ORDEN));
    }

    @Transactional(readOnly = true)
    public Producto obtener(String id) {
        return productos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Producto no encontrado."));
    }

    @Transactional
    public Producto crear(ProductoRequest r) {
        if (vacio(r.id()) || vacio(r.nombre())) {
            throw ApiException.solicitudInvalida("El id de la carta y el nombre son obligatorios.");
        }
        if (r.precio() == null || r.stock() == null) {
            throw ApiException.solicitudInvalida("El precio y el stock son obligatorios.");
        }
        if (productos.existsById(r.id())) {
            throw ApiException.conflicto("La carta ya está en el inventario.");
        }
        Producto producto = new Producto();
        producto.setId(r.id());
        producto.setNombre(r.nombre().trim());
        aplicar(producto, r);
        return productos.save(producto);
    }

    @Transactional
    public Producto actualizar(String id, ProductoRequest r) {
        Producto producto = obtener(id);
        if (!vacio(r.nombre())) {
            producto.setNombre(r.nombre().trim());
        }
        aplicar(producto, r);
        return productos.save(producto);
    }

    @Transactional
    public void eliminar(String id) {
        productos.delete(obtener(id));
    }

    private void aplicar(Producto producto, ProductoRequest r) {
        if (r.categoria() != null) {
            producto.setCategoria(r.categoria());
        }
        if (r.tipo() != null) {
            producto.setTipo(r.tipo());
        }
        if (r.rareza() != null) {
            producto.setRareza(r.rareza());
        }
        if (r.edicion() != null) {
            producto.setEdicion(r.edicion());
        }
        if (r.imagen() != null) {
            if (!r.imagen().isBlank() && !r.imagen().startsWith("https://") && !r.imagen().startsWith("/")) {
                throw ApiException.solicitudInvalida("La imagen debe ser una URL https.");
            }
            producto.setImagen(r.imagen());
        }
        if (r.precio() != null) {
            producto.setPrecio(r.precio());
        }
        if (r.stock() != null) {
            producto.setStock(r.stock());
        }
        if (r.oferta() != null) {
            producto.setOferta(r.oferta());
        }
        if (r.eliminado() != null) {
            producto.setEliminado(r.eliminado());
        }
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
