package cbo.risk.sms.models;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Entity
@Audited
public class RequestCpo {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
private Long cpoId;
    private String serialNum;
    private LocalDateTime receivedDate;
    private LocalDateTime issuedDate;
    private String issuedBy;
    private String receivedBy;
    private String issuedById;
    private String receivedById;    @NotBlank
    @Column(name = "BRANCH_ID")
    private String branchId;

    @Column(name ="ACCOUNT_NUMBER")
    private String accountNumber;

    @NotBlank
    @Column(name = "SUBPROCESS_ID")
    private String subProcessId;
    @NotBlank
    @Column(name = "PROCESS_ID")
    private String processId;

    @NotBlank
    @Column(nullable = false)
    private String createdBy;

    @NotBlank
    @Column(nullable = false)
    private String lastUpdatedBy;
    @NotBlank
    @Column(nullable = false)
    private String createdById;

    @NotBlank
    @Column(nullable = false)
    private String lastUpdatedById;

    @Column(name = "CREATED_TS",nullable = false)
    @CreationTimestamp
    private LocalDateTime createdTimestamp;
    @UpdateTimestamp
    @Column(name = "MODIFIED_TS",nullable = false)
    private LocalDateTime modifiedTimestamp;
}
