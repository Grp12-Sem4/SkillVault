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
      const response = await api.post('/auth/register', formData);
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
    <main style={pageStyle}>
      <section style={cardStyle}>
        <h1>Create account</h1>
        <form onSubmit={handleSubmit} style={formStyle}>
          <label htmlFor="name">Name</label>
          <input
            id="name"
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            autoComplete="name"
            required
          />

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
            autoComplete="new-password"
            required
          />

          <label htmlFor="role">Role</label>
          <select id="role" name="role" value={formData.role} onChange={handleChange}>
            <option value="STUDENT">STUDENT</option>
            <option value="ADMIN">ADMIN</option>
          </select>

          {error ? <p style={{ color: '#b91c1c', margin: 0 }}>{error}</p> : null}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Creating account...' : 'Register'}
          </button>
        </form>

        <p style={{ marginTop: '1rem' }}>
          Already registered? <Link to="/login">Login</Link>
        </p>
      </section>
    </main>
  );
}
