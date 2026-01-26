package com.arsnyan.taskmanagementservice.repository;

import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.model.TaskStatus;
import com.arsnyan.taskmanagementservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.owner.username = :ownerUsername")
    List<Task> findAllTasks(@Param("ownerUsername") String ownerUsername);

    @Query("SELECT t FROM Task t WHERE t.owner.username = :ownerUsername")
    List<Task> findFinished(@Param("ownerUsername") String ownerUsername);

    @Query("SELECT t FROM Task t WHERE t.owner.username = :ownerUsername AND t.status = :status")
    List<Task> findWithStatus(@Param("ownerUsername") String ownerUsername, @Param("status") TaskStatus status);

    Optional<Task> findByTaskIdAndOwner(Long taskId, User owner);

    @Modifying
    @Query("UPDATE Task t SET t.finishedAt = :finishedAt WHERE t.taskId = :id AND t.owner.username = :ownerUsername")
    Optional<Task> finishTaskById(
            @Param("ownerUsername") String ownerUsername,
            @Param("id") Long taskId,
            @Param("finishedAt")ZonedDateTime finishedAt
            );

    void deleteByTaskIdAndOwnerUsername(Long taskId, String ownerUsername);
}
