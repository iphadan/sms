package cbo.risk.sms.models;


import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Data
@Audited
public class PassBook {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
   private PassBookType passBookType;
    @NotBlank
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PassBookCategory passBookCategory;
    private String startingSerialNum;
    private String endSerialNum;
    @NotBlank
    @Column(nullable = false)
    private int numOfPad;

 @NotBlank
 @Column(name = "BRANCH_ID")
 private String branchId;
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
 @Column(name = "CREATED_TS",nullable = false)
 @CreationTimestamp
 private LocalDateTime createdTimestamp;
 @UpdateTimestamp
 @Column(name = "MODIFIED_TS",nullable = false)
 private LocalDateTime modifiedTimestamp;



}
