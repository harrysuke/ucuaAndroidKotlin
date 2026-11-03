# UCUA — Native Android App

Native Android client for the **UCUA (U-SEE U-ACT)** workplace HSE hazard reporting system,
built for **Johor Port Berhad (JPB)**. It implements the mobile requirements captured in
`../generated_documents/` and talks to the PHP REST API in `../ucua_api/`, which in turn reads
and writes the existing `ucua-staging` database used by the web application.

---

## 1. What the app does

| Capability | Detail |
| --- | --- |
| **Authentication** | Staff ID + password login, Bearer-token session persisted across launches, registration (BR-001/BR-002), password recovery (BR-005), sign-out |
| **Unsafe Condition (UC)** | Location + condition description + photo evidence, observer details captured automatically |
| **Unsafe Action (UA)** | Location, offence, violator name/company/department, action taken + photo evidence |
| **Draft & submit** | Save incomplete reports as drafts (BR-013), resume and submit later; rejected reports can be edited and resubmitted |
| **Photo evidence** | Up to 5 images per report, 8 MB each, extension-validated server-side, stored under `ucua300/uploads/` |
| **Approval workflow** | PSSD HSE Officer → HOS → HOD → Closed, role-gated per `AccessLevel`; rejection requires a written reason (BR-025) |
| **Workflow timeline** | Per-report history (submitted / approved / rejected / resubmitted / closed) with author and timestamp |
| **Dashboard** | Personal counters, role-gated approval queue, 6-month submission trend, activity feed, participation leaderboard |
| **Reports list** | Search, type filter (UC/UA), status filter (All / Drafts / Pending / Closed / Rejected) |
| **Profile** | Account details, permission checklist derived from role, build + API info |

---

## 2. Tech stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose, Material 3 (BOM 2026.06.01) |
| Architecture | MVVM + Clean Architecture (`core` / `ui` layers) |
| Navigation | Navigation Compose 2.9.8 |
| Async | Coroutines + Flow (`StateFlow` / `SharedFlow`) |
| Local storage | Room 2.8.4 (report cache) + Preferences DataStore (session) |
| Networking | Retrofit 3.0.0 + OkHttp 5.4.0 + kotlinx.serialization |
| Images | Coil 2.7.0 |
| Build | AGP 9.4.1, KSP 2.3.6, compileSdk/targetSdk 37, minSdk 24, Java 17 |

The trend chart is drawn with Compose `Canvas` — no third-party charting dependency.

---

## 3. Project structure

```
app/src/main/java/com/ucua/app/
├── MainActivity.kt                 # Compose host, edge-to-edge
├── UcuaApplication.kt              # Instantiates AppContainer
├── core/
│   ├── di/AppContainer.kt          # Manual DI container
│   ├── model/
│   │   ├── AuthModels.kt           # User (role helpers), Register/Forgot DTOs
│   │   ├── ReportModels.kt         # ReportStatus, FormType, Report, request/response DTOs
│   │   ├── DashboardModels.kt      # Counts, queues, trend, activity, leaderboard
│   │   └── LookupModels.kt         # Location, Offence, Company, Department
│   ├── network/                    # ApiService (Retrofit), AuthInterceptor, NetworkModule
│   ├── repository/                 # Auth, Report, Dashboard, Lookups
│   ├── security/TokenStore.kt      # DataStore session persistence
│   └── util/
│       ├── ReportEvents.kt         # SharedFlow change signal (refresh lists/dashboard)
│       └── ReportsFilterRequest.kt # One-shot dashboard → reports pre-filter hand-off
└── ui/
    ├── theme/                      # JPB palette, typography, shapes
    ├── components/
    │   ├── StateComponents.kt      # Loading / Empty / Error, StatusChip, InlineBanner
    │   ├── UcuaComponents.kt       # SectionHeader, KpiCard, TypeBadge, InfoRow, DropdownField,
    │   │                           #   WorkflowTimeline, date/time formatters
    │   └── TrendChart.kt           # Canvas grouped bar chart
    ├── navigation/UcuaNavHost.kt   # Splash gate + bottom nav (Home/Reports/Report/Profile)
    ├── auth/                       # Login, Register, ForgotPassword (+ ViewModels)
    ├── dashboard/                  # DashboardScreen + DashboardViewModel
    ├── report/                     # ReportTypeScreen, ReportFormScreen + ReportFormViewModel
    ├── reports/                    # ReportsScreen, ReportDetailScreen (+ ViewModels)
    └── profile/ProfileScreen.kt
```

---

## 4. Backend API (`../ucua_api/`)

All endpoints are procedural PHP 7.4-compatible, PDO/MySQL, and authenticate with a
`Authorization: Bearer <token>` header issued by `login.php` (HMAC-SHA256, stored in `mobile_tokens`).

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `login.php` | Issue token (3-failed-login lockout per BRR-008) |
| POST | `register.php` | Self-registration, pending approval (BR-001/002) |
| POST | `forgot.php` | Password recovery request (BR-005), no staff-ID enumeration |
| GET | `dashboard.php` | Counters, queues, trend, activity, leaderboard |
| GET | `lookups.php` | Locations, offences, companies, departments |
| GET/POST | `reports.php` | List (filter/search/mine) and create (draft or submit) |
| GET/PUT/PATCH/DELETE | `reports.php?id=N` | Detail (photos, history, permissions), update, delete |
| POST | `workflow.php` | `approve` / `reject` / `close` / `resubmit` |
| GET/POST | `photos.php` | List and multipart upload of evidence |

The API deliberately uses the **legacy status vocabulary** so mobile-created reports appear
correctly in the existing web UI:

```
Draft → Pending PSSD HSE Officer → Approved by PSSD HSE Officer
      → Approved by HOS → Approved by HOD (closed)
```

---

## 5. Setup

### Backend

1. `ucua_api/` must sit under the web root — it is already at `D:/laragon/www/ucua300/ucua_api/`.
2. Confirm the credentials in `ucua_api/config.php` point at the `ucua-staging` database.
3. Ensure `D:/laragon/www/ucua300/uploads/` exists and is writable (created automatically on
   first upload if the parent is writable).
4. `config.php` auto-creates the `mobile_tokens` and `mobile_report_history` tables on first run.

### Android

1. **Base URL** — `app/build.gradle.kts` sets `API_BASE_URL` to
   `http://10.0.2.2:8181/ucua300/ucua_api/` (`10.0.2.2` = host loopback from the emulator,
   `8181` = Laragon nginx port). Point it at your server for a physical device.
2. **Build:**
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`
3. **Install:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
4. **Sign in** with a staff ID + password from the `user` table. Passwords are verified against
   the legacy MD5 hash so existing web users work unchanged.

A test account used during verification: staff ID `MOBILETEST`, AccessLevel 4
(PSSD HSE Officer — can submit, view all reports, and approve at the HSE stage only).

---

## 6. Verification performed

End-to-end against a live Laragon server (`http://127.0.0.1:8181/ucua300/ucua_api/`) and the
`ucua_test` emulator (Android 13, 1080×2340):

- Login, token issue, session restore across app restarts, unauthorised request → `401`
- Registration: duplicate staff ID → `409`, new staff → `201` (pending approval)
- Lookups: 155 locations, 30 offences, 20 companies, 25 departments
- UC create (`201`, id 1052), UA create (`201`, id 1053)
- Photo upload → stored under `uploads/`, served back as `HTTP 200 image/jpeg`
- Approve → `Approved by PSSD HSE Officer`; wrong role → `403`; reject without reason → `422`;
  reject → `Rejected by ...`; resubmit → `Pending PSSD HSE Officer`
- Draft save → promote to submitted
- Dashboard activity feed and 6-month trend populate from real data
- Device UI verified: login, dashboard KPIs/queue/trend/activity/leaderboard, reports list with
  filters, report detail with workflow timeline, approve action moving status to *Pending HOS*,
  profile with role-derived permissions

All ten PHP files pass `php -l`.

---

## 7. Known considerations

- **Cleartext HTTP** is enabled in the debug manifest for emulator testing — use HTTPS in production.
- **Legacy MD5 password hashing** is retained for compatibility with the existing web application.
- **Cold start on the test emulator is slow** (~38 s on a software-rendered x86_64 image with a
  debug build). This is emulator overhead, not app cost — no ANRs or crashes were observed.
- **Test records** created during verification remain in `ucua-staging`: staff IDs `MOBILETEST`
  (AccessLevel 4, active) and `MOBILEREG` (AccessLevel 1, pending), plus reports `UCUA-01047`,
  `01048`, `01050`, `01052`, `01053`, `01054`, `01055`. They were intentionally left in place
  rather than deleted so nothing is removed without your confirmation — and they double as demo
  data covering *Pending HSE* and *Approved by PSSD HSE Officer* states. To purge them:

  ```sql
  DELETE FROM mobile_report_history WHERE report_id IN (1047,1048,1050,1052,1053,1054,1055);
  DELETE FROM ucuaphoto WHERE ucuaform_Id IN (1047,1048,1050,1052,1053,1054,1055);
  DELETE FROM ucuaform  WHERE id IN (1047,1048,1050,1052,1053,1054,1055)
                          AND staffId IN ('MOBILETEST','MOBILEREG');
  DELETE FROM history   WHERE staffid IN ('MOBILETEST','MOBILEREG');
  DELETE FROM mobile_tokens WHERE staffId IN ('MOBILETEST','MOBILEREG');
  DELETE FROM user      WHERE staffId IN ('MOBILETEST','MOBILEREG');
  ```

  Also delete the single orphaned evidence file
  `ucua300/uploads/20261030034031_1047_e23c61.jpg` (report 1047's photo).
