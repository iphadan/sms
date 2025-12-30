package cbo.risk.sms.controllers;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.models.RequestCheckBook;
import cbo.risk.sms.services.CheckBookService;
import cbo.risk.sms.services.impl.CheckBookServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.Audited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Path;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/checkbooks")
@Tag(name = "CheckBook Management", description = "APIs for managing individual CheckBooks")
public class CheckBookController {
    private final CheckBookServiceImpl checkBookService;

    public CheckBookController(CheckBookServiceImpl checkBookService) {
        this.checkBookService = checkBookService;
    }

    @PostMapping
    @Operation(summary = "Create a new CheckBook")
    public ResponseEntity<CheckBookDTO> createCheckBook(
            @Valid @RequestBody CheckBookCreateDTO createDTO) {
        CheckBookDTO created = checkBookService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get CheckBook by ID")
    public ResponseEntity<CheckBookDTO> getCheckBookById(@PathVariable Long id) {
        return checkBookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/serial/{serialNumber}")
    @Operation(summary = "Get CheckBook by serial number")
    public ResponseEntity<CheckBookDTO> getCheckBookBySerial(@PathVariable String serialNumber) {
        return checkBookService.findBySerialNumber(serialNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all CheckBooks")
    public ResponseEntity<List<CheckBookDTO>> getAllCheckBooks() {
        List<CheckBookDTO> checkBooks = checkBookService.findAll();
        return ResponseEntity.ok(checkBooks);
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all CheckBooks with pagination")
    public ResponseEntity<Page<CheckBookDTO>> getAllCheckBooksPaged(Pageable pageable) {
        Page<CheckBookDTO> checkBooks = checkBookService.findAll(pageable);
        return ResponseEntity.ok(checkBooks);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get CheckBooks by branch")
    public ResponseEntity<List<CheckBookDTO>> getCheckBooksByBranch(@PathVariable String branchId) {
        List<CheckBookDTO> checkBooks = checkBookService.findByBranchId(branchId);
        return ResponseEntity.ok(checkBooks);
    }

    @GetMapping("/branch/{branchId}/available")
    @Operation(summary = "Get available CheckBooks by branch")
    public ResponseEntity<List<CheckBookDTO>> getAvailableCheckBooksByBranch(@PathVariable String branchId) {
        List<CheckBookDTO> checkBooks = checkBookService.findAvailableByBranch(branchId);
        return ResponseEntity.ok(checkBooks);
    }

    @GetMapping("/branch/{branchId}/issued")
    @Operation(summary = "Get issued CheckBooks by branch")
    public ResponseEntity<List<RequestCheckBookDTO>> getIssuedCheckBooksByBranch(@PathVariable String branchId) {
        List<RequestCheckBookDTO> checkBooks = checkBookService.findIssuedRequestCheckBookByBranch(branchId);
        return ResponseEntity.ok(checkBooks);
    }



    @GetMapping("/parent/{parentId}")
    @Operation(summary = "Get CheckBooks by parent batch")
    public ResponseEntity<List<CheckBookDTO>> getCheckBooksByParent(@PathVariable Long parentId) {
        List<CheckBookDTO> checkBooks = checkBookService.findByBookParentId(parentId);
        return ResponseEntity.ok(checkBooks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update CheckBook")
    public ResponseEntity<CheckBookDTO> updateCheckBook(
            @PathVariable Long id,
            @Valid @RequestBody CheckBookUpdateDTO updateDTO) {
        CheckBookDTO updated = checkBookService.update(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Issue a CheckBook")
    public ResponseEntity<CheckBookDTO> issueCheckBook(
            @PathVariable Long id,
            @RequestParam String issuedBy) {
        CheckBookDTO issued = checkBookService.issueItem(id, issuedBy);
        return ResponseEntity.ok(issued);
    }

    @PostMapping("/issue")
    @Operation(summary = "Issue a CheckBook")
    public ResponseEntity<ResponseDTO<RequestCheckBookDTO>> issueCheckBook(
            @RequestBody RequestCheckBookDTO requestCheckBookDTO) {
        System.out.println(requestCheckBookDTO);
        ResponseDTO<RequestCheckBookDTO> issued = checkBookService.issueAvailableCheckBook(requestCheckBookDTO);
        return ResponseEntity.ok(issued);
    }



    @PostMapping("/{id}/return")
    @Operation(summary = "Return a CheckBook")
    public ResponseEntity<CheckBookDTO> returnCheckBook(
            @PathVariable Long id,
            @RequestParam String returnedBy) {
        CheckBookDTO returned = checkBookService.returnItem(id, returnedBy);
        return ResponseEntity.ok(returned);
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive a CheckBook")
    public ResponseEntity<RequestCheckBookDTO> receiveCheckBook(
            @RequestBody RequestCheckBookDTO requestCheckBookDTO,
    @PathVariable("id") Long id) {
        System.out.println(requestCheckBookDTO);
        RequestCheckBookDTO received = checkBookService.receiveItem(requestCheckBookDTO);
        return ResponseEntity.ok(received);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a CheckBook")
    public ResponseEntity<Void> deleteCheckBook(@PathVariable Long id) {
        checkBookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/branch/{branchId}")
    @Operation(summary = "Get CheckBook statistics by branch")
    public ResponseEntity<?> getCheckBookStats(@PathVariable String branchId) {
        // Return counts by status
        long available = checkBookService.countByBranchAndStatus(branchId, "available");
        long issued = checkBookService.countByBranchAndStatus(branchId, "issued");
        long returned = checkBookService.countByBranchAndStatus(branchId, "returned");

        return ResponseEntity.ok(new Object() {
            public final long availableCount = available;
            public final long issuedCount = issued;
            public final long returnedCount = returned;
            public final long totalCount = available + issued + returned;
        });
    }
}