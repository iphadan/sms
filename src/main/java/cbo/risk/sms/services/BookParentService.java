package cbo.risk.sms.services;

import cbo.risk.sms.dtos.BatchRegistrationDTO;
import cbo.risk.sms.dtos.BatchResponseDTO;
import cbo.risk.sms.dtos.IssueRequestDTO;
import cbo.risk.sms.dtos.ReturnRequestDTO;
import cbo.risk.sms.models.BookParent;

import java.util.List;
import java.util.Optional;

public interface BookParentService {

    // Batch Registration
    BatchResponseDTO registerCheckBookBatch(BatchRegistrationDTO registrationDTO);
    BatchResponseDTO registerCpoBatch(BatchRegistrationDTO registrationDTO);
    BatchResponseDTO registerPassBookBatch(BatchRegistrationDTO registrationDTO);

    // CRUD Operations
    Optional<BookParent> findById(Long id);
    List<BookParent> findAll();
    List<BookParent> findByBranchId(String branchId);
    void delete(Long id);

    // Business Operations
    BatchResponseDTO issueBook(IssueRequestDTO issueRequest);
    BatchResponseDTO returnBook(ReturnRequestDTO returnRequest);

    // Query Operations
    Optional<BookParent> findBySerialRange(String serialNumber);
    boolean isSerialIssuable(String serialNumber, String branchId);
    String getNextIssuableSerial(String branchId, String bookType);

    // Status Methods
    int getAvailableCount(Long parentId);
    int getUsedCount(Long parentId);
    boolean isBatchComplete(Long parentId); // All pads used
}