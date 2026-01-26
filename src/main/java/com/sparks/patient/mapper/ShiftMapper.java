package com.sparks.patient.mapper;

import org.springframework.stereotype.Component;

import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.dto.ShiftResponse;
import com.sparks.patient.entity.Shift;

/**
 * Mapper for converting between Shift entity and DTOs
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
@Component
public class ShiftMapper {

    /**
     * Convert ShiftRequest DTO to Shift entity
     */
    public Shift toEntity(ShiftRequest request) {
        if (request == null) {
            return null;
        }
        
        return Shift.builder()
                .doctorId(request.getDoctorId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .room(request.getRoom())
                .build();
    }

    /**
     * Convert Shift entity to ShiftResponse DTO
     */
    public ShiftResponse toResponse(Shift shift) {
        if (shift == null) {
            return null;
        }
        
        return ShiftResponse.builder()
                .id(shift.getId())
                .doctorId(shift.getDoctorId())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .room(shift.getRoom())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Shift entity with data from ShiftRequest
     */
    public void updateEntity(Shift shift, ShiftRequest request) {
        if (shift == null || request == null) {
            return;
        }
        
        shift.setDoctorId(request.getDoctorId());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setRoom(request.getRoom());
    }
}
