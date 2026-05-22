package com.labournow.backend.controller;

import com.labournow.backend.dto.BookingDto;
import com.labournow.backend.dto.BookingRequest;
import com.labournow.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            return ResponseEntity.ok(BookingDto.from(bookingService.createBooking(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Accept a booking by the worker identified by workerPhone query param.
     * No JWT required — the phone acts as the worker identity token here.
     */
    @PostMapping("/{bookingId}/accept")
    public ResponseEntity<?> acceptBooking(
            @PathVariable Long bookingId,
            @RequestParam String workerPhone) {
        try {
            return ResponseEntity.ok(bookingService.acceptBookingByPhone(bookingId, workerPhone));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{bookingId}/decline")
    public ResponseEntity<?> declineBooking(@PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(bookingService.declineBooking(bookingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long bookingId) {
        try {
            return ResponseEntity.ok(bookingService.completeBooking(bookingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Admin panel — all bookings as safe DTOs (no lazy-load issues) */
    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookingsAsDto());
    }

    /**
     * Worker app — bookings assigned to this worker only.
     * Returns clean DTOs (no lazy-load issues).
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyBookings(@RequestParam String workerPhone) {
        return ResponseEntity.ok(bookingService.getBookingsByWorkerPhone(workerPhone));
    }
}

