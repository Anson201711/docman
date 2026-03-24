import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type ViewMode = 'list' | 'grid';
type Theme = 'light' | 'dark' | 'system';

interface AppState {
  theme: Theme;
  viewMode: ViewMode;
  sidebarCollapsed: boolean;
  language: string;
  setTheme: (theme: Theme) => void;
  setViewMode: (mode: ViewMode) => void;
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setLanguage: (language: string) => void;
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      theme: 'light',
      viewMode: 'list',
      sidebarCollapsed: false,
      language: 'zh',

      setTheme: (theme) => set({ theme }),
      setViewMode: (viewMode) => set({ viewMode }),
      toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
      setSidebarCollapsed: (sidebarCollapsed) => set({ sidebarCollapsed }),
      setLanguage: (language) => set({ language }),
    }),
    {
      name: 'app-storage',
    }
  )
);

export default useAppStore;
