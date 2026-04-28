import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../api/axiosConfig.js';
import AppNavigation from '../components/AppNavigation.jsx';
import CreditDisplay, { CURRENT_USER_QUERY_KEY } from '../components/CreditDisplay.jsx';

const MARKETPLACE_SKILLS_QUERY_KEY = ['marketplace-skills'];
const TRADES_QUERY_KEY = ['trades'];

const headerRowStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'flex-start',
  gap: '1rem',
  flexWrap: 'wrap',
};

const fullWidthStyle = {
  gridColumn: '1 / -1',
};

const tradeStatusToChipClassName = {
  PENDING: 'app-chip app-chip-pending',
  ACTIVE: 'app-chip app-chip-active',
  COMPLETED: 'app-chip app-chip-completed',
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
      const response = await api.get('/api/users/me');
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
      const response = await api.get('/api/skills/marketplace');
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
      const response = await api.get('/api/trades');
      return response.data ?? [];
    },
  });

  const groupedTrades = useMemo(() => groupTradesByStatus(trades), [trades]);

  const createTradeMutation = useMutation({
    mutationFn: async (payload) => {
      const response = await api.post('/api/trades', payload);
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
      queryClient.invalidateQueries({ queryKey: CURRENT_USER_QUERY_KEY });
    },
  });

  const acceptTradeMutation = useMutation({
    mutationFn: async (tradeId) => {
      const response = await api.put(`/api/trades/${tradeId}/accept`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TRADES_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: CURRENT_USER_QUERY_KEY });
    },
  });

  const completeTradeMutation = useMutation({
    mutationFn: async ({ tradeId, rating }) => {
      const response = await api.put(`/api/trades/${tradeId}/complete`, { rating });
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
    acceptTradeMutation.reset();
    completeTradeMutation.reset();
  };

  const handleResetRequest = () => {
    setSelectedSkill(null);
    setFormData({
      scheduledTime: '',
      duration: 1,
    });
    createTradeMutation.reset();
  };

  const handleAcceptTrade = (tradeId) => {
    createTradeMutation.reset();
    completeTradeMutation.reset();
    acceptTradeMutation.mutate(tradeId);
  };

  const handleCompleteTrade = (tradeId, rating) => {
    createTradeMutation.reset();
    acceptTradeMutation.reset();
    completeTradeMutation.mutate({
      tradeId,
      rating,
    });
  };

  const handleCreateTrade = (event) => {
    event.preventDefault();

    if (!selectedSkill) {
      return;
    }

    acceptTradeMutation.reset();
    completeTradeMutation.reset();
    createTradeMutation.mutate({
      providerId: selectedSkill.provider.id,
      skillId: selectedSkill.id,
      scheduledTime: formData.scheduledTime,
      duration: Number(formData.duration),
    });
  };

  return (
    <main className="app-page">
      <section className="app-layout">
        <div>
          <AppNavigation />

          <header className="app-panel app-hero-panel app-panel-soft">
            <div style={headerRowStyle}>
              <div>
                <p className="app-eyebrow">
                  Trade Marketplace
                </p>
                <h1 style={{ marginTop: 0, marginBottom: '0.75rem' }}>Find skills and manage live trades</h1>
                <p className="app-subtle-text" style={{ margin: 0 }}>
                  Browse offered skills, request sessions, and act on trades tied to your account.
                </p>
              </div>

              <CreditDisplay />
            </div>

            {isCurrentUserError ? (
              <p className="app-feedback-error" style={{ marginTop: '1rem' }}>
                {getErrorMessage(currentUserError, 'Unable to load your profile.')}
              </p>
            ) : null}
          </header>
        </div>

        <section className="app-panel">
          <div className="app-section-header">
            <div>
              <h2 style={{ margin: 0 }}>Marketplace</h2>
              <p className="app-subtle-text" style={{ margin: '0.35rem 0 0' }}>
                Offered skills returned by <code>/api/skills/marketplace</code>.
              </p>
            </div>
            <button
              className="app-button app-button-secondary"
              type="button"
              onClick={() => queryClient.invalidateQueries({ queryKey: MARKETPLACE_SKILLS_QUERY_KEY })}
              disabled={isMarketplaceLoading}
            >
              Refresh
            </button>
          </div>

          {isMarketplaceLoading ? <p style={{ margin: 0 }}>Loading marketplace skills...</p> : null}

          {isMarketplaceError ? (
            <p className="app-feedback-error">
              {getErrorMessage(marketplaceError, 'Unable to load marketplace skills.')}
            </p>
          ) : null}

          {!isMarketplaceLoading && !isMarketplaceError && marketplaceSkills.length === 0 ? (
            <div className="app-empty-state">
              <h3 style={{ marginTop: 0 }}>No marketplace skills yet</h3>
              <p className="app-subtle-text" style={{ margin: 0 }}>
                Once other users add offered skills, they will appear here for trading.
              </p>
            </div>
          ) : null}

          {!isMarketplaceLoading && !isMarketplaceError && marketplaceSkills.length > 0 ? (
            <div className="app-card-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(270px, 1fr))' }}>
              {marketplaceSkills.map((skill) => {
                const isSelected = selectedSkill?.id === skill.id;

                return (
                  <article key={skill.id} className="app-item-card" style={{ display: 'grid', gap: '0.9rem', textAlign: 'left' }}>
                    <div>
                      <p className="app-meta-text">
                        {skill.category || 'Uncategorized'}
                      </p>
                      <h3 style={{ margin: '0.35rem 0 0' }}>{skill.title}</h3>
                    </div>

                    <div className="app-chip-row">
                      <span className="app-chip">{skill.skillCategory ?? 'UNKNOWN'}</span>
                      <span className="app-chip">{skill.creditValue ?? 0} credits</span>
                    </div>

                    <p className="app-subtle-text">{skill.description}</p>

                    <div className="app-detail-list">
                      <span>Provider: {skill.provider?.name ?? 'Unknown'}</span>
                      <span>{skill.provider?.email ?? 'No email available'}</span>
                    </div>
                    <div className="app-chip-row">
                      <span className="app-chip">
                        {skill.averageRating?.toFixed(1) ?? 0} 
                      </span>

                      <span className="app-chip">
                        Expertise: {skill.confidenceIndex?.toFixed(0) ?? 0}
                      </span>
                    </div>
                    <div>
                      <button className="app-button" type="button" onClick={() => handleSelectSkill(skill)}>
                        {isSelected ? 'Selected' : 'Request Trade'}
                      </button>
                    </div>
                  </article>
                );
              })}
            </div>
          ) : null}
        </section>

        <section className="app-panel">
          <h2 style={{ marginTop: 0 }}>Request Trade</h2>
          <p className="app-subtle-text" style={{ marginTop: 0 }}>
            Choose a marketplace skill, then schedule a session request.
          </p>

          {selectedSkill ? (
            <div className="app-highlight-card" style={{ marginBottom: '1rem' }}>
              <p style={{ margin: 0, color: '#1d4ed8', fontWeight: 700 }}>{selectedSkill.title}</p>
              <p style={{ marginTop: '0.3rem', color: '#334155' }}>
                {selectedSkill.provider?.name} ({selectedSkill.provider?.email}) | {selectedSkill.creditValue ?? 0} credits
              </p>
            </div>
          ) : (
            <div className="app-empty-state" style={{ marginBottom: '1rem' }}>
              <h3 style={{ marginTop: 0 }}>Select a marketplace skill</h3>
              <p className="app-subtle-text" style={{ margin: 0 }}>
                Use the Request Trade button on a marketplace card to prefill this form.
              </p>
            </div>
          )}

          <form className="app-form-grid" onSubmit={handleCreateTrade}>
            <div className="field-group">
              <label className="field-label" htmlFor="scheduledTime">Scheduled Time</label>
              <input
                className="app-input"
                id="scheduledTime"
                name="scheduledTime"
                type="datetime-local"
                value={formData.scheduledTime}
                onChange={handleFormChange}
                required
                disabled={!selectedSkill || createTradeMutation.isPending}
              />
            </div>

            <div className="field-group">
              <label className="field-label" htmlFor="duration">Duration</label>
              <input
                className="app-input"
                id="duration"
                name="duration"
                type="number"
                min="1"
                step="1"
                value={formData.duration}
                onChange={handleFormChange}
                required
                disabled={!selectedSkill || createTradeMutation.isPending}
              />
            </div>

            {createTradeMutation.isError ? (
              <p className="app-feedback-error" style={fullWidthStyle}>
                {getErrorMessage(createTradeMutation.error, 'Unable to create trade right now.')}
              </p>
            ) : null}

            {createTradeMutation.isSuccess ? (
              <p className="app-feedback-success" style={fullWidthStyle}>
                Trade request created successfully.
              </p>
            ) : null}

            <div className="app-actions-row" style={fullWidthStyle}>
              <button
                className="app-button"
                type="submit"
                disabled={!selectedSkill || createTradeMutation.isPending}
              >
                {createTradeMutation.isPending ? 'Submitting...' : 'Submit Trade Request'}
              </button>

              <button
                className="app-button app-button-secondary"
                type="button"
                onClick={handleResetRequest}
                disabled={createTradeMutation.isPending}
              >
                Clear
              </button>
            </div>
          </form>
        </section>

        <section className="app-panel">
          <div className="app-section-header">
            <div>
              <h2 style={{ margin: 0 }}>My Trades</h2>
              <p className="app-subtle-text" style={{ margin: '0.35rem 0 0' }}>
                Trades where you are the requester or provider from <code>/api/trades</code>.
              </p>
            </div>
            <button
              className="app-button app-button-secondary"
              type="button"
              onClick={() => queryClient.invalidateQueries({ queryKey: TRADES_QUERY_KEY })}
              disabled={isTradesLoading}
            >
              Refresh
            </button>
          </div>

          {isTradesLoading ? <p style={{ margin: 0 }}>Loading trades...</p> : null}

          {isTradesError ? (
            <p className="app-feedback-error">
              {getErrorMessage(tradesError, 'Unable to load trades.')}
            </p>
          ) : null}

          {!isTradesLoading && !isTradesError ? (
            <div className="app-stack-lg">
              {['PENDING', 'ACTIVE', 'COMPLETED'].map((status) => {
                const tradesForStatus = groupedTrades[status];
                const statusClassName =
                  tradeStatusToChipClassName[status] ?? 'app-chip';

                return (
                  <section key={status}>
                    <div className="app-section-header">
                      <h3 style={{ marginTop: 0 }}>
                        {status.charAt(0) + status.slice(1).toLowerCase()}
                      </h3>
                      <span className={statusClassName}>{tradesForStatus.length} trade(s)</span>
                    </div>

                    {tradesForStatus.length === 0 ? (
                      <div className="app-empty-state">
                        <p className="app-subtle-text" style={{ margin: 0 }}>
                          No {status.toLowerCase()} trades right now.
                        </p>
                      </div>
                    ) : (
                      <div className="app-card-grid">
                        {tradesForStatus.map((trade) => {
                          const isProvider = currentUser?.id === trade.provider?.id;
                          const isRequester = currentUser?.id === trade.requester?.id;
                          const pendingRatingValue = ratingInputs[trade.id] ?? trade.rating ?? 5;
                          const isCompletingCurrentTrade =
                            completeTradeMutation.isPending && completeTradeMutation.variables?.tradeId === trade.id;
                          const isAcceptingCurrentTrade =
                            acceptTradeMutation.isPending && acceptTradeMutation.variables === trade.id;

                          return (
                            <article key={trade.id} className="app-item-card" style={{ display: 'grid', gap: '0.9rem' }}>
                              <div className="app-section-header" style={{ marginBottom: 0 }}>
                                <div>
                                  <p className="app-meta-text">
                                    {trade.skill?.creditValue ?? 0} credits
                                  </p>
                                  <h3 style={{ margin: '0.35rem 0 0' }}>{trade.skill?.title ?? 'Untitled skill'}</h3>
                                </div>
                                <span className={tradeStatusToChipClassName[trade.status] ?? 'app-chip'}>
                                  {trade.status}
                                </span>
                              </div>

                              <div className="app-detail-list">
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
                                    className="app-button"
                                    type="button"
                                    onClick={() => handleAcceptTrade(trade.id)}
                                    disabled={isAcceptingCurrentTrade}
                                  >
                                    {isAcceptingCurrentTrade ? 'Accepting...' : 'Accept'}
                                  </button>
                                </div>
                              ) : null}

                              {trade.status === 'ACTIVE' && isRequester ? (
                                <div className="app-actions-row">
                                  <div className="field-group">
                                    <label className="field-label" htmlFor={`rating-${trade.id}`}>Rating</label>
                                    <input
                                      className="app-input"
                                      id={`rating-${trade.id}`}
                                      type="number"
                                      min="1"
                                      max="5"
                                      step="1"
                                      value={pendingRatingValue}
                                      onChange={(event) => handleRatingChange(trade.id, event.target.value)}
                                      style={{ width: '110px' }}
                                      disabled={isCompletingCurrentTrade}
                                    />
                                  </div>

                                  <button
                                    className="app-button app-button-subtle"
                                    type="button"
                                    onClick={() => handleCompleteTrade(trade.id, Number(pendingRatingValue))}
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
            <p className="app-feedback-error" style={{ marginTop: '1rem' }}>
              {getErrorMessage(acceptTradeMutation.error, 'Unable to accept this trade.')}
            </p>
          ) : null}

          {acceptTradeMutation.isSuccess ? (
            <p className="app-feedback-success" style={{ marginTop: '1rem' }}>
              Trade accepted successfully.
            </p>
          ) : null}

          {completeTradeMutation.isError ? (
            <p className="app-feedback-error" style={{ marginTop: '1rem' }}>
              {getErrorMessage(completeTradeMutation.error, 'Unable to complete this trade.')}
            </p>
          ) : null}

          {completeTradeMutation.isSuccess ? (
            <p className="app-feedback-success" style={{ marginTop: '1rem' }}>
              Trade completed and credits refreshed.
            </p>
          ) : null}
        </section>
      </section>
    </main>
  );
}
