package com.uav.backend.alarm.repository;

import com.uav.backend.alarm.domain.AlarmEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmEventRepository extends JpaRepository<AlarmEvent, Long> {
    List<AlarmEvent> findTop20ByOrderByEventTimeDesc();
}
