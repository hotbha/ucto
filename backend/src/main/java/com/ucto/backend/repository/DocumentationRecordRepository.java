package com.ucto.backend.repository;

import com.ucto.backend.entity.DocumentationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentationRecordRepository extends JpaRepository<DocumentationRecord, Long> {
    List<DocumentationRecord> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<DocumentationRecord> findByProjectIdAndDocTypeOrderByCreatedAtDesc(Long projectId, String docType);
    List<DocumentationRecord> findByProjectIdAndStatusOrderByCreatedAtDesc(Long projectId, String status);
    List<DocumentationRecord> findByStoryIdOrderByCreatedAtAsc(Long storyId);
}
