package cl.duoc.magicshop.controller;

import cl.duoc.magicshop.dto.UsuarioDto;
import cl.duoc.magicshop.dto.UsuarioRequest;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.security.AuthUser;
import cl.duoc.magicshop.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<UsuarioDto> listar(@RequestParam Map<String, String> params) {
        return servicio.listar(params);
    }

    @GetMapping("/{id}")
    public UsuarioDto obtener(@PathVariable Long id, Authentication autenticacion) {
        return servicio.obtener(id, actor(autenticacion));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest solicitud) {
        return servicio.crear(solicitud);
    }

    @PatchMapping("/{id}")
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest solicitud, Authentication autenticacion) {
        return servicio.actualizar(id, solicitud, actor(autenticacion));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication autenticacion) {
        servicio.eliminar(id, actor(autenticacion));
    }

    private static AuthUser actor(Authentication autenticacion) {
        AuthUser actor = AuthUser.actual(autenticacion);
        if (actor == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }
        return actor;
    }
}
