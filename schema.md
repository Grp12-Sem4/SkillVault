1️⃣ Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

2️⃣ ENUM DEFINITIONS

User Roles
Roles describe profile specialization, not permissions.
 Any role can teach or learn.
CREATE TYPE user_role_enum AS ENUM (
   'STUDENT',
   'INSTRUCTOR',
   'MENTOR',
   'PROFESSIONAL',
   'ADMIN'
);

Account Status
CREATE TYPE account_status_enum AS ENUM (
   'ACTIVE',
   'SUSPENDED',
   'DEACTIVATED',
   'PENDING_VERIFICATION'
);

Skill Type
CREATE TYPE skill_type_enum AS ENUM (
   'TECHNICAL',
   'SOFT'
);

Trade Session Status
CREATE TYPE trade_status_enum AS ENUM (
   'REQUESTED',
   'ACCEPTED',
   'COMPLETED',
   'CANCELLED'
);

Knowledge Status
CREATE TYPE knowledge_status_enum AS ENUM (
   'CURRENT',
   'NEEDS_REVISION'
);

Credit Transaction Type
CREATE TYPE credit_transaction_enum AS ENUM (
   'EARN_TEACHING',
   'SPEND_SESSION',
   'BONUS_RATING',
   'ADMIN_ADJUSTMENT'
);

Notification Type
CREATE TYPE notification_type_enum AS ENUM (
   'SKILL_DECAY_ALERT',
   'TRADE_REQUEST',
   'TRADE_ACCEPTED',
   'TRADE_COMPLETED',
   'REVISION_REMINDER'
);

3️⃣ USERS TABLE
Stores all system users.
CREATE TABLE users (

   user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   username VARCHAR(50) UNIQUE NOT NULL,
   email VARCHAR(100) UNIQUE NOT NULL,

   password_hash VARCHAR(255) NOT NULL,

   role user_role_enum NOT NULL DEFAULT 'STUDENT',

   account_status account_status_enum NOT NULL DEFAULT 'ACTIVE',

   credit_balance DECIMAL(10,2) DEFAULT 100.00,

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   last_active TIMESTAMP
);

4️⃣ SKILLS TABLE
Implements single-table inheritance for skill types.
CREATE TABLE skills (

   skill_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   user_id UUID NOT NULL,

   skill_name VARCHAR(100) NOT NULL,

   skill_type skill_type_enum NOT NULL,

   skill_score DECIMAL(4,2) DEFAULT 1.00
       CHECK (skill_score >= 1 AND skill_score <= 10),

   confidence_index DECIMAL(4,2) DEFAULT 1.00
       CHECK (confidence_index >= 1 AND confidence_index <= 10),

   last_used DATE,

   practice_hours DECIMAL(6,2),

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_skill_owner
       FOREIGN KEY (user_id)
       REFERENCES users(user_id)
       ON DELETE CASCADE
);
Notes:
practice_hours → only used for TechnicalSkill
SoftSkill rows will keep it NULL

5️⃣ SKILL HISTORY
Tracks every change to skill metrics.
CREATE TABLE skill_history (

   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   skill_id UUID NOT NULL,

   previous_score DECIMAL(4,2),
   new_score DECIMAL(4,2),

   previous_confidence DECIMAL(4,2),
   new_confidence DECIMAL(4,2),

   event_reason VARCHAR(100),

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_skill_history
       FOREIGN KEY (skill_id)
       REFERENCES skills(skill_id)
       ON DELETE CASCADE
);
Example reasons:
PRACTICE
TRADE_RATING
DECAY
MANUAL_UPDATE

6️⃣ KNOWLEDGE TOPICS
Tracks theoretical knowledge and forgetting curve.
CREATE TABLE knowledge_topics (

   topic_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   user_id UUID NOT NULL,

   title VARCHAR(200) NOT NULL,

   subject VARCHAR(100),

   mastery_level DECIMAL(4,2)
       CHECK (mastery_level >= 0 AND mastery_level <= 10),

   decay_rate DECIMAL(5,4) NOT NULL,

   last_reviewed DATE,

   status knowledge_status_enum DEFAULT 'CURRENT',

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_topic_owner
       FOREIGN KEY (user_id)
       REFERENCES users(user_id)
       ON DELETE CASCADE
);

7️⃣ KNOWLEDGE REVISION HISTORY
Tracks learning revisions.
CREATE TABLE knowledge_revision_history (

   revision_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   topic_id UUID NOT NULL,

   previous_mastery DECIMAL(4,2),
   new_mastery DECIMAL(4,2),

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_revision_topic
       FOREIGN KEY (topic_id)
       REFERENCES knowledge_topics(topic_id)
       ON DELETE CASCADE
);

8️⃣ TRADE SESSIONS
Core barter system table.
CREATE TABLE trade_sessions (

   session_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   teacher_id UUID NOT NULL,

   student_id UUID NOT NULL,

   skill_id UUID NOT NULL,

   duration_hours DECIMAL(4,2),

   credits_transferred DECIMAL(10,2),

   rating INT CHECK (rating BETWEEN 1 AND 5),

   status trade_status_enum DEFAULT 'REQUESTED',

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   completed_at TIMESTAMP,

   CONSTRAINT fk_teacher
       FOREIGN KEY (teacher_id)
       REFERENCES users(user_id),

   CONSTRAINT fk_student
       FOREIGN KEY (student_id)
       REFERENCES users(user_id),

   CONSTRAINT fk_trade_skill
       FOREIGN KEY (skill_id)
       REFERENCES skills(skill_id)
);

9️⃣ CREDIT TRANSACTIONS
Ledger system for credit economy.
CREATE TABLE credit_transactions (

   transaction_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   user_id UUID NOT NULL,

   transaction_type credit_transaction_enum NOT NULL,

   amount DECIMAL(10,2) NOT NULL,

   reference_session UUID,

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_credit_user
       FOREIGN KEY (user_id)
       REFERENCES users(user_id)
       ON DELETE CASCADE,

   CONSTRAINT fk_reference_session
       FOREIGN KEY (reference_session)
       REFERENCES trade_sessions(session_id)
);

🔟 NOTIFICATIONS
Used for alerts and real-time updates.
CREATE TABLE notifications (

   notification_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

   user_id UUID NOT NULL,

   type notification_type_enum NOT NULL,

   message TEXT,

   is_read BOOLEAN DEFAULT FALSE,

   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_notification_user
       FOREIGN KEY (user_id)
       REFERENCES users(user_id)
       ON DELETE CASCADE
);

FINAL TABLE LIST
Your clean final schema contains 8 tables
users
skills
skill_history
knowledge_topics
knowledge_revision_history
trade_sessions
credit_transactions
notifications

FINAL RELATIONSHIP STRUCTURE
users
  │
  ├── skills
  │      └── skill_history
  │
  ├── knowledge_topics
  │      └── knowledge_revision_history
  │
  ├── trade_sessions (teacher)
  ├── trade_sessions (student)
  │
  ├── credit_transactions
  │
  └── notifications
Cardinality summary:
Relationship
Type
User → Skills
1:N
Skill → SkillHistory
1:N
User → KnowledgeTopics
1:N
KnowledgeTopic → RevisionHistory
1:N
User → TradeSessions
1:N
Skill → TradeSessions
1:N
User → CreditTransactions
1:N
User → Notifications
1:N




