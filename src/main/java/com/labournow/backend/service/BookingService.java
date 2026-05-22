package com.labournow.backend.service;

import com.labournow.backend.dto.BookingDto;
import com.labournow.backend.dto.BookingRequest;
import com.labournow.backend.entity.Booking;
import com.labournow.backend.entity.User;
import com.labournow.backend.repository.BookingRepository;
import com.labournow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Booking createBooking(BookingRequest request) {
        User customer = userRepository.findByPhone(request.getCustomerPhone())
                .orElseThrow(() -> new RuntimeException("Customer not found with phone: " + request.getCustomerPhone()));

        Booking.BookingBuilder builder = Booking.builder()
                .customer(customer)
                .category(request.getCategory())
                .status(Booking.Status.PENDING)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .addressDetails(request.getAddressDetails())
                .problemDescription(request.getProblemDescription())
                .estimatedPrice(request.getEstimatedPrice());

        if (request.getWorkerId() != null) {
            userRepository.findById(request.getWorkerId()).ifPresent(builder::worker);
        }

        Booking booking = bookingRepository.save(builder.build());
        messagingTemplate.convertAndSend("/topic/bookings/new", booking.getId());
        return booking;
    }

    @Transactional
    public Map<String, Object> acceptBookingByPhone(Long bookingId, String workerPhone, String eta) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new RuntimeException("Booking is no longer pending. Current status: " + booking.getStatus());
        }

        User worker = userRepository.findByPhone(workerPhone)
                .orElseThrow(() -> new RuntimeException("Worker not found with phone: " + workerPhone));

        booking.setWorker(worker);
        booking.setStatus(Booking.Status.ACCEPTED);
        if (eta != null && !eta.trim().isEmpty()) {
            booking.setEta(eta);
        }
        bookingRepository.save(booking);

        messagingTemplate.convertAndSend("/topic/customer/" + booking.getCustomer().getId(),
                "Booking accepted by " + worker.getName());

        return toWorkerBookingDto(booking);
    }

    @Transactional
    public Map<String, Object> declineBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);
        return toWorkerBookingDto(booking);
    }

    @Transactional
    public Map<String, Object> completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setStatus(Booking.Status.COMPLETED);
        bookingRepository.save(booking);
        return toWorkerBookingDto(booking);
    }

    /**
     * Returns bookings assigned to a worker (by their phone), as clean DTOs.
     * Uses an eager JPQL query — no lazy loading issues.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBookingsByWorkerPhone(String phone) {
        return bookingRepository.findByWorkerPhoneEager(phone)
                .stream()
                .map(this::toWorkerBookingDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookingsAsDto() {
        return bookingRepository.findAllEager()
                .stream()
                .map(BookingDto::from)
                .toList();
    }

    // ─── DTO helpers ────────────────────────────────────────────────────────────

    private Map<String, Object> toWorkerBookingDto(Booking b) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", b.getId());
        m.put("category", b.getCategory());
        m.put("status", b.getStatus() != null ? b.getStatus().name() : "PENDING");
        m.put("addressDetails", b.getAddressDetails() != null ? b.getAddressDetails() : "");
        m.put("problemDescription", b.getProblemDescription() != null ? b.getProblemDescription() : "");
        m.put("eta", b.getEta() != null ? b.getEta() : "");
        m.put("estimatedPrice", b.getEstimatedPrice() != null ? b.getEstimatedPrice().toString() : "0");
        m.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");

        if (b.getCustomer() != null) {
            Map<String, Object> cust = new HashMap<>();
            cust.put("id", b.getCustomer().getId());
            cust.put("name", b.getCustomer().getName());
            cust.put("phone", b.getCustomer().getPhone());
            cust.put("profilePictureUrl", b.getCustomer().getProfilePictureUrl() != null
                    ? b.getCustomer().getProfilePictureUrl()
                    : "https://i.pravatar.cc/150?u=" + b.getCustomer().getPhone());
            m.put("customer", cust);
        }

        if (b.getWorker() != null) {
            Map<String, Object> wrk = new HashMap<>();
            wrk.put("id", b.getWorker().getId());
            wrk.put("name", b.getWorker().getName());
            wrk.put("phone", b.getWorker().getPhone());
            m.put("worker", wrk);
        }

        return m;
    }
}
