package com.uav.backend.alarm.controller;

import com.uav.backend.alarm.dto.AlarmResponse;
import com.uav.backend.alarm.dto.CreateAlarmRequest;
import com.uav.backend.alarm.service.AlarmService;
import com.uav.backend.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alarms")
public class AlarmController {
    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @PostMapping
    public ApiResponse<AlarmResponse> create(@Valid @RequestBody CreateAlarmRequest request) {
        return ApiResponse.ok(alarmService.create(request));
    }

    @GetMapping("/latest")
    public ApiResponse<List<AlarmResponse>> latest() {
        return ApiResponse.ok(alarmService.latest());
    }
}
