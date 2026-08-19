# Event Ticket App

An app to book and manage tickets to events built with Spring Boot 4, PostgreSQL, and Keycloak.

## Getting Started

### Welcome

Have you ever looked at some code and wondered _what on earth were they thinking?_

Why did you code it in _that way_?

Why not use library x? Why not use pattern y?

I think I've found a fix for that...

Over the next 8-12 weeks we're going to build an "Event Ticket Platform". This app let's organizers create events and sell tickets, event goers buy those tickets and the system handles the whole ticket QR code scanning at the event.

This is usually where I'd show you clips of the fanciest bits the finished app, but here's the thing -- the app's not built yet.

We've got a project brief, which we'll walk through in the next section, but that's it -- but fear not, that's what we want!

This build is going to be a bit more raw than my previous builds, as I'll be taking you through the build, as I build it.

No safety net, we're all in.

Without the benefit of foresight, We'll likely have to rework some stuff as we go, but as a result you'll get to see my real, as-it-happens, build thought process -- the good, the bad and the ugly.

#### Build Plan

Here's the plan for this build. It's likely to change as we learn more about the domain, but here's today's understanding:

1. System Design - Analyze the domain, user personas, UI Design and domain modelling That's this video.
2. Design REST API and the application's architecture
3. Project Setup, including Security
4. Domain Implementation
5. Event Creation & Management
6. Ticket Sale & Purchase
7. Ticket Validation
8. Sales Reporting

#### Prerequisites

That's the plan, we'll see how we get on, so let's cover the prereqs for this build.

This is going to be in the intermediate build territory, and as we're on a time limit we'll need to focus on the build and can't make too many diversions cover theory in-depth.

As a result, you should already be comfortable with:

- Java
- Spring Boot
- Spring Security

Have a basic understanding of:

- OAuth2 and OpenID connect
- React + TanStack Start + React Query + npm

#### Source Code

This build will be focused on the Spring Boot backend, and the source code is in the `backend` directory of this repository.

As an app isn't complete without a UI, I also built a React frontend using TanStack Start (with React Query for data fetching) for this build. You'll find it in the `frontend` directory.

I'll not include how to code the frontend app in this series.

Right, with that covered, let's jump into system design.

### Project Brief

In this lesson, we'll explore the project brief for our event ticketing platform, aiming to understand the requirements of the system we are to build.

#### Project Overview

Let's break down the main components of our event ticketing system.

Our system needs to handle event creation, ticket sales, sales monitoring, and ticket validation -- the complete event management lifecycle.

The platform will serve three types of users:

1. Event _organizers_
2. Event _attendees_
3. Event _staff_

Each user type has their own needs, and way of using the system.

#### User Story Analysis

##### Event Creation Requirements

![User Story 1](./images/1-3-user-story-1.webp)

The event creation story focuses on organizers setting up new events.

We'll need to design a robust data model to store event details including:

- Name, date, time, and venue
- Multiple ticket types with varying prices
- Ticket quantity limits per type

The system must maintain data integrity to prevent issues like duplicate events or invalid ticket configurations.

##### Ticket Purchase Flow

![User Story 2](./images/1-3-user-story-2.webp)

The ticket purchase story requires a user-friendly search and selection process.

We'll need to implement:

- A search mechanism for events
- A clear display of available ticket types
- A secure payment processing system
- Real-time inventory management to prevent overselling

##### Sales Management Features

![User Story 3](./images/1-3-user-story-3.webp)

The sales management story requires comprehensive tracking capabilities.

This involves creating:

- A dashboard for sales metrics
- Secure storage of purchaser information
- Automated sales control based on quantity and date rules

##### Ticket Validation System

![User Story 4](./images/1-3-user-story-4.webp)

The validation story focuses on entry management at events.

Key technical considerations include:

- QR code generation and scanning functionality
- Ticket status verification
- Prevention of duplicate ticket use
- Fallback manual entry system

#### Technical Implementation Notes

We'll need to create RESTful endpoints for each major function:

- Event management APIs
- Ticket purchase APIs
- Sales monitoring APIs
- Ticket validation APIs

#### Summary

- Platform manages complete event lifecycle from creation through validation
- System serves three user types: organizers, attendees, and staff members
- Features include event setup, ticket sales, monitoring, and entry validation

## User Interface Design

### User Personas Summary

Let's summarize the user personas which model users of our system.

Please note that the following user personas were generated by an LLM based on our project brief, so we can be confident that these people are fictional, and their descriptions un-biased as possible.

#### Organizers

##### Corporate Event Manager

![Corporate Event Manager](./images/corporate-event-manager.webp)

**Working Name**: Corporate Event Manager

**Primary Goal**: To represent company brand values through professional events while tracking attendee analytics for business development.

**Key Challenge**: Needs comprehensive analytics and reporting features to justify event ROI to executives while ensuring seamless attendee experience.

**Distinguishing Characteristics**:

- Data-driven decision maker who values detailed analytics
- Brand-conscious and needs customization options
- Security and privacy focused due to handling customer data

**Usage Context**: Uses the platform on both desktop and tablet devices, often preparing reports in the office but needing mobile access during events to monitor real-time metrics and address issues.

##### Event Planning Professional

![Event Planning Professional](./images/event-planning-professional.webp)

**Working Name**: Event Planning Professional

**Primary Goal**: To efficiently create and manage multiple events with different ticket tiers while maximizing attendance and revenue.

**Key Challenge**: Juggling numerous events simultaneously while ensuring accurate ticket inventory and preventing overselling.

**Distinguishing Characteristics**:

- Detail-oriented with strong organizational skills
- Tech-savvy but values intuitive interfaces
- Time-conscious and appreciates automation

**Usage Context**: Uses the platform primarily on a laptop while at the office or remotely, often needs to make quick updates between meetings with clients and venue representatives.

##### Part-Time Event Organizer

![Part Time Event Organizer](./images/part-time-event-organizer.webp)

**Working Name**: Part-Time Event Organizer

**Primary Goal**: To create occasional events with minimal effort while maintaining a professional appearance to attendees.

**Key Challenge**: Limited technical expertise and time to dedicate to learning complex systems while needing to appear professionally competent.

**Distinguishing Characteristics**:

- Prefers simple, guided interfaces with templates
- Values mobile accessibility for on-the-go management
- Prioritizes customer-facing aesthetics over backend complexity

**Usage Context**: Uses the platform primarily on mobile devices during free moments between other responsibilities, often creating and managing events during evenings and weekends.

#### Attendees

##### Busy Parent Event Attendee

![Busy Parent Attendee](./images/busy-parent-event-goer.webp)

**Working Name**: Busy Parent Event Attendee

**Primary Goal**: To reliably secure tickets for family-friendly events well in advance with clear information about venue facilities and accessibility.

**Key Challenge**: Coordinating tickets for multiple family members while ensuring all necessary information is available for planning the family outing.
**Distinguishing Characteristics**:

- Careful planner who books events weeks or months in advance
- Values clear information about venue accessibility and facilities
- Appreciates email confirmations and reminders

**Usage Context**: Primarily uses tablet or desktop devices during evening hours after children are in bed, often comparing multiple events before making a purchase decision, and may print physical tickets as backup.

##### Young Event-Goer

![Young Event Goer](./images/young-event-goer.webp)

**Working Name**: Young Event-Goer

**Primary Goal**: To easily discover, purchase, and access tickets for trendy events without complications or hidden fees.

**Key Challenge**: Finding affordable tickets to popular events and ensuring ticket validation works smoothly to avoid entry issues.

**Distinguishing Characteristics**:

- Digital native who expects intuitive mobile experiences
- Price-sensitive but willing to pay for unique experiences
- Often purchases tickets at the last minute

**Usage Context**: Uses exclusively mobile devices to purchase tickets, often while commuting or during short breaks, and prefers digital tickets stored in mobile wallet apps for easy access.

##### Corporate Networking Attendee

![Corporate Networking Attendee](./images/corporate-neworking-attendee.webp)

**Working Name**: Corporate Networking Attendee

**Primary Goal**: To efficiently register for professional events and conferences with the ability to expense tickets and receive proper documentation.

**Key Challenge**: Needs detailed receipts and event information for expense reporting and calendar integration for busy schedule management.

**Distinguishing Characteristics**:

- Values streamlined checkout processes with minimal steps
- Requires detailed receipts for corporate expense reporting
- Appreciates calendar integration and professional reminders

**Usage Context**: Uses both desktop (during office hours) and mobile devices (while traveling) to purchase tickets, often needs to buy multiple tickets for colleagues, and requires seamless integration with professional tools like calendar and expense apps.

#### Staff

#### Event Staff Coordinator

![Event Staff Organizer](./images/event-staff-coordinator.webp)

**Working Name**: Event Staff Coordinator

**Primary Goal**: To efficiently manage entry validation processes for large events with multiple entry points and staff members.

**Key Challenge**: Ensuring consistent and accurate ticket validation across different entry points while handling high-volume entry during peak times.

**Distinguishing Characteristics**:

- Process-oriented with focus on security and accuracy
- Comfortable with technology but needs reliable, simple tools for team
- Values speed and reliability over complex features

**Usage Context**: Primarily uses the admin interface before events for setup and staff training, then uses mobile validation tools during events while moving between entry points to supervise staff.

#### Entry-Level Event Staff

![Entry Level Staff](./images/entry-level-event-staff.webp)

**Working Name**: Entry-Level Event Staff

**Primary Goal**: To quickly and accurately validate tickets at event entry points without causing delays or errors.

**Key Challenge**: Must handle high-pressure entry situations with minimal training while maintaining positive customer interactions despite potential technical issues.

**Distinguishing Characteristics**:

- Limited technical expertise requires intuitive interfaces
- May be working first job or temporary position
- Values clear instructions and error messages

**Usage Context**: Uses primarily the mobile ticket scanning application during event hours, often in challenging environments (poor lighting, noisy, crowded) requiring quick and clear validation feedback.

#### Summary

- Explored user personas representing organizers, attendees and staff
- These user personas will influence how the system is designed

### User Journey

Let's explore how these user personas may interact with our application.

I've used some imagination here to fill in the gaps, however we should still be able to learn a thing or two from these user journeys!

> [!TIP]
> Paste this MermaidJS code into the live editor on their site to see it come to life!

#### Organizers

##### Corporate Event Manager

```mermaid
journey
    title Corporate Event Manager - Platform User Journey
    section Event Setup
        Access platform: 5: Sarah Lee
        Configure event details: 4
        Set ticket types & pricing: 5
        Customize branding: 3
    section Pre-Event Management
        Monitor ticket sales: 5
        Review analytics dashboard: 5
        Export attendee data: 4
        Configure staff access: 3
    section Event Day Operations
        Brief staff on scanning process: 4
        Monitor entry validation: 5
        Handle scanning issues: 3
        Track real-time attendance: 4
    section Post-Event
        Export final attendance data: 5
        Generate sales report: 5
        Review validation metrics: 4
```

##### Event Planning Professional

```mermaid
journey
    title Event Planning Professional - User Journey
    section Event Setup
        Login to platform: 5: Priya
        Create new event: 4
        Configure ticket types: 3
        Set pricing tiers: 4
        Review event details: 5
        Publish event: 4
    section Sales Management
        Monitor real-time dashboard: 5
        Track ticket inventory: 4
        Export sales reports: 3
        Adjust ticket pricing: 4
    section Event Preparation
        Generate QR codes: 5
        Brief staff on check-in process: 4
        Test scanning equipment: 5
    section Event Day
        Initialize check-in system: 4
        Monitor entry analytics: 5
        Handle ticket exceptions: 3
        Generate final reports: 4
```

##### Part-Time Event Organizer

```mermaid
journey
    title Part-Time Event Organizer - User Journey
    section Event Creation
        Access platform during lunch break: 4: Marcus
        Create new event template: 3
        Input event details: 4
        Configure ticket types/prices: 5
        Preview event listing: 4
    section Pre-Event Management
        Monitor ticket sales on mobile: 5
        Check metrics during breaks: 4
        Update event details as needed: 3
        Share on social media: 5
    section Event Day Setup
        Generate QR scanner link: 4
        Brief venue staff on scanning: 3
        Test scanner functionality: 5
    section Event Execution
        Monitor entry scanning: 4
        Track real-time attendance: 5
        Handle scanning issues: 3
    section Post-Event
        Export attendance data: 4
        Review sales metrics: 5
        Archive event details: 4
```

#### Attendees

##### Busy Parent Event Attendee

```mermaid
journey
    title Family Event Planner - Sarah Mitchell's Journey
    section Research & Discovery
        Browse family events: 4: Sarah
        Read venue details & reviews: 5
        Check family calendar: 5
        Coordinate with spouse: 3
    section Planning
        Compare ticket options: 4
        Check venue accessibility: 5
        Review child age policies: 4
    section Purchase
        Select family tickets: 5
        Enter payment details: 4
        Receive confirmation: 5
    section Pre-Event Prep
        Add to family calendar: 5
        Save digital tickets: 4
        Print backup tickets: 5
        Pack family essentials: 4
    section Event Day
        Travel to venue: 3
        Park & navigate to entrance: 4
        Present tickets: 5
        Enter venue: 5
```

##### Young Event-Goer

```mermaid
journey
    title Young Event Goer - Event Ticket Platform Journey
    section Discovery
        Browse events on morning commute: 5: Alex
        Check ticket prices: 4
        Share event with friends: 5
        Coordinate group attendance: 3
    section Purchase
        Select ticket quantity: 5
        Review fees and total: 2
        Complete mobile payment: 4
        Receive digital tickets: 5
    section Pre-Event
        Store tickets in digital wallet: 4
        Share tickets with friends: 3
        Get event reminder: 5
    section Attendance
        Arrive at venue: 4
        Quick QR code scan: 5
        Enter event: 5
    section Post-Event
        Share experience on social: 4
        Follow venue for future events: 5
```

##### Corporate Networking Attendee

```mermaid
journey
    title Corporate Event Attendee - User Journey
    section Discovery
        Search business events: 4: Karim
        Review event details: 5
        Check calendar availability: 4
    section Registration
        Select ticket quantity: 5
        Input team member details: 3
        Corporate payment/billing: 4
        Receive confirmation email: 5
    section Pre-Event
        Add to Outlook calendar: 5
        Download mobile tickets: 4
        Save event details: 3
    section Event Day
        Access mobile tickets: 5
        Present QR code: 4
        Enter venue: 5
    section Post-Event
        Download receipt: 4
        Submit expense report: 3
```

#### Event Staff Coordinator

```mermaid
journey
    title Event Staff Coordinator - User Journey
    section Pre-Event Setup
        Access event details: 5: Jordan
        Review ticket types/rules: 4
        Configure scanning devices: 3
        Brief staff on procedures: 5
    section Entry Point Setup
        Test scanning equipment: 4
        Position staff members: 5
        Verify radio communications: 4
        Set up backup validation: 3
    section Event Operations
        Monitor entry flow: 5
        Scan attendee tickets: 4
        Handle validation issues: 3
        Coordinate between gates: 4
    section Post-Event
        Generate entry reports: 4
        Debrief with staff: 5
        Document issues: 4
        Submit final counts: 5
```

#### Entry-Level Event Staff

```mermaid
journey
    title Entry-Level Event Staff - Ticket Validation Journey
    section Pre-Event Setup
        Arrive at venue: 4: Event Staff
        Attend briefing: 3
        Test scanning equipment: 4
        Set up entry lanes: 5
    section Entry Rush Preparation
        Review event details: 4
        Position at assigned station: 5
        Open scanning app: 4
        Test scan sample ticket: 3
    section Main Entry Period
        Greet attendee: 5
        Request ticket: 4
        Scan QR code: 3
        Verify ticket status: 4
        Direct to entrance: 5
    section Issue Resolution
        Identify scan problems: 2
        Try manual entry: 3
        Escalate to supervisor: 2
        Document issues: 3
    section End of Shift
        Record final statistics: 4
        Report technical issues: 3
        Clean up station: 5
        Complete shift log: 4
```

#### Summary

- Explored user journeys for organizers, attendees and staff
- The user journeys offer insight into how different users may interact with the system
- The user journeys will influence how the system is designed and implemented

### User Interface Design

Let's explore the initial design of the user interface.

#### Sign up and Logging

All users will need to sign up and log in, although we may not go as far as implementing a custom sign up and log in page, I've included them for completeness.

##### Sign Up Page

![Sign Up Page](./images/ui-design-account-page-v1.webp)

##### Sign Up Confirmation Page

![Sign Up Confirmation Page](./images/ui-design-signup-confirmation-page-v1.webp)

##### Log In Page

![Log In Page](./images/ui-design-login-page-v1.webp)

#### Organizer Flow

Here's how I imagined an organizer user type to interact with our application.

##### Organizer Landing Page

![Organizer Landing Page](./images/ui-design-organizer-landing-page-v1.webp)

##### Create & Edit Event Page

![Create Edit Event Page](./images/ui-design-create-event-page-v1.webp)

##### Dashboard Landing Page

![Dashboard Landing Page](./images/ui-design-dashboard-landing-page-v1.webp)

##### Dashboard Events Page

![Dashboard Events Page](./images/ui-design-your-events-page-v1.webp)

##### Dashboard Ticket Sales Page

![Dashboard Ticket Sales Page](./images/ui-design-your-ticket-sales-page-v1.webp)

##### Dashboard Ticket Types Page

![Dashboard Ticket Types Page](./images/ui-design-dashboard-ticket-types-page-v1.webp)

##### Dashboard Reports Page

![Dashboard Reports Page](./images/ui-design-dashboard-reports-page-v1.webp)

#### Attendee Flow

Here's how I imagine attendees interacting with our system:

##### Attendee Landing Page

![Attendee Landing Page](./images/ui-design-attendee-landing-page-v1.webp)

##### Event Details Page

![Event Details Page](./images/ui-design-event-details-page-v1.webp)

##### Purchase Tickets Page

![Purchase Tickets Page](./images/ui-design-purchase-tickets-page-v1.webp)

##### Purchase Tickets Confirmation Page

![](./images/ui-design-purchase-confirmation-page-v1.webp)

##### Dashboard Purchased Tickets

![Purchased Tickets Page](./images/ui-design-dashbaord-purchased-tickets-page-v1.webp)

##### Ticket QR Code Page

![Ticket QR Code Page](./images/ui-design-qr-code-page-v1.webp)

#### Staff Flow

Finally, these are the pages I imagine the staff using:

##### Select Event Page

![Event Select Page](./images/ui-design-staff-events-page-v1.webp)

##### Ticket Scanning Page

![Ticket Scanning Page](./images/ui-design-scan-qr-code-page-v1.webp)

#### Summary

- Designed the first iteration of the user interface
- Designs have been influenced by user stories, personas, and journeys

## Domain Modelling

### Domain Model Summary

Let's explore the initial domain model described in the project brief.

#### Review the Domain Diagram

After spending a bit of time analyzing the project brief, using a variation on the "noun-verb analysis" technique, I produced the following domain diagram:

![Domain Model](./images/2-11-domain-diagram.webp)

Or if you prefer the MermaidJs version:

```mermaid
classDiagram
    class Event {
        id
        name
        date
        time
        venue
        salesEndDate
    }

    class TicketType {
        id
        name
        price
        totalAvailable
    }

    class Ticket {
        id
        status
        createdDateTime
    }

    class QrCode {
        id
        generatedDateTime
        status
    }

    class Organizer {
    }

    class Attendee {
    }

    class Staff {
    }

    class User {
        id
        name
        email
    }

    class TicketValidation {
        id
        status
        validationDateTime
        validationMethod
    }

    class TicketSale {
        id
        status
        purchaseDateTime
    }

    Organizer "is a" --> User
    Attendee "is a" --> User
    Staff "is a" --> User
    Organizer "organizes" --> Event
    User "purchases" --> Ticket
    User "validates" --> TicketValidation
    Event "offers" --> TicketType
    TicketType "categorizes" --> Ticket
    Ticket "validates" --> TicketValidation
    Ticket "purchased" --> TicketSale
    Ticket "has a" --> QrCode
    Staff "works at" --> Event
    Attendee "attends" --> Event
```

#### Explore the Initial Domain

From the domain model, I've identified the following domain objects.

Note that this is just our initial understanding, which we will refine over time.

##### Event

The `Event` class represents a planned gathering with properties like:

- `id` - A unique identifier
- `name` - The event's title
- `date` and `time` - When the event occurs
- `venue` - Where the event takes place
- `salesEndDate` - When ticket sales stop

##### Ticket Type

The `TicketType` class defines different categories of tickets available for an event:

- `id` - A unique identifier
- `name` - The type of ticket (e.g., "VIP", "Standard")
- `price` - Cost of the ticket
- `totalAvailable` - Maximum number that can be sold

##### Ticket

The `Ticket` represents an individual purchase:

- `id` - A unique identifier
- `status` - The status of the ticket, perhaps it's been cancelled?
- `createdDateTime` - The date and time the ticket was created

##### QR Code

The `QrCode` represents the code used to represent the ticket's information, present on each ticket:

- `id` - A unique identifier
- `generatedDateTime` - The time and date the QR code was generated
- `status` - The status of the QR code -- is it still valid?

##### User

The `User` class represents people interacting with our system, where `Organizer`, `Attendee` and `Staff` are different types of `User`:

- `id` - A unique identifier
- `name` - Person's name
- `email` - Contact information

##### Ticket Validation

Finally, `TicketValidation` is for event entry management:

- `id` - A unique identifier
- `validationTime` - When validation occurred
- `validationMethod` - How it was validated
- `status` - Result of validation

We'll evolve this domain diagram further as our design progresses.

#### Summary

- Events can have multiple ticket types, each with their own pricing and availability
- Users can act as organizers, event goers, and staff
- Tickets include QR codes for validation at event entry
- Ticket validation ensures proper event access control

### Domain Modelling Cardinality

Let's now add cardinality information to the class diagram.

#### Add Cardinality to the Class Diagram

Here's what I came to in Miro:

![Domain Diagram with Cardinality](./images/5-3-class-diagram-cardinality.webp)

#### Formalize the Class Diagram

Here's the class diagram formalized as MermaidJs:

```mermaid
classDiagram
    class Organizer {
    }

    class Attendee {
    }

    class Staff {
    }

    class User {
        id
        name
        email
    }

    class Event {
        id
        name
        start
        end
        salesEndDate
        salesEndDate
        status
    }

    class Venue {
        id
        name
        addressLine1
        addressLine2
        city
        postalCode
        country
        latitude
        longitude
        capacity
        accessibilityInfo
    }

    class TicketType {
        id
        name
        price
        totalAvailable
    }

    class Ticket {
        id
        status
        createdDateTime
    }

    class QrCode {
        id
        generatedTime
        status
    }

    class TicketValidation {
        id
        status
        validationTime
        validationMethod
    }

    Organizer --|> User
    Attendee --|> User
    Staff --|> User
    Organizer "1" --* "0..*" Event: organizes
    Attendee --o Event: attends
    Staff --o Event: works at
    User "1" --* "0..*" Ticket: purchases
    Venue "1" --* "0..*" Event: hosts
    Event "1" --* "0..*" TicketType: offers
    TicketType "1" --* "0..*" Ticket: categorizes
    Ticket "1" --* "0..*" TicketValidation: validated
    Ticket "1" --* "1..*" QrCode : has
```

#### Summary

- Added cardinality information to the application's class diagram
- Extracted `venue` out of `Event` into its own `Venue` entity, so a venue can be reused across many events (one `Venue` hosts `0..*` events)

### Domain Modelling Data Types

Let's now add type information to our class diagram.

#### Add Type Information to the Class Diagram

Here's what I came to in Miro:

![Domain Diagram with Type Information](./images/5-3-class-diagram-cardinality.webp)

#### Formalize the Class Diagram

Here's the class diagram formalized as MermaidJs.

I've went with the Java-style way of declaring types in the formalized version:

```mermaid
classDiagram
    class Organizer {
    }

    class Attendee {
    }

    class Staff {
    }

    class User {
        UUID id
        String name
        String email
    }

    class Event {
        UUID id
        String name
        LocalDateTime start
        LocalDateTime end
        LocalDateTime salesEndDate
        LocalDateTime salesEndDate
        EventStatusEnum status
    }

    class Venue {
        UUID id
        String name
        String addressLine1
        String addressLine2
        String city
        String postalCode
        String country
        Double latitude
        Double longitude
        Integer capacity
        String accessibilityInfo
    }

    class TicketType {
        UUID id
        String name
        Double price
        Integer totalAvailable
    }

    class Ticket {
        UUID: id
        TicketStatusEnum status
        LocalDateTime createdDateTime
    }

    class QrCode {
        UUID id
        LocalDateTime generatedTime
        QrCodeStatusEnum status
    }

    class TicketValidation {
        UUID id
        TicketValidationStatusEnum status
        LocalDateTime validationTime
        TicketValidationMethodEnum validationMethod
    }

    Organizer --|> User
    Attendee --|> User
    Staff --|> User
    Organizer "1" --* "0..*" Event: organizes
    Attendee --o Event: attends
    Staff --o Event: works at
    User "1" --* "0..*" Ticket: purchases
    Venue "1" --* "0..*" Event: hosts
    Event "1" --* "0..*" TicketType: offers
    TicketType "1" --* "0..*" Ticket: categorizes
    Ticket "1" --* "0..*" TicketValidation: validated
    Ticket "1" --* "1..*" QrCode : has
```

#### Summary

- Added data type information to the application's class diagram
- Typed the new `Venue` entity, including `Double latitude`/`longitude` to support future geo/map search

### Data Types Required Optional

Let's now define each property as either required or optional.

#### Add Required/Optional Information to the Class Diagram

Here's what I came to in Miro:

![Domain Diagram with Type Information](./images/5-3-class-diagram-cardinality.webp)

#### Formalize the Class Diagram

Here's the class diagram formalized as MermaidJs.

Here I've assumed that all properties are required, unless they have a question mark `?` suffix. e.g. `String` is required, where's `String?` is optional.

This saves us writing a whole bunch of asterisks in our class diagram.

```mermaid
classDiagram
    class Organizer {
    }

    class Attendee {
    }

    class Staff {
    }

    class User {
        UUID id
        String name
        String email
    }

    class Event {
        UUID id
        String name
        LocalDateTime start
        LocalDateTime end
        LocalDateTime? salesStartDate
        LocalDateTime? salesEndDate
        EventStatusEnum status
    }

    class Venue {
        UUID id
        String name
        String addressLine1
        String? addressLine2
        String city
        String postalCode
        String country
        Double? latitude
        Double? longitude
        Integer? capacity
        String? accessibilityInfo
    }

    class TicketType {
        UUID id
        String name
        Double price
        Integer? totalAvailable
    }

    class Ticket {
        UUID: id
        TicketStatusEnum status
        LocalDateTime createdDateTime
    }

    class QrCode {
        UUID id
        LocalDateTime generatedTime
        QrCodeStatusEnum status
    }

    class TicketValidation {
        UUID id
        TicketValidationStatusEnum status
        LocalDateTime validationTime
        TicketValidationMethodEnum validationMethod
    }

    Organizer --|> User
    Attendee --|> User
    Staff --|> User
    Organizer "1" --* "0..*" Event: organizes
    Attendee --o Event: attends
    Staff --o Event: works at
    User "1" --* "0..*" Ticket: purchases
    Venue "1" --* "0..*" Event: hosts
    Event "1" --* "0..*" TicketType: offers
    TicketType "1" --* "0..*" Ticket: categorizes
    Ticket "1" --* "0..*" TicketValidation: validated
    Ticket "1" --* "1..*" QrCode : has
```

#### Summary

- Added optional / required information to the application's class diagram
- `Venue.name`, `addressLine1`, `city`, `postalCode`, and `country` are required; `addressLine2`, `latitude`/`longitude`, `capacity`, and `accessibilityInfo` are optional so a venue can be created before its geo-coordinates or capacity are known
- An `Event` always requires a `Venue` (the relationship itself is mandatory, even though several `Venue` fields are optional)

## System Design

### Rest Api Design Organizer

Let's analyze the organizer user interface flow in order to identify the REST API endpoints.

#### Analyze the Organizer Flow

Here's the REST API endpoints I've identified from the first round of analysis:

```markdown
## Create Event

POST /api/v1/events
Request Body: Event

## List Events

GET /api/v1/events

## Retrieve Event

GET /api/v1/events/{event_id}

## Update Event

PUT /api/v1/events/{event_id}
Request Body: Event

## Delete Event

DELETE /api/v1/events/{event_id}

## List Ticket Sales

GET /api/v1/events/{event_id}/tickets

## Retrieve Ticket Sale

GET /api/v1/events/{event_id}/tickets/tickets/{ticket_id}

## Partial Update

PATCH /api/v1/events/{event_id}/tickets
Request Body: Partial Event

## List Ticket Type

GET /api/v1/events/{event_id}/ticket-types

## Retrieve Ticket Type

GET /api/v1/events/{event_id}/ticket-types/{ticket_type_id}

## Delete Ticket Type

DELETE /api/v1/events/{event_id}/ticket-types/{ticket_type_id}

## Partial Update Ticket Type

PATCH GET /api/v1/events/{event_id}/ticket-types/{ticket_type_id}
Request Body: Partial Ticket Type

## TODO: Dedicated endpoint for report data
```

#### Summary

- Identified multiple REST API endpoints from the organizer UI Flow

### Rest Api Design Attendee Flow

Let's analyze the attendee UI flow, further refining our REST API design.

#### Analyze the Attendee Flow

```markdown
## Create Event

POST /api/v1/events
Request Body: Event

## List Events

GET /api/v1/events

## Retrieve Event

GET /api/v1/events/{event_id}

## Update Event

PUT /api/v1/events/{event_id}
Request Body: Event

## Delete Event

DELETE /api/v1/events/{event_id}

## List Ticket Sales

GET /api/v1/events/{event_id}/tickets

## Retrieve Ticket Sale

GET /api/v1/events/{event_id}/tickets/tickets/{ticket_id}

## Partial Update Ticket

PATCH /api/v1/events/{event_id}/tickets
Request Body: Partial Ticket

## List Ticket Type

GET /api/v1/events/{event_id}/ticket-types

## Retrieve Ticket Type

GET /api/v1/events/{event_id}/ticket-types/{ticket_type_id}

## Delete Ticket Type

DELETE /api/v1/events/{event_id}/ticket-types/{ticket_type_id}

## Partial Update Ticket Type

PATCH GET /api/v1/events/{event_id}/ticket-types/{ticket_type_id}
Request Body: Partial Ticket Type

## Search Published Events

GET /api/v1/published-events

## Retrieve Published Event

GET /api/v1/published-event/{published_event_id}

## Purchase Ticket

POST /api/v1/published-event/{published_event_id}/ticket-types/{ticket_types_id}

## List Tickets (for user)

GET /api/v1/tickets

## Retrieve Ticket (for user)

GET /api/v1/tickets/{ticket_id}

## Retrieve Ticket QR Code

GET /api/v1/tickets/{ticket_id}/qr-codes

## TODO: Dedicated endpoint for report data
```

#### Summary

- Identified several more REST API endpoints from the attendee UI Flow

### Rest Api Design Staff Flow

Let's analyze the staff UI flow, attempting to refine our understanding of the REST API we are to build.

#### Analyze the Staff Flow

Here's what I've come up with:

```markdown
## Create Event

POST /api/v1/events
Request Body: Event

## List Events

GET /api/v1/events

## Retrieve Event

GET /api/v1/events/{event_id}

## Update Event

PUT /api/v1/events/{event_id}
Request Body: Event

## Delete Event

DELETE /api/v1/events/{event_id}

## Validate Ticket

POST /api/v1/events/{event_id}/ticket-validations

## List Ticket Validations

GET /api/v1/events/{event_id}/ticket-validations

## List Ticket Sales

GET /api/v1/events/{event_id}/tickets

## Retrieve Ticket Sale

GET /api/v1/events/{event_id}/tickets/tickets/{ticket_id}

## Partial Update Ticket

PATCH /api/v1/events/{event_id}/tickets
Request Body: Partial Ticket

## List Ticket Type

GET /api/v1/events/{event_id}/ticket-types

## Retrieve Ticket Type

GET /api/v1/events/{event_id}/ticket-types/{ticket_type_id}

## Delete Ticket Type

DELETE /api/v1/events/{event_id}/ticket-types/{ticket_type_id}

## Partial Update Ticket Type

PATCH GET /api/v1/events/{event_id}/ticket-types/{ticket_type_id}
Request Body: Partial Ticket Type

## Search Published Events

GET /api/v1/published-events

## Retrieve Published Event

GET /api/v1/published-event/{published_event_id}

## Purchase Ticket

POST /api/v1/published-event/{published_event_id}/ticket-types/{ticket_types_id}

## List Tickets (for user)

GET /api/v1/tickets

## Retrieve Ticket (for user)

GET /api/v1/tickets/{ticket_id}

## Retrieve Ticket QR Code

GET /api/v1/tickets/{ticket_id}/qr-codes

## TODO: Dedicated endpoint for report data
```

#### Summary

- Identified several more REST API endpoints from the staff UI Flow
- Completed the initial REST API Design

### Architecture Design

We've learned a lot about the application we are to build, let's use this knowledge to design our application's architecture.

#### Design the Architecture

![Architecture Design](./images/6-5-architecture-design.webp)

Based on the functionality we've captured, we'll need a few components:

- Spring Boot app -- The backend of our application, exposing a REST API
- React App (TanStack Start + React Query) -- The frontend of our application, which calls the REST API
- Keycloak -- Our auth server, handling authentication and authorization

Here's the mermaid diagram:

```mermaid
flowchart LR
    A[Event Ticket App</br>Frontend</br><< React + TanStack Start >>]
    B[Event Ticket App</br>Backend</br><< Spring Boot >>]
    C[Database</br><< PostgreSQL >>]
    A --- B
    B --- C
```

#### Summary

- Our architecture includes a Spring Boot backend, a React + TanStack Start frontend (using React Query), PostgreSQL database, and a Keycloak server

## Project Setup

### New Project

Let's create a new Spring Boot project that will serve as the foundation for our event ticket platform. We'll use the Spring Initializer to set up our project with all the necessary dependencies and configurations we'll need.

#### Set Up the Project

The Spring Initializer (start.spring.io) helps us create a new Spring Boot project with our chosen configuration.

Let's select these project settings:

- Build Tool: Apache Maven
- Language: Java
- Spring Boot Version: 4.1.1 (Latest stable release)
- Java Version: 25 (Latest LTS)
- Packaging: JAR

For our project metadata:

- Group: `com.devtiro`
- Artifact: `tickets`
- Description: An Event Ticket Platform
- Package Name: `com.devtiro.tickets`

#### Add Dependencies

Our application needs several key dependencies to function:

##### Web Dependencies

For building our REST API endpoints we'll use _Spring Web_. We selected this over _WebFlux_ for a simpler development experience, and it provides good performance for our needs.

##### Security Dependencies

We'll need _Spring Security_ for securing our application and _OAuth2 Resource Server_ for integration with Keycloak.

##### Database Dependencies

We choose _Spring Data JPA_ for database interactions using Java objects. We'll want the _PostgreSQL Driver_ for our production database and _H2 Database_ for running isolated tests.

##### Development Tools:

Let's also select _Lombok_ as it reduces boilerplate code through annotations.

#### Summary

- Created a new Spring Boot project using Spring Initializer
- Set up core dependencies for web, security, and data persistence
- Added development tools like Lombok to improve productivity

### Explore The Project

Let's explore the structure and content of your Spring Boot project for building an event ticket platform.

#### Project Structure

The project follows the standard Maven project structure with separate directories for source code, tests, and resources.

In the main source directory (`src/main/java`), we have:

- The main application class `TicketsApplication.java` under the `com.devtiro.tickets` package
- The `@SpringBootApplication` annotation marks this as our application's entry point
- The `main` method uses `SpringApplication.run()` to start our application

The resources directory (`src/main/resources`) contains:

- `application.properties` file for configuration settings
- We've removed the unused `static` and `templates` directories since we'll be using React + TanStack Start for our frontend

#### Dependencies

Our `pom.xml` file includes key dependencies:

- Spring Boot starter dependencies for web, JPA, security, and OAuth2
- PostgreSQL for our production database
- H2 database for testing
- Lombok for reducing boilerplate code
- Testing dependencies including Spring Security Test

#### Configuration

The `application.properties` file contains settings for our application, currently only the application's name.

```properties
spring.application.name=tickets
```

#### Testing Setup

The test directory (`src/test/java`) mirrors the main source structure and includes:

- A basic test class that verifies our Spring context loads correctly
- H2 database configuration for testing, preventing the need for PostgreSQL during tests

#### Summary

- Project uses standard Maven structure with Spring Boot configuration
- Main application class serves as the entry point with Spring Boot annotations
- Key dependencies include Spring Web, JPA, Security, and PostgreSQL
- H2 database configured for testing environment

### Running Postgresql

In this lesson, we'll set up PostgreSQL using Docker and configure our Spring Boot application to connect to it.

#### Set Up PostgreSQL with Docker

Docker makes running PostgreSQL simple and consistent across different development environments. Let's create a `docker-compose.yml` file to define our database setup:

```yaml
services:
  db:
    image: postgres:latest
    ports:
      - '5432:5432'
    restart: always
    environment:
      POSTGRES_PASSWORD: changemeinprod!

  adminer:
    image: adminer:latest
    restart: always
    ports:
      - 8888:8080
```

This configuration sets up two services:

- A PostgreSQL database running on port 5432
- Adminer, a web-based database management tool, running on port 8888

To start these services, run:

```bash
docker-compose up
```

#### Configure Spring Boot Database Connection

Our application needs to know how to connect to PostgreSQL. We'll configure this in `application.properties`:

```properties
# Database Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=changemeinprod!

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

The database connection properties tell Spring Boot:

- Where to find the database (`localhost:5432`)
- Which database to use (`postgres`)
- The login credentials

The JPA configuration enables:

- SQL logging for development debugging
- PostgreSQL-specific SQL dialect for optimal database interaction

Notice `ddl-auto` is set to `validate`, not `update`. We're not letting Hibernate create or alter tables for us -- it'll only check on startup that our entities match whatever schema is already there, and fail fast if they don't. Schema changes are Liquibase's job from here on, which we'll set up next.

#### Development Best Practices

When working with databases in development:

- Never store real passwords in configuration files
- Use environment variables or secure configuration management in production
- Keep the SQL logging enabled only in development for debugging

#### Summary

- PostgreSQL runs in Docker for consistent local development
- Adminer provides a web interface for database management
- Spring Boot connects to PostgreSQL using configuration properties
- Set `ddl-auto=validate` so Hibernate checks the schema instead of managing it

### Configure Liquibase

Letting Hibernate auto-generate our schema (`ddl-auto=update`) is convenient early on, but it doesn't give us a reviewable history of schema changes, doesn't handle renames or constraint changes reliably, and isn't something you'd want anywhere near a production database. We're going to use Liquibase to manage the schema explicitly instead, as versioned XML changesets.

#### Add the Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-liquibase</artifactId>
</dependency>
```

This has to be `spring-boot-starter-liquibase`, not the raw `org.liquibase:liquibase-core` artifact. Spring Boot 4 split `spring-boot-autoconfigure` into many small per-feature modules, and Liquibase's autoconfiguration now lives in its own `spring-boot-liquibase` module, which only gets pulled in via this starter -- `liquibase-core` alone gets you the library with nothing wiring it up. Get this wrong and there's no error at all: the app starts cleanly, the datasource connects fine, and Liquibase just never runs, silently. `spring-boot-starter-liquibase` pulls in `spring-boot-liquibase` (the autoconfiguration) and `liquibase-core` itself transitively, so it's the only dependency needed here.

With the right dependency present, Spring Boot autoconfigures Liquibase automatically -- it runs any pending changesets against the database on startup, before the rest of the application context loads.

#### Set Up the Changelog

Liquibase is driven by a master changelog that includes individual changeset files. Let's create `src/main/resources/db/changelog/db.changelog-master.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <includeAll path="changes" relativeToChangelogFile="true"/>

</databaseChangeLog>
```

This tells Liquibase to pick up every changelog file in the `db/changelog/changes` directory, in filename order. We don't have any entities yet, so that directory starts out empty -- we'll add our first changeset (covering the full schema in one go, as XML) once the domain model is settled, later in this section.

Note `path="changes"`, not `path="db/changelog/changes"` -- `relativeToChangelogFile="true"` resolves the path relative to the directory the master changelog itself lives in, which is already `db/changelog/`. Writing the fuller path here is a common mistake (an easy one to make, since it *looks* more explicit) and it silently resolves to a directory that doesn't exist.

Spring Boot's default changelog location is `classpath:/db/changelog/db.changelog-master.yaml` -- note the `.yaml`. Since we're using XML, we need to point at it explicitly:

```properties
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

#### Summary

- Added the `spring-boot-starter-liquibase` dependency (not raw `liquibase-core` -- Spring Boot 4's Liquibase autoconfiguration lives in its own module, only pulled in via the starter)
- Created the Liquibase master changelog at `db/changelog/db.changelog-master.xml`
- Pointed `spring.liquibase.change-log` at it explicitly, since Spring Boot's default assumes a YAML changelog
- Schema changes now go through Liquibase changesets instead of Hibernate's `ddl-auto`

### Running Keycloak

In this lesson, we'll set up Keycloak, a powerful identity and access management solution, to handle authentication and authorization for our event ticket platform. We'll configure Keycloak using Docker Compose and create the initial realm, client, and user settings needed for our application.

#### Set Up Keycloak with Docker Compose

Docker Compose makes it easy to run Keycloak alongside our other services. Let's add the Keycloak service to our existing `docker-compose.yml`:

```yaml
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    ports:
      - "9090:8080"
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    volumes:
      - keycloak-data:/opt/keycloak/data
    command:
      - start-dev
      - --db=dev-file

volumes:
  keycloak-data:
    driver: local
```

The configuration maps Keycloak's default port `8080` to `9090` on our host machine to avoid conflicts with our Spring Boot application.

We're using volumes to persist Keycloak's data between container restarts, unlike our PostgreSQL setup where we prefer a fresh start each time.

#### Configuring Keycloak

Once Keycloak is running, we need to set up three main components:

1. Create a realm named `event-ticket-platform`
2. Set up a client for our frontend application
3. Create a test user to represent an organizer

For the client configuration:

- Client ID: `event-ticket-platform-app`
- Client authentication: Off (for public access)
- Valid redirect URIs: `http://localhost:5173`
- Post logout redirect URIs: `http://localhost:5173`

#### Connecting Spring Boot to Keycloak

To connect our Spring Boot application to Keycloak, we add this property to `application.properties`:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/event-ticket-platform
```

This tells Spring Security to validate JWTs against our Keycloak instance.

#### Summary

- Set up Keycloak using Docker Compose with data persistence
- Created realm, client, and test user in Keycloak
- Connected Spring Boot application to Keycloak for JWT validation

### Running RabbitMQ

`ticket-service` is eventually going to need to tell some other service -- most likely a NestJS `notifications-service` -- that a ticket was purchased, so a confirmation email can go out. We don't want that to be a direct HTTP call from the purchase flow: if the notifications side is slow or down, that shouldn't be able to fail or delay someone buying a ticket. Instead, `ticket-service` will publish a message and move on, and whatever's listening can pick it up whenever it's ready. RabbitMQ is the broker that sits in between.

#### Add RabbitMQ to Docker Compose

Let's add a RabbitMQ service to our existing `docker-compose.yml`:

```yaml
  rabbitmq:
    image: rabbitmq:management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: ticket-platform
      RABBITMQ_DEFAULT_PASS: changemeinprod!
```

Two ports are exposed:

- `5672` is the AMQP port -- this is what `ticket-service` (and later, `notifications-service`) actually connects on to publish and consume messages
- `15672` is the management UI, available at `http://localhost:15672`, where you can inspect exchanges, queues, and individual messages while developing

We're setting an explicit `RABBITMQ_DEFAULT_USER`/`RABBITMQ_DEFAULT_PASS` rather than relying on the image's default `guest`/`guest` account. That's not just a security habit -- RabbitMQ's `guest` user is hard-restricted to connections from `localhost` by the broker itself, and a connection from another container on the Compose network doesn't count as `localhost`, so `ticket-service` would be refused outright if we left it as-is.

#### Summary

- Added a RabbitMQ service to `docker-compose.yml`, with the management UI enabled
- Exposed `5672` (AMQP, for services) and `15672` (management UI, for us)
- Set an explicit user/password, since the default `guest` account can't authenticate from another container

### Configure Internationalization

Every validation and error message we write from here on is going to be a hardcoded English string -- `"Event name is required"`, `"Venue not found"`, and so on -- unless we route them through a message source instead. Let's set that up now, before we start writing the DTOs and exception handlers that will use it.

#### Add the Message Properties

Let's create `src/main/resources/application_messages_en.properties`:

```properties
# Validation messages
validation.event.id.required=Event ID must be provided
validation.event.name.required=Event name is required
validation.event.venue.required=Venue is required
validation.event.status.required=Event status must be provided
validation.event.ticket-types.required=At least one ticket type is required
validation.ticket-type.name.required=Ticket type name is required
validation.ticket-type.price.required=Price is required
validation.ticket-type.price.positive-or-zero=Price must be zero or greater

# Error messages
error.unknown=An unknown error occurred
error.user.not-found=User not found
error.venue.not-found=Venue not found
error.event.not-found=Event not found
error.event.update-failed=Unable to update event
error.ticket-type.not-found=Ticket type not found
error.qr-code.generation-failed=Unable to generate QR Code
error.constraint-violation=A constraint violation occurred
error.validation-failed=Validation error occurred
```

Adding a language is just adding another file with the same keys -- `application_messages_el.properties` for Greek, `application_messages_fr.properties` for French, and so on. Nothing in the Java code changes when a language gets added.

#### Wire Up the Message Source

```java
@Configuration
public class InternationalizationConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("application_messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}
```

Two things worth calling out:

- `setUseCodeAsDefaultMessage(true)` means a typo'd or missing key falls back to showing the key itself (e.g. `validation.event.nam.required`) instead of throwing `NoSuchMessageException`. That's a deliberate safety net for development -- a wrong key becomes an obviously-wrong string in a response, not a 500.
- `LocalValidatorFactoryBean` is what makes Bean Validation -- our `@NotBlank`/`@NotNull`/`@PositiveOrZero` annotations -- resolve `{code}`-style messages against this same message source. Without it, `{validation.event.name.required}` would show up verbatim, braces and all, in a validation error.

#### How the Locale Is Chosen

We're not writing any locale-resolution code at all -- Spring MVC's default `LocaleResolver` (`AcceptHeaderLocaleResolver`) already reads the standard `Accept-Language` header on every request and populates `LocaleContextHolder` with it, and both `MessageSource` and Bean Validation resolve messages against whatever's in there. A request with `Accept-Language: el` gets Greek messages once an `application_messages_el.properties` file exists; anything else falls back to the English default we configured above.

#### Summary

- Added `application_messages_en.properties`, covering every validation and error message we've written so far
- Configured a `MessageSource` bean, falling back to the message code itself when a key is missing
- Configured `LocalValidatorFactoryBean` to resolve Bean Validation's `{code}` messages against the same source
- Locale comes from the standard `Accept-Language` header -- no custom header, no manual locale-reading code anywhere

### On Mapping: No MapStruct

Every DTO in this build needs converting to and from our domain entities somewhere. We're deliberately not reaching for MapStruct to generate that code. Instead, each service gets its own explicit `convertToXxx`/`convertFromXxx` methods, written by hand, living right next to the business logic that produces the entities they convert.

The trade-off is honest: MapStruct means less code to write and maintain by hand, field-by-field mapping bugs caught at compile time instead of runtime, and less boilerplate overall. Hand-written conversion methods mean more typing, and a missed field is a runtime bug (a `null` in a response) rather than a compiler error. What we get in exchange: no annotation processor to configure, no generated implementation classes to go looking for when something maps unexpectedly, and every conversion is a plain method you can open, read top to bottom, and step through in a debugger -- nothing to look up in generated code you didn't write. Given how much this build already leans on `@NaturalId`/`domainId` mapping (a place MapStruct's automatic field-name matching specifically *doesn't* help, since `id` and `domainId` never share a name), the "just read the method" trade-off is one we're choosing on purpose.

We'll write the first of these -- `EventServiceImpl.convertToDto` and friends -- in the next lesson, as soon as we have a DTO that needs one.

## Domain

### Create Enums

In this lesson we'll create the enumerations we'll need when we start implementing our entities.

We'll implement the enums first as they're the simplest to implement and will allow us to build our domain form the bottom up.

#### Create our Domain's Enums

Enums in Java provide a way to define a fixed set of constants.

In our ticket platform, enums help us maintain data integrity by restricting certain fields to specific, predefined values.

Let's explore each enum and its purpose:

##### Event Status

The `EventStatusEnum` represents the different states an event can be in throughout its lifecycle:

```java
public enum EventStatusEnum {
    DRAFT,      // Initial state when creating an event
    PUBLISHED,  // Event is live and tickets can be purchased
    CANCELLED,  // Event will not take place
    COMPLETED   // Event has finished
}
```

##### Ticket Status

The `TicketStatusEnum` tracks the state of purchased tickets:

```java
public enum TicketStatusEnum {
    PURCHASED,  // Default state when ticket is bought
    CANCELLED   // Ticket has been cancelled
}
```

##### Ticket Validation

We use two enums for ticket validation:

```java
public enum TicketValidationMethod {
    QR_SCAN,  // Ticket validated via QR code scan
    MANUAL    // Ticket validated via manual entry
}

public enum TicketValidationStatusEnum {
    VALID,    // Ticket is valid for entry
    INVALID,  // Ticket is not valid
    EXPIRED   // Ticket has expired
}
```

##### QR Code Status

The `QrCodeStatusEnum` manages the state of QR codes:

```java
public enum QrCodeStatusEnum {
    ACTIVE,   // QR code can be used
    EXPIRED   // QR code has been invalidated
}
```

#### Summary

- The `EventStatusEnum` represents the states an `Event` could be in
- The `TicketStatusEnum` represents the states a `Ticket` could be in
- The `TicketValidationMethodEnum` represents the different way a ticket can be validated
- The `TicketValidationStatusEnum` represents the the state a `TicketValidation` could be in
- The `QrCodeStatusEnum` represents the different states a `QrCode` could be in

### Entity Identifiers

Before we build our entities, let's settle on how they're identified -- both internally and externally.

#### Why Two IDs?

Every entity in our system gets two identifiers instead of one:

- `id: Long` -- a sequential, database-generated primary key. Fast to index, fast to join on, and never leaves the database.
- `domainId: UUID` -- the identifier we actually expose to the outside world: in URLs, DTOs, and anywhere the frontend touches an entity.

The sequential `id` is an implementation detail of the persistence layer. Exposing it directly (as `@GeneratedValue(strategy = GenerationType.UUID)` on the JPA `@Id`, which is what we'd otherwise reach for) leaks information we don't want to leak -- sequential integers make it trivial to guess how many rows a table has, or to enumerate every record by incrementing a URL. A `domainId` doesn't have that problem, and it's what every DTO from here on will carry.

#### The Pattern

Here's the shape every entity will follow, using `User` as an example:

```java
@Id
@GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "userGenerator"
)
@SequenceGenerator(
        name = "userGenerator",
        sequenceName = "users_seq",
        allocationSize = 1,
        initialValue = 1
)
@Basic(optional = false)
@Column(name = "id")
private Long id;

@NaturalId
@Column(name = "domain_id", nullable = false, updatable = false, unique = true)
private UUID domainId;
```

A few things worth calling out:

- `@GeneratedValue(strategy = GenerationType.SEQUENCE)` with a dedicated `@SequenceGenerator` per entity (e.g. `users_seq`, `events_seq`) is more efficient under load than `GenerationType.UUID`, since Hibernate can pre-allocate blocks of IDs rather than round-tripping to the database for every insert. We've set `allocationSize = 1` to keep generated IDs gap-free and easy to reason about while we're building; it's a one-line change to raise it later if insert throughput ever becomes a bottleneck.
- `@NaturalId` (from `org.hibernate.annotations.NaturalId`, not `jakarta.persistence`) marks `domainId` as a business key -- a stable, unique identifier that isn't the primary key, but that we'll look entities up by just as often. Hibernate can cache natural ID lookups separately from primary key lookups.
- `domainId` is `updatable = false` -- once set, it never changes.
- Every repository interface changes from `JpaRepository<X, UUID>` to `JpaRepository<X, Long>`, since the JPA `@Id` is now a `Long`. Anywhere we used to call `repository.findById(someUuid)`, we now need a `findByDomainId(someUuid)` method instead -- `findById` now expects a `Long`, which we never hand out.
- Every DTO keeps its existing `id: UUID` field name -- the frontend doesn't need to know or care that it's internally called `domainId` -- but every hand-written `convertToXxx` method needs to explicitly map `.setId(entity.getDomainId())`. It's a one-line reminder, but an easy one to forget, and forgetting it doesn't fail loudly: the DTO's `id` just comes back `null`, since Java initializes an unset field to `null` rather than erroring.
- Most entities generate their own `domainId` in application code (`UUID.randomUUID()`) at creation time. `User` is the one exception: its `domainId` is set to the Keycloak JWT subject during provisioning, so we can look a user up by the ID Keycloak already gave them.

We'll apply this pattern to every entity as we build it.

#### Summary

- Every entity gets a sequential `id: Long` (internal, database-generated primary key) and a `domainId: UUID` (external identifier, exposed to DTOs and the frontend)
- Repositories key off `Long` now; external lookups go through a new `findByDomainId`-style method instead of `findById`
- Every hand-written `convertToXxx` method needs to explicitly set a DTO's `id` from the entity's `domainId` -- there's no automatic field-name matching to lean on

### Create User Entity

In this lesson, we'll create a user entity that represents users in our ticketing platform.

#### Decide the Implementation Approach

While we have three distinct types of users (organizers, staff, and attendees), we'll use a single `User` entity rather than creating separate classes for each type.

This approach simplifies our domain model while still maintaining the flexibility to handle different user roles through Keycloak.

#### Create the User Entity

Let's create our `User` entity with the necessary fields and JPA annotations:

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "userGenerator"
    )
    @SequenceGenerator(
            name = "userGenerator",
            sequenceName = "users_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    // Relationships to be implemented later
    // TODO: Organized events
    // TODO: Attending events
    // TODO: Staffing events

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Summary

- Created a `User` class to represent a user of the system
- We'll use the same `User` class for attendees, staff and organizers
- We'll model the user's permissions using roles which we can assign in Keycloak
- Added `createdAt` and `updatedAt` audit fields

### Create Event Entity

In a ticketing platform, events are central to everything - they're what people buy tickets for and attend. Let's create the event entity which will store all the important information about events in our system.

#### Create the Event Entity

The event entity needs to store basic event details and maintain relationships with users who organize, attend, or staff the event.

```java
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "eventGenerator"
    )
    @SequenceGenerator(
            name = "eventGenerator",
            sequenceName = "events_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start")
    private LocalDateTime start;

    @Column(name = "end")
    private LocalDateTime end;

    @Column(name = "venue", nullable = false)
    private String venue;

    @Column(name = "sales_start")
    private LocalDateTime salesStart;

    @Column(name = "sales_end")
    private LocalDateTime salesEnd;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToMany(mappedBy = "attendingEvents")
    private List<User> attendees = new ArrayList<>();

    @ManyToMany(mappedBy = "staffingEvents")
    private List<User> staff = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
```

#### Update the User Entity

The `User` class needs corresponding relationships to events:

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "userGenerator"
    )
    @SequenceGenerator(
            name = "userGenerator",
            sequenceName = "users_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    private List<Event> organizedEvents = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_attending_events",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> attendingEvents = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_staffing_events",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> staffingEvents = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Summary

- Created an initial `Event` class
- Added `Event` references to the `User` class

### Create Venue Entity

Venues get reused across many events, and we want to support geo/map search later, so we're pulling `venue` out of the `Event` entity and modelling it as its own entity with a proper address and coordinates.

#### Create the Venue Entity

```java
@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "venueGenerator"
    )
    @SequenceGenerator(
            name = "venueGenerator",
            sequenceName = "venues_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "accessibility_info")
    private String accessibilityInfo;

    @OneToMany(mappedBy = "venue")
    private List<Event> events = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

The `latitude`/`longitude` pair is here for future geo/map search (e.g. "events near me"), even though we're not building that yet -- it's cheap to capture now and expensive to backfill later.

Notice there's no `cascade = CascadeType.ALL` on the `events` side -- deleting a `Venue` shouldn't cascade-delete every event that was ever held there.

#### Update the Event Entity

`Event` now references a `Venue` instead of storing venue details as a plain string:

```java
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "eventGenerator"
    )
    @SequenceGenerator(
            name = "eventGenerator",
            sequenceName = "events_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start")
    private LocalDateTime start;

    @Column(name = "end")
    private LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "sales_start")
    private LocalDateTime salesStart;

    @Column(name = "sales_end")
    private LocalDateTime salesEnd;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToMany(mappedBy = "attendingEvents")
    private List<User> attendees = new ArrayList<>();

    @ManyToMany(mappedBy = "staffingEvents")
    private List<User> staff = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
```

#### Summary

- Created a `Venue` entity with address, geo-coordinates, capacity, and accessibility info
- Replaced `Event.venue: String` with a `@ManyToOne` relationship to `Venue`
- A `Venue` can be reused across many `Event`s; deleting a `Venue` does not cascade-delete its `Event`s

### Create Ticket Type Entity

In this lesson, we'll create the `TicketType` entity which represents different categories of tickets available for events in our ticket platform.

#### Create the Ticket Type Entity

The ticket type entity requires specific attributes to effectively model different ticket categories.

Let's create a new entity class with its required fields and relationships:

```java
@Entity
@Table(name = "ticket_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketType {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketTypeGenerator"
    )
    @SequenceGenerator(
            name = "ticketTypeGenerator",
            sequenceName = "ticket_types_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "total_available")
    private Integer totalAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // TODO: Tickets

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Event Class Update

To complete the relationship between events and ticket types, we need to update the `Event` class:

```java
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "eventGenerator"
    )
    @SequenceGenerator(
            name = "eventGenerator",
            sequenceName = "events_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start")
    private LocalDateTime start;

    @Column(name = "end")
    private LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "sales_start")
    private LocalDateTime salesStart;

    @Column(name = "sales_end")
    private LocalDateTime salesEnd;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToMany(mappedBy = "attendingEvents")
    private List<User> attendees = new ArrayList<>();

    @ManyToMany(mappedBy = "staffingEvents")
    private List<User> staff = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<TicketType> ticketTypes = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
```

#### Summary

- Created `TicketType` class to model the different types of ticket available for events
- Added `ticketTypes` reference in the `Event` class

### Create Ticket Entity

In this lesson, we'll build the `Ticket` entity, which represents a purchased ticket for an event.

#### Create the Ticket Entity

A ticket is a record of a purchase that gives someone access to an event. Each ticket needs to track its status, who bought it, and what type of ticket it is.

Let's create our `Ticket` class:

```java
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketGenerator"
    )
    @SequenceGenerator(
            name = "ticketGenerator",
            sequenceName = "tickets_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id")
    private User purchaser;

    // TODO: Validation

    // TODO: QrCode

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
```

#### Update the Ticket Type Entity

We also need to update our `TicketType` class to include the inverse relationship:

```java
@Entity
@Table(name = "ticket_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketType {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketTypeGenerator"
    )
    @SequenceGenerator(
            name = "ticketTypeGenerator",
            sequenceName = "ticket_types_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "total_available")
    private Integer totalAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Summary

- Created `Ticket` class to model a ticket to an event
- Added `tickets` reference to the `TicketType` class

### Create Ticket Validation Entity

In this lesson, we'll create the ticket validation entity that records the validation of a ticket when attendees enter an event.

This entity tracks important details like the validation method used (QR code scan or manual) and the validation status, forming a key part of our ticket management system.

#### Creating the Ticket Validation Entity

The `TicketValidation` entity represents a single validation attempt of a ticket:

```java
@Entity
@Table(name = "ticket_validations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketValidation {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketValidationGenerator"
    )
    @SequenceGenerator(
            name = "ticketValidationGenerator",
            sequenceName = "ticket_validations_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketValidationStatusEnum status;

    @Column(name = "validation_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketValidationMethod validationMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Establishing the Relationship with Ticket

We need to update the `Ticket` class to maintain the relationship with its validations:

```java
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketGenerator"
    )
    @SequenceGenerator(
            name = "ticketGenerator",
            sequenceName = "tickets_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id")
    private User purchaser;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketValidation> validations = new ArrayList<>();

    // TODO: QrCode

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
```

#### Summary

- Created `TicketValidation` class to model a ticket being validated at the event
- Added `validations` reference to the `Ticket` class

### Create Qr Code Entity

In this lesson, we'll create the QR code entity which represents a unique QR code associated with a ticket.

QR codes are a key part of our ticketing system, allowing for quick and reliable ticket validation at events.

#### Create the QR Code Entity

Here's the complete implementation of the `QrCode` class:

```java
@Entity
@Table(name = "qr_codes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrCode {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "qrCodeGenerator"
    )
    @SequenceGenerator(
            name = "qrCodeGenerator",
            sequenceName = "qr_codes_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private QrCodeStatusEnum status;

    @Column(name = "value", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Updating the Ticket Entity

To complete the relationship between tickets and QR codes, we need to update the `Ticket` entity to include a reference to its QR codes:

```java
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketGenerator"
    )
    @SequenceGenerator(
            name = "ticketGenerator",
            sequenceName = "tickets_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id")
    private User purchaser;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketValidation> validations = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<QrCode> qrCodes = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
```

#### Summary

- Created `QrCode` class to model a ticket's QrCode
- Added `qrCodes` reference to the `Ticket` class

### Equals Hashcode

When working with Java entities in a Spring Boot application, implementing `equals()` and `hashCode()` methods correctly is necessary to prevent potential issues like infinite recursion in bidirectional relationships and to ensure proper object comparison behavior.

#### Equals and HashCode in JPA Entities

The `equals()` and `hashCode()` methods are fundamental Java methods used for object comparison and hash-based collections.

When dealing with JPA entities that have relationships with other entities, we need to be careful about which fields we include in these methods to avoid stack overflow errors caused by circular references.

#### Implementing Equals and HashCode

For our ticket platform entities, we'll generate these methods using our IDE, following these guidelines:

- Include primitive fields and basic types
- Include the entity's ID
- Exclude references to other entities
- Include audit fields (`createdAt` and `updatedAt`)

Here's an example for our `Event` entity:

```java
@Override
public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Event event = (Event) o;
    return Objects.equals(id, event.id) &&
           Objects.equals(domainId, event.domainId) &&
           Objects.equals(name, event.name) &&
           Objects.equals(start, event.start) &&
           Objects.equals(end, event.end) &&
           Objects.equals(salesStart, event.salesStart) &&
           Objects.equals(salesEnd, event.salesEnd) &&
           status == event.status &&
           Objects.equals(createdAt, event.createdAt) &&
           Objects.equals(updatedAt, event.updatedAt);
}

@Override
public int hashCode() {
    return Objects.hash(id, domainId, name, start, end,
                       salesStart, salesEnd, status,
                       createdAt, updatedAt);
}
```

Notice how we've excluded the `organizer`, `attendees`, `staff`, `ticketTypes`, and `venue` fields to prevent recursive calls -- `venue` became an entity reference rather than a primitive when we extracted it into its own `Venue` entity, so the same rule that applies to `organizer` now applies to it too. `domainId`, on the other hand, is included alongside `id` -- it's just another (business) identifier, not a relation to another entity.

#### Summary

- Used our IDE to generate `equals` and `hashCode` methods for each entity class

### Create the Baseline Liquibase Migration

Now that the domain model is settled -- seven entities, their sequences, and their relationships -- let's write the first real Liquibase changeset: a baseline that creates the whole schema in one go.

#### Write the Changeset

Let's create `src/main/resources/db/changelog/changes/001-initial-schema.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="1-create-sequences" author="event-ticket-platform">
        <createSequence sequenceName="users_seq" startValue="1" incrementBy="1"/>
        <createSequence sequenceName="venues_seq" startValue="1" incrementBy="1"/>
        <createSequence sequenceName="events_seq" startValue="1" incrementBy="1"/>
        <createSequence sequenceName="ticket_types_seq" startValue="1" incrementBy="1"/>
        <createSequence sequenceName="tickets_seq" startValue="1" incrementBy="1"/>
        <createSequence sequenceName="ticket_validations_seq" startValue="1" incrementBy="1"/>
        <createSequence sequenceName="qr_codes_seq" startValue="1" incrementBy="1"/>
    </changeSet>

    <changeSet id="2-create-users-table" author="event-ticket-platform">
        <createTable tableName="users">
            <column name="id" type="BIGINT" defaultValueSequenceNext="users_seq">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="domain_id" type="UUID">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_users_domain_id"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="email" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="3-create-venues-table" author="event-ticket-platform">
        <createTable tableName="venues">
            <column name="id" type="BIGINT" defaultValueSequenceNext="venues_seq">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="domain_id" type="UUID">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_venues_domain_id"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="address_line_1" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="address_line_2" type="VARCHAR(255)"/>
            <column name="city" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="postal_code" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="country" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="latitude" type="DOUBLE"/>
            <column name="longitude" type="DOUBLE"/>
            <column name="capacity" type="INT"/>
            <column name="accessibility_info" type="TEXT"/>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="4-create-events-table" author="event-ticket-platform">
        <createTable tableName="events">
            <column name="id" type="BIGINT" defaultValueSequenceNext="events_seq">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="domain_id" type="UUID">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_events_domain_id"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="event_start" type="TIMESTAMP"/>
            <column name="event_end" type="TIMESTAMP"/>
            <column name="venue_id" type="BIGINT">
                <constraints nullable="false" foreignKeyName="fk_events_venue" references="venues(id)"/>
            </column>
            <column name="sales_start" type="TIMESTAMP"/>
            <column name="sales_end" type="TIMESTAMP"/>
            <column name="status" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="organizer_id" type="BIGINT">
                <constraints foreignKeyName="fk_events_organizer" references="users(id)"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="5-create-ticket-types-table" author="event-ticket-platform">
        <createTable tableName="ticket_types">
            <column name="id" type="BIGINT" defaultValueSequenceNext="ticket_types_seq">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="domain_id" type="UUID">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_ticket_types_domain_id"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="price" type="DOUBLE">
                <constraints nullable="false"/>
            </column>
            <column name="description" type="TEXT"/>
            <column name="total_available" type="INT"/>
            <column name="event_id" type="BIGINT">
                <constraints foreignKeyName="fk_ticket_types_event" references="events(id)"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="6-create-tickets-table" author="event-ticket-platform">
        <createTable tableName="tickets">
            <column name="id" type="BIGINT" defaultValueSequenceNext="tickets_seq">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="domain_id" type="UUID">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_tickets_domain_id"/>
            </column>
            <column name="status" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="ticket_type_id" type="BIGINT">
                <constraints foreignKeyName="fk_tickets_ticket_type" references="ticket_types(id)"/>
            </column>
            <column name="purchaser_id" type="BIGINT">
                <constraints foreignKeyName="fk_tickets_purchaser" references="users(id)"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="7-create-ticket-validations-table" author="event-ticket-platform">
        <createTable tableName="ticket_validations">
            <column name="id" type="BIGINT" defaultValueSequenceNext="ticket_validations_seq">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="domain_id" type="UUID">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_ticket_validations_domain_id"/>
            </column>
            <column name="status" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="validation_method" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="ticket_id" type="BIGINT">
                <constraints foreignKeyName="fk_ticket_validations_ticket" references="tickets(id)"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="8-create-qr-codes-table" author="event-ticket-platform">
        <createTable tableName="qr_codes">
            <column name="id" type="BIGINT" defaultValueSequenceNext="qr_codes_seq">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="domain_id" type="UUID">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_qr_codes_domain_id"/>
            </column>
            <column name="status" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="value" type="TEXT">
                <constraints nullable="false"/>
            </column>
            <column name="ticket_id" type="BIGINT">
                <constraints foreignKeyName="fk_qr_codes_ticket" references="tickets(id)"/>
            </column>
            <column name="created_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="9-create-user-attending-events-table" author="event-ticket-platform">
        <createTable tableName="user_attending_events">
            <column name="user_id" type="BIGINT">
                <constraints primaryKey="true" nullable="false" foreignKeyName="fk_user_attending_events_user" references="users(id)"/>
            </column>
            <column name="event_id" type="BIGINT">
                <constraints primaryKey="true" nullable="false" foreignKeyName="fk_user_attending_events_event" references="events(id)"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet id="10-create-user-staffing-events-table" author="event-ticket-platform">
        <createTable tableName="user_staffing_events">
            <column name="user_id" type="BIGINT">
                <constraints primaryKey="true" nullable="false" foreignKeyName="fk_user_staffing_events_user" references="users(id)"/>
            </column>
            <column name="event_id" type="BIGINT">
                <constraints primaryKey="true" nullable="false" foreignKeyName="fk_user_staffing_events_event" references="events(id)"/>
            </column>
        </createTable>
    </changeSet>

</databaseChangeLog>
```

A few things worth calling out:

- The changesets are ordered so every table is created after the tables it references -- `events` needs `venues` and `users` to exist first, `ticket_types` needs `events`, and so on.
- `defaultValueSequenceNext` wires each `id` column to its matching sequence at the database level, so `INSERT`s that don't specify an `id` get the next sequence value automatically -- this is what Hibernate relies on when it saves a new entity with a null `id`.
- Every `domain_id` column gets `nullable="false"` plus a named unique constraint, matching `@NaturalId` on the entity side.
- Foreign keys are declared inline on the referencing column rather than as separate `addForeignKeyConstraint` changesets -- fewer changesets, and the constraint sits right next to the column it belongs to.
- `organizer_id` on `events` and the `ticket_type_id`/`purchaser_id`/`ticket_id` columns elsewhere are nullable (no `nullable="false"`), matching the `@ManyToOne` associations on the entities, which were never marked required.

With this changeset in place, drop `spring.jpa.hibernate.ddl-auto` down to `validate` (already done) and restart the application -- Liquibase creates the schema, and Hibernate just confirms it matches what our entities expect.

#### Summary

- Wrote a single baseline changeset creating all seven tables, their sequences, and the `user_attending_events`/`user_staffing_events` join tables
- Every table's `id` is wired to its sequence via `defaultValueSequenceNext`
- Every `domain_id` gets a `NOT NULL` unique constraint
- Foreign keys are declared inline, in dependency order

## User Provisioning

### User Provisioning Filter

In this lesson, we'll implement a filter that creates new users in our database when they first log in, ensuring every authenticated user has a corresponding `User` in the database.

#### Understanding User Provisioning

A user provisioning filter intercepts incoming requests after authentication to check if a user exists in our database and creates them if they don't.

This filter is valuable because it automatically creates user records in our database when users first authenticate through Keycloak, without requiring additional API endpoints or manual intervention.

#### Implementing the User Repository

First, we need a repository to interact with our user database:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.domainId = :domainId")
    boolean existsByDomainId(@Param("domainId") UUID domainId);

    @Query("SELECT u FROM User u WHERE u.domainId = :domainId")
    Optional<User> findByDomainId(@Param("domainId") UUID domainId);
}
```

By extending `JpaRepository`, we get built-in methods for:

- Creating users
- Finding users by their internal `Long` ID
- And many other common database operations

Note the type parameter is now `Long`, not `UUID` -- that's the JPA `@Id`. Since the only ID we ever actually have on hand (from a JWT subject, a path variable, and so on) is the `domainId`, we've added `existsByDomainId` and `findByDomainId` to look users up by that instead of the built-in `existsById`/`findById`.

We've written both as explicit `@Query` methods rather than relying on Spring Data's derived-query naming, so the actual JPQL being run against the database is always visible right next to the method signature, instead of implied by the method name.

#### Creating the User Provisioning Filter

The filter needs to:

1. Extract user information from the JWT token
2. Check if the user exists in our database
3. Create the user if they don't exist

Here's the implementation:

```java
@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Get the authentication object from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            // Extract the user ID from the JWT subject
            UUID keycloakId = UUID.fromString(jwt.getSubject());

            if (!userRepository.existsByDomainId(keycloakId)) {
                User user = new User();
                user.setDomainId(keycloakId);
                user.setName(jwt.getClaimAsString("preferred_username"));
                user.setEmail(jwt.getClaimAsString("email"));

                userRepository.save(user);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

The filter extends `OncePerRequestFilter` to ensure it only runs once per request.

We use `@RequiredArgsConstructor` to inject our `UserRepository` through constructor injection.

The filter checks if we have an authenticated user with a JWT token, then extracts the user ID and creates a new user record if one doesn't exist. Note we set `domainId` to the Keycloak subject, not `id` -- `id` is left unset and gets generated by the `users_seq` sequence when `save()` runs. This is the one entity where `domainId` isn't a random UUID we generate ourselves; it's the identity Keycloak already gave the user.

#### Summary

- Created the `UserRepository` interface
- Implemented the `UserProvisioningFilter`

### Spring Security Configuration

In this lesson, we'll configure Spring Security to work with our user provisioning filter.

#### Create Spring Security Configuration

Spring Security configuration is central to managing authentication and authorization in our application.

Let's create a new configuration class:

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            UserProvisioningFilter userProvisioningFilter) throws Exception {
        http
                .authorizeHttpRequests(authorize ->
                        // All requests must be authenticated
                        authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(
                                Customizer.withDefaults()
                        ))
                .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
```

#### Configuration Components

The configuration consists of several key parts that work together:

The `authorizeHttpRequests()` method sets up request authorization, requiring authentication for all incoming requests.

We disable CSRF protection as it's not typically needed for REST APIs.

The `sessionManagement()` configuration sets our application to be stateless, which is standard for REST APIs.

The `oauth2ResourceServer()` configuration sets up JWT token validation using default settings.

Finally, we add our user provisioning filter after the `BearerTokenAuthenticationFilter`, ensuring authentication happens before user provisioning.

#### Summary

- Created a basic Spring Security configuration file
- Disabled the CSRF mechanism on our REST API
- Configured Spring Security to be stateless
- Configured Spring Security to use the `UserProvisioningFilter`

### Configure Spring Audit

In order to track when our entities are created and updated, we need to enable Spring JPA's auditing functionality to automatically maintain audit fields like `@CreatedDate` and `@LastModifiedDate`.

#### Enable JPA Auditing

Spring JPA auditing allows us to automatically track when entities are created and modified, but it needs to be explicitly enabled.

We'll enable it globally for our application with two key components:

1. A configuration class with the `@EnableJpaAuditing` annotation
2. An `orm.xml` configuration file that registers the auditing entity listener

Here's how we implement the configuration class:

```java
@Configuration
@EnableJpaAuditing
public class JpaConfiguration {
}
```

Next, we need to create the `orm.xml` file in a specific location:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-mappings xmlns="http://java.sun.com/xml/ns/persistence/orm"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://java.sun.com/xml/ns/persistence/orm http://java.sun.com/xml/ns/persistence/orm_2_0.xsd"
                 version="2.0">
    <persistence-unit-metadata>
        <persistence-unit-defaults>
            <entity-listeners>
                <entity-listener class="org.springframework.data.jpa.domain.support.AuditingEntityListener"/>
            </entity-listeners>
        </persistence-unit-defaults>
    </persistence-unit-metadata>
</entity-mappings>
```

The `orm.xml` file needs to be placed in the `src/main/resources/META-INF` directory. When the application is built, this file will be included in the final package and made available to Hibernate.

#### Why Global Configuration?

While we could add the `@EntityListeners` annotation to each entity class individually, using global configuration through `orm.xml` is a better approach because:

- It reduces code duplication
- It makes the auditing behavior consistent across all entities
- It's easier to maintain and modify in one central location

#### Summary

- Created `JpaConfiguration` class with the `@EnableJpaAuditing` annotation
- Added `orm.xml` configuration file enabling the audit fields

## Create Event

### Ui Overview

In this lesson, we'll explore the user interface for creating events and identify any new requirements it introduces for our backend implementation.

#### Frontend Architecture

![Organizer Landing Page](./images/14-2-organizer-landing-page.webp)

The frontend application is built using React with TanStack Start (which uses Vite under the hood) and React Query for data fetching, with authentication handled through Keycloak. To run it locally:

```bash
# Install dependencies
npm install --force  # Force flag needed due to shadcn dependency conflicts

# Start development server
npm run dev
```

The application will be available at `localhost:5173`.

#### Create Event Form

![Create Event Form](./images/14-2-create-event-form.webp)

The create event form allows organizers to input:

- Event name (required)
- Event dates (start/end, optional)
- Venue details (required)
- Sales dates (start/end, optional)
- Ticket types (optional)
- Event status (draft/published)

Each ticket type has:

- Name
- Price
- Total available tickets
- Description (new field)

#### New Backend Requirements

The UI introduces a change to the requirements we need to consider. The `description` field for ticket types needs to be added to our backend model.

#### Summary

- The user interface can be used to create an event
- A field named `description` has been added to the ticket type object

### Create Event Design

In this lesson, we'll design the service layer for creating events in our ticket platform.

#### Domain Objects Design

The first step is creating data transfer objects (DTOs) that will carry the event creation information between layers.

We'll create two DTOs:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketTypeRequest {
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequest {
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private UUID venueId;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<CreateTicketTypeRequest> ticketTypes = new ArrayList<>();
}
```

The `CreateTicketTypeRequest` contains only the fields needed to create a ticket type, omitting relationships and audit fields. This makes the object focused and easier to validate.

The `CreateEventRequest` follows the same pattern, including only the data needed to create an event. Notice we're using a list of `CreateTicketTypeRequest` objects rather than `TicketType` entities, maintaining the separation between our domain and persistence layers.

Notice also that `CreateEventRequest` takes a `venueId` rather than a `Venue` object -- the caller picks an existing venue by ID, and the service layer resolves it, the same way `organizerId` is resolved to a `User`.

#### Service Interface Design

The service interface defines the contract for creating events:

```java
public interface EventService {
    Event createEvent(UUID organizerId, CreateEventRequest event);
}
```

The `createEvent` method takes two parameters:

- `organizerId`: The UUID of the user creating the event
- `event`: The event creation request containing all event details

This design separates the concerns of user identification from event data, making the code more flexible and easier to test.

#### Summary

- Implemented the `CreateTicketTypeRequest` class
- Implemented the `CreateEventRequest` class
- Created the `EventService` interface with `createEvent` method

### User Not Found Exception

In this lesson, we'll implement an exception that is thrown when a user is not found in our system.

This will help us handle error cases in a clean and organized way.

#### Understanding Custom Exceptions

Custom exceptions help us handle application-specific error cases in a way that makes sense for our domain.

When creating custom exceptions, it's helpful to have a base exception class that all other exceptions extend.

Let's create our base exception class:

```java
public class EventTicketException extends RuntimeException {
    public EventTicketException() {
    }

    public EventTicketException(String message) {
        super(message);
    }

    public EventTicketException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventTicketException(Throwable cause) {
        super(cause);
    }

    public EventTicketException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

#### Creating the User Not Found Exception

Now that we have our base exception, we can create our specific exception for when a user is not found:

```java
public class UserNotFoundException extends EventTicketException {
    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    public UserNotFoundException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

#### Creating the Venue Not Found Exception

Creating an event also requires resolving a `venueId` to an existing `Venue`, so we need a matching exception for that case:

```java
public class VenueNotFoundException extends EventTicketException {
    public VenueNotFoundException() {
    }

    public VenueNotFoundException(String message) {
        super(message);
    }

    public VenueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VenueNotFoundException(Throwable cause) {
        super(cause);
    }

    public VenueNotFoundException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

#### Using Runtime Exceptions

We extend `RuntimeException` in our base exception class rather than `Exception`.

This choice means we don't need to declare throws clauses on methods that might throw our exceptions.

This approach, recommended by Robert C. Martin in "Clean Code", helps maintain the Open-Closed Principle by preventing changes to method signatures when new exceptions are added.

#### Summary

- Created a `EventTicketException` parent custom exception
- Created the `UserNotFoundException` to represent the invalid state when a specified user does not exist
- Created the `VenueNotFoundException` to represent the invalid state when a specified venue does not exist

### Create Event Implementation

In this lesson, we'll implement the service layer logic to create events in our event tick platform.

#### Implementation Details

The event creation service needs two pieces of information: the organizer's ID and the event details.

```java
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    @Override
    public Event createEvent(UUID organizerId, CreateEventRequest event) {
        // Find the organizer or throw an exception if not found
        User organizer = userRepository.findByDomainId(organizerId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with ID '%s' not found", organizerId))
                );

        // Find the venue or throw an exception if not found
        Venue venue = venueRepository.findByDomainId(event.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException(
                        String.format("Venue with ID '%s' not found", event.getVenueId()))
                );

        // Create ticket types
        List<TicketType> ticketTypesToCreate = event.getTicketTypes().stream().map(
                ticketType -> {
                    TicketType ticketTypeToCreate = new TicketType();
                    ticketTypeToCreate.setDomainId(UUID.randomUUID());
                    ticketTypeToCreate.setName(ticketType.getName());
                    ticketTypeToCreate.setPrice(ticketType.getPrice());
                    ticketTypeToCreate.setDescription(ticketType.getDescription());
                    ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
                    return ticketTypeToCreate;
                }).toList();

        // Create and populate the event
        Event eventToCreate = new Event();
        eventToCreate.setDomainId(UUID.randomUUID());
        eventToCreate.setName(event.getName());
        eventToCreate.setStart(event.getStart());
        eventToCreate.setEnd(event.getEnd());
        eventToCreate.setVenue(venue);
        eventToCreate.setSalesStart(event.getSalesStart());
        eventToCreate.setSalesEnd(event.getSalesEnd());
        eventToCreate.setStatus(event.getStatus());
        eventToCreate.setOrganizer(organizer);
        eventToCreate.setTicketTypes(ticketTypesToCreate);

        return eventRepository.save(eventToCreate);
    }
}
```

We also need a minimal `VenueRepository` for the lookup above:

```java
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query("SELECT v FROM Venue v WHERE v.domainId = :domainId")
    Optional<Venue> findByDomainId(@Param("domainId") UUID domainId);
}
```

#### Error Handling

The service includes error handling for cases where the organizer or venue doesn't exist:

- We use `userRepository.findByDomainId()` to look up the organizer
- We use `venueRepository.findByDomainId()` to look up the venue
- If either isn't found, we throw a `UserNotFoundException` or `VenueNotFoundException` with a clear message
- Both exceptions include the ID that wasn't found to help with troubleshooting
- We also generate a fresh `domainId` (`UUID.randomUUID()`) for the new `Event` and each new `TicketType` -- their `id` is left unset and generated by the database sequence when `save()` runs

#### Summary

- Created the `EventRepository` interface
- Created the `VenueRepository` interface
- Created the `EventServiceImpl` class
- Implemented the `createEvent` method, including resolving the event's `Venue` by ID

### Dtos And Conversion Methods

In this lesson, we'll implement the DTOs and conversion methods needed to handle event creation in our REST API.

These components help us maintain a clear separation between our API contract and internal domain model, while ensuring data validation at the API boundary.

#### Understanding DTOs and Their Purpose

Data Transfer Objects (DTOs) serve as specialized objects for transferring data between our API and clients. Unlike our domain models, DTOs:

- Are tailored specifically for API communication
- Include validation rules for request data
- Can be modified without affecting our domain models
- Help prevent exposing internal implementation details

#### Create Request DTOs

Let's create our request DTOs with proper validation:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequestDto {
    @NotBlank(message = "{validation.event.name.required}")
    private String name;

    private LocalDateTime start;
    private LocalDateTime end;

    @NotNull(message = "{validation.event.venue.required}")
    private UUID venueId;

    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;

    @NotNull(message = "{validation.event.status.required}")
    private EventStatusEnum status;

    @NotEmpty(message = "{validation.event.ticket-types.required}")
    @Valid
    private List<CreateTicketTypeRequestDto> ticketTypes;
}
```

For ticket types, we create a separate DTO with its own validations:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketTypeRequestDto {
    @NotBlank(message = "{validation.ticket-type.name.required}")
    private String name;

    @NotNull(message = "{validation.ticket-type.price.required}")
    @PositiveOrZero(message = "{validation.ticket-type.price.positive-or-zero}")
    private Double price;

    private String description;
    private Integer totalAvailable;
}
```

#### Venue Response DTO

Since `Venue` is now its own entity, we need a small DTO to represent it wherever an event's venue shows up in a response. We'll reuse this same `VenueResponseDto` across every event-related response DTO for the rest of the build, rather than redefining it each time:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueResponseDto {
    private UUID id;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
}
```

#### Creating Response DTOs

Response DTOs represent the data we send back after creating an event:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<CreateTicketTypeResponseDto> ticketTypes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Add CreateTicketTypeResponseDto

`CreateEventResponseDto.ticketTypes` needs a shape of its own too -- this one was always implied but never actually written out:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketTypeResponseDto {
    private UUID id;
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
}
```

#### Convert Between DTOs and Domain Objects

No MapStruct -- these are plain methods, declared on `EventService` alongside `createEvent` and implemented in `EventServiceImpl`:

```java
public interface EventService {
    Event createEvent(UUID organizerId, CreateEventRequest event);

    CreateEventRequest convertFromDto(CreateEventRequestDto dto);
    CreateTicketTypeRequest convertFromDto(CreateTicketTypeRequestDto dto);
    VenueResponseDto convertToVenueResponseDto(Venue venue);
    CreateTicketTypeResponseDto convertToCreateTicketTypeResponseDto(TicketType ticketType);
    CreateEventResponseDto convertToCreateEventResponseDto(Event event);
}
```

```java
@Override
public CreateEventRequest convertFromDto(CreateEventRequestDto dto) {
    CreateEventRequest request = new CreateEventRequest();
    request.setName(dto.getName());
    request.setStart(dto.getStart());
    request.setEnd(dto.getEnd());
    request.setVenueId(dto.getVenueId());
    request.setSalesStart(dto.getSalesStart());
    request.setSalesEnd(dto.getSalesEnd());
    request.setStatus(dto.getStatus());
    request.setTicketTypes(dto.getTicketTypes().stream()
            .map(this::convertFromDto)
            .toList());
    return request;
}

@Override
public CreateTicketTypeRequest convertFromDto(CreateTicketTypeRequestDto dto) {
    CreateTicketTypeRequest request = new CreateTicketTypeRequest();
    request.setName(dto.getName());
    request.setPrice(dto.getPrice());
    request.setDescription(dto.getDescription());
    request.setTotalAvailable(dto.getTotalAvailable());
    return request;
}

@Override
public VenueResponseDto convertToVenueResponseDto(Venue venue) {
    VenueResponseDto dto = new VenueResponseDto();
    dto.setId(venue.getDomainId());
    dto.setName(venue.getName());
    dto.setAddressLine1(venue.getAddressLine1());
    dto.setAddressLine2(venue.getAddressLine2());
    dto.setCity(venue.getCity());
    dto.setPostalCode(venue.getPostalCode());
    dto.setCountry(venue.getCountry());
    dto.setLatitude(venue.getLatitude());
    dto.setLongitude(venue.getLongitude());
    return dto;
}

@Override
public CreateTicketTypeResponseDto convertToCreateTicketTypeResponseDto(TicketType ticketType) {
    CreateTicketTypeResponseDto dto = new CreateTicketTypeResponseDto();
    dto.setId(ticketType.getDomainId());
    dto.setName(ticketType.getName());
    dto.setPrice(ticketType.getPrice());
    dto.setDescription(ticketType.getDescription());
    dto.setTotalAvailable(ticketType.getTotalAvailable());
    return dto;
}

@Override
public CreateEventResponseDto convertToCreateEventResponseDto(Event event) {
    CreateEventResponseDto dto = new CreateEventResponseDto();
    dto.setId(event.getDomainId());
    dto.setName(event.getName());
    dto.setStart(event.getStart());
    dto.setEnd(event.getEnd());
    dto.setVenue(convertToVenueResponseDto(event.getVenue()));
    dto.setSalesStart(event.getSalesStart());
    dto.setSalesEnd(event.getSalesEnd());
    dto.setStatus(event.getStatus());
    dto.setTicketTypes(event.getTicketTypes().stream()
            .map(this::convertToCreateTicketTypeResponseDto)
            .toList());
    dto.setCreatedAt(event.getCreatedAt());
    dto.setUpdatedAt(event.getUpdatedAt());
    return dto;
}
```

Every field gets set explicitly, one line each -- there's no automatic field-name matching to lean on, so a renamed or newly-added field on either side is a compile error at the call site the moment it's used, not a silent gap. `id` is the field most worth double-checking in every one of these: it's always `dto.setId(entity.getDomainId())`, never `entity.getId()`.

#### Summary

- Created `CreateTicketTypeResponseDto`, closing a gap that was always implied but never actually defined
- Added `convertFromDto`/`convertToXxx` methods to `EventService`, hand-written in `EventServiceImpl`
- Created the `CreateEventRequestDto` (now takes a `venueId` instead of a `venue` string)
- Created the `CreateEventResponseDto`
- The DTOs make use of validation annotations to ensure data consistency

### Event Controller

In this lesson, we'll implement the REST API endpoint for creating events.

#### Create the Event Controller

The event controller is responsible for handling HTTP requests related to events. Let's create a new controller class with Spring's `@RestController` annotation:

```java
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<CreateEventResponseDto> createEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateEventRequestDto createEventRequestDto) {
        // Convert DTO to domain object
        CreateEventRequest createEventRequest = eventService.convertFromDto(createEventRequestDto);

        // Extract user ID from JWT
        UUID userId = UUID.fromString(jwt.getSubject());

        // Create the event
        Event createdEvent = eventService.createEvent(userId, createEventRequest);

        // Convert response to DTO
        CreateEventResponseDto createEventResponseDto = eventService.convertToCreateEventResponseDto(createdEvent);

        return new ResponseEntity<>(createEventResponseDto, HttpStatus.CREATED);
    }
}
```

Notice the controller no longer injects a separate mapper -- `eventService` is the only dependency, for both the business logic and the conversions either side of it.

#### Summary

- Created the `EventController` class
- Implemented create event endpoint
- The controller now depends only on `EventService`, not a separate mapper bean

### Global Exception Handler

In REST APIs, handling errors consistently and providing meaningful feedback to clients is a key part of creating a good developer experience.

In this lesson, we'll implement a global exception handler to manage errors across our application.

#### Custom Error Response Format

To maintain consistency, we'll create a simple DTO class for our error responses:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDto {
    private String error;
}
```

#### Understanding Exception Handlers

Spring's exception handling mechanism allows us to centralize our error handling logic in one place. This means we can define how different types of exceptions should be handled and what response the client should receive.

Here's how we'll create our global exception handler:

```java
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Caught exception", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(resolve("error.unknown"));
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String resolve(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
```

#### Handling Specific Exceptions

For validation errors, we need to handle two specific types of exceptions: `ConstraintViolationException` and `MethodArgumentNotValidException`. These occur when request validation fails:

```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ErrorDto> handleConstraintViolation(ConstraintViolationException ex) {
    log.error("Caught ConstraintViolationException", ex);

    String errorMessage = ex.getConstraintViolations()
        .stream()
        .findFirst()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .orElse(resolve("error.constraint-violation"));

    ErrorDto errorDto = new ErrorDto();
    errorDto.setError(errorMessage);
    return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
}
```

Notice `violation.getMessage()` isn't calling `resolve()` -- it doesn't need to. Bean Validation interpolates `{code}` placeholders itself, against `LocaleContextHolder`'s current locale, using the `LocalValidatorFactoryBean` we wired up earlier. By the time we see `violation.getMessage()` here, it's already the resolved, correctly-localized string.

```java
  @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        log.error("Caught MethodArgumentNotValidException", ex);
        ErrorDto errorDto = new ErrorDto();

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(resolve("error.validation-failed"));

        errorDto.setError(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }
```

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorDto> handleUserNotFoundException(UserNotFoundException ex) {
    log.error("Caught UserNotFoundException", ex);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setError(resolve("error.user.not-found"));
    return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
}
```

```java
@ExceptionHandler(VenueNotFoundException.class)
public ResponseEntity<ErrorDto> handleVenueNotFoundException(VenueNotFoundException ex) {
    log.error("Caught VenueNotFoundException", ex);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setError(resolve("error.venue.not-found"));
    return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
}
```

Unlike the validation handlers above, these two don't have a `{code}` anywhere for Bean Validation to auto-resolve -- the literal string was always just us, so we call `resolve()` (the helper we defined alongside `messageSource` earlier in this class) ourselves.

#### Summary

- Created the `ErrorDto` class
- Created the `GlobalExceptionHandler` class, injected with `MessageSource`
- Handler methods resolve their own literal messages via `resolve(code)`; validation-derived messages are already resolved by the time we see them
- All errors and now returned in the expected format

### Ui Testing

In this lesson, we'll test the newly implemented event creation functionality through the user interface.

#### Set Up the Environment

Before we can test our event creation functionality, we need to start our development environment:

1. Start the Docker services by running:

```bash
docker compose up
```

2. Run Maven clean and compile to ensure all generated code is up to date:

```bash
mvn clean compile
```

3. Start the Spring Boot application

#### Fixing the Database Column Names

When starting the application, you might encounter an error related to reserved keywords in PostgreSQL.

To fix this, update the `Event` entity's column names:

```java
@Column(name = "event_start")
private LocalDateTime start;

@Column(name = "event_end")
private LocalDateTime end;
```

This change avoids using PostgreSQL's reserved keywords `start` and `end` as column names.

#### Creating Your First Event

Navigate to '/organizers' in your browser and click "Create an Event".

You'll need to:

- Log in with your organizer credentials
- Fill in the event details:
  - Name (required)
  - Venue (required)
  - At least one ticket type with:
    - Name
    - Price
    - Total available tickets
    - Description (optional)
- Choose whether to publish the event or keep it as a draft

#### Verifying Event Creation

After creating an event, you can verify its creation in several ways:

- Check the HTTP response status (should be 201 Created)
- Review the response JSON to ensure all fields are correct
- Use Adminer (available at port 8888) to check the database tables:
  - events table - contains the event details
  - ticket_types table - contains the ticket configuration
  - users table - contains the organizer information

#### Link Event and TicketType

When checking adminer we see that the ticket types's `event_id` field is null in the database.

We fix this by ensuring we set the event object on the `TicketType` class when we create them in the event service:

```java
@Override
public Event createEvent(UUID organizerId, CreateEventRequest event) {
    User organizer = userRepository.findByDomainId(organizerId)
            .orElseThrow(() -> new UserNotFoundException(
                    String.format("User with ID '%s' not found", organizerId))
            );

    Venue venue = venueRepository.findByDomainId(event.getVenueId())
            .orElseThrow(() -> new VenueNotFoundException(
                    String.format("Venue with ID '%s' not found", event.getVenueId()))
            );

    // eventToCreate needs to be moved up here
    Event eventToCreate = new Event();
    eventToCreate.setDomainId(UUID.randomUUID());

    List<TicketType> ticketTypesToCreate = event.getTicketTypes().stream().map(
            ticketType -> {
                TicketType ticketTypeToCreate = new TicketType();
                ticketTypeToCreate.setDomainId(UUID.randomUUID());
                ticketTypeToCreate.setName(ticketType.getName());
                ticketTypeToCreate.setPrice(ticketType.getPrice());
                ticketTypeToCreate.setDescription(ticketType.getDescription());
                ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
                // 2. The next line needs to be added
                ticketTypeToCreate.setEvent(eventToCreate);
                return ticketTypeToCreate;
            }).toList();

    eventToCreate.setName(event.getName());
    eventToCreate.setStart(event.getStart());
    eventToCreate.setEnd(event.getEnd());
    eventToCreate.setVenue(venue);
    eventToCreate.setSalesStart(event.getSalesStart());
    eventToCreate.setSalesEnd(event.getSalesEnd());
    eventToCreate.setStatus(event.getStatus());
    eventToCreate.setOrganizer(organizer);
    eventToCreate.setTicketTypes(ticketTypesToCreate);

    return eventRepository.save(eventToCreate);
}
```

#### Summary

- We experienced an error when attempting to save an event in the database
- `start` and `end` are reserved keywords in PostgreSQL
- We updated the event object to no longer use the reserve keywords
- We have been able to successfully create an event

## List Events

### List Event Service

In this lesson, we'll implement the list events functionality in the service layer, allowing organizers to view their events through pagination. This feature enables efficient data retrieval and better performance when dealing with large numbers of events.

#### Understanding Pagination

Spring Data JPA's pagination feature helps manage large datasets by breaking them into smaller, manageable chunks. Instead of fetching all records at once, pagination returns a specific "page" of results, which includes both the data and metadata about the total results.

#### Repository Method

Spring Data JPA can generate a query just from a method name (`findByOrganizerDomainId` would work fine on its own), but we're going to write every custom finder with an explicit `@Query` instead. It's a few more characters, but the JPQL actually being run against the database is right there in the code -- no need to mentally parse a long method name to know what it does.

```java
// In EventRepository interface
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.organizer.domainId = :organizerDomainId")
    Page<Event> findByOrganizerDomainId(@Param("organizerDomainId") UUID organizerDomainId, Pageable pageable);
}
```

The query:

- Selects `Event` entities (`e`)
- Filters by the organizer's `domainId`, navigating from `Event` into the `organizer` relationship and then into its `domainId` field
- Returns results in pages -- Spring Data JPA handles pagination automatically because the method takes a `Pageable` and returns a `Page<Event>`, no `LIMIT`/`OFFSET` needed in the JPQL itself

Note this is `Long` now, not `UUID` -- that's the JPA `@Id` type. We only ever have the organizer's `domainId` on hand (from the JWT), never their internal `id`, so the query needs to reach into the relationship rather than filtering on `Event`'s own primary key.

#### Implement the Service Layer

```java
// Method in EventService interface
Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable);

// Implementation in EventServiceImpl
@Override
public Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable) {
    // Use the repository to find events by organizer ID with pagination
    return eventRepository.findByOrganizerDomainId(organizerId, pageable);
}
```

#### Summary

- Added the `listEventsForOrganizer` method to the `EventService` interface
- Added the `findByOrganizerDomainId` method to the `EventRepository` interface
- Implemented `listEventsForOrganizer` method in the `EventServiceImpl` class

### Dtos And Conversion Methods

In this lesson we implement the DTOs and mappers required to implement the list event functionality.

#### Understanding DTOs for Event Listing

Data Transfer Objects (DTOs) help us move data between layers while controlling exactly what information we share. When listing events, we need to consider what information is necessary for display and what should be kept private.

Let's create two DTOs:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListEventResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<ListEventTicketTypeResponseDto> ticketTypes = new ArrayList<>();
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListEventTicketTypeResponseDto {
    private UUID id;
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
}
```

#### Conversion Methods

Two more methods on `EventService`/`EventServiceImpl`:

```java
public interface EventService {
    // Existing methods...

    ListEventTicketTypeResponseDto convertToListEventTicketTypeResponseDto(TicketType ticketType);
    ListEventResponseDto convertToListEventResponseDto(Event event);
}
```

```java
@Override
public ListEventTicketTypeResponseDto convertToListEventTicketTypeResponseDto(TicketType ticketType) {
    ListEventTicketTypeResponseDto dto = new ListEventTicketTypeResponseDto();
    dto.setId(ticketType.getDomainId());
    dto.setName(ticketType.getName());
    dto.setPrice(ticketType.getPrice());
    dto.setDescription(ticketType.getDescription());
    dto.setTotalAvailable(ticketType.getTotalAvailable());
    return dto;
}

@Override
public ListEventResponseDto convertToListEventResponseDto(Event event) {
    ListEventResponseDto dto = new ListEventResponseDto();
    dto.setId(event.getDomainId());
    dto.setName(event.getName());
    dto.setStart(event.getStart());
    dto.setEnd(event.getEnd());
    dto.setVenue(convertToVenueResponseDto(event.getVenue()));
    dto.setSalesStart(event.getSalesStart());
    dto.setSalesEnd(event.getSalesEnd());
    dto.setStatus(event.getStatus());
    dto.setTicketTypes(event.getTicketTypes().stream()
            .map(this::convertToListEventTicketTypeResponseDto)
            .toList());
    return dto;
}
```

Note this one's named `convertToListEventTicketTypeResponseDto`, not `convertToCreateTicketTypeResponseDto` from the last lesson, even though both take a `TicketType` and both exist purely to represent it in a response -- Java can't overload on return type alone, so a `TicketType` mapping to a different DTO shape needs a different method name each time. It's more typing than MapStruct's single overloaded `toDto`, but it also means the method name always tells you exactly which shape you're getting.

#### Data Transfer Considerations

When designing DTOs for listing events, we've made specific choices about what data to include:

- Included basic event details needed for display (ID, name, dates, venue)
- Included ticket types because they're shown in event listings
- Excluded sensitive or unnecessary data (organizer details, attendees, staff)
- Excluded audit fields (createdAt, updatedAt) as they're not displayed
- Initialized collections to empty lists to prevent null pointer issues

#### Summary

- Created the `ListEventResponseDto`
- Create the `ListEventTicketTypeResponseDto`
- Added `convertToListEventResponseDto`/`convertToListEventTicketTypeResponseDto` to `EventService`

### Event Controller

In this lesson we will implement the list events functionality in the events controller.

#### Implementing the List Events Endpoint

Let's add a new endpoint to our `EventController` that will handle GET requests to list events.

```java
@GetMapping
public ResponseEntity<Page<ListEventResponseDto>> listEvents(
        @AuthenticationPrincipal Jwt jwt, Pageable pageable
) {
    UUID userId = parseUserId(jwt);
    Page<Event> events = eventService.listEventsForOrganizer(userId, pageable);
    return ResponseEntity.ok(
            events.map(eventService::convertToListEventResponseDto)
    );
}
```

The endpoint takes two parameters:

- The `@AuthenticationPrincipal Jwt jwt` which contains the authenticated user's information
- A `Pageable` object that Spring automatically creates from query parameters

#### Understanding the Implementation

The endpoint follows a clear flow:

1. Extract the user ID from the JWT token using our helper method `parseUserId`
2. Call the service layer to retrieve a page of events for the organizer
3. Map the page of events to DTOs using `EventService.convertToListEventResponseDto`
4. Return the mapped page with a 200 OK status

The `Pageable` parameter deserves special attention.
Spring will automatically create this object from standard query parameters:

- `page` - The page number (0-based)
- `size` - The number of items per page
- `sort` - The sorting criteria

For example: `/api/v1/events?page=0&size=10&sort=name,desc`

#### Authentication vs Authorization

At this stage, our implementation handles authentication but not full authorization.

We verify that users are who they claim to be through JWT validation, and we filter events by organizer.

However, we haven't yet implemented role-based access control to restrict endpoints to specific user types (organizers, staff, attendees).

This distinction is important:

- Authentication confirms identity (Who are you?)
- Authorization controls access (What are you allowed to do?)

#### Summary

- Added the list event endpoint to the `EventsController`

### Ui Testing

Testing the event listing functionality in the user interface allows us to verify that our Spring Boot backend is correctly integrated with our frontend application and that users can view their events with pagination support.

#### Testing the List Events Endpoint

Let's verify that our list events functionality works correctly by testing it through the user interface:

##### Step 1: Compile and Run

First, we need to ensure our application builds correctly and is running:

- Run `mvn clean compile` to build the application
- Start the Spring Boot application
- Navigate to the organizer's landing page in your browser
- Login using your Keycloak credentials

##### Step 2: Inspecting the Network Requests

To verify our backend is working correctly, we can use the browser's developer tools:

1. Open the Network tab in your browser's developer tools
2. Clear any existing network requests
3. Refresh the page

The browser will make a request to `/api/v1/events` with query parameters:

```
page=0&size=2
```

##### Step 3: Analyzing the Response

The response from our endpoint contains:

- A `content` array containing the event data
- Pagination metadata including:
  - `pageNumber`: Current page (starting from 0)
  - `pageSize`: Number of records per page
  - `totalPages`: Total number of pages
  - `totalElements`: Total number of events

##### Step 4: Testing Pagination

The user interface implements pagination controls:

- Events are displayed in pages of 2 items
- Navigate between pages using the pagination controls
- The UI updates to show the next set of events when clicking "Next"
- Event details displayed include:
  - Start and end dates
  - Sales period
  - Description
  - Venue information
  - Ticket types

#### Summary

- We tested the list events functionality using the user interface

## Get Event Endpoint

### Implement Service Method

In this lesson, we'll implement the get event service layer functionality.

#### Add Method to the Repository

Let's add a new method to our `EventRepository` interface:

```java
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Existing methods...

    @Query("SELECT e FROM Event e WHERE e.domainId = :domainId AND e.organizer.domainId = :organizerDomainId")
    Optional<Event> findByDomainIdAndOrganizerDomainId(@Param("domainId") UUID domainId, @Param("organizerDomainId") UUID organizerDomainId);
}
```

This repository method combines two search criteria:

- The event's `domainId`
- The organizer's `domainId`, navigated through the `organizer` relationship, to ensure users can only access their own events

#### Update the Service Layer

Next, let's add the corresponding method to our `EventService` interface:

```java
public interface EventService {
    // Existing methods...

    Optional<Event> getEventForOrganizer(UUID organizerId, UUID id);
}
```

Finally, let's implement the method in our `EventServiceImpl` class:

```java
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;

    // Existing methods...

    @Override
    public Optional<Event> getEventForOrganizer(UUID organizerId, UUID id) {
        // Pass the parameters to the repository method
        return eventRepository.findByDomainIdAndOrganizerDomainId(id, organizerId);
    }
}
```

#### Summary

- Added repository method to find events by domain ID and organizer domain ID
- Created service interface method for retrieving events
- Implemented service method to fetch event details using repository

### Create Dto Classes

In this lesson, we'll implement the Data Transfer Objects (DTOs) needed to implement our get event endpoint.

#### Create the DTOs

We'll create two DTOs - one for the event details and another for its ticket types.

Let's start with the ticket type DTO:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEventDetailsTicketTypesResponseDto {
    private UUID id;
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

Next, let's create the event details DTO:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEventDetailsResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<GetEventDetailsTicketTypesResponseDto> ticketTypes = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Add Conversion Methods

```java
public interface EventService {
    // ... existing methods ...

    GetEventDetailsTicketTypesResponseDto convertToGetEventDetailsTicketTypesResponseDto(TicketType ticketType);
    GetEventDetailsResponseDto convertToGetEventDetailsResponseDto(Event event);
}
```

```java
@Override
public GetEventDetailsTicketTypesResponseDto convertToGetEventDetailsTicketTypesResponseDto(TicketType ticketType) {
    GetEventDetailsTicketTypesResponseDto dto = new GetEventDetailsTicketTypesResponseDto();
    dto.setId(ticketType.getDomainId());
    dto.setName(ticketType.getName());
    dto.setPrice(ticketType.getPrice());
    dto.setDescription(ticketType.getDescription());
    dto.setTotalAvailable(ticketType.getTotalAvailable());
    dto.setCreatedAt(ticketType.getCreatedAt());
    dto.setUpdatedAt(ticketType.getUpdatedAt());
    return dto;
}

@Override
public GetEventDetailsResponseDto convertToGetEventDetailsResponseDto(Event event) {
    GetEventDetailsResponseDto dto = new GetEventDetailsResponseDto();
    dto.setId(event.getDomainId());
    dto.setName(event.getName());
    dto.setStart(event.getStart());
    dto.setEnd(event.getEnd());
    dto.setVenue(convertToVenueResponseDto(event.getVenue()));
    dto.setSalesStart(event.getSalesStart());
    dto.setSalesEnd(event.getSalesEnd());
    dto.setStatus(event.getStatus());
    dto.setTicketTypes(event.getTicketTypes().stream()
            .map(this::convertToGetEventDetailsTicketTypesResponseDto)
            .toList());
    dto.setCreatedAt(event.getCreatedAt());
    dto.setUpdatedAt(event.getUpdatedAt());
    return dto;
}
```

#### Summary

- Created `GetEventDetailsResponseDto` to represent complete event information
- Created `GetEventDetailsTicketTypeResponseDto` to represent ticket type details
- Added `convertToGetEventDetailsResponseDto`/`convertToGetEventDetailsTicketTypesResponseDto` to `EventService`

### Implement Controller Endpoint

In this lesson, we'll implement a controller endpoint that lets event organizers retrieve the details of a specific event.

#### Implement the Controller Endpoint

The get event endpoint needs to handle both successful and unsuccessful requests. Let's implement this in our `EventController`:

```java
@GetMapping(path = "/{eventId}")
public ResponseEntity<GetEventDetailsResponseDto> getEvent(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID eventId
) {
    // Get the user's ID from the JWT token
    UUID userId = parseUserId(jwt);

    // Call the service layer and transform the response
    return eventService.getEventForOrganizer(userId, eventId)
            .map(eventService::convertToGetEventDetailsResponseDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

Let's break down what's happening:

- We use `@GetMapping` with a path parameter `{eventId}` to capture the event identifier
- The `@PathVariable` annotation maps the URL path parameter to our method parameter
- We extract the user ID from the JWT token using our helper method
- We use the service layer to find the event, which returns an `Optional`
- We chain `map` operations to convert the event to a DTO and wrap it in a response
- If no event is found, we return a 404 Not Found response

#### Error Handling

Our endpoint handles two main cases:

- When the event exists and belongs to the organizer, it returns HTTP 200 with the event details
- When the event doesn't exist or doesn't belong to the organizer, it returns HTTP 404

This approach follows REST best practices by using standard HTTP status codes to communicate the outcome of the request.

#### Summary

- Implemented a GET endpoint to retrieve event details by ID
- Used Optional to handle cases where events aren't found

### Ui Testing

In this lesson, we'll verify that our get event endpoint functions correctly through the user interface.

By testing through the UI, we can ensure our endpoint not only returns data but also that the data is correctly displayed to users.

#### Testing the API Response

First, let's prepare our development environment:

1. Clean and compile the project to ensure no issues with Lombok:

```bash
./mvnw clean compile
```

You may notice some warnings about the use of `builder`, but since we're not using that feature, we can proceed.

2. Start the application and navigate to the organizer's landing page.

3. After logging in, go to the "Create an Event" page where you'll see your existing events.

#### Examining the Network Traffic

When clicking the "Edit" button for an event, we can observe the API request:

1. Open your browser's Developer Tools (F12) and select the Network tab

2. Click the "Edit" button for Test Event 2

3. Observe the GET request:

- URL pattern: `api/v1/events/{uid}`
- Response status: HTTP 200
- Response body contains:

```json
{
  "id": "...",
  "name": "Test Event 2",
  "start": "...",
  "end": "...",
  "venue": {...},
  "published": false,
  "ticketTypes": [...],
  "createdAt": "...",
  "updatedAt": "..."
}
```

#### Verifying UI Display

The UI should correctly display all event details:

- Event name
- Event dates
- Venue information
- Sales period dates
- Ticket types (including capacity)
- Publication status

The fact that all this information displays correctly confirms that:

- The API endpoint is working
- The response format is correct
- The UI can properly parse and display the data

#### Summary

- Tested the get event endpoints works in the user interface

## Update Event

### Update Design

In this lesson, we'll create the objects and interface declaration needed for implementing the update event endpoint, building on our existing event management functionality.

#### Design Overview

Let's start by examining what we need for our update functionality.

When updating an event, we want to replace all the event data with new data, except for system-managed fields like `id`, `createdAt`, and `updatedAt`.

Here's how we'll structure our update objects:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventRequest {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private UUID venueId;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<UpdateTicketTypeRequest> ticketTypes = new ArrayList<>();
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketTypeRequest {
    private UUID id;
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
}
```

#### Service Interface Declaration

We need to declare our update method in the `EventService` interface.

The method needs to:

- Take the organizer's ID to verify ownership
- Take the event ID to identify which event to update
- Accept the update request containing the new data
- Return the updated event

Here's the interface declaration:

```java
public interface EventService {
    // ... existing methods ...

    Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event);
}
```

#### Summary

- Added the `UpdateTicketTypeRequest` class
- Added the `updateEventForOrganizer` method to the `EventService` interface

### Exceptions

In this lesson, we'll implement the exceptions needed for updating events in our ticket platform. We'll create custom exceptions to handle various error scenarios that could occur during event updates, making our application more robust and user-friendly.

#### Custom Exceptions

Let's create three custom exceptions that extend our base `EventTicketException` class:

```java
public class EventNotFoundException extends EventTicketException {
    public EventNotFoundException() {
    }

    public EventNotFoundException(String message) {
        super(message);
    }

    public EventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventNotFoundException(Throwable cause) {
        super(cause);
    }

    public EventNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

```java
public class TicketTypeNotFoundException extends EventTicketException {
    public TicketTypeNotFoundException() {
    }

    public TicketTypeNotFoundException(String message) {
        super(message);
    }

    public TicketTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TicketTypeNotFoundException(Throwable cause) {
        super(cause);
    }

    public TicketTypeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

```java
public class EventUpdateException extends EventTicketException {
    public EventUpdateException() {
    }

    public EventUpdateException(String message) {
        super(message);
    }

    public EventUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventUpdateException(Throwable cause) {
        super(cause);
    }

    public EventUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

Each exception serves a specific purpose:

- `EventNotFoundException` - When a requested event doesn't exist
- `TicketTypeNotFoundException` - When a referenced ticket type can't be found
- `EventUpdateException` - For general update-related errors

#### Exception Handler

We'll add these exceptions to our global exception handler to ensure consistent error responses:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EventUpdateException.class)
    public ResponseEntity<ErrorDto> handleEventUpdateException(EventUpdateException ex) {
        log.error("Caught EventUpdateException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(resolve("error.event.update-failed"));
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TicketTypeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTicketTypeNotFoundException(TicketTypeNotFoundException ex) {
        log.error("Caught TicketTypeNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(resolve("error.ticket-type.not-found"));
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEventNotFoundException(EventNotFoundException ex) {
        log.error("Caught EventNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(resolve("error.event.not-found"));
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Other handler methods
}
```

All these exceptions return HTTP 400 Bad Request responses, as they represent client-side errors.

#### Summary

- Added the `EventNotFoundException` exception
- Added the `EventUpdateException` exception
- Added the `TicketTypeNotFoundException` exception
- Updated the `GlobalExceptionHandler` to handle the new exceptions

### Update Event Service

In this lesson, we'll implement the update event functionality in the service layer, which allows event organizers to modify existing events and their associated ticket types.

#### Service Layer Implementation

The update functionality needs to handle both the event details and its ticket types, ensuring data consistency and proper validation.

Let's implement the `updateEventForOrganizer` method in our service:

```java
@Override
@Transactional
  public Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event) {
      if(null == event.getId()) {
          throw new EventUpdateException("Event ID cannot be null");
      }

      if(!id.equals(event.getId())) {
          throw new EventUpdateException("Cannot update the ID of an event");
      }

      Event existingEvent = eventRepository
              .findByDomainIdAndOrganizerDomainId(id, organizerId)
              .orElseThrow(() -> new EventNotFoundException(
                      String.format("Event with ID '%s' does not exist", id))
              );

      Venue venue = venueRepository.findByDomainId(event.getVenueId())
              .orElseThrow(() -> new VenueNotFoundException(
                      String.format("Venue with ID '%s' not found", event.getVenueId()))
              );

      existingEvent.setName(event.getName());
      existingEvent.setStart(event.getStart());
      existingEvent.setEnd(event.getEnd());
      existingEvent.setVenue(venue);
      existingEvent.setSalesStart(event.getSalesStart());
      existingEvent.setSalesEnd(event.getSalesEnd());
      existingEvent.setStatus(event.getStatus());

      // UpdateTicketTypeRequest.id is the ticket type's domainId, not its internal id
      Set<UUID> requestTicketTypeDomainIds = event.getTicketTypes()
              .stream()
              .map(UpdateTicketTypeRequest::getId)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());

      existingEvent.getTicketTypes().removeIf(existingTicketType ->
              !requestTicketTypeDomainIds.contains(existingTicketType.getDomainId())
      );

      Map<UUID, TicketType> existingTicketTypesIndex = existingEvent.getTicketTypes().stream()
              .collect(Collectors.toMap(TicketType::getDomainId, Function.identity()));

      for(UpdateTicketTypeRequest ticketType : event.getTicketTypes()) {
          if(null == ticketType.getId()) {
              // Create
              TicketType ticketTypeToCreate = new TicketType();
              ticketTypeToCreate.setDomainId(UUID.randomUUID());
              ticketTypeToCreate.setName(ticketType.getName());
              ticketTypeToCreate.setPrice(ticketType.getPrice());
              ticketTypeToCreate.setDescription(ticketType.getDescription());
              ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
              ticketTypeToCreate.setEvent(existingEvent);
              existingEvent.getTicketTypes().add(ticketTypeToCreate);

          } else if(existingTicketTypesIndex.containsKey(ticketType.getId())) {
              // Update
              TicketType existingTicketType = existingTicketTypesIndex.get(ticketType.getId());
              existingTicketType.setName(ticketType.getName());
              existingTicketType.setPrice(ticketType.getPrice());
              existingTicketType.setDescription(ticketType.getDescription());
              existingTicketType.setTotalAvailable(ticketType.getTotalAvailable());
          } else {
              throw new TicketTypeNotFoundException(String.format(
                      "Ticket type with ID '%s' does not exist", ticketType.getId()
              ));
          }
      }

      return eventRepository.save(existingEvent);
  }
```

Note that `ticketType.getId()` throughout this method refers to `UpdateTicketTypeRequest.id`, which -- like every other DTO-facing ID -- is really the ticket type's `domainId`. We match it against `existingTicketType.getDomainId()`, never `existingTicketType.getId()` (the internal sequential key). New ticket types get a fresh `domainId` the same way new events do.

#### Handle Orphaned Types

We'll also need to update our `Event` entity to handle orphaned types:

```java
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    // ...

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketType> ticketTypes = new ArrayList<>();

    //...
}
```

#### Summary

- Implemented the `updateEventForOrganizer` method in the `EventServiceImpl` class
- Updated the `Event` class to handle orphaned `TicketTypes`

### Dtos And Conversion Methods

In this lesson, we'll implement the DTOs and mappers needed for updating events through our presentation layer, following the same pattern we used for creating events.

#### Data Transfer Objects

Let's create dedicated DTOs for updating events while maintaining separation from our create event DTOs.

First, let's create the ticket type update DTOs:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketTypeRequestDto {

    private UUID id;

    @NotBlank(message = "{validation.ticket-type.name.required}")
    private String name;

    @NotNull(message = "{validation.ticket-type.price.required}")
    @PositiveOrZero(message = "{validation.ticket-type.price.positive-or-zero}")
    private Double price;

    private String description;

    private Integer totalAvailable;
}
```

Now for the event update DTOs:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventRequestDto {

    @NotNull(message = "{validation.event.id.required}")
    private UUID id;

    @NotBlank(message = "{validation.event.name.required}")
    private String name;

    private LocalDateTime start;

    private LocalDateTime end;

    @NotNull(message = "{validation.event.venue.required}")
    private UUID venueId;

    private LocalDateTime salesStart;

    private LocalDateTime salesEnd;

    @NotNull(message = "{validation.event.status.required}")
    private EventStatusEnum status;

    @NotEmpty(message = "{validation.event.ticket-types.required}")
    @Valid
    private List<UpdateTicketTypeRequestDto> ticketTypes;
}
```

#### Response DTOs

`UpdateTicketTypeResponseDto` and `UpdateEventResponseDto` were always referenced by the mapper but never actually defined -- let's close that gap now. They mirror `GetEventDetailsTicketTypesResponseDto`/`GetEventDetailsResponseDto` exactly, since "the event as it looks right after updating it" is the same shape as "the event's full details":

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketTypeResponseDto {
    private UUID id;
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<UpdateTicketTypeResponseDto> ticketTypes = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Conversion Methods

```java
public interface EventService {
    // Existing methods...

    UpdateTicketTypeRequest convertFromDto(UpdateTicketTypeRequestDto dto);
    UpdateEventRequest convertFromDto(UpdateEventRequestDto dto);
    UpdateTicketTypeResponseDto convertToUpdateTicketTypeResponseDto(TicketType ticketType);
    UpdateEventResponseDto convertToUpdateEventResponseDto(Event event);
}
```

```java
@Override
public UpdateTicketTypeRequest convertFromDto(UpdateTicketTypeRequestDto dto) {
    UpdateTicketTypeRequest request = new UpdateTicketTypeRequest();
    request.setId(dto.getId());
    request.setName(dto.getName());
    request.setPrice(dto.getPrice());
    request.setDescription(dto.getDescription());
    request.setTotalAvailable(dto.getTotalAvailable());
    return request;
}

@Override
public UpdateEventRequest convertFromDto(UpdateEventRequestDto dto) {
    UpdateEventRequest request = new UpdateEventRequest();
    request.setId(dto.getId());
    request.setName(dto.getName());
    request.setStart(dto.getStart());
    request.setEnd(dto.getEnd());
    request.setVenueId(dto.getVenueId());
    request.setSalesStart(dto.getSalesStart());
    request.setSalesEnd(dto.getSalesEnd());
    request.setStatus(dto.getStatus());
    request.setTicketTypes(dto.getTicketTypes().stream()
            .map(this::convertFromDto)
            .toList());
    return request;
}

@Override
public UpdateTicketTypeResponseDto convertToUpdateTicketTypeResponseDto(TicketType ticketType) {
    UpdateTicketTypeResponseDto dto = new UpdateTicketTypeResponseDto();
    dto.setId(ticketType.getDomainId());
    dto.setName(ticketType.getName());
    dto.setPrice(ticketType.getPrice());
    dto.setDescription(ticketType.getDescription());
    dto.setTotalAvailable(ticketType.getTotalAvailable());
    dto.setCreatedAt(ticketType.getCreatedAt());
    dto.setUpdatedAt(ticketType.getUpdatedAt());
    return dto;
}

@Override
public UpdateEventResponseDto convertToUpdateEventResponseDto(Event event) {
    UpdateEventResponseDto dto = new UpdateEventResponseDto();
    dto.setId(event.getDomainId());
    dto.setName(event.getName());
    dto.setStart(event.getStart());
    dto.setEnd(event.getEnd());
    dto.setVenue(convertToVenueResponseDto(event.getVenue()));
    dto.setSalesStart(event.getSalesStart());
    dto.setSalesEnd(event.getSalesEnd());
    dto.setStatus(event.getStatus());
    dto.setTicketTypes(event.getTicketTypes().stream()
            .map(this::convertToUpdateTicketTypeResponseDto)
            .toList());
    dto.setCreatedAt(event.getCreatedAt());
    dto.setUpdatedAt(event.getUpdatedAt());
    return dto;
}
```

Notice `convertFromDto(UpdateTicketTypeRequestDto dto)` sets `request.setId(dto.getId())` directly -- no `domainId` translation needed here. `UpdateTicketTypeRequestDto.id` is already a `domainId` value coming *in* from the frontend, going to another plain `UUID` field on `UpdateTicketTypeRequest`, no entity involved on either side. The `getDomainId()` translation is only needed going the other direction, from an entity out to a response DTO.

#### Summary

- Defined `UpdateTicketTypeResponseDto` and `UpdateEventResponseDto`, closing a gap that was always implied but never written out
- Added `convertFromDto`/`convertToUpdateEventResponseDto`/`convertToUpdateTicketTypeResponseDto` to `EventService`

### Event Controller

In this lesson, we'll implement a REST endpoint to update events.

#### Understanding the Update Event Endpoint

The update event endpoint uses HTTP PUT to support full updates of event resources. Let's look at how we implement this endpoint in our event controller:

```java
@PutMapping(path = "/{eventId}")
public ResponseEntity<UpdateEventResponseDto> updateEvent(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID eventId,
        @Valid @RequestBody UpdateEventRequestDto updateEventRequestDto){
    UpdateEventRequest updateEventRequest = eventService.convertFromDto(updateEventRequestDto);
    UUID userId = parseUserId(jwt);

    Event updatedEvent = eventService.updateEventForOrganizer(
            userId, eventId, updateEventRequest
    );

    UpdateEventResponseDto updateEventResponseDto = eventService.convertToUpdateEventResponseDto(updatedEvent);

    return ResponseEntity.ok(updateEventResponseDto);
}
```

#### Summary

- Implemented the update event endpoint on the `EventController`

### Ui Testing

In this lesson, we'll test the user interface for updating events in our ticketing platform.

We'll use the browser's development tools to inspect the HTTP requests and responses as we modify an existing event, ensuring our update functionality works correctly.

#### Testing the Update Event UI

Let's walk through the process of updating an event through the user interface and verify that our endpoint is working correctly.

First, we need to launch our application and navigate to the UI:

- Run `mvn clean compile` to ensure no issues with Lombok
- Start the Spring Boot application
- Navigate to the organizer's landing page
- Log in with organizer credentials
- Click through to the list events page

When viewing an existing event with complete information (dates, venue, ticket types), clicking the edit button shows us the edit event page populated with current event data via the `GET /api/v1/events/{id}` endpoint.

#### Making Updates

Let's modify various aspects of the event to test our update functionality:

- Update the event name by adding "updated" suffix
- Adjust event dates and times
- Change the selected venue
- Change ticket sales period
- Update existing ticket type details
- Add a new ticket type

#### Verifying the Update

After the update, we can verify the changes by returning to the edit page and examining the fresh GET request, confirming all our modifications were saved correctly.

#### Summary

- Tested the event update functionality in the user interface

## Delete Event

### Delete Event Service

In this lesson, we'll implement the `deleteEventForOrganizer` method in our service layer, allowing event organizers to delete their events from the system.

#### Service Implementation

Let's break down the implementation of the delete event functionality:

```java
  void deleteEventForOrganizer(UUID organizerId, UUID eventId);
```

```java
@Override
@Transactional
public void deleteEventForOrganizer(UUID organizerId, UUID id) {
    // Get the event and delete it if found
    getEventForOrganizer(organizerId, id).ifPresent(eventRepository::delete);
}
```

This implementation:

- Uses the `@Transactional` annotation to ensure database operations are atomic
- Leverages the existing `getEventForOrganizer` method to verify ownership
- Only deletes the event if it exists and belongs to the specified organizer
- Returns void, silently handling cases where the event doesn't exist

The code follows a simple but effective pattern:

1. Reuses the existing `getEventForOrganizer` method which checks both existence and ownership
2. Uses the `ifPresent` method to only execute the delete operation if an event is found
3. Passes a method reference to `eventRepository::delete` for clean, functional programming style

#### Security Considerations

The delete operation is secure because:

- It verifies the organizer owns the event before deletion
- Uses the same authorization check as other event operations
- Operates within a transaction to maintain data consistency
- Prevents unauthorized users from deleting events they don't own

#### Error Handling

The current implementation takes a silent failure approach when:

- The event doesn't exist
- The organizer doesn't own the event
- The event ID is invalid

This approach might need to be revisited if explicit error feedback becomes a requirement.

#### Summary

- Added the `deleteEventForOrganizer` method to the service layer

### Delete Event Endpoint

In this lesson, we'll build the delete event endpoint in our REST controller.

#### Implementation

The delete endpoint follows REST conventions by using the HTTP DELETE method and accepting the event ID as a path variable. Let's add the endpoint to our `EventController`:

```java
@DeleteMapping(path = "/{eventId}")
public ResponseEntity<Void> deleteEvent(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID eventId
) {
    UUID userId = parseUserId(jwt);
    eventService.deleteEventForOrganizer(userId, eventId);
    return ResponseEntity.noContent().build();
}
```

#### Summary

- Implemented the delete event endpoint

### Ui Testing

In this lesson, we'll test the delete event functionality through the user interface.

#### Testing the Delete Operation

Before testing the delete functionality in the browser, we should ensure our application compiles correctly:

```bash
# Run these commands in your terminal
./mvnw clean compile
```

#### User Interface Elements

The delete functionality appears as a delete button in the bottom right corner of each event card.

When clicked, it triggers a confirmation dialog to prevent accidental deletions:

- The dialog displays the event name and asks for confirmation
- Users can choose to cancel (which closes the dialog) or continue with the deletion

#### Network Communication

To observe the backend communication, open your browser's developer tools and select the Network tab.

When deleting an event:

1. The frontend sends a DELETE request to `/api/v1/events/{eventId}`
2. The server responds with HTTP status code 204 (No Content) on success
3. The UI automatically refreshes to show the updated list of events

#### Summary

- Tested the delete event functionality works in the frontend

## List Published Events

### List Published Events Service

In this lesson, we'll implement the list published events service layer.

#### Service Layer Implementation

Let's add the `listPublishedEvents` method to our `EventService` interface:

```java
public interface EventService {
    // ... existing methods ...

    Page<Event> listPublishedEvents(Pageable pageable);
}
```

Next, we'll implement the method in our `EventServiceImpl` class:

```java
@Override
public Page<Event> listPublishedEvents(Pageable pageable) {
    // Use the repository to find events with PUBLISHED status
    return eventRepository.findByStatus(EventStatusEnum.PUBLISHED, pageable);
}
```

#### Repository Layer Implementation

To support our service layer, we need to add a method to our `EventRepository` interface that can find events by their status:

```java
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // ... existing methods ...

    // Find events by their status (e.g., PUBLISHED)
    @Query("SELECT e FROM Event e WHERE e.status = :status")
    Page<Event> findByStatus(@Param("status") EventStatusEnum status, Pageable pageable);
}
```

The `findByStatus` method follows Spring Data JPA's method naming convention, which automatically generates the correct query based on the method name.

#### Summary

- Added the `findByStatus` method to the `EventRepository` interface
- Added the `listPublishedEvents` method to the `EventService` interface
- Implemented the `listPublishedEvents` method in the `EventServiceImpl` class

### Dtos And Conversion Methods

In this lesson, we'll implement the DTOs and mappers needed for displaying published events on the attendee landing page.

#### Implement the DTO

Let's create the `ListPublishedEventResponseDto` class:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListPublishedEventResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
}
```

This DTO includes only the fields needed for the event cards on the landing page: the event's ID, name, start and end times, and venue.

#### Add a Conversion Method

```java
public interface EventService {
    // ... existing methods ...

    ListPublishedEventResponseDto convertToListPublishedEventResponseDto(Event event);
}
```

```java
@Override
public ListPublishedEventResponseDto convertToListPublishedEventResponseDto(Event event) {
    ListPublishedEventResponseDto dto = new ListPublishedEventResponseDto();
    dto.setId(event.getDomainId());
    dto.setName(event.getName());
    dto.setStart(event.getStart());
    dto.setEnd(event.getEnd());
    dto.setVenue(convertToVenueResponseDto(event.getVenue()));
    return dto;
}
```

#### Summary

- Created the `ListPublishedEventResponseDto` class
- Added `convertToListPublishedEventResponseDto` to `EventService`

### List Published Event Endpoint

In this lesson, we'll create an endpoint that allows anyone to view published events, enabling potential attendees to browse available events without needing to log in first.

#### Creating the Published Events Controller

Let's create a dedicated controller for published events, keeping it separate from our existing event management endpoints.

```java
@RestController
@RequestMapping(path = "/api/v1/published-events")
@RequiredArgsConstructor
public class PublishedEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<ListPublishedEventResponseDto>> listPublishedEvents(Pageable pageable) {
        // Map the events to DTOs and return them in the response
        return ResponseEntity.ok(eventService.listPublishedEvents(pageable)
            .map(eventService::convertToListPublishedEventResponseDto));
    }
}
```

#### Configuring Public Access

To make the endpoint accessible without authentication, we need to update our security configuration.

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            UserProvisioningFilter userProvisioningFilter) throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                authorize
                    // Allow public access to published events
                    .requestMatchers(HttpMethod.GET, "/api/v1/published-events").permitAll()
                    // All other endpoints require authentication
                    .anyRequest().authenticated())
            // ... rest of the configuration
        return http.build();
    }
}
```

#### Summary

- Created the `PublishedEventController` class
- Implemented the list published events endpoint
- Made the list published events endpoint public

### Ui Testing

Now that we've built our list published events endpoint, let's validate it works correctly by testing it through our user interface, where we'll see our events displayed on the landing page.

#### Setting Up for Testing

Before we begin testing in the browser, we need to ensure our application is running correctly:

1. Clean and compile the project to ensure everything is in order
2. Start the application
3. Navigate to `http://localhost:5173` in your browser

#### Testing the User Interface

The landing page has been updated to showcase published events to attendees.

The page now features:

- A hero image at the top
- A login button
- Event cards displaying published events

Each event card shows:

- A placeholder random image
- The event name
- Venue details
- Event dates

An important aspect to test is that these events are visible without requiring authentication.

#### Verifying the API Call

We can confirm our endpoint is working correctly by examining the network traffic:

1. Open your browser's developer tools
2. Navigate to the Network tab
3. Filter to show API calls
4. Refresh the page

You should see a successful HTTP 200 response from `api/v1/published-events`.

The response includes events marked as published, with our test demonstrating that both "Test Event 3" and "Test Event 4" are visible.

To verify the authentication requirement is working as intended:

1. Log in as an organizer
2. Confirm you can see the same events in the organizer view
3. Log out
4. Verify the events remain visible on the landing page

#### Summary

- Tested the list published events endpoint works by using the user interface

## Published Events Search

### Search Service Layer

In this lesson, we'll implement search functionality for published events using PostgreSQL's text search capabilities in the service layer. This will allow users to find events by searching terms that match event names and venues.

#### PostgreSQL Text Search Query

Let's look at the SQL query that powers our search functionality:

```java
@Query(value = "SELECT e.* FROM events e " +
    "JOIN venues v ON v.id = e.venue_id WHERE " +
    "e.status = 'PUBLISHED' AND " +
    "to_tsvector('english', COALESCE(e.name, '') || ' ' || COALESCE(v.name, '') || ' ' || COALESCE(v.city, '')) " +
    "@@ plainto_tsquery('english', :searchTerm)",
    countQuery = "SELECT count(*) FROM events e " +
        "JOIN venues v ON v.id = e.venue_id WHERE " +
        "e.status = 'PUBLISHED' AND " +
        "to_tsvector('english', COALESCE(e.name, '') || ' ' || COALESCE(v.name, '') || ' ' || COALESCE(v.city, '')) " +
        "@@ plainto_tsquery('english', :searchTerm)",
    nativeQuery = true)
Page<Event> searchEvents(@Param("searchTerm") String searchTerm, Pageable pageable);
```

This query:

- Joins `events` to `venues` via `venue_id`, now that venue details live in their own table
- Uses PostgreSQL's `to_tsvector` to create a searchable text vector from the event name plus the venue's name and city
- Applies `plainto_tsquery` to convert the search term into a format PostgreSQL can use
- Only returns events with PUBLISHED status
- Supports pagination through Spring Data JPA's `Pageable` parameter

#### Service Layer Implementation

The service layer implementation connects the repository query to our application:

```java
@Override
public Page<Event> searchPublishedEvents(String query, Pageable pageable) {
    return eventRepository.searchEvents(query, pageable);
}
```

#### Summary

- Added the `searchEvents` custom query to the `EventRepository` interface
- Added the `searchPublishedEvents` method to the `EventService` interface
- Implemented the `searchPublishedEvents` method in the `EventServiceImpl` class

### Search Controller Updates

In this lesson, we'll enhance the published events controller to support optional search functionality, allowing users to find events by searching through event names and venues.

#### Understanding Optional Search Parameters

The `@RequestParam` annotation in Spring Boot lets us add optional parameters to our endpoints.

By setting `required = false`, we tell Spring that the parameter is optional, meaning the endpoint will work both with and without the search parameter.

Here's how we update our controller:

```java
@GetMapping
public ResponseEntity<Page<ListPublishedEventResponseDto>> listPublishedEvents(
    @RequestParam(required = false) String q,
    Pageable pageable) {

  Page<Event> events;
  if(null != q && !q.trim().isEmpty()) {
    events = eventService.searchPublishedEvents(q, pageable);
  } else {
    events = eventService.listPublishedEvents(pageable);
  }

  return ResponseEntity.ok(
      events.map(eventService::convertToListPublishedEventResponseDto)
  );
}
```

#### Search Flow Implementation

The controller now handles two different scenarios:

- When a search query is provided, it calls `searchPublishedEvents` with the query
- When no search query is provided, it calls `listPublishedEvents` to show all published events

The code checks if the query parameter `q` exists and isn't empty after trimming whitespace.

We use the query parameter name `q` as it's a common convention in search APIs and keeps our URLs clean and readable.

#### Summary

- Added the search published events endpoint the `PublishedEventsController`

### Ui Testing

In this lesson, we'll explore how to test the published event search functionality through the user interface.

Testing through the UI provides a practical way to validate that our search feature works as expected from the user's perspective.

#### Manual UI Testing

The search functionality allows users to find published events by matching text in the event details.

Let's walk through the testing process:

1. First, ensure your application is running by executing a clean compile:

```bash
./mvnw clean compile
```

2. Navigate to your frontend application where you should see the published events displayed (in this case, "Test Event Three" and "Test Event Four").

3. To properly test and monitor the search functionality, open your browser's developer tools (F12 in most browsers) and select the Network tab.

#### Testing Search Scenarios

Let's test different search scenarios to verify the functionality:

1. Basic Search Test:

- Enter "test" in the search field
- Click search
- Verify both events are returned
- Check the network tab shows a 200 status code

2. Specific Event Search:

- Enter "test event three"
- Verify only "Test Event Three" is displayed
- Enter "test event four"
- Verify only "Test Event Four" is displayed

3. Venue Details Search:

- Enter "details" in the search field
- Verify only events containing "details" in their venue information are displayed

4. Empty Search:

- Clear the search field
- Click search
- Verify all published events are displayed

When testing through the UI, pay attention to:

- The immediate response of the interface
- The network requests being made
- The accuracy of the returned results
- The handling of different search terms

#### Summary

- Tested the search event functionality using the user interface

## Get Published Event

### Service Layer

In this lesson, we'll implement the get published event functionality in our service layer.

#### Service Layer Implementation

Let's add the `getPublishedEvent` method to our event service interface:

```java
public interface EventService {
    // Other methods...
    Optional<Event> getPublishedEvent(UUID id);
}
```

In the implementation class, we'll use our repository to find events that match both the ID and published status:

```java
@Override
public Optional<Event> getPublishedEvent(UUID id) {
    // Only return events that are both published and match the domain ID
    return eventRepository.findByDomainIdAndStatus(id, EventStatusEnum.PUBLISHED);
}
```

#### Repository Extension

To support this functionality, we need to add a custom query method to our repository:

```java
public interface EventRepository extends JpaRepository<Event, Long> {
    // Other methods...
    @Query("SELECT e FROM Event e WHERE e.domainId = :domainId AND e.status = :status")
    Optional<Event> findByDomainIdAndStatus(@Param("domainId") UUID domainId, @Param("status") EventStatusEnum status);
}
```

This method explicitly filters by both domain ID and status.

#### Summary

- Added the `findByDomainIdAndStatus` method to the `EventRepository`
- Added the `getPublishedEvent` method to the `EventService` interface
- Implemented the `getPublishedEvent` method in the `EventServiceImpl` class

### Dtos And Conversion Methods

In this lesson, we'll implement the DTOs and mappers needed for the get published event endpoint in the presentation layer.

#### Implement the DTOs

Let's look at what we need to include in our DTOs:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPublishedEventDetailsResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    private List<GetPublishedEventDetailsTicketTypesResponseDto> ticketTypes = new ArrayList<>();
}
```

For ticket types, we'll create a separate DTO with only the necessary fields:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPublishedEventDetailsTicketTypesResponseDto {
    private UUID id;
    private String name;
    private Double price;
    private String description;
}
```

#### Conversion Methods

```java
public interface EventService {
    // ... existing methods ...

    GetPublishedEventDetailsTicketTypesResponseDto convertToGetPublishedEventDetailsTicketTypesResponseDto(TicketType ticketType);
    GetPublishedEventDetailsResponseDto convertToGetPublishedEventDetailsResponseDto(Event event);
}
```

```java
@Override
public GetPublishedEventDetailsTicketTypesResponseDto convertToGetPublishedEventDetailsTicketTypesResponseDto(TicketType ticketType) {
    GetPublishedEventDetailsTicketTypesResponseDto dto = new GetPublishedEventDetailsTicketTypesResponseDto();
    dto.setId(ticketType.getDomainId());
    dto.setName(ticketType.getName());
    dto.setPrice(ticketType.getPrice());
    dto.setDescription(ticketType.getDescription());
    return dto;
}

@Override
public GetPublishedEventDetailsResponseDto convertToGetPublishedEventDetailsResponseDto(Event event) {
    GetPublishedEventDetailsResponseDto dto = new GetPublishedEventDetailsResponseDto();
    dto.setId(event.getDomainId());
    dto.setName(event.getName());
    dto.setStart(event.getStart());
    dto.setEnd(event.getEnd());
    dto.setVenue(convertToVenueResponseDto(event.getVenue()));
    dto.setTicketTypes(event.getTicketTypes().stream()
            .map(this::convertToGetPublishedEventDetailsTicketTypesResponseDto)
            .toList());
    return dto;
}
```

#### Summary

- Added the `GetPublishedEventDetailsResponseDto` and `GetPublishedEventDetailsTicketTypesResponseDto` DTO classes
- Added `convertToGetPublishedEventDetailsResponseDto` and `convertToGetPublishedEventDetailsTicketTypesResponseDto` to `EventService`

### Get Published Event Endpoint

In this lesson, we'll implement the get published event details endpoint.

#### Implementing the Get Published Event Endpoint

We'll add a new endpoint to our `PublishedEventController` class that retrieves the details of a specific published event.

```java
@GetMapping(path = "/{eventId}")
public ResponseEntity<GetPublishedEventDetailsResponseDto> getPublishedEventDetails(
    @PathVariable UUID eventId
) {
    return eventService.getPublishedEvent(eventId)
        .map(eventService::convertToGetPublishedEventDetailsResponseDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

Let's break down what this code does:

- The `@GetMapping` annotation with a path variable defines the URL pattern for this endpoint
- The method takes an `eventId` parameter that is extracted from the URL path
- It calls the `eventService.getPublishedEvent()` method to fetch the event details
- The result is mapped to our DTO using `EventService.convertToGetPublishedEventDetailsResponseDto`
- If an event is found, it returns a 200 OK response with the event details
- If no event is found, it returns a 404 Not Found response

#### Configuring Security

We need to ensure this endpoint is publicly accessible. Let's update the security configuration:

```java
.requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
```

This configuration uses a wildcard (`**`) to match any path after `/api/v1/published-events/`, including our new endpoint that includes the event ID.

#### Summary

- Implemented the get published event endpoint
- Updated `SecurityConfig` to make calls to make the get published event endpoint public

### Ui Testing

In this lesson, we'll test the get published event functionality using the user interface, ensuring our endpoint works correctly and delivers the expected event information to users.

#### Testing Through the User Interface

First, we need to compile and start our backend application:

```bash
# Clean and compile the application
mvn clean compile

# Start the Spring Boot application
mvn spring-boot:run
```

Once the application is running, we can navigate to the attendee landing page where we should see our test events listed.

When clicking on an event (like "Test Event Three"), the application makes a network request to our `/api/v1/published-events/{id}` endpoint.

Let's examine what happens in this request:

- The request returns a HTTP 200 OK status
- No authorization header is present, confirming the endpoint is public
- The response includes complete event details and ticket types

The UI displays several key pieces of information from the response:

- Event name
- Venue details
- Event dates
- Event image
- Ticket types with their respective prices
- Purchase options (though not yet implemented)

#### Summary

- Tested the get published event endpoint through the user interface

## New Users And Roles

### Attendee User

In this lesson, we'll expand our Keycloak configuration by adding an attendee user and corresponding roles.

#### Adding the Attendee User

Creating a new user in Keycloak involves setting up basic user information and credentials.

Let's create a new attendee user with these steps:

1. Navigate to the Keycloak Admin Console at `localhost:1990`
2. Select the "event-ticket-platform" realm
3. Go to Users and click "Add User"
4. Fill in the following details:
   - Username: `attendee`
   - Email: `attendee@yourdomain.com`
   - First Name: `attendee`
   - Last Name: `user`

After creating the user, we need to set up their password:

1. Go to the Credentials tab
2. Set the password as "password" (Note: This is for development only)
3. Disable the "Temporary" option to prevent password reset requirements

#### Creating and Assigning Roles

Roles in Keycloak help us manage user permissions effectively.

We'll create two roles:

```plaintext
ROLE_ATTENDEE    // For regular event attendees
ROLE_ORGANIZER   // For event organizers
```

To create these roles:

1. Navigate to Realm Roles
2. Click "Create Role"
3. Enter `ROLE_ATTENDEE` for the first role
4. Repeat the process with `ROLE_ORGANIZER`

Next, we'll assign these roles:

1. For the attendee user:

   - Go to Users → attendee → Role Mapping
   - Click "Assign Role"
   - Select `ROLE_ATTENDEE`

2. For the organizer user:

   - Go to Users → organizer → Role Mapping
   - Click "Assign Role"
   - Select `ROLE_ORGANIZER`

#### Summary

- Added the attendee user to Keycloak
- Added the `ROLE_ATTENDEE` role to Keycloak
- Added the `ROLE_ORGANIZER` role to Keycloak

### Staff User

In this lesson, we'll expand our user management system by adding a staff user and role to Keycloak.

#### Adding the Staff User

Creating a new staff user in Keycloak follows the same pattern we used for our other users, but with staff-specific details:

1. Navigate to the Users page in the Keycloak admin console
2. Click "Add User" and provide these details:
   - Username: `staff`
   - Email: `staff@example.com`
   - First Name: `staff`
   - Last Name: `user`

#### Creating the Staff Role

The staff role will be used to control access to staff-specific features in our application:

1. Navigate to "Realm Roles" in the sidebar
2. Click "Create Role"
3. Set the role name as `ROLE_STAFF`
4. Save the role

#### Assigning the Role

To connect our new user with their role:

1. Go back to Users and select the staff user
2. Click on "Role Mapping"
3. Choose "Assign Role"
4. Filter by realm roles
5. Select `ROLE_STAFF`
6. Confirm the assignment

#### Summary

- Added the staff user to Keycloak
- Added the `ROLE_STAFF` role to Keycloak

## Purchase Ticket

### Qr Code Generation Design

In this lesson, we'll design the QR code generation functionality that will be used to create scannable tickets for event attendees.

#### Service Design Overview

The QR code generation process needs to be flexible and maintainable. Let's examine the key components we've established:

- The `QrCode` entity has been updated to store QR codes as text in the database
- The `domainId` field is manually set to the same UUID that gets embedded in the QR image itself, rather than a random one generated after the fact
- A `QrCodeRepository` has been created for database operations
- A `QrCodeService` interface defines the core functionality
- A `QrCodeGenerationException` handles error cases

#### Database Storage Considerations

We're storing QR codes in the database as base64 encoded strings using a `TEXT` column type. Here's why this approach works for our current needs:

- The `TEXT` type allows for variable-length storage without the 255 character limit of `VARCHAR`
- Base64 encoding lets us store binary image data as text
- This approach requires minimal additional infrastructure

However, it's worth noting that this solution may need to be revised as the system scales, since storing images in the database can impact performance.

#### Error Handling

We've implemented a dedicated exception type for QR code generation failures:

```java

public class QrCodeGenerationException extends EventTicketException {

  public QrCodeGenerationException() {
    super();
  }

  public QrCodeGenerationException(String message) {
    super(message);
  }

  public QrCodeGenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  public QrCodeGenerationException(Throwable cause) {
    super(cause);
  }

  public QrCodeGenerationException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
```

```java
@ExceptionHandler(QrCodeGenerationException.class)
public ResponseEntity<ErrorDto> handleQrCodeGenerationException(QrCodeGenerationException ex) {
    log.error("Caught QrCodeGenerationException", ex);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setError(resolve("error.qr-code.generation-failed"));
    return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
}
```

This provides clear error messages to clients while using the appropriate 500 status code, since QR code generation failures are server-side issues.

#### Service Interface

The `QrCodeService` interface is intentionally simple:

```java
public interface QrCodeService {
    QrCode generateQrCode(Ticket ticket);
}
```

#### Summary

- Updated types in the `QrCode` entity
- Added `QrCodeGenerationException`
- Added `QrCodeService`
- Added `QrCodeRepository`

### Qr Code Generation Service Layer

In this lesson, we'll implement the QR code generation functionality in our ticket platform's service layer using the ZXing library.

#### Setting Up QR Code Dependencies

To generate QR codes, we need to add the ZXing library dependencies to our project:

```xml
<dependency>
  <groupId>com.google.zxing</groupId>
  <artifactId>core</artifactId>
  <version>3.5.1</version>
</dependency>
<dependency>
  <groupId>com.google.zxing</groupId>
  <artifactId>javase</artifactId>
  <version>3.5.1</version>
</dependency>
```

#### Creating the QR Code Writer Bean

Let's create a configuration class to provide a `QRCodeWriter` bean:

```java
@Configuration
public class QrCodeConfig {
    @Bean
    public QRCodeWriter qrCodeWriter() {
        return new QRCodeWriter();
    }
}
```

#### Implementing the QR Code Service

The QR code service implementation handles generating and storing QR codes:

```java
@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {
    private static final int QR_HEIGHT = 300;
    private static final int QR_WIDTH = 300;

    private final QRCodeWriter qrCodeWriter;
    private final QrCodeRepository qrCodeRepository;

    @Override
    public QrCode generateQrCode(Ticket ticket) {
        try {
            // Generate a unique ID for the QR code
            UUID uniqueId = UUID.randomUUID();
            String qrCodeImage = generateQrCodeImage(uniqueId);

            // Create and save the QR code entity
            QrCode qrCode = new QrCode();
            qrCode.setDomainId(uniqueId);
            qrCode.setStatus(QrCodeStatusEnum.ACTIVE);
            qrCode.setValue(qrCodeImage);
            qrCode.setTicket(ticket);

            return qrCodeRepository.saveAndFlush(qrCode);
        } catch(IOException | WriterException ex) {
            throw new QrCodeGenerationException("Failed to generate QR Code", ex);
        }
    }

    private String generateQrCodeImage(UUID uniqueId) throws WriterException, IOException {
        // Create a bit matrix for the QR code
        BitMatrix bitMatrix = qrCodeWriter.encode(
            uniqueId.toString(),
            BarcodeFormat.QR_CODE,
            QR_WIDTH,
            QR_HEIGHT
        );

        // Convert to BufferedImage
        BufferedImage qrCodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Convert to base64 string
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(qrCodeImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }
}
```

#### Summary

- Added the `zxing` dependency
- Implemented QRCode generation

### Ticket Purchase Service Layer

In this lesson, we'll build the service layer functionality for purchasing tickets, implementing concurrent access handling and QR code generation to create a robust ticket purchasing system.

#### Understanding the Purchase Flow

The ticket purchase process involves several key steps:

1. Finding the user and ticket type in the database
2. Checking if tickets are still available
3. Creating a new ticket record
4. Generating a QR code for the ticket

Let's implement this in our `TicketTypeServiceImpl` class:

```java
@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
        // Look up the user
        User user = userRepository.findByDomainId(userId)
            .orElseThrow(() -> new UserNotFoundException(
                String.format("User with ID %s was not found", userId)
            ));

        // Get ticket type with pessimistic lock
        TicketType ticketType = ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)
            .orElseThrow(() -> new TicketTypeNotFoundException(
                String.format("Ticket type with ID %s was not found", ticketTypeId)
            ));

        // Check ticket availability -- ticketType.getId() here is the resolved entity's
        // internal sequential id, used purely as an internal join key against tickets.ticket_type_id
        int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
        Integer totalAvailable = ticketType.getTotalAvailable();

        if(purchasedTickets + 1 > totalAvailable) {
            throw new TicketsSoldOutException();
        }

        // Create new ticket
        Ticket ticket = new Ticket();
        ticket.setDomainId(UUID.randomUUID());
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setTicketType(ticketType);
        ticket.setPurchaser(user);

        // Save and generate QR code
        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);

        return ticketRepository.save(savedTicket);
    }
}
```

#### Handling Concurrent Access

To prevent overselling tickets when multiple users try to purchase at the same time, we use a pessimistic lock:

```java
@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    @Query("SELECT tt FROM TicketType tt WHERE tt.domainId = :domainId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketType> findByDomainIdWithLock(@Param("domainId") UUID domainId);
}
```

`ticketTypeId` here is the value from the purchase URL -- the ticket type's `domainId` -- so the lookup (and the lock) needs to go through `domainId`, not the internal `id`.

We also need a `TicketRepository` for the availability check and to save the new ticket:

```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId")
    int countByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);
}
```

Unlike every other lookup we've written, this one filters on `ticketType.id` -- the internal sequential key -- rather than a `domainId`. That's deliberate: by this point we've already resolved the real `TicketType` entity (via `findByDomainIdWithLock`), so `ticketType.getId()` is a trusted internal value, not something handed to us from outside. Counting rows by joining on the internal integer key is exactly the kind of thing that key exists for.

This ensures that when one user is purchasing a ticket, other users must wait until the transaction completes before they can access the same ticket type.

#### Error Handling

We handle several error cases:

- User not found
- Ticket type not found
- Tickets sold out
- QR code generation failure

These are caught and handled by our global exception handler to provide clear error messages to users.

#### Summary

- Implemented the initial purchase ticket functionality

### Ticket Purchase Endpoint

In this lesson, we'll implement the ticket purchase endpoint.

#### Creating the Controller

Let's create a `TicketTypeController` that will handle ticket purchase requests:

```java
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/events/{eventId}/ticket-types")
public class TicketTypeController {

  private final TicketTypeService ticketTypeService;

  @PostMapping(path = "/{ticketTypeId}/tickets")
  public ResponseEntity<Void> purchaseTicket(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID ticketTypeId
  ) {
    // Purchase the ticket using the service
    ticketTypeService.purchaseTicket(parseUserId(jwt), ticketTypeId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
```

#### Understanding the Implementation

The purchase ticket endpoint follows RESTful conventions and includes several important components:

- The endpoint URL structure follows the pattern `/api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets`

- We use `@PostMapping` since we're creating a new ticket resource

- The controller accepts a JWT token to identify the purchaser and a ticket type ID to specify which type of ticket to purchase

- We return HTTP 204 (No Content) on success since we don't need to send any information back to the client

#### Summary

- Implemented the purchase ticket endpoint

### Ui Testing

In this lesson, we'll test our ticket purchasing functionality through the user interface to verify the purchase flow works correctly and validate both the frontend and backend components work together properly.

#### Testing Through the UI

The first step in testing our ticket purchase functionality is to start our backend application.

We'll begin by cleaning and compiling our project using Maven, then starting the Spring Boot application.

Once the application is running, we can access the attendee landing page in our browser.

Here's the sequence of steps to test the ticket purchase:

- Select an event from the landing page
- Choose a ticket type (e.g. standard entry)
- Open browser dev tools to monitor network requests
- Click "Purchase Ticket" button
- Login with attendee credentials
- Enter mock payment information
- Complete purchase

#### Troubleshooting Issues

When testing the purchase flow, we may encounter errors that need debugging.

If you receive an HTTP 500 error after attempting to purchase, check the server logs.

In our case, we discovered a null pointer exception in the `QrCodeService` due to a missing `final` keyword on the repository field.

After fixing the code and restarting the server, the purchase flow should complete successfully with an HTTP 204 response.

#### Verifying the Purchase

To confirm the ticket purchase worked correctly:

```sql
-- Check the tickets table
SELECT * FROM tickets;

-- Check the QR codes table
SELECT * FROM qr_codes;
```

When examining the database records, verify:

- The purchaser ID matches the logged in user
- The ticket type ID matches the selected ticket
- A QR code was generated and stored
- The timestamps are correct

#### Summary

- Fixed NPE bug
- Created a ticket in the database
- Created a QRCode in the database

## Role Based Access

### Extract Roles

In this lesson we're going to extract the roles from the user's access token.

#### Understanding JWT Claims

The JWT used for authentication contains useful information about the user called claims.

Among these claims is the `realm_access` claim which contains the roles assigned to the user in Keycloak.

When we decode a JWT at jwt.io, we can see claims like `ROLE_ORGANIZER`, `ROLE_ATTENDEE`, and `ROLE_STAFF` under the `realm_access.roles` section.

#### Implementing Role Extraction

To extract roles from the JWT, we need to create a custom converter that transforms the JWT into Spring Security's internal representation.

Here's how we implement the `JwtAuthenticationConverter`:

```java
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

  @Override
  public JwtAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities);
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");

    if(null == realmAccess || !realmAccess.containsKey("roles")) {
      return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>)realmAccess.get("roles");

    return roles.stream()
        .filter(role -> role.startsWith("ROLE_"))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}
```

The converter does the following:

1. Gets the `realm_access` claim from the JWT
2. Checks if the claim exists and contains roles
3. Extracts the roles and converts them to Spring Security's `SimpleGrantedAuthority` objects

#### Configuring Security to Use the Converter

We need to update our security configuration to use our custom converter:

```java
@Bean
public SecurityFilterChain filterChain(
    HttpSecurity http,
    UserProvisioningFilter userProvisioningFilter,
    JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
  http
      .authorizeHttpRequests(authorize ->
          authorize
              .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
              // Catch all rule
              .anyRequest().authenticated())
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session ->
          session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .oauth2ResourceServer(oauth2 ->
          oauth2.jwt(jwt ->
              jwt.jwtAuthenticationConverter(jwtAuthenticationConverter) // Add this
          ))
      .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

  return http.build();
}
```

The key change is adding the `jwtAuthenticationConverter` to the JWT configuration.

#### Summary

- Added a custom JWT converter to extract a user's roles
- Updated `SecurityConfig` to use the JWT converter

### Lock Down Endpoints

In this lesson, we're going to lock down certain endpoints to only certain roles.

#### Configuring Role-Based Access

Spring Security allows us to restrict access to API endpoints based on user roles.

In our application, we want to ensure that only users with the organizer role can access the events controller endpoints.

Here's how we implement this in the `SecurityConfig` class:

```java
.authorizeHttpRequests(authorize ->
    authorize
        .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
        .requestMatchers("/api/v1/events").hasRole("ORGANIZER")
        // Catch all rule
        .anyRequest().authenticated())
```

The `.hasRole("ORGANIZER")` method is used to restrict access to users with the organizer role.

When using `hasRole()`, Spring Security automatically adds the `ROLE_` prefix to the role name, so we don't need to include it in our configuration.

#### Testing Role-Based Access

To verify our role-based access is working correctly, we can test with different user roles:

1. When logged in as an organizer user (with the `ROLE_ORGANIZER` role), requests to the events endpoint return HTTP 200 OK.

2. When logged in as an attendee user (without the `ROLE_ORGANIZER` role), requests to the events endpoint return HTTP 403 Forbidden.

#### Summary

- Locked down the events endpoints to only organizer users

## List Ticket

### List Ticket Service Layer

In this lesson, we're going to implement the list ticket service layer.

#### Repository Method Implementation

The first step is to add a method to the `TicketRepository` interface that will retrieve tickets for a specific user.

We'll add a method called `findByPurchaserDomainId` that takes two parameters:

```java
// In TicketRepository interface
@Query("SELECT t FROM Ticket t WHERE t.purchaser.domainId = :purchaserDomainId")
Page<Ticket> findByPurchaserDomainId(@Param("purchaserDomainId") UUID purchaserDomainId, Pageable pageable);
```

`userId` here comes from the JWT subject, which is the purchaser's `domainId` -- so the query needs to navigate from `Ticket` into the `purchaser` relationship and filter on its `domainId`, not `Ticket`'s own internal `id`.

This method will return a page of tickets, which allows for pagination of results.

Spring Data JPA will automatically implement this method based on the method name, as it understands the relationship between a `Ticket` and its purchaser.

#### Service Interface Creation

Next, we'll define the contract for our ticket service by creating the `TicketService` interface:

```java
public interface TicketService {
  Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable);
}
```

This interface declares a single method that will retrieve a page of tickets for a given user ID.

#### Service Implementation

Finally, we'll create the implementation of our `TicketService` interface:

```java
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

  private final TicketRepository ticketRepository;

  @Override
  public Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable) {
    return ticketRepository.findByPurchaserDomainId(userId, pageable);
  }
}
```

The implementation is straightforward - it simply delegates to the repository method we created earlier.

#### Summary

- Added the `findByPurchaserDomainId` method to `TicketRepository`
- Implemented the `TicketService` with `listTicketsForUser` method

### Get Ticket Dto Conversion

In this lesson we'll implement the DTOs and mappers that we need for the list tickets endpoint.

#### Creating the List Ticket Response DTOs

We'll start by creating two Data Transfer Objects (DTOs) to represent ticket information when listing tickets.

The first DTO, `ListTicketResponseDto`, will contain the main ticket information:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTicketResponseDto {
  private UUID id;
  private TicketStatusEnum status;
  private ListTicketTicketTypeResponseDto ticketType;
}
```

The second DTO, `ListTicketTicketTypeResponseDto`, will contain information about the ticket type:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTicketTicketTypeResponseDto {
  private UUID id;
  private String name;
  private Double price;
}
```

#### Conversion Methods

No MapStruct here either -- two methods on `TicketService`/`TicketServiceImpl`:

```java
public interface TicketService {
  Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable);

  ListTicketTicketTypeResponseDto convertToListTicketTicketTypeResponseDto(TicketType ticketType);
  ListTicketResponseDto convertToListTicketResponseDto(Ticket ticket);
}
```

```java
@Override
public ListTicketTicketTypeResponseDto convertToListTicketTicketTypeResponseDto(TicketType ticketType) {
  ListTicketTicketTypeResponseDto dto = new ListTicketTicketTypeResponseDto();
  dto.setId(ticketType.getDomainId());
  dto.setName(ticketType.getName());
  dto.setPrice(ticketType.getPrice());
  return dto;
}

@Override
public ListTicketResponseDto convertToListTicketResponseDto(Ticket ticket) {
  ListTicketResponseDto dto = new ListTicketResponseDto();
  dto.setId(ticket.getDomainId());
  dto.setStatus(ticket.getStatus());
  dto.setTicketType(convertToListTicketTicketTypeResponseDto(ticket.getTicketType()));
  return dto;
}
```

#### Summary

- Created `ListTicketResponseDto` and `ListTicketTicketTypeResponseDto` classes
- Added `convertToListTicketResponseDto`/`convertToListTicketTicketTypeResponseDto` to `TicketService`

### Get Ticket Endpoint

In this lesson we'll implement the list ticket endpoint.

#### Creating the Ticket Controller

We'll start by creating a new controller class to handle ticket-related operations.

Let's create a new `TicketController` class with the `@RestController` annotation and set up the base path for our API:

```java
@RestController
@RequestMapping(path = "/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService ticketService;

  @GetMapping
  public Page<ListTicketResponseDto> listTickets(
      @AuthenticationPrincipal Jwt jwt,
      Pageable pageable
  ) {
    return ticketService.listTicketsForUser(
        parseUserId(jwt),
        pageable
    ).map(ticketService::convertToListTicketResponseDto);
  }

}
```

#### Summary

- Implemented the list ticket endpoint

### Ui Testing

In this lesson, we'll test our list ticket endpoint through the user interface.

#### Setting Up the Environment

Before testing through the UI, we need to ensure our application is running correctly:

1. Start by building the backend with Maven:

```bash
mvn clean compile
```

2. Run the Spring Boot application.

#### Testing Through the Browser

Let's walk through the testing process in the browser:

1. Open your application's UI in your browser.

2. Log in using an attendee account that has previously purchased tickets.

3. Navigate to the dashboard, which should automatically redirect to the list tickets page.

#### Inspecting the Network Calls

Using the browser's developer tools, we can examine the API calls being made:

1. Open the Network tab in your browser's developer tools.

2. Refresh the page to see the network requests.

3. Look for calls to `/api/v1/tickets` which should include:

- Page information in the request
- Response containing ticket `id`, `status`, and `ticketType`

#### Summary

- Tested the list ticket endpoint using the user interface

## Get Ticket

### Get Ticket Service Layer

In this lesson we'll implement the get ticket functionality in the service layer.

#### Adding a New Repository Method

Let's start by adding a new method to the `TicketRepository` interface.

The method will help us find a ticket by both its domain ID and the purchaser's domain ID.

This ensures tickets can only be retrieved by their rightful owners.

```java
// In TicketRepository interface
@Query("SELECT t FROM Ticket t WHERE t.domainId = :domainId AND t.purchaser.domainId = :purchaserDomainId")
Optional<Ticket> findByDomainIdAndPurchaserDomainId(@Param("domainId") UUID domainId, @Param("purchaserDomainId") UUID purchaserDomainId);
```

#### Implementing the Service Layer

Now we'll create a method in the `TicketService` that uses our new repository method.

This method will act as a pass-through to the repository, maintaining the same return type and validation logic.

```java
@Override
public Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId) {
  return ticketRepository.findByDomainIdAndPurchaserDomainId(ticketId, userId);
}
```

#### Summary

- Added the `findByDomainIdAndPurchaserDomainId` method to `TicketRepository`
- Implemented the `getTicketForUser` method in `TicketService`

### Get Ticket Dto Conversion

In this lesson, we'll implement the DTOs and mappers that we need to implement the get ticket endpoint.

#### Creating the Get Ticket Response DTO

The `GetTicketResponseDto` will combine information from the ticket, ticket type, and event entities.

Instead of nesting this information in separate objects, we'll flatten it into a single DTO for simplicity:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTicketResponseDto {
  private UUID id;
  private TicketStatusEnum status;
  private Double price;
  private String description;
  private String eventName;
  private String eventVenueName;
  private LocalDateTime eventStart;
  private LocalDateTime eventEnd;
}
```

#### Add a Conversion Method

```java
public interface TicketService {
  // ... existing methods ...

  GetTicketResponseDto convertToGetTicketResponseDto(Ticket ticket);
}
```

```java
@Override
public GetTicketResponseDto convertToGetTicketResponseDto(Ticket ticket) {
  GetTicketResponseDto dto = new GetTicketResponseDto();
  dto.setId(ticket.getDomainId());
  dto.setStatus(ticket.getStatus());
  dto.setPrice(ticket.getTicketType().getPrice());
  dto.setDescription(ticket.getTicketType().getDescription());
  dto.setEventName(ticket.getTicketType().getEvent().getName());
  dto.setEventVenueName(ticket.getTicketType().getEvent().getVenue().getName());
  dto.setEventStart(ticket.getTicketType().getEvent().getStart());
  dto.setEventEnd(ticket.getTicketType().getEvent().getEnd());
  return dto;
}
```

This is the clearest illustration yet of the trade-off we're making. MapStruct's `@Mapping(source = "ticket.ticketType.event.venue.name")` navigated four relationships deep in a single annotation; by hand, that's four `.get...()` calls chained together, spelled out explicitly. It's more to type and more to read, but it's also just... Java -- there's nothing generated to go check, and a `NullPointerException` here points at this exact line, not at a mapper implementation class you never wrote.

#### Summary

- Created the `GetTicketResponseDto` class
- Added `convertToGetTicketResponseDto` to `TicketService`

### Get Ticket Endpoint

In this lesson we'll implement the get ticket endpoint.

#### Implementing the Get Ticket Endpoint

The get ticket endpoint allows users to retrieve detailed information about a specific ticket they have purchased.

Let's add this new endpoint to our `TicketController` class:

```java
@GetMapping(path = "/{ticketId}")
public ResponseEntity<GetTicketResponseDto> getTicket(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID ticketId
) {
    return ticketService
        .getTicketForUser(parseUserId(jwt), ticketId)
        .map(ticketService::convertToGetTicketResponseDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

The endpoint follows these steps:

1. Gets the authenticated user's ID from the JWT token
2. Uses the `TicketService` to find the ticket for that user
3. Maps the ticket to a DTO if found
4. Returns HTTP 200 with the ticket data if found, or HTTP 404 if not found

#### Summary

- Implemented the get ticket endpoint

### Ticket Get Qr Code Service Layer

In this lesson, we'll implement the service layer logic to get the QR code image.

#### QR Code Repository Method

Let's begin by creating the `QrCodeRepository` to help us find QR codes by both the ticket's domain ID and the ticket purchaser's domain ID.

```java
@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    @Query("SELECT qc FROM QrCode qc WHERE qc.ticket.domainId = :ticketDomainId AND qc.ticket.purchaser.domainId = :ticketPurchaserDomainId")
    Optional<QrCode> findByTicketDomainIdAndTicketPurchaserDomainId(@Param("ticketDomainId") UUID ticketDomainId, @Param("ticketPurchaserDomainId") UUID ticketPurchaserDomainId);
}
```

This method allows us to look up QR codes using both the ticket's domain ID and the domain ID of the purchaser, ensuring we only return QR codes to their rightful owners. It navigates two relationships deep -- from `QrCode` into `ticket`, and from `ticket` into `purchaser` -- which is exactly the kind of query that's much easier to read as explicit JPQL than as a derived method name.

#### Service Layer Implementation

Next, we'll implement the method in our QR code service to retrieve the QR code image.

```java
@Override
public byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId) {
    QrCode qrCode = qrCodeRepository.findByTicketDomainIdAndTicketPurchaserDomainId(ticketId, userId)
        .orElseThrow(QrCodeNotFoundException::new);

    try {
      return Base64.getDecoder().decode(qrCode.getValue());
     catch(IllegalArgumentException ex) {
      log.error("Invalid base64 QR Code for ticket ID: {}", ticketId, ex);
      throw new QrCodeNotFoundException();
    }
}
```

The method performs two main tasks:

1. It retrieves the QR code from the database using both the user ID and ticket ID.
2. It decodes the Base64-encoded QR code back into a byte array.

If anything goes wrong during the process - either the QR code isn't found or can't be decoded - we throw appropriate exceptions and log the error.

#### Summary

- Added `findByTicketDomainIdAndTicketPurchaserDomainId` to `QrCodeRepository`
- Implemented `getQrCodeImageForUserAndTicket` on `QrCodeService`

### Ticket Qrcode Endpoint

In this lesson, we implement the endpoint to get the QR code image.

#### Creating the QR Code Endpoint

We'll add a new endpoint to the `TicketController` that returns a QR code image associated with a specific ticket.

The endpoint will be an extension of the existing get ticket functionality:

```java
@GetMapping(path = "/{ticketId}/qr-codes")
public ResponseEntity<byte[]> getTicketQrCode(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID ticketId
) {
    byte[] qrCodeImage = qrCodeService.getQrCodeImageForUserAndTicket(
        parseUserId(jwt),
        ticketId
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    headers.setContentLength(qrCodeImage.length);

    return ResponseEntity.ok()
        .headers(headers)
        .body(qrCodeImage);
}
```

#### HTTP Headers for Image Response

To properly serve the QR code image, we need to set specific HTTP headers:

1. We set the `Content-Type` header to `image/png` since we're returning a PNG image
2. We set the `Content-Length` header to match the size of our image data in bytes

These headers help the client browser understand how to handle and display the received data correctly.

#### Summary

- Implemented the get QR Code endpoint

### Ui Testing

In this lesson, we'll test the get ticket and QR code functionality through the user interface, building on our previous implementation of these endpoints.

#### Testing the Get Ticket Functionality

Before we begin testing, we need to ensure our environment is properly set up:

- Run `clean` and `compile` commands to build the application
- Start the backend application
- Log in as an attendee user through the UI

Once logged in, navigate to the dashboard where you'll see your purchased tickets.

Each ticket entry displays basic information and clicking on a ticket reveals the full details including:

- Event name
- Venue
- Start and end times
- Ticket ID
- QR code representation

#### Summary

- Tested the get ticket functionality using the user interface

## Validate Ticket

### Validate Ticket Service Layer

In this lesson, we'll implement the validate ticket service layer.

#### Repository Setup

First, we need to create a repository for ticket validations. This forms the foundation for our database interactions:

```java
@Repository
public interface TicketValidationRepository extends JpaRepository<TicketValidation, Long> {

}
```

#### Service Interface

The service interface defines two methods for validating tickets - one using a QR code and another for manual validation:

```java
public interface TicketValidationService {
  TicketValidation validateTicketByQrCode(UUID qrCodeId);
  TicketValidation validateTicketManually(UUID ticketId);
}
```

#### Service Implementation

The implementation handles the business logic for ticket validation:

```java
@Service
@RequiredArgsConstructor
@Transactional
public class TicketValidationServiceImpl implements TicketValidationService {

  private final QrCodeRepository qrCodeRepository;
  private final TicketValidationRepository ticketValidationRepository;
  private final TicketRepository ticketRepository;

  @Override
  public TicketValidation validateTicketByQrCode(UUID qrCodeId) {
    QrCode qrCode = qrCodeRepository.findByDomainIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)
        .orElseThrow(() -> new QrCodeNotFoundException(
            String.format(
                "QR Code with ID %s was not found", qrCodeId
            )
        ));

    Ticket ticket = qrCode.getTicket();

    return validateTicket(ticket);
  }

  private TicketValidation validateTicket(Ticket ticket) {
    TicketValidation ticketValidation = new TicketValidation();
    ticketValidation.setDomainId(UUID.randomUUID());
    ticketValidation.setTicket(ticket);
    ticketValidation.setValidationMethod(TicketValidationMethod.QR_SCAN);

    TicketValidationStatusEnum ticketValidationStatus = ticket.getValidations().stream()
        .filter(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()))
        .findFirst()
        .map(v -> TicketValidationStatusEnum.INVALID)
        .orElse(TicketValidationStatusEnum.VALID);

    ticketValidation.setStatus(ticketValidationStatus);

    return ticketValidationRepository.save(ticketValidation);
  }

  @Override
  public TicketValidation validateTicketManually(UUID ticketId) {
    Ticket ticket = ticketRepository.findByDomainId(ticketId)
        .orElseThrow(TicketNotFoundException::new);
    return validateTicket(ticket);
  }
}
```

Two more `domainId`-based lookups are needed to support this: `QrCodeRepository` needs a way to find an active QR code by its `domainId` (the `qrCodeId` scanned off a ticket's QR code is its `domainId`), and `TicketRepository` needs a way to find a ticket directly by its `domainId`, for manual entry where staff type in the ticket's externally-visible ID.

```java
// In QrCodeRepository interface
@Query("SELECT qc FROM QrCode qc WHERE qc.domainId = :domainId AND qc.status = :status")
Optional<QrCode> findByDomainIdAndStatus(@Param("domainId") UUID domainId, @Param("status") QrCodeStatusEnum status);
```

```java
// In TicketRepository interface
@Query("SELECT t FROM Ticket t WHERE t.domainId = :domainId")
Optional<Ticket> findByDomainId(@Param("domainId") UUID domainId);
```

The implementation includes these key features:

- The `validateTicketByQrCode` method looks up an active QR code and validates the associated ticket.

- The `validateTicketManually` method looks up a ticket directly by domain ID and validates it.

- The private `validateTicket` method contains the shared validation logic.

- Tickets can only be validated once - subsequent validations will return invalid status.

#### Summary

- Implemented the ticket validation service layer functionality

### Validate Ticket Dto Conversion

In this lesson, we're going to create the DTOs and mappers that we need in order to implement our validate ticket endpoint.

#### Creating the Request DTO

The `TicketValidationRequestDto` is a simple data class that carries validation request information.

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationRequestDto {
  private UUID id;
  private TicketValidationMethod method;
}
```

The class has two fields:

- An `id` field of type `UUID` which can represent either a QR code ID or a ticket ID
- A `method` field of type `TicketValidationMethod` enum to specify the validation method

#### Creating the Response DTO

The `TicketValidationResponseDto` represents the result of a ticket validation attempt.

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationResponseDto {
  private UUID ticketId;
  private TicketValidationStatusEnum status;
}
```

This class contains:

- A `ticketId` field to identify the validated ticket
- A `status` field using `TicketValidationStatusEnum` to indicate the validation result

#### Add a Conversion Method

```java
public interface TicketValidationService {
  TicketValidation validateTicketByQrCode(UUID qrCodeId);
  TicketValidation validateTicketManually(UUID ticketId);

  TicketValidationResponseDto convertToTicketValidationResponseDto(TicketValidation ticketValidation);
}
```

```java
@Override
public TicketValidationResponseDto convertToTicketValidationResponseDto(TicketValidation ticketValidation) {
  TicketValidationResponseDto dto = new TicketValidationResponseDto();
  dto.setTicketId(ticketValidation.getTicket().getDomainId());
  dto.setStatus(ticketValidation.getStatus());
  return dto;
}
```

Note `dto.setTicketId(ticketValidation.getTicket().getDomainId())`, not `.getId()` -- `Ticket`'s internal `Long` primary key has no business ever reaching a response, and there's no compiler here to catch the difference the way MapStruct would have. Writing it out by hand means that check now lives entirely in code review and testing, not the build.

#### Summary

- Added `TicketValidationRequestDto` and `TicketValidationResponseDto` DTO classes
- Added `convertToTicketValidationResponseDto` to `TicketValidationService`

### Validate Ticket Endpoint

In this lesson we're going to implement the validate ticket endpoint.

#### Creating the Controller

Let's create a new controller to handle ticket validation requests. The controller will be responsible for validating tickets through two methods - QR code scanning and manual validation.

First, we'll create a new class called `TicketValidationController` with the required annotations:

```java
@RestController
@RequestMapping(path = "/api/v1/ticket-validations")
@RequiredArgsConstructor
public class TicketValidationController {

  private final TicketValidationService ticketValidationService;

  @PostMapping
  public ResponseEntity<TicketValidationResponseDto> validateTicket(
      @RequestBody TicketValidationRequestDto ticketValidationRequestDto
  ){
    TicketValidationMethod method = ticketValidationRequestDto.getMethod();
    TicketValidation ticketValidation;
    if(TicketValidationMethod.MANUAL.equals(method)) {
      ticketValidation = ticketValidationService.validateTicketManually(
          ticketValidationRequestDto.getId());
    } else {
      ticketValidation = ticketValidationService.validateTicketByQrCode(
          ticketValidationRequestDto.getId()
      );
    }
    return ResponseEntity.ok(
        ticketValidationService.convertToTicketValidationResponseDto(ticketValidation)
    );
  }
}
```

#### Securing the Endpoint

To ensure only staff members can validate tickets, we need to secure the endpoint with the appropriate role:

```java
http
    .authorizeHttpRequests(authorize ->
        authorize
            .requestMatchers("/api/v1/ticket-validations").hasRole("STAFF")
            // Catch all rule
            .anyRequest().authenticated())
```

#### Summary

- Implemented the validate ticket endpoint

### Ui Testing

In this lesson, we'll test the ticket validation functionality through the user interface.

#### Testing QR Code Validation

QR code validation allows staff members to quickly scan and validate attendee tickets using their device's camera.

Let's start by testing the QR code scanning functionality:

- Log in as an attendee in one browser window to display the ticket
- Log in as staff in another browser window to access the validation page
- Use the network panel in the browser's developer tools to monitor the API calls

When scanning the same QR code twice, we should observe:

- First scan: Returns "valid" status with a green checkmark
- Second scan: Returns "invalid" status with a red cross (as tickets can only be validated once)

#### Testing Manual Validation

Manual validation provides a fallback method when QR code scanning isn't possible or practical.

To test manual validation:

- Copy the ticket ID from the attendee's ticket
- Navigate to the manual input section on the validation page
- Enter the ticket ID and submit
- Verify the response in both the UI and network panel

#### Summary

- Tested the ticket validation functionality through the UI

## Frontend Internationalization

`ticket-service` now resolves its own validation and error messages by locale. The frontend needs the same capability for everything the backend doesn't own -- button labels, page titles, form field names, empty states. We're keeping these two systems deliberately separate: `ticket-service` owns backend-originated text, and the frontend owns UI text, each with its own translation files and no runtime dependency on the other.

### Own the UI Labels as Static Files

Unlike `auth-service`'s Angular frontend, which fetches UI labels from a backend `/i18n/ui-labels` endpoint, we're not doing that here -- there's no DB-editable-labels feature to justify centralizing them server-side, and a fetch-based approach introduces an async loading step that's awkward to reconcile with TanStack Start's server-side rendering (the server would need translations loaded before it can render, which means either blocking on a network call or accepting a hydration mismatch). Static, bundled JSON files avoid all of that -- they're available synchronously on both the server and the client.

#### Add the Dependencies

```bash
npm install i18next react-i18next
```

#### Add the Translation Files

```json
// src/i18n/locales/en/common.json
{
  "buttons": {
    "save": "Save",
    "cancel": "Cancel",
    "publish": "Publish",
    "purchase": "Purchase Ticket"
  },
  "events": {
    "createEvent": "Create Event",
    "venue": "Venue",
    "ticketTypes": "Ticket Types",
    "salesPeriod": "Sales Period"
  },
  "tickets": {
    "yourTickets": "Your Tickets",
    "qrCode": "QR Code"
  }
}
```

Adding a language is adding another file with the same key structure -- `src/i18n/locales/el/common.json` for Greek, and so on. Same principle as the backend's `application_messages_<lang>.properties` files, just JSON instead of properties, and living in the frontend instead of `ticket-service`.

#### Configure i18next

```typescript
// src/i18n/index.ts
import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';
import en from './locales/en/common.json';

export const defaultNS = 'common';

i18next.use(initReactI18next).init({
  resources: {
    en: { common: en },
  },
  lng: localStorage.getItem('lang') ?? 'en',
  fallbackLng: 'en',
  defaultNS,
  interpolation: { escapeValue: false }, // React already escapes output
});

export default i18next;
```

Import this once, near the root of the app (e.g. in TanStack Start's root route), before anything tries to render translated text.

#### Use It in a Component

```tsx
import { useTranslation } from 'react-i18next';

function CreateEventForm() {
  const { t } = useTranslation();

  return (
    <form>
      <label>{t('events.venue')}</label>
      <button type="submit">{t('buttons.save')}</button>
    </form>
  );
}
```

#### Summary

- Added `i18next` and `react-i18next`, with static JSON translation files bundled into the frontend
- No backend endpoint involved -- translations are available synchronously, which matters for TanStack Start's SSR
- UI labels and backend messages are two independent systems, each with its own files, kept in sync only by both respecting the same active language

### Keep the Backend in Sync

`ticket-service` resolves validation and error messages from the request's `Accept-Language` header. If the frontend switches language but keeps sending requests without that header, or with the browser's default language instead of whatever the user picked in the UI, the two will drift -- English form labels next to a Greek validation error, or vice versa. The API client needs to explicitly forward the active language on every request:

```typescript
// src/lib/api-client.ts
import i18next from '../i18n';

export function apiFetch(input: RequestInfo, init: RequestInit = {}) {
  return fetch(input, {
    ...init,
    headers: {
      ...init.headers,
      'Accept-Language': i18next.language,
    },
  });
}
```

Every call into `ticket-service` should go through this wrapper (or whatever TanStack Query fetcher wraps it) rather than calling `fetch` directly, or the header simply won't be there.

One honest limitation worth flagging: there's no shared source of truth between the two translation systems. Renaming a field on the backend doesn't touch the frontend's JSON files, and adding a UI label doesn't touch the backend's properties files -- each has to be updated by hand, in its own language, in its own repository. That's the trade-off for keeping them decoupled; it's the same trade-off `auth-service` avoided by centralizing UI labels server-side, at the cost of the DB-backed management layer we specifically decided not to build here.

#### Summary

- API calls now forward the active UI language via `Accept-Language`, so backend error messages match whatever language the frontend is displaying
- Confirmed the two translation systems stay independent by design, with the maintenance trade-off that implies

## Analytics Service

Everything so far has lived inside `ticket-service`. Sales reporting is going to be different: it's read-heavy, it shouldn't run expensive aggregate queries against the same database that's serving live purchases, and it has no business needing write access to tickets or events at all. We're pulling it out into its own service, `analytics-service`, built with NestJS, with its own Postgres database, connected to `ticket-service` only via RabbitMQ -- never a direct HTTP call in either direction.

### Publish Ticket Purchase Events from ticket-service

Before `analytics-service` can consume anything, `ticket-service` needs to publish it. We'll start with a single event, `ticket.purchased`, published once a purchase completes.

#### Add the Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

#### Configure the Connection and the Exchange

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=ticket-platform
spring.rabbitmq.password=changemeinprod!
```

```java
@Configuration
public class RabbitMqConfig {

    public static final String EVENTS_EXCHANGE = "ticket-platform.events";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
```

Notice we only declare the exchange here -- deliberately no queue. `ticket-service` publishes to a topic exchange and doesn't need to know or care who's listening, or how many services are. Each consumer -- `analytics-service`, and later maybe `notifications-service` too -- declares and binds its own queue.

#### Define the Event and Publish It

```java
public record TicketPurchasedEvent(
        UUID ticketId,
        UUID ticketTypeId,
        UUID eventId,
        UUID purchaserId,
        Double price,
        LocalDateTime purchasedAt
) {
}
```

Every field here is a `domainId` or a plain value -- never an internal sequential `id`. This message is leaving the service boundary entirely, so the same rule that governs our DTOs applies to it too.

```java
@Service
@RequiredArgsConstructor
public class TicketEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTicketPurchased(Ticket ticket) {
        TicketPurchasedEvent event = new TicketPurchasedEvent(
                ticket.getDomainId(),
                ticket.getTicketType().getDomainId(),
                ticket.getTicketType().getEvent().getDomainId(),
                ticket.getPurchaser().getDomainId(),
                ticket.getTicketType().getPrice(),
                ticket.getCreatedAt()
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.EVENTS_EXCHANGE, "ticket.purchased", event);
    }
}
```

#### Hook It Into the Purchase Flow

```java
@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;
    private final TicketEventPublisher ticketEventPublisher;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
        // ... user lookup, ticket type lookup, availability check are unchanged ...

        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);
        savedTicket = ticketRepository.save(savedTicket);

        ticketEventPublisher.publishTicketPurchased(savedTicket);

        return savedTicket;
    }
}
```

One honest limitation worth flagging: this publishes inside the same `@Transactional` method that does the purchase. If RabbitMQ is briefly unreachable when `convertAndSend` runs, the exception rolls back the whole purchase -- the opposite of the decoupling we wanted. We're accepting that trade-off for now, since it's simple and RabbitMQ runs right alongside everything else in Docker Compose. The real fix, if this needs to be genuinely reliable, is the transactional outbox pattern -- write the event to a table in the same transaction as the ticket, and have a separate poller publish it. We're not building that yet.

#### Summary

- Added `spring-boot-starter-amqp` and configured the RabbitMQ connection
- Declared a `ticket-platform.events` topic exchange, with no queue of its own
- Published a `TicketPurchasedEvent` -- domain IDs only -- on `ticket.purchased` after a successful purchase
- Flagged that this isn't fully reliable yet; a transactional outbox is the production-grade fix

### Scaffold the NestJS Project

```bash
nest new analytics-service
cd analytics-service
npm install drizzle-orm postgres
npm install -D drizzle-kit
npm install @nestjs/config
npm install amqp-connection-manager amqplib
npm install @nestjs/passport passport passport-jwt jwks-rsa
npm install -D @types/passport-jwt
```

We're using Drizzle rather than TypeORM or Prisma. It's SQL-first -- the query builder reads like the SQL it generates, which suits this service's actual workload (inserts and `COUNT`/`SUM` aggregates) better than a heavier ORM abstraction would. There's no decorator or reflection layer either: the schema definition alone is the source of the TypeScript types, and every query comes back properly typed, with no manual casting needed. `amqp-connection-manager` and `amqplib` give us direct control over exchange and queue declarations, rather than relying on NestJS's built-in RMQ microservice transport, which doesn't cleanly support binding a queue to an exchange someone else declared.

#### Add an Analytics Database to Docker Compose

```yaml
  analytics-db:
    image: postgres:latest
    ports:
      - '5433:5432'
    restart: always
    environment:
      POSTGRES_PASSWORD: changemeinprod!
      POSTGRES_DB: analytics
```

A separate container on a separate host port (`5433`, since `5432` is already taken by `ticket-service`'s database), with its own database name. `analytics-service` never gets credentials to the `ticket-service` database, and vice versa. The existing Adminer service can connect to either one from its login screen -- no need for a second Adminer.

#### Connect Drizzle to the Database

Unlike TypeORM, there's no module to register -- Drizzle's client is just a value, provided to Nest's DI container like any other:

```typescript
// drizzle.provider.ts
import postgres from 'postgres';
import { drizzle } from 'drizzle-orm/postgres-js';
import * as schema from './schema';

export const DRIZZLE = Symbol('DRIZZLE');

export const drizzleProvider = {
  provide: DRIZZLE,
  useFactory: () => drizzle(postgres(process.env.ANALYTICS_DATABASE_URL), { schema }),
};
```

Register it once, in `AppModule`'s `providers` array, and any service can inject it with `@Inject(DRIZZLE)`.

#### Summary

- Scaffolded a new NestJS project, `analytics-service`
- Added Drizzle, RabbitMQ client, and JWT auth dependencies
- Added a dedicated `analytics-db` Postgres container, isolated from `ticket-service`'s database
- Registered the Drizzle client as a plain DI provider, `DRIZZLE`

### Consume Ticket Purchase Events

#### The Sales Fact Table

```typescript
// schema.ts
import { pgTable, bigserial, uuid, doublePrecision, timestamp } from 'drizzle-orm/pg-core';

export const ticketSales = pgTable('ticket_sales', {
  id: bigserial('id', { mode: 'number' }).primaryKey(),
  ticketId: uuid('ticket_id').notNull().unique(),
  ticketTypeId: uuid('ticket_type_id').notNull(),
  eventId: uuid('event_id').notNull(),
  purchaserId: uuid('purchaser_id').notNull(),
  price: doublePrecision('price').notNull(),
  purchasedAt: timestamp('purchased_at', { withTimezone: true }).notNull(),
  recordedAt: timestamp('recorded_at', { withTimezone: true }).notNull().defaultNow(),
});

export type TicketSale = typeof ticketSales.$inferSelect;
```

This is deliberately a flat, denormalized fact table -- one row per sale -- rather than a mirror of `ticket-service`'s relational model. It exists to be summed and grouped by `eventId`, nothing else. The `unique` constraint on `ticketId` is what makes recording a sale idempotent. Notice there's no separate entity class to keep in sync -- `TicketSale` is inferred directly from the table definition.

#### Record a Sale

```typescript
export interface TicketPurchasedEvent {
  ticketId: string;
  ticketTypeId: string;
  eventId: string;
  purchaserId: string;
  price: number;
  purchasedAt: string;
}
```

```typescript
import { Injectable, Inject } from '@nestjs/common';
import { eq, sql } from 'drizzle-orm';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { DRIZZLE } from './drizzle.provider';
import { ticketSales } from './schema';
import { TicketPurchasedEvent } from './ticket-purchased.event';
import * as schema from './schema';

@Injectable()
export class TicketSalesService {
  constructor(@Inject(DRIZZLE) private readonly db: PostgresJsDatabase<typeof schema>) {}

  async recordSale(event: TicketPurchasedEvent): Promise<void> {
    await this.db
      .insert(ticketSales)
      .values({
        ticketId: event.ticketId,
        ticketTypeId: event.ticketTypeId,
        eventId: event.eventId,
        purchaserId: event.purchaserId,
        price: event.price,
        purchasedAt: new Date(event.purchasedAt),
      })
      .onConflictDoNothing({ target: ticketSales.ticketId });
  }

  async getSummaryForEvent(eventId: string) {
    const [summary] = await this.db
      .select({
        ticketsSold: sql<number>`count(*)`,
        revenue: sql<number>`coalesce(sum(${ticketSales.price}), 0)`,
      })
      .from(ticketSales)
      .where(eq(ticketSales.eventId, eventId));

    return { eventId, ...summary };
  }
}
```

Two things worth calling out against the TypeORM version this replaces: idempotency is now a single atomic `INSERT ... ON CONFLICT DO NOTHING` statement, rather than throwing on the unique constraint and catching Postgres error code `23505` in a `try/catch` -- a redelivered message is routine, expected behavior, not something that should drive control flow through an exception. And the aggregate query result is properly typed from the `select({...})` shape, so there's no `getRawOne()` returning an untyped object and no manual `Number()` casting.

#### Run Migrations with Drizzle Kit

Same principle as `ticket-service`'s move to Liquibase -- we don't want the schema managed implicitly by the ORM. Drizzle Kit diffs `schema.ts` against migration history and generates plain SQL files:

```typescript
// drizzle.config.ts
import { defineConfig } from 'drizzle-kit';

export default defineConfig({
  schema: './src/schema.ts',
  out: './drizzle',
  dialect: 'postgresql',
  dbCredentials: {
    url: process.env.ANALYTICS_DATABASE_URL!,
  },
});
```

```bash
npx drizzle-kit generate
```

This writes a new `.sql` file under `drizzle/` -- reviewable and committable, not a black box. To apply pending migrations automatically on startup, the same way Liquibase runs when `ticket-service` boots:

```typescript
// main.ts
import { migrate } from 'drizzle-orm/postgres-js/migrator';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const db = app.get(DRIZZLE);

  await migrate(db, { migrationsFolder: './drizzle' });

  await app.listen(3001);
}
```

#### Wire Up the Consumer

```typescript
import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import * as amqp from 'amqp-connection-manager';
import { ConfirmChannel, ConsumeMessage } from 'amqplib';
import { TicketSalesService } from './ticket-sales.service';

const EVENTS_EXCHANGE = 'ticket-platform.events';
const QUEUE_NAME = 'analytics-service.ticket-events';

@Injectable()
export class RabbitMqConsumerService implements OnModuleInit {
  private readonly logger = new Logger(RabbitMqConsumerService.name);

  constructor(private readonly ticketSalesService: TicketSalesService) {}

  onModuleInit() {
    const connection = amqp.connect([
      process.env.RABBITMQ_URL ?? 'amqp://ticket-platform:changemeinprod!@localhost:5672',
    ]);

    connection.createChannel({
      setup: async (channel: ConfirmChannel) => {
        await channel.assertExchange(EVENTS_EXCHANGE, 'topic', { durable: true });
        await channel.assertQueue(QUEUE_NAME, { durable: true });
        await channel.bindQueue(QUEUE_NAME, EVENTS_EXCHANGE, 'ticket.purchased');

        await channel.consume(QUEUE_NAME, (message: ConsumeMessage | null) =>
          this.handleMessage(channel, message),
        );
      },
    });
  }

  private async handleMessage(channel: ConfirmChannel, message: ConsumeMessage | null) {
    if (!message) {
      return;
    }

    try {
      const event = JSON.parse(message.content.toString());
      await this.ticketSalesService.recordSale(event);
      channel.ack(message);
    } catch (error) {
      this.logger.error('Failed to process ticket.purchased message', error);
      channel.nack(message, false, false);
    }
  }
}
```

Both sides assert the exchange (idempotent -- whichever service starts first actually creates it), but only `analytics-service` declares and binds its own queue, with only the routing key it cares about. `ticket-service` never needs to know `analytics-service` exists. A future `notifications-service` could bind its own differently-named queue to the same exchange -- for `ticket.purchased`, `ticket.validated`, or anything else -- without touching this code at all.

`channel.nack(message, false, false)` discards a message that fails to process rather than requeueing it, which matters: a malformed message would otherwise loop forever through consume-fail-requeue. That's also a simplification -- a production setup would route failed messages to a dead-letter exchange instead of dropping them silently, so nothing gets lost without a trace. We're keeping it simple for now.

#### Summary

- Defined the `ticketSales` table as a Drizzle schema, with a unique `ticketId` for idempotency
- Implemented `TicketSalesService.recordSale` as an atomic `ON CONFLICT DO NOTHING` upsert, and `getSummaryForEvent` with a fully-typed result
- Set up Drizzle Kit migrations, applied automatically on startup
- Implemented `RabbitMqConsumerService`, which declares and binds its own queue to `ticket-service`'s exchange
- Failed messages are dropped rather than looped forever, with a noted follow-up (dead-lettering) we're not building yet

### Expose the Reporting API

`analytics-service` doesn't get its own identity system -- it validates the same Keycloak-issued JWTs `ticket-service` does, against the same realm's JWKS endpoint.

```typescript
import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { Strategy, ExtractJwt } from 'passport-jwt';
import * as jwksRsa from 'jwks-rsa';

@Injectable()
export class KeycloakJwtStrategy extends PassportStrategy(Strategy) {
  constructor() {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      secretOrKeyProvider: jwksRsa.passportJwtSecret({
        jwksUri: 'http://localhost:9090/realms/event-ticket-platform/protocol/openid-connect/certs',
      }),
      algorithms: ['RS256'],
    });
  }

  async validate(payload: any) {
    const roles: string[] = payload.realm_access?.roles ?? [];
    return { userId: payload.sub, roles };
  }
}
```

With that in place, a summary endpoint is a thin controller on top of the service we already wrote:

```typescript
import { Controller, Get, Param, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { TicketSalesService } from './ticket-sales.service';

@Controller('analytics/events')
@UseGuards(AuthGuard('jwt'))
export class EventAnalyticsController {
  constructor(private readonly ticketSalesService: TicketSalesService) {}

  @Get(':eventId/summary')
  getSummary(@Param('eventId') eventId: string) {
    return this.ticketSalesService.getSummaryForEvent(eventId);
  }
}
```

This endpoint only ever reads by `eventId` -- it never needs to resolve who the organizer's internal user record is, so there's no provisioning filter or user table to build on this side at all. Sales-over-time and organizer-level rollups follow the exact same shape: a query method on `TicketSalesService`, a route on this controller. We're not building every endpoint here -- this establishes the pattern.

#### Summary

- Added a `KeycloakJwtStrategy` validating against the same realm as `ticket-service`, no separate identity system
- Exposed `GET /analytics/events/:eventId/summary` as the first reporting endpoint
- `analytics-service` now has no direct dependency on `ticket-service` in either direction -- RabbitMQ in, a read API out
