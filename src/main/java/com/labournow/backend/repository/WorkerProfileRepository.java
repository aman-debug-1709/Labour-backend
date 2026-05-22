package com.labournow.backend.repository;

import com.labournow.backend.entity.User;
import com.labournow.backend.entity.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {

    Optional<WorkerProfile> findByUser(User user);
    
    // Custom query to find nearby available workers (Haversine formula approximation)
    @Query(value = "SELECT * FROM worker_profiles w " +
            "WHERE w.is_available = true AND w.is_verified = true " +
            "AND w.primary_skill = :skill " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(w.current_latitude)) * " +
            "cos(radians(w.current_longitude) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(w.current_latitude)))) < w.service_radius_km", nativeQuery = true)
    List<WorkerProfile> findNearbyAvailableWorkers(@Param("lat") Double lat, 
                                                   @Param("lng") Double lng, 
                                                   @Param("skill") String skill);
}
