# Skill Vault Project Description

## Product Perspective

Skill Vault is a full-stack, rule-driven skill economy platform that combines peer-to-peer learning, credit-based trading, skill growth tracking, and knowledge decay monitoring. The product is built as a web application with a React frontend and a Spring Boot backend connected to a PostgreSQL database.

The system is positioned as more than a simple CRUD application. It treats skills and knowledge as dynamic assets whose values change over time based on usage, review, practice, trade activity, and decay rules. Users can register, authenticate, add skills, browse other users' offered skills, request trade sessions, accept trades, complete trades, transfer credits, and maintain a personal knowledge vault.

The backend follows a layered architecture:

- Controller layer for REST APIs related to authentication, users, profiles, skills, trades, and knowledge topics.
- Service layer for business rules such as credit transfer, skill evaluation, trade validation, and knowledge decay.
- Repository layer using Spring Data JPA for persistence.
- Model layer using JPA entities for users, skills, trade sessions, knowledge topics, credit transactions, history records, and notifications.
- Security layer using Spring Security, JWT authentication, BCrypt password hashing, stateless sessions, and CORS configuration.

The frontend provides protected application pages for Dashboard, Skills, Knowledge, and Trades. It communicates with the backend through Axios, stores JWT tokens in browser local storage, and uses React Query to load and refresh server state.

The database schema is considered fixed and must not be changed. It is defined in `schema.md` and contains the following tables:

- `users`
- `skills`
- `skill_history`
- `knowledge_topics`
- `knowledge_revision_history`
- `trade_sessions`
- `credit_transactions`
- `notifications`

## Product Functions

Skill Vault currently supports the following major product functions:

### User Authentication and Profile Access

- Register a new user with name, email, password, and role.
- Hash user passwords before saving them.
- Log in using email and password.
- Generate JWT tokens after successful authentication.
- Protect all non-authentication APIs using JWT authentication.
- Load the current authenticated user profile.
- Display user identity and credit information in the frontend.

### Skill Management

- Allow authenticated users to create skills under their own account.
- Support offered and requested skill records.
- Support technical and soft skill categories through single-table inheritance.
- Store skill title, description, category, credit value, score, confidence index, usage frequency, active state, and owner.
- List the authenticated user's own skills.
- Display offered skills from other users in a marketplace.
- Log practice against a skill.
- Increase skill score based on skill category behavior.
- Update skill usage frequency and last-used date after practice.
- Apply manual confidence decay based on days since last use.

### Trade Marketplace and Credit Economy

- Display marketplace skills offered by users other than the current user.
- Allow a user to request a trade session for another user's offered skill.
- Validate that requester and provider are different users.
- Validate that the selected skill belongs to the provider.
- Validate that only offered skills can be traded.
- Create trade sessions with scheduled time, duration, skill, requester, provider, and initial pending status.
- Allow the provider to accept a pending trade.
- Allow the requester to complete an active trade.
- Require ratings from 1 to 5 when completing a trade.
- Calculate credit cost using skill credit value and session duration.
- Prevent completion when the requester does not have enough credits.
- Transfer credits from requester to provider after trade completion.
- Record credit ledger entries for earned and spent credits.
- Display trades grouped by status in the frontend.

### Knowledge Vault and Decay Tracking

- Allow authenticated users to create knowledge topics.
- Store topic title, subject, content, mastery level, decay rate, last reviewed date, last decay check, status, and owner.
- Show the current user's knowledge topics.
- Show topic summary metrics such as total topics, average mastery, subjects, and revision count.
- Filter topics by subject.
- Calculate current mastery using an exponential decay model.
- Mark topics as current, decaying, or needing revision based on mastery thresholds.
- Mark a topic as reviewed, restoring mastery to 100%.
- Optionally save revised topic content and create revision history.
- Automatically run a scheduled knowledge decay scan every five minutes.
- Display a revision queue for topics that need another review pass.

### Dashboard and Navigation

- Provide a protected dashboard after login.
- Present the main product areas: Skills, Knowledge, and Trades.
- Show the authenticated user's credit status.
- Redirect unauthenticated users to login.
- Remove invalid or expired tokens and force re-authentication.

## User Classes and Characteristics

### Student

Students primarily use Skill Vault to learn from other users. They browse the marketplace, request sessions, spend credits, complete trades, rate providers, and track knowledge topics that require revision.

Characteristics:

- Needs a simple way to discover offered skills.
- Requires clear credit balance visibility before requesting sessions.
- Benefits from revision reminders and mastery tracking.
- May also offer skills despite having a student role, because roles describe profile specialization rather than strict permissions.

### Instructor

Instructors primarily offer skills to other users. They create offered skills, accept trade requests, teach sessions, and earn credits after completed trades.

Characteristics:

- Needs skill cards with clear title, category, description, and credit value.
- Requires visibility into pending and active trade requests.
- Benefits from reputation, rating, and trade completion tracking.

### Mentor

Mentors act as experienced users who may both teach and guide other users. They can offer technical or soft skills and participate in credit-based exchanges.

Characteristics:

- Often participates in both teaching and learning workflows.
- Needs reliable trade status management.
- Benefits from knowledge and skill progress visibility.

### Professional

Professionals represent users who may offer industry-oriented skills and consume learning sessions from others.

Characteristics:

- Needs a marketplace that supports practical skill discovery.
- Requires secure account access and protected profile data.
- Benefits from clear credit rules and auditability through credit transactions.

### Admin

Admins are included in the user role model for platform management and future governance. The current implementation uses authentication-based access control for protected APIs, while role-specific administrative operations can be extended later.

Characteristics:

- May require user, skill, trade, notification, and credit oversight in future versions.
- Needs schema consistency and reliable audit records.
- Should not require changes to the fixed database schema for ordinary reporting and management extensions.

## Design and Implementation Constraints

- The database schema defined in `schema.md` is fixed and must not be changed.
- PostgreSQL is the primary production database target.
- The schema uses UUID primary keys and foreign key relationships across users, skills, knowledge topics, trades, credit transactions, history tables, and notifications.
- The backend is implemented using Spring Boot, Spring Web, Spring Data JPA, Spring Security, JWT, Lombok, and Maven.
- The frontend is implemented using React, Vite, React Router, React Query, Axios, and JWT decoding.
- Authentication must remain stateless using bearer tokens.
- Passwords must be stored only as hashes, not plain text.
- All protected APIs require a valid JWT.
- The frontend expects the backend API base URL to be available at `http://localhost:8080/api` during local development.
- CORS is configured for local Vite development origins.
- Skill modeling uses object-oriented inheritance with an abstract `Skill` type and concrete `TechnicalSkill` and `SoftSkill` implementations.
- Knowledge decay uses time-based calculations, so correct system date and time are important.
- Scheduled decay processing must run safely without user interaction.
- Credit transfers must be handled consistently so requester and provider balances remain synchronized with credit transaction records.
- Trade status transitions must be controlled by business rules instead of arbitrary client-side updates.
- The application should preserve the separation between DTOs, services, repositories, entities, and controllers.
- The report and project documentation should describe the fixed schema as the authoritative database design.

## Assumptions and Dependencies

- Users have access to a modern browser capable of running the React frontend.
- The backend server, frontend dev server, and PostgreSQL database are available during local development.
- The PostgreSQL database contains or can create the required schema objects described in `schema.md`.
- JWT tokens are trusted for identifying the authenticated user after login.
- Email is treated as a unique login identity.
- Each user begins with an initial credit balance on registration.
- A user may act as both learner and provider depending on the trade.
- Offered skills are visible in the marketplace to other users, while the current user's own offered skills are excluded from their marketplace list.
- Trade completion is performed by the requester after a provider accepts the trade.
- Credit transfer occurs only when a trade is completed successfully.
- Skill score and confidence values are calculated by application rules and should stay within meaningful business ranges.
- Knowledge mastery is represented as a percentage-like value where higher values indicate stronger retention.
- Knowledge decay depends on elapsed days since review or decay checkpoint.
- The scheduled decay engine depends on Spring scheduling being enabled in the backend application.
- Frontend state depends on React Query cache invalidation after mutations such as creating skills, requesting trades, accepting trades, completing trades, and reviewing topics.
- The current implementation is designed for academic/project demonstration use and can be expanded for production-grade monitoring, role-specific authorization, notifications, and audit dashboards.

## Performance Requirements

- The application should respond to normal user actions such as login, skill loading, trade loading, and topic loading within an acceptable interactive time for a web application.
- Dashboard, Skills, Knowledge, and Trades pages should load data asynchronously without blocking the entire interface.
- React Query should be used to cache server data and refresh only the affected queries after mutations.
- Marketplace skill retrieval should exclude the current user's own skills to reduce unnecessary client-side filtering.
- User-specific queries should return only the authenticated user's relevant records where applicable.
- Credit transfer and trade completion should execute in a single logical operation so balances and trade status do not become inconsistent.
- Knowledge decay scans should run in the background without requiring manual user action.
- The scheduled decay scan should be efficient enough to process existing knowledge topics periodically without noticeably slowing normal API requests.
- The system should avoid unnecessary database writes when no meaningful state change is required.
- API responses should use DTOs where appropriate to reduce overexposure of internal entity structure.
- The frontend should remain usable on standard development machines and browsers.

## Safety Requirements

- The system must prevent users from creating trade sessions with themselves as both requester and provider.
- The system must prevent users from trading a skill that does not belong to the selected provider.
- The system must prevent non-offered skills from being traded in the marketplace.
- The system must reject trade requests with missing provider, skill, scheduled time, or invalid duration.
- The system must reject trade completion unless the trade is active.
- The system must reject trade acceptance unless the trade is pending.
- Only the provider should be allowed to accept a trade.
- Only the requester should be allowed to complete a trade.
- Ratings must be restricted to the valid range of 1 to 5.
- Credit transfer must not occur if the requester has insufficient credits.
- Knowledge mastery should not fall below the minimum allowed value after decay.
- Review operations should reset knowledge topics to a safe current state.
- Expired or invalid JWT tokens should be removed from the frontend and users should be redirected to login.
- The system should avoid exposing protected application pages to unauthenticated users.
- Database foreign keys should preserve referential integrity between users, skills, trades, knowledge topics, transactions, history records, and notifications.

## Security Requirements

- User passwords must be encrypted using BCrypt before being saved.
- Authentication must be performed through Spring Security.
- JWT tokens must be required for all protected API endpoints.
- The backend must operate using stateless sessions.
- Unauthenticated API requests must return unauthorized responses instead of protected data.
- The frontend must attach the JWT token as a bearer token on authenticated API requests.
- The frontend must clear stored tokens when the backend returns an unauthorized response.
- CORS must be restricted to approved frontend origins during local development.
- Swagger/OpenAPI endpoints may remain public for development documentation, while business APIs remain protected.
- User identity should be derived from the authenticated principal, not from untrusted client-submitted user IDs, wherever the operation is account-specific.
- Trade operations must verify the authenticated user's role in the trade before allowing status changes.
- Password hashes, JWT secrets, and database passwords should not be committed as plain production credentials.
- Credit-changing operations should be performed only through validated service-layer methods.
- Future admin features should use role-based authorization before allowing platform-wide changes.

## Software Quality Attributes

### Maintainability

The project uses a layered backend structure that separates controllers, services, repositories, models, DTOs, security, and enums. This makes the code easier to extend and helps keep business rules out of controllers.

### Modularity

Major product areas are separated into authentication, users/profiles, skills, trades, credits, and knowledge. The frontend mirrors these areas with separate pages and reusable components.

### Extensibility

The abstract skill model supports technical and soft skill behavior through polymorphism. The fixed schema also includes history and notification tables, allowing future reporting and alert features without redesigning the core product.

### Reliability

Trade status transitions, credit transfer validation, authentication checks, and knowledge decay rules reduce the chance of invalid states. Credit transaction records provide a ledger-style trail for credit movement.

### Usability

The frontend provides clear navigation, protected routes, loading states, empty states, error feedback, credit display, marketplace cards, grouped trades, topic summaries, and revision indicators.

### Testability

The service-layer design makes key rules testable, including skill evaluation, knowledge decay, trade validation, credit transfer, authentication behavior, and profile aggregation.

### Scalability

The architecture can be scaled by adding indexes, pagination, caching, background workers, and role-specific endpoints. The current schema supports one-to-many relationships for skills, topics, trades, credit transactions, notifications, and history records.

### Portability

The backend can run through Maven and the frontend can run through npm/Vite. PostgreSQL is the intended database, while H2 is available for tests.

### Auditability

The schema includes `skill_history`, `knowledge_revision_history`, `credit_transactions`, and `notifications`, which support tracking important state changes and user-facing events.

## Business Rules

- A user must register and log in before accessing protected application features.
- Email addresses must be unique.
- Passwords must be stored as hashes.
- New users receive an initial credit balance.
- User roles describe profile specialization and do not prevent a user from learning or teaching.
- A skill belongs to exactly one user.
- A skill can be technical or soft.
- A skill can be offered or requested.
- Only offered skills should appear in the trade marketplace.
- A user's own offered skills should not appear in that user's marketplace list.
- Practice on a skill increases skill score and updates usage metadata.
- Technical skills and soft skills can evolve using different evaluation formulas.
- Skill confidence can decay based on time since last use.
- A trade must involve two different users.
- A trade must reference a valid skill owned by the provider.
- A trade begins in a pending/requested state.
- Only the provider may accept a pending trade.
- Accepting a trade moves it to an active/accepted state.
- Only the requester may complete an active trade.
- Completing a trade requires a valid rating from 1 to 5.
- Completing a trade transfers credits from requester to provider.
- Credit cost is calculated using skill credit value and session duration.
- A requester cannot complete a trade if they do not have enough credits.
- Credit debit and credit earning records must be created when credits are transferred.
- Knowledge topics belong to a specific user.
- New knowledge topics start with high mastery and current status.
- Knowledge mastery decays over time using an exponential decay model.
- Topics below the revision threshold must be marked as needing revision.
- Topics in the middle mastery range may be marked as decaying.
- Reviewing a topic restores mastery and updates last reviewed information.
- Updating topic content during review should create a revision history entry.
- Scheduled decay processing must periodically update knowledge states.
- Protected frontend routes must redirect unauthenticated users to login.
