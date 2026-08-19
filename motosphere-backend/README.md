# MotoSphere Backend

A Spring Boot backend for **MotoSphere**, a multi-garage vehicle service management
platform. Built to the exact package layout and entity/field spec provided,
using the same overall conventions (JWT auth, layered architecture, centralized
exception handling, DTO-only responses) as the companion `healthcare-monolithic-jwt`
and `garage-monolithic-jwt` reference projects.

## Stack

Java 21, Spring Boot 3.5.14, Spring Security 6 (stateless JWT), Spring Data JPA +
MySQL, JJWT 0.13.0, ModelMapper, Lombok, Jakarta Validation, springdoc-openapi.

> **Note on this build**: this sandbox has no access to Maven Central, so this
> could not be verified with a real `mvn compile`. It has been checked
> carefully for brace balance, package/folder consistency, DTO constructor
> argument order, and - critically for this project - correct Spring Data
> derived-query property paths (see below). Please still run a real
> `mvn clean compile` yourself the first time.

## Frontend

`src/main/resources/static/index.html` is a complete single-page console for
all four roles - vanilla JS, no build step, no framework, nothing to install.
Spring Boot serves static content from that folder automatically, so the
whole app (API + UI) runs as one process on one port:

```
mvn spring-boot:run
# then open http://localhost:8080
```

This is same-origin by construction (the page and the API share a host and
port), so there's no CORS configuration to set up. Auth state (JWT + role)
persists in the browser's `localStorage` so a page refresh doesn't log you
out - this is a real file the browser loads on its own, not a sandboxed
in-chat preview, so `localStorage` is the right tool here.

What it covers per role:
- **Customer**: register vehicles, browse approved garages, book an
  appointment, track it through to a job card, and pay the invoice once the
  garage marks it ready.
- **Garage manager**: see the garage's appointment queue, assign an active
  mechanic to a booking, add/deactivate/reactivate mechanics.
- **Mechanic**: see assigned appointments, open a job card, log diagnosis and
  parts/labour, mark the job complete (which triggers invoice generation).
- **Super admin**: review and approve/reject pending garage applications
  (setting the commission rate on approval), create a garage or staff account
  directly, view/deactivate/reactivate any user.

It talks to the exact endpoints documented below and was cross-checked
field-by-field against every request/response DTO and every
`SecurityConfig` role matcher - including catching and fixing a real bug
during that check: the mechanic's "my jobs" view was initially pointed at
`/appointments/my` (customer-only), when the correct endpoint for a mechanic
is `/appointments/mechanic`.

## Package layout (as specified)

```
com.motosphere
├── controller
├── service            (interfaces)
├── serviceImpl         (implementations)
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── security
├── config
├── exception
├── util
└── enums
```

## A gotcha specific to this project's field naming

Every entity here uses its own explicitly-named primary key (`userId`,
`garageId`, `vehicleId`, `appointmentId`, `jobCardId`, `jobCardItemId`,
`invoiceId`, `paymentId`) rather than a generic `id` field. Spring Data's
derived-query convention (`findByAssociationId(...)`) normally assumes the
associated entity exposes a property literally called `id` - since none of
these entities do, every association-based derived query in this project had
to be written with an explicit underscore path instead, e.g.:

```java
List<Vehicle> findByCustomer_UserId(Long customerId);   // not findByCustomerId
List<User> findByGarage_GarageId(Long garageId);          // not findByGarageId
Optional<JobCard> findByAppointment_AppointmentId(Long appointmentId);
```

If you add new repository methods later, keep this in mind - `findByXAssociationId`
will fail at startup with a `PropertyReferenceException` unless you spell out
the real nested property name this way.

## Entity model

8 entities exactly as specified: `User`, `Garage`, `Vehicle`, `Appointment`,
`JobCard`, `JobCardItem`, `Invoice`, `Payment`. `Appointment` deliberately has
no `customerId` field - the customer is always derived via
`Appointment -> Vehicle -> customer`.

## Roles

`SUPER_ADMIN`, `GARAGE_MANAGER`, `MECHANIC`, `CUSTOMER`. Public self-registration
(`POST /auth/register`) always creates a `CUSTOMER`. `GARAGE_MANAGER` and
`MECHANIC` accounts are provisioned by a `SUPER_ADMIN` via `POST /users/staff`,
tied to a specific `garageId`.

**Getting your first SUPER_ADMIN**: call `POST /auth/register-super-admin`
with a first name, email, and password - it's `permitAll` in
`SecurityConfig` so it needs no token. The service layer checks
`existsByRole(SUPER_ADMIN)` first and rejects the call outright if one
already exists, so this genuinely only ever works once, regardless of who
calls it or with what body:
```json
POST /auth/register-super-admin
{
  "firstName": "Super",
  "lastName": "Admin",
  "email": "admin@motosphere.com",
  "password": "ChangeMe#123"
}
```
The response includes a ready-to-use JWT, same as `/auth/login`. From there,
use that token for `POST /users/staff` and `POST /garages`.

This is deliberately an explicit, observable action (you get an immediate
HTTP response) rather than a config-file-driven startup seeder - a seeder is
opaque to verify (you have to check logs/DB to know if it actually ran) and
silently becomes a no-op forever after the first successful boot, which is
easy to trip over if your properties file changes after that first run. Once
you're confident in your deployment process, you may still want to remove or
firewall this endpoint at the infra level (e.g. don't expose it publicly past
initial setup) since a `BadRequestException` response still confirms to an
anonymous caller whether a super admin exists yet.

**Registering as a garage (with approval)**: a prospective `GARAGE_MANAGER`
doesn't need a `SUPER_ADMIN` to provision them - they self-register a garage
+ their own manager account together, as one pending application:
```json
POST /auth/register-garage-manager
{
  "firstName": "Ravi", "lastName": "Kumar",
  "email": "ravi@speedymotors.com", "password": "Manager#123",
  "phoneNumber": "9876543210",
  "garageName": "Speedy Motors", "ownerName": "Ravi Kumar",
  "address": "123 MG Road", "garageContactNumber": "9876543210",
  "garageEmail": "contact@speedymotors.com"
}
```
Both the `Garage` (`approvalStatus = PENDING`) and the manager's `User`
(`active = false`) are created immediately, but neither is usable yet: the
garage doesn't show up in `GET /garages` (public browsing only ever returns
`APPROVED` garages), and the manager can't log in at all - `active = false`
means Spring Security's `UserDetails.isEnabled()` returns false, so
`/auth/login` fails outright with a disabled-account error, not just a
permissions error.

A `SUPER_ADMIN` reviews applications and decides:
```
GET   /garages/pending                    -> list of pending applications,
                                              including the applicant's name/email
PATCH /garages/{garageId}/approve         -> body: { "commissionPercentage": 10.0 }
PATCH /garages/{garageId}/reject          -> body: { "reason": "optional, for your own records" }
```
Approving sets the commission terms (deliberately not something the
applicant proposes themselves), flips the garage to `APPROVED`, and
reactivates the manager's account so they can finally log in. Rejecting
leaves the manager permanently deactivated - there's no automatic re-apply
flow; they'd need to submit a fresh application if you want to allow that.

Directly created garages (`POST /garages`, `SUPER_ADMIN`) skip this review
entirely and start `APPROVED` immediately - the admin creating it has
already vetted it by the act of creating it.

**Garage managers add/remove their own mechanics** - this doesn't require a
`SUPER_ADMIN` either, and is scoped to the manager's own garage on the server
side (never trusts a client-supplied `garageId`):
```
POST   /garages/my/mechanics                          -> add a mechanic to my garage
GET    /garages/my/mechanics                          -> list my garage's mechanics
PATCH  /garages/my/mechanics/{mechanicUserId}/deactivate
PATCH  /garages/my/mechanics/{mechanicUserId}/reactivate
```
"Remove" is a deactivation, not a hard delete - a mechanic can be referenced
by existing `JobCard`/`Appointment`/`Payment` records, and `JobCard.mechanic`
is a non-nullable FK, so hard-deleting a mechanic with any history would
either violate that constraint or (with a careless cascade) silently destroy
job card records. Deactivating blocks their login (same `isEnabled()`
mechanism as above) while preserving everything they're linked to. The
existing `DELETE /users/{userId}` (`SUPER_ADMIN` only) still exists for
genuinely mistaken/empty accounts, but expect it to fail on the FK constraint
for any mechanic with real history - that's intentional, not a bug.

A `SUPER_ADMIN` has the equivalent broader versions of these same actions:
`POST /users/staff` (any role, any garage), `PATCH /users/{userId}/deactivate`,
`PATCH /users/{userId}/reactivate` (any user, not just mechanics).

## Business workflow

1. Customer registers, registers a vehicle, browses garages, books an
   appointment (`BOOKED`).
2. Garage Manager (scoped to their own garage) views bookings, assigns a
   mechanic from their own garage's staff (`ASSIGNED`).
3. Mechanic views their assigned appointments, creates a Job Card with
   diagnosis/remarks/labour charge (`IN_PROGRESS`), adds Job Card Items -
   `amount` is always computed server-side as `quantity × unitPrice`, never
   trusted from the client.
4. Mechanic completes the job (`COMPLETED`). This is the single trigger point
   for invoice generation:
   - `subtotal = labourCharge + sum(item amounts)`
   - `gstAmount = subtotal × 18%` (fixed rate, from `invoice.gst.percentage`)
   - `totalAmount = subtotal + gstAmount`
   - `Invoice.paymentStatus = PENDING`
5. Customer pays (`POST /payments/invoice/{invoiceId}`). Commission is split
   on the **subtotal only, GST excluded**, per the business rule:
   - `commissionAmount = subtotal × garage.commissionPercentage / 100`
   - `garageAmount = subtotal - commissionAmount`
   - `Payment.amountPaid = invoice.totalAmount` (the full GST-inclusive amount
     actually collected from the customer)
   - `Invoice.paymentStatus -> PAID`

No real payment gateway is wired in here (the spec calls for `ONLINE`/`CASH`
as a recorded `paymentMethod` + `transactionId`, not gateway integration), so
a successful call marks the payment `PAID` immediately after computing the
split. If you want real gateway verification later, the signature-verification
pattern from the companion `garage-monolithic-jwt` project's Razorpay
integration is a direct template - never trust a client-reported success
on its own.

## Ownership / scoping rules

Every "my own resource" action pulls the acting user's id from the JWT
(`SecurityUtils.getCurrentUserId()`), never from a client-supplied path/body id:

- Customer: own vehicles, own appointments (derived via vehicle ownership),
  own invoices/payments.
- Garage Manager: appointments/job cards/invoices scoped to their own
  `garageId` only - checked explicitly against `manager.getGarage().getGarageId()`,
  not just role membership.
- Mechanic: only appointments/job cards assigned to them.
- Super Admin: unrestricted read access; the only role that can create
  garages/staff or delete users.

## API surface

| Endpoint | Method | Role |
|---|---|---|
| /auth/register | POST | public |
| /auth/register-super-admin | POST | public, but only succeeds once (ever) |
| /auth/register-garage-manager | POST | public - creates a PENDING garage + inactive manager |
| /auth/login | POST | public |
| /users/staff | POST | SUPER_ADMIN |
| /users | GET | SUPER_ADMIN |
| /users/{id} | GET | self, SUPER_ADMIN |
| /users/me | PUT | self |
| /users/{id}/deactivate | PATCH | SUPER_ADMIN |
| /users/{id}/reactivate | PATCH | SUPER_ADMIN |
| /users/{id} | DELETE | SUPER_ADMIN (hard delete - fails on FK if the user has history) |
| /garages | POST | SUPER_ADMIN (starts APPROVED) |
| /garages | GET | public (APPROVED only) |
| /garages/pending | GET | SUPER_ADMIN |
| /garages/{id} | GET | public if APPROVED, else SUPER_ADMIN or the applicant manager |
| /garages/{id} | PUT | SUPER_ADMIN, own GARAGE_MANAGER |
| /garages/{id}/approve | PATCH | SUPER_ADMIN |
| /garages/{id}/reject | PATCH | SUPER_ADMIN |
| /garages/{id} | DELETE | SUPER_ADMIN |
| /garages/my/mechanics | POST | GARAGE_MANAGER (own garage) |
| /garages/my/mechanics | GET | GARAGE_MANAGER (own garage) |
| /garages/my/mechanics/{id}/deactivate | PATCH | GARAGE_MANAGER (own garage's mechanics only) |
| /garages/my/mechanics/{id}/reactivate | PATCH | GARAGE_MANAGER (own garage's mechanics only) |
| /vehicles | POST | CUSTOMER |
| /vehicles/my | GET | CUSTOMER |
| /appointments | POST | CUSTOMER |
| /appointments/my | GET | CUSTOMER |
| /appointments/garage | GET | GARAGE_MANAGER (own garage) |
| /appointments/mechanic | GET | MECHANIC (own) |
| /appointments/{id}/assign-mechanic | PATCH | GARAGE_MANAGER (own garage) |
| /appointments/{id}/cancel | PATCH | CUSTOMER (own) |
| /jobcards/appointment/{appointmentId} | POST | MECHANIC (own assigned) |
| /jobcards/appointment/{appointmentId} | GET | owning CUSTOMER, assigned MECHANIC, own-garage MANAGER, SUPER_ADMIN |
| /jobcards/{id} | GET | owning CUSTOMER, assigned MECHANIC, own-garage MANAGER, SUPER_ADMIN |
| /jobcards/{id}/complete | PATCH | MECHANIC (own) |
| /jobcards/{id}/items | POST | MECHANIC (own) |
| /invoices/jobcard/{id} | GET | owning CUSTOMER, assigned MECHANIC, own-garage MANAGER, SUPER_ADMIN |
| /payments/invoice/{id} | POST | CUSTOMER (owner) |

## Known gaps / things to decide next

- `AppointmentStatus.CANCELLED` is only reachable via the customer-initiated
  cancel endpoint before work starts; there's no manager/mechanic-side
  cancellation flow if, say, a vehicle turns out to be unserviceable.
- Garage `rating` is stored but nothing currently sets/updates it - decide
  whether that's customer-submitted post-service feedback (would need its
  own entity/endpoint) or an admin-set field.
- `GarageRequest` validation requires `commissionPercentage` on every
  create/update call; if you want it optional/defaultable for updates,
  loosen that.
- A rejected garage application has no re-apply path - the manager account
  stays permanently deactivated. If you want applicants to be able to fix
  something and try again, you'd need either a re-submit endpoint or to
  allow `POST /auth/register-garage-manager` again for a previously-rejected
  email (right now it'll fail on the `existsByEmail` duplicate check).
- `PATCH /garages/{id}/approve` and `/reject` aren't guarded against a race
  between two concurrent SUPER_ADMIN calls on the same pending garage - low
  risk in practice (a human reviewing applications), but worth knowing if
  this ever gets automated.
- There's still no self-service "change my own password" endpoint - only
  name/phone/specialization/experience are editable via `PUT /users/me`.
