package cbo.risk.sms.controllers;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.dtos.BatchRegistrationDTO;
import cbo.risk.sms.dtos.BatchResponseDTO;
import cbo.risk.sms.dtos.IssueRequestDTO;
import cbo.risk.sms.dtos.ReturnRequestDTO;
import cbo.risk.sms.models.BookParent;
import cbo.risk.sms.services.BookParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/book-parents")
@RequiredArgsConstructor
@Tag(name = "Book Parent Management", description = "APIs for managing book parent batches")
public class BookParentController {
@Autowired
    private  BookParentService bookParentService;

    @PostMapping("/checkbooks/register")
    @Operation(summary = "Register a new batch of CheckBooks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CheckBook batch registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Serial numbers already exist")
    })
    public ResponseEntity<BatchResponseDTO> registerCheckBookBatch(
            @Valid @RequestBody BatchRegistrationDTO registrationDTO) {
        System.out.println(registrationDTO.getParentBookType());
        System.out.println(registrationDTO.getCheckBookType());
        BatchResponseDTO response = bookParentService.registerCheckBookBatch(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/cpos/register")
    @Operation(summary = "Register a new batch of CPOs")
    public ResponseEntity<BatchResponseDTO> registerCpoBatch(
            @Valid @RequestBody BatchRegistrationDTO registrationDTO) {
        System.out.println(registrationDTO);
        BatchResponseDTO response = bookParentService.registerCpoBatch(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/passbooks/register")
    @Operation(summary = "Register a new batch of PassBooks")
    public ResponseEntity<BatchResponseDTO> registerPassBookBatch(
            @Valid @RequestBody BatchRegistrationDTO registrationDTO) {
        System.out.println(registrationDTO);
        BatchResponseDTO response = bookParentService.registerPassBookBatch(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/issue")
    @Operation(summary = "Issue a book to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book issued successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid issue request or sequential rule violation"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "409", description = "Book already issued")
    })
    public ResponseEntity<BatchResponseDTO> issueBook(
            @Valid @RequestBody IssueRequestDTO issueRequest) {
        BatchResponseDTO response = bookParentService.issueBook(issueRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/return")
    @Operation(summary = "Return an issued book")
    public ResponseEntity<BatchResponseDTO> returnBook(
            @Valid @RequestBody ReturnRequestDTO returnRequest) {
        BatchResponseDTO response = bookParentService.returnBook(returnRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book parent batch by ID")
    public ResponseEntity<BatchResponseDTO> getBookParentById(
            @Parameter(description = "ID of the book parent batch")
            @PathVariable Long id) {
        return bookParentService.findById(id)
                .map(parent -> {
                    BatchResponseDTO response = new BatchResponseDTO();
                    response.setParentId(parent.getId());
                    response.setParentBookType(parent.getParentBookType().name());
                    response.setBookType(parent.getParentBookType().name());
                    response.setStartSerial(parent.getStartingSerial());
                    response.setEndSerial(parent.getEndingSerial());
                    response.setNumOfPad(parent.getNumOfPad());
                    response.setUsed(parent.getUsed());
                    response.setAvailable(parent.getNumOfPad() - parent.getUsed());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all book parent batches")
    public ResponseEntity<List<BookParent>> getAllBookParents() {
        List<BookParent> parents = bookParentService.findAll();
        return ResponseEntity.ok(parents);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get all book parent batches by branch")
    public ResponseEntity<List<BookParent>> getBookParentsByBranch(
            @PathVariable String branchId) {
        List<BookParent> parents = bookParentService.findByBranchId(branchId);
        return ResponseEntity.ok(parents);
    }

    @GetMapping("/serial/{serialNumber}")
    @Operation(summary = "Find book parent by serial number")
    public ResponseEntity<BatchResponseDTO> getParentBySerial(
            @PathVariable String serialNumber) {
        return bookParentService.findBySerialRange(serialNumber)
                .map(parent -> {
                    BatchResponseDTO response = new BatchResponseDTO();
                    response.setParentId(parent.getId());
                    response.setStartSerial(parent.getStartingSerial());
                    response.setEndSerial(parent.getEndingSerial());
                    response.setNumOfPad(parent.getNumOfPad());
                    response.setUsed(parent.getUsed());
                    response.setAvailable(parent.getNumOfPad() - parent.getUsed());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/serial/{serialNumber}/issuable")
    @Operation(summary = "Check if a serial number is issuable")
    public ResponseEntity<Boolean> isSerialIssuable(
            @PathVariable String serialNumber,
            @RequestParam String branchId) {
        boolean issuable = bookParentService.isSerialIssuable(serialNumber, branchId);
        return ResponseEntity.ok(issuable);
    }

    @GetMapping("/next-issuable")
    @Operation(summary = "Get next issuable serial number")
    public ResponseEntity<String> getNextIssuableSerial(
            @RequestParam String branchId,
            @RequestParam String bookType) {
        String nextSerial = bookParentService.getNextIssuableSerial(branchId, bookType);
        return nextSerial != null ?
                ResponseEntity.ok(nextSerial) :
                ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/available-count")
    @Operation(summary = "Get available count for a batch")
    public ResponseEntity<Integer> getAvailableCount(@PathVariable Long id) {
        int available = bookParentService.getAvailableCount(id);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/{id}/used-count")
    @Operation(summary = "Get used count for a batch")
    public ResponseEntity<Integer> getUsedCount(@PathVariable Long id) {
        int used = bookParentService.getUsedCount(id);
        return ResponseEntity.ok(used);
    }

    @GetMapping("/{id}/complete")
    @Operation(summary = "Check if batch is complete (all issued)")
    public ResponseEntity<Boolean> isBatchComplete(@PathVariable Long id) {
        boolean complete = bookParentService.isBatchComplete(id);
        return ResponseEntity.ok(complete);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book parent batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Batch deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete - books have been issued"),
            @ApiResponse(responseCode = "404", description = "Batch not found")
    })
    public ResponseEntity<Void> deleteBookParent(@PathVariable Long id) {
        bookParentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}