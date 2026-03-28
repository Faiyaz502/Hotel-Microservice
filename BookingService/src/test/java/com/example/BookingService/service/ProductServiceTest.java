package com.example.BookingService.service;

import com.example.BookingService.repository.InventoryRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    InventoryRepo inventoryRepo;




    @InjectMocks
    BookingService bookingService;

    @Test
    void initiateHoldTest(){


        System.out.println("InitiateBooking Test");

        String hotelId = "9a1f15c2-f9b4-4912-9d72-2fe5d5369825";
        String roomTypeID = "5ee19c87-a682-43a3-bcd9-325ea09946ba";
        LocalDate checkIn = LocalDate.ofEpochDay(2027- 3 -22);
        LocalDate checkout = LocalDate.ofEpochDay(2027- 3 -22);
        String userId = "d5493297-10b2-4bbe-b3ef-c6f929618b9b";
        String idmKey = "6846516846816417447435434";





        bookingService.initiateHold(hotelId,roomTypeID,checkIn,checkout,userId,idmKey);





    }





}
