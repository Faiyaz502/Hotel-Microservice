package com.example.BookingService.controller;

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

    @PostMapping("/sync-inventory")
    public ResponseEntity<String> forceSync() {
        inventoryScheduler.syncAllInventory();
        return ResponseEntity.ok("Inventory generation triggered for 1 year ahead.");
    }


}
