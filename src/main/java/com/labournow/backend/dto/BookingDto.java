package com.labournow.backend.dto;

import com.labournow.backend.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Safe serializable DTO for Booking — avoids Jackson lazy-loading
 * infinite loops when returning Booking entities directly from controllers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;
    private String category;
    private String status;
    private Double latitude;
    private Double longitude;
    private String addressDetails;
    private String problemDescription;
    private String eta;
    private BigDecimal estimatedPrice;
    private LocalDateTime createdAt;

    // Flattened customer info
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerProfilePictureUrl;

    // Flattened worker info (nullable)
    private Long workerId;
    private String workerName;
    private String workerPhone;

    /** Factory method — converts a JPA Booking entity to this DTO safely. */
    public static BookingDto from(Booking b) {
        BookingDtoBuilder dto = BookingDto.builder()
                .id(b.getId())
                .category(b.getCategory())
                .status(b.getStatus() != null ? b.getStatus().name() : "PENDING")
                .latitude(b.getLatitude())
                .longitude(b.getLongitude())
                .addressDetails(b.getAddressDetails())
                .problemDescription(b.getProblemDescription())
                .eta(b.getEta())
                .estimatedPrice(b.getEstimatedPrice())
                .createdAt(b.getCreatedAt());

        if (b.getCustomer() != null) {
            dto.customerId(b.getCustomer().getId())
               .customerName(b.getCustomer().getName())
               .customerPhone(b.getCustomer().getPhone())
               .customerProfilePictureUrl(
                   b.getCustomer().getProfilePictureUrl() != null
                       ? b.getCustomer().getProfilePictureUrl()
                       : "https://i.pravatar.cc/150?u=" + b.getCustomer().getPhone()
               );
        }

        if (b.getWorker() != null) {
            dto.workerId(b.getWorker().getId())
               .workerName(b.getWorker().getName())
               .workerPhone(b.getWorker().getPhone());
        }

        return dto.build();
    }
}
