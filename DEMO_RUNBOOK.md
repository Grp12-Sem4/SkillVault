# Demo Runbook

## Start Commands

Backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Frontend:

```powershell
cd frontend
npm run dev
```

## Demo URLs

- Frontend: `http://localhost:5173`
- Frontend fallback: `http://127.0.0.1:5173`
- Backend API base: `http://localhost:8080/api`

## Verified Demo Accounts

- Provider account:
  - Email: `shrey.pro@g`
  - Password: `DemoPass123!`
- Requester account:
  - Email: `shrey.req@g`
  - Password: `DemoPass123!`

## Verified Demo Data

- Offered skill created for the provider:
  - Title: `React Mentoring`
  - Category: `Frontend`
  - Credit Value: `3`
  - Skill Category: `TECHNICAL`

## Exact Demo Click Path

### 1. Login as requester

Path:

1. Open `http://localhost:5173/login`
2. Log in as `shrey.req@g`
3. Password: `DemoPass123!`

What to say/show:
- This is the protected login entry point for the app.
- After login, the app redirects into the authenticated experience instead of exposing pages publicly.

### 2. Show Dashboard

Path:

1. Stay on Dashboard after login

What to say/show:
- Point out the authenticated welcome state and the live credit balance card.
- Use the top navigation to show the app areas are grouped into Skills, Knowledge, and Trades.

### 3. Show Skills page

Path:

1. Click `Skills`

What to say/show:
- This page is tied to the logged-in user and loads personal skills from `/api/skills/my`.
- It demonstrates the existing authenticated frontend/backend integration still works.

### 4. Show Knowledge page

Path:

1. Click `Knowledge`

What to say/show:
- This confirms another authenticated page loads correctly under the same session.
- It helps show the app is broader than a single marketplace screen.

### 5. Create a trade as requester

Path:

1. Click `Trades`
2. In Marketplace, find `React Mentoring`
3. Click `Request Trade`
4. Set a scheduled time
5. Set duration to `2`
6. Click `Submit Trade Request`

What to say/show:
- The marketplace is populated from backend data, not hardcoded cards.
- The trade request succeeds and immediately appears in the requester’s `Pending` section.

### 6. Switch to provider and accept

Path:

1. Click `Logout`
2. Log in as `shrey.pro@g`
3. Click `Trades`
4. Find the pending trade
5. Click `Accept`

What to say/show:
- Only the provider sees the `Accept` action for a pending trade.
- The board refreshes immediately and the trade moves from `Pending` to `Active`.

### 7. Switch back to requester and complete

Path:

1. Click `Logout`
2. Log in again as `shrey.req@g`
3. Click `Trades`
4. In the `Active` section, keep rating at `5`
5. Click `Complete`

What to say/show:
- Only the requester sees the `Complete` action for an active trade.
- Completion moves the trade into `Completed`, records the rating, and updates credit balance live in the UI.

### 8. Optional balance proof on provider side

Path:

1. Click `Logout`
2. Log in as `shrey.pro@g`
3. Observe Dashboard credit balance

What to say/show:
- The provider balance increases after completion.
- This confirms the credit transfer is not just cosmetic on the requester side.

## Proof Points To Highlight

- Protected auth flow:
  - Login gates access to Dashboard, Skills, Knowledge, and Trades.
  - Protected routes survive direct page refresh once authenticated.
- Marketplace:
  - Cards are loaded from `GET /api/skills/marketplace`.
  - The marketplace excludes the current user’s own offered skills.
- Trade lifecycle:
  - Requester creates a trade.
  - Provider accepts it.
  - Requester completes it with a rating.
- Credit transfer:
  - Verified requester balance dropped from `50` to `44`.
  - Verified provider balance increased from `50` to `56`.
- Dynamic UI refresh:
  - Trade board updates after each mutation.
  - Credit display refreshes after completion.
  - Completed trade shows persisted rating.

## Troubleshooting

### Backend not starting

- Confirm Postgres is running on `localhost:5432`.
- Start from repo root, not from `frontend`.
- If Maven wrapper fails on a fresh machine, let it download its dependencies before retrying.

### Frontend not starting

- Run the command from the `frontend` directory.
- If Vite says the port is busy, stop the old process or let it choose another local port.
- If dependencies are missing, run `npm install` inside `frontend`.

### CORS issue

- Use `http://localhost:5173` first for the demo.
- The backend now also allows `http://127.0.0.1:5173`, but `localhost` is still the primary verified URL.

### Empty marketplace

- Log in as the provider account and confirm `React Mentoring` exists on the Skills page.
- If needed, recreate a single offered skill with title `React Mentoring`, category `Frontend`, credit value `3`, and a short description.

### Stale browser session

- Log out and log back in with the intended account.
- If the UI still looks stale, hard refresh the page once.
- As a last step, clear local storage for the app origin and log in again.
