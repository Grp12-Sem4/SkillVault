import { useQuery } from '@tanstack/react-query';
import api from '../api/axiosConfig.js';

export const CURRENT_USER_QUERY_KEY = ['current-user'];

export default function CreditDisplay() {
  const {
    data: currentUser,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: CURRENT_USER_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/users/me');
      return response.data;
    },
  });

  return (
    <aside className="app-credit-display">
      <p className="app-eyebrow" style={{ marginBottom: 0 }}>Current Balance</p>

      {isLoading ? (
        <p style={{ marginTop: '0.35rem', fontSize: '1.15rem', color: '#0f172a', fontWeight: 700 }}>Loading...</p>
      ) : isError ? (
        <>
          <p style={{ marginTop: '0.35rem', fontSize: '1.15rem', color: '#b91c1c', fontWeight: 700 }}>Unavailable</p>
          <p className="app-subtle-text" style={{ marginTop: '0.25rem', color: '#7f1d1d', fontSize: '0.9rem' }}>
            {error?.response?.data?.message ?? 'Unable to load your profile.'}
          </p>
        </>
      ) : (
        <>
          <p style={{ marginTop: '0.3rem', fontSize: '1.95rem', color: '#0f172a', fontWeight: 800 }}>
            {currentUser?.creditBalance ?? 0}
          </p>
          <p className="app-subtle-text" style={{ marginTop: '0.2rem', fontSize: '0.95rem' }}>
            {currentUser?.name ?? 'Authenticated user'}
          </p>
        </>
      )}
    </aside>
  );
}
