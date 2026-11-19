package cl.duoc.magicshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class OrdenDtos {

    private OrdenDtos() {
    }

    public record ClienteRequest(
            @NotBlank(message = "El nombre es obligatorio.") @Size(max = 80) String nombre,
            @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 80) String apellidos,
            @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no es válido.") @Size(max = 120) String email) {
    }

    public record DespachoRequest(
            @NotBlank(message = "La calle es obligatoria.") @Size(max = 200) String calle,
            @Size(max = 50) String departamento,
            @NotBlank(message = "La región es obligatoria.") @Size(max = 100) String region,
            @NotBlank(message = "La comuna es obligatoria.") @Size(max = 100) String comuna,
            @Size(max = 300) String indicaciones) {
    }

    public record ItemRequest(
            @NotBlank(message = "El id de la carta es obligatorio.") @Size(max = 64) String id,
            @NotNull(message = "La cantidad es obligatoria.")
            @Min(value = 1, message = "La cantidad mínima es 1.")
            @Max(value = 99, message = "La cantidad máxima es 99.") Integer cantidad) {
    }

    public record OrdenRequest(
            @NotNull(message = "Los datos del cliente son obligatorios.") @Valid ClienteRequest cliente,
            @NotNull(message = "Los datos de despacho son obligatorios.") @Valid DespachoRequest despacho,
            @NotEmpty(message = "El carrito está vacío.") @Valid List<ItemRequest> items,
            Boolean simularRechazo) {
    }
}
