import type {
  LeaderboardEntry, Me, Notification, Page, QuestionDetail, QuestionSummary, ReputationProfile, SearchResult,
} from './types';

/** 后端以 RFC 9457 Problem Details 返回错误，这里统一转成带业务码的异常。 */
export class ApiError extends Error {
  constructor(readonly status: number, readonly code: string, message: string) {
    super(message);
  }
}

function cookie(name: string): string | undefined {
  return document.cookie.split('; ').find((c) => c.startsWith(`${name}=`))?.split('=')[1];
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET') {
    // CSRF 双提交：从 XSRF-TOKEN Cookie 取出 token 放进请求头
    let token = cookie('XSRF-TOKEN');
    if (!token) {
      await fetch('/api/v1/auth/csrf', { credentials: 'include' });
      token = cookie('XSRF-TOKEN');
    }
    if (token) headers['X-XSRF-TOKEN'] = decodeURIComponent(token);
  }
  const res = await fetch(`/api${path}`, {
    method, headers, credentials: 'include',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  const data = text ? JSON.parse(text) : undefined;
  if (!res.ok) {
    throw new ApiError(res.status, data?.code ?? 'SYS-000', data?.detail ?? '请求失败');
  }
  return data as T;
}

const get = <T>(path: string) => request<T>('GET', path);

export const api = {
  csrf: () => get<{ token: string }>('/v1/auth/csrf'),
  register: (body: Record<string, string>) => request<{ id: number }>('POST', '/v1/auth/register', body),
  login: (username: string, password: string) => request<Me>('POST', '/v1/auth/login', { username, password }),
  logout: () => request<void>('POST', '/v1/auth/logout'),
  me: () => get<Me>('/v1/auth/me'),

  questions: (page = 1, courseId?: number) =>
    get<Page<QuestionSummary>>(`/v1/questions?page=${page}${courseId ? `&courseId=${courseId}` : ''}`),
  question: (id: number) => get<QuestionDetail>(`/v1/questions/${id}`),
  ask: (body: { courseId?: number; title: string; body: string; tags: string[] }) =>
    request<{ id: number }>('POST', '/v1/questions', body),
  deleteQuestion: (id: number) => request<void>('DELETE', `/v1/questions/${id}`),
  answer: (questionId: number, body: string) =>
    request<{ id: number }>('POST', `/v1/questions/${questionId}/answers`, { body }),
  accept: (questionId: number, answerId: number) =>
    request<void>('PUT', `/v1/questions/${questionId}/accepted-answer`, { answerId }),
  endorse: (answerId: number) => request<void>('POST', `/v1/answers/${answerId}/endorsement`, {}),
  vote: (targetType: 'QUESTION' | 'ANSWER', targetId: number, value: number) =>
    request<{ myVote: number; scoreDelta: number }>('PUT', '/v1/votes', { targetType, targetId, value }),
  comment: (targetType: 'QUESTION' | 'ANSWER', targetId: number, body: string, parentId?: number) =>
    request<{ id: number }>('POST', '/v1/comments', { targetType, targetId, body, parentId }),

  search: (q: string, params: { tag?: string; sort?: string; unanswered?: boolean; page?: number } = {}) => {
    const search = new URLSearchParams({ q, page: String(params.page ?? 1) });
    if (params.tag) search.set('tag', params.tag);
    if (params.sort) search.set('sort', params.sort);
    if (params.unanswered) search.set('unanswered', 'true');
    return get<Page<SearchResult>>(`/v1/search/questions?${search}`);
  },
  related: (questionId: number) =>
    get<{ id: number; title: string; answerCount: number; accepted: boolean }[]>(`/v1/questions/${questionId}/related`),
  popularTags: () => get<{ name: string; questionCount: number }[]>('/v1/tags/popular?limit=15'),

  leaderboard: (period: string) => get<LeaderboardEntry[]>(`/v1/leaderboard?period=${period}`),
  reputation: (userId: number) => get<ReputationProfile>(`/v1/users/${userId}/reputation`),
  bounty: (questionId: number, points: number, days: number) =>
    request<{ id: number }>('POST', `/v1/questions/${questionId}/bounty`, { points, days }),

  notifications: () => get<Page<Notification>>('/v1/notifications'),
  unreadCount: () => get<{ count: number }>('/v1/notifications/unread-count'),
  readAll: () => request<{ updated: number }>('POST', '/v1/notifications/read-all', {}),
};
