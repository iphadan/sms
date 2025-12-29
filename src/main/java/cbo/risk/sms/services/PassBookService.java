package cbo.risk.sms.services;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PassBookService {
    ResponseDTO<RequestPassBookDTO> issueAvailablePassBook(RequestPassBookDTO request);

    // CRUD Operations
    PassBookDTO create(PassBookCreateDTO createDTO);
    Optional<PassBookDTO> findById(Long id);
    List<PassBookDTO> findAll();
    Page<PassBookDTO> findAll(Pageable pageable);
    PassBookDTO update(Long id, PassBookUpdateDTO updateDTO);
    void delete(Long id);

    // Find by specific criteria
    Optional<PassBookDTO> findBySerialNumber(String serialNumber);
    List<PassBookDTO> findByBranchId(String branchId);
    List<PassBookDTO> findByBookParentId(Long parentId);

    // Business operations
    PassBookDTO issueItem(Long id, String issuedBy);
    PassBookDTO returnItem(Long id, String returnedBy);
    PassBookDTO receiveItem(Long id, String receivedBy);
    RequestPassBookDTO receiveItem(RequestPassBookDTO requestPassBookDTO);


    // Status-based queries
    List<PassBookDTO> findAvailableByBranch(String branchId);
    List<PassBookDTO> findIssuedByBranch(String branchId);

    // Statistics
    long countByBranchAndStatus(String branchId, String status);

    // Additional utility methods
    boolean existsBySerialNumber(String serialNumber);
    List<PassBookDTO> findByBranchIdAndStatus(String branchId, String status);
    Page<PassBookDTO> findByBranchId(String branchId, Pageable pageable);

    // Type and category based queries
    List<PassBookDTO> findByPassBookType(PassBookType type);
    List<PassBookDTO> findByPassBookCategory(PassBookCategory category);
    List<PassBookDTO> findByPassBookTypeAndCategory(PassBookType type, PassBookCategory category);

    // Batch operations
    List<PassBookDTO> createBatch(List<PassBookCreateDTO> createDTOs);

    // Parent book operations
    List<PassBookDTO> createPassBookBatchWithParent(Long parentId, List<String> serialNumbers,
                                                    PassBookType type, PassBookCategory category, String createdBy);
}