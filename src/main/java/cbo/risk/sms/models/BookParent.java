package cbo.risk.sms.models;

import cbo.risk.sms.enums.BookType;
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

    private int used = 0;           // Number of pads that have been issued (not returned)
    @Column(nullable = false)
private BookType bookType;
    // available = numOfPad - used

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