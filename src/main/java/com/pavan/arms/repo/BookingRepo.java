package com.pavan.arms.repo;

import com.pavan.arms.entity.AirPlane;
import com.pavan.arms.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepo extends JpaRepository<Booking,Integer> {



}
