package cbo.risk.sms.controllers;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import cbo.risk.sms.models.RequestPassBook;
import cbo.risk.sms.services.PassBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/passbooks")
@RequiredArgsConstructor
@Tag(name = "PassBook Management", description = "APIs for managing individual PassBooks")
public class PassBookController {
@Autowired
    private  PassBookService passBookService;

    @PostMapping
    @Operation(summary = "Create a new PassBook")
    public ResponseEntity<PassBookDTO> createPassBook(
            @Valid @RequestBody PassBookCreateDTO createDTO) {
        PassBookDTO created = passBookService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get PassBook by ID")
    public ResponseEntity<PassBookDTO> getPassBookById(@PathVariable Long id) {
        return passBookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/serial/{serialNumber}")
    @Operation(summary = "Get PassBook by serial number")
    public ResponseEntity<PassBookDTO> getPassBookBySerial(@PathVariable String serialNumber) {
        return passBookService.findBySerialNumber(serialNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all PassBooks")
    public ResponseEntity<List<PassBookDTO>> getAllPassBooks() {
        List<PassBookDTO> passBooks = passBookService.findAll();
        return ResponseEntity.ok(passBooks);
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all PassBooks with pagination")
    public ResponseEntity<Page<PassBookDTO>> getAllPassBooksPaged(Pageable pageable) {
        Page<PassBookDTO> passBooks = passBookService.findAll(pageable);
        return ResponseEntity.ok(passBooks);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get PassBooks by branch")
    public ResponseEntity<List<PassBookDTO>> getPassBooksByBranch(@PathVariable String branchId) {
        List<PassBookDTO> passBooks = passBookService.findByBranchId(branchId);
        return ResponseEntity.ok(passBooks);
    }

    @GetMapping("/branch/{branchId}/available")
    @Operation(summary = "Get available PassBooks by branch")
    public ResponseEntity<List<PassBookDTO>> getAvailablePassBooksByBranch(@PathVariable String branchId) {
        List<PassBookDTO> passBooks = passBookService.findAvailableByBranch(branchId);
        return ResponseEntity.ok(passBooks);
    }

    @GetMapping("/branch/{branchId}/issued")
    @Operation(summary = "Get issued PassBooks by branch")
    public ResponseEntity<List<PassBookDTO>> getIssuedPassBooksByBranch(@PathVariable String branchId) {
        List<PassBookDTO> passBooks = passBookService.findIssuedByBranch(branchId);
        return ResponseEntity.ok(passBooks);
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "Get PassBooks by parent batch")
    public ResponseEntity<List<PassBookDTO>> getPassBooksByParent(@PathVariable Long parentId) {
        List<PassBookDTO> passBooks = passBookService.findByBookParentId(parentId);
        return ResponseEntity.ok(passBooks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update PassBook")
    public ResponseEntity<PassBookDTO> updatePassBook(
            @PathVariable Long id,
            @Valid @RequestBody PassBookUpdateDTO updateDTO) {
        PassBookDTO updated = passBookService.update(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Issue a PassBook")
    public ResponseEntity<PassBookDTO> issuePassBook(
            @PathVariable Long id,
            @RequestParam String issuedBy) {
        PassBookDTO issued = passBookService.issueItem(id, issuedBy);
        return ResponseEntity.ok(issued);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a PassBook")
    public ResponseEntity<PassBookDTO> returnPassBook(
            @PathVariable Long id,
            @RequestParam String returnedBy) {
        PassBookDTO returned = passBookService.returnItem(id, returnedBy);
        return ResponseEntity.ok(returned);
    }
    @PostMapping("/issue")
    @Operation(summary = "Issue a Pass Book")
    public ResponseEntity<ResponseDTO<RequestPassBookDTO>> issueCheckBook(
            @RequestBody RequestPassBookDTO requestPassBookDTO) {
        System.out.println(requestPassBookDTO);
        ResponseDTO<RequestPassBookDTO> issued = passBookService.issueAvailablePassBook(requestPassBookDTO);
        return ResponseEntity.ok(issued);
    }
    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive a Pass book")
    public ResponseEntity<RequestPassBookDTO> receivePassBook(
            @RequestBody RequestPassBookDTO requestPassBookDTO,
            @PathVariable("id") Long id) {
        System.out.println(requestPassBookDTO);
        RequestPassBookDTO received = passBookService.receiveItem(requestPassBookDTO);
        return ResponseEntity.ok(received);
    }
//    @PostMapping("/{id}/receive")
//    @Operation(summary = "Receive a PassBook")
//    public ResponseEntity<PassBookDTO> receivePassBook(
//            @PathVariable Long id,
//            @RequestParam String receivedBy) {
//        PassBookDTO received = passBookService.receiveItem(id, receivedBy);
//        return ResponseEntity.ok(received);
//    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a PassBook")
    public ResponseEntity<Void> deletePassBook(@PathVariable Long id) {
        passBookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/branch/{branchId}")
    @Operation(summary = "Get PassBook statistics by branch")
    public ResponseEntity<?> getPassBookStats(@PathVariable String branchId) {
        long available = passBookService.countByBranchAndStatus(branchId, "available");
        long issued = passBookService.countByBranchAndStatus(branchId, "issued");
        long returned = passBookService.countByBranchAndStatus(branchId, "returned");

        return ResponseEntity.ok(new Object() {
            public final long availableCount = available;
            public final long issuedCount = issued;
            public final long returnedCount = returned;
            public final long totalCount = available + issued + returned;
        });
    }
    @GetMapping("/type/{type}")
    @Operation(summary = "Get PassBooks by type")
    public ResponseEntity<List<PassBookDTO>> getPassBooksByType(@PathVariable PassBookType type) {
        List<PassBookDTO> passBooks = passBookService.findByPassBookType(type);
        return ResponseEntity.ok(passBooks);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get PassBooks by category")
    public ResponseEntity<List<PassBookDTO>> getPassBooksByCategory(@PathVariable PassBookCategory category) {
        List<PassBookDTO> passBooks = passBookService.findByPassBookCategory(category);
        return ResponseEntity.ok(passBooks);
    }

    @GetMapping("/type/{type}/category/{category}")
    @Operation(summary = "Get PassBooks by type and category")
    public ResponseEntity<List<PassBookDTO>> getPassBooksByTypeAndCategory(
            @PathVariable PassBookType type,
            @PathVariable PassBookCategory category) {
        List<PassBookDTO> passBooks = passBookService.findByPassBookTypeAndCategory(type, category);
        return ResponseEntity.ok(passBooks);
    }



    @GetMapping("/branch/{branchId}/type/{type}")
    @Operation(summary = "Get PassBooks by branch and type")
    public ResponseEntity<List<PassBookDTO>> getPassBooksByBranchAndType(
            @PathVariable String branchId,
            @PathVariable PassBookType type) {
        // This would need a custom service method or repository query
        // For now, filter in memory
        List<PassBookDTO> allPassBooks = passBookService.findByBranchId(branchId);
//        List<PassBookDTO> filtered = allPassBooks.stream()
//                .filter(pb -> type.equals(pb.getPassBookType()))
//                .collect(Collectors.toList());
        return ResponseEntity.ok(allPassBooks);
    }
}