package cbo.risk.sms.repositories;

import cbo.risk.sms.models.RequestPassBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestPassBookRepository  extends JpaRepository<RequestPassBook,Long> {
}
