export type TaskStatus = 'CREATED' | 'CANCELLED' | 'IN_BACKLOG' | 'BLOCKED' | 'DONE';

export interface TaskOverview {
  taskId: number;
  title: string;
  status: TaskStatus;
}

export interface TaskDetails {
  taskId: number;
  title: string;
  content: string | null;
  status: TaskStatus;
  finishedAt: string | null;
}

export interface TaskCreateRequest {
  title: string;
  content?: string;
}

export interface TaskUpdateRequest {
  title?: string;
  content?: string;
  status?: TaskStatus;
}
