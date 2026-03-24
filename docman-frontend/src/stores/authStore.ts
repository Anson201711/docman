import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/types';
import authService from '@/services/auth';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  register: (email: string, username: string, password: string) => Promise<void>;
  clearError: () => void;
  setUser: (user: User | null) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (username: string, password: string) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authService.login({ username, password });
          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
          });
          if (typeof window !== 'undefined') {
            localStorage.setItem('token', response.token);
          }
        } catch (error: any) {
          set({
            error: error.message || 'Login failed',
            isLoading: false,
          });
          throw error;
        }
      },

      logout: async () => {
        set({ isLoading: true });
        try {
          await authService.logout();
        } catch (error) {
          // Ignore logout errors
        } finally {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
          });
          if (typeof window !== 'undefined') {
            localStorage.removeItem('token');
          }
        }
      },

      register: async (email: string, username: string, password: string) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authService.register({ email, username, password });
          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
          });
          if (typeof window !== 'undefined') {
            localStorage.setItem('token', response.token);
          }
        } catch (error: any) {
          set({
            error: error.message || 'Registration failed',
            isLoading: false,
          });
          throw error;
        }
      },

      clearError: () => set({ error: null }),

      setUser: (user: User | null) => set({ user }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

export default useAuthStore;
