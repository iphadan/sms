package cbo.risk.sms.enums;


import lombok.Data;
import lombok.Getter;

/**
 * Enum representing different types of checkbooks based on the number of leaves/pages.
 * Used for stock management and tracking of checkbook inventory.
 */

@Getter

public enum CheckBookLeaveType {

    /**
     * Checkbook with 10 leaves/pages
     */
    TEN_LEAVES(10),

    /**
     * Checkbook with 25 leaves/pages
     */
    TWENTY_FIVE_LEAVES(25),

    /**
     * Checkbook with 50 leaves/pages
     */
    FIFTY_LEAVES(50),

    /**
     * Checkbook with 100 leaves/pages
     */
    ONE_HUNDRED_LEAVES(100);

    private final int numberOfLeaves;

    CheckBookLeaveType(int numberOfLeaves) {
        this.numberOfLeaves = numberOfLeaves;
    }

    /**
     * Get display name for UI purposes
     */
    public String getDisplayName() {
        return this.numberOfLeaves + " Leaves";
    }

    /**
     * Get the enum value from number of leaves
     */
    public static CheckBookLeaveType fromLeaves(int leaves) {
        for (CheckBookLeaveType type : values()) {
            if (type.numberOfLeaves == leaves) {
                return type;
            }
        }
        throw new IllegalArgumentException("No checkbook type with " + leaves + " leaves");
    }

    /**
     * Get the enum value from string representation
     */
    public static CheckBookLeaveType fromString(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkbook type cannot be null or empty");
        }

        // Try to parse from various formats
        String normalized = typeStr.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        try {
            return CheckBookLeaveType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Try to parse from "10_Leaves" format
            for (CheckBookLeaveType type : values()) {
                if (normalized.equals(type.numberOfLeaves + "_LEAVES")) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid checkbook type: " + typeStr);
        }
    }
}