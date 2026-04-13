import { useQuery } from '@tanstack/react-query';
import api from '../api/axiosConfig.js';

export const CURRENT_USER_QUERY_KEY = ['current-user'];

const creditDisplayStyle = {
  minWidth: '220px',
  padding: '1rem 1.1rem',
  borderRadius: '14px',
  backgroundColor: '#eff6ff',
  border: '1px solid #bfdbfe',
};

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
    <aside style={creditDisplayStyle}>
      <p style={{ margin: 0, color: '#1e3a8a', fontWeight: 600 }}>Current Balance</p>

      {isLoading ? (
        <p style={{ marginTop: '0.35rem', fontSize: '1.15rem', color: '#0f172a' }}>Loading...</p>
      ) : isError ? (
        <>
          <p style={{ marginTop: '0.35rem', fontSize: '1.15rem', color: '#b91c1c' }}>Unavailable</p>
          <p style={{ marginTop: '0.25rem', color: '#7f1d1d', fontSize: '0.9rem' }}>
            {error?.response?.data?.message ?? 'Unable to load your profile.'}
          </p>
        </>
      ) : (
        <>
          <p style={{ marginTop: '0.35rem', fontSize: '1.8rem', color: '#0f172a', fontWeight: 700 }}>
            {currentUser?.creditBalance ?? 0}
          </p>
          <p style={{ marginTop: '0.25rem', color: '#475569', fontSize: '0.95rem' }}>
            {currentUser?.name ?? 'Authenticated user'}
          </p>
        </>
      )}
    </aside>
  );
}
