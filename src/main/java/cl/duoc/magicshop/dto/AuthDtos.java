package cl.duoc.magicshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio.")
            @Email(message = "El correo no es válido.")
            String email,
            @NotBlank(message = "La contraseña es obligatoria.")
            String password) {
    }

    public record RegistroRequest(
            @Size(max = 12, message = "El RUN es demasiado largo.")
            String run,
            @NotBlank(message = "El nombre es obligatorio.")
            @Size(max = 80)
            String nombre,
            @NotBlank(message = "Los apellidos son obligatorios.")
            @Size(max = 80)
            String apellidos,
            @NotBlank(message = "El correo es obligatorio.")
            @Email(message = "El correo no es válido.")
            @Size(max = 120)
            String email,
            @NotBlank(message = "La contraseña es obligatoria.")
            @Size(min = 6, max = 20, message = "La contraseña debe tener entre 6 y 20 caracteres.")
            String password,
            @Size(max = 100) String region,
            @Size(max = 100) String comuna,
            @Size(max = 200) String direccion) {
    }

    public record AuthResponse(String token, UsuarioDto usuario) {
    }
}
