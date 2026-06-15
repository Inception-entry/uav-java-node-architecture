package com.uav.backend.device;

import com.uav.backend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/devices")
public class DeviceController {
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(List.of(
                Map.of("deviceCode", "UAV-001", "deviceName", "一号无人机", "status", "ONLINE"),
                Map.of("deviceCode", "CAMERA-001", "deviceName", "一号固定摄像头", "status", "OFFLINE")
        ));
    }
}
