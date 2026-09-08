# Design
## Purpose and Scope
Simple Invoice Platform is a REST API for managing customers, products and invoices.

The project models the core lifecycle of an invoice. Additional features such as payment processing are outside the scope of this current version.

## Domain Model
### Customer
A customer contains:
* ID
* name

A customer can be associated with multiple invoices.

### Product
A product contains:
* ID
* name
* price

### Invoice
An invoice contains:
* ID
* customer ID
* customer name
* issue date
* status
* invoice lines
* total amount

Each invoice belongs to exactly one customer.

Once an invoice has been created, its contents are immutable except for its status.

### Invoice Line
An invoice line contains:
* product ID
* product name
* quantity
* unit price

Invoice lines are embedded within an invoice. They cannot exist independently and cannot be modified independently after the invoice has been created.

### User
A user contains:
* ID
* username
* password
* user role

The application currently supports two roles:
* `USER`
* `ADMIN`

## Snapshot Strategy
Invoices represent historical records, so they must not change when the current customer or product data changes.

When an invoice is created, the application stores a snapshot of:
* the customer's name
* each product's name
* each product's unit price

The invoice total is calculated from its invoice lines at creation time and stored in the invoice.

Because the information required to interpret an invoice is stored directly within it, customers and products may later be updated or deleted without changing historical invoices.

The stored customer and product IDs preserve a reference to the entities from which the snapshot originated, but historical invoices do not depend on those entities continuing to exist.

## Invoice Lifecycle
An invoice begins in the `CREATED` state.

The normal lifecycle is:
`CREATED -> VALIDATED -> SENT -> PAID`

An invoice may also transition to `CANCELED` from:
* `CREATED`
* `VALIDATED`
* `SENT`

`PAID` and `CANCELED` are terminal states. No other status transitions are permitted.

## Security
Authentication is based on username and password credentials.

After successful authentication, the application issues a JWT. This token is used for subsequent authenticated requests.

Authorization is role-based using the `USER` and `ADMIN` roles.

Passwords are stored as encoded values rather than plaintext.

An administrator account can be initialized from the application's configuration.

Usernames are unique. This constraint is enforced both by application-level validation and a unique MongoDB index.

## Persistence
MongoDB is used as the persistence layer.

Customers, products, invoices and users are stored as separate collections.

Invoice lines are embedded within their parent invoice because they are part of the invoice aggregate and have no independent lifecycle.

Historical invoice data is intentionally denormalized through snapshots. This is so that invoices remain stable even when related customer or product records change.

## Key Invariants
The application maintains the following invariants:
* usernames are unique
* each invoice belongs to exactly one customer
* an invoice must contain at least one invoice line
* each invoice line refers to a product that exists when the invoice is created
* quantities must be positive
* product prices must be positive
* invoice contents cannot be changed after creation, except for the invoice status
* invoice status changes must follow the defined lifecycle
* historical customer names, product names and product prices do not change after invoice creation
* invoice totals are derived from the invoice lines when the invoice is created
