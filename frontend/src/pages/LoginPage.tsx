import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [form, setForm] = useState({
    username: '', password: '', displayName: '', email: '', college: '', className: '',
  });
  const [error, setError] = useState('');
  const set = (k: keyof typeof form) => (e: { target: { value: string } }) =>
    setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    try {
      if (mode === 'register') {
        await api.register(form);
      }
      await login(form.username, form.password);
      navigate('/');
    } catch (e) {
      setError((e as Error).message);
    }
  };

  return (
    <div className="form-page narrow">
      <div className="tabs">
        <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>登录</button>
        <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>注册</button>
      </div>
      <label>学号 / 工号<input value={form.username} onChange={set('username')} /></label>
      <label>密码<input type="password" value={form.password} onChange={set('password')} /></label>
      {mode === 'register' && (
        <>
          <label>昵称<input value={form.displayName} onChange={set('displayName')} /></label>
          <label>校园邮箱<input value={form.email} onChange={set('email')} /></label>
          <label>学院<input value={form.college} onChange={set('college')} /></label>
          <label>班级<input value={form.className} onChange={set('className')} /></label>
        </>
      )}
      {error && <p className="error">{error}</p>}
      <button className="primary" onClick={submit}>{mode === 'login' ? '登录' : '注册并登录'}</button>
    </div>
  );
}
