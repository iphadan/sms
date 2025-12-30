package cbo.risk.sms.services.impl;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.ParentBookType;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import cbo.risk.sms.exceptions.ResourceNotFoundException;
import cbo.risk.sms.exceptions.BusinessRuleException;
import cbo.risk.sms.models.*;
import cbo.risk.sms.repositories.BookParentRepository;
import cbo.risk.sms.repositories.PassBookRepository;
import cbo.risk.sms.repositories.RequestPassBookRepository;
import cbo.risk.sms.services.PassBookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassBookServiceImpl implements PassBookService {

    private static final Logger log = LoggerFactory.getLogger(PassBookServiceImpl.class);

    private final PassBookRepository passBookRepository;
    private final BookParentRepository bookParentRepository;
    private final ModelMapper modelMapper;
    private final RequestPassBookRepository requestPassBookRepository;

    @Transactional
    public ResponseDTO<RequestPassBookDTO> issueAvailablePassBook(RequestPassBookDTO request) {

        log.info("Issuing PassBook for branch: {}, account: {}, issued by: {}",
                request.getBranchId(),
                request.getAccountNumber(),
                request.getCreatedBy());

        // 1. Find next available passbook
        PassBook availablePassBook = findAndReserveNextAvailablePassBook(request);
        System.out.println(1);
        // 2. Create request record
        RequestPassBook requestPassBook = new RequestPassBook();
        requestPassBook.setPassBookId(availablePassBook.getId());
        requestPassBook.setPassBookCategory(availablePassBook.getPassBookCategory());
        requestPassBook.setPassBookType(availablePassBook.getPassBookType());
        requestPassBook.setSerialNum(availablePassBook.getSerialNumber());
        requestPassBook.setBranchId(request.getBranchId());
        requestPassBook.setAccountNumber(request.getAccountNumber());
        requestPassBook.setProcessId(request.getProcessId());
        requestPassBook.setIssuedById(request.getIssuedById());
        requestPassBook.setIssuedBy(request.getIssuedBy());
        requestPassBook.setLastUpdatedById(request.getLastUpdatedById());
        requestPassBook.setLastUpdatedBy(request.getLastUpdatedBy());
        requestPassBook.setCreatedBy(request.getCreatedBy());
        requestPassBook.setCreatedById(request.getCreatedById());
        requestPassBook.setCreatedTimestamp(LocalDateTime.now());
        requestPassBook.setIssuedDate(LocalDateTime.now());
        requestPassBook.setModifiedTimestamp(LocalDateTime.now());
        requestPassBook.setSubProcessId(request.getSubProcessId());
        System.out.println(2);
        RequestPassBook savedRequest =
                requestPassBookRepository.save(requestPassBook);
        System.out.println(3);
        // 3. Update passbook status
        availablePassBook.setIssuedById(request.getIssuedById());
        availablePassBook.setIssuedBy(request.getIssuedBy());
        availablePassBook.setLastUpdatedBy(request.getLastUpdatedBy());
        availablePassBook.setLastUpdatedById(request.getLastUpdatedById());
        availablePassBook.setCreatedBy(request.getCreatedBy());
        availablePassBook.setCreatedById(request.getCreatedById());
        availablePassBook.setIssuedBy(request.getIssuedBy());
        availablePassBook.setIssuedById(request.getIssuedById());
        availablePassBook.setCreatedTimestamp(LocalDateTime.now());
        availablePassBook.setIssuedDate(LocalDateTime.now());
        availablePassBook.setModifiedTimestamp(LocalDateTime.now());
        System.out.println(4);
        passBookRepository.save(availablePassBook);
        System.out.println(5);
        BookParent parent = availablePassBook.getBookParent();
        parent.setUsed(parent.getUsed() + 1);
        parent.setLastIssuedChild(availablePassBook.getId());
        bookParentRepository.save(parent);

        log.info("PassBook issued - Request ID: {}, PassBook ID: {}, Serial: {}",
                savedRequest.getId(),
                availablePassBook.getId(),
                availablePassBook.getSerialNumber());

        return createIssueResponse(savedRequest, availablePassBook);
    }
    private ResponseDTO<RequestPassBookDTO> createIssueResponse(
            RequestPassBook requestPassBook,
            PassBook passBook) {

        RequestPassBookDTO dto = new RequestPassBookDTO();

        dto.setId(requestPassBook.getId());
        dto.setPassBookId(passBook.getId());
        dto.setSerialNum(requestPassBook.getSerialNum());
        dto.setBranchId(requestPassBook.getBranchId());
        dto.setAccountNumber(requestPassBook.getAccountNumber());
        dto.setProcessId(requestPassBook.getProcessId());
        dto.setSubProcessId(requestPassBook.getSubProcessId());
        dto.setCreatedBy(requestPassBook.getCreatedBy());
        dto.setLastUpdatedBy(requestPassBook.getLastUpdatedBy());
        dto.setIssuedDate(requestPassBook.getIssuedDate());
        dto.setCreatedTimestamp(requestPassBook.getCreatedTimestamp());
        dto.setModifiedTimestamp(requestPassBook.getModifiedTimestamp());
ResponseDTO<RequestPassBookDTO> responseDTO = new ResponseDTO<>();
responseDTO.setResult(dto);
responseDTO.setStatus(true);
responseDTO.setMessage("PassBook issued successfully");
        return responseDTO;
    }

    private PassBook findAndReserveNextAvailablePassBook(RequestPassBookDTO request) {

        Optional<BookParent>  availableParent = bookParentRepository.findAvailablePassBookParent(
                request.getBranchId(), request.getPassBookType().name(), ParentBookType.PASS_BOOK);
        if(availableParent.isPresent()) {

        List<PassBook> passBooks = passBookRepository
                .findByBranchIdOrderBySerialNumberAsc(request.getBranchId());



        passBooks.sort(Comparator.comparingInt(this::extractPassBookNumber));

        for (PassBook passBook : passBooks) {

            // Available = not yet issued
            if (passBook.getIssuedDate() == null) {

                // Sequential rule
                if (isSequentiallyIssuable(passBook, passBooks)) {
                    return passBook;
                }
            }
        }
        }

        throw new BusinessRuleException(
                String.format("No available PassBooks found for branch %s",
                        request.getBranchId()));
    }
    private int extractPassBookNumber(PassBook passBook) {
        // Examples: pas1, pas10, pas003
        String serial = passBook.getSerialNumber();
        return Integer.parseInt(serial.replaceAll("\\D+", ""));
    }

    private boolean isSequentiallyIssuable(PassBook passBook, List<PassBook> allPassBooks) {

        int currentIndex = allPassBooks.indexOf(passBook);

        // First passbook can always be issued
        if (currentIndex == 0) {
            return true;
        }

        PassBook previousPassBook = allPassBooks.get(currentIndex - 1);

        return previousPassBook.getIssuedDate() != null;
    }

    @Override
    @Transactional
    public PassBookDTO create(PassBookCreateDTO createDTO) {
        log.info("Creating new PassBook: {}", createDTO);

        // Validate if serial number already exists
        if (passBookRepository.existsBySerialNumber(createDTO.getSerialNumber())) {
            throw new BusinessRuleException("PassBook with serial number " +
                    createDTO.getSerialNumber() + " already exists");
        }

        // Validate BookParent exists if provided
        if (createDTO.getBookParentId() != null) {
            BookParent bookParent = bookParentRepository.findById(createDTO.getBookParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", createDTO.getBookParentId()));

            // Validate serial number is within parent's range
            validateSerialInRange(createDTO.getSerialNumber(), bookParent);

            // Check if parent already has maximum number of pads
            int currentChildCount = passBookRepository.countByBookParentId(createDTO.getBookParentId());
            if (currentChildCount >= bookParent.getNumOfPad()) {
                throw new BusinessRuleException(
                        String.format("BookParent already has maximum number of pads (%d)", bookParent.getNumOfPad()));
            }
        }

        PassBook passBook = modelMapper.map(createDTO, PassBook.class);

        // Set audit fields
        passBook.setCreatedBy(createDTO.getCreatedBy());
        passBook.setLastUpdatedBy(createDTO.getCreatedBy());

        // Set received date if not provided
        if (passBook.getReceivedDate() == null) {
            passBook.setReceivedDate(LocalDateTime.now());
        }

        // If BookParent ID is provided, fetch and set the entity
        if (createDTO.getBookParentId() != null) {
            BookParent bookParent = bookParentRepository.findById(createDTO.getBookParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", createDTO.getBookParentId()));
            passBook.setBookParent(bookParent);
        }

        PassBook saved = passBookRepository.save(passBook);
        log.info("PassBook created with ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public List<PassBookDTO> createPassBookBatchWithParent(Long parentId, List<String> serialNumbers,
                                                           PassBookType type, PassBookCategory category, String createdBy) {
        log.info("Creating PassBook batch with parent ID: {}, {} serials, type: {}, category: {}",
                parentId, serialNumbers.size(), type, category);

        BookParent bookParent = bookParentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", parentId));

        // Validate number of serials doesn't exceed parent's numOfPad
        int currentChildCount = passBookRepository.countByBookParentId(parentId);
        if (currentChildCount + serialNumbers.size() > bookParent.getNumOfPad()) {
            throw new BusinessRuleException(
                    String.format("Cannot add %d PassBooks. Parent can only hold %d total, already has %d",
                            serialNumbers.size(), bookParent.getNumOfPad(), currentChildCount));
        }

        List<PassBook> passBooks = serialNumbers.stream()
                .map(serial -> {
                    // Validate serial doesn't already exist
                    if (passBookRepository.existsBySerialNumber(serial)) {
                        throw new BusinessRuleException("PassBook with serial number " + serial + " already exists");
                    }

                    // Validate serial is within parent's range
                    validateSerialInRange(serial, bookParent);

                    PassBook passBook = new PassBook();
                    passBook.setSerialNumber(serial);
                    passBook.setBookParent(bookParent);
                    passBook.setPassBookType(type);
                    passBook.setPassBookCategory(category);
                    passBook.setReceivedDate(LocalDateTime.now());
                    passBook.setCreatedBy(createdBy);
                    passBook.setLastUpdatedBy(createdBy);
                    passBook.setBranchId(bookParent.getBranchId());
                    passBook.setProcessId(bookParent.getProcessId());
                    passBook.setSubProcessId(bookParent.getSubProcessId());
//                    passBook.setNumOfPad(1); // Each PassBook is one pad

                    return passBook;
                })
                .collect(Collectors.toList());

        List<PassBook> saved = passBookRepository.saveAll(passBooks);
        log.info("Created {} PassBooks for parent ID: {}", saved.size(), parentId);

        return saved.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PassBookDTO> findById(Long id) {
        log.debug("Finding PassBook by ID: {}", id);
        return passBookRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public List<PassBookDTO> findAll() {
        log.debug("Fetching all PassBooks");
        return passBookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PassBookDTO> findAll(Pageable pageable) {
        log.debug("Fetching PassBooks with pagination");
        return passBookRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public PassBookDTO update(Long id, PassBookUpdateDTO updateDTO) {
        log.info("Updating PassBook with ID: {}", id);

        PassBook passBook = passBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PassBook", "id", id));

        // Update fields
        if (updateDTO.getIssuedDate() != null) {
            // Validate if not already issued
            if (passBook.getIssuedDate() != null) {
                throw new BusinessRuleException("PassBook is already issued");
            }
            passBook.setIssuedDate(updateDTO.getIssuedDate());
        }

        if (updateDTO.getReturnedDate() != null) {
            // Validate if issued but not returned
            if (passBook.getIssuedDate() == null) {
                throw new BusinessRuleException("PassBook must be issued before it can be returned");
            }
            if (passBook.getReturnedDate() != null) {
                throw new BusinessRuleException("PassBook is already returned");
            }
            passBook.setReturnedDate(updateDTO.getReturnedDate());
        }

//        if (updateDTO.getNumOfPad() > 0) {
//            passBook.setNumOfPad(updateDTO.getNumOfPad());
//        }

        // Update audit fields
        passBook.setLastUpdatedBy(updateDTO.getLastUpdatedBy());

        PassBook updated = passBookRepository.save(passBook);
        log.info("PassBook updated: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting PassBook with ID: {}", id);

        if (!passBookRepository.existsById(id)) {
            throw new ResourceNotFoundException("PassBook", "id", id);
        }

        // Check if PassBook is issued
        PassBook passBook = passBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PassBook", "id", id));

        if (passBook.getIssuedDate() != null && passBook.getReturnedDate() == null) {
            throw new BusinessRuleException("Cannot delete an issued PassBook. Please return it first.");
        }

        passBookRepository.deleteById(id);
        log.info("PassBook deleted: {}", id);
    }

    @Override
    public Optional<PassBookDTO> findBySerialNumber(String serialNumber) {
        log.debug("Finding PassBook by serial number: {}", serialNumber);
        return passBookRepository.findBySerialNumber(serialNumber)
                .map(this::convertToDTO);
    }

    @Override
    public List<PassBookDTO> findByBranchId(String branchId) {
        log.debug("Finding PassBooks for branch: {}", branchId);
        return passBookRepository.findByBranchId(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PassBookDTO> findByBookParentId(Long parentId) {
        log.debug("Finding PassBooks for parent batch: {}", parentId);
        return passBookRepository.findByBookParentId(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PassBookDTO> findByPassBookType(PassBookType type) {
        log.debug("Finding PassBooks by type: {}", type);
        return passBookRepository.findByPassBookType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PassBookDTO> findByPassBookCategory(PassBookCategory category) {
        log.debug("Finding PassBooks by category: {}", category);
        return passBookRepository.findByPassBookCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PassBookDTO> findByPassBookTypeAndCategory(PassBookType type, PassBookCategory category) {
        log.debug("Finding PassBooks by type: {} and category: {}", type, category);
        return passBookRepository.findByPassBookTypeAndPassBookCategory(type, category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PassBookDTO issueItem(Long id, String issuedBy) {
        log.info("Issuing PassBook with ID: {} by user: {}", id, issuedBy);

        PassBook passBook = passBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PassBook", "id", id));

        // Validate if not already issued
        if (passBook.getIssuedDate() != null) {
            throw new BusinessRuleException("PassBook is already issued");
        }

        // Check sequential issuance rule if part of a parent batch
        if (passBook.getBookParent() != null) {
            validateSequentialIssuance(passBook);
        }

        passBook.setIssuedDate(LocalDateTime.now());
        passBook.setIssuedBy(issuedBy);
        passBook.setLastUpdatedBy(issuedBy);

        PassBook issued = passBookRepository.save(passBook);

        // Update parent's used count
        if (issued.getBookParent() != null) {
            BookParent parent = issued.getBookParent();
            parent.setUsed(parent.getUsed() + 1);
            parent.setLastUpdatedBy(issuedBy);
            bookParentRepository.save(parent);
            log.info("Parent used count updated to: {}", parent.getUsed());
        }

        log.info("PassBook issued: {}", issued.getId());

        return convertToDTO(issued);
    }

    @Override
    @Transactional
    public PassBookDTO returnItem(Long id, String returnedBy) {
        log.info("Returning PassBook with ID: {} by user: {}", id, returnedBy);

        PassBook passBook = passBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PassBook", "id", id));

        // Validate if issued but not returned
        if (passBook.getIssuedDate() == null) {
            throw new BusinessRuleException("PassBook is not issued");
        }
        if (passBook.getReturnedDate() != null) {
            throw new BusinessRuleException("PassBook is already returned");
        }

        passBook.setReturnedDate(LocalDateTime.now());
//        passBook.setReturnedBy(returnedBy);
        passBook.setLastUpdatedBy(returnedBy);

        PassBook returned = passBookRepository.save(passBook);

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

        log.info("PassBook returned: {}", returned.getId());

        return convertToDTO(returned);
    }

    @Override
    @Transactional
    public PassBookDTO receiveItem(Long id, String receivedBy) {
        log.info("Receiving PassBook with ID: {} by user: {}", id, receivedBy);

        PassBook passBook = passBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PassBook", "id", id));

        // Validate if not received
        if (passBook.getReceivedDate() != null) {
            throw new BusinessRuleException("PassBook is already received");
        }

        passBook.setReceivedDate(LocalDateTime.now());
        passBook.setLastUpdatedBy(receivedBy);

        PassBook received = passBookRepository.save(passBook);
        log.info("PassBook received: {}", received.getId());

        return convertToDTO(received);
    }
    @Override
    @Transactional
    public RequestPassBookDTO receiveItem(RequestPassBookDTO request) {
        log.info("Receiving PassBook with ID: {} by user: {}", request.getId(), request.getLastUpdatedBy());

        PassBook passBook = passBookRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("PassBook", "id", request.getId()));

        // Validate if not received
        if (passBook.getReceivedDate() != null) {
            throw new BusinessRuleException("PassBook is already received");
        }
        RequestPassBook requestPassBook = requestPassBookRepository.findById(request.getId()).orElse(null);
        System.out.println(requestPassBook);

        if(requestPassBook == null ){
            return null;
        }
        requestPassBook.setReceivedDate(LocalDateTime.now());
        requestPassBook.setReceivedBy(request.getReceivedBy());
        requestPassBook.setReceivedById(request.getReceivedById());
        requestPassBookRepository.save(requestPassBook);
        // Validate if not received
        passBook.setReceivedDate(LocalDateTime.now());
        passBook.setReceivedBy(request.getReceivedBy());
        passBook.setReceivedById(request.getReceivedById());

        PassBook received = passBookRepository.save(passBook);
        BookParent bookParent =received.getBookParent();
        bookParent.setUsed(bookParent.getUsed() + 1);
        bookParent.setLastIssuedChild(passBook.getId());
        bookParentRepository.save(bookParent);
        log.info("PassBook received: {}", received.getId());

        return convertToRequestPassBookDTO(received);
    }

    @Override
    public List<PassBookDTO> findAvailableByBranch(String branchId) {
        log.debug("Finding available PassBooks for branch: {}", branchId);
        return passBookRepository.findByBranchIdAndIssuedDateIsNull(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PassBookDTO> findIssuedByBranch(String branchId) {
        log.debug("Finding issued PassBooks for branch: {}", branchId);
        return passBookRepository.findByBranchIdAndIssuedDateIsNotNull(branchId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countByBranchAndStatus(String branchId, String status) {
        log.debug("Counting PassBooks for branch: {} with status: {}", branchId, status);
        switch (status.toLowerCase()) {
            case "available":
                return passBookRepository.countByBranchIdAndIssuedDateIsNull(branchId);
            case "issued":
                return passBookRepository.countByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(branchId);
            case "returned":
                return passBookRepository.countByBranchIdAndReturnedDateIsNotNull(branchId);
            default:
                return passBookRepository.countByBranchId(branchId);
        }
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return passBookRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    public List<PassBookDTO> findByBranchIdAndStatus(String branchId, String status) {
        log.debug("Finding PassBooks for branch: {} with status: {}", branchId, status);
        switch (status.toLowerCase()) {
            case "available":
                return passBookRepository.findByBranchIdAndIssuedDateIsNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            case "issued":
                return passBookRepository.findByBranchIdAndIssuedDateIsNotNullAndReturnedDateIsNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            case "returned":
                return passBookRepository.findByBranchIdAndReturnedDateIsNotNull(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            default:
                return passBookRepository.findByBranchId(branchId).stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
        }
    }

    @Override
    public Page<PassBookDTO> findByBranchId(String branchId, Pageable pageable) {
        log.debug("Finding PassBooks for branch with pagination: {}", branchId);
        return passBookRepository.findByBranchId(branchId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public List<PassBookDTO> createBatch(List<PassBookCreateDTO> createDTOs) {
        log.info("Batch creating {} PassBooks", createDTOs.size());

        List<PassBook> passBooks = createDTOs.stream()
                .map(dto -> {
                    // Validate serial number
                    if (passBookRepository.existsBySerialNumber(dto.getSerialNumber())) {
                        throw new BusinessRuleException(
                                "PassBook with serial number " + dto.getSerialNumber() + " already exists");
                    }

                    PassBook passBook = modelMapper.map(dto, PassBook.class);
                    passBook.setCreatedBy(dto.getCreatedBy());
                    passBook.setLastUpdatedBy(dto.getCreatedBy());

                    if (passBook.getReceivedDate() == null) {
                        passBook.setReceivedDate(LocalDateTime.now());
                    }

                    // Set BookParent if provided
                    if (dto.getBookParentId() != null) {
                        BookParent bookParent = bookParentRepository.findById(dto.getBookParentId())
                                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", dto.getBookParentId()));
                        passBook.setBookParent(bookParent);
                        validateSerialInRange(dto.getSerialNumber(), bookParent);
                    }

                    return passBook;
                })
                .collect(Collectors.toList());

        List<PassBook> saved = passBookRepository.saveAll(passBooks);
        log.info("Batch created {} PassBooks", saved.size());

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

    private void validateSequentialIssuance(PassBook passBook) {
        BookParent parent = passBook.getBookParent();
        List<PassBook> allPassBooks = passBookRepository.findByBookParentOrderBySerialNumberAsc(parent);

        int currentIndex = -1;
        for (int i = 0; i < allPassBooks.size(); i++) {
            if (allPassBooks.get(i).getSerialNumber().equals(passBook.getSerialNumber())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            PassBook previousPassBook = allPassBooks.get(currentIndex - 1);
            if (previousPassBook.getIssuedDate() == null) {
                throw new BusinessRuleException(
                        String.format("Cannot issue serial %s. Previous serial %s must be issued first.",
                                passBook.getSerialNumber(), previousPassBook.getSerialNumber())
                );
            }
        }
    }

    private PassBookDTO convertToDTO(PassBook passBook) {
        PassBookDTO dto = modelMapper.map(passBook, PassBookDTO.class);

        // Map BookParent ID if exists
        if (passBook.getBookParent() != null) {
            dto.setBookParentId(passBook.getBookParent().getId());
            // Also include parent stats
            dto.setParentNumOfPad(passBook.getBookParent().getNumOfPad());
            dto.setParentUsed(passBook.getBookParent().getUsed());
            dto.setParentAvailable(passBook.getBookParent().getNumOfPad() - passBook.getBookParent().getUsed());
        }

        return dto;
    }

    private RequestPassBookDTO convertToRequestPassBookDTO(PassBook passBook) {
        RequestPassBookDTO dto = modelMapper.map(passBook, RequestPassBookDTO.class);

        // Map BookParent ID if exists
        if (passBook.getBookParent() != null) {
            dto.setPassBookId(passBook.getId());
            // Also include parent stats
            dto.setLastUpdatedBy(passBook.getLastUpdatedBy());
//            dto.setParentUsed(passBook.getBookParent().getUsed());
//            dto.setParentAvailable(passBook.getBookParent().getNumOfPad() - passBook.getBookParent().getUsed());
        }

        return dto;
    }
}