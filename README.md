# API autotests: Playwright + TestNG

[![API tests](https://github.com/eageordie/api-autotests/actions/workflows/api-tests.yml/badge.svg)](https://github.com/eageordie/api-autotests/actions/workflows/api-tests.yml)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Playwright](https://img.shields.io/badge/Playwright-1.49-2EAD33)
![TestNG](https://img.shields.io/badge/TestNG-7.10-blue)
![Allure](https://img.shields.io/badge/report-Allure-yellow)

Automated API tests for [Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html), a public practice API for bookings with token auth.

**Stack:** Java 21 · Playwright (`APIRequestContext`) · TestNG · AssertJ · Jackson · Datafaker · Allure · Maven · GitHub Actions

## What is covered

| Area | Checks | Group |
| --- | --- | --- |
| Health check | service is up, response time under 5 s | smoke |
| Authentication | valid login returns a token; 5 invalid credential cases (incl. SQL injection) return no token | smoke, regression |
| Booking lifecycle | chained scenario: create → read → replace → patch → delete → 404; response echoes the request; search by name | smoke, regression |
| Access control | PUT / PATCH / DELETE without a valid token → 403 and data stays unchanged | regression |
| Negative | unknown ids → 404, malformed JSON → 400 | regression |
| Known issues | defects found while writing the suite (below) | known-issue |

Regression: 23 tests, about 20 s, run in parallel by class.

## Defects found

These tests describe the **expected** behavior, so they fail until the defect is fixed. They are kept out of the regression run to keep CI green.

| ID | Defect | Expected | Actual | Severity |
| --- | --- | --- | --- | --- |
| BUG-1 | Invalid booking body (empty, required fields missing) | 400 | 500 Internal Server Error | Normal |
| BUG-2 | Checkout date before checkin is accepted | 400 | 200, booking created | Critical |
| BUG-3 | Negative `totalprice` is accepted | 400 | 200, booking created | Critical |
| BUG-4 | Text in numeric `totalprice` is accepted | 400 | 200, price silently lost | Critical |

## Design

- **`ApiClient`**: wrapper over Playwright `APIRequestContext`; attaches every request and response to Allure, so a failed test carries its own evidence.
- **`AuthClient`, `BookingClient`**: one method per endpoint, marked as Allure steps.
- **Models**: Java records + Jackson; responses are compared with `usingRecursiveComparison()`.
- **Test data**: `BookingFactory` makes unique data per test, so parallel runs never collide.
- **Cleanup**: bookings created by a test class are deleted in `@AfterClass`.
- **Config**: `base.url`, `admin.user`, `admin.password`, `timeout.ms` via `-D` properties or environment variables.

```
src/test/java/io/github/qa/api
├── client/   ApiClient, AuthClient, BookingClient, ApiResponse
├── config/   Config
├── data/     BookingFactory
├── model/    Booking, BookingDates, CreatedBooking, AuthResponse
├── tests/    HealthCheck, Auth, BookingLifecycle, BookingNegative, KnownIssues
└── utils/    Json
```

## Run

Requires JDK 21 and Maven 3.9+.

```bash
mvn test                              # regression (default)
mvn test -Dsuite=smoke.xml            # smoke only
mvn test -Dsuite=known-issues.xml     # open defects (fail by design)
mvn test -Dbase.url=http://localhost:3001   # another environment

mvn allure:serve                      # open the Allure report
```

## CI

GitHub Actions runs regression on every push and pull request, and on weekday mornings. A manual run can pick the suite. The Allure report is uploaded as a build artifact.
