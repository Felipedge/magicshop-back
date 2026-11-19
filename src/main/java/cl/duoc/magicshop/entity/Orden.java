package cl.duoc.magicshop.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
public class Orden {

    public static final String PAGADA = "pagada";
    public static final String RECHAZADA = "rechazada";

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false)
    private Instant fecha;

    private Long usuarioId;

    @Embedded
    private Cliente cliente;

    @Embedded
    private Despacho despacho;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrdenItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Integer total = 0;

    @Column(nullable = false, length = 12)
    private String estado;

    @Column(length = 300)
    private String motivo = "";

    public void agregarItem(OrdenItem item) {
        item.setOrden(this);
        items.add(item);
    }
}
