package cbo.risk.sms.repositories;

import cbo.risk.sms.models.RequestCpo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestCpoRepository extends JpaRepository<RequestCpo, Long> {
}
