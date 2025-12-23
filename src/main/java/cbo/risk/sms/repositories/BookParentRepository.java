package cbo.risk.sms.repositories;

import cbo.risk.sms.models.BookParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookParentRepository extends JpaRepository<BookParent, Long> {
    List<BookParent> findByBranchId(String branchId);

    @Query("SELECT bp FROM BookParent bp WHERE :serial BETWEEN bp.startingSerial AND bp.endingSerial")
    Optional<BookParent> findBySerialInRange(@Param("serial") String serial);

    @Query("SELECT bp FROM BookParent bp WHERE bp.branchId = :branchId AND bp.used < bp.numOfPad")
    List<BookParent> findAvailableBatchesByBranch(@Param("branchId") String branchId);

}
