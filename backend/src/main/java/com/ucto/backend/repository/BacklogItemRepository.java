package com.ucto.backend.repository;

import com.ucto.backend.entity.BacklogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacklogItemRepository extends JpaRepository<BacklogItem, Long> {
    List<BacklogItem> findByProjectIdOrderByPriorityAsc(Long projectId);
    List<BacklogItem> findBySprintIdOrderByPriorityAsc(Long sprintId);
    List<BacklogItem> findByProjectIdAndStatus(Long projectId, String status);
    List<BacklogItem> findByProjectIdAndItemType(Long projectId, String itemType);
    long countByProjectIdAndStatus(Long projectId, String status);
    long countBySprintId(Long sprintId);
    long countBySprintIdAndStatus(Long sprintId, String status);
}
