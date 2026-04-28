import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig.js';
import AuthShell from '../components/AuthShell.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function Register() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'STUDENT',
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
      const response = await api.post('/api/auth/register', formData);
      const token = response.data?.token;

      if (!token || !login(token)) {
        setError('Account was created, but the session token was invalid.');
        return;
      }

      navigate('/dashboard', { replace: true });
    } catch (error) {
      if (error.response?.status === 400) {
        setError('Email already exists');
      } else {
        setError('Unable to register right now');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthShell
      eyebrow="Create account"
      title="Start your workspace"
      subtitle="Register once to manage skills, keep knowledge current, and trade help through the marketplace."
      footer={(
        <p>
          Already registered? <Link className="auth-link" to="/login">Login</Link>
        </p>
      )}
    >
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="field-group">
          <label className="field-label" htmlFor="name">Name</label>
          <input
            className="auth-input"
            id="name"
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            autoComplete="name"
            required
          />
        </div>

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
            autoComplete="new-password"
            required
          />
        </div>

        <div className="field-group">
          <label className="field-label" htmlFor="role">Role</label>
          <select className="auth-input" id="role" name="role" value={formData.role} onChange={handleChange}>
            <option value="STUDENT">STUDENT</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>

        {error ? <p className="app-feedback-error">{error}</p> : null}

        <button className="app-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating account...' : 'Register'}
        </button>
      </form>
    </AuthShell>
  );
}
