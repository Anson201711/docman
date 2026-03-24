'use client';

import { useAuthStore } from '@/stores/authStore';

export function useAuth() {
  const { user, token, isAuthenticated, isLoading, error, login, logout, register, clearError } = useAuthStore();

  return {
    user,
    token,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    register,
    clearError,
  };
}

export default useAuth;
