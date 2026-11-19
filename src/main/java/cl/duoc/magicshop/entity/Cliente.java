package cl.duoc.magicshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Column(name = "cliente_nombre", length = 80)
    private String nombre;

    @Column(name = "cliente_apellidos", length = 80)
    private String apellidos;

    @Column(name = "cliente_email", length = 120)
    private String email;
}
