import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../api/axiosConfig.js';
import AppNavigation from '../components/AppNavigation.jsx';

const CURRENT_USER_QUERY_KEY = ['current-user'];
const MARKETPLACE_SKILLS_QUERY_KEY = ['marketplace-skills'];
const TRADES_QUERY_KEY = ['trades'];

const pageStyle = {
  minHeight: '100vh',
  padding: '2rem',
  backgroundColor: '#f5f5f5',
};

const layoutStyle = {
  maxWidth: '1180px',
  margin: '0 auto',
  display: 'grid',
  gap: '1.5rem',
};

const panelStyle = {
  backgroundColor: '#ffffff',
  border: '1px solid #d4d4d8',
  borderRadius: '16px',
  padding: '1.5rem',
  boxShadow: '0 10px 30px rgba(15, 23, 42, 0.05)',
};

const headerRowStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'flex-start',
  gap: '1rem',
  flexWrap: 'wrap',
};

const creditCardStyle = {
  minWidth: '180px',
  padding: '1rem 1.1rem',
  borderRadius: '14px',
  backgroundColor: '#eff6ff',
  border: '1px solid #bfdbfe',
};

const sectionHeaderStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  gap: '1rem',
  flexWrap: 'wrap',
  marginBottom: '1rem',
};

const gridStyle = {
  display: 'grid',
  gap: '1rem',
  gridTemplateColumns: 'repeat(auto-fit, minmax(270px, 1fr))',
};

const tradeGridStyle = {
  display: 'grid',
  gap: '1rem',
};

const cardStyle = {
  border: '1px solid #e2e8f0',
  borderRadius: '14px',
  padding: '1rem',
  backgroundColor: '#f8fafc',
  display: 'grid',
  gap: '0.85rem',
  textAlign: 'left',
};

const badgeRowStyle = {
  display: 'flex',
  gap: '0.5rem',
  flexWrap: 'wrap',
};

const badgeStyle = {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '0.3rem 0.65rem',
  borderRadius: '999px',
  backgroundColor: '#e2e8f0',
  color: '#0f172a',
  fontSize: '0.82rem',
  fontWeight: 600,
};

const statusStyles = {
  PENDING: { backgroundColor: '#fef3c7', color: '#92400e' },
  ACTIVE: { backgroundColor: '#dcfce7', color: '#166534' },
  COMPLETED: { backgroundColor: '#e0e7ff', color: '#3730a3' },
};

const buttonStyle = {
  padding: '0.8rem 1rem',
  borderRadius: '10px',
  border: 'none',
  backgroundColor: '#2563eb',
  color: '#ffffff',
  fontWeight: 600,
  cursor: 'pointer',
};

const secondaryButtonStyle = {
  ...buttonStyle,
  backgroundColor: '#0f172a',
};

const subtleButtonStyle = {
  ...buttonStyle,
  backgroundColor: '#1d4ed8',
};

const formGridStyle = {
  display: 'grid',
  gap: '1rem',
  gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
};

const inputStyle = {
  width: '100%',
  padding: '0.8rem 0.9rem',
  borderRadius: '10px',
  border: '1px solid #cbd5e1',
  boxSizing: 'border-box',
  font: 'inherit',
  marginTop: '0.35rem',
};

const fullWidthStyle = {
  gridColumn: '1 / -1',
};

const emptyStateStyle = {
  padding: '2rem',
  borderRadius: '14px',
  border: '1px dashed #cbd5e1',
  backgroundColor: '#f8fafc',
};

function getErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message ?? fallbackMessage;
}

function formatScheduledTime(value) {
  if (!value) {
    return 'Not scheduled';
  }

  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString();
}

function groupTradesByStatus(trades) {
  return {
    PENDING: trades.filter((trade) => trade.status === 'PENDING'),
    ACTIVE: trades.filter((trade) => trade.status === 'ACTIVE'),
    COMPLETED: trades.filter((trade) => trade.status === 'COMPLETED'),
  };
}

export default function Trades() {
  const queryClient = useQueryClient();
  const [selectedSkill, setSelectedSkill] = useState(null);
  const [formData, setFormData] = useState({
    scheduledTime: '',
    duration: 1,
  });
  const [ratingInputs, setRatingInputs] = useState({});

  const {
    data: currentUser,
    isLoading: isCurrentUserLoading,
    isError: isCurrentUserError,
    error: currentUserError,
  } = useQuery({
    queryKey: CURRENT_USER_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/users/me');
      return response.data;
    },
  });

  const {
    data: marketplaceSkills = [],
    isLoading: isMarketplaceLoading,
    isError: isMarketplaceError,
    error: marketplaceError,
  } = useQuery({
    queryKey: MARKETPLACE_SKILLS_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/skills/marketplace');
      return response.data ?? [];
    },
  });

  const {
    data: trades = [],
    isLoading: isTradesLoading,
    isError: isTradesError,
    error: tradesError,
  } = useQuery({
    queryKey: TRADES_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/trades');
      return response.data ?? [];
    },
  });

  const groupedTrades = useMemo(() => groupTradesByStatus(trades), [trades]);

  const createTradeMutation = useMutation({
    mutationFn: async (payload) => {
      const response = await api.post('/trades', payload);
      return response.data;
    },
    onSuccess: () => {
      setFormData({
        scheduledTime: '',
        duration: 1,
      });
      setSelectedSkill(null);
      queryClient.invalidateQueries({ queryKey: TRADES_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: MARKETPLACE_SKILLS_QUERY_KEY });
    },
  });

  const acceptTradeMutation = useMutation({
    mutationFn: async (tradeId) => {
      const response = await api.put(`/trades/${tradeId}/accept`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TRADES_QUERY_KEY });
    },
  });

  const completeTradeMutation = useMutation({
    mutationFn: async ({ tradeId, rating }) => {
      const response = await api.put(`/trades/${tradeId}/complete`, { rating });
      return response.data;
    },
    onSuccess: (_, variables) => {
      setRatingInputs((currentRatings) => {
        const nextRatings = { ...currentRatings };
        delete nextRatings[variables.tradeId];
        return nextRatings;
      });
      queryClient.invalidateQueries({ queryKey: TRADES_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: CURRENT_USER_QUERY_KEY });
    },
  });

  const handleFormChange = (event) => {
    const { name, value } = event.target;

    setFormData((currentFormData) => ({
      ...currentFormData,
      [name]: value,
    }));
  };

  const handleRatingChange = (tradeId, value) => {
    setRatingInputs((currentRatings) => ({
      ...currentRatings,
      [tradeId]: value,
    }));
  };

  const handleSelectSkill = (skill) => {
    setSelectedSkill(skill);
    createTradeMutation.reset();
  };

  const handleResetRequest = () => {
    setSelectedSkill(null);
    setFormData({
      scheduledTime: '',
      duration: 1,
    });
    createTradeMutation.reset();
  };

  const handleCreateTrade = (event) => {
    event.preventDefault();

    if (!selectedSkill) {
      return;
    }

    createTradeMutation.mutate({
      providerId: selectedSkill.provider.id,
      skillId: selectedSkill.id,
      scheduledTime: formData.scheduledTime,
      duration: Number(formData.duration),
    });
  };

  return (
    <main style={pageStyle}>
      <section style={layoutStyle}>
        <div>
          <AppNavigation />

          <header style={panelStyle}>
            <div style={headerRowStyle}>
              <div>
                <p style={{ marginBottom: '0.5rem', color: '#475569' }}>
                  Trade Marketplace
                </p>
                <h1 style={{ marginTop: 0, marginBottom: '0.75rem' }}>Find skills and manage live trades</h1>
                <p style={{ margin: 0, color: '#475569' }}>
                  Browse offered skills, request sessions, and act on trades tied to your account.
                </p>
              </div>

              <aside style={creditCardStyle}>
                <p style={{ margin: 0, color: '#1e3a8a', fontWeight: 600 }}>Current Balance</p>
                {isCurrentUserLoading ? (
                  <p style={{ marginTop: '0.35rem', fontSize: '1.4rem', color: '#0f172a' }}>Loading...</p>
                ) : isCurrentUserError ? (
                  <p style={{ marginTop: '0.35rem', color: '#b91c1c' }}>Unavailable</p>
                ) : (
                  <>
                    <p style={{ marginTop: '0.35rem', fontSize: '1.8rem', color: '#0f172a', fontWeight: 700 }}>
                      {currentUser?.creditBalance ?? 0}
                    </p>
                    <p style={{ marginTop: '0.25rem', color: '#475569', fontSize: '0.95rem' }}>
                      {currentUser?.name} ({currentUser?.email})
                    </p>
                  </>
                )}
              </aside>
            </div>

            {isCurrentUserError ? (
              <p style={{ marginTop: '1rem', color: '#b91c1c' }}>
                {getErrorMessage(currentUserError, 'Unable to load your profile.')}
              </p>
            ) : null}
          </header>
        </div>

        <section style={panelStyle}>
          <div style={sectionHeaderStyle}>
            <div>
              <h2 style={{ margin: 0 }}>Marketplace</h2>
              <p style={{ margin: '0.35rem 0 0', color: '#475569' }}>
                Offered skills returned by <code>/api/skills/marketplace</code>.
              </p>
            </div>
            <button
              type="button"
              style={secondaryButtonStyle}
              onClick={() => queryClient.invalidateQueries({ queryKey: MARKETPLACE_SKILLS_QUERY_KEY })}
              disabled={isMarketplaceLoading}
            >
              Refresh
            </button>
          </div>

          {isMarketplaceLoading ? <p style={{ margin: 0 }}>Loading marketplace skills...</p> : null}

          {isMarketplaceError ? (
            <p style={{ margin: 0, color: '#b91c1c' }}>
              {getErrorMessage(marketplaceError, 'Unable to load marketplace skills.')}
            </p>
          ) : null}

          {!isMarketplaceLoading && !isMarketplaceError && marketplaceSkills.length === 0 ? (
            <div style={emptyStateStyle}>
              <h3 style={{ marginTop: 0 }}>No marketplace skills yet</h3>
              <p style={{ margin: 0, color: '#475569' }}>
                Once other users add offered skills, they will appear here for trading.
              </p>
            </div>
          ) : null}

          {!isMarketplaceLoading && !isMarketplaceError && marketplaceSkills.length > 0 ? (
            <div style={gridStyle}>
              {marketplaceSkills.map((skill) => {
                const isSelected = selectedSkill?.id === skill.id;

                return (
                  <article key={skill.id} style={cardStyle}>
                    <div>
                      <p style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>
                        {skill.category || 'Uncategorized'}
                      </p>
                      <h3 style={{ margin: '0.35rem 0 0' }}>{skill.title}</h3>
                    </div>

                    <div style={badgeRowStyle}>
                      <span style={badgeStyle}>{skill.skillCategory ?? 'UNKNOWN'}</span>
                      <span style={badgeStyle}>{skill.creditValue ?? 0} credits</span>
                    </div>

                    <p style={{ margin: 0, color: '#475569' }}>{skill.description}</p>

                    <div style={{ color: '#334155', display: 'grid', gap: '0.25rem' }}>
                      <span>Provider: {skill.provider?.name ?? 'Unknown'}</span>
                      <span>{skill.provider?.email ?? 'No email available'}</span>
                    </div>

                    <div>
                      <button type="button" style={buttonStyle} onClick={() => handleSelectSkill(skill)}>
                        {isSelected ? 'Selected' : 'Request Trade'}
                      </button>
                    </div>
                  </article>
                );
              })}
            </div>
          ) : null}
        </section>

        <section style={panelStyle}>
          <h2 style={{ marginTop: 0 }}>Request Trade</h2>
          <p style={{ marginTop: 0, color: '#475569' }}>
            Choose a marketplace skill, then schedule a session request.
          </p>

          {selectedSkill ? (
            <div
              style={{
                border: '1px solid #bfdbfe',
                backgroundColor: '#eff6ff',
                borderRadius: '14px',
                padding: '1rem',
                marginBottom: '1rem',
              }}
            >
              <p style={{ margin: 0, color: '#1d4ed8', fontWeight: 700 }}>{selectedSkill.title}</p>
              <p style={{ marginTop: '0.3rem', color: '#334155' }}>
                {selectedSkill.provider?.name} ({selectedSkill.provider?.email}) | {selectedSkill.creditValue ?? 0} credits
              </p>
            </div>
          ) : (
            <div style={{ ...emptyStateStyle, marginBottom: '1rem' }}>
              <h3 style={{ marginTop: 0 }}>Select a marketplace skill</h3>
              <p style={{ margin: 0, color: '#475569' }}>
                Use the Request Trade button on a marketplace card to prefill this form.
              </p>
            </div>
          )}

          <form onSubmit={handleCreateTrade} style={formGridStyle}>
            <div>
              <label htmlFor="scheduledTime">Scheduled Time</label>
              <input
                id="scheduledTime"
                name="scheduledTime"
                type="datetime-local"
                value={formData.scheduledTime}
                onChange={handleFormChange}
                style={inputStyle}
                required
                disabled={!selectedSkill || createTradeMutation.isPending}
              />
            </div>

            <div>
              <label htmlFor="duration">Duration</label>
              <input
                id="duration"
                name="duration"
                type="number"
                min="1"
                step="1"
                value={formData.duration}
                onChange={handleFormChange}
                style={inputStyle}
                required
                disabled={!selectedSkill || createTradeMutation.isPending}
              />
            </div>

            {createTradeMutation.isError ? (
              <p style={{ ...fullWidthStyle, margin: 0, color: '#b91c1c' }}>
                {getErrorMessage(createTradeMutation.error, 'Unable to create trade right now.')}
              </p>
            ) : null}

            {createTradeMutation.isSuccess ? (
              <p style={{ ...fullWidthStyle, margin: 0, color: '#15803d' }}>
                Trade request created successfully.
              </p>
            ) : null}

            <div style={{ ...fullWidthStyle, display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              <button
                type="submit"
                style={buttonStyle}
                disabled={!selectedSkill || createTradeMutation.isPending}
              >
                {createTradeMutation.isPending ? 'Submitting...' : 'Submit Trade Request'}
              </button>

              <button
                type="button"
                style={secondaryButtonStyle}
                onClick={handleResetRequest}
                disabled={createTradeMutation.isPending}
              >
                Clear
              </button>
            </div>
          </form>
        </section>

        <section style={panelStyle}>
          <div style={sectionHeaderStyle}>
            <div>
              <h2 style={{ margin: 0 }}>My Trades</h2>
              <p style={{ margin: '0.35rem 0 0', color: '#475569' }}>
                Trades where you are the requester or provider from <code>/api/trades</code>.
              </p>
            </div>
            <button
              type="button"
              style={secondaryButtonStyle}
              onClick={() => queryClient.invalidateQueries({ queryKey: TRADES_QUERY_KEY })}
              disabled={isTradesLoading}
            >
              Refresh
            </button>
          </div>

          {isTradesLoading ? <p style={{ margin: 0 }}>Loading trades...</p> : null}

          {isTradesError ? (
            <p style={{ margin: 0, color: '#b91c1c' }}>
              {getErrorMessage(tradesError, 'Unable to load trades.')}
            </p>
          ) : null}

          {!isTradesLoading && !isTradesError ? (
            <div style={{ display: 'grid', gap: '1.5rem' }}>
              {['PENDING', 'ACTIVE', 'COMPLETED'].map((status) => {
                const tradesForStatus = groupedTrades[status];

                return (
                  <section key={status}>
                    <h3 style={{ marginTop: 0, marginBottom: '0.75rem' }}>
                      {status.charAt(0) + status.slice(1).toLowerCase()}
                    </h3>

                    {tradesForStatus.length === 0 ? (
                      <div style={emptyStateStyle}>
                        <p style={{ margin: 0, color: '#475569' }}>
                          No {status.toLowerCase()} trades right now.
                        </p>
                      </div>
                    ) : (
                      <div style={tradeGridStyle}>
                        {tradesForStatus.map((trade) => {
                          const isProvider = currentUser?.id === trade.provider?.id;
                          const isRequester = currentUser?.id === trade.requester?.id;
                          const pendingRatingValue = ratingInputs[trade.id] ?? trade.rating ?? 5;
                          const isCompletingCurrentTrade =
                            completeTradeMutation.isPending && completeTradeMutation.variables?.tradeId === trade.id;
                          const isAcceptingCurrentTrade =
                            acceptTradeMutation.isPending && acceptTradeMutation.variables === trade.id;

                          return (
                            <article key={trade.id} style={cardStyle}>
                              <div style={{ ...sectionHeaderStyle, marginBottom: 0 }}>
                                <div>
                                  <p style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>
                                    {trade.skill?.creditValue ?? 0} credits
                                  </p>
                                  <h3 style={{ margin: '0.35rem 0 0' }}>{trade.skill?.title ?? 'Untitled skill'}</h3>
                                </div>
                                <span style={{ ...badgeStyle, ...(statusStyles[trade.status] ?? {}) }}>
                                  {trade.status}
                                </span>
                              </div>

                              <div style={{ color: '#334155', display: 'grid', gap: '0.35rem' }}>
                                <span>
                                  Requester: {trade.requester?.name ?? 'Unknown'} ({trade.requester?.email ?? 'No email'})
                                </span>
                                <span>
                                  Provider: {trade.provider?.name ?? 'Unknown'} ({trade.provider?.email ?? 'No email'})
                                </span>
                                <span>Scheduled: {formatScheduledTime(trade.scheduledTime)}</span>
                                <span>Duration: {trade.duration ?? 0} hour(s)</span>
                                {trade.rating != null ? <span>Rating: {trade.rating}/5</span> : null}
                              </div>

                              {trade.status === 'PENDING' && isProvider ? (
                                <div>
                                  <button
                                    type="button"
                                    style={buttonStyle}
                                    onClick={() => acceptTradeMutation.mutate(trade.id)}
                                    disabled={isAcceptingCurrentTrade}
                                  >
                                    {isAcceptingCurrentTrade ? 'Accepting...' : 'Accept'}
                                  </button>
                                </div>
                              ) : null}

                              {trade.status === 'ACTIVE' && isRequester ? (
                                <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                                  <div>
                                    <label htmlFor={`rating-${trade.id}`}>Rating</label>
                                    <input
                                      id={`rating-${trade.id}`}
                                      type="number"
                                      min="1"
                                      max="5"
                                      step="1"
                                      value={pendingRatingValue}
                                      onChange={(event) => handleRatingChange(trade.id, event.target.value)}
                                      style={{ ...inputStyle, marginTop: '0.35rem', width: '110px' }}
                                      disabled={isCompletingCurrentTrade}
                                    />
                                  </div>

                                  <button
                                    type="button"
                                    style={subtleButtonStyle}
                                    onClick={() =>
                                      completeTradeMutation.mutate({
                                        tradeId: trade.id,
                                        rating: Number(pendingRatingValue),
                                      })
                                    }
                                    disabled={isCompletingCurrentTrade}
                                  >
                                    {isCompletingCurrentTrade ? 'Completing...' : 'Complete'}
                                  </button>
                                </div>
                              ) : null}
                            </article>
                          );
                        })}
                      </div>
                    )}
                  </section>
                );
              })}
            </div>
          ) : null}

          {acceptTradeMutation.isError ? (
            <p style={{ marginTop: '1rem', marginBottom: 0, color: '#b91c1c' }}>
              {getErrorMessage(acceptTradeMutation.error, 'Unable to accept this trade.')}
            </p>
          ) : null}

          {completeTradeMutation.isError ? (
            <p style={{ marginTop: '1rem', marginBottom: 0, color: '#b91c1c' }}>
              {getErrorMessage(completeTradeMutation.error, 'Unable to complete this trade.')}
            </p>
          ) : null}
        </section>
      </section>
    </main>
  );
}
