import { apiClient } from './client';
import type { TaskOverview, TaskDetails, TaskCreateRequest, TaskUpdateRequest } from '../types/task';

export const tasksApi = {
  getAll: async (): Promise<TaskOverview[]> => {
    const response = await apiClient.get<TaskOverview[]>('/tasks/');
    return response.data;
  },

  getById: async (id: number): Promise<TaskDetails> => {
    const response = await apiClient.get<TaskDetails>(`/tasks/${id}`);
    return response.data;
  },

  create: async (data: TaskCreateRequest): Promise<TaskDetails> => {
    const response = await apiClient.post<TaskDetails>('/tasks/', data);
    return response.data;
  },

  update: async (id: number, data: TaskUpdateRequest): Promise<TaskDetails> => {
    const response = await apiClient.patch<TaskDetails>(`/tasks/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/tasks/${id}`);
  },
};
