package cbo.risk.sms.controllers;

import cbo.risk.sms.dtos.*;
import cbo.risk.sms.models.RequestCpo;
import cbo.risk.sms.services.CpoService;
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

@RestController
@RequestMapping("/api/v1/cpos")
@RequiredArgsConstructor
@Tag(name = "CPO Management", description = "APIs for managing individual CPOs")
public class CpoController {
@Autowired
    private  CpoService cpoService;

    @PostMapping
    @Operation(summary = "Create a new CPO")
    public ResponseEntity<CpoDTO> createCpo(@Valid @RequestBody CpoCreateDTO createDTO) {
        CpoDTO created = cpoService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get CPO by ID")
    public ResponseEntity<CpoDTO> getCpoById(@PathVariable Long id) {
        return cpoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/serial/{serialNumber}")
    @Operation(summary = "Get CPO by serial number")
    public ResponseEntity<CpoDTO> getCpoBySerial(@PathVariable String serialNumber) {
        return cpoService.findBySerialNumber(serialNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all CPOs")
    public ResponseEntity<List<CpoDTO>> getAllCpos() {
        List<CpoDTO> cpos = cpoService.findAll();
        return ResponseEntity.ok(cpos);
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all CPOs with pagination")
    public ResponseEntity<Page<CpoDTO>> getAllCposPaged(Pageable pageable) {
        Page<CpoDTO> cpos = cpoService.findAll(pageable);
        return ResponseEntity.ok(cpos);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get CPOs by branch")
    public ResponseEntity<List<CpoDTO>> getCposByBranch(@PathVariable String branchId) {
        List<CpoDTO> cpos = cpoService.findByBranchId(branchId);
        return ResponseEntity.ok(cpos);
    }

    @GetMapping("/branch/{branchId}/available")
    @Operation(summary = "Get available CPOs by branch")
    public ResponseEntity<List<CpoDTO>> getAvailableCposByBranch(@PathVariable String branchId) {
        List<CpoDTO> cpos = cpoService.findAvailableByBranch(branchId);
        return ResponseEntity.ok(cpos);
    }

    @GetMapping("/branch/{branchId}/issued")
    @Operation(summary = "Get issued CPOs by branch")
    public ResponseEntity<List<CpoDTO>> getIssuedCposByBranch(@PathVariable String branchId) {
        List<CpoDTO> cpos = cpoService.findIssuedByBranch(branchId);
        return ResponseEntity.ok(cpos);
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "Get CPOs by parent batch")
    public ResponseEntity<List<CpoDTO>> getCposByParent(@PathVariable Long parentId) {
        List<CpoDTO> cpos = cpoService.findByBookParentId(parentId);
        return ResponseEntity.ok(cpos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update CPO")
    public ResponseEntity<CpoDTO> updateCpo(
            @PathVariable Long id,
            @Valid @RequestBody CpoUpdateDTO updateDTO) {
        CpoDTO updated = cpoService.update(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Issue a CPO")
    public ResponseEntity<CpoDTO> issueCpo(
            @PathVariable Long id,
            @RequestParam String issuedBy) {
        CpoDTO issued = cpoService.issueItem(id, issuedBy);
        return ResponseEntity.ok(issued);
    }

    @PostMapping("/issue")
    @Operation(summary = "Issue a CheckBook")
    public ResponseEntity<ResponseDTO<RequestCpoDTO>> issueCheckBook(
            @RequestBody RequestCpoDTO requestCpoDTO) {
        System.out.println(requestCpoDTO);
        ResponseDTO<RequestCpoDTO> issued = cpoService.issueAvailableCpo(requestCpoDTO);
        return ResponseEntity.ok(issued);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a CPO")
    public ResponseEntity<CpoDTO> returnCpo(
            @PathVariable Long id,
            @RequestParam String returnedBy) {
        CpoDTO returned = cpoService.returnItem(id, returnedBy);
        return ResponseEntity.ok(returned);
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive a CPO")
    public ResponseEntity<CpoDTO> receiveCpo(
            @PathVariable Long id,
            @RequestBody RequestCpoDTO requestCpoDTO){
        CpoDTO received = cpoService.receiveItem( requestCpoDTO);
        return ResponseEntity.ok(received);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a CPO")
    public ResponseEntity<Void> deleteCpo(@PathVariable Long id) {
        cpoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/branch/{branchId}")
    @Operation(summary = "Get CPO statistics by branch")
    public ResponseEntity<?> getCpoStats(@PathVariable String branchId) {
        long available = cpoService.countByBranchAndStatus(branchId, "available");
        long issued = cpoService.countByBranchAndStatus(branchId, "issued");
        long returned = cpoService.countByBranchAndStatus(branchId, "returned");

        return ResponseEntity.ok(new Object() {
            public final long availableCount = available;
            public final long issuedCount = issued;
            public final long returnedCount = returned;
            public final long totalCount = available + issued + returned;
        });
    }

}