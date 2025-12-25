package cbo.risk.sms.models;


import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Audited



public class PassBook {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
   private PassBookType passBookType;
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PassBookCategory passBookCategory;
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "book_parent_id", nullable = false)
 private BookParent bookParent;
 @NotBlank
 @Column(nullable = false)
 private String serialNumber;
 private LocalDateTime receivedDate;
 private LocalDateTime issuedDate;
 private LocalDateTime returnedDate;
 private String issuedBy;
 private String receivedBy;
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

    public void setId(Long id) {
  this.id = id;
 }

    public void setPassBookType(PassBookType passBookType) {
  this.passBookType = passBookType;
 }

    public void setPassBookCategory(PassBookCategory passBookCategory) {
  this.passBookCategory = passBookCategory;
 }

    public void setBookParent(BookParent bookParent) {
  this.bookParent = bookParent;
 }

    public void setSerialNumber(String serialNumber) {
  this.serialNumber = serialNumber;
 }

    public void setReceivedDate(LocalDateTime receivedDate) {
  this.receivedDate = receivedDate;
 }

    public void setIssuedDate(LocalDateTime issuedDate) {
  this.issuedDate = issuedDate;
 }

    public void setReturnedDate(LocalDateTime returnedDate) {
  this.returnedDate = returnedDate;
 }

    public void setIssuedBy(String issuedBy) {
  this.issuedBy = issuedBy;
 }

    public void setReceivedBy(String receivedBy) {
  this.receivedBy = receivedBy;
 }

    public void setBranchId(String branchId) {
  this.branchId = branchId;
 }

    public void setSubProcessId(String subProcessId) {
  this.subProcessId = subProcessId;
 }

    public void setProcessId(String processId) {
  this.processId = processId;
 }

    public void setCreatedBy(String createdBy) {
  this.createdBy = createdBy;
 }

    public void setLastUpdatedBy(String lastUpdatedBy) {
  this.lastUpdatedBy = lastUpdatedBy;
 }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
  this.createdTimestamp = createdTimestamp;
 }

    public void setModifiedTimestamp(LocalDateTime modifiedTimestamp) {
  this.modifiedTimestamp = modifiedTimestamp;
 }
}
