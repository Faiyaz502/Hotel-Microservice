package com.hotel.service.HotelService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/staffs")
public class StaffController {


    @GetMapping
    public ResponseEntity<List<String>> getAllStaffs(){

        List<String> StaffsName = Arrays.asList("Rakib","Robiul","Sams");


        return ResponseEntity.ok().body(StaffsName);


    }


}
