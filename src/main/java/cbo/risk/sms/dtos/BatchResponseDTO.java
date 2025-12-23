package cbo.risk.sms.dtos;

import lombok.Data;


public class BatchResponseDTO {
    private Long parentId;
    private String bookType;
    private String startSerial;
    private String endSerial;
    private int numOfPad;
    private int used;
    private int available;
    private String message;
    private int childrenCreated;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getBookType() {
        return bookType;
    }

    public void setBookType(String bookType) {
        this.bookType = bookType;
    }

    public String getStartSerial() {
        return startSerial;
    }

    public void setStartSerial(String startSerial) {
        this.startSerial = startSerial;
    }

    public String getEndSerial() {
        return endSerial;
    }

    public void setEndSerial(String endSerial) {
        this.endSerial = endSerial;
    }

    public int getNumOfPad() {
        return numOfPad;
    }

    public void setNumOfPad(int numOfPad) {
        this.numOfPad = numOfPad;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getChildrenCreated() {
        return childrenCreated;
    }

    public void setChildrenCreated(int childrenCreated) {
        this.childrenCreated = childrenCreated;
    }
}
