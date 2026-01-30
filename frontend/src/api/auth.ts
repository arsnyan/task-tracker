import { apiClient } from './client';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types/auth';

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/accounts/login', data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/accounts/user', data);
    return response.data;
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>('/accounts/user');
    return response.data;
  },
};
