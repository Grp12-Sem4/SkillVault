# Time Capsule Functionality for Skill Vault

## What is the Time Capsule?
The Time Capsule is a feature designed to preserve, snapshot, and later revisit a user's skills, knowledge, and achievements at a specific point in time. It acts as a digital archive, allowing users to capture their current state (skills, knowledge, credits, trades, etc.) and reflect on their growth, decay, and evolution over time.

---

## What is to be Achieved?
- **Snapshot**: Allow users to create a "time capsule" snapshot of their profile, including:
  - All skills (with scores, confidence, usage, decay state)
  - Knowledge topics (with mastery, decay, revision status)
  - Credit balance
  - Trade history (recent trades, ratings)
  - Achievements or milestones
- **Preservation**: Store these snapshots securely, timestamped and immutable.
- **Reflection**: Enable users to view and compare past time capsules with their current state, visualizing growth, decay, and learning patterns.
- **Motivation**: Use time capsules to motivate users by showing progress, highlighting decayed skills, and suggesting revision or practice.

---

## Core Functionality
1. **Create Time Capsule**
   - User triggers a snapshot (manual or scheduled, e.g., monthly)
   - System gathers:
     - All current skills (with all metrics)
     - All knowledge topics (with mastery, decay, revision status)
     - Credit balance
     - Recent trades (last N trades, ratings)
     - Achievements
   - Store as a new TimeCapsule entity (with timestamp, user reference, and all data)

2. **View Time Capsules**
   - List all past time capsules for the user
   - View details of each snapshot (all metrics as above)
   - Compare two or more time capsules (diff view: skill growth, knowledge decay, credit changes, etc.)

3. **Analyze & Visualize**
   - Graphs showing skill/knowledge evolution over time
   - Highlight decayed or improved skills/topics
   - Suggest actions (revise, practice, teach, etc.)

---

## Data Model (Example)
- **TimeCapsule**
  - id (UUID)
  - user (User reference)
  - createdAt (timestamp)
  - skillsSnapshot (list of SkillSnapshot)
  - knowledgeSnapshot (list of KnowledgeTopicSnapshot)
  - creditBalance (decimal)
  - tradesSnapshot (list of TradeSnapshot)
  - achievements (list)

- **SkillSnapshot**
  - skillId
  - name
  - type
  - score
  - confidence
  - usageFrequency
  - lastUsed
  - decayState

- **KnowledgeTopicSnapshot**
  - topicId
  - title
  - subject
  - masteryLevel
  - decayRate
  - lastReviewed
  - status

- **TradeSnapshot**
  - tradeId
  - skill
  - provider
  - requester
  - duration
  - rating
  - status
  - timestamp

---

## Implementation Steps
1. **Backend**
   - Add TimeCapsule entity and related snapshot entities
   - Implement service to create, store, and retrieve time capsules
   - Add API endpoints:
     - POST /api/time-capsule (create snapshot)
     - GET /api/time-capsule (list all)
     - GET /api/time-capsule/{id} (view details)
     - GET /api/time-capsule/compare?id1=&id2= (compare)
2. **Frontend**
   - UI to trigger snapshot creation
   - List and view time capsules
   - Visual comparison (charts, tables)
   - Progress/motivation features

---

## Why is this Valuable?
- **Self-reflection**: Users see their learning journey, not just static scores
- **Motivation**: Encourages regular practice and revision
- **Accountability**: Users can track their own progress and decay
- **Gamification**: Achievements and milestones can be tied to time capsules

---

## Advanced Ideas
- **Automated Snapshots**: Monthly/quarterly auto-capture
- **Shareable Capsules**: Allow users to share progress with mentors/peers
- **AI Insights**: Suggest personalized actions based on time capsule analysis

---

## Summary
The Time Capsule is a digital archive and reflection tool, capturing the dynamic state of a user's skills, knowledge, and achievements, enabling deep self-reflection, motivation, and data-driven learning journeys.
