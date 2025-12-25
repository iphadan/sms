package cbo.risk.sms.services;

import cbo.risk.sms.dtos.CheckBookCreateDTO;
import cbo.risk.sms.dtos.CheckBookDTO;
import cbo.risk.sms.dtos.CheckBookUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CheckBookService {

    // CRUD Operations
    CheckBookDTO create(CheckBookCreateDTO createDTO);
    Optional<CheckBookDTO> findById(Long id);
    List<CheckBookDTO> findAll();
    Page<CheckBookDTO> findAll(Pageable pageable);
    CheckBookDTO update(Long id, CheckBookUpdateDTO updateDTO);
    void delete(Long id);

    // Find by specific criteria
    Optional<CheckBookDTO> findBySerialNumber(String serialNumber);
    List<CheckBookDTO> findByBranchId(String branchId);
    List<CheckBookDTO> findByBookParentId(Long parentId);

    // Business operations
    CheckBookDTO issueItem(Long id, String issuedBy);
    CheckBookDTO returnItem(Long id, String returnedBy);
    CheckBookDTO receiveItem(Long id, String receivedBy);

    // Status-based queries
    List<CheckBookDTO> findAvailableByBranch(String branchId);
    List<CheckBookDTO> findIssuedByBranch(String branchId);

    // Statistics
    long countByBranchAndStatus(String branchId, String status);

    // Additional utility methods
    boolean existsBySerialNumber(String serialNumber);
    List<CheckBookDTO> findByBranchIdAndStatus(String branchId, String status);
    Page<CheckBookDTO> findByBranchId(String branchId, Pageable pageable);

    // Batch operations
    List<CheckBookDTO> createBatch(List<CheckBookCreateDTO> createDTOs);
}