package com.labournow.backend.repository;

import com.labournow.backend.entity.Booking;
import com.labournow.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Booking> findByWorkerOrderByCreatedAtDesc(User worker);
    List<Booking> findByStatus(Booking.Status status);

    // Fetch bookings assigned to a specific worker (by phone), eagerly loading customer + worker
    @Query("SELECT b FROM Booking b JOIN FETCH b.worker w JOIN FETCH b.customer WHERE w.phone = :phone")
    List<Booking> findByWorkerPhoneEager(@Param("phone") String phone);

    // Fetch ALL bookings with customer and worker eagerly loaded (avoids N+1 / lazy-load issues)
    @Query("SELECT b FROM Booking b JOIN FETCH b.customer LEFT JOIN FETCH b.worker ORDER BY b.createdAt DESC")
    List<Booking> findAllEager();
}

