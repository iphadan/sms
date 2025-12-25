package cbo.risk.sms.repositories;

import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.Cpo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CpoRepository extends JpaRepository<Cpo, Long> {

    // Basic CRUD
    Optional<Cpo> findById(Long id);

    // Find by serial number
    Optional<Cpo> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);

    // Find by branch
    List<Cpo> findByBranchId(String branchId);
    Page<Cpo> findByBranchId(String branchId, Pageable pageable);

    // Find by BookParent
    List<Cpo> findByBookParentId(Long bookParentId);

    // Count by parent
    int countByBookParentId(Long bookParentId);

    // Find by parent with ordering for sequential issuance
    @Query("SELECT c FROM Cpo c WHERE c.bookParent = :bookParent ORDER BY c.serialNumber ASC")
    List<Cpo> findByBookParentOrderBySerialNumberAsc(@Param("bookParent") BookParent bookParent);

    // Status-based queries
    List<Cpo> findByBranchIdAndIssuedDateIsNull(String branchId);
    List<Cpo> findByBranchIdAndIssuedDateIsNotNull(String branchId);
    List<Cpo> findByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(String branchId);
    List<Cpo> findByBranchIdAndReturnedDateIsNotNull(String branchId);

    // Count queries
    int countByBranchId(String branchId);
    int countByBranchIdAndIssuedDateIsNull(String branchId);
    int countByBranchIdAndIssuedDateIsNotNull(String branchId);
    int countByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(String branchId);
    int countByBranchIdAndReturnedDateIsNotNull(String branchId);

    // Find by process
    List<Cpo> findByProcessId(String processId);
    List<Cpo> findBySubProcessId(String subProcessId);
    List<Cpo> findByCreatedBy(String createdBy);

    // Combined queries
    List<Cpo> findByBranchIdAndProcessId(String branchId, String processId);
    List<Cpo> findByBranchIdAndSubProcessId(String branchId, String subProcessId);
    List<Cpo> findByBranchIdAndCreatedBy(String branchId, String createdBy);

    // Find by parent and status
    @Query("SELECT c FROM Cpo c WHERE c.bookParent.id = :parentId AND c.issuedDate IS NULL")
    List<Cpo> findAvailableByBookParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Cpo c WHERE c.bookParent.id = :parentId AND c.issuedDate IS NOT NULL AND c.returnedDate IS NULL")
    List<Cpo> findIssuedByBookParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Cpo c WHERE c.bookParent.id = :parentId AND c.returnedDate IS NOT NULL")
    List<Cpo> findReturnedByBookParentId(@Param("parentId") Long parentId);

    // Check if any CPO in parent is issued
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Cpo c WHERE c.bookParent.id = :parentId AND c.issuedDate IS NOT NULL")
    boolean hasIssuedItemsByBookParentId(@Param("parentId") Long parentId);
}