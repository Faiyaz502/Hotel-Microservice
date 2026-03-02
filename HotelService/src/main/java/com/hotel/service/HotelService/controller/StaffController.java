package com.hotel.service.HotelService.controller;

import com.hotel.service.HotelService.Dto.PaginatedResponse;
import com.hotel.service.HotelService.Dto.StaffProjection;
import com.hotel.service.HotelService.services.StuffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StuffService staffService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<StaffProjection>> getAllStaffs(
            @RequestParam(required = false) String hotelId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String lastId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(staffService.getStaffPaginated(hotelId, role, lastId, size));
    }
}
