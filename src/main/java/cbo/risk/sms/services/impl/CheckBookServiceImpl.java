package cbo.risk.sms.services.impl;

import cbo.risk.sms.dtos.CheckBookCreateDTO;
import cbo.risk.sms.dtos.CheckBookDTO;
import cbo.risk.sms.dtos.CheckBookUpdateDTO;
import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import cbo.risk.sms.exceptions.ResourceNotFoundException;
import cbo.risk.sms.exceptions.BusinessRuleException;
import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.CheckBook;
import cbo.risk.sms.repositories.BookParentRepository;
import cbo.risk.sms.repositories.CheckBookRepository;
import cbo.risk.sms.services.CheckBookService;
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
public class CheckBookServiceImpl implements CheckBookService {

    private static final Logger log = LoggerFactory.getLogger(CheckBookServiceImpl.class);

    private final CheckBookRepository checkBookRepository;
    private final BookParentRepository bookParentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CheckBookDTO create(CheckBookCreateDTO createDTO) {
        log.info("Creating new CheckBook: {}", createDTO);

        // Validate if serial number already exists
        if (checkBookRepository.existsBySerialNumber(createDTO.getSerialNumber())) {
            throw new BusinessRuleException("CheckBook with serial number " +
                    createDTO.getSerialNumber() + " already exists");
        }

        // Validate BookParent exists if provided
        if (createDTO.getBookParentId() != null) {
            BookParent bookParent = bookParentRepository.findById(createDTO.getBookParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", createDTO.getBookParentId()));

            // Validate serial number is within parent's range
            validateSerialInRange(createDTO.getSerialNumber(), bookParent);
        }

        CheckBook checkBook = modelMapper.map(createDTO, CheckBook.class);

        // Set audit fields
        checkBook.setCreatedBy(createDTO.getCreatedBy());
        checkBook.setLastUpdatedBy(createDTO.getCreatedBy());

        // Set received date if not provided
        if (checkBook.getReceivedDate() == null) {
            checkBook.setReceivedDate(LocalDateTime.now());
        }

        // If BookParent ID is provided, fetch and set the entity
        if (createDTO.getBookParentId() != null) {
            BookParent bookParent = bookParentRepository.findById(createDTO.getBookParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", createDTO.getBookParentId()));
            checkBook.setBookParent(bookParent);
        }

        CheckBook saved = checkBookRepository.save(checkBook);
        log.info("CheckBook created with ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Override
    public Optional<CheckBookDTO> findById(Long id) {
        log.debug("Finding CheckBook by ID: {}", id);
        return checkBookRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public List<CheckBookDTO> findAll() {
        log.debug("Fetching all CheckBooks");
        return checkBookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CheckBookDTO> findAll(Pageable pageable) {
        log.debug("Fetching CheckBooks with pagination");
        return checkBookRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CheckBookDTO update(Long id, CheckBookUpdateDTO updateDTO) {
        log.info("Updating CheckBook with ID: {}", id);

        CheckBook checkBook = checkBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckBook", "id", id));

        // Update fields
        if (updateDTO.getIssuedDate() != null) {
            // Validate if not already issued
            if (checkBook.getIssuedDate() != null) {
                throw new BusinessRuleException("CheckBook is already issued");
            }
            checkBook.setIssuedDate(updateDTO.getIssuedDate());
        }

        if (updateDTO.getReturnedDate() != null) {
            // Validate if issued but not returned
            if (checkBook.getIssuedDate() == null) {
                throw new BusinessRuleException("CheckBook must be issued before it can be returned");
            }
            if (checkBook.getReturnedDate() != null) {
                throw new BusinessRuleException("CheckBook is already returned");
            }
            checkBook.setReturnedDate(updateDTO.getReturnedDate());
        }

//        if (updateDTO.getNumOfPad() > 0) {
//            checkBook.setNumOfPad(updateDTO.getNumOfPad());
//        }

        // Update audit fields
        checkBook.setLastUpdatedBy(updateDTO.getLastUpdatedBy());

        CheckBook updated = checkBookRepository.save(checkBook);
        log.info("CheckBook updated: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting CheckBook with ID: {}", id);

        if (!checkBookRepository.existsById(id)) {
            throw new ResourceNotFoundException("CheckBook", "id", id);
        }

        // Check if checkbook is issued
        CheckBook checkBook = checkBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckBook", "id", id));

        if (checkBook.getIssuedDate() != null && checkBook.getReturnedDate() == null) {
            throw new BusinessRuleException("Cannot delete an issued CheckBook. Please return it first.");
        }

        checkBookRepository.deleteById(id);
        log.info("CheckBook deleted: {}", id);
    }

    @Override
    public Optional<CheckBookDTO> findBySerialNumber(String serialNumber) {
        log.debug("Finding CheckBook by serial number: {}", serialNumber);
        return checkBookRepository.findBySerialNumber(serialNumber)
                .map(this::convertToDTO);
    }

    @Override
    public List<CheckBookDTO> findByBranchId(String branchId) {
        log.debug("Finding CheckBooks for branch: {}", branchId);
        return checkBookRepository.findByBranchId(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CheckBookDTO> findByBookParentId(Long parentId) {
        log.debug("Finding CheckBooks for parent batch: {}", parentId);
        return checkBookRepository.findByBookParentId(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CheckBookDTO issueItem(Long id, String issuedBy) {
        log.info("Issuing CheckBook with ID: {} by user: {}", id, issuedBy);

        CheckBook checkBook = checkBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckBook", "id", id));

        // Validate if not already issued
        if (checkBook.getIssuedDate() != null) {
            throw new BusinessRuleException("CheckBook is already issued");
        }

        // Check sequential issuance rule if part of a parent batch
        if (checkBook.getBookParent() != null) {
            validateSequentialIssuance(checkBook);
        }

        checkBook.setIssuedDate(LocalDateTime.now());
        checkBook.setIssuedBy(issuedBy);
        checkBook.setLastUpdatedBy(issuedBy);

        CheckBook issued = checkBookRepository.save(checkBook);

        // Update parent's used count
        if (issued.getBookParent() != null) {
            BookParent parent = issued.getBookParent();
            parent.setUsed(parent.getUsed() + 1);
            parent.setLastUpdatedBy(issuedBy);
            bookParentRepository.save(parent);
        }

        log.info("CheckBook issued: {}", issued.getId());

        return convertToDTO(issued);
    }

    @Override
    @Transactional
    public CheckBookDTO returnItem(Long id, String returnedBy) {
        log.info("Returning CheckBook with ID: {} by user: {}", id, returnedBy);

        CheckBook checkBook = checkBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckBook", "id", id));

        // Validate if issued but not returned
        if (checkBook.getIssuedDate() == null) {
            throw new BusinessRuleException("CheckBook is not issued");
        }
        if (checkBook.getReturnedDate() != null) {
            throw new BusinessRuleException("CheckBook is already returned");
        }

        checkBook.setReturnedDate(LocalDateTime.now());
        checkBook.setLastUpdatedBy(returnedBy);

        CheckBook returned = checkBookRepository.save(checkBook);

        // Update parent's used count
        if (returned.getBookParent() != null) {
            BookParent parent = returned.getBookParent();
            parent.setUsed(parent.getUsed() - 1);
            if (parent.getUsed() < 0) parent.setUsed(0);
            parent.setLastUpdatedBy(returnedBy);
            bookParentRepository.save(parent);
        }

        log.info("CheckBook returned: {}", returned.getId());

        return convertToDTO(returned);
    }

    @Override
    @Transactional
    public CheckBookDTO receiveItem(Long id, String receivedBy) {
        log.info("Receiving CheckBook with ID: {} by user: {}", id, receivedBy);

        CheckBook checkBook = checkBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckBook", "id", id));

        // Validate if not received
        if (checkBook.getReceivedDate() != null) {
            throw new BusinessRuleException("CheckBook is already received");
        }

        checkBook.setReceivedDate(LocalDateTime.now());
        checkBook.setLastUpdatedBy(receivedBy);

        CheckBook received = checkBookRepository.save(checkBook);
        log.info("CheckBook received: {}", received.getId());

        return convertToDTO(received);
    }

    @Override
    public List<CheckBookDTO> findAvailableByBranch(String branchId) {
        log.debug("Finding available CheckBooks for branch: {}", branchId);
        return checkBookRepository.findByBranchIdAndIssuedDateIsNull(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CheckBookDTO> findIssuedByBranch(String branchId) {
        log.debug("Finding issued CheckBooks for branch: {}", branchId);
        return checkBookRepository.findByBranchIdAndIssuedDateIsNotNull(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countByBranchAndStatus(String branchId, String status) {
        log.debug("Counting CheckBooks for branch: {} with status: {}", branchId, status);
        switch (status.toLowerCase()) {
            case "available":
                return checkBookRepository.countByBranchIdAndIssuedDateIsNull(branchId);
            case "issued":
                return checkBookRepository.countByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(branchId);
            case "returned":
                return checkBookRepository.countByBranchIdAndReturnedDateIsNotNull(branchId);
            default:
                return checkBookRepository.countByBranchId(branchId);
        }
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return checkBookRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    public List<CheckBookDTO> findByBranchIdAndStatus(String branchId, String status) {
        log.debug("Finding CheckBooks for branch: {} with status: {}", branchId, status);
        switch (status.toLowerCase()) {
            case "available":
                return checkBookRepository.findByBranchIdAndIssuedDateIsNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            case "issued":
                return checkBookRepository.findByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            case "returned":
                return checkBookRepository.findByBranchIdAndReturnedDateIsNotNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            default:
                return checkBookRepository.findByBranchId(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
        }
    }

    @Override
    public Page<CheckBookDTO> findByBranchId(String branchId, Pageable pageable) {
        log.debug("Finding CheckBooks for branch with pagination: {}", branchId);
        return checkBookRepository.findByBranchId(branchId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public List<CheckBookDTO> createBatch(List<CheckBookCreateDTO> createDTOs) {
        log.info("Batch creating {} CheckBooks", createDTOs.size());

        List<CheckBook> checkBooks = createDTOs.stream()
                .map(dto -> {
                    // Validate serial number
                    if (checkBookRepository.existsBySerialNumber(dto.getSerialNumber())) {
                        throw new BusinessRuleException(
                                "CheckBook with serial number " + dto.getSerialNumber() + " already exists");
                    }

                    CheckBook checkBook = modelMapper.map(dto, CheckBook.class);
                    checkBook.setCreatedBy(dto.getCreatedBy());
                    checkBook.setLastUpdatedBy(dto.getCreatedBy());

                    if (checkBook.getReceivedDate() == null) {
                        checkBook.setReceivedDate(LocalDateTime.now());
                    }

                    // Set BookParent if provided
                    if (dto.getBookParentId() != null) {
                        BookParent bookParent = bookParentRepository.findById(dto.getBookParentId())
                                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", dto.getBookParentId()));
                        checkBook.setBookParent(bookParent);
                        validateSerialInRange(dto.getSerialNumber(), bookParent);
                    }

                    return checkBook;
                })
                .collect(Collectors.toList());

        List<CheckBook> saved = checkBookRepository.saveAll(checkBooks);
        log.info("Batch created {} CheckBooks", saved.size());

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
        } catch (NumberFormatException e) {
            throw new BusinessRuleException("Invalid serial number format: " + serialNumber);
        }
    }

    private void validateSequentialIssuance(CheckBook checkBook) {
        BookParent parent = checkBook.getBookParent();
        List<CheckBook> allBooks = checkBookRepository.findByBookParentOrderBySerialNumberAsc(parent);

        int currentIndex = -1;
        for (int i = 0; i < allBooks.size(); i++) {
            if (allBooks.get(i).getSerialNumber().equals(checkBook.getSerialNumber())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            CheckBook previousBook = allBooks.get(currentIndex - 1);
            if (previousBook.getIssuedDate() == null) {
                throw new BusinessRuleException(
                        String.format("Cannot issue serial %s. Previous serial %s must be issued first.",
                                checkBook.getSerialNumber(), previousBook.getSerialNumber())
                );
            }
        }
    }

    private CheckBookDTO convertToDTO(CheckBook checkBook) {
        CheckBookDTO dto = modelMapper.map(checkBook, CheckBookDTO.class);

        // Map BookParent ID if exists
        if (checkBook.getBookParent() != null) {
            dto.setBookParentId(checkBook.getBookParent().getId());
        }

        return dto;
    }
}