import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';

export function AskPage() {
  const { me } = useAuth();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [tags, setTags] = useState('');
  const [courseId, setCourseId] = useState<string>('');
  const [error, setError] = useState('');

  const submit = async () => {
    try {
      const { id } = await api.ask({
        courseId: courseId ? Number(courseId) : undefined,
        title,
        body,
        tags: tags.split(/[,，\s]+/).filter(Boolean),
      });
      navigate(`/questions/${id}`);
    } catch (e) {
      setError((e as Error).message);
    }
  };

  return (
    <div className="form-page">
      <h1>提出一个问题</h1>
      <label>标题（10–150 字，尽量具体）
        <input value={title} onChange={(e) => setTitle(e.target.value)} />
      </label>
      <label>正文（Markdown，至少 20 字：说明你尝试过什么、卡在哪里）
        <textarea rows={12} value={body} onChange={(e) => setBody(e.target.value)} />
      </label>
      <label>标签（1–5 个，用空格或逗号分隔）
        <input value={tags} onChange={(e) => setTags(e.target.value)} placeholder="数据结构 红黑树" />
      </label>
      <label>所属课程（可选）
        <select value={courseId} onChange={(e) => setCourseId(e.target.value)}>
          <option value="">不关联课程</option>
          {me?.courses.map((c) => <option key={c.id} value={c.id}>{c.code} {c.name}</option>)}
        </select>
      </label>
      {error && <p className="error">{error}</p>}
      <button className="primary" onClick={submit}>发布问题</button>
    </div>
  );
}
