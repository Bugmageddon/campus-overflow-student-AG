import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api } from './api';
import type { Me } from './types';

interface AuthState {
  me: Me | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [me, setMe] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setMe(await api.me());
    } catch {
      setMe(null);      // 401 表示未登录，属于正常状态
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    api.csrf().catch(() => undefined).then(refresh);
  }, [refresh]);

  const value = useMemo<AuthState>(() => ({
    me,
    loading,
    login: async (username, password) => setMe(await api.login(username, password)),
    logout: async () => {
      await api.logout();
      setMe(null);
    },
    refresh,
  }), [me, loading, refresh]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth 必须在 AuthProvider 内使用');
  return ctx;
}
