package cbo.risk.sms.models;

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
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookParent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String startingSerial;  // First serial in the batch range
    @NotBlank
    private String endingSerial;    // Last serial in the batch range

    @NotBlank
    @Column(nullable = false)
    private int numOfPad;           // Total number of pads in this batch

    private int used = 0;           // Number of pads that have been issued (not returned)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartingSerial() {
        return startingSerial;
    }

    public void setStartingSerial(String startingSerial) {
        this.startingSerial = startingSerial;
    }

    public String getEndingSerial() {
        return endingSerial;
    }

    public void setEndingSerial(String endingSerial) {
        this.endingSerial = endingSerial;
    }

    public int getNumOfPad() {
        return numOfPad;
    }

    public void setNumOfPad(int numOfPad) {
        this.numOfPad = numOfPad;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getSubProcessId() {
        return subProcessId;
    }

    public void setSubProcessId(String subProcessId) {
        this.subProcessId = subProcessId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setModifiedTimestamp(LocalDateTime modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    public LocalDateTime getBatchReceivedDate() {
        return batchReceivedDate;
    }

    public void setBatchReceivedDate(LocalDateTime batchReceivedDate) {
        this.batchReceivedDate = batchReceivedDate;
    }

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