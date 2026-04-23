package com.capricorn_adventures.entity;

public enum StaffDepartment {
    ROOM_SERVICE("Room Service"),
    HOUSEKEEPING("Housekeeping"),
    LAUNDRY("Laundry");

    private final String displayName;

    StaffDepartment(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
