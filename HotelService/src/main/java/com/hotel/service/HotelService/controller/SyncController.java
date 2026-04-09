package com.hotel.service.HotelService.controller;

import com.hotel.service.HotelService.schedulers.FullDatabaseSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sync")
@RequiredArgsConstructor
public class SyncController {
    private final FullDatabaseSyncService syncService;

    @PostMapping("/all")
    public String triggerFullSync() {
        syncService.syncAllDataToReplicas();
        return "Sync triggered. Check logs for details.";
    }
}
