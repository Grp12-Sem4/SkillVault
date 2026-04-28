import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig.js';
import AuthShell from '../components/AuthShell.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((currentFormData) => ({
      ...currentFormData,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const response = await api.post('/api/auth/login', formData);
      const token = response.data?.token;

      if (!token || !login(token)) {
        setError('Unable to log in with the provided token.');
        return;
      }

      navigate('/dashboard', { replace: true });
    } catch {
      setError('Invalid credentials');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthShell
      eyebrow="Skill Vault"
      title="Welcome back"
      subtitle="Sign in to access your dashboard, personal skills, knowledge tracker, and live trades."
      footer={(
        <p>
          Need an account? <Link className="auth-link" to="/register">Register</Link>
        </p>
      )}
    >
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="field-group">
          <label className="field-label" htmlFor="email">Email</label>
          <input
            className="auth-input"
            id="email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            autoComplete="email"
            required
          />
        </div>

        <div className="field-group">
          <label className="field-label" htmlFor="password">Password</label>
          <input
            className="auth-input"
            id="password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            autoComplete="current-password"
            required
          />
        </div>

        {error ? <p className="app-feedback-error">{error}</p> : null}

        <button className="app-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Logging in...' : 'Login'}
        </button>
      </form>
    </AuthShell>
  );
}
