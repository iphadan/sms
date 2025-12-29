package cbo.risk.sms.repositories;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.ParentBookType;
import cbo.risk.sms.enums.PassBookType;
import cbo.risk.sms.models.BookParent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BookParentRepository extends JpaRepository<BookParent, Long> {
    List<BookParent> findByBranchId(String branchId);

    @Query("SELECT bp FROM BookParent bp WHERE :serial BETWEEN bp.startingSerial AND bp.endingSerial")
    Optional<BookParent> findBySerialInRange(@Param("serial") String serial);

    @Query("SELECT bp FROM BookParent bp WHERE bp.branchId = :branchId AND bp.used < bp.numOfPad")
    List<BookParent> findAvailableBatchesByBranch(@Param("branchId") String branchId);
    // In BookParentRepository
//    @Query("SELECT bp FROM BookParent bp WHERE bp.branchId = :branchId " +
//            "AND bp.leavesPerCheckBook = :leavesPerCheckBook " +
//            "AND (bp.numOfPad - bp.used) > 0")
//    List<BookParent> findByBranchIdAndLeavesPerCheckBookAndAvailablePads(
//            @Param("branchId") String branchId,
//            @Param("leavesPerCheckBook") int leavesPerCheckBook);

//    @Query("SELECT bp FROM BookParent bp WHERE bp.branchId = :branchId " +
//            "AND bp.bookParentType = :bookType " + "AND bp.finished = false ")
//    List<BookParent> findByBranchIdAndBookTypeAndiSFinished(
//            @Param("branchId") String branchId,
//            @Param("bookType") String bookType
//
//    );

    @Query("""
    SELECT bp
    FROM BookParent bp
    WHERE bp.branchId = :branchId
      AND bp.checkLeaveType = :checkLeaveType
      AND bp.parentBookType = :parentBookType
      AND bp.finished = false
      AND bp.numOfPad > bp.used
""")
    Optional<BookParent> findAvailableBookParent(
            @Param("branchId") String branchId,
            @Param("checkLeaveType") CheckBookLeaveType checkLeaveType,
            @Param("parentBookType") ParentBookType parentBookType
    );


    @Query("""
    SELECT bp
    FROM BookParent bp
    WHERE bp.branchId = :branchId
      AND bp.passCheckType = :passCheckType
      AND bp.parentBookType = :parentBookType
      AND bp.finished = false
      AND bp.numOfPad > bp.used
""")
    Optional<BookParent> findAvailablePassBookParent(
            @Param("branchId") String branchId,
            @Param("passCheckType") String passCheckType,
            @Param("parentBookType") ParentBookType parentBookType
    );



}
