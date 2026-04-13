import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig.js';
import { useAuth } from '../context/AuthContext.jsx';

const formStyle = {
  display: 'grid',
  gap: '0.75rem',
  width: '100%',
  maxWidth: '360px',
};

const pageStyle = {
  minHeight: '100vh',
  display: 'grid',
  placeItems: 'center',
  padding: '2rem',
};

const cardStyle = {
  width: '100%',
  maxWidth: '420px',
  padding: '2rem',
  border: '1px solid #d4d4d8',
  borderRadius: '12px',
  backgroundColor: '#ffffff',
};

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
      const response = await api.post('/auth/login', formData);
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
    <main style={pageStyle}>
      <section style={cardStyle}>
        <h1>Login</h1>
        <form onSubmit={handleSubmit} style={formStyle}>
          <label htmlFor="email">Email</label>
          <input
            id="email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            autoComplete="email"
            required
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            autoComplete="current-password"
            required
          />

          {error ? <p style={{ color: '#b91c1c', margin: 0 }}>{error}</p> : null}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p style={{ marginTop: '1rem' }}>
          Need an account? <Link to="/register">Register</Link>
        </p>
      </section>
    </main>
  );
}
