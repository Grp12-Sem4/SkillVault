import { useContext, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../api/axiosConfig.js';
import AppNavigation from '../components/AppNavigation.jsx';
import AuthContext from '../context/AuthContext.jsx';

const ALL_TOPICS_QUERY_KEY = ['knowledge', 'all-topics'];
const NEEDS_REVISION_QUERY_KEY = ['knowledge', 'needs-revision'];

const pageStyle = {
  minHeight: '100vh',
  padding: '2rem',
  backgroundColor: '#f5f5f5',
};

const layoutStyle = {
  maxWidth: '1100px',
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

const formStyle = {
  display: 'grid',
  gap: '0.9rem',
};

const inputStyle = {
  width: '100%',
  padding: '0.8rem 0.9rem',
  borderRadius: '10px',
  border: '1px solid #cbd5e1',
  boxSizing: 'border-box',
  font: 'inherit',
};

const textareaStyle = {
  ...inputStyle,
  minHeight: '140px',
  resize: 'vertical',
};

function getErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message ?? fallbackMessage;
}

function formatDate(value) {
  if (!value) {
    return 'Not available';
  }

  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleDateString();
}

function formatMastery(value) {
  const numericValue = Number(value);

  if (Number.isNaN(numericValue)) {
    return 'Unknown';
  }

  return `${Math.round(numericValue)}%`;
}

function truncateText(value, maxLength = 160) {
  if (!value) {
    return '';
  }

  return value.length > maxLength ? `${value.slice(0, maxLength)}...` : value;
}

function getStatusChipClassName(status) {
  switch (status) {
    case 'CURRENT':
      return 'app-chip app-chip-active';
    case 'DECAYING':
      return 'app-chip app-chip-pending';
    case 'NEEDS_REVISION':
      return 'app-chip app-chip-danger';
    case 'PUBLISHED':
      return 'app-chip app-chip-completed';
    default:
      return 'app-chip';
  }
}

function getMasteryBarClassName(masteryLevel) {
  if (masteryLevel < 40) {
    return 'app-progress-bar app-progress-bar-danger';
  }

  if (masteryLevel < 75) {
    return 'app-progress-bar app-progress-bar-low';
  }

  return 'app-progress-bar';
}

export default function Knowledge() {
  const queryClient = useQueryClient();
  const { user } = useContext(AuthContext);
  const [selectedSubject, setSelectedSubject] = useState('ALL');
  const [formData, setFormData] = useState({
    title: '',
    subject: '',
    content: '',
  });

  const {
    data: allTopics = [],
    isLoading: isAllTopicsLoading,
    isError: isAllTopicsError,
    error: allTopicsError,
  } = useQuery({
    queryKey: ALL_TOPICS_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/api/knowledge');
      return response.data ?? [];
    },
  });

  const {
    data: revisionTopics = [],
    isLoading: isRevisionTopicsLoading,
    isError: isRevisionTopicsError,
    error: revisionTopicsError,
  } = useQuery({
    queryKey: NEEDS_REVISION_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/api/knowledge/needs-revision');
      return response.data ?? [];
    },
  });

  const createTopicMutation = useMutation({
    mutationFn: async (payload) => {
      const response = await api.post('/api/knowledge', payload);
      return response.data;
    },
    onSuccess: () => {
      setFormData({
        title: '',
        subject: '',
        content: '',
      });
      queryClient.invalidateQueries({ queryKey: ALL_TOPICS_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: NEEDS_REVISION_QUERY_KEY });
    },
  });

  const reviewTopicMutation = useMutation({
    mutationFn: async (topicId) => {
      const response = await api.put(`/api/knowledge/${topicId}/review`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ALL_TOPICS_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: NEEDS_REVISION_QUERY_KEY });
    },
  });

  const subjectOptions = useMemo(() => {
    const subjects = new Set(
      allTopics
        .map((topic) => topic.subject?.trim())
        .filter(Boolean),
    );

    return ['ALL', ...Array.from(subjects).sort((left, right) => left.localeCompare(right))];
  }, [allTopics]);

  const filteredTopics = useMemo(() => {
    if (selectedSubject === 'ALL') {
      return allTopics;
    }

    return allTopics.filter((topic) => topic.subject === selectedSubject);
  }, [allTopics, selectedSubject]);

  const knowledgeSummary = useMemo(() => {
    const totalTopics = allTopics.length;
    const revisionCount = revisionTopics.length;
    const averageMastery = totalTopics
      ? Math.round(allTopics.reduce((sum, topic) => sum + (Number(topic.masteryLevel) || 0), 0) / totalTopics)
      : 0;
    const distinctSubjects = new Set(
      allTopics.map((topic) => topic.subject?.trim()).filter(Boolean),
    ).size;

    const masteryDistribution = allTopics.reduce(
      (counts, topic) => {
        const masteryLevel = Number(topic.masteryLevel) || 0;

        if (masteryLevel >= 75) {
          counts.high += 1;
        } else if (masteryLevel >= 40) {
          counts.medium += 1;
        } else {
          counts.low += 1;
        }

        return counts;
      },
      { high: 0, medium: 0, low: 0 },
    );

    return {
      totalTopics,
      revisionCount,
      averageMastery,
      distinctSubjects,
      masteryDistribution,
    };
  }, [allTopics, revisionTopics]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((currentFormData) => ({
      ...currentFormData,
      [name]: value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    createTopicMutation.mutate({
      title: formData.title.trim(),
      subject: formData.subject.trim(),
      content: formData.content.trim(),
    });
  };

  return (
    <main className="app-page" style={pageStyle}>
      <section className="app-layout" style={layoutStyle}>
        <div>
          <AppNavigation />

          <header className="app-panel app-hero-panel app-panel-soft" style={panelStyle}>
            <p className="app-eyebrow">
              Signed in as {user?.email ?? user?.sub ?? 'authenticated user'}
            </p>
            <h1 style={{ marginTop: 0, marginBottom: '0.75rem' }}>Knowledge Vault</h1>
            <p className="app-subtle-text" style={{ margin: 0 }}>
              Track all of your learning topics, spot revision risk early, and refresh topics that need attention.
            </p>
          </header>
        </div>

        <section className="app-panel" style={panelStyle}>
          <div className="app-section-header">
            <div>
              <h2 style={{ margin: 0 }}>Knowledge Summary</h2>
              <p className="app-subtle-text" style={{ margin: '0.35rem 0 0' }}>
                A quick snapshot of the current user’s topic library and revision risk.
              </p>
            </div>
          </div>

          {isAllTopicsLoading ? <p>Loading summary...</p> : null}

          {isAllTopicsError ? (
            <p className="app-feedback-error">
              {getErrorMessage(allTopicsError, 'Unable to load your knowledge summary.')}
            </p>
          ) : null}

          {!isAllTopicsLoading && !isAllTopicsError ? (
            <div className="app-stack-lg">
              <div className="app-card-grid-compact">
                <article className="app-stat-card">
                  <p className="app-eyebrow" style={{ marginBottom: 0 }}>Total Topics</p>
                  <p className="app-stat-value">{knowledgeSummary.totalTopics}</p>
                  <p className="app-subtle-text">Everything in your personal knowledge vault.</p>
                </article>

                <article className="app-stat-card">
                  <p className="app-eyebrow" style={{ marginBottom: 0 }}>Needs Revision</p>
                  <p className="app-stat-value">{knowledgeSummary.revisionCount}</p>
                  <p className="app-subtle-text">Topics already flagged for another review pass.</p>
                </article>

                <article className="app-stat-card">
                  <p className="app-eyebrow" style={{ marginBottom: 0 }}>Average Mastery</p>
                  <p className="app-stat-value">{knowledgeSummary.averageMastery}%</p>
                  <p className="app-subtle-text">Average stored mastery across all your topics.</p>
                </article>

                <article className="app-stat-card">
                  <p className="app-eyebrow" style={{ marginBottom: 0 }}>Subjects</p>
                  <p className="app-stat-value">{knowledgeSummary.distinctSubjects}</p>
                  <p className="app-subtle-text">Distinct subject areas currently represented.</p>
                </article>
              </div>

              <div className="app-card-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))' }}>
                <article className="app-item-card" style={{ display: 'grid', gap: '0.9rem' }}>
                  <div>
                    <h3>Mastery Distribution</h3>
                    <p className="app-subtle-text" style={{ marginTop: '0.35rem' }}>
                      Simple buckets to show how healthy your knowledge portfolio looks right now.
                    </p>
                  </div>

                  <div className="app-chip-row">
                    <span className="app-chip app-chip-active">High: {knowledgeSummary.masteryDistribution.high}</span>
                    <span className="app-chip app-chip-pending">Medium: {knowledgeSummary.masteryDistribution.medium}</span>
                    <span className="app-chip app-chip-danger">Low: {knowledgeSummary.masteryDistribution.low}</span>
                  </div>
                </article>

                <article className="app-item-card" style={{ display: 'grid', gap: '0.9rem' }}>
                  <div>
                    <h3>Health Snapshot</h3>
                    <p className="app-subtle-text" style={{ marginTop: '0.35rem' }}>
                      {knowledgeSummary.revisionCount === 0
                        ? 'Your current topics are in a healthy state with nothing urgently flagged.'
                        : `${knowledgeSummary.revisionCount} topic(s) need review soon. Use the revision list below to refresh them.`}
                    </p>
                  </div>

                  <div className="app-progress">
                    <div
                      className={getMasteryBarClassName(knowledgeSummary.averageMastery)}
                      style={{ width: `${Math.max(0, Math.min(100, knowledgeSummary.averageMastery))}%` }}
                    />
                  </div>

                  <p className="app-subtle-text">
                    Portfolio mastery indicator: {knowledgeSummary.averageMastery}%
                  </p>
                </article>
              </div>
            </div>
          ) : null}
        </section>

        <section className="app-panel" style={panelStyle}>
          <h2 style={{ marginTop: 0 }}>Create Topic</h2>
          <form onSubmit={handleSubmit} style={formStyle}>
            <div className="field-group">
              <label className="field-label" htmlFor="title">Title</label>
              <input
                className="app-input"
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleChange}
                style={inputStyle}
                required
              />
            </div>

            <div className="field-group">
              <label className="field-label" htmlFor="subject">Subject</label>
              <input
                className="app-input"
                id="subject"
                name="subject"
                type="text"
                value={formData.subject}
                onChange={handleChange}
                style={inputStyle}
                required
              />
            </div>

            <div className="field-group">
              <label className="field-label" htmlFor="content">Content</label>
              <textarea
                className="app-input app-textarea"
                id="content"
                name="content"
                value={formData.content}
                onChange={handleChange}
                style={textareaStyle}
                required
              />
            </div>

            {createTopicMutation.isError ? (
              <p className="app-feedback-error">
                {getErrorMessage(createTopicMutation.error, 'Unable to create knowledge topic right now.')}
              </p>
            ) : null}

            {createTopicMutation.isSuccess ? (
              <p className="app-feedback-success">Knowledge topic created.</p>
            ) : null}

            <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
              <button
                className="app-button"
                type="submit"
                disabled={createTopicMutation.isPending}
              >
                {createTopicMutation.isPending ? 'Creating...' : 'Create Topic'}
              </button>
            </div>
          </form>
        </section>

        <section className="app-panel" style={panelStyle}>
          <div className="app-section-header">
            <div>
              <h2 style={{ margin: 0 }}>All Topics</h2>
              <p className="app-subtle-text" style={{ margin: '0.35rem 0 0' }}>
                Your full authenticated topic list from <code>/api/knowledge</code>.
              </p>
            </div>

            <div className="app-actions-row">
              <select
                className="app-input app-inline-filter"
                value={selectedSubject}
                onChange={(event) => setSelectedSubject(event.target.value)}
                aria-label="Filter topics by subject"
              >
                {subjectOptions.map((subject) => (
                  <option key={subject} value={subject}>
                    {subject === 'ALL' ? 'All subjects' : subject}
                  </option>
                ))}
              </select>

              <button
                className="app-button app-button-secondary"
                type="button"
                onClick={() => {
                  queryClient.invalidateQueries({ queryKey: ALL_TOPICS_QUERY_KEY });
                  queryClient.invalidateQueries({ queryKey: NEEDS_REVISION_QUERY_KEY });
                }}
                disabled={isAllTopicsLoading}
              >
                Refresh
              </button>
            </div>
          </div>

          {isAllTopicsLoading ? <p>Loading all topics...</p> : null}

          {isAllTopicsError ? (
            <p className="app-feedback-error">
              {getErrorMessage(allTopicsError, 'Unable to load your topics.')}
            </p>
          ) : null}

          {!isAllTopicsLoading && !isAllTopicsError && filteredTopics.length === 0 ? (
            <div className="app-empty-state">
              <h3 style={{ marginTop: 0 }}>No topics to show</h3>
              <p className="app-subtle-text" style={{ margin: 0 }}>
                {allTopics.length === 0
                  ? 'Create your first knowledge topic to start building the vault.'
                  : 'No topics match the current subject filter.'}
              </p>
            </div>
          ) : null}

          {!isAllTopicsLoading && !isAllTopicsError && filteredTopics.length > 0 ? (
            <div className="app-card-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))' }}>
              {filteredTopics.map((topic) => {
                const masteryLevel = Number(topic.masteryLevel) || 0;
                const isReviewingCurrentTopic =
                  reviewTopicMutation.isPending && reviewTopicMutation.variables === topic.id;

                return (
                  <article key={topic.id} className="app-item-card" style={{ display: 'grid', gap: '0.95rem' }}>
                    <div className="app-section-header" style={{ marginBottom: 0 }}>
                      <div>
                        <p className="app-meta-text">{topic.subject || 'No subject'}</p>
                        <h3 style={{ marginTop: '0.35rem' }}>{topic.title}</h3>
                      </div>
                      <span className={getStatusChipClassName(topic.status)}>
                        {topic.needsRevision ? 'Needs Revision' : topic.status ?? 'Unknown'}
                      </span>
                    </div>

                    <div style={{ display: 'grid', gap: '0.45rem' }}>
                      <div className="app-section-header" style={{ marginBottom: 0 }}>
                        <span className="app-subtle-text">Mastery</span>
                        <span style={{ fontWeight: 700 }}>{formatMastery(masteryLevel)}</span>
                      </div>
                      <div className="app-progress">
                        <div
                          className={getMasteryBarClassName(masteryLevel)}
                          style={{ width: `${Math.max(0, Math.min(100, masteryLevel))}%` }}
                        />
                      </div>
                    </div>

                    <div className="app-chip-row">
                      <span className="app-chip">Decay Rate: {Number(topic.decayRate ?? 0).toFixed(2)}</span>
                      <span className="app-chip">Last Reviewed: {formatDate(topic.lastReviewed)}</span>
                    </div>

                    <div className="app-detail-list">
                      <span>Last decay check: {formatDate(topic.lastDecayCheck)}</span>
                      <span>Created: {formatDate(topic.createdAt)}</span>
                    </div>

                    {topic.content ? (
                      <p className="app-subtle-text">{truncateText(topic.content)}</p>
                    ) : null}

                    {topic.needsRevision ? (
                      <div>
                        <button
                          className="app-button"
                          type="button"
                          onClick={() => reviewTopicMutation.mutate(topic.id)}
                          disabled={isReviewingCurrentTopic}
                        >
                          {isReviewingCurrentTopic ? 'Reviewing...' : 'Mark as Reviewed'}
                        </button>
                      </div>
                    ) : null}
                  </article>
                );
              })}
            </div>
          ) : null}
        </section>

        <section className="app-panel" style={panelStyle}>
          <div className="app-section-header">
            <div>
              <h2 style={{ margin: 0 }}>Needs Revision</h2>
              <p className="app-subtle-text" style={{ margin: '0.35rem 0 0' }}>
                Topics returned by <code>/api/knowledge/needs-revision</code>.
              </p>
            </div>
            <button
              className="app-button app-button-secondary"
              type="button"
              onClick={() => queryClient.invalidateQueries({ queryKey: NEEDS_REVISION_QUERY_KEY })}
              disabled={isRevisionTopicsLoading}
            >
              Refresh
            </button>
          </div>

          {isRevisionTopicsLoading ? <p>Loading revision queue...</p> : null}

          {isRevisionTopicsError ? (
            <p className="app-feedback-error">
              {getErrorMessage(revisionTopicsError, 'Unable to load topics needing revision.')}
            </p>
          ) : null}

          {!isRevisionTopicsLoading && !isRevisionTopicsError && revisionTopics.length === 0 ? (
            <div className="app-empty-state">
              <h3 style={{ marginTop: 0 }}>Nothing needs review right now</h3>
              <p className="app-subtle-text" style={{ margin: 0 }}>
                Your revision queue is clear for the moment.
              </p>
            </div>
          ) : null}

          {!isRevisionTopicsLoading && !isRevisionTopicsError && revisionTopics.length > 0 ? (
            <div className="app-card-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))' }}>
              {revisionTopics.map((topic) => {
                const isReviewingCurrentTopic =
                  reviewTopicMutation.isPending && reviewTopicMutation.variables === topic.id;
                const masteryLevel = Number(topic.masteryLevel) || 0;

                return (
                  <article key={topic.id} className="app-item-card" style={{ display: 'grid', gap: '0.95rem' }}>
                    <div className="app-section-header" style={{ marginBottom: 0 }}>
                      <div>
                        <p className="app-meta-text">{topic.subject || 'No subject'}</p>
                        <h3 style={{ marginTop: '0.35rem' }}>{topic.title}</h3>
                      </div>
                      <span className={getStatusChipClassName(topic.status)}>
                        {topic.status ?? 'Unknown'}
                      </span>
                    </div>

                    <div style={{ display: 'grid', gap: '0.45rem' }}>
                      <div className="app-section-header" style={{ marginBottom: 0 }}>
                        <span className="app-subtle-text">Mastery</span>
                        <span style={{ fontWeight: 700 }}>{formatMastery(masteryLevel)}</span>
                      </div>
                      <div className="app-progress">
                        <div
                          className={getMasteryBarClassName(masteryLevel)}
                          style={{ width: `${Math.max(0, Math.min(100, masteryLevel))}%` }}
                        />
                      </div>
                    </div>

                    <div className="app-detail-list">
                      <span>Last reviewed: {formatDate(topic.lastReviewed)}</span>
                      <span>Decay rate: {Number(topic.decayRate ?? 0).toFixed(2)}</span>
                    </div>

                    {topic.content ? (
                      <p className="app-subtle-text">{truncateText(topic.content)}</p>
                    ) : null}

                    <div>
                      <button
                        className="app-button"
                        type="button"
                        onClick={() => reviewTopicMutation.mutate(topic.id)}
                        disabled={isReviewingCurrentTopic}
                      >
                        {isReviewingCurrentTopic ? 'Reviewing...' : 'Mark as Reviewed'}
                      </button>
                    </div>
                  </article>
                );
              })}
            </div>
          ) : null}

          {reviewTopicMutation.isError ? (
            <p className="app-feedback-error" style={{ marginTop: '1rem' }}>
              {getErrorMessage(reviewTopicMutation.error, 'Unable to mark the topic as reviewed.')}
            </p>
          ) : null}
        </section>
      </section>
    </main>
  );
}
