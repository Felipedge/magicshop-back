package cl.duoc.magicshop.service;

import cl.duoc.magicshop.dto.AuthDtos.AuthResponse;
import cl.duoc.magicshop.dto.AuthDtos.LoginRequest;
import cl.duoc.magicshop.dto.AuthDtos.RegistroRequest;
import cl.duoc.magicshop.dto.UsuarioDto;
import cl.duoc.magicshop.entity.Usuario;
import cl.duoc.magicshop.exception.ApiException;
import cl.duoc.magicshop.repository.UsuarioRepository;
import cl.duoc.magicshop.security.AuthUser;
import cl.duoc.magicshop.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final String hashRelleno;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.hashRelleno = encoder.encode("relleno-para-igualar-tiempos");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest solicitud) {
        String email = solicitud.email().trim().toLowerCase();
        Usuario usuario = usuarios.findByEmailIgnoreCase(email).orElse(null);
        String hash = usuario != null ? usuario.getPassword() : hashRelleno;
        boolean coincide = encoder.matches(solicitud.password(), hash);
        if (usuario == null || !coincide) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, MENSAJE_CREDENCIALES);
        }
        return new AuthResponse(jwtService.generar(usuario), UsuarioDto.de(usuario));
    }

    /**
     * Renueva la sesión: entrega un token nuevo con la fecha de vencimiento reiniciada.
     * Solo se llega aquí con un token válido y no vencido (lo exige Spring Security) y los datos
     * (rol, correo) se vuelven a leer de la BD, así un cambio de rol o una cuenta eliminada se notan.
     */
    @Transactional(readOnly = true)
    public AuthResponse renovar(AuthUser actor) {
        Usuario usuario = usuarios.findById(actor.id())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión."));
        return new AuthResponse(jwtService.generar(usuario), UsuarioDto.de(usuario));
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest solicitud) {
        String email = solicitud.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setRun(solicitud.run());
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellidos(solicitud.apellidos().trim());
        usuario.setEmail(email);
        usuario.setPassword(encoder.encode(solicitud.password()));
        usuario.setRol(Usuario.ROL_CLIENTE);
        usuario.setRegion(solicitud.region());
        usuario.setComuna(solicitud.comuna());
        usuario.setDireccion(solicitud.direccion());
        Usuario guardado = usuarios.save(usuario);
        return new AuthResponse(jwtService.generar(guardado), UsuarioDto.de(guardado));
    }
}
