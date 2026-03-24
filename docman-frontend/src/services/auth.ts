import { api } from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types';

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    return api.post<AuthResponse>('/api/auth/login', data);
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    return api.post<AuthResponse>('/api/auth/register', data);
  },

  async logout(): Promise<void> {
    return api.post<void>('/api/auth/logout');
  },

  async getCurrentUser(): Promise<User> {
    return api.get<User>('/api/auth/me');
  },

  async refreshToken(): Promise<AuthResponse> {
    return api.post<AuthResponse>('/api/auth/refresh');
  },

  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    return api.post<void>('/api/auth/change-password', { oldPassword, newPassword });
  },
};

export default authService;
