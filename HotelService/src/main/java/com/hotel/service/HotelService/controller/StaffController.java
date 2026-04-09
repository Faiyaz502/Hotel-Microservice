package com.hotel.service.HotelService.controller;

import com.hotel.service.HotelService.Dto.PaginatedResponse;
import com.hotel.service.HotelService.Dto.StaffProjection;
import com.hotel.service.HotelService.schedulers.FullDatabaseSyncService;
import com.hotel.service.HotelService.services.StuffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StuffService staffService;
    private final FullDatabaseSyncService syncService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<StaffProjection>> getAllStaffs(
            @RequestParam(required = false) String hotelId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String lastId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(staffService.getStaffPaginated(hotelId, role, lastId, size));
    }

    @PostMapping("/all")
    public String triggerFullSync() {
        syncService.syncAllDataToReplicas();
        return "Sync triggered. Check logs for details.";
    }
}
