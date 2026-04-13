import { useAuth } from '../context/AuthContext.jsx';

const pageStyle = {
  minHeight: '100vh',
  padding: '2rem',
  backgroundColor: '#f5f5f5',
};

const panelStyle = {
  maxWidth: '720px',
  margin: '0 auto',
  padding: '2rem',
  border: '1px solid #d4d4d8',
  borderRadius: '12px',
  backgroundColor: '#ffffff',
};

export default function Dashboard() {
  const { user, logout } = useAuth();
  const userIdentifier = user?.email ?? user?.sub ?? 'User';

  return (
    <main style={pageStyle}>
      <section style={panelStyle}>
        <h1>Dashboard</h1>
        <p>Welcome to Skill Vault, {userIdentifier}.</p>
        <button type="button" onClick={logout}>
          Logout
        </button>
      </section>
    </main>
  );
}
