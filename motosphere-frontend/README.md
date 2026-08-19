# MotoSphere Frontend

A real, standalone React frontend for the MotoSphere backend — separate app,
separate process, separate deploy from the Spring Boot API, talking to it
over HTTP with CORS configured on the backend side for exactly this.

(There's also a single-file vanilla-JS version of this UI meant to be served
directly from the backend's `src/main/resources/static/` folder, for a
zero-dependency same-origin option. This project is the other end of that
tradeoff: real tooling, real component structure, easier to extend, but a
separate deployable that needs the backend's CORS opened up to it — already
done, see below.)

## Stack

- **React 19** + **Vite** (dev server + build)
- **react-router-dom v6** for routing and role-based route guarding
- **Bootstrap 5** + **react-bootstrap** for components, re-themed via CSS
  variables rather than fighting the framework
- **Axios** with request/response interceptors (attaches the JWT, normalizes
  backend error shapes into `err.message`)
- **React Context** for auth/session state and app-wide toasts — no Redux;
  the app's state surface doesn't need it

> This was actually built and verified in the sandbox that produced it —
> `npm run build` and `npm run lint` both pass clean (0 errors, 0 warnings)
> at time of writing. It could not be run in an actual browser here, so a
> real `npm run dev` + click-through is still worth doing as your first step.

## Project structure

```
src/
├── api/            one file per backend resource (authApi, vehicleApi, ...),
│                   each just a thin set of axios calls returning r.data
├── context/         AuthProvider + ToastProvider (definitions split into
│                   authContext.js / toastContext.js for clean Fast Refresh)
├── hooks/           useAuth(), useToast()
├── components/
│   ├── common/       FormField, StatusBadge, EmptyState, LoadingBlock,
│   │                 Money, ProtectedRoute
│   ├── layout/       Sidebar, Topbar, AppLayout (the shell for every
│   │                 logged-in page)
│   ├── appointment/  AppointmentCard (customer), BookAppointmentForm
│   └── jobcard/      JobCardDetails, InvoicePanel, CreateJobCardForm,
│                     MechanicJobCardWork — the shared pieces of the
│                     "diagnose → itemize → complete → invoice → pay" flow
├── pages/
│   ├── auth/         LoginPage, RegisterCustomerPage, RegisterGaragePage,
│                     SuperAdminSetupPage
│   ├── customer/      VehiclesPage, GaragesPage, AppointmentsPage
│   ├── manager/        GarageQueuePage, MechanicsPage
│   ├── mechanic/        MyJobsPage
│   └── admin/            PendingGaragesPage, CreateGaragePage,
│                         CreateStaffPage, AllUsersPage
├── utils/roleNav.js  single source of truth for each role's sidebar links
│                     and "home" route
└── App.jsx           the full route tree
```

This mirrors how a real production frontend is usually laid out: a thin API
layer with no UI knowledge, page components that own data-fetching for their
route, and small reusable pieces (`FormField`, `StatusBadge`, `Money`) so
forms and status displays look and behave identically everywhere instead of
each page reinventing them.

## Running it

```bash
npm install
npm run dev      # http://localhost:5173
```

You need the backend running too (`mvn spring-boot:run`, defaults to
`http://localhost:8080`). Point the frontend at it via `.env`:

```
VITE_API_BASE_URL=http://localhost:8080
```

(`.env` is already set to this default; `.env.example` documents it for
anyone cloning the repo fresh.)

### CORS

Because this is a genuinely separate origin from the API (`:5173` vs
`:8080`), the backend needs to explicitly allow it — already configured in
`SecurityConfig` via a `CorsConfigurationSource` bean, driven by
`motosphere.cors.allowed-origins` in `application.properties` (defaults to
`http://localhost:5173`). If you deploy this frontend somewhere real, add its
real origin to that property.

## Design

Same visual language as the single-file version: a "shop floor" palette
(concrete background, ink side rail, amber as the one primary accent, grease
green / torque red for success / danger) with Space Grotesk for headings and
IBM Plex Mono for anything numeric — IDs, prices, invoice numbers. The
`StatusBadge` component is the one signature element, reused for every status
in the app (appointment, job card via `completionDate`, invoice payment
status, garage approval status, user active/inactive) so status always reads
the same way no matter where you see it.

Rather than write custom CSS from scratch, Bootstrap's own `--bs-primary`,
`--bs-success`, `--bs-danger`, and `--bs-warning` variables are re-pointed at
this palette in `index.css` — so a plain `<Button variant="outline-danger">`
or `<Button variant="outline-success">` anywhere in the app is already
on-brand without extra classes.

## Auth flow

`AuthProvider` holds `{ token, userId, role, profile }` in React state,
persisted to `localStorage` under `ms_auth` (a real browser running this app
directly, not a sandboxed preview — `localStorage` is the right tool here,
same as the single-file version). On login, it fetches the full profile via
`GET /users/{userId}` so the topbar can show a real name/garage instead of
just a role.

`ProtectedRoute` wraps each role's route subtree and redirects to `/login` if
unauthenticated, or to `/` (which redirects to the caller's own role home) if
they're logged in but the wrong role for that subtree. The Axios response
interceptor also clears stored auth on any `401`, so an expired/invalid token
gets cleaned up the next time any request fails, not just at click-through.

### Bootstrapping the first super admin

`/setup-admin` (linked from the bottom of the login form, not one of the
three visible tabs) creates the platform's very first `SUPER_ADMIN` account.
It's deliberately kept out of the normal tab flow because it only ever works
once — the backend rejects every call after a super admin already exists,
regardless of who makes it. Since `POST /auth/register-super-admin` returns a
ready-to-use JWT (same shape as login), submitting this form logs you
straight into the new account instead of bouncing you back to a login
screen — `AuthProvider` exposes `applyAuthResponse()` for exactly this, so
both `login()` and this one-time setup flow share the same "persist token +
fetch profile" logic instead of duplicating it.

## Cross-checked against the backend, endpoint by endpoint

Every function in `src/api/*.js` was checked against the exact path and HTTP
method in the backend's `SecurityConfig`, and every page/component that calls
a role-restricted endpoint was confirmed to only be reachable by that role
(via `ProtectedRoute` + the fact that `utils/roleNav.js` only ever routes a
role to its own pages). Unlike the single-file version's first draft, this
was designed with three separate named functions from the start
(`getMyAppointments` / `getGarageAppointments` / `getMechanicAppointments`)
specifically to make it structurally impossible to accidentally call the
wrong role's endpoint by reusing one ambiguous function name across roles.

## Known gaps / things to decide next

- No password-change / forgot-password flow — same gap as the backend itself
  (see the backend README).
- `npm audit` reports 2 moderate advisories in `react-router` (open-redirect
  via backslash in `<Link>`/`useNavigate`, and an SSR-hydration deserializer
  issue) that only have fixes in the v7 line, which reintroduces a different
  high-severity RSC-mode advisory instead. Neither applies to this app in
  practice — there's no SSR here, and every route/redirect target in this
  codebase is a hardcoded string, never built from user input — but it's a
  real tradeoff worth re-evaluating whenever react-router ships a patch that
  clears both without a breaking major-version jump.
- No test suite (unit or e2e) yet — reasonable next step once the UI has been
  clicked through for real.
