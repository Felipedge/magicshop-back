package cl.duoc.magicshop.repository;

import cl.duoc.magicshop.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrdenRepository extends JpaRepository<Orden, String>, JpaSpecificationExecutor<Orden> {
}
