package cbo.risk.sms.services;

import cbo.risk.sms.dtos.CpoCreateDTO;
import cbo.risk.sms.dtos.CpoDTO;
import cbo.risk.sms.dtos.CpoUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CpoService {

    // CRUD Operations
    CpoDTO create(CpoCreateDTO createDTO);
    Optional<CpoDTO> findById(Long id);
    List<CpoDTO> findAll();
    Page<CpoDTO> findAll(Pageable pageable);
    CpoDTO update(Long id, CpoUpdateDTO updateDTO);
    void delete(Long id);

    // Find by specific criteria
    Optional<CpoDTO> findBySerialNumber(String serialNumber);
    List<CpoDTO> findByBranchId(String branchId);
    List<CpoDTO> findByBookParentId(Long parentId);

    // Business operations
    CpoDTO issueItem(Long id, String issuedBy);
    CpoDTO returnItem(Long id, String returnedBy);
    CpoDTO receiveItem(Long id, String receivedBy);

    // Status-based queries
    List<CpoDTO> findAvailableByBranch(String branchId);
    List<CpoDTO> findIssuedByBranch(String branchId);

    // Statistics
    long countByBranchAndStatus(String branchId, String status);

    // Additional utility methods
    boolean existsBySerialNumber(String serialNumber);
    List<CpoDTO> findByBranchIdAndStatus(String branchId, String status);
    Page<CpoDTO> findByBranchId(String branchId, Pageable pageable);

    // Batch operations
    List<CpoDTO> createBatch(List<CpoCreateDTO> createDTOs);

    // Parent book operations
    List<CpoDTO> createCpoBatchWithParent(Long parentId, List<String> serialNumbers, String createdBy);
}