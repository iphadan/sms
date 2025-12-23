package cbo.risk.sms.models;

import cbo.risk.sms.enums.TransactionType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Data
public class StockTransaction {
    @Id
    @GeneratedValue
    private Long transactionId;
    private TransactionType type; // RECEIVE, ISSUE, RETURN, REVERSE
    private String serialNumber;
    private String issuerId;
    private String receiverId;

    @NotBlank
    @Column(nullable = false)
    private String createdBy;
    @NotBlank
    @Column(nullable = false)
    private String lastUpdatedBy;
    @Column(name = "CREATED_TS",nullable = false)
    @CreationTimestamp
    private LocalDateTime createdTimestamp;
    @UpdateTimestamp
    @Column(name = "MODIFIED_TS",nullable = false)
    private LocalDateTime modifiedTimestamp;
}