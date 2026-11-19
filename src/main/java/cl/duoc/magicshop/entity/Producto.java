package cl.duoc.magicshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 50)
    private String categoria;

    @Column(length = 150)
    private String tipo;

    @Column(length = 30)
    private String rareza;

    @Column(length = 100)
    private String edicion;

    @Column(length = 500)
    private String imagen;

    @Column(nullable = false)
    private Integer precio = 0;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private Integer oferta = 0;

    @Column(nullable = false)
    private Boolean eliminado = false;
}
