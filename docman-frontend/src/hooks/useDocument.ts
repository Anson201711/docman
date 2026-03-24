'use client';

import useSWR from 'swr';
import { documentService } from '@/services/document';
import type { Document, SearchResult } from '@/types';

export function useDocuments(params?: {
  folderId?: string;
  categoryId?: string;
  page?: number;
  pageSize?: number;
  search?: string;
}) {
  const { data, error, isLoading, mutate } = useSWR(
    ['documents', params],
    () => documentService.getDocuments(params),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    documents: data?.data?.documents || [],
    total: data?.data?.total || 0,
    isLoading,
    error,
    mutate,
  };
}

export function useDocument(id: string) {
  const { data, error, isLoading, mutate } = useSWR(
    id ? [`document`, id] : null,
    () => documentService.getDocument(id),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    document: data?.data,
    isLoading,
    error,
    mutate,
  };
}

export function useRecentDocuments(limit: number = 10) {
  const { data, error, isLoading } = useSWR(
    ['recent-documents', limit],
    () => documentService.getRecentDocuments(limit),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    documents: data?.data || [],
    isLoading,
    error,
  };
}

export function useSearchDocuments(query: string) {
  const { data, error, isLoading } = useSWR(
    query ? ['search', query] : null,
    () => documentService.searchDocuments(query),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    results: data?.data as SearchResult | undefined,
    isLoading,
    error,
  };
}

export function useFolders(params?: { parentId?: string }) {
  const { data, error, isLoading, mutate } = useSWR(
    ['folders', params],
    () => documentService.getFolders(params),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    folders: data?.data || [],
    isLoading,
    error,
    mutate,
  };
}

export function useCategories() {
  const { data, error, isLoading, mutate } = useSWR(
    'categories',
    () => documentService.getCategories(),
    {
      revalidateOnFocus: false,
      shouldRetryOnError: false,
    }
  );

  return {
    categories: data?.data || [],
    isLoading,
    error,
    mutate,
  };
}

export default useDocument;
