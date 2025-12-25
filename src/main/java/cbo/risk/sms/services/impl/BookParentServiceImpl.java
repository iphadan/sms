package cbo.risk.sms.services.impl;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.enums.CheckBookType;
import cbo.risk.sms.enums.ParentBookType;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import cbo.risk.sms.exceptions.ResourceNotFoundException;
import cbo.risk.sms.exceptions.BusinessRuleException;
import cbo.risk.sms.models.*;
import cbo.risk.sms.repositories.*;
import cbo.risk.sms.services.BookParentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookParentServiceImpl implements BookParentService {
    private static final Logger log = LoggerFactory.getLogger(BookParentServiceImpl.class);
    @Autowired
    private BookParentRepository bookParentRepository;

    @Autowired
    private CheckBookRepository checkBookRepository;

    @Autowired
    private CpoRepository cpoRepository;

    @Autowired
    private PassBookRepository passBookRepository;

    @Override
    @Transactional
    public BatchResponseDTO registerCheckBookBatch(BatchRegistrationDTO registrationDTO) {
        log.info("Registering CheckBook batch: {} to {}",
                registrationDTO.getStartSerial(), registrationDTO.getEndSerial());

        validateBatchRegistration(registrationDTO);

        // 1. Create Parent
        BookParent parent = createParent(registrationDTO);
        parent.setParentBookType(ParentBookType.valueOf(registrationDTO.getParentBookType().name()));
        parent.setBatchReceivedDate(LocalDateTime.now());
        BookParent savedParent = bookParentRepository.save(parent);

        // 2. Generate serial numbers and create CheckBook children
        List<String> serials = generateSerials(
                registrationDTO.getStartSerial(),
                registrationDTO.getEndSerial()
        );

        List<CheckBook> checkBooks = new ArrayList<>();
        for (String serial : serials) {
            CheckBook checkBook = new CheckBook();
            checkBook.setSerialNumber(serial);
            checkBook.setBookParent(savedParent);
            checkBook.setCheckBookType(registrationDTO.getCheckBookType());
            checkBook.setCheckBookLeaveType(registrationDTO.getCheckBookLeaveType());
            checkBook.setCreatedBy(registrationDTO.getCreatedBy());
            checkBook.setLastUpdatedBy(registrationDTO.getCreatedBy());
            checkBook.setBranchId(registrationDTO.getBranchId());
            checkBook.setSubProcessId(registrationDTO.getSubProcessId());
            checkBook.setProcessId(registrationDTO.getProcessId());

            checkBooks.add(checkBook);
        }

        checkBookRepository.saveAll(checkBooks);
        log.info("Created {} CheckBooks for parent ID: {}", checkBooks.size(), savedParent.getId());

        return createBatchResponse(savedParent, "CHECKBOOK", checkBooks.size());
    }

    @Override
    @Transactional
    public BatchResponseDTO registerCpoBatch(BatchRegistrationDTO registrationDTO) {
        log.info("Registering CPO batch: {} to {}",
                registrationDTO.getStartSerial(), registrationDTO.getEndSerial());
        System.out.println("1");
        validateBatchRegistration(registrationDTO);

        // 1. Create Parent
        BookParent parent = createParent(registrationDTO);
        parent.setBatchReceivedDate(LocalDateTime.now());
        parent.setParentBookType(ParentBookType.valueOf(registrationDTO.getParentBookType().name()));
        BookParent savedParent = bookParentRepository.save(parent);

        // 2. Generate serial numbers and create CPO children
        List<String> serials = generateSerials(
                registrationDTO.getStartSerial(),
                registrationDTO.getEndSerial()
        );

        List<Cpo> cpos = new ArrayList<>();
        for (String serial : serials) {
            Cpo cpo = new Cpo();
            cpo.setSerialNumber(serial); // Assuming you added serialNumber to Cpo model
            cpo.setBookParent(savedParent);
            cpo.setCreatedBy(registrationDTO.getCreatedBy());
            cpo.setLastUpdatedBy(registrationDTO.getCreatedBy());
            cpo.setBranchId(registrationDTO.getBranchId());
            cpo.setSubProcessId(registrationDTO.getSubProcessId());
            cpo.setProcessId(registrationDTO.getProcessId());

            cpos.add(cpo);
        }

        cpoRepository.saveAll(cpos);
        log.info("Created {} CPOs for parent ID: {}", cpos.size(), savedParent.getId());

        return createBatchResponse(savedParent, "CPO", cpos.size());
    }

    @Override
    @Transactional
    public BatchResponseDTO registerPassBookBatch(BatchRegistrationDTO registrationDTO) {
        log.info("Registering PassBook batch: {} to {}",
                registrationDTO.getStartSerial(), registrationDTO.getEndSerial());

        validateBatchRegistration(registrationDTO);
        System.out.println("1");
        // 1. Create Parent
        BookParent parent = createParent(registrationDTO);
        parent.setBatchReceivedDate(LocalDateTime.now());
        parent.setParentBookType(ParentBookType.valueOf(registrationDTO.getParentBookType().name()));
        BookParent savedParent = bookParentRepository.save(parent);

        // 2. Generate serial numbers and create PassBook children
        List<String> serials = generateSerials(
                registrationDTO.getStartSerial(),
                registrationDTO.getEndSerial()
        );

        List<PassBook> passBooks = new ArrayList<>();
        for (String serial : serials) {
            PassBook passBook = new PassBook();
            passBook.setSerialNumber(serial); // Assuming you added serialNumber to PassBook model
            passBook.setBookParent(savedParent);
            passBook.setPassBookType(PassBookType.valueOf(registrationDTO.getPassBookType().name()));
            passBook.setPassBookCategory(PassBookCategory.valueOf(registrationDTO.getPassBookCategory().name()));
            passBook.setCreatedBy(registrationDTO.getCreatedBy());
            passBook.setLastUpdatedBy(registrationDTO.getCreatedBy());
            passBook.setBranchId(registrationDTO.getBranchId());
            passBook.setSubProcessId(registrationDTO.getSubProcessId());
            passBook.setProcessId(registrationDTO.getProcessId());

            passBooks.add(passBook);
        }

        passBookRepository.saveAll(passBooks);
        log.info("Created {} PassBooks for parent ID: {}", passBooks.size(), savedParent.getId());

        return createBatchResponse(savedParent, "PASSBOOK", passBooks.size());
    }

    @Override
    @Transactional
    public BatchResponseDTO issueBook(IssueRequestDTO issueRequest) {
        log.info("Issuing book with serial: {}", issueRequest.getSerialNumber());

        // Find which type of book this is
        Optional<CheckBook> checkBookOpt = checkBookRepository.findBySerialNumber(issueRequest.getSerialNumber());
        Optional<Cpo> cpoOpt = cpoRepository.findBySerialNumber(issueRequest.getSerialNumber());
        Optional<PassBook> passBookOpt = passBookRepository.findBySerialNumber(issueRequest.getSerialNumber());

        BookParent parent = null;
        String bookType = "";

        if (checkBookOpt.isPresent()) {
            CheckBook checkBook = checkBookOpt.get();
            validateIssuance(checkBook, issueRequest);

            // Check sequential issuance rule
            validateSequentialIssuance(checkBook, issueRequest.getBranchId());

            // Update CheckBook
            checkBook.setIssuedDate(LocalDateTime.now());
            checkBook.setIssuedBy(issueRequest.getIssuedBy());
            checkBook.setLastUpdatedBy(issueRequest.getIssuedBy());
            checkBookRepository.save(checkBook);

            parent = checkBook.getBookParent();
            bookType = "CHECKBOOK";

        } else if (cpoOpt.isPresent()) {
            Cpo cpo = cpoOpt.get();
            validateIssuance(cpo, issueRequest);

            // Check sequential issuance rule
            validateSequentialIssuance(cpo, issueRequest.getBranchId());

            // Update CPO
            cpo.setIssuedDate(LocalDateTime.now());
            cpo.setReceivedBy(issueRequest.getIssuedTo());
            cpo.setIssuedBy(issueRequest.getIssuedBy());
            cpo.setLastUpdatedBy(issueRequest.getIssuedBy());
            cpoRepository.save(cpo);

            parent = cpo.getBookParent();
            bookType = "CPO";

        } else if (passBookOpt.isPresent()) {
            PassBook passBook = passBookOpt.get();
            validateIssuance(passBook, issueRequest);

            // Check sequential issuance rule
            validateSequentialIssuance(passBook, issueRequest.getBranchId());

            // Update PassBook
            passBook.setIssuedDate(LocalDateTime.now());
            passBook.setReceivedBy(issueRequest.getIssuedTo());
            passBook.setIssuedBy(issueRequest.getIssuedBy());
            passBook.setLastUpdatedBy(issueRequest.getIssuedBy());
            passBookRepository.save(passBook);

            parent = passBook.getBookParent();
            bookType = "PASSBOOK";

        } else {
            throw new ResourceNotFoundException("Book", "serial number", issueRequest.getSerialNumber());
        }

        // Update parent's used count
        parent.setUsed(parent.getUsed() + 1);
        parent.setLastUpdatedBy(issueRequest.getIssuedBy());
        bookParentRepository.save(parent);

        log.info("Book {} issued successfully. Parent used count: {}",
                issueRequest.getSerialNumber(), parent.getUsed());

        return createBatchResponse(parent, bookType, 0);
    }

    @Override
    @Transactional
    public BatchResponseDTO returnBook(ReturnRequestDTO returnRequest) {
        log.info("Returning book with serial: {}", returnRequest.getSerialNumber());

        // Find which type of book this is
        Optional<CheckBook> checkBookOpt = checkBookRepository.findBySerialNumber(returnRequest.getSerialNumber());
        Optional<Cpo> cpoOpt = cpoRepository.findBySerialNumber(returnRequest.getSerialNumber());
        Optional<PassBook> passBookOpt = passBookRepository.findBySerialNumber(returnRequest.getSerialNumber());

        BookParent parent = null;
        String bookType = "";

        if (checkBookOpt.isPresent()) {
            CheckBook checkBook = checkBookOpt.get();
            validateReturn(checkBook);

            // Update CheckBook
            checkBook.setReturnedDate(LocalDateTime.now());
            checkBook.setLastUpdatedBy(returnRequest.getReturnedBy());
            checkBookRepository.save(checkBook);

            parent = checkBook.getBookParent();
            bookType = "CHECKBOOK";

        } else if (cpoOpt.isPresent()) {
            Cpo cpo = cpoOpt.get();
            validateReturn(cpo);

            // Update CPO
            cpo.setReturnedDate(LocalDateTime.now());
            cpo.setLastUpdatedBy(returnRequest.getReturnedBy());
            cpoRepository.save(cpo);

            parent = cpo.getBookParent();
            bookType = "CPO";

        } else if (passBookOpt.isPresent()) {
            PassBook passBook = passBookOpt.get();
            validateReturn(passBook);

            // Update PassBook
            passBook.setReturnedDate(LocalDateTime.now());
            passBook.setLastUpdatedBy(returnRequest.getReturnedBy());
            passBookRepository.save(passBook);

            parent = passBook.getBookParent();
            bookType = "PASSBOOK";

        } else {
            throw new ResourceNotFoundException("Book", "serial number", returnRequest.getSerialNumber());
        }

        // Update parent's used count (decrement)
        parent.setUsed(parent.getUsed() - 1);
        if (parent.getUsed() < 0) parent.setUsed(0); // Safety check
        parent.setLastUpdatedBy(returnRequest.getReturnedBy());
        bookParentRepository.save(parent);

        log.info("Book {} returned successfully. Parent used count: {}",
                returnRequest.getSerialNumber(), parent.getUsed());

        return createBatchResponse(parent, bookType, 0);
    }

    // ============== SEQUENTIAL ISSUANCE VALIDATION ==============

    private void validateSequentialIssuance(CheckBook checkBook, String branchId) {
        BookParent parent = checkBook.getBookParent();
        List<CheckBook> allBooks = checkBookRepository.findByBookParentOrderBySerialNumberAsc(parent);

        // Find the current book's position
        int currentIndex = -1;
        for (int i = 0; i < allBooks.size(); i++) {
            if (allBooks.get(i).getSerialNumber().equals(checkBook.getSerialNumber())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            // Check if previous book is issued
            CheckBook previousBook = allBooks.get(currentIndex - 1);
            if (previousBook.getIssuedDate() == null) {
                throw new BusinessRuleException(
                        String.format("Cannot issue serial %s. Previous serial %s must be issued first.",
                                checkBook.getSerialNumber(), previousBook.getSerialNumber())
                );
            }
        }
    }

    private void validateSequentialIssuance(Cpo cpo, String branchId) {
        BookParent parent = cpo.getBookParent();
        List<Cpo> allBooks = cpoRepository.findByBookParentOrderBySerialNumberAsc(parent);

        int currentIndex = -1;
        for (int i = 0; i < allBooks.size(); i++) {
            if (allBooks.get(i).getSerialNumber().equals(cpo.getSerialNumber())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            Cpo previousBook = allBooks.get(currentIndex - 1);
            if (previousBook.getIssuedDate() == null) {
                throw new BusinessRuleException(
                        String.format("Cannot issue serial %s. Previous serial %s must be issued first.",
                                cpo.getSerialNumber(), previousBook.getSerialNumber())
                );
            }
        }
    }

    private void validateSequentialIssuance(PassBook passBook, String branchId) {
        BookParent parent = passBook.getBookParent();
        List<PassBook> allBooks = passBookRepository.findByBookParentOrderBySerialNumberAsc(parent);

        int currentIndex = -1;
        for (int i = 0; i < allBooks.size(); i++) {
            if (allBooks.get(i).getSerialNumber().equals(passBook.getSerialNumber())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            PassBook previousBook = allBooks.get(currentIndex - 1);
            if (previousBook.getIssuedDate() == null) {
                throw new BusinessRuleException(
                        String.format("Cannot issue serial %s. Previous serial %s must be issued first.",
                                passBook.getSerialNumber(), previousBook.getSerialNumber())
                );
            }
        }
    }

    // ============== HELPER METHODS ==============

    private BookParent createParent(BatchRegistrationDTO registrationDTO) {
        BookParent parent = new BookParent();
        parent.setStartingSerial(registrationDTO.getStartSerial());
        parent.setEndingSerial(registrationDTO.getEndSerial());

        // Calculate number of pads
        List<String> serials = generateSerials(
                registrationDTO.getStartSerial(),
                registrationDTO.getEndSerial()
        );
        parent.setNumOfPad(serials.size());
        parent.setUsed(0);

        parent.setBranchId(registrationDTO.getBranchId());
        parent.setSubProcessId(registrationDTO.getSubProcessId());
        parent.setProcessId(registrationDTO.getProcessId());
        parent.setCreatedBy(registrationDTO.getCreatedBy());
        parent.setLastUpdatedBy(registrationDTO.getCreatedBy());

        return parent;
    }

    private List<String> generateSerials(String start, String end) {
        List<String> serials = new ArrayList<>();

        try {
            // Extract numeric parts
            String prefix = start.replaceAll("\\d+", "");
            String suffix = start.substring(prefix.length());

            int startNum = Integer.parseInt(suffix);
            int endNum = Integer.parseInt(end.substring(prefix.length()));

            if (endNum < startNum) {
                throw new BusinessRuleException("End serial must be greater than start serial");
            }

            for (int i = startNum; i <= endNum; i++) {
                serials.add(prefix + String.format("%0" + suffix.length() + "d", i));
            }

        } catch (NumberFormatException e) {
            // If not numeric, just add the single range
            serials.add(start);
            if (!start.equals(end)) {
                throw new BusinessRuleException(
                        "Only numeric serial ranges are supported for batch registration");
            }
        }

        return serials;
    }

    private void validateBatchRegistration(BatchRegistrationDTO dto) {
        if (dto.getStartSerial() == null || dto.getEndSerial() == null) {
            throw new BusinessRuleException("Start and end serials are required");
        }

        // Check if any serial in range already exists
        List<String> serials = generateSerials(dto.getStartSerial(), dto.getEndSerial());
        for (String serial : serials) {
            if (checkBookRepository.existsBySerialNumber(serial) ||
                    cpoRepository.existsBySerialNumber(serial) ||
                    passBookRepository.existsBySerialNumber(serial)) {
                throw new BusinessRuleException(
                        String.format("Serial number %s already exists in the system", serial)
                );
            }
        }
    }

    private void validateIssuance(CheckBook checkBook, IssueRequestDTO request) {
        if (checkBook.getIssuedDate() != null) {
            throw new BusinessRuleException(
                    String.format("CheckBook %s is already issued", checkBook.getSerialNumber())
            );
        }

        if (!checkBook.getBranchId().equals(request.getBranchId())) {
            throw new BusinessRuleException(
                    String.format("CheckBook %s does not belong to branch %s",
                            checkBook.getSerialNumber(), request.getBranchId())
            );
        }
    }

    private void validateIssuance(Cpo cpo, IssueRequestDTO request) {
        if (cpo.getIssuedDate() != null) {
            throw new BusinessRuleException(
                    String.format("CPO %s is already issued", cpo.getSerialNumber())
            );
        }

        if (!cpo.getBranchId().equals(request.getBranchId())) {
            throw new BusinessRuleException(
                    String.format("CPO %s does not belong to branch %s",
                            cpo.getSerialNumber(), request.getBranchId())
            );
        }
    }

    private void validateIssuance(PassBook passBook, IssueRequestDTO request) {
        if (passBook.getIssuedDate() != null) {
            throw new BusinessRuleException(
                    String.format("PassBook %s is already issued", passBook.getSerialNumber())
            );
        }

        if (!passBook.getBranchId().equals(request.getBranchId())) {
            throw new BusinessRuleException(
                    String.format("PassBook %s does not belong to branch %s",
                            passBook.getSerialNumber(), request.getBranchId())
            );
        }
    }

    private void validateReturn(CheckBook checkBook) {
        if (checkBook.getIssuedDate() == null) {
            throw new BusinessRuleException(
                    String.format("CheckBook %s is not issued", checkBook.getSerialNumber())
            );
        }

        if (checkBook.getReturnedDate() != null) {
            throw new BusinessRuleException(
                    String.format("CheckBook %s is already returned", checkBook.getSerialNumber())
            );
        }
    }

    private void validateReturn(Cpo cpo) {
        if (cpo.getIssuedDate() == null) {
            throw new BusinessRuleException(
                    String.format("CPO %s is not issued", cpo.getSerialNumber())
            );
        }

        if (cpo.getReturnedDate() != null) {
            throw new BusinessRuleException(
                    String.format("CPO %s is already returned", cpo.getSerialNumber())
            );
        }
    }

    private void validateReturn(PassBook passBook) {
        if (passBook.getIssuedDate() == null) {
            throw new BusinessRuleException(
                    String.format("PassBook %s is not issued", passBook.getSerialNumber())
            );
        }

        if (passBook.getReturnedDate() != null) {
            throw new BusinessRuleException(
                    String.format("PassBook %s is already returned", passBook.getSerialNumber())
            );
        }
    }

    private BatchResponseDTO createBatchResponse(BookParent parent, String bookType, int childrenCreated) {
        BatchResponseDTO response = new BatchResponseDTO();
        response.setParentId(parent.getId());
        response.setBookType(bookType);
        response.setStartSerial(parent.getStartingSerial());
        response.setEndSerial(parent.getEndingSerial());
        response.setNumOfPad(parent.getNumOfPad());
        response.setUsed(parent.getUsed());
        response.setAvailable(parent.getNumOfPad() - parent.getUsed());
        response.setChildrenCreated(childrenCreated);
        response.setMessage(
                String.format("%s batch registered successfully. %d items created.",
                        bookType, childrenCreated)
        );
        return response;
    }

    // ============== IMPLEMENTATION OF OTHER METHODS ==============

    @Override
    public Optional<BookParent> findById(Long id) {
        return bookParentRepository.findById(id);
    }

    @Override
    public List<BookParent> findAll() {
        return bookParentRepository.findAll();
    }

    @Override
    public List<BookParent> findByBranchId(String branchId) {
        return bookParentRepository.findByBranchId(branchId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BookParent parent = bookParentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", id));

        // Check if any books are issued
        if (parent.getUsed() > 0) {
            throw new BusinessRuleException(
                    "Cannot delete batch. Some books have been issued."
            );
        }

        bookParentRepository.delete(parent);
        log.info("Deleted BookParent with ID: {}", id);
    }

    @Override
    public Optional<BookParent> findBySerialRange(String serialNumber) {
        return bookParentRepository.findBySerialInRange(serialNumber);
    }

    @Override
    public boolean isSerialIssuable(String serialNumber, String branchId) {
        // Check if serial exists and is not issued
        Optional<CheckBook> checkBook = checkBookRepository.findBySerialNumber(serialNumber);
        if (checkBook.isPresent()) {
            if (checkBook.get().getIssuedDate() != null) return false;

            // Check sequential rule
            BookParent parent = checkBook.get().getBookParent();
            List<CheckBook> allBooks = checkBookRepository.findByBookParentOrderBySerialNumberAsc(parent);

            int currentIndex = -1;
            for (int i = 0; i < allBooks.size(); i++) {
                if (Objects.equals(allBooks.get(i).getSerialNumber(), serialNumber)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex > 0) {
                CheckBook previousBook = allBooks.get(currentIndex - 1);
                return previousBook.getIssuedDate() != null;
            }

            return currentIndex == 0; // First in sequence is always issuable
        }

        // Similar logic for CPO and PassBook...
        return false;
    }

    @Override
    public String getNextIssuableSerial(String branchId, String bookType) {
        List<BookParent> parents = bookParentRepository.findByBranchId(branchId);

        for (BookParent parent : parents) {
            switch (bookType.toUpperCase()) {
                case "CHECKBOOK":
                    List<CheckBook> checkBooks = checkBookRepository
                            .findByBookParentOrderBySerialNumberAsc(parent);
                    for (CheckBook book : checkBooks) {
                        if (book.getIssuedDate() == null) {
                            // Check sequential rule
                            int index = checkBooks.indexOf(book);
                            if (index == 0 || checkBooks.get(index - 1).getIssuedDate() != null) {
                                return book.getSerialNumber();
                            }
                        }
                    }
                    break;

                case "CPO":
                    // Similar logic for CPO
                    break;

                case "PASSBOOK":
                    // Similar logic for PassBook
                    break;
            }
        }

        return null; // No issuable serial found
    }

    @Override
    public int getAvailableCount(Long parentId) {
        BookParent parent = bookParentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", parentId));
        return parent.getNumOfPad() - parent.getUsed();
    }

    @Override
    public int getUsedCount(Long parentId) {
        BookParent parent = bookParentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", parentId));
        return parent.getUsed();
    }

    @Override
    public boolean isBatchComplete(Long parentId) {
        BookParent parent = bookParentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("BookParent", "id", parentId));
        return parent.getUsed() == parent.getNumOfPad();
    }
}