package com.labournow.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookingRequest {
    private String customerPhone; // identifies the customer (permit-all endpoint)
    private String category;
    private Double latitude;
    private Double longitude;
    private String addressDetails;
    private String problemDescription;
    private java.math.BigDecimal estimatedPrice;
    private Long workerId; // Optional: target a specific worker
}
