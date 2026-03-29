package com.example.BookingService.service;

import com.example.BookingService.projection.InventoryProjection;
import com.example.BookingService.repository.BookingRepo;
import com.example.BookingService.repository.InventoryRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    InventoryRepo inventoryRepo;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    BookingRepo bookingRepo;

    @Mock
    DefaultRedisScript<String> holdRoomsScript;

    @InjectMocks
    BookingService bookingService;

    @Test
    @DisplayName("Successfully initiate room hold using Mockito")
    void initiateHoldTest() {

        String hotelId = "9a1f15c2-f9b4-4912-9d72-2fe5d5369825";
        String roomTypeId = "5ee19c87-a682-43a3-bcd9-325ea09946ba";
        LocalDate checkIn = LocalDate.of(2026, 3, 30);
        LocalDate checkOut = LocalDate.of(2026, 3, 31);
        String userId = "d5493297-10b2-4bbe-b3ef-c6f929618b9b";
        String idmKey = "6846516846816417447435434";
        List<LocalDate> stayDates = List.of(checkIn);

        //Stubbing: Mock the Database Inventory call
        InventoryProjection mockInventory = Mockito.mock(InventoryProjection.class);
        Mockito.lenient().when(mockInventory.getInventoryDate()).thenReturn(checkIn);
        Mockito.lenient().when(mockInventory.getHotelId()).thenReturn(hotelId);
        Mockito.when(mockInventory.getTotalCapacity()).thenReturn(10);
        Mockito.when(mockInventory.getBookedCount()).thenReturn(2);

        Map<LocalDate, InventoryProjection> mockInventoryMap = Map.of(checkIn, mockInventory);

        Mockito.when(inventoryRepo.getInventoryBatch(eq(hotelId), eq(roomTypeId), anyList()))
                .thenReturn(mockInventoryMap);

        // 3. Stubbing: Mock the Redis Lua Script execution
        // Note: We use any(Object[].class) because args.toArray() creates an array
        String expectedToken = "PAY_TK_sample_uuid";
        Mockito.when(redisTemplate.execute(
                eq(holdRoomsScript),
                anyList(),
                any(Object[].class)
        )).thenReturn(expectedToken);

        // 4. Act
        String res = bookingService.initiateHold(hotelId, roomTypeId, checkIn, checkOut, userId, idmKey);

        // 5. Assert
        assertNotNull(res);
        assertEquals(expectedToken, res);

        // Verify interactions
        Mockito.verify(inventoryRepo).getInventoryBatch(eq(hotelId), eq(roomTypeId), anyList());
        Mockito.verify(redisTemplate).execute(eq(holdRoomsScript), anyList(), any(Object[].class));
    }
}