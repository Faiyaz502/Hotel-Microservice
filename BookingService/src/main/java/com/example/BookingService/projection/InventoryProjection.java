package com.example.BookingService.projection;

import java.time.LocalDate;

public interface InventoryProjection {

    LocalDate getInventoryDate();

    int getTotalCapacity();

    int getBookedCount();
}
