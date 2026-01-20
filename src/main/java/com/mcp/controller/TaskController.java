package com.mcp.controller;

import com.mcp.entity.Task;
import com.mcp.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * Task Controller
 * SCRUM-1: REST API for Task operations
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        return taskRepository.findById(id).map(existingTask -> {
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
            existingTask.setAssignedTo(task.getAssignedTo());
            return ResponseEntity.ok(taskRepository.save(existingTask));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable String status) {
        return ResponseEntity.ok(taskRepository.findByStatus(status));
    }
}
