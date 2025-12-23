package cbo.risk.sms.repositories;

import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.CheckBook;
import cbo.risk.sms.models.Cpo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CpoRepository extends JpaRepository<Cpo, Long> {
    Optional<Cpo> findBySerialNumber(String serialNumber);
    // Find by book parent ordered by serial number - METHOD 1: Using derived query
    List<Cpo> findByBookParentOrderBySerialNumberAsc(BookParent bookParent);
    boolean existsBySerialNumber(String serialNumber);

}
