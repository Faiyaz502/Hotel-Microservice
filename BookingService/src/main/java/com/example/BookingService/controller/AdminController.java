package com.example.BookingService.controller;

import com.example.BookingService.scheduler.InventoryGapScheduler;
import com.example.BookingService.scheduler.InventoryScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final InventoryScheduler inventoryScheduler;
    private final InventoryGapScheduler inventoryGapScheduler;

    @PostMapping("/sync-inventory")
    public ResponseEntity<String> forceSync() {
        inventoryScheduler.syncAllInventory();
        return ResponseEntity.ok("Inventory generation triggered for 1 year ahead (Target Date: today + 365 days).");
    }

    @PostMapping("/fill-gaps")
    public ResponseEntity<String> forceGapFill() {
        inventoryGapScheduler.fillNextTwoMonthsInventory();
        return ResponseEntity.ok("Inventory gap-filler triggered for the next 60 days.");
    }
}