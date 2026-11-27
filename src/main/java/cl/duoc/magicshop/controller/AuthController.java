package cl.duoc.magicshop.controller;

import cl.duoc.magicshop.dto.AuthDtos.AuthResponse;
import cl.duoc.magicshop.dto.AuthDtos.LoginRequest;
import cl.duoc.magicshop.dto.AuthDtos.RegistroRequest;
import cl.duoc.magicshop.dto.UsuarioDto;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.security.AuthUser;
import cl.duoc.magicshop.service.AuthService;
import cl.duoc.magicshop.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.login(solicitud);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registro(@Valid @RequestBody RegistroRequest solicitud) {
        return authService.registrar(solicitud);
    }

    /** Renovación del token: requiere un token válido y devuelve uno nuevo con el vencimiento reiniciado. */
    @PostMapping("/refresh")
    public AuthResponse refresh(Authentication autenticacion) {
        AuthUser actor = AuthUser.actual(autenticacion);
        if (actor == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }
        return authService.renovar(actor);
    }

    @GetMapping("/me")
    public UsuarioDto me(Authentication autenticacion) {
        AuthUser actor = AuthUser.actual(autenticacion);
        if (actor == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }
        return usuarioService.obtener(actor.id(), actor);
    }
}
