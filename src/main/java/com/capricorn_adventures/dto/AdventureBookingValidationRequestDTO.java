package com.capricorn_adventures.dto;

public class AdventureBookingValidationRequestDTO {
    private Integer age;
    private Long scheduleId;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
