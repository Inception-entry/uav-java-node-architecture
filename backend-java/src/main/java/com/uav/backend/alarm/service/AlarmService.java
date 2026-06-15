package com.uav.backend.alarm.service;

import com.uav.backend.alarm.domain.AlarmEvent;
import com.uav.backend.alarm.dto.AlarmResponse;
import com.uav.backend.alarm.dto.CreateAlarmRequest;
import com.uav.backend.alarm.repository.AlarmEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AlarmService {
    private final AlarmEventRepository alarmEventRepository;

    public AlarmService(AlarmEventRepository alarmEventRepository) {
        this.alarmEventRepository = alarmEventRepository;
    }

    @Transactional
    public AlarmResponse create(CreateAlarmRequest request) {
        AlarmEvent event = new AlarmEvent();
        event.setEventCode("ALARM-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(request.eventTime()) + "-" + UUID.randomUUID().toString().substring(0, 8));
        event.setDeviceCode(request.deviceCode());
        event.setTaskCode(request.taskCode());
        event.setEventType(request.eventType());
        event.setWeaponType(request.weaponType());
        event.setConfidence(request.confidence());
        event.setLatitude(request.latitude());
        event.setLongitude(request.longitude());
        event.setImageUrl(request.imageUrl());
        event.setVideoUrl(request.videoUrl());
        event.setEventTime(request.eventTime());
        return toResponse(alarmEventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public List<AlarmResponse> latest() {
        return alarmEventRepository.findTop20ByOrderByEventTimeDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private AlarmResponse toResponse(AlarmEvent event) {
        return new AlarmResponse(
                event.getId(),
                event.getEventCode(),
                event.getDeviceCode(),
                event.getTaskCode(),
                event.getEventType(),
                event.getWeaponType(),
                event.getConfidence(),
                event.getLatitude(),
                event.getLongitude(),
                event.getImageUrl(),
                event.getVideoUrl(),
                event.getStatus(),
                event.getEventTime()
        );
    }
}
