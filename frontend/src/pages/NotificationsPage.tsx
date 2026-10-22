import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import type { Notification } from '../types';

export function NotificationsPage() {
  const [items, setItems] = useState<Notification[]>([]);

  const load = () => api.notifications().then((p) => setItems(p.items)).catch(() => undefined);
  useEffect(() => { load(); }, []);

  return (
    <div className="form-page">
      <h1>通知</h1>
      <button onClick={() => api.readAll().then(load)}>全部标记为已读</button>
      <ul className="plain">
        {items.map((n) => (
          <li key={n.id} className={n.read ? 'muted' : ''}>
            <Link to={n.link.replace(/^\//, '/')}>{n.title}</Link>
            <span className="muted"> · {new Date(n.createdAt).toLocaleString('zh-CN')}</span>
          </li>
        ))}
        {items.length === 0 && <li className="muted">暂时没有通知。</li>}
      </ul>
    </div>
  );
}
