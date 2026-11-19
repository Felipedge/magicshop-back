package cl.duoc.magicshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @Size(max = 12, message = "El RUN es demasiado largo.") String run,
        @Size(max = 80, message = "El nombre es demasiado largo.") String nombre,
        @Size(max = 80, message = "Los apellidos son demasiado largos.") String apellidos,
        @Email(message = "El correo no es válido.") @Size(max = 120) String email,
        @Pattern(regexp = "^$|^.{6,20}$", message = "La contraseña debe tener entre 6 y 20 caracteres.") String password,
        @Size(max = 10) String rol,
        @Size(max = 100) String region,
        @Size(max = 100) String comuna,
        @Size(max = 200) String direccion) {
}
