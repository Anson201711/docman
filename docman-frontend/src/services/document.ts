import { api } from './api';
import type { Document, Folder, Category, SearchResult, ApiResponse } from '@/types';

export const documentService = {
  async getDocuments(params?: {
    folderId?: string;
    categoryId?: string;
    page?: number;
    pageSize?: number;
    search?: string;
  }): Promise<ApiResponse<{ documents: Document[]; total: number }>> {
    return api.get<ApiResponse<{ documents: Document[]; total: number }>>('/api/documents', { params });
  },

  async getDocument(id: string): Promise<ApiResponse<Document>> {
    return api.get<ApiResponse<Document>>(`/api/documents/${id}`);
  },

  async createDocument(data: Partial<Document>): Promise<ApiResponse<Document>> {
    return api.post<ApiResponse<Document>>('/api/documents', data);
  },

  async updateDocument(id: string, data: Partial<Document>): Promise<ApiResponse<Document>> {
    return api.put<ApiResponse<Document>>(`/api/documents/${id}`, data);
  },

  async deleteDocument(id: string): Promise<ApiResponse<void>> {
    return api.delete<ApiResponse<void>>(`/api/documents/${id}`);
  },

  async uploadDocument(file: File, folderId?: string, categoryId?: string): Promise<ApiResponse<Document>> {
    const formData = new FormData();
    formData.append('file', file);
    if (folderId) formData.append('folderId', folderId);
    if (categoryId) formData.append('categoryId', categoryId);
    return api.upload<ApiResponse<Document>>('/api/documents/upload', formData);
  },

  async downloadDocument(id: string): Promise<Blob> {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/documents/${id}/download`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
    });
    return response.blob();
  },

  async getRecentDocuments(limit: number = 10): Promise<ApiResponse<Document[]>> {
    return api.get<ApiResponse<Document[]>>('/api/documents/recent', { params: { limit } });
  },

  async searchDocuments(query: string): Promise<ApiResponse<SearchResult>> {
    return api.get<ApiResponse<SearchResult>>('/api/documents/search', { params: { q: query } });
  },

  async getFolders(params?: { parentId?: string }): Promise<ApiResponse<Folder[]>> {
    return api.get<ApiResponse<Folder[]>>('/api/folders', { params });
  },

  async createFolder(data: Partial<Folder>): Promise<ApiResponse<Folder>> {
    return api.post<ApiResponse<Folder>>('/api/folders', data);
  },

  async updateFolder(id: string, data: Partial<Folder>): Promise<ApiResponse<Folder>> {
    return api.put<ApiResponse<Folder>>(`/api/folders/${id}`, data);
  },

  async deleteFolder(id: string): Promise<ApiResponse<void>> {
    return api.delete<ApiResponse<void>>(`/api/folders/${id}`);
  },

  async getCategories(): Promise<ApiResponse<Category[]>> {
    return api.get<ApiResponse<Category[]>>('/api/categories');
  },

  async createCategory(data: Partial<Category>): Promise<ApiResponse<Category>> {
    return api.post<ApiResponse<Category>>('/api/categories', data);
  },

  async updateCategory(id: string, data: Partial<Category>): Promise<ApiResponse<Category>> {
    return api.put<ApiResponse<Category>>(`/api/categories/${id}`, data);
  },

  async deleteCategory(id: string): Promise<ApiResponse<void>> {
    return api.delete<ApiResponse<void>>(`/api/categories/${id}`);
  },
};

export default documentService;
