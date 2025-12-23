package cbo.risk.sms.repositories;

import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.CheckBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckBookRepository extends JpaRepository<CheckBook, Long> {
    Optional<CheckBook> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);
    List<CheckBook> findByBookParent(BookParent bookParent);
    List<CheckBook> findByBookParentOrderBySerialNumberAsc(BookParent bookParent);
    List<CheckBook> findByBranchId(String branchId);
    List<CheckBook> findByBranchIdAndIssuedDateIsNull(String branchId);
    List<CheckBook> findByBranchIdAndIssuedDateIsNotNull(String branchId);
    int countByBookParent(BookParent bookParent);
    int countByBookParentAndIssuedDateIsNotNull(BookParent bookParent);
}
