package cbo.risk.sms.models;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
@Data
@Entity
@Audited
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Cpo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_parent_id", nullable = false)
    private BookParent bookParent;

    @NotBlank
    @Column(name = "BRANCH_ID")
    private String branchId;

    @NotBlank
    @Column(nullable = false)
    private String serialNumber;
    private LocalDateTime receivedDate;
    private LocalDateTime issuedDate;
    private LocalDateTime returnedDate;
    private String issuedBy;
    private String receivedBy;
    private String issuedById;
    private String receivedById;
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
