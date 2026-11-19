package cl.duoc.magicshop.util;

import cl.duoc.magicshop.exception.ApiException;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Filtros {

    private Filtros() {
    }

    public static <T> Specification<T> desde(Map<String, String> params, Set<String> permitidos) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            for (Map.Entry<String, String> entrada : params.entrySet()) {
                String clave = entrada.getKey();
                if (clave.startsWith("_")) {
                    continue;
                }
                String operador = "";
                String campo = clave;
                for (String sufijo : new String[]{"_ne", "_lte", "_gte"}) {
                    if (clave.endsWith(sufijo)) {
                        operador = sufijo;
                        campo = clave.substring(0, clave.length() - sufijo.length());
                    }
                }
                if (!permitidos.contains(campo)) {
                    throw ApiException.solicitudInvalida("Filtro no permitido: " + clave);
                }
                Path<Object> ruta = root.get(campo);
                Object valor = convertir(entrada.getValue(), ruta.getJavaType(), clave);
                switch (operador) {
                    case "_ne" -> predicados.add(cb.notEqual(ruta, valor));
                    case "_lte" -> predicados.add(cb.le(root.<Number>get(campo), (Number) numero(valor, clave)));
                    case "_gte" -> predicados.add(cb.ge(root.<Number>get(campo), (Number) numero(valor, clave)));
                    default -> predicados.add(cb.equal(ruta, valor));
                }
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    public static Sort ordenar(Map<String, String> params, Set<String> permitidos) {
        String campo = params.get("_sort");
        if (campo == null || campo.isBlank()) {
            return Sort.unsorted();
        }
        if (!permitidos.contains(campo)) {
            throw ApiException.solicitudInvalida("Orden no permitido: " + campo);
        }
        Sort.Direction direccion = "desc".equalsIgnoreCase(params.get("_order")) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direccion, campo);
    }

    private static Object numero(Object valor, String clave) {
        if (valor instanceof Number) {
            return valor;
        }
        throw ApiException.solicitudInvalida("El filtro " + clave + " requiere un número.");
    }

    private static Object convertir(String texto, Class<?> tipo, String clave) {
        try {
            if (tipo == Boolean.class || tipo == boolean.class) {
                return Boolean.valueOf(texto);
            }
            if (tipo == Integer.class || tipo == int.class) {
                return Integer.valueOf(texto);
            }
            if (tipo == Long.class || tipo == long.class) {
                return Long.valueOf(texto);
            }
            return texto;
        } catch (NumberFormatException e) {
            throw ApiException.solicitudInvalida("Valor inválido para el filtro " + clave + ".");
        }
    }
}
