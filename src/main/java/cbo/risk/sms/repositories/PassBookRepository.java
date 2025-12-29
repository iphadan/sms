package cbo.risk.sms.repositories;

import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.PassBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassBookRepository extends JpaRepository<PassBook, Long> {

    // Basic CRUD
    List<PassBook> findByBranchIdOrderBySerialNumberAsc(String branchId);
    Optional<PassBook> findById(Long id);

    // Find by serial number
    Optional<PassBook> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);

    // Find by branch
    List<PassBook> findByBranchId(String branchId);
    Page<PassBook> findByBranchId(String branchId, Pageable pageable);

    // Find by BookParent
    List<PassBook> findByBookParentId(Long bookParentId);

    // Count by parent
    int countByBookParentId(Long bookParentId);

    // Find by parent with ordering for sequential issuance
    @Query("SELECT pb FROM PassBook pb WHERE pb.bookParent = :bookParent ORDER BY pb.serialNumber ASC")
    List<PassBook> findByBookParentOrderBySerialNumberAsc(@Param("bookParent") BookParent bookParent);

    // Status-based queries
    List<PassBook> findByBranchIdAndIssuedDateIsNull(String branchId);
    List<PassBook> findByBranchIdAndIssuedDateIsNotNull(String branchId);
    List<PassBook> findByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(String branchId);
    List<PassBook> findByBranchIdAndReturnedDateIsNotNull(String branchId);

    // Count queries
    int countByBranchId(String branchId);
    int countByBranchIdAndIssuedDateIsNull(String branchId);
    int countByBranchIdAndIssuedDateIsNotNull(String branchId);
    int countByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(String branchId);
    int countByBranchIdAndReturnedDateIsNotNull(String branchId);

    // Find by type and category
    List<PassBook> findByPassBookType(PassBookType type);
    List<PassBook> findByPassBookCategory(PassBookCategory category);
    List<PassBook> findByPassBookTypeAndPassBookCategory(PassBookType type, PassBookCategory category);

    // Find by process
    List<PassBook> findByProcessId(String processId);
    List<PassBook> findBySubProcessId(String subProcessId);
    List<PassBook> findByCreatedBy(String createdBy);

    // Combined queries
    List<PassBook> findByBranchIdAndProcessId(String branchId, String processId);
    List<PassBook> findByBranchIdAndSubProcessId(String branchId, String subProcessId);
    List<PassBook> findByBranchIdAndCreatedBy(String branchId, String createdBy);

    // Find by type/category and branch
    List<PassBook> findByBranchIdAndPassBookType(String branchId, PassBookType type);
    List<PassBook> findByBranchIdAndPassBookCategory(String branchId, PassBookCategory category);
    List<PassBook> findByBranchIdAndPassBookTypeAndPassBookCategory(String branchId, PassBookType type, PassBookCategory category);

    // Find by parent and status
    @Query("SELECT pb FROM PassBook pb WHERE pb.bookParent.id = :parentId AND pb.issuedDate IS NULL")
    List<PassBook> findAvailableByBookParentId(@Param("parentId") Long parentId);

    @Query("SELECT pb FROM PassBook pb WHERE pb.bookParent.id = :parentId AND pb.issuedDate IS NOT NULL AND pb.returnedDate IS NULL")
    List<PassBook> findIssuedByBookParentId(@Param("parentId") Long parentId);

    @Query("SELECT pb FROM PassBook pb WHERE pb.bookParent.id = :parentId AND pb.returnedDate IS NOT NULL")
    List<PassBook> findReturnedByBookParentId(@Param("parentId") Long parentId);

    // Find by parent and type/category
    List<PassBook> findByBookParentIdAndPassBookType(Long parentId, PassBookType type);
    List<PassBook> findByBookParentIdAndPassBookCategory(Long parentId, PassBookCategory category);
    List<PassBook> findByBookParentIdAndPassBookTypeAndPassBookCategory(Long parentId, PassBookType type, PassBookCategory category);

    // Check if any PassBook in parent is issued
    @Query("SELECT CASE WHEN COUNT(pb) > 0 THEN TRUE ELSE FALSE END " +
            "FROM PassBook pb WHERE pb.bookParent.id = :parentId AND pb.issuedDate IS NOT NULL")
    boolean hasIssuedItemsByBookParentId(@Param("parentId") Long parentId);
}