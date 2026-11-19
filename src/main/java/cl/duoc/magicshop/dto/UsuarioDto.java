package cl.duoc.magicshop.dto;

import cl.duoc.magicshop.entity.Usuario;

public record UsuarioDto(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String email,
        String rol,
        String region,
        String comuna,
        String direccion) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getEmail(),
                u.getRol(), u.getRegion(), u.getComuna(), u.getDireccion());
    }
}
