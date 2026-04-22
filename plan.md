# Profile Tab Plan for Skill Vault

## Goal

Design a backend-facing plan for a user profile tab without changing the database schema and without modifying any existing source code right now. This plan is based on:

- the current backend implementation in `src/main/java/com/skillvault/skillvault_backend`
- the feature direction written in `description.md`
- the existing database schema in `schema.md`

The purpose of this plan is to decide what profile information should be fetched and displayed so the profile tab feels complete, useful, and realistic for the current Skill Vault project.

## Important Constraint

We are **not** changing the database schema.

That means the profile tab plan should use:

- fields already present in current entities
- fields already present in the schema even if the current entity classes do not expose them yet
- derived values that can be calculated from existing tables

It should **not** depend on adding new tables or changing column structure.

## What Exists Today in the Project

After reviewing the current project deeply, these major data areas already exist:

- `users`
- `skills`
- `knowledge_topics`
- `trade_sessions`
- `credit_transactions`
- `notifications`
- `skill_history`
- `knowledge_revision_history`

The current backend already has working support for:

- authentication
- current user fetch through `/api/users/me`
- user skills
- marketplace skills
- trade creation, acceptance, completion
- knowledge topic creation, review, decay, and revision-needed listing

The current code already stores or exposes these user-related fields directly:

- user id
- name
- email
- role
- credit balance
- account status
- reputation score
- trades completed
- no-show count

The schema also includes profile-relevant fields that are not fully reflected in current entity classes yet:

- `created_at`
- `last_active`
- skill `practice_hours`
- skill and knowledge history metrics
- notification records

## Profile Tab Philosophy

The profile tab should not be just a basic identity card. In this project, the profile should represent the user as:

- a platform member
- a learner
- a teacher/provider
- a skill owner
- a knowledge tracker
- a trade participant
- a credit economy participant

So the profile should combine:

- identity details
- trust and status indicators
- skills summary
- learning and knowledge summary
- trading summary
- credit summary
- recent activity indicators

## Recommended Profile Sections

## 1. Basic Identity Section

This is the top part of the profile.

### Show

- User name
- Email
- Role
- Account status
- User ID

### Why it matters

- This is the minimum identity block
- It helps the user confirm which account is active
- Role is meaningful in this system because it describes profile specialization
- Account status is important for platform trust and account health

### Source

- `User`
- current `/api/users/me` already returns a subset of this

### Already available right now

- name
- email
- role
- id

### Available in entity but not exposed in `/me` yet

- account status

## 2. Profile Health and Trust Section

This section should show whether the user is a reliable participant in the ecosystem.

### Show

- Reputation score
- Trades completed
- No-show count
- Reliability badge or health label derived from these values

### Why it matters

- This platform is based on peer-to-peer exchange
- Users need trust indicators
- These values help future trade decisions

### Source

- `User`

### Already present in current entity

- `reputationScore`
- `tradesCompleted`
- `noShowCount`

### Suggested derived values

- Reliability status:
  - Excellent
  - Good
  - Needs attention
- Completion ratio if future cancelled/no-show stats are tracked from trades

## 3. Credits and Economy Section

This section should explain the user’s current financial state inside the skill economy.

### Show

- Current credit balance
- Total credits earned
- Total credits spent
- Net credit movement
- Number of credit transactions
- Last credit activity date

### Why it matters

- Credits are a core mechanic of the platform
- Users should understand their earning/spending history
- This helps them decide whether to teach or learn next

### Source

- `User.creditBalance`
- `credit_transactions`

### Already available right now

- current credit balance

### Can be fetched from existing schema without schema changes

- total earned credits
- total spent credits
- last transaction timestamp
- transaction count

### Future display idea

- A compact ledger preview with latest 5 transactions

## 4. Skills Summary Section

This should be one of the most important blocks in the profile.

### Show

- Total skills
- Offered skills count
- Requested skills count
- Technical skills count
- Soft skills count
- Active skills count
- Decaying or low-confidence skills count
- Average skill score
- Average confidence index

### Why it matters

- Skills are the core asset in the platform
- A user profile should immediately communicate what the user can offer and what the user wants to learn

### Source

- `skills`

### Already available from current code

- all user skills through `/api/skills/my`
- skill title
- description
- category
- type
- credit value
- active flag
- skill score
- confidence index
- usage frequency
- last used
- skill category through subtype

### Can be derived from current data

- total number of skills
- offered/requested split
- technical/soft split
- average score
- average confidence
- stale skill count
- most practiced / most used skill

### Skill cards list inside profile

Each skill shown in the profile can display:

- title
- description
- category
- offered or requested type
- technical or soft classification
- credit value
- skill score
- confidence index
- usage frequency
- last used date
- active status

### Decay-focused indicators

Because this project emphasizes evolution and decay, include:

- skills at risk of decay
- lowest-confidence skills
- never-practiced or rarely-used skills

## 5. Skill Performance and Growth Section

This section is different from raw skill listing. It explains progress.

### Show

- Top 3 strongest skills by score
- Top 3 most confident skills
- Top 3 most used skills
- Skills needing attention
- Skill growth trend placeholder

### Why it matters

- This creates a profile that feels dynamic rather than static
- It matches the project’s evolution-and-decay theme

### Source

- `skills`
- `skill_history`

### Already possible now

- ranking by score
- ranking by confidence
- ranking by usage frequency

### Possible once backend reads `skill_history`

- recent score changes
- recent confidence changes
- reason for changes such as practice, rating, decay, manual update
- mini timeline of skill evolution

## 6. Knowledge Summary Section

This should represent the user’s theoretical learning side.

### Show

- Total knowledge topics
- Distinct subjects count
- Average mastery level
- Topics needing revision count
- Current topics count
- Decaying topics count if mapped in backend
- Last reviewed topic date

### Why it matters

- Skill Vault tracks both practical skills and theoretical knowledge
- The profile should reflect both dimensions

### Source

- `knowledge_topics`

### Already available from current backend

- all user knowledge topics through `/api/knowledge`
- needs-revision topics through `/api/knowledge/needs-revision`
- title
- subject
- content
- status
- mastery level
- decay rate
- last reviewed
- last decay check
- created at

### Can be derived easily

- total topics
- average mastery
- revision-needed count
- subject diversity
- strongest topic
- weakest topic

### Topic cards inside profile

Each knowledge item preview can show:

- title
- subject
- mastery level
- revision status
- last reviewed
- decay risk

## 7. Knowledge Revision and Learning Consistency Section

This section shows how disciplined the user is about maintaining knowledge.

### Show

- Topics needing immediate revision
- Most recently reviewed topics
- Oldest unreviewed or longest-unreviewed topics
- Revision activity count
- Knowledge health badge

### Why it matters

- The forgetting-curve model is a signature feature of the project
- Users should quickly see where their attention is needed

### Source

- `knowledge_topics`
- `knowledge_revision_history`

### Already possible now

- identify revision-needed topics
- identify latest reviewed dates from topic rows

### Possible once history is used

- total revision events
- recent revision timeline
- mastery recovery trend

## 8. Trade Summary Section

This should show the user’s marketplace participation.

### Show

- Total trade sessions
- Pending trades count
- Active trades count
- Completed trades count
- Cancelled trades count when supported
- Trades as requester/student
- Trades as provider/teacher
- Average rating received as provider
- Average rating given as requester
- Total teaching hours
- Total learning hours

### Why it matters

- Trades are one of the main pillars of the platform
- A strong profile needs to show both teaching and learning activity

### Source

- `trade_sessions`

### Already available from current backend

- user trades through `/api/trades`
- scheduled time
- duration
- status
- rating
- linked skill
- requester summary
- provider summary

### Can be derived from current data

- trade counts by status
- provider vs requester counts
- completed session count
- average session duration
- latest trade
- next upcoming trade

### Important note

The schema and spec mention richer trade fields like:

- created time
- completed time
- credits transferred

Those should also be part of the eventual profile summary if the backend maps them, but no schema change is needed.

## 9. Teaching Profile Section

This section focuses specifically on the user as a provider/teacher.

### Show

- Number of offered skills
- Number of completed teaching sessions
- Average learner rating received
- Credits earned from teaching
- Most booked skill
- Total teaching hours

### Why it matters

- Many users will want to know how strong their provider profile looks
- This is useful for self-assessment and future public profile display

### Source

- `skills`
- `trade_sessions`
- `credit_transactions`

### Current status

All of these are possible from the schema and mostly derivable from current domain objects.

## 10. Learning Profile Section

This section focuses on the user as a learner/requester.

### Show

- Number of requested skills
- Number of sessions booked
- Credits spent on learning
- Topics or skill areas currently being pursued
- Learning activity trend

### Why it matters

- The system supports both giving and receiving value
- The user profile should not only glorify teaching; it should also reflect learning behavior

### Source

- `skills` with requested type
- `trade_sessions`
- `credit_transactions`
- `knowledge_topics`

## 11. Notifications and Alerts Section

This should be a compact profile-side panel or tab subsection.

### Show

- Unread notifications count
- Latest notifications
- Decay alerts
- Trade-related alerts
- Revision reminders

### Why it matters

- The schema explicitly includes notifications
- Alerts are central to the project’s dynamic behavior

### Source

- `notifications`

### Already possible from schema without changes

- recent notifications list
- unread count
- grouped counts by notification type

## 12. Recent Activity Section

This gives the profile a live, active feel.

### Show

- Most recent skill added
- Most recent practice update
- Most recent trade request
- Most recent trade completion
- Most recent knowledge topic created
- Most recent knowledge review
- Most recent credit transaction

### Why it matters

- Users should feel that the profile reflects current activity
- This is especially helpful on a dashboard-like profile page

### Source

- `skills`
- `skill_history`
- `trade_sessions`
- `knowledge_topics`
- `knowledge_revision_history`
- `credit_transactions`

## 13. Account Timeline Section

This is a small but useful metadata section.

### Show

- Account created at
- Last active at
- Days on platform

### Why it matters

- These give context to all other metrics
- They help explain whether a profile is new, established, active, or inactive

### Source

- `users.created_at`
- `users.last_active`

### Important note

These exist in the schema and specification, but the current `User` entity does not yet map them. Still, they should absolutely be part of the profile plan because the schema already supports them.

## Best Final Profile Tab Structure

To keep the profile tab useful and not overloaded, the best display structure would be:

1. Header block
2. Trust and credits snapshot
3. Skills snapshot
4. Knowledge snapshot
5. Trade snapshot
6. Recent activity
7. Notifications and alerts

## Recommended Header Block

Show:

- name
- email
- role
- account status
- credit balance
- reputation score

This gives instant visibility into the user’s identity and platform standing.

## Recommended Stats Row

Show:

- total skills
- total knowledge topics
- completed trades
- credits balance
- topics needing revision
- unread notifications

These six numbers would make a very strong profile summary row.

## Recommended Tabs or Subsections Inside Profile

### Option A: Single-page profile

Sections:

- Overview
- Skills
- Knowledge
- Trades
- Activity

### Option B: Profile with inner tabs

Tabs:

- Overview
- Skills
- Knowledge
- Trades
- Credits
- Alerts

For this project, **Option B** is better if the profile page becomes large.

## Best Data to Fetch for the Profile API

If a future backend profile endpoint is created, the profile response should ideally contain these grouped payloads:

### A. User core

- id
- name
- email
- role
- accountStatus
- creditBalance
- reputationScore
- tradesCompleted
- noShowCount
- createdAt
- lastActive

### B. Skill summary

- totalSkills
- offeredSkills
- requestedSkills
- technicalSkills
- softSkills
- activeSkills
- lowConfidenceSkills
- averageSkillScore
- averageConfidenceIndex
- topSkills

### C. Knowledge summary

- totalTopics
- averageMastery
- needsRevisionCount
- distinctSubjects
- latestReviewedDate
- weakestTopics

### D. Trade summary

- totalTrades
- pendingTrades
- activeTrades
- completedTrades
- cancelledTrades
- asProviderCount
- asRequesterCount
- averageRatingReceived
- averageRatingGiven
- totalTeachingHours
- totalLearningHours

### E. Credit summary

- currentBalance
- totalEarned
- totalSpent
- netCredits
- lastTransactionAt
- recentTransactions

### F. Alerts summary

- unreadCount
- decayAlertsCount
- revisionReminderCount
- tradeAlertsCount
- recentNotifications

### G. Recent activity

- recentSkills
- recentTrades
- recentKnowledgeUpdates
- recentCreditEvents

## Priority Order for Implementation Later

Since some parts already exist and some are only schema-backed, this is the best implementation order for the backend profile layer later:

### Phase 1: Must-have profile details

- basic identity
- account status
- credit balance
- reputation score
- trades completed
- no-show count
- total skills
- total knowledge topics
- completed trades
- needs revision count

### Phase 2: Strong overview metrics

- offered/requested skill split
- average skill score
- average confidence
- average mastery
- trade counts by status
- provider/requester split
- unread notifications count

### Phase 3: Rich profile intelligence

- top skills
- weakest skills
- average rating received
- total earned/spent credits
- recent activity feed
- skill history summary
- knowledge revision history summary

## What Can Be Displayed Immediately Based on Current Project

Without changing schema, and based on current project direction, the profile tab can safely be planned to display:

- name
- email
- role
- user id
- credit balance
- account status
- reputation score
- trades completed
- no-show count
- all owned skills
- skill counts and skill averages
- all knowledge topics
- revision-needed topics count
- all user trades
- trade counts by status
- offered vs requested skills
- technical vs soft skill split
- recent skills
- recent knowledge topics
- recent trades

## What Should Be Included Because the Schema Already Supports It

These should also be included in the plan even if current entity/API code is not fully exposing them yet:

- account created date
- last active date
- practice hours per technical skill
- skill history timeline
- knowledge revision history
- credit transaction totals and recent ledger
- notifications summary and unread count
- credits transferred per trade
- trade completion timestamps

## Final Recommendation

The user profile tab should be designed as a **full progress and trust dashboard**, not just a static personal-info page.

The strongest version of the profile for this project should combine:

- identity
- credibility
- skill portfolio
- knowledge health
- trade participation
- credit economy activity
- alerts
- recent activity

This fits the actual architecture of Skill Vault and matches the project vision of a rule-driven, evolving, decaying skill economy.

## Suggested Outcome for Backend Planning

When the backend profile layer is implemented later, it should aim for one consolidated profile response that includes:

- user core details
- aggregated counts
- summary metrics
- a few recent items from each domain

That approach will make the frontend profile tab much easier to build and will avoid many separate requests.
