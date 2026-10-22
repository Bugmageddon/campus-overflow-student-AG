import { useEffect, useState } from 'react';
import { api } from '../api';
import type { LeaderboardEntry } from '../types';

const PERIODS = [['week', '本周'], ['month', '本月'], ['term', '本学期'], ['all', '总榜']] as const;

export function LeaderboardPage() {
  const [period, setPeriod] = useState<string>('week');
  const [rows, setRows] = useState<LeaderboardEntry[]>([]);

  useEffect(() => {
    api.leaderboard(period).then(setRows).catch(() => setRows([]));
  }, [period]);

  return (
    <div className="form-page">
      <h1>声誉排行榜</h1>
      <div className="tabs">
        {PERIODS.map(([value, label]) => (
          <button key={value} className={period === value ? 'active' : ''} onClick={() => setPeriod(value)}>
            {label}
          </button>
        ))}
      </div>
      <table className="board">
        <thead><tr><th>名次</th><th>用户</th><th>声誉</th></tr></thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.userId}>
              <td>{r.rank}</td>
              <td>{r.displayName}{r.verified && <span className="verified" title="已认证">✔</span>}</td>
              <td>{r.score}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {rows.length === 0 && <p className="muted">这个周期还没有数据。</p>}
    </div>
  );
}
