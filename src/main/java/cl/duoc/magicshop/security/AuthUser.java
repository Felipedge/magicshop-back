package cl.duoc.magicshop.security;

import cl.duoc.magicshop.entity.Usuario;
import org.springframework.security.core.Authentication;

public record AuthUser(Long id, String email, String rol) {

    public static AuthUser de(Usuario usuario) {
        return new AuthUser(usuario.getId(), usuario.getEmail(), usuario.getRol());
    }

    public static AuthUser actual(Authentication autenticacion) {
        if (autenticacion != null && autenticacion.getPrincipal() instanceof AuthUser usuario) {
            return usuario;
        }
        return null;
    }

    public boolean esAdmin() {
        return Usuario.ROL_ADMIN.equals(rol);
    }
}
