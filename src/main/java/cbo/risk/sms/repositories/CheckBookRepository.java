package cbo.risk.sms.repositories;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.CheckBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckBookRepository extends JpaRepository<CheckBook, Long> {
    @Query("SELECT CASE WHEN COUNT(cb) > 0 THEN TRUE ELSE FALSE END " +
            "FROM CheckBook cb WHERE cb.startSerialNumber = :startSerial " +
            "AND cb.endSerialNumber = :endSerial")
    boolean existsByStartSerialNumberAndEndSerialNumber(
            @Param("startSerial") String startSerial,
            @Param("endSerial") String endSerial);

    // Find checkbook by serial range
    Optional<CheckBook> findByStartSerialNumberAndEndSerialNumber(
            String startSerialNumber, String endSerialNumber);

    // Find checkbooks within a serial range
    @Query("SELECT cb FROM CheckBook cb WHERE " +
            "cb.startSerialNumber >= :startSerial AND cb.endSerialNumber <= :endSerial")
    List<CheckBook> findBySerialRange(
            @Param("startSerial") String startSerial,
            @Param("endSerial") String endSerial);

    // Basic CRUD
    Optional<CheckBook> findById(Long id);

    // Find by serial number
    Optional<CheckBook> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);

    // Find by branch
    List<CheckBook> findByBranchId(String branchId);
    Page<CheckBook> findByBranchId(String branchId, Pageable pageable);

    // Find by BookParent
    List<CheckBook> findByBookParentId(Long bookParentId);

    // Find by parent with ordering for sequential issuance
    @Query("SELECT cb FROM CheckBook cb WHERE cb.bookParent = :bookParent ORDER BY cb.serialNumber ASC")
    List<CheckBook> findByBookParentOrderBySerialNumberAsc(@Param("bookParent") BookParent bookParent);

    // Status-based queries
    List<CheckBook> findByBranchIdAndIssuedDateIsNull(String branchId);
    List<CheckBook> findByBranchIdAndIssuedDateIsNotNull(String branchId);
    List<CheckBook> findByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(String branchId);
    List<CheckBook> findByBranchIdAndReturnedDateIsNotNull(String branchId);

    // Count queries
    int countByBranchId(String branchId);
    int countByBranchIdAndIssuedDateIsNull(String branchId);
    int countByBranchIdAndIssuedDateIsNotNull(String branchId);
    int countByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(String branchId);
    int countByBranchIdAndReturnedDateIsNotNull(String branchId);

    // Find by type
    List<CheckBook> findByCheckBookType(CheckBookType type);
    List<CheckBook> findByCheckBookLeaveType(CheckBookLeaveType leaveType);
    List<CheckBook> findByCheckBookTypeAndCheckBookLeaveType(CheckBookType type, CheckBookLeaveType leaveType);

    // Find by process
    List<CheckBook> findByProcessId(String processId);
    List<CheckBook> findBySubProcessId(String subProcessId);
    List<CheckBook> findByCreatedBy(String createdBy);

    // Combined queries
    List<CheckBook> findByBranchIdAndProcessId(String branchId, String processId);
    List<CheckBook> findByBranchIdAndSubProcessId(String branchId, String subProcessId);
    List<CheckBook> findByBranchIdAndCreatedBy(String branchId, String createdBy);

    // Search with multiple criteria
    @Query("SELECT cb FROM CheckBook cb WHERE " +
            "(:branchId IS NULL OR cb.branchId = :branchId) AND " +
            "(:processId IS NULL OR cb.processId = :processId) AND " +
            "(:subProcessId IS NULL OR cb.subProcessId = :subProcessId) AND " +
            "(:createdBy IS NULL OR cb.createdBy = :createdBy) AND " +
            "(:checkBookType IS NULL OR cb.checkBookType = :checkBookType) AND " +
            "(:checkBookLeaveType IS NULL OR cb.checkBookLeaveType = :checkBookLeaveType)")
    List<CheckBook> findByMultipleCriteria(
            @Param("branchId") String branchId,
            @Param("processId") String processId,
            @Param("subProcessId") String subProcessId,
            @Param("createdBy") String createdBy,
            @Param("checkBookType") CheckBookType checkBookType,
            @Param("checkBookLeaveType") CheckBookLeaveType checkBookLeaveType);
}