package cl.duoc.magicshop.service;

import cl.duoc.magicshop.dto.CatalogoDtos.CategoriaRequest;
import cl.duoc.magicshop.entity.Categoria;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.repository.CategoriaRepository;
import cl.duoc.magicshop.util.Filtros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CategoriaService {

    private static final Set<String> CAMPOS_FILTRO = Set.of("id", "slug", "tipoApi");
    private static final Set<String> CAMPOS_ORDEN = Set.of("id", "nombre", "slug");

    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar(Map<String, String> params) {
        return categorias.findAll(Filtros.<Categoria>desde(params, CAMPOS_FILTRO), Filtros.ordenar(params, CAMPOS_ORDEN));
    }

    @Transactional(readOnly = true)
    public Categoria obtener(Long id) {
        return categorias.findById(id).orElseThrow(() -> ApiException.noEncontrado("Categoría no encontrada."));
    }

    @Transactional
    public Categoria crear(CategoriaRequest r) {
        if (vacio(r.nombre()) || vacio(r.slug()) || vacio(r.tipoApi())) {
            throw ApiException.solicitudInvalida("Nombre, slug y tipo de carta son obligatorios.");
        }
        if (categorias.existsBySlug(r.slug())) {
            throw ApiException.conflicto("Ya existe una categoría para ese tipo de carta.");
        }
        Categoria categoria = new Categoria();
        categoria.setSlug(r.slug());
        categoria.setTipoApi(r.tipoApi());
        categoria.setNombre(r.nombre().trim());
        categoria.setDescripcion(r.descripcion());
        categoria.setImagen(r.imagen());
        return categorias.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaRequest r) {
        Categoria categoria = obtener(id);
        if (!vacio(r.nombre())) {
            categoria.setNombre(r.nombre().trim());
        }
        if (r.descripcion() != null) {
            categoria.setDescripcion(r.descripcion());
        }
        if (r.imagen() != null) {
            categoria.setImagen(r.imagen());
        }
        return categorias.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        categorias.delete(obtener(id));
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
