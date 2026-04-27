# Application Flow

End-to-end user journeys through Kapotiabi.

---

## Main Flow — Car Owner

```mermaid
flowchart TD
    A([Register / Login]) --> B[Add a car]
    B --> C[Create a job request\ndescription + city + deadline]
    C --> D{Wait for offers}
    D --> E[View incoming offers\nfrom repair shops]
    E --> F{Decision}
    F -->|Accept| G[Booking created automatically\nstatus: awaiting_shop]
    F -->|Reject| D
    G --> H{Wait for shop\nconfirmation}
    H --> I[Shop confirms\nstatus: confirmed]
    I --> J[Shop schedules appointment\nstatus: confirmed + scheduledAt set]
    J --> K[Service completed\nstatus: completed]
    K --> L[Leave a review\nrating + comment]
```

---

## Main Flow — Shop Owner

```mermaid
flowchart TD
    A([Register as shop owner]) --> B[Admin reviews shop\nstatus: draft]
    B --> C{Admin decision}
    C -->|Approve| D[Shop goes live\nstatus: active]
    C -->|Reject| E([Shop rejected])
    D --> F[Browse available job requests\nfilter by city / county]
    F --> G[Submit offer\nprice + breakdown + PDF optional]
    G --> H{Wait for car owner}
    H -->|Offer accepted| I[Booking arrives\nstatus: awaiting_shop]
    H -->|Offer rejected| F
    I --> J[Confirm booking\nstatus: confirmed]
    J --> K[Schedule appointment\nset scheduledAt]
    K --> L[Mark complete\nstatus: completed]
```

---

## Guest Estimate Flow

No account required.

```mermaid
flowchart LR
    A([Visitor]) --> B["POST /api/v1/guest-estimates\nplate + description + city"]
    B --> C[Backend matches active shops\nin the given city]
    C --> D[Returns price range\nper matching shop]
    D --> E([Visitor sees estimates])
```

---

## Booking Status Lifecycle

```mermaid
stateDiagram-v2
    [*] --> awaiting_shop : Car owner accepts offer
    awaiting_shop --> confirmed : Shop confirms
    awaiting_shop --> cancelled : Either party cancels
    confirmed --> completed : Shop marks complete
    confirmed --> cancelled : Either party cancels
    completed --> [*]
    cancelled --> [*]
```

---

## Offer Status Lifecycle

```mermaid
stateDiagram-v2
    [*] --> pending : Shop submits offer
    pending --> accepted : Car owner accepts
    pending --> rejected : Car owner rejects
    accepted --> [*]
    rejected --> [*]
```

---

## Admin Flow

```mermaid
flowchart TD
    A([Admin logs in]) --> B["GET /api/v1/admin/shops\nfilter by status=draft"]
    B --> C{Review shop profile}
    C -->|Approve| D["PATCH /api/v1/admin/shops/{id}/status\naction: approve"]
    C -->|Reject| E["PATCH /api/v1/admin/shops/{id}/status\naction: reject"]
    D --> F([Shop becomes visible\nstatus: active])
    E --> G([Shop hidden\nstatus: rejected])
```
