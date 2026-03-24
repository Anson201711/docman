export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  role: 'admin' | 'user' | 'guest';
}

export interface Document {
  id: string;
  name: string;
  type: string;
  size: number;
  path: string;
  folderId?: string;
  categoryId?: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  downloadUrl?: string;
  thumbnailUrl?: string;
}

export interface Folder {
  id: string;
  name: string;
  parentId?: string;
  path: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  documentCount: number;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  documentCount: number;
}

export interface SearchResult {
  documents: Document[];
  folders: Folder[];
  categories: Category[];
  total: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
