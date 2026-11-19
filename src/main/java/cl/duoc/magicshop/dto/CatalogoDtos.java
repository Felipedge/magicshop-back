package cl.duoc.magicshop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record ProductoRequest(
            @Size(max = 64) String id,
            @Size(max = 150) String nombre,
            @Size(max = 50) String categoria,
            @Size(max = 150) String tipo,
            @Size(max = 30) String rareza,
            @Size(max = 100) String edicion,
            @Size(max = 500) String imagen,
            @Min(value = 0, message = "El precio no puede ser negativo.") Integer precio,
            @Min(value = 0, message = "El stock no puede ser negativo.") Integer stock,
            @Min(value = 0, message = "La oferta no puede ser negativa.")
            @Max(value = 90, message = "La oferta no puede superar el 90%.") Integer oferta,
            Boolean eliminado) {
    }

    public record CategoriaRequest(
            @Size(max = 50) String slug,
            @Size(max = 80) String nombre,
            @Size(max = 500) String descripcion,
            @Size(max = 50) String tipoApi,
            @Size(max = 500) String imagen) {
    }
}
