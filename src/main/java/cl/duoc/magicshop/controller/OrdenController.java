package cl.duoc.magicshop.controller;

import cl.duoc.magicshop.dto.OrdenDtos.OrdenRequest;
import cl.duoc.magicshop.entity.Orden;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.security.AuthUser;
import cl.duoc.magicshop.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService servicio;

    public OrdenController(OrdenService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Orden> listar(@RequestParam Map<String, String> params, Authentication autenticacion) {
        AuthUser actor = AuthUser.actual(autenticacion);
        if (actor == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }
        Map<String, String> filtros = new HashMap<>(params);
        if (!actor.esAdmin()) {
            filtros.put("usuarioId", String.valueOf(actor.id()));
        }
        return servicio.listar(filtros);
    }

    @GetMapping("/{id}")
    public Orden obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Orden crear(@Valid @RequestBody OrdenRequest solicitud, Authentication autenticacion) {
        AuthUser actor = AuthUser.actual(autenticacion);
        return servicio.crear(solicitud, actor == null ? null : actor.id());
    }
}
