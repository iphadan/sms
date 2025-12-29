package cbo.risk.sms.models;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data

@Audited

@Entity
public class CheckBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckBookType checkBookType;
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckBookLeaveType checkBookLeaveType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_parent_id", nullable = false)
    private BookParent bookParent;




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
    @NotBlank
    @Column(nullable = false)
    private String startSerialNumber;
    @NotBlank
    @Column(nullable = false)
    private String endSerialNumber;

    private LocalDateTime receivedDate;
    private String issuedBy;
    private String receivedBy;
    private String issuedById;
    private String receivedById;
    private LocalDateTime issuedDate;
    private LocalDateTime returnedDate;



}