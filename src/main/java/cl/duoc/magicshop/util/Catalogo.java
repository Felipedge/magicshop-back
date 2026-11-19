package cl.duoc.magicshop.util;

import cl.duoc.magicshop.entity.Producto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Catalogo {

    public static final int STOCK_INICIAL = 10;

    private static final Map<String, String> CATEGORIA_POR_TIPO = new LinkedHashMap<>();
    private static final Map<String, Integer> PRECIO_POR_RAREZA = Map.of(
            "Common", 500,
            "Uncommon", 1200,
            "Rare", 3500,
            "Mythic Rare", 8000
    );

    static {
        CATEGORIA_POR_TIPO.put("Creature", "criaturas");
        CATEGORIA_POR_TIPO.put("Planeswalker", "planeswalkers");
        CATEGORIA_POR_TIPO.put("Instant", "instantaneos");
        CATEGORIA_POR_TIPO.put("Sorcery", "conjuros");
        CATEGORIA_POR_TIPO.put("Enchantment", "encantamientos");
        CATEGORIA_POR_TIPO.put("Artifact", "artefactos");
        CATEGORIA_POR_TIPO.put("Land", "tierras");
    }

    private Catalogo() {
    }

    public static String categoriaDeTipos(List<String> tipos) {
        for (Map.Entry<String, String> entrada : CATEGORIA_POR_TIPO.entrySet()) {
            if (tipos.contains(entrada.getKey())) {
                return entrada.getValue();
            }
        }
        return "otros";
    }

    public static int precioBase(String idCarta, String rareza) {
        int hash = 0;
        for (char caracter : idCarta.toCharArray()) {
            hash += caracter;
        }
        int base = PRECIO_POR_RAREZA.getOrDefault(rareza == null ? "" : rareza, 800);
        return base + (hash % 10) * 50;
    }

    public static int precioFinal(Producto producto) {
        int descuento = producto.getOferta() == null ? 0 : producto.getOferta();
        return (int) Math.round(producto.getPrecio() * (1 - descuento / 100.0));
    }
}
