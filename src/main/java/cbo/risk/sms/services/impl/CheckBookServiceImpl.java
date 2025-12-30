package cbo.risk.sms.services.impl;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import cbo.risk.sms.enums.ParentBookType;
import cbo.risk.sms.exceptions.ResourceNotFoundException;
import cbo.risk.sms.exceptions.BusinessRuleException;
import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.models.CheckBook;
import cbo.risk.sms.models.RequestCheckBook;
import cbo.risk.sms.repositories.BookParentRepository;
import cbo.risk.sms.repositories.CheckBookRepository;
import cbo.risk.sms.repositories.RequestCheckBookRepository;
import cbo.risk.sms.services.CheckBookService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckBookServiceImpl implements CheckBookService {

    private static final Logger log = LoggerFactory.getLogger(CheckBookServiceImpl.class);
@Autowired
    private  CheckBookRepository checkBookRepository;
@Autowired
    private  BookParentRepository bookParentRepository;
@Autowired
    private  ModelMapper modelMapper;
@Autowired
private RequestCheckBookRepository requestCheckBookRepository;


    @Override
    @Transactional
    public ResponseDTO<RequestCheckBookDTO> issueAvailableCheckBook(RequestCheckBookDTO request) {
        log.info("Issuing next available CheckBook for branch: {}, issued by: {}",
                request.getBranchId(), request.getCreatedBy());

        // 1. Find the next available checkbook (sequential issuance)
        CheckBook availableCheckBook = findAndReserveNextAvailableCheckBook(request);
        System.out.println("1");
        // 2. Create request record
        RequestCheckBook requestCheckBook = new RequestCheckBook();
        requestCheckBook.setCheckBookId(availableCheckBook.getId());
        requestCheckBook.setStartSerialNumber(availableCheckBook.getStartSerialNumber());
        requestCheckBook.setEndSerialNumber(availableCheckBook.getEndSerialNumber());
        requestCheckBook.setBranchId(request.getBranchId());
        requestCheckBook.setProcessId(request.getProcessId());
        requestCheckBook.setSubProcessId(request.getSubProcessId());
        requestCheckBook.setCreatedBy(request.getCreatedBy());
        requestCheckBook.setCreatedTimestamp(LocalDateTime.now());
        requestCheckBook.setLastUpdatedBy(request.getLastUpdatedBy());
        requestCheckBook.setCreatedById(request.getCreatedById());
        requestCheckBook.setLastUpdatedBy(request.getLastUpdatedBy());
        requestCheckBook.setModifiedTimestamp(LocalDateTime.now());
        requestCheckBook.setIssuedBy(request.getIssuedBy());
        requestCheckBook.setIssuedDate(LocalDateTime.now());
        requestCheckBook.setIssuedById(request.getIssuedById());
        requestCheckBook.setLastUpdatedById(request.getLastUpdatedById());
        requestCheckBook.setIssuedDate(LocalDateTime.now());
        requestCheckBook.setAccountNumber(request.getAccountNumber());

        RequestCheckBook savedRequest = requestCheckBookRepository.save(requestCheckBook);
        System.out.println(2);
        // 3. Update checkbook status
        availableCheckBook.setIssuedBy(request.getIssuedBy());
        availableCheckBook.setIssuedById(request.getIssuedById());
        availableCheckBook.setIssuedDate(LocalDateTime.now());

        availableCheckBook.setLastUpdatedBy(request.getLastUpdatedBy());
        availableCheckBook.setLastUpdatedById(request.getLastUpdatedById());
        availableCheckBook.setModifiedTimestamp(LocalDateTime.now());
        checkBookRepository.save(availableCheckBook);

        System.out.println(4);

        log.info("CheckBook issued - Request ID: {}, CheckBook ID: {}, Serial: {} to {}",
                savedRequest.getId(), availableCheckBook.getId(),
                availableCheckBook.getStartSerialNumber(), request.getBranchId());

        return createIssueResponse(savedRequest, availableCheckBook);
    }

    @Override
    @Transactional
    public ResponseDTO<RequestCheckBookDTO> receiveCheckBook(RequestCheckBookDTO request) {
        log.info("Receiving CheckBook for request ID: {}, received by: {}",
                request.getId(), request.getLastUpdatedBy());

        // 1. Find the request
        RequestCheckBook requestCheckBook = requestCheckBookRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CheckBook Request", "id", request.getCheckBookId()));

        // 2. Validate it's issued but not received
        if (requestCheckBook.getIssuedDate() == null) {
            throw new BusinessRuleException("CheckBook is not issued yet");
        }
        if (requestCheckBook.getReceivedDate() != null) {
            throw new BusinessRuleException("CheckBook is already received");
        }

        // 3. Update request with received info
        requestCheckBook.setReceivedDate(LocalDateTime.now());
        requestCheckBook.setLastUpdatedBy(request.getLastUpdatedBy());
//        requestCheckBook.setLastUpdatedBy(request.getReceivedBy());
        RequestCheckBook updatedRequest = requestCheckBookRepository.save(requestCheckBook);

        // 4. Update checkbook
        CheckBook checkBook = checkBookRepository.findById(updatedRequest.getCheckBookId()).orElse(null);
        if (checkBook!= null) {
            throw new BusinessRuleException("CheckBook Does not Exist");
        }
        checkBook.setReceivedDate(LocalDateTime.now());
        checkBook.setLastUpdatedBy(request.getLastUpdatedBy());
//        checkBook.setLastUpdatedBy(request.getReceivedBy());
        checkBookRepository.save(checkBook);

        log.info("CheckBook received - Request ID: {}, CheckBook ID: {}",
                updatedRequest.getId(), checkBook.getId());

        return createIssueResponse(updatedRequest, checkBook);
    }

    private CheckBook findAndReserveNextAvailableCheckBook(RequestCheckBookDTO request) {
        // Find parent batches with available checkbooks using your query

            // Fallback: check any available parents
          Optional<BookParent>  availableParent = bookParentRepository.findFirstByBranchIdAndCheckLeaveTypeAndParentBookTypeAndFinishedFalseOrderByIdAsc(
                    request.getBranchId(),CheckBookLeaveType.fromLeaves(request.getCheckBookLeaveType()) , ParentBookType.CHECK_BOOK);

if(availableParent.isPresent()) {

    // Find the first available checkbook in this parent (sequential)
    List<CheckBook> checkBooks = checkBookRepository
            .findByBookParentIdOrderByStartSerialNumberAsc(availableParent.get().getId());
    checkBooks.sort(Comparator.comparingInt(this::extractStartSerialNumber));
    for (CheckBook checkBook : checkBooks) {
        // Check if not issued and matches type
        if (checkBook.getIssuedDate() == null &&
                checkBook.getCheckBookType() == request.getCheckBookType()) {

            // Check sequential issuance rule
            if (isSequentiallyIssuable(checkBook, checkBooks)) {
                return checkBook;
            }

        }
    }
}
        throw new BusinessRuleException(
                String.format("No available CheckBooks found for branch %s, type %s, %d leaves",
                        request.getBranchId(), request.getCheckBookType(),
                        request.getCheckBookLeaveType()));
    }
    private int extractStartSerialNumber(CheckBook checkBook) {
        // Examples: check1, check2, check3
        String serial = checkBook.getStartSerialNumber();
        return Integer.parseInt(serial.replaceAll("\\D+", ""));
    }


    private boolean isSequentiallyIssuable(CheckBook checkBook, List<CheckBook> allCheckBooks) {
        int currentIndex = allCheckBooks.indexOf(checkBook);

        // First checkbook in sequence is always issuable
        if (currentIndex == 0) {
            return true;
        }

        // Check if previous checkbook is issued
        CheckBook previousCheckBook = allCheckBooks.get(currentIndex - 1);
        return previousCheckBook.getIssuedDate() != null;
    }

    private ResponseDTO<RequestCheckBookDTO> createIssueResponse(RequestCheckBook request, CheckBook checkBook) {
        RequestCheckBookDTO response = new RequestCheckBookDTO();
        response.setId(request.getId());
        response.setCheckBookId(checkBook.getId());
        response.setStartSerialNumber(checkBook.getStartSerialNumber());
        response.setEndSerialNumber(checkBook.getEndSerialNumber());
        response.setIssuedDate(request.getIssuedDate());
        response.setCreatedBy(request.getCreatedBy());
//        response.setIssuedTo(request.getIssuedTo());
        ResponseDTO<RequestCheckBookDTO> responseDTO = new ResponseDTO<>();
        responseDTO.setResult(response);

        if (request.getReceivedDate() != null) {
            responseDTO.setStatus(true);
            responseDTO.setMessage("CheckBook has been received by CSO");
        } else {
            responseDTO.setStatus(true);
            responseDTO.setMessage("CheckBook issued, awaiting CSO receipt");
        }

        return responseDTO;
    }

    @Override
    public Optional<CheckBookDTO> findNextAvailableCheckBook(String branchId, CheckBookType type,
                                                             CheckBookLeaveType leaveType) {
//        // Similar logic to findAndReserve but without reserving
//        List<BookParent> availableParents = bookParentRepository.findByBranchIdAndLeavesPerCheckBookAndAvailablePads(
//                branchId, leaveType.getNumberOfLeaves());
//
//        for (BookParent parent : availableParents) {
//            List<CheckBook> checkBooks = checkBookRepository.findByBookParentIdOrderByStartSerialNumberAsc(parent.getId());
//
//            for (CheckBook checkBook : checkBooks) {
//                if (checkBook.getIssuedDate() == null && checkBook.getCheckBookType() == type) {
//                    if (isSequentiallyIssuable(checkBook, checkBooks)) {
//                        return Optional.of(convertToDTO(checkBook));
//                    }
//                }
//            }
//        }

        return Optional.empty();
    }

    @Override
    public List<CheckBookDTO> findAvailableCheckBooks(String branchId, CheckBookType type,
                                                      CheckBookLeaveType leaveType) {
//        List<BookParent> availableParents = bookParentRepository.findByBranchIdAndLeavesPerCheckBookAndAvailablePads(
//                branchId, leaveType.getNumberOfLeaves());
//
//        List<CheckBookDTO> availableCheckBooks = new ArrayList<>();
//
//        for (BookParent parent : availableParents) {
//            List<CheckBook> checkBooks = checkBookRepository.findByBookParentIdAndCheckBookTypeAndIssuedDateIsNull(
//                    parent.getId(), type);
//
//            for (CheckBook checkBook : checkBooks) {
//                if (isSequentiallyIssuable(checkBook, checkBooks)) {
//                    availableCheckBooks.add(convertToDTO(checkBook));
//                }
//            }
//        }
//
//        return availableCheckBooks;
        return null;
    }

    @Override
    @Transactional
    public CheckBookDTO create(CheckBookCreateDTO createDTO) {
        log.info("Creating new CheckBook: {}", createDTO);

        // Validate if serial number already exists
        if (checkBookRepository.existsByStartSerialNumber(createDTO.getSerialNumber())) {
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
        return checkBookRepository.findByStartSerialNumber(serialNumber)
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
//        if (checkBook.getBookParent() != null) {
//            validateSequentialIssuance(checkBook);
//        }

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
    public CheckBookDTO receiveItem(Long id, String receivedBy) {
        return null;
    }


    @Transactional
    public RequestCheckBookDTO receiveItem(RequestCheckBookDTO received) {
        log.info("Receiving CheckBook with ID: {} by user: {}", received.getId(), received.getLastUpdatedBy());
        System.out.println(received);
        CheckBook checkBook = checkBookRepository.findById(received.getId())
                .orElse(null);
        System.out.println(checkBook);
        if(checkBook == null ){
            return null;
        }
        if (checkBook.getReceivedDate() != null) {
            throw new BusinessRuleException("CheckBook is already received");
        }
        if(!Objects.equals(checkBook.getIssuedById(), received.getReceivedById())){
            throw new BusinessRuleException("You can not Receive Check Book issued by CSO " + checkBook.getIssuedBy());

        }
        checkBook.setReceivedDate(LocalDateTime.now());
        checkBook.setReceivedBy(received.getReceivedBy());
        checkBook.setReceivedById(received.getReceivedById());
        RequestCheckBook requestCheckBook = requestCheckBookRepository.findById(received.getId()).orElse(null);
        System.out.println(requestCheckBook);

        if(requestCheckBook == null ){
            return null;
        }
        requestCheckBook.setReceivedDate(LocalDateTime.now());
        requestCheckBook.setReceivedBy(received.getReceivedBy());
        requestCheckBook.setReceivedById(received.getReceivedById());

        // Validate if not received
        System.out.println("here1");





        System.out.println("here2");
     CheckBook saved=   checkBookRepository.save(checkBook);
        System.out.println("here3");
        BookParent bookParent =saved.getBookParent();

        bookParent.setUsed(bookParent.getUsed() + 1);
        bookParent.setLastIssuedChild(checkBook.getId());
        bookParentRepository.save(bookParent);
       RequestCheckBook savedRequestCheckBook = requestCheckBookRepository.save(requestCheckBook);
        log.info("CheckBook received: {}", received.getId());

        return convertToRequestCheckBookDTO(savedRequestCheckBook);
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

    public List<RequestCheckBookDTO> findIssuedRequestCheckBookByBranch(String branchId) {
        log.debug("Finding issued Request Check books for branch: {}", branchId);
        return requestCheckBookRepository.findByBranchId(branchId).stream()
                .map(this::convertToRequestCheckBookDTO)
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
        return checkBookRepository.existsByStartSerialNumber(serialNumber);
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
                    if (checkBookRepository.existsByStartSerialNumber(dto.getSerialNumber())) {
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

//    private void validateSequentialIssuance(CheckBook checkBook) {
//        BookParent parent = checkBook.getBookParent();
//        List<CheckBook> allBooks = checkBookRepository.findByBookParentOrderBySerialNumberAsc(parent);
//
//        int currentIndex = -1;
//        for (int i = 0; i < allBooks.size(); i++) {
//            if (allBooks.get(i).getSerialNumber().equals(checkBook.getSerialNumber())) {
//                currentIndex = i;
//                break;
//            }
//        }
//
//        if (currentIndex > 0) {
//            CheckBook previousBook = allBooks.get(currentIndex - 1);
//            if (previousBook.getIssuedDate() == null) {
//                throw new BusinessRuleException(
//                        String.format("Cannot issue serial %s. Previous serial %s must be issued first.",
//                                checkBook.getSerialNumber(), previousBook.getSerialNumber())
//                );
//            }
//        }
//    }

    private CheckBookDTO convertToDTO(CheckBook checkBook) {
        CheckBookDTO dto = modelMapper.map(checkBook, CheckBookDTO.class);

        // Map BookParent ID if exists
        if (checkBook.getBookParent() != null) {
            dto.setBookParentId(checkBook.getBookParent().getId());
        }

        return dto;
    }

    private RequestCheckBookDTO convertToRequestCheckBookDTO(RequestCheckBook requestCheckBook) {
        RequestCheckBookDTO dto = modelMapper.map(requestCheckBook, RequestCheckBookDTO.class);


        return dto;
    }
}