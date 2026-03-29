package com.example.BookingService.projection;

import java.time.LocalDate;

public interface InventoryProjection {

    String getHotelId();

    String getRoomTypeId();

    LocalDate getInventoryDate();

    int getTotalCapacity();

    int getBookedCount();
}
