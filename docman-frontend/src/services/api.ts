const API_BASE = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080';

interface FetchOptions extends RequestInit {
  params?: Record<string, any>;
}

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private getAuthHeaders(): HeadersInit {
    if (typeof window === 'undefined') return {};
    const token = localStorage.getItem('token');
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  private buildUrl(url: string, params?: Record<string, any>): string {
    if (!params) return url;
    const queryString = new URLSearchParams(params).toString();
    return queryString ? `${url}?${queryString}` : url;
  }

  async get<T>(url: string, options?: FetchOptions): Promise<T> {
    const { params, ...fetchOptions } = options || {};
    const finalUrl = this.buildUrl(url, params);

    const response = await fetch(`${this.baseUrl}${finalUrl}`, {
      ...fetchOptions,
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(),
        ...fetchOptions.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async post<T>(url: string, data?: any, options?: FetchOptions): Promise<T> {
    const response = await fetch(`${this.baseUrl}${url}`, {
      ...options,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(),
        ...options?.headers,
      },
      body: data ? JSON.stringify(data) : undefined,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async put<T>(url: string, data?: any, options?: FetchOptions): Promise<T> {
    const response = await fetch(`${this.baseUrl}${url}`, {
      ...options,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(),
        ...options?.headers,
      },
      body: data ? JSON.stringify(data) : undefined,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async delete<T>(url: string, options?: FetchOptions): Promise<T> {
    const response = await fetch(`${this.baseUrl}${url}`, {
      ...options,
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(),
        ...options?.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async upload<T>(url: string, formData: FormData, options?: FetchOptions): Promise<T> {
    const response = await fetch(`${this.baseUrl}${url}`, {
      ...options,
      method: 'POST',
      headers: {
        ...this.getAuthHeaders(),
        ...options?.headers,
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }
}

export const api = new ApiClient(API_BASE);
export default api;
