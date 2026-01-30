package com.arsnyan.taskmanagementservice.repository;

import com.arsnyan.taskmanagementservice.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT t FROM Task t")
    List<Task> findAllWithOwners();

    @Query("SELECT t FROM Task t WHERE t.owner.username = :ownerUsername")
    List<Task> findAllTasks(@Param("ownerUsername") String ownerUsername);

    @Query("SELECT t FROM Task t WHERE t.taskId = :taskId AND t.owner.username = :ownerUsername")
    Optional<Task> findByIdAndOwner(@Param("taskId") Long taskId, @Param("ownerUsername") String ownerUsername);

    @Modifying
    @Query("UPDATE Task t SET t.finishedAt = :finishedAt WHERE t.taskId = :id AND t.owner.username = :ownerUsername")
    void finishTaskById(
            @Param("ownerUsername") String ownerUsername,
            @Param("id") Long taskId,
            @Param("finishedAt")ZonedDateTime finishedAt
    );

    void deleteByTaskIdAndOwnerUsername(Long taskId, String ownerUsername);
}
