package cbo.risk.sms.dtos;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
@Getter
@Setter
public class BookParentDTO {

    private Long id;
    @NotBlank
    @Column(nullable = false)
    private int numOfPad;
    private int used;


    @NotBlank
    @Column(name = "BRANCH_ID")
    private String branchId;


    @NotBlank
    @Column(name = "SUBPROCESS_ID")
    private String subProcessId;
    @NotBlank
    @Column(name = "PROCESS_ID")
    private String processId;


    private String createdBy;

    private String createdById;



    private String lastUpdatedBy;

    private String lastUpdatedById;
    @Column(name = "CREATED_TS",nullable = false)
    @CreationTimestamp
    private LocalDateTime createdTimestamp;
    @UpdateTimestamp
    @Column(name = "MODIFIED_TS",nullable = false)
    private LocalDateTime modifiedTimestamp;
}
