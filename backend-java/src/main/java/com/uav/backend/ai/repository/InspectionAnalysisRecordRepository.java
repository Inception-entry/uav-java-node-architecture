package com.uav.backend.ai.repository;

import com.uav.backend.ai.domain.InspectionAnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionAnalysisRecordRepository
        extends JpaRepository<InspectionAnalysisRecord, Long> {

    Optional<InspectionAnalysisRecord> findByAnalysisId(
            String analysisId
    );

    List<InspectionAnalysisRecord>
    findByTaskCodeOrderByCreatedAtDesc(String taskCode);
}
