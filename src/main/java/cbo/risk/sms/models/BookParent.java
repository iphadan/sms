package cbo.risk.sms.models;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import cbo.risk.sms.enums.ParentBookType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;


@Entity
@Data
@Audited

public class BookParent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String startingSerial;  // First serial in the batch range
    @NotBlank
    private String endingSerial;    // Last serial in the batch range

@Column(nullable = false)

    private int numOfPad;           // Total number of pads in this batch

    private int used = 0;// Number of pads that have been issued (not returned)

    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private ParentBookType parentBookType;
    // available = numOfPad - used
    @Column(name = "finished")
private boolean finished = false;
    @NotBlank
    @Column(name = "BRANCH_ID")
    private String branchId;
private Long lastIssuedChild;
private CheckBookLeaveType checkLeaveType;
private String passCheckType;
private String passBookType;
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
    private String createdById;


    @NotBlank
    @Column(nullable = false)
    private String lastUpdatedBy;
    @NotBlank
    @Column(nullable = false)
    private String lastUpdatedById;

    @Column(name = "CREATED_TS", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @UpdateTimestamp
    @Column(name = "MODIFIED_TS", nullable = false)
    private LocalDateTime modifiedTimestamp;

    // Optional: For easier querying
    private LocalDateTime batchReceivedDate;

    // Helper method
    public int getAvailablePads() {
        return numOfPad - used;
    }
}