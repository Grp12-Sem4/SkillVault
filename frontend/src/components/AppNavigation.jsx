import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const navContainerStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  gap: '1rem',
  flexWrap: 'wrap',
  marginBottom: '1.25rem',
};

const linksStyle = {
  display: 'flex',
  gap: '0.75rem',
  flexWrap: 'wrap',
};

const linkStyle = {
  padding: '0.65rem 0.95rem',
  borderRadius: '999px',
  border: '1px solid #cbd5e1',
  textDecoration: 'none',
  color: '#0f172a',
  backgroundColor: '#ffffff',
  fontWeight: 600,
};

const activeLinkStyle = {
  ...linkStyle,
  backgroundColor: '#dbeafe',
  borderColor: '#93c5fd',
  color: '#1d4ed8',
};

const logoutButtonStyle = {
  padding: '0.65rem 1rem',
  borderRadius: '999px',
  border: 'none',
  backgroundColor: '#0f172a',
  color: '#ffffff',
  fontWeight: 600,
  cursor: 'pointer',
};

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/skills', label: 'Skills' },
  { to: '/knowledge', label: 'Knowledge' },
  { to: '/trades', label: 'Trades' },
];

export default function AppNavigation() {
  const { logout } = useAuth();

  return (
    <nav style={navContainerStyle} aria-label="Primary">
      <div style={linksStyle}>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            style={({ isActive }) => (isActive ? activeLinkStyle : linkStyle)}
          >
            {item.label}
          </NavLink>
        ))}
      </div>

      <button type="button" onClick={logout} style={logoutButtonStyle}>
        Logout
      </button>
    </nav>
  );
}
