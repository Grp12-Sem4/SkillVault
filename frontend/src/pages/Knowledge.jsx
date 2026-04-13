import { useContext, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../api/axiosConfig.js';
import AuthContext from '../context/AuthContext.jsx';

const KNOWLEDGE_QUERY_KEY = ['knowledge', 'needs-revision'];

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

const topicGridStyle = {
  display: 'grid',
  gap: '1rem',
  gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
};

const topicCardStyle = {
  border: '1px solid #e2e8f0',
  borderRadius: '14px',
  padding: '1rem',
  backgroundColor: '#f8fafc',
  display: 'grid',
  gap: '0.8rem',
  textAlign: 'left',
};

const badgeStyle = {
  display: 'inline-flex',
  alignItems: 'center',
  width: 'fit-content',
  padding: '0.25rem 0.6rem',
  borderRadius: '999px',
  backgroundColor: '#dbeafe',
  color: '#1d4ed8',
  fontSize: '0.85rem',
  fontWeight: 600,
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

const emptyStateStyle = {
  padding: '2rem',
  borderRadius: '14px',
  border: '1px dashed #cbd5e1',
  backgroundColor: '#f8fafc',
};

function getErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message ?? fallbackMessage;
}

export default function Knowledge() {
  const queryClient = useQueryClient();
  const { user } = useContext(AuthContext);
  const [formData, setFormData] = useState({
    title: '',
    subject: '',
    content: '',
  });

  const {
    data: topics = [],
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: KNOWLEDGE_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/knowledge/needs-revision');
      return response.data ?? [];
    },
  });

  const createTopicMutation = useMutation({
    mutationFn: async (payload) => {
      const response = await api.post('/knowledge', payload);
      return response.data;
    },
    onSuccess: () => {
      setFormData({
        title: '',
        subject: '',
        content: '',
      });
      queryClient.invalidateQueries({ queryKey: KNOWLEDGE_QUERY_KEY });
    },
  });

  const reviewTopicMutation = useMutation({
    mutationFn: async (topicId) => {
      const response = await api.put(`/knowledge/${topicId}/review`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: KNOWLEDGE_QUERY_KEY });
    },
  });

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
    <main style={pageStyle}>
      <section style={layoutStyle}>
        <header style={panelStyle}>
          <p style={{ marginBottom: '0.5rem', color: '#475569' }}>
            Signed in as {user?.email ?? user?.sub ?? 'authenticated user'}
          </p>
          <h1 style={{ marginTop: 0, marginBottom: '0.75rem' }}>Knowledge Vault</h1>
          <p style={{ margin: 0, color: '#475569' }}>
            Create new knowledge topics and review the ones that the backend has flagged for revision.
          </p>
        </header>

        <section style={panelStyle}>
          <h2 style={{ marginTop: 0 }}>Create Topic</h2>
          <form onSubmit={handleSubmit} style={formStyle}>
            <div>
              <label htmlFor="title">Title</label>
              <input
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleChange}
                style={inputStyle}
                required
              />
            </div>

            <div>
              <label htmlFor="subject">Subject</label>
              <input
                id="subject"
                name="subject"
                type="text"
                value={formData.subject}
                onChange={handleChange}
                style={inputStyle}
                required
              />
            </div>

            <div>
              <label htmlFor="content">Content</label>
              <textarea
                id="content"
                name="content"
                value={formData.content}
                onChange={handleChange}
                style={textareaStyle}
                required
              />
            </div>

            {createTopicMutation.isError ? (
              <p style={{ margin: 0, color: '#b91c1c' }}>
                {getErrorMessage(createTopicMutation.error, 'Unable to create knowledge topic right now.')}
              </p>
            ) : null}

            {createTopicMutation.isSuccess ? (
              <p style={{ margin: 0, color: '#15803d' }}>Knowledge topic created.</p>
            ) : null}

            <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
              <button
                type="submit"
                style={buttonStyle}
                disabled={createTopicMutation.isPending}
              >
                {createTopicMutation.isPending ? 'Creating...' : 'Create Topic'}
              </button>
            </div>
          </form>
        </section>

        <section style={panelStyle}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              gap: '1rem',
              marginBottom: '1rem',
              flexWrap: 'wrap',
            }}
          >
            <div>
              <h2 style={{ margin: 0 }}>Needs Revision</h2>
              <p style={{ margin: '0.35rem 0 0', color: '#475569' }}>
                Topics returned by <code>/api/knowledge/needs-revision</code>.
              </p>
            </div>
            <button
              type="button"
              style={secondaryButtonStyle}
              onClick={() => queryClient.invalidateQueries({ queryKey: KNOWLEDGE_QUERY_KEY })}
              disabled={isLoading}
            >
              Refresh
            </button>
          </div>

          {isLoading ? <p style={{ margin: 0 }}>Loading topics...</p> : null}

          {isError ? (
            <p style={{ margin: 0, color: '#b91c1c' }}>
              {getErrorMessage(error, 'Unable to load knowledge topics.')}
            </p>
          ) : null}

          {!isLoading && !isError && topics.length === 0 ? (
            <div style={emptyStateStyle}>
              <h3 style={{ marginTop: 0 }}>Nothing needs review right now</h3>
              <p style={{ margin: 0, color: '#475569' }}>
                Once the backend flags topics for revision, they will appear here.
              </p>
            </div>
          ) : null}

          {!isLoading && !isError && topics.length > 0 ? (
            <div style={topicGridStyle}>
              {topics.map((topic) => {
                const isReviewingCurrentTopic =
                  reviewTopicMutation.isPending && reviewTopicMutation.variables === topic.id;

                return (
                  <article key={topic.id} style={topicCardStyle}>
                    <div>
                      <p style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>{topic.subject}</p>
                      <h3 style={{ margin: '0.35rem 0 0' }}>{topic.title}</h3>
                    </div>

                    <div style={badgeStyle}>
                      Mastery: {topic.masteryLevel ?? 'Unknown'}
                    </div>

                    {topic.content ? (
                      <p style={{ margin: 0, color: '#475569' }}>
                        {topic.content.length > 180 ? `${topic.content.slice(0, 180)}...` : topic.content}
                      </p>
                    ) : null}

                    <div>
                      <button
                        type="button"
                        style={buttonStyle}
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
            <p style={{ marginTop: '1rem', marginBottom: 0, color: '#b91c1c' }}>
              {getErrorMessage(reviewTopicMutation.error, 'Unable to mark the topic as reviewed.')}
            </p>
          ) : null}
        </section>
      </section>
    </main>
  );
}
