package com.arsnyan.taskmanagementservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_task_title", columnList = "owner_id,title"),
                @Index(name = "idx_task_status", columnList = "owner_id,status"),
                @Index(name = "idx_task_finished_at", columnList = "owner_id,finished_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", updatable = false, nullable = false)
    private User owner;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    @Size(min = 1)
    private String content;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.CREATED;

    @Column(name = "finished_at")
    private ZonedDateTime finishedAt;

    public boolean isFinished() {
        return status == TaskStatus.DONE;
    }

    public static Task create(User user, String title, String content) {
        return Task.builder()
                .owner(user)
                .title(title)
                .content(content)
                .status(TaskStatus.CREATED)
                .build();
    }
}
