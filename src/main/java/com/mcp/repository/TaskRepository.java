package com.mcp.repository;

import com.mcp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Task Repository
 * SCRUM-1: Database operations for Tasks
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(String status);
    List<Task> findByAssignedToId(Long userId);
}
