export type UserRole = 'USER' | 'ADMIN';

export interface User {
  userId: number;
  username: string;
  email: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}
