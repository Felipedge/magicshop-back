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
public class Despacho {

    @Column(name = "despacho_calle", length = 200)
    private String calle;

    @Column(name = "despacho_departamento", length = 50)
    private String departamento;

    @Column(name = "despacho_region", length = 100)
    private String region;

    @Column(name = "despacho_comuna", length = 100)
    private String comuna;

    @Column(name = "despacho_indicaciones", length = 300)
    private String indicaciones;
}
