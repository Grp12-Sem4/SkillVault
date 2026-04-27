# Time Capsule Backend Implementation Plan

## Goal

Implement the Time Capsule backend only. Do not touch frontend code.

The feature will let an authenticated user:
- Store a snapshot of their current SkillVault state.
- Fetch all of their stored snapshots so the frontend can render the list.
- Select any two snapshots from that list and compare them.

The controller layer should accept only request-level data such as snapshot name and snapshot IDs. It should not ask the frontend/controller to provide skill, knowledge, credit, trade, or rating values. The service layer must fetch the current user data from the database through repositories and then build the snapshot.

## Existing Project Rules To Follow

- Existing users use `UUID` IDs in `User.java`.
- Existing skills use `UUID` IDs in `Skill.java`.
- Existing knowledge topics use `UUID` IDs in `KnowledgeTopic.java`.
- Snapshot tables from `schema2.sql` can use generated `Long` IDs for snapshot records.
- Snapshot entities must reference the authenticated `User` entity instead of accepting raw user data from the frontend.
- No frontend files should be changed.
- Existing controllers, services, and repositories should not be changed unless absolutely required. Prefer adding new Time Capsule files.

## Model Files To Add

### `model/TimeCapsuleSnapshot.java`

Represents one saved snapshot for one user.

Fields:
- `Long id`
- `User user`
- `String name`
- `LocalDateTime createdAt`
- `Integer creditBalance`
- `Integer totalTrades`
- `Double averageRating`
- `List<TimeCapsuleSkill> skills`
- `List<TimeCapsuleKnowledge> knowledgeTopics`

Responsibilities:
- Map to `time_capsule_snapshot`.
- Own the snapshot-level data copied from the user at snapshot creation time.
- Use `@ManyToOne` for the user.
- Use `@OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)` for child skill and knowledge rows.

### `model/TimeCapsuleSkill.java`

Represents one skill captured inside a snapshot.

Fields:
- `Long id`
- `TimeCapsuleSnapshot snapshot`
- `UUID sourceSkillId`
- `String skillName`
- `Double skillScore`
- `Double confidenceIndex`
- `Integer masteryLevel`
- `Integer practiceCount`
- `Integer teachingCount`
- `Integer usageFrequency`

Responsibilities:
- Map to `time_capsule_skill`.
- Store copied skill values from the current `skills` table at the time of snapshot creation.
- Include `sourceSkillId` so the same skill can be matched between two snapshots even if the title changes.
- Include `skillName` for display fallback.

### `model/TimeCapsuleKnowledge.java`

Represents one knowledge topic captured inside a snapshot.

Fields:
- `Long id`
- `TimeCapsuleSnapshot snapshot`
- `UUID sourceTopicId`
- `String topicName`
- `Double masteryLevel`
- `Integer reviewCount`
- `Double retrievabilityScore`
- `String decayStatus`
- `String revisionStatus`

Responsibilities:
- Map to `time_capsule_knowledge`.
- Store copied topic values from the current `knowledge_topics` table at the time of snapshot creation.
- Include `sourceTopicId` so the same knowledge topic can be matched between two snapshots even if the title changes.
- Include `topicName` for display fallback.

## DTO Files To Add

### `dto/CreateTimeCapsuleSnapshotRequest.java`

Fields:
- `String name`

Purpose:
- Used by `POST /api/time-capsules`.
- The name is the only snapshot creation input from the frontend.

### `dto/TimeCapsuleSnapshotSummaryResponse.java`

Fields:
- `Long id`
- `String name`
- `LocalDateTime createdAt`
- `Integer creditBalance`
- `Integer totalTrades`
- `Double averageRating`
- `Integer skillCount`
- `Integer knowledgeTopicCount`

Purpose:
- Returned by create snapshot and get all snapshots.
- Gives the frontend enough data to render the snapshot list and let the user select any two snapshots.

### `dto/TimeCapsuleSnapshotDetailResponse.java`

Fields:
- Snapshot summary fields.
- `List<TimeCapsuleSkillSnapshotDto> skills`
- `List<TimeCapsuleKnowledgeSnapshotDto> knowledgeTopics`

Purpose:
- Optional detailed response if a snapshot detail endpoint is added.
- Useful for debugging or future snapshot detail screens.

### `dto/TimeCapsuleSkillSnapshotDto.java`

Fields:
- `UUID sourceSkillId`
- `String skillName`
- `Double skillScore`
- `Double confidenceIndex`
- `Integer masteryLevel`
- `Integer practiceCount`
- `Integer teachingCount`
- `Integer usageFrequency`

Purpose:
- Represents a stored skill row from one snapshot.

### `dto/TimeCapsuleKnowledgeSnapshotDto.java`

Fields:
- `UUID sourceTopicId`
- `String topicName`
- `Double masteryLevel`
- `Integer reviewCount`
- `Double retrievabilityScore`
- `String decayStatus`
- `String revisionStatus`

Purpose:
- Represents a stored knowledge topic row from one snapshot.

### `dto/CompareTimeCapsulesRequest.java`

Fields:
- `Long oldSnapshotId`
- `Long newSnapshotId`

Purpose:
- Used by the comparison endpoint.
- The frontend can choose any two snapshot IDs from `getAllSnapshots`.

### `dto/TimeCapsuleComparisonResponse.java`

Fields:
- `TimeCapsuleSnapshotSummaryResponse oldSnapshot`
- `TimeCapsuleSnapshotSummaryResponse newSnapshot`
- `List<SkillSnapshotComparisonDto> commonSkills`
- `List<KnowledgeSnapshotComparisonDto> commonKnowledgeTopics`

Purpose:
- Main response for comparing two selected snapshots.
- Only common skills and common knowledge topics are included.
- New-only and removed-only items are not required for this plan.

### `dto/SkillSnapshotComparisonDto.java`

Fields:
- `UUID sourceSkillId`
- `String skillName`
- `Double oldConfidenceIndex`
- `Integer oldMasteryLevel`
- `Double newConfidenceIndex`
- `Integer newMasteryLevel`

Purpose:
- Shows only the requested comparison fields for each common skill.

### `dto/KnowledgeSnapshotComparisonDto.java`

Fields:
- `UUID sourceTopicId`
- `String topicName`
- `Double oldMasteryLevel`
- `Integer oldReviewCount`
- `Double oldRetrievabilityScore`
- `Double newMasteryLevel`
- `Integer newReviewCount`
- `Double newRetrievabilityScore`

Purpose:
- Shows only the requested comparison fields for each common knowledge topic.

## Repository Files To Add

### `repository/TimeCapsuleSnapshotRepository.java`

Extends:
- `JpaRepository<TimeCapsuleSnapshot, Long>`

Functions:
- `List<TimeCapsuleSnapshot> findByUser_IdOrderByCreatedAtDesc(UUID userId)`
- `Optional<TimeCapsuleSnapshot> findByIdAndUser_Id(Long snapshotId, UUID userId)`

Responsibilities:
- Save snapshot headers.
- Fetch all snapshots for the authenticated user.
- Fetch a specific snapshot while enforcing user ownership.

### `repository/TimeCapsuleSkillRepository.java`

Extends:
- `JpaRepository<TimeCapsuleSkill, Long>`

Functions:
- `List<TimeCapsuleSkill> findBySnapshot_Id(Long snapshotId)`
- `List<TimeCapsuleSkill> findBySnapshot_IdIn(Collection<Long> snapshotIds)`

Responsibilities:
- Save captured skill rows.
- Fetch skill rows belonging to one or more snapshots for comparison.

### `repository/TimeCapsuleKnowledgeRepository.java`

Extends:
- `JpaRepository<TimeCapsuleKnowledge, Long>`

Functions:
- `List<TimeCapsuleKnowledge> findBySnapshot_Id(Long snapshotId)`
- `List<TimeCapsuleKnowledge> findBySnapshot_IdIn(Collection<Long> snapshotIds)`

Responsibilities:
- Save captured knowledge topic rows.
- Fetch knowledge topic rows belonging to one or more snapshots for comparison.

## Existing Repositories To Use From The Service Layer

### `UserRepository`

Used to:
- Resolve the authenticated user from `Principal.getName()`.
- Avoid trusting user IDs from the frontend.

### `SkillRepository`

Used to:
- Fetch the authenticated user's current skills with `findByUser(User user)`.
- Copy the current skill values into `TimeCapsuleSkill`.

### `KnowledgeTopicRepository`

Used to:
- Fetch the authenticated user's current knowledge topics with `findByOwner_IdOrderByCreatedAtDesc(UUID userId)`.
- Copy the current topic values into `TimeCapsuleKnowledge`.

### `TradeSessionRepository`

Used to:
- Fetch or count the authenticated user's trades.
- Compute snapshot-level `totalTrades` from the database instead of accepting it from the controller.

## Service File To Add

### `service/TimeCapsuleService.java`

Dependencies:
- `UserRepository`
- `SkillRepository`
- `KnowledgeTopicRepository`
- `TradeSessionRepository`
- `TimeCapsuleSnapshotRepository`
- `TimeCapsuleSkillRepository`
- `TimeCapsuleKnowledgeRepository`

Functions:

### `TimeCapsuleSnapshotSummaryResponse createSnapshot(String userEmail, CreateTimeCapsuleSnapshotRequest request)`

Flow:
- Load the authenticated user from `UserRepository.findByEmail(userEmail)`.
- Fetch current skills from `SkillRepository.findByUser(user)`.
- Fetch current knowledge topics from `KnowledgeTopicRepository.findByOwner_IdOrderByCreatedAtDesc(user.getId())`.
- Fetch current trade data from `TradeSessionRepository`.
- Read `creditBalance`, `tradesCompleted`, and `reputationScore` from the user and/or trade repository data.
- Create and save `TimeCapsuleSnapshot`.
- Convert every current `Skill` into `TimeCapsuleSkill`.
- Convert every current `KnowledgeTopic` into `TimeCapsuleKnowledge`.
- Save child rows through repositories or cascade from the snapshot.
- Return `TimeCapsuleSnapshotSummaryResponse`.

Important:
- The controller provides only the authenticated principal and snapshot name.
- Skill, knowledge, credit, trade, and rating data must come from the database.

### `List<TimeCapsuleSnapshotSummaryResponse> getAllSnapshots(String userEmail)`

Flow:
- Load the authenticated user.
- Fetch all snapshots by `TimeCapsuleSnapshotRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())`.
- Map every snapshot to `TimeCapsuleSnapshotSummaryResponse`.

Purpose:
- This is the response the frontend will render.
- The user can choose any two snapshot IDs from this response for comparison.

### `TimeCapsuleSnapshotDetailResponse getSnapshot(String userEmail, Long snapshotId)`

Flow:
- Load the authenticated user.
- Fetch snapshot by `findByIdAndUser_Id(snapshotId, user.getId())`.
- Fetch or read child skill and knowledge rows.
- Return full snapshot detail.

Purpose:
- Optional but useful for a snapshot detail screen and backend verification.

### `TimeCapsuleComparisonResponse compareSnapshots(String userEmail, Long oldSnapshotId, Long newSnapshotId)`

Flow:
- Load the authenticated user.
- Fetch both snapshots using `findByIdAndUser_Id` so users cannot compare another user's snapshots.
- Fetch skills for both snapshots.
- Fetch knowledge topics for both snapshots.
- Match common skills by `sourceSkillId`.
- Match common knowledge topics by `sourceTopicId`.
- Build comparison DTOs only for common rows.

Skill comparison output:
- `oldConfidenceIndex`
- `oldMasteryLevel`
- `newConfidenceIndex`
- `newMasteryLevel`

Knowledge topic comparison output:
- `oldMasteryLevel`
- `oldReviewCount`
- `oldRetrievabilityScore`
- `newMasteryLevel`
- `newReviewCount`
- `newRetrievabilityScore`

Important:
- The request names the two snapshots as old and new.
- If the frontend sends them in either order, the service can optionally order by `createdAt`, but the API should document whether request order or timestamp order is used.
- For this project, prefer request order: `oldSnapshotId` is treated as old and `newSnapshotId` is treated as new.

### Private helper functions

Functions:
- `User getAuthenticatedUser(String userEmail)`
- `TimeCapsuleSnapshot getOwnedSnapshotOrThrow(Long snapshotId, User user)`
- `TimeCapsuleSnapshotSummaryResponse toSummary(TimeCapsuleSnapshot snapshot)`
- `TimeCapsuleSnapshotDetailResponse toDetail(TimeCapsuleSnapshot snapshot, List<TimeCapsuleSkill> skills, List<TimeCapsuleKnowledge> knowledgeTopics)`
- `TimeCapsuleSkill toSnapshotSkill(TimeCapsuleSnapshot snapshot, Skill skill)`
- `TimeCapsuleKnowledge toSnapshotKnowledge(TimeCapsuleSnapshot snapshot, KnowledgeTopic topic)`
- `SkillSnapshotComparisonDto toSkillComparison(TimeCapsuleSkill oldSkill, TimeCapsuleSkill newSkill)`
- `KnowledgeSnapshotComparisonDto toKnowledgeComparison(TimeCapsuleKnowledge oldTopic, TimeCapsuleKnowledge newTopic)`

## Controller File To Add

### `controller/TimeCapsuleController.java`

Base path:
- `/api/time-capsules`

Dependencies:
- `TimeCapsuleService`

Endpoints:

### `POST /api/time-capsules`

Method:
- `createSnapshot(@RequestBody CreateTimeCapsuleSnapshotRequest request, Principal principal)`

Behavior:
- Require authentication.
- Pass `principal.getName()` and the request to the service.
- Return `TimeCapsuleSnapshotSummaryResponse`.

### `GET /api/time-capsules`

Method:
- `getAllSnapshots(Principal principal)`

Behavior:
- Require authentication.
- Return all snapshots owned by the authenticated user.
- The frontend will render this response and allow the user to select any two snapshots.

### `GET /api/time-capsules/{snapshotId}`

Method:
- `getSnapshot(@PathVariable Long snapshotId, Principal principal)`

Behavior:
- Require authentication.
- Return snapshot details only if it belongs to the authenticated user.

### `POST /api/time-capsules/compare`

Method:
- `compareSnapshots(@RequestBody CompareTimeCapsulesRequest request, Principal principal)`

Behavior:
- Require authentication.
- Compare any two snapshots selected by the user from the `GET /api/time-capsules` response.
- Return only common skill comparisons and common knowledge topic comparisons.

## Comparison Rules

### Skill matching

Primary key:
- Match by `sourceSkillId`.

Fallback:
- If `sourceSkillId` is missing, match by normalized `skillName`.

Output only common skills:
- Skip skills that exist only in the old snapshot.
- Skip skills that exist only in the new snapshot.

For each common skill, return:
- Old confidence index.
- Old mastery level.
- New confidence index.
- New mastery level.

### Knowledge topic matching

Primary key:
- Match by `sourceTopicId`.

Fallback:
- If `sourceTopicId` is missing, match by normalized `topicName`.

Output only common topics:
- Skip topics that exist only in the old snapshot.
- Skip topics that exist only in the new snapshot.

For each common knowledge topic, return:
- Old mastery level.
- Old review count.
- Old retrievability score.
- New mastery level.
- New review count.
- New retrievability score.

## Error Handling

Use `ResponseStatusException` or service-level exceptions mapped by the controller.

Cases:
- Missing authentication: `401 UNAUTHORIZED`.
- Authenticated user not found: `401 UNAUTHORIZED`.
- Snapshot not found for user: `404 NOT_FOUND`.
- Missing snapshot name: `400 BAD_REQUEST`.
- Same snapshot selected twice for comparison: `400 BAD_REQUEST`.

## Transaction Rules

- `createSnapshot` should be `@Transactional` because it writes snapshot header, skill rows, and knowledge rows together.
- `compareSnapshots`, `getAllSnapshots`, and `getSnapshot` can be read-only transactions.

## Implementation Order

1. Add model files:
   - `TimeCapsuleSnapshot.java`
   - `TimeCapsuleSkill.java`
   - `TimeCapsuleKnowledge.java`

2. Add repository files:
   - `TimeCapsuleSnapshotRepository.java`
   - `TimeCapsuleSkillRepository.java`
   - `TimeCapsuleKnowledgeRepository.java`

3. Add DTO files:
   - `CreateTimeCapsuleSnapshotRequest.java`
   - `CompareTimeCapsulesRequest.java`
   - `TimeCapsuleSnapshotSummaryResponse.java`
   - `TimeCapsuleSnapshotDetailResponse.java`
   - `TimeCapsuleSkillSnapshotDto.java`
   - `TimeCapsuleKnowledgeSnapshotDto.java`
   - `TimeCapsuleComparisonResponse.java`
   - `SkillSnapshotComparisonDto.java`
   - `KnowledgeSnapshotComparisonDto.java`

4. Add service file:
   - `TimeCapsuleService.java`

5. Add controller file:
   - `TimeCapsuleController.java`

6. Run backend tests:
   - `mvn test`

## Notes About `schema2.sql`

The model classes should implement the tables described in `schema2.sql`, but the Java model should stay consistent with the current project:
- Snapshot entity IDs can be `Long`.
- `TimeCapsuleSnapshot.user` should reference `User`, whose ID is currently `UUID`.
- Captured skill rows should include `sourceSkillId` as `UUID`.
- Captured knowledge rows should include `sourceTopicId` as `UUID`.

If `schema2.sql` is later used as a real migration, its `user_id` column should match the database type used by `users.id`.
