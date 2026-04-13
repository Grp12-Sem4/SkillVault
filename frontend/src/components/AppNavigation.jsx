import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/skills', label: 'Skills' },
  { to: '/knowledge', label: 'Knowledge' },
  { to: '/trades', label: 'Trades' },
];

export default function AppNavigation() {
  const { logout } = useAuth();

  return (
    <nav className="app-nav" aria-label="Primary">
      <div className="app-nav-links">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => (isActive ? 'app-nav-link app-nav-link-active' : 'app-nav-link')}
          >
            {item.label}
          </NavLink>
        ))}
      </div>

      <button className="app-button app-button-secondary" type="button" onClick={logout}>
        Logout
      </button>
    </nav>
  );
}
