package cl.duoc.magicshop.service;

import cl.duoc.magicshop.entity.Producto;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.util.Catalogo;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MtgClient {

    private final RestClient rest;

    public MtgClient(@Value("${app.mtg.url}") String url, RestClient.Builder builder) {
        this.rest = builder.baseUrl(url).build();
    }

    public Optional<Producto> obtenerCarta(String id) {
        try {
            JsonNode raiz = rest.get().uri("/cards/{id}", id).retrieve().body(JsonNode.class);
            JsonNode carta = raiz == null ? null : raiz.get("card");
            if (carta == null || carta.isNull()) {
                return Optional.empty();
            }
            return Optional.of(aProducto(carta));
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.BadRequest e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo consultar la API de Magic.");
        }
    }

    private Producto aProducto(JsonNode carta) {
        List<String> tipos = new ArrayList<>();
        JsonNode nodoTipos = carta.get("types");
        if (nodoTipos != null && nodoTipos.isArray()) {
            nodoTipos.forEach(t -> tipos.add(t.asText()));
        }
        String id = carta.path("id").asText();
        String rareza = carta.path("rarity").asText("");

        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(carta.path("name").asText());
        producto.setCategoria(Catalogo.categoriaDeTipos(tipos));
        producto.setTipo(carta.path("type").asText(""));
        producto.setRareza(rareza);
        producto.setEdicion(carta.path("setName").asText(""));
        producto.setImagen(carta.path("imageUrl").asText("").replaceFirst("^http:", "https:"));
        producto.setPrecio(Catalogo.precioBase(id, rareza));
        producto.setStock(Catalogo.STOCK_INICIAL);
        producto.setOferta(0);
        producto.setEliminado(false);
        return producto;
    }
}
