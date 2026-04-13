import { useContext, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import api from '../api/axiosConfig.js';
import AppNavigation from '../components/AppNavigation.jsx';
import AuthContext from '../context/AuthContext.jsx';

const SKILLS_QUERY_KEY = ['skills', 'my'];

const pageStyle = {
  minHeight: '100vh',
  padding: '2rem',
  backgroundColor: '#f5f5f5',
};

const layoutStyle = {
  maxWidth: '1120px',
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

const formGridStyle = {
  display: 'grid',
  gap: '1rem',
  gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
};

const fullWidthStyle = {
  gridColumn: '1 / -1',
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

const textareaStyle = {
  ...inputStyle,
  minHeight: '120px',
  resize: 'vertical',
};

const skillGridStyle = {
  display: 'grid',
  gap: '1rem',
  gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
};

const skillCardStyle = {
  border: '1px solid #e2e8f0',
  borderRadius: '14px',
  padding: '1rem',
  backgroundColor: '#f8fafc',
  display: 'grid',
  gap: '0.85rem',
  textAlign: 'left',
};

const metaRowStyle = {
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

const scoreBadgeStyle = {
  ...badgeStyle,
  backgroundColor: '#dbeafe',
  color: '#1d4ed8',
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

export default function Skills() {
  const queryClient = useQueryClient();
  const { user } = useContext(AuthContext);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: '',
    creditValue: 1,
    type: 'OFFERED',
    skillCategory: 'TECHNICAL',
  });

  const {
    data: skills = [],
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: SKILLS_QUERY_KEY,
    queryFn: async () => {
      const response = await api.get('/skills/my');
      return response.data ?? [];
    },
  });

  const createSkillMutation = useMutation({
    mutationFn: async (payload) => {
      const response = await api.post('/skills', payload);
      return response.data;
    },
    onSuccess: () => {
      setFormData({
        title: '',
        description: '',
        category: '',
        creditValue: 1,
        type: 'OFFERED',
        skillCategory: 'TECHNICAL',
      });
      queryClient.invalidateQueries({ queryKey: SKILLS_QUERY_KEY });
    },
  });

  const practiceSkillMutation = useMutation({
    mutationFn: async (skillId) => {
      const response = await api.put(`/skills/${skillId}/practice?hours=1.0`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SKILLS_QUERY_KEY });
    },
  });

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((currentFormData) => ({
      ...currentFormData,
      [name]: name === 'creditValue' ? value : value,
    }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    createSkillMutation.mutate({
      title: formData.title.trim(),
      description: formData.description.trim(),
      category: formData.category.trim(),
      creditValue: Number(formData.creditValue),
      type: formData.type,
      skillCategory: formData.skillCategory,
    });
  };

  return (
    <main style={pageStyle}>
      <section style={layoutStyle}>
        <div>
          <AppNavigation />

          <header style={panelStyle}>
            <p style={{ marginBottom: '0.5rem', color: '#475569' }}>
              Signed in as {user?.email ?? user?.sub ?? 'authenticated user'}
            </p>
            <h1 style={{ marginTop: 0, marginBottom: '0.75rem' }}>Skills Vault</h1>
            <p style={{ margin: 0, color: '#475569' }}>
              Add new skills, see your current progress, and log practice sessions directly against your profile.
            </p>
          </header>
        </div>

        <section style={panelStyle}>
          <h2 style={{ marginTop: 0 }}>Add Skill</h2>
          <form onSubmit={handleSubmit} style={formGridStyle}>
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
              <label htmlFor="category">Category</label>
              <input
                id="category"
                name="category"
                type="text"
                value={formData.category}
                onChange={handleChange}
                style={inputStyle}
                required
              />
            </div>

            <div>
              <label htmlFor="creditValue">Credit Value</label>
              <input
                id="creditValue"
                name="creditValue"
                type="number"
                min="1"
                step="1"
                value={formData.creditValue}
                onChange={handleChange}
                style={inputStyle}
                required
              />
            </div>

            <div>
              <label htmlFor="type">Type</label>
              <select
                id="type"
                name="type"
                value={formData.type}
                onChange={handleChange}
                style={inputStyle}
              >
                <option value="OFFERED">OFFERED</option>
                <option value="REQUESTED">REQUESTED</option>
              </select>
            </div>

            <div>
              <label htmlFor="skillCategory">Skill Category</label>
              <select
                id="skillCategory"
                name="skillCategory"
                value={formData.skillCategory}
                onChange={handleChange}
                style={inputStyle}
              >
                <option value="TECHNICAL">TECHNICAL</option>
                <option value="SOFT">SOFT</option>
              </select>
            </div>

            <div style={fullWidthStyle}>
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                style={textareaStyle}
                required
              />
            </div>

            {createSkillMutation.isError ? (
              <p style={{ ...fullWidthStyle, margin: 0, color: '#b91c1c' }}>
                {getErrorMessage(createSkillMutation.error, 'Unable to create skill right now.')}
              </p>
            ) : null}

            {createSkillMutation.isSuccess ? (
              <p style={{ ...fullWidthStyle, margin: 0, color: '#15803d' }}>Skill created.</p>
            ) : null}

            <div style={{ ...fullWidthStyle, display: 'flex', justifyContent: 'flex-start' }}>
              <button
                type="submit"
                style={buttonStyle}
                disabled={createSkillMutation.isPending}
              >
                {createSkillMutation.isPending ? 'Creating...' : 'Add Skill'}
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
              <h2 style={{ margin: 0 }}>My Skills</h2>
              <p style={{ margin: '0.35rem 0 0', color: '#475569' }}>
                Loaded from <code>/api/skills/my</code>.
              </p>
            </div>
            <button
              type="button"
              style={secondaryButtonStyle}
              onClick={() => queryClient.invalidateQueries({ queryKey: SKILLS_QUERY_KEY })}
              disabled={isLoading}
            >
              Refresh
            </button>
          </div>

          {isLoading ? <p style={{ margin: 0 }}>Loading skills...</p> : null}

          {isError ? (
            <p style={{ margin: 0, color: '#b91c1c' }}>
              {getErrorMessage(error, 'Unable to load skills.')}
            </p>
          ) : null}

          {!isLoading && !isError && skills.length === 0 ? (
            <div style={emptyStateStyle}>
              <h3 style={{ marginTop: 0 }}>No skills yet</h3>
              <p style={{ margin: 0, color: '#475569' }}>
                Create your first skill above and it will appear here.
              </p>
            </div>
          ) : null}

          {!isLoading && !isError && skills.length > 0 ? (
            <div style={skillGridStyle}>
              {skills.map((skill) => {
                const isPracticingCurrentSkill =
                  practiceSkillMutation.isPending && practiceSkillMutation.variables === skill.id;

                return (
                  <article key={skill.id} style={skillCardStyle}>
                    <div>
                      <p style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>
                        {skill.category || 'Uncategorized'}
                      </p>
                      <h3 style={{ margin: '0.35rem 0 0' }}>{skill.title}</h3>
                    </div>

                    <div style={metaRowStyle}>
                      <span style={badgeStyle}>{skill.type}</span>
                      <span style={badgeStyle}>{skill.skillCategory ?? 'UNKNOWN'}</span>
                      <span style={scoreBadgeStyle}>
                        Score: {typeof skill.skillScore === 'number' ? skill.skillScore.toFixed(1) : '0.0'}
                      </span>
                    </div>

                    <p style={{ margin: 0, color: '#475569' }}>{skill.description}</p>

                    <div>
                      <button
                        type="button"
                        style={buttonStyle}
                        onClick={() => practiceSkillMutation.mutate(skill.id)}
                        disabled={isPracticingCurrentSkill}
                      >
                        {isPracticingCurrentSkill ? 'Logging...' : 'Log Practice (1 Hour)'}
                      </button>
                    </div>
                  </article>
                );
              })}
            </div>
          ) : null}

          {practiceSkillMutation.isError ? (
            <p style={{ marginTop: '1rem', marginBottom: 0, color: '#b91c1c' }}>
              {getErrorMessage(practiceSkillMutation.error, 'Unable to log practice right now.')}
            </p>
          ) : null}
        </section>
      </section>
    </main>
  );
}
