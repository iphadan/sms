package cbo.risk.sms.models;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
@Entity
@Data
public class CheckBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckBookType checkBookType;
    @NotBlank
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckBookLeaveType checkBookLeaveType;
    private String startingSerialNum;
    private String endSerialNum;


    @NotBlank
    @Column(nullable = false)
    private int numOfBook;
}