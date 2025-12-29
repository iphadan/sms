package cbo.risk.sms.repositories;

import cbo.risk.sms.models.CheckBook;
import cbo.risk.sms.models.RequestCheckBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestCheckBookRepository extends JpaRepository<RequestCheckBook,Long> {
    List<RequestCheckBook> findByBranchId(String branchId);

}
