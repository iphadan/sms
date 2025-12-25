package cbo.risk.sms.services.impl;

import cbo.risk.sms.dtos.CpoCreateDTO;
import cbo.risk.sms.dtos.CpoDTO;
import cbo.risk.sms.dtos.CpoUpdateDTO;
import cbo.risk.sms.exceptions.ResourceNotFoundException;
import cbo.risk.sms.exceptions.BusinessRuleException;
import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.Cpo;
import cbo.risk.sms.repositories.BookParentRepository;
import cbo.risk.sms.repositories.CpoRepository;
import cbo.risk.sms.services.CpoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CpoServiceImpl implements CpoService {

    private static final Logger log = LoggerFactory.getLogger(CpoServiceImpl.class);

    private final CpoRepository cpoRepository;
    private final BookParentRepository bookParentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CpoDTO create(CpoCreateDTO createDTO) {
        log.info("Creating new CPO: {}", createDTO);

        // Validate if serial number already exists
        if (cpoRepository.existsBySerialNumber(createDTO.getSerialNumber())) {
            throw new BusinessRuleException("CPO with serial number " +
                    createDTO.getSerialNumber() + " already exists");
        }

        // Validate BookParent exists if provided
        if (createDTO.getBookParentId() != null) {
            BookParent bookParent = bookParentRepository.findById(createDTO.getBookParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", createDTO.getBookParentId()));

            // Validate serial number is within parent's range
            validateSerialInRange(createDTO.getSerialNumber(), bookParent);

            // Check if parent already has maximum number of pads
            int currentChildCount = cpoRepository.countByBookParentId(createDTO.getBookParentId());
            if (currentChildCount >= bookParent.getNumOfPad()) {
                throw new BusinessRuleException(
                        String.format("BookParent already has maximum number of pads (%d)", bookParent.getNumOfPad()));
            }
        }

        Cpo cpo = modelMapper.map(createDTO, Cpo.class);

        // Set audit fields
        cpo.setCreatedBy(createDTO.getCreatedBy());
        cpo.setLastUpdatedBy(createDTO.getCreatedBy());

        // Set received date if not provided
        if (cpo.getReceivedDate() == null) {
            cpo.setReceivedDate(LocalDateTime.now());
        }

        // If BookParent ID is provided, fetch and set the entity
        if (createDTO.getBookParentId() != null) {
            BookParent bookParent = bookParentRepository.findById(createDTO.getBookParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", createDTO.getBookParentId()));
            cpo.setBookParent(bookParent);
        }

        Cpo saved = cpoRepository.save(cpo);
        log.info("CPO created with ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public List<CpoDTO> createCpoBatchWithParent(Long parentId, List<String> serialNumbers, String createdBy) {
        log.info("Creating CPO batch with parent ID: {}, {} serials", parentId, serialNumbers.size());

        BookParent bookParent = bookParentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", parentId));

        // Validate number of serials doesn't exceed parent's numOfPad
        int currentChildCount = cpoRepository.countByBookParentId(parentId);
        if (currentChildCount + serialNumbers.size() > bookParent.getNumOfPad()) {
            throw new BusinessRuleException(
                    String.format("Cannot add %d CPOs. Parent can only hold %d total, already has %d",
                            serialNumbers.size(), bookParent.getNumOfPad(), currentChildCount));
        }

        List<Cpo> cpos = serialNumbers.stream()
                .map(serial -> {
                    // Validate serial doesn't already exist
                    if (cpoRepository.existsBySerialNumber(serial)) {
                        throw new BusinessRuleException("CPO with serial number " + serial + " already exists");
                    }

                    // Validate serial is within parent's range
                    validateSerialInRange(serial, bookParent);

                    Cpo cpo = new Cpo();
                    cpo.setSerialNumber(serial);
                    cpo.setBookParent(bookParent);
                    cpo.setReceivedDate(LocalDateTime.now());
                    cpo.setCreatedBy(createdBy);
                    cpo.setLastUpdatedBy(createdBy);
                    cpo.setBranchId(bookParent.getBranchId());
                    cpo.setProcessId(bookParent.getProcessId());
                    cpo.setSubProcessId(bookParent.getSubProcessId());
//                    cpo.setNumOfPad(1); // Each CPO is one pad

                    return cpo;
                })
                .collect(Collectors.toList());

        List<Cpo> saved = cpoRepository.saveAll(cpos);
        log.info("Created {} CPOs for parent ID: {}", saved.size(), parentId);

        return saved.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CpoDTO> findById(Long id) {
        log.debug("Finding CPO by ID: {}", id);
        return cpoRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public List<CpoDTO> findAll() {
        log.debug("Fetching all CPOs");
        return cpoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CpoDTO> findAll(Pageable pageable) {
        log.debug("Fetching CPOs with pagination");
        return cpoRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CpoDTO update(Long id, CpoUpdateDTO updateDTO) {
        log.info("Updating CPO with ID: {}", id);

        Cpo cpo = cpoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CPO", "id", id));

        // Update fields
        if (updateDTO.getIssuedDate() != null) {
            // Validate if not already issued
            if (cpo.getIssuedDate() != null) {
                throw new BusinessRuleException("CPO is already issued");
            }
            cpo.setIssuedDate(updateDTO.getIssuedDate());
        }

        if (updateDTO.getReturnedDate() != null) {
            // Validate if issued but not returned
            if (cpo.getIssuedDate() == null) {
                throw new BusinessRuleException("CPO must be issued before it can be returned");
            }
            if (cpo.getReturnedDate() != null) {
                throw new BusinessRuleException("CPO is already returned");
            }
            cpo.setReturnedDate(updateDTO.getReturnedDate());
        }

//        if (updateDTO.getNumOfPad() > 0) {
//            cpo.setNumOfPad(updateDTO.getNumOfPad());
//        }

        // Update audit fields
        cpo.setLastUpdatedBy(updateDTO.getLastUpdatedBy());

        Cpo updated = cpoRepository.save(cpo);
        log.info("CPO updated: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting CPO with ID: {}", id);

        if (!cpoRepository.existsById(id)) {
            throw new ResourceNotFoundException("CPO", "id", id);
        }

        // Check if CPO is issued
        Cpo cpo = cpoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CPO", "id", id));

        if (cpo.getIssuedDate() != null && cpo.getReturnedDate() == null) {
            throw new BusinessRuleException("Cannot delete an issued CPO. Please return it first.");
        }

        // If CPO has a parent, we might want to decrement counts or handle differently
        // For now, just delete
        cpoRepository.deleteById(id);
        log.info("CPO deleted: {}", id);
    }

    @Override
    public Optional<CpoDTO> findBySerialNumber(String serialNumber) {
        log.debug("Finding CPO by serial number: {}", serialNumber);
        return cpoRepository.findBySerialNumber(serialNumber)
                .map(this::convertToDTO);
    }

    @Override
    public List<CpoDTO> findByBranchId(String branchId) {
        log.debug("Finding CPOs for branch: {}", branchId);
        return cpoRepository.findByBranchId(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CpoDTO> findByBookParentId(Long parentId) {
        log.debug("Finding CPOs for parent batch: {}", parentId);
        return cpoRepository.findByBookParentId(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CpoDTO issueItem(Long id, String issuedBy) {
        log.info("Issuing CPO with ID: {} by user: {}", id, issuedBy);

        Cpo cpo = cpoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CPO", "id", id));

        // Validate if not already issued
        if (cpo.getIssuedDate() != null) {
            throw new BusinessRuleException("CPO is already issued");
        }

        // Check sequential issuance rule if part of a parent batch
        if (cpo.getBookParent() != null) {
            validateSequentialIssuance(cpo);
        }

        cpo.setIssuedDate(LocalDateTime.now());
        cpo.setIssuedBy(issuedBy);
        cpo.setLastUpdatedBy(issuedBy);

        Cpo issued = cpoRepository.save(cpo);

        // Update parent's used count
        if (issued.getBookParent() != null) {
            BookParent parent = issued.getBookParent();
            parent.setUsed(parent.getUsed() + 1);
            parent.setLastUpdatedBy(issuedBy);
            bookParentRepository.save(parent);
            log.info("Parent used count updated to: {}", parent.getUsed());
        }

        log.info("CPO issued: {}", issued.getId());

        return convertToDTO(issued);
    }

    @Override
    @Transactional
    public CpoDTO returnItem(Long id, String returnedBy) {
        log.info("Returning CPO with ID: {} by user: {}", id, returnedBy);

        Cpo cpo = cpoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CPO", "id", id));

        // Validate if issued but not returned
        if (cpo.getIssuedDate() == null) {
            throw new BusinessRuleException("CPO is not issued");
        }
        if (cpo.getReturnedDate() != null) {
            throw new BusinessRuleException("CPO is already returned");
        }

        cpo.setReturnedDate(LocalDateTime.now());
        cpo.setLastUpdatedBy(returnedBy);

        Cpo returned = cpoRepository.save(cpo);

        // Update parent's used count (decrement)
        if (returned.getBookParent() != null) {
            BookParent parent = returned.getBookParent();
            parent.setUsed(parent.getUsed() - 1);
            if (parent.getUsed() < 0) {
                log.warn("Parent used count became negative, resetting to 0. Parent ID: {}", parent.getId());
                parent.setUsed(0);
            }
            parent.setLastUpdatedBy(returnedBy);
            bookParentRepository.save(parent);
            log.info("Parent used count updated to: {}", parent.getUsed());
        }

        log.info("CPO returned: {}", returned.getId());

        return convertToDTO(returned);
    }

    @Override
    @Transactional
    public CpoDTO receiveItem(Long id, String receivedBy) {
        log.info("Receiving CPO with ID: {} by user: {}", id, receivedBy);

        Cpo cpo = cpoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CPO", "id", id));

        // Validate if not received
        if (cpo.getReceivedDate() != null) {
            throw new BusinessRuleException("CPO is already received");
        }

        cpo.setReceivedDate(LocalDateTime.now());
        cpo.setLastUpdatedBy(receivedBy);

        Cpo received = cpoRepository.save(cpo);
        log.info("CPO received: {}", received.getId());

        return convertToDTO(received);
    }

    @Override
    public List<CpoDTO> findAvailableByBranch(String branchId) {
        log.debug("Finding available CPOs for branch: {}", branchId);
        return cpoRepository.findByBranchIdAndIssuedDateIsNull(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CpoDTO> findIssuedByBranch(String branchId) {
        log.debug("Finding issued CPOs for branch: {}", branchId);
        return cpoRepository.findByBranchIdAndIssuedDateIsNotNull(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countByBranchAndStatus(String branchId, String status) {
        log.debug("Counting CPOs for branch: {} with status: {}", branchId, status);
        switch (status.toLowerCase()) {
            case "available":
                return cpoRepository.countByBranchIdAndIssuedDateIsNull(branchId);
            case "issued":
                return cpoRepository.countByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(branchId);
            case "returned":
                return cpoRepository.countByBranchIdAndReturnedDateIsNotNull(branchId);
            default:
                return cpoRepository.countByBranchId(branchId);
        }
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return cpoRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    public List<CpoDTO> findByBranchIdAndStatus(String branchId, String status) {
        log.debug("Finding CPOs for branch: {} with status: {}", branchId, status);
        switch (status.toLowerCase()) {
            case "available":
                return cpoRepository.findByBranchIdAndIssuedDateIsNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            case "issued":
                return cpoRepository.findByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            case "returned":
                return cpoRepository.findByBranchIdAndReturnedDateIsNotNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            default:
                return cpoRepository.findByBranchId(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
        }
    }

    @Override
    public Page<CpoDTO> findByBranchId(String branchId, Pageable pageable) {
        log.debug("Finding CPOs for branch with pagination: {}", branchId);
        return cpoRepository.findByBranchId(branchId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public List<CpoDTO> createBatch(List<CpoCreateDTO> createDTOs) {
        log.info("Batch creating {} CPOs", createDTOs.size());

        List<Cpo> cpos = createDTOs.stream()
                .map(dto -> {
                    // Validate serial number
                    if (cpoRepository.existsBySerialNumber(dto.getSerialNumber())) {
                        throw new BusinessRuleException(
                                "CPO with serial number " + dto.getSerialNumber() + " already exists");
                    }

                    Cpo cpo = modelMapper.map(dto, Cpo.class);
                    cpo.setCreatedBy(dto.getCreatedBy());
                    cpo.setLastUpdatedBy(dto.getCreatedBy());

                    if (cpo.getReceivedDate() == null) {
                        cpo.setReceivedDate(LocalDateTime.now());
                    }

                    // Set BookParent if provided
                    if (dto.getBookParentId() != null) {
                        BookParent bookParent = bookParentRepository.findById(dto.getBookParentId())
                                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", dto.getBookParentId()));
                        cpo.setBookParent(bookParent);
                        validateSerialInRange(dto.getSerialNumber(), bookParent);
                    }

                    return cpo;
                })
                .collect(Collectors.toList());

        List<Cpo> saved = cpoRepository.saveAll(cpos);
        log.info("Batch created {} CPOs", saved.size());

        return saved.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ============== HELPER METHODS ==============

    private void validateSerialInRange(String serialNumber, BookParent bookParent) {
        try {
            String prefix = bookParent.getStartingSerial().replaceAll("\\d+", "");
            String serialNumStr = serialNumber.substring(prefix.length());
            String startNumStr = bookParent.getStartingSerial().substring(prefix.length());
            String endNumStr = bookParent.getEndingSerial().substring(prefix.length());

            int serialNum = Integer.parseInt(serialNumStr);
            int startNum = Integer.parseInt(startNumStr);
            int endNum = Integer.parseInt(endNumStr);

            if (serialNum < startNum || serialNum > endNum) {
                throw new BusinessRuleException(
                        String.format("Serial number %s is not in range %s to %s",
                                serialNumber, bookParent.getStartingSerial(), bookParent.getEndingSerial()));
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new BusinessRuleException(
                    String.format("Invalid serial number format: %s. Expected format matching parent range: %s-%s",
                            serialNumber, bookParent.getStartingSerial(), bookParent.getEndingSerial()));
        }
    }

    private void validateSequentialIssuance(Cpo cpo) {
        BookParent parent = cpo.getBookParent();
        List<Cpo> allCpos = cpoRepository.findByBookParentOrderBySerialNumberAsc(parent);

        int currentIndex = -1;
        for (int i = 0; i < allCpos.size(); i++) {
            if (allCpos.get(i).getSerialNumber().equals(cpo.getSerialNumber())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            Cpo previousCpo = allCpos.get(currentIndex - 1);
            if (previousCpo.getIssuedDate() == null) {
                throw new BusinessRuleException(
                        String.format("Cannot issue serial %s. Previous serial %s must be issued first.",
                                cpo.getSerialNumber(), previousCpo.getSerialNumber())
                );
            }
        }
    }

    private CpoDTO convertToDTO(Cpo cpo) {
        CpoDTO dto = modelMapper.map(cpo, CpoDTO.class);

        // Map BookParent ID if exists
        if (cpo.getBookParent() != null) {
            dto.setBookParentId(cpo.getBookParent().getId());
            // Also include parent stats
            dto.setParentNumOfPad(cpo.getBookParent().getNumOfPad());
            dto.setParentUsed(cpo.getBookParent().getUsed());
            dto.setParentAvailable(cpo.getBookParent().getNumOfPad() - cpo.getBookParent().getUsed());
        }

        return dto;
    }
}