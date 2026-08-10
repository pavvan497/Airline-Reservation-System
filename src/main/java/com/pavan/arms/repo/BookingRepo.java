package com.pavan.arms.repo;

import com.pavan.arms.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepo extends JpaRepository<Booking,Integer> {
    List<Booking> findByUserEmailOrderByTravelDateDescIdDesc(String userEmail);

}
