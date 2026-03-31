package com.example.BookingService.service;

import com.example.BookingService.projection.InventoryProjection;
import com.example.BookingService.repository.InventoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class BookingServiceIntegrationTest {

    @Autowired
    RedisTemplate<String, String> redisTemplate;



    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> 6379);
    }

    @Autowired
    private BookingService bookingService;

    @MockBean
    private InventoryRepo inventoryRepo;


    private final String hotelId = "9a1f15c2-f9b4-4912-9d72-2fe5d5369825";
    private final String roomTypeId = "5ee19c87-a682-43a3-bcd9-325ea09946ba";
    private final LocalDate checkIn = LocalDate.of(2026, 3, 30);
    private final LocalDate checkOut = LocalDate.of(2026, 3, 31);
    private final String userId = "d5493297-10b2-4bbe-b3ef-c6f929618b9b";
    private final String idmKey = "6846516846816417447435434";

    @Test
    void shouldSuccessfullyHoldRoom_UsingRealLuaScript() {

        InventoryProjection mockInv = Mockito.mock(InventoryProjection.class);
        when(mockInv.getTotalCapacity()).thenReturn(10);
        when(mockInv.getBookedCount()).thenReturn(2);
        when(mockInv.getInventoryDate()).thenReturn(checkIn);

        // Map setup for the service logic
        when(inventoryRepo.getInventoryBatch(eq(hotelId), eq(roomTypeId), anyList()))
                .thenReturn(Map.of(checkIn, mockInv));

        // --- Act ---
        String token = bookingService.initiateHold(hotelId, roomTypeId, checkIn, checkOut, userId, idmKey);

        // --- Assert ---
        assertNotNull(token);
        assertTrue(token.startsWith("PAY_TK_"));
        System.out.println("Integration Test Success! Token: " + token);
    }

    @Test
    void shouldReturnSoldOut_WhenNoCapacityLeft() {
        // Mock inventory
        InventoryProjection mockInv = Mockito.mock(InventoryProjection.class);
        when(mockInv.getTotalCapacity()).thenReturn(5);
        when(mockInv.getBookedCount()).thenReturn(5); // fully booked
        when(mockInv.getInventoryDate()).thenReturn(checkIn);

        when(inventoryRepo.getInventoryBatch(eq(hotelId), eq(roomTypeId), eq(List.of(checkIn))))
                .thenReturn(Map.of(checkIn, mockInv));

        String uniqueKey = "idm-" + UUID.randomUUID();

        // Assert exception
        RuntimeException exception = assertThrows(RuntimeException .class, () ->
                bookingService.initiateHold(hotelId, roomTypeId, checkIn, checkOut, userId, uniqueKey)
        );






        assertTrue(exception.getMessage().contains("sold"));

        System.out.println("No Room Available for Booking : " + exception.getMessage());
    }

    @BeforeEach
    void setup() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}