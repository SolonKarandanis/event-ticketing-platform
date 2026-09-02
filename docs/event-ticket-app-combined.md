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

The screenshot above is the very first pass at this -- just a frontend, a backend, and a database. The system has grown a lot since: a dedicated auth server, a message broker, and a whole second service. The mermaid diagram below is the one to trust going forward; treat the screenshot as a historical snapshot, not the current design.

Based on the functionality we've captured, we'll need the following components:

- **React App (TanStack Start + React Query)** -- the frontend, calling `ticket-service`'s REST API directly and `analytics-service`'s reporting API directly (see "Analytics Service" later in this document) -- never proxied through one another
- **Keycloak** -- our auth server; both `ticket-service` and `analytics-service` validate JWTs against the same realm, and neither runs its own identity system
- **ticket-service (Spring Boot)** -- the backend for everything organizer/attendee/staff-facing: events, venues, tickets, purchases, validation
- **ticket-service's PostgreSQL database** -- owned exclusively by `ticket-service`; nothing else reads or writes it directly
- **RabbitMQ** -- the message broker connecting `ticket-service` to `analytics-service`. `ticket-service` publishes to a topic exchange (`ticket-platform.events`) without knowing who's listening; `analytics-service` declares and binds its own queue to it
- **analytics-service (NestJS)** -- consumes `ticket.purchased` events off RabbitMQ and exposes a read-only reporting API; it never calls `ticket-service`, and `ticket-service` never calls it -- the only connection between them is the exchange
- **analytics-service's own PostgreSQL database** -- separate from `ticket-service`'s, so expensive reporting/aggregate queries never compete with live purchase traffic for the same database

Here's the mermaid diagram:

```mermaid
flowchart LR
    F[Event Ticket App</br>Frontend</br><< React + TanStack Start >>]
    K[Keycloak</br><< Auth Server >>]
    B[ticket-service</br><< Spring Boot >>]
    DB1[(ticketservice DB</br><< PostgreSQL >>)]
    MQ{{ticket-platform.events</br><< RabbitMQ Topic Exchange >>}}
    A[analytics-service</br><< NestJS >>]
    DB2[(analytics DB</br><< PostgreSQL >>)]

    F -- REST API --> B
    F -- reporting API --> A
    F -. login/JWT .-> K
    B -. validates JWT .-> K
    A -. validates JWT .-> K
    B --- DB1
    A --- DB2
    B -- publishes ticket.purchased --> MQ
    MQ -- consumed by --> A
```

Two things worth calling out in the diagram itself: there's no arrow anywhere between `ticket-service` and `analytics-service` directly -- the only path between them runs through the exchange, in one direction, and neither service knows the other exists beyond that. And `analytics-service` has its own database node, not a shared one with `ticket-service` -- the two services don't just run separately, they own their data separately too.

#### Summary

- Our architecture now includes `ticket-service` (Spring Boot) and `analytics-service` (NestJS), each with its own PostgreSQL database, a React + TanStack Start frontend (using React Query) calling both services' APIs directly, a shared Keycloak auth server, and RabbitMQ connecting `ticket-service` to `analytics-service` one-way via a topic exchange
- `ticket-service` and `analytics-service` never call each other directly -- RabbitMQ in, independent REST APIs out

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
spring.jpa.open-in-view=false
```

The database connection properties tell Spring Boot:

- Where to find the database (`localhost:5432`)
- Which database to use (`postgres`)
- The login credentials

`spring.jpa.open-in-view=false` is worth calling out on its own. Spring Boot's default (`true`) keeps a Hibernate session open for the entire HTTP request, which papers over a common mistake -- touching a lazy `@ManyToOne`/`@OneToMany` after the transaction that loaded the entity has already closed -- at the cost of holding a database connection for the whole request and hiding N+1 queries that would otherwise be obvious. We're turning it off deliberately: every place a lazy association needs to be available outside its original transaction, we'll fetch it explicitly (with `JOIN FETCH` or an equivalent), rather than relying on the session staying open by accident. We'll hit this directly once pagination and lazy collections meet each other, in "List Event Service".

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
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin
```

Two ports are exposed:

- `5672` is the AMQP port -- this is what `ticket-service` (and later, `notifications-service`) actually connects on to publish and consume messages
- `15672` is the management UI, available at `http://localhost:15672`, where you can inspect exchanges, queues, and individual messages while developing

We're setting an explicit `RABBITMQ_DEFAULT_USER`/`RABBITMQ_DEFAULT_PASS` rather than relying on the image's default `guest`/`guest` account -- not primarily for security (`admin`/`admin` isn't meaningfully stronger, and this is local dev only, matching the same `admin`/`admin` convenience already used for Keycloak), but because RabbitMQ's `guest` user is hard-restricted to connections from `localhost` by the broker itself. A connection from another container on the Compose network doesn't count as `localhost`, so `ticket-service` would be refused outright if we left it as `guest`/`guest`.

#### Summary

- Added a RabbitMQ service to `docker-compose.yml`, with the management UI enabled
- Exposed `5672` (AMQP, for services) and `15672` (management UI, for us)
- Set an explicit user/password, since the default `guest` account can't authenticate from another container

### Configure Internationalization

Every validation and error message we write from here on is going to be a hardcoded English string -- `"Event name is required"`, `"Venue not found"`, and so on -- unless we route them through a message source instead. Let's set that up now, before we start writing the DTOs and exception handlers that will use it.

We're also about to lean heavily on Bean Validation annotations (`@NotBlank`, `@NotNull`, `@PositiveOrZero`) across every request DTO in this build, starting with `CreateEventRequestDto` in the next major section -- that needs `spring-boot-starter-validation` on the classpath:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

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
error.ticket-type.not-found=Ticket type not found
error.qr-code.generation-failed=Unable to generate QR Code
error.constraint-violation=A constraint violation occurred
error.validation-failed=Validation error occurred
```

This is a starting set, covering what we need for the very first DTOs and exceptions -- it grows throughout the build as we add more validation rules (`venue.*` once `Venue` exists as its own entity) and more `ErrorCode` values (see "Error Codes"; each one needs a matching `messageKey` entry here, e.g. `error.event.id-required`/`error.event.id-mismatch`, `error.ticket.not-found`, `error.ticket.sold-out`, `error.qr-code.not-found`). We won't call out every single addition to this file going forward -- treat each new `ErrorCode` constant or `@NotBlank`/`@NotNull` message key later in the build as implying a corresponding line here.

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

### Relationship Collections: Set, Not List

Every `@OneToMany`/`@ManyToMany` collection we've written so far -- `Event.ticketTypes`, `User.organizedEvents`, `Venue.events`, and the rest -- is typed as `List<...>`. Before moving on to migrations, let's fix that: every one of them becomes a `Set`, backed by `LinkedHashSet`.

#### Why Not List

A JPA `@OneToMany`/`@ManyToMany` collection isn't an ordered sequence with duplicates the way a general-purpose `List` implies -- it's a set of associated rows, and Hibernate already treats it that way internally (a `PersistentBag` for `List`, a `PersistentSet` for `Set`). Using `List` invites two real problems down the line:

- Combining more than one `@OneToMany`/`@ManyToMany` fetch join on the same query (something we'll want for avoiding N+1 queries) throws `MultipleBagFetchException` when the collections are `List`s -- Hibernate can't figure out which row belongs to which bag once two of them are joined in the same result set. `Set`s don't have this problem.
- A `List`-backed bag has no natural uniqueness check, so the same association could end up added twice by accident with no error, silently double-counting.

`LinkedHashSet` specifically (over plain `HashSet`) keeps insertion order -- so `event.getTicketTypes()` comes back in the order ticket types were added, rather than an unspecified order that can vary between runs.

#### Update Every Relationship Field

Every entity with a `@OneToMany`/`@ManyToMany` field needs the same three changes: swap `List<X>` for `Set<X>`, swap `new ArrayList<>()` for `new LinkedHashSet<>()`, and swap the `ArrayList`/`List` imports for `LinkedHashSet`/`Set`.

```java
// User
@OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
@Builder.Default
private Set<Event> organizedEvents = new LinkedHashSet<>();

@ManyToMany
@JoinTable(
        name = "user_attending_events",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
)
@Builder.Default
private Set<Event> attendingEvents = new LinkedHashSet<>();

@ManyToMany
@JoinTable(
        name = "user_staffing_events",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
)
@Builder.Default
private Set<Event> staffingEvents = new LinkedHashSet<>();
```

```java
// Event
@ManyToMany(mappedBy = "attendingEvents")
@Builder.Default
private Set<User> attendees = new LinkedHashSet<>();

@ManyToMany(mappedBy = "staffingEvents")
@Builder.Default
private Set<User> staff = new LinkedHashSet<>();

@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private Set<TicketType> ticketTypes = new LinkedHashSet<>();
```

```java
// Venue
@OneToMany(mappedBy = "venue")
@Builder.Default
private Set<Event> events = new LinkedHashSet<>();
```

```java
// TicketType
@OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL)
@Builder.Default
private Set<Ticket> tickets = new LinkedHashSet<>();
```

```java
// Ticket
@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
@Builder.Default
private Set<TicketValidation> validations = new LinkedHashSet<>();

@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
@Builder.Default
private Set<QrCode> qrCodes = new LinkedHashSet<>();
```

Nothing about `equals`/`hashCode` changes -- these fields were already excluded from both, so switching their type doesn't touch either method.

#### Summary

- Every `@OneToMany`/`@ManyToMany` collection field is now `Set<X>` backed by `LinkedHashSet`, not `List<X>`/`ArrayList`
- Avoids `MultipleBagFetchException` when we later combine fetch joins, and rules out accidental duplicate associations
- `LinkedHashSet` keeps insertion order, so this isn't a behavior change from the reader's perspective -- just a safer underlying type

### Bidirectional Relationship Helpers

Every relationship above is bidirectional -- `Event.ticketTypes` and `TicketType.event` are two sides of the same association. Setting only one side (e.g. `ticketType.setEvent(event)` without also adding `ticketType` to `event.getTicketTypes()`) leaves the in-memory object graph inconsistent: Hibernate will still persist the foreign key correctly, but any code in the same transaction that reads `event.getTicketTypes()` afterward won't see the new ticket type until the entity is reloaded from the database. Let's add a small `addX`/`removeX` method pair to the owning side of each relationship, so setting up (or tearing down) an association always keeps both sides in sync in one call.

#### The Pattern

Each pair does the same two things: mutate the collection on `this`, and set (or clear) the back-reference on the other side.

```java
// User
public void addEventOrganized(Event event){
    this.organizedEvents.add(event);
    event.setOrganizer(this);
}

public void removeEventOrganized(Event event){
    this.organizedEvents.remove(event);
    event.setOrganizer(null);
}

public void addAttendingEvent(Event event) {
    this.attendingEvents.add(event);
    event.getAttendees().add(this);
}

public void removeAttendingEvent(Event event) {
    this.attendingEvents.remove(event);
    event.getAttendees().remove(this);
}

public void addStaffingEvent(Event event) {
    this.staffingEvents.add(event);
    event.getStaff().add(this);
}

public void removeStaffingEvent(Event event) {
    this.staffingEvents.remove(event);
    event.getStaff().remove(this);
}
```

```java
// Event
public void addTicketType(TicketType ticketType) {
    this.ticketTypes.add(ticketType);
    ticketType.setEvent(this);
}

public void removeTicketType(TicketType ticketType) {
    this.ticketTypes.remove(ticketType);
    ticketType.setEvent(null);
}
```

```java
// Venue
public void addEvent(Event event) {
    this.events.add(event);
    event.setVenue(this);
}

public void removeEvent(Event event) {
    this.events.remove(event);
    event.setVenue(null);
}
```

```java
// TicketType
public void addTicket(Ticket ticket) {
    this.tickets.add(ticket);
    ticket.setTicketType(this);
}

public void removeTicket(Ticket ticket) {
    this.tickets.remove(ticket);
    ticket.setTicketType(null);
}
```

```java
// Ticket
public void addValidation(TicketValidation validation) {
    this.validations.add(validation);
    validation.setTicket(this);
}

public void removeValidation(TicketValidation validation) {
    this.validations.remove(validation);
    validation.setTicket(null);
}

public void addQrCode(QrCode qrCode) {
    this.qrCodes.add(qrCode);
    qrCode.setTicket(this);
}

public void removeQrCode(QrCode qrCode) {
    this.qrCodes.remove(qrCode);
    qrCode.setTicket(null);
}
```

We put each pair on the "one" side of a `@OneToMany` (`Event` owns `addTicketType`, not `TicketType`), and for the two `@ManyToMany` join-table relationships (`attendingEvents`/`staffingEvents`) we put both pairs on `User` rather than splitting them across `User` and `Event` -- either side could reasonably own them, but having one canonical place to look avoids the two sides drifting out of sync with each other.

From here on, every place in the service layer that establishes or removes one of these relationships uses these methods instead of calling `setXxx`/`getXxx().add(...)` directly -- we'll update `EventServiceImpl`, `TicketTypeServiceImpl`, `QrCodeServiceImpl`, and `TicketValidationServiceImpl` to use them as we revisit each lesson below.

#### Summary

- Added `addX`/`removeX` method pairs for every bidirectional relationship, keeping both sides of the association in sync in one call
- Placed each pair on the "one"/parent side of `@OneToMany` relationships, and on `User` for the two `@ManyToMany` join-table relationships
- The service layer will use these instead of raw `setXxx`/collection `.add()` calls going forward

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

### Error Codes

Every domain exception we're about to write needs two things once it reaches a client: an HTTP status, and a message. Rather than let each exception carry a hand-typed English sentence (inconsistent, not localizable, and impossible to search for by error type), we'll give every domain exception a structured `ErrorCode` instead -- a single enum constant that carries both.

#### The ErrorCode Enum

```java
public enum ErrorCode {
    USER_NOT_FOUND("error.user.not-found", HttpStatus.BAD_REQUEST),
    VENUE_NOT_FOUND("error.venue.not-found", HttpStatus.BAD_REQUEST),
    VENUE_ID_REQUIRED("error.venue.id-required", HttpStatus.BAD_REQUEST),
    VENUE_ID_MISMATCH("error.venue.id-mismatch", HttpStatus.BAD_REQUEST),
    EVENT_NOT_FOUND("error.event.not-found", HttpStatus.BAD_REQUEST),
    EVENT_ID_REQUIRED("error.event.id-required", HttpStatus.BAD_REQUEST),
    EVENT_ID_MISMATCH("error.event.id-mismatch", HttpStatus.BAD_REQUEST),
    TICKET_TYPE_NOT_FOUND("error.ticket-type.not-found", HttpStatus.BAD_REQUEST),
    TICKET_NOT_FOUND("error.ticket.not-found", HttpStatus.BAD_REQUEST),
    TICKET_SOLD_OUT("error.ticket.sold-out", HttpStatus.CONFLICT),
    QR_CODE_GENERATION_FAILED("error.qr-code.generation-failed", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_CODE_NOT_FOUND("error.qr-code.not-found", HttpStatus.BAD_REQUEST);

    private final String messageKey;
    private final HttpStatus httpStatus;

    ErrorCode(String messageKey, HttpStatus httpStatus) {
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
```

Every constant is a complete, self-contained piece of information: which `application_messages_en.properties` key resolves the client-facing message, and which HTTP status the response should carry. `TICKET_SOLD_OUT` gets `409 Conflict` instead of the `400 Bad Request` everything else uses, since running out of tickets is a different kind of failure than "the thing you asked for doesn't exist." Notice `EVENT_ID_REQUIRED` and `EVENT_ID_MISMATCH` (and their `VENUE_` equivalents) are two separate codes, not one shared "event update failed" -- a client should be able to tell "you didn't send an ID" apart from "the ID in the body doesn't match the ID in the URL," and one shared code can't express that.

We're listing all twelve constants up front rather than growing the enum lesson by lesson, the same way `EventStatusEnum` and the other enums were defined complete before anything used them -- some of these (`TICKET_SOLD_OUT`, `QR_CODE_NOT_FOUND`, and so on) won't have a corresponding exception until later lessons, but the full set is easier to reason about in one place than scattered across a dozen small additions.

#### Understanding Custom Exceptions

Custom exceptions help us handle application-specific error cases in a way that makes sense for our domain. When creating custom exceptions, it's helpful to have a base exception class that all other exceptions extend -- and now that we have `ErrorCode`, every exception in that hierarchy can require one.

```java
public class EventTicketException extends RuntimeException {
    private final ErrorCode errorCode;

    public EventTicketException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public EventTicketException(ErrorCode errorCode, Object detail) {
        super(errorCode.name() + ": " + detail);
        this.errorCode = errorCode;
    }

    public EventTicketException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.name(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
```

Three constructors cover every case we'll need: a bare code for exceptions with no useful extra context, a code plus a `detail` (almost always the ID that couldn't be resolved) for `.getMessage()` to include, and a code plus a `cause` for wrapping a lower-level exception. None of them take a free-typed `String message` -- the whole point is that there's no hand-written sentence left to keep in sync with anything. `.getMessage()` is derived entirely from the code (and the detail, when there is one), which is exactly enough to make server-side logs searchable by error type without duplicating the client-facing text that `ErrorCode.getMessageKey()` already owns.

#### Creating the User Not Found Exception

Every subclass repeats the same three constructors, just delegating straight to `super(...)`:

```java
public class UserNotFoundException extends EventTicketException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public UserNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

#### Creating the Venue Not Found Exception

Creating an event also requires resolving a `venueId` to an existing `Venue`, so we need a matching exception for that case:

```java
public class VenueNotFoundException extends EventTicketException {
    public VenueNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VenueNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public VenueNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

#### Using Runtime Exceptions

We extend `RuntimeException` in our base exception class rather than `Exception`.

This choice means we don't need to declare throws clauses on methods that might throw our exceptions.

This approach, recommended by Robert C. Martin in "Clean Code", helps maintain the Open-Closed Principle by preventing changes to method signatures when new exceptions are added.

#### Summary

- Created the `ErrorCode` enum, pairing every domain error with its message key and HTTP status up front
- Created a `EventTicketException` parent custom exception that requires an `ErrorCode` on every construction path, with no free-typed message
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
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND, organizerId));

        // Find the venue or throw an exception if not found
        Venue venue = venueRepository.findByDomainId(event.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException(ErrorCode.VENUE_NOT_FOUND, event.getVenueId()));

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

Both `UserNotFoundException` and `VenueNotFoundException` extend `EventTicketException` and were constructed with an `ErrorCode` (see "Error Codes"), so instead of a handler per exception class, one handler covers all of them -- and every `EventTicketException` subclass we add from here on, automatically:

```java
@ExceptionHandler(EventTicketException.class)
public ResponseEntity<ErrorDto> handleEventTicketException(EventTicketException ex) {
    log.error("Caught {}", ex.getClass().getSimpleName(), ex);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setError(resolve(ex.getErrorCode().getMessageKey()));
    return new ResponseEntity<>(errorDto, ex.getErrorCode().getHttpStatus());
}
```

There's no `resolve("error.user.not-found")`/`resolve("error.venue.not-found")` literal here at all -- the message key comes from `ex.getErrorCode().getMessageKey()`, and even the HTTP status comes from the code (`ex.getErrorCode().getHttpStatus()`) rather than being hardcoded per handler. `Exception`, `ConstraintViolationException`, and `MethodArgumentNotValidException` stay as their own handlers above, since none of those are `EventTicketException`s -- they're thrown by the JVM/Spring/Bean Validation themselves, not by our own code, so there's no `ErrorCode` to read off them.

#### Summary

- Created the `ErrorDto` class
- Created the `GlobalExceptionHandler` class, injected with `MessageSource`
- One `@ExceptionHandler(EventTicketException.class)` covers every current and future domain exception, reading its status and message key from `ex.getErrorCode()`
- `Exception`/`ConstraintViolationException`/`MethodArgumentNotValidException` keep their own handlers, since they're framework-thrown, not ours
- All errors are now returned in the expected format

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

The direct cause is the missing `ticketTypeToCreate.setEvent(eventToCreate)` call -- but rather than just adding that one line, let's fix it with the `addTicketType`/`addEvent`/`addEventOrganized` helpers from "Bidirectional Relationship Helpers": they set both sides of each relationship in one call, so this exact bug (setting a collection without setting the matching back-reference, or vice versa) isn't something you have to remember to get right by hand at every call site going forward.

```java
@Override
@Transactional
public Event createEvent(UUID organizerId, CreateEventRequest event) {
    User organizer = userRepository.findByDomainId(organizerId)
            .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND, organizerId));

    Venue venue = venueRepository.findByDomainId(event.getVenueId())
            .orElseThrow(() -> new VenueNotFoundException(ErrorCode.VENUE_NOT_FOUND, event.getVenueId()));

    Event eventToCreate = new Event();
    eventToCreate.setDomainId(UUID.randomUUID());
    eventToCreate.setName(event.getName());
    eventToCreate.setStart(event.getStart());
    eventToCreate.setEnd(event.getEnd());
    eventToCreate.setSalesStart(event.getSalesStart());
    eventToCreate.setSalesEnd(event.getSalesEnd());
    eventToCreate.setStatus(event.getStatus());

    venue.addEvent(eventToCreate);
    organizer.addEventOrganized(eventToCreate);

    event.getTicketTypes().forEach(ticketType -> {
        TicketType ticketTypeToCreate = new TicketType();
        ticketTypeToCreate.setDomainId(UUID.randomUUID());
        ticketTypeToCreate.setName(ticketType.getName());
        ticketTypeToCreate.setPrice(ticketType.getPrice());
        ticketTypeToCreate.setDescription(ticketType.getDescription());
        ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
        eventToCreate.addTicketType(ticketTypeToCreate);
    });

    return eventRepository.save(eventToCreate);
}
```

Two things changed beyond the bug fix itself:

- `venue.addEvent(eventToCreate)` and `organizer.addEventOrganized(eventToCreate)` touch `venue.getEvents()`/`organizer.getOrganizedEvents()` -- both lazy collections. Since `venue`/`organizer` were each loaded by their own repository call (each with its own short-lived, auto-committed transaction), they'd already be detached by the time this method touched those collections without an enclosing transaction of its own -- so this method now needs `@Transactional`, keeping one session open for its whole body.
- Building `ticketTypesToCreate` as a separate `List` and calling `eventToCreate.setTicketTypes(...)` at the end doesn't fit the helper-method pattern -- `addTicketType` needs `eventToCreate` to already exist so it can set the back-reference, so ticket types are now added one at a time via `forEach` instead of collected into a list first.

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

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.venue WHERE e.organizer.domainId = :organizerDomainId")
    Page<Event> findByOrganizerDomainId(@Param("organizerDomainId") UUID organizerDomainId, Pageable pageable);
}
```

The query:

- Selects `Event` entities (`e`)
- Filters by the organizer's `domainId`, navigating from `Event` into the `organizer` relationship and then into its `domainId` field
- Returns results in pages -- Spring Data JPA handles pagination automatically because the method takes a `Pageable` and returns a `Page<Event>`, no `LIMIT`/`OFFSET` needed in the JPQL itself
- `LEFT JOIN FETCH e.venue` loads each event's venue in the same query, instead of leaving it as a lazy `@ManyToOne` proxy -- more on why below

Note this is `Long` now, not `UUID` -- that's the JPA `@Id` type. We only ever have the organizer's `domainId` on hand (from the JWT), never their internal `id`, so the query needs to reach into the relationship rather than filtering on `Event`'s own primary key.

#### Avoiding LazyInitializationException

`ListEventResponseDto` needs both `event.getVenue()` and `event.getTicketTypes()`, and the controller does the entity-to-DTO conversion *after* this service method returns:

```java
Page<Event> events = eventService.listEventsForOrganizer(userId, pageable);
return ResponseEntity.ok(events.map(eventService::convertToListEventResponseDto));
```

With `spring.jpa.open-in-view=false`, the Hibernate session backing `findByOrganizerDomainId` closes the moment this method returns. `venue` and `ticketTypes` are both lazy by default, so if either were left un-fetched, the controller's later `event.getVenue()`/`event.getTicketTypes()` calls would throw `LazyInitializationException` against an already-detached entity.

`venue` is straightforward -- it's a `@ManyToOne`, so `LEFT JOIN FETCH e.venue` above loads it in the same query, and pagination is unaffected: a to-one fetch join can't multiply result rows the way a to-many one can.

`ticketTypes` is the harder case, because it's exactly the kind of `@OneToMany` that *would* multiply rows -- and fetch-joining a collection together with a `Pageable` forces Hibernate to abandon `LIMIT`/`OFFSET` at the SQL level and paginate in memory instead, silently fetching every matching row before slicing out a page. So instead of a fetch join, `ticketTypes` gets hydrated in a second, unpaged query, once we already have this page's event IDs:

```java
// Second method on EventRepository, used only by listEventsForOrganizer
@Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.ticketTypes WHERE e.id IN :ids")
List<Event> findByIdInWithTicketTypes(@Param("ids") Collection<Long> ids);
```

```java
@Override
@Transactional(readOnly = true)
public Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable) {
    Page<Event> page = eventRepository.findByOrganizerDomainId(organizerId, pageable);

    List<Long> eventIds = page.getContent().stream().map(Event::getId).toList();
    if (!eventIds.isEmpty()) {
        eventRepository.findByIdInWithTicketTypes(eventIds);
    }

    return page;
}
```

This works because both queries run inside the same `@Transactional(readOnly = true)` method, sharing one Hibernate persistence context. Hibernate recognizes the `Event` rows returned by the second query as the *same* managed instances already sitting in `page.getContent()` (same entity, same `id`), so fetch-joining `ticketTypes` there populates the collection directly on those objects -- no separate list to merge back in. `SELECT DISTINCT` matters here too: without it, an event with three ticket types would come back as three duplicate `Event` rows from the join, one per ticket type.

The `@Transactional(readOnly = true)` isn't just good practice here -- it's what keeps both queries in the same session, which the second query's fetch join depends on.

#### Summary

- Added the `listEventsForOrganizer` method to the `EventService` interface
- Added `findByOrganizerDomainId` (fetch-joining `venue`) and `findByIdInWithTicketTypes` (fetch-joining `ticketTypes`, unpaged) to the `EventRepository` interface
- Implemented `listEventsForOrganizer` as a two-step, same-transaction "paginate, then hydrate collections" pattern -- required because `spring.jpa.open-in-view=false` means nothing outside this method can safely touch a lazy association on the entities it returns

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

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.ticketTypes WHERE e.domainId = :domainId AND e.organizer.domainId = :organizerDomainId")
    Optional<Event> findByDomainIdAndOrganizerDomainId(@Param("domainId") UUID domainId, @Param("organizerDomainId") UUID organizerDomainId);
}
```

This repository method combines two search criteria:

- The event's `domainId`
- The organizer's `domainId`, navigated through the `organizer` relationship, to ensure users can only access their own events

Unlike `listEventsForOrganizer`, this method returns a single `Optional<Event>`, not a `Page`, so there's no pagination to conflict with fetch-joining `ticketTypes` -- both `venue` and `ticketTypes` are safe to fetch join directly in one query. `SELECT DISTINCT` is still required, though: without it, an event with more than one ticket type comes back as multiple duplicate rows, and Spring Data would throw `NonUniqueResultException` trying to collapse them into a single `Optional`.

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
    @Transactional(readOnly = true)
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
    public EventNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public EventNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

```java
public class TicketTypeNotFoundException extends EventTicketException {
    public TicketTypeNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketTypeNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketTypeNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

```java
public class EventUpdateException extends EventTicketException {
    public EventUpdateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventUpdateException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public EventUpdateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

Each exception serves a specific purpose:

- `EventNotFoundException` - When a requested event doesn't exist
- `TicketTypeNotFoundException` - When a referenced ticket type can't be found
- `EventUpdateException` - For validation failures during an update -- note `EVENT_ID_REQUIRED` and `EVENT_ID_MISMATCH` are two distinct `ErrorCode` values, not one shared "update failed" code, so the client can tell which specific problem occurred

#### Exception Handler

Unlike earlier lessons, there's no `GlobalExceptionHandler` update to make here. It has a single `@ExceptionHandler(EventTicketException.class)` method (see "Global Exception Handler") that reads its HTTP status and message straight from `ex.getErrorCode()` -- so any new subclass of `EventTicketException`, as long as it's constructed with an `ErrorCode`, is already handled the moment it's thrown. That's the whole point of routing every domain exception through `ErrorCode` instead of a hardcoded string: adding a new failure case is now just a new enum constant and a new message key, not a new handler method too.

#### Summary

- Added the `EventNotFoundException` exception
- Added the `EventUpdateException` exception
- Added the `TicketTypeNotFoundException` exception
- No `GlobalExceptionHandler` changes needed -- its single `EventTicketException` handler already covers these

### Update Event Service

In this lesson, we'll implement the update event functionality in the service layer, which allows event organizers to modify existing events and their associated ticket types.

#### Service Layer Implementation

The update functionality needs to handle both the event details and its ticket types, ensuring data consistency and proper validation.

Let's implement the `updateEventForOrganizer` method in our service:

```java
@Override
@Transactional
public Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event) {
    if (null == event.getId()) {
        throw new EventUpdateException(ErrorCode.EVENT_ID_REQUIRED);
    }

    if (!id.equals(event.getId())) {
        throw new EventUpdateException(ErrorCode.EVENT_ID_MISMATCH, id);
    }

    Event existingEvent = eventRepository
            .findByDomainIdAndOrganizerDomainId(id, organizerId)
            .orElseThrow(() -> new EventNotFoundException(ErrorCode.EVENT_NOT_FOUND, id));

    Venue venue = venueRepository.findByDomainId(event.getVenueId())
            .orElseThrow(() -> new VenueNotFoundException(ErrorCode.VENUE_NOT_FOUND, event.getVenueId()));

    existingEvent.setName(event.getName());
    existingEvent.setStart(event.getStart());
    existingEvent.setEnd(event.getEnd());
    venue.addEvent(existingEvent);
    existingEvent.setSalesStart(event.getSalesStart());
    existingEvent.setSalesEnd(event.getSalesEnd());
    existingEvent.setStatus(event.getStatus());

    // UpdateTicketTypeRequest.id is the ticket type's domainId, not its internal id
    Set<UUID> requestTicketTypeDomainIds = event.getTicketTypes()
            .stream()
            .map(UpdateTicketTypeRequest::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    Set<TicketType> ticketTypesToRemove = existingEvent.getTicketTypes().stream()
            .filter(existingTicketType -> !requestTicketTypeDomainIds.contains(existingTicketType.getDomainId()))
            .collect(Collectors.toSet());
    ticketTypesToRemove.forEach(existingEvent::removeTicketType);

    Map<UUID, TicketType> existingTicketTypesIndex = existingEvent.getTicketTypes().stream()
            .collect(Collectors.toMap(TicketType::getDomainId, Function.identity()));

    for (UpdateTicketTypeRequest ticketType : event.getTicketTypes()) {
        if (null == ticketType.getId()) {
            // Create
            TicketType ticketTypeToCreate = new TicketType();
            ticketTypeToCreate.setDomainId(UUID.randomUUID());
            ticketTypeToCreate.setName(ticketType.getName());
            ticketTypeToCreate.setPrice(ticketType.getPrice());
            ticketTypeToCreate.setDescription(ticketType.getDescription());
            ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
            existingEvent.addTicketType(ticketTypeToCreate);
        } else if (existingTicketTypesIndex.containsKey(ticketType.getId())) {
            // Update
            TicketType existingTicketType = existingTicketTypesIndex.get(ticketType.getId());
            existingTicketType.setName(ticketType.getName());
            existingTicketType.setPrice(ticketType.getPrice());
            existingTicketType.setDescription(ticketType.getDescription());
            existingTicketType.setTotalAvailable(ticketType.getTotalAvailable());
        } else {
            throw new TicketTypeNotFoundException(ErrorCode.TICKET_TYPE_NOT_FOUND, ticketType.getId());
        }
    }

    return eventRepository.save(existingEvent);
}
```

Note that `ticketType.getId()` throughout this method refers to `UpdateTicketTypeRequest.id`, which -- like every other DTO-facing ID -- is really the ticket type's `domainId`. We match it against `existingTicketType.getDomainId()`, never `existingTicketType.getId()` (the internal sequential key). New ticket types get a fresh `domainId` the same way new events do.

Two changes from the raw-`Set`-mutation version you might expect: `existingEvent.getTicketTypes().removeIf(...)` is now `existingEvent.getTicketTypes().stream().filter(...).collect(...)` followed by `ticketTypesToRemove.forEach(existingEvent::removeTicketType)` -- collecting into a separate `Set` first avoids mutating `existingEvent.getTicketTypes()` while we're still iterating a view of it, and routing the removal through `removeTicketType` also nulls out each removed `TicketType`'s back-reference to the event, not just dropping it from the collection. And `venue.addEvent(existingEvent)` replaces `existingEvent.setVenue(venue)` for the same reason it did in `createEvent`.

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
    @Builder.Default
    private Set<TicketType> ticketTypes = new LinkedHashSet<>();

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
@Transactional(readOnly = true)
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

    // Find events by their status (e.g., PUBLISHED), fetch-joining venue -- safe with
    // Pageable since it's a @ManyToOne, not a collection (see List Event Service)
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.venue WHERE e.status = :status")
    Page<Event> findByStatus(@Param("status") EventStatusEnum status, Pageable pageable);
}
```

`ListPublishedEventResponseDto` only needs `venue`, not `ticketTypes`, so a single `LEFT JOIN FETCH` is enough here -- no need for the two-step pattern `listEventsForOrganizer` uses.

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

The service layer implementation connects the repository query to our application, but there's one more thing to handle: `ListPublishedEventResponseDto` needs `event.getVenue()`, and this is a native query -- JPQL's `JOIN FETCH` syntax isn't available here, so `venue` would otherwise stay a lazy proxy that throws `LazyInitializationException` once this method returns and the session closes. We force it to load while the session is still open instead:

```java
@Override
@Transactional(readOnly = true)
public Page<Event> searchPublishedEvents(String query, Pageable pageable) {
    Page<Event> page = eventRepository.searchEvents(query, pageable);
    page.getContent().forEach(event -> Hibernate.initialize(event.getVenue()));
    return page;
}
```

`Hibernate.initialize(...)` is the standard escape hatch for exactly this case: it forces an uninitialized lazy proxy to load right now, inside the current session, rather than waiting for the first real property access. It costs one extra query per distinct venue in this page (there's no way to batch it into the native query itself), but for a page of search results that's a small, bounded cost -- and it's the only option left once `JOIN FETCH` isn't on the table.

#### Summary

- Added the `searchEvents` custom query to the `EventRepository` interface
- Added the `searchPublishedEvents` method to the `EventService` interface
- Implemented `searchPublishedEvents`, using `Hibernate.initialize(...)` to force-load each result's `venue` since the native query can't `JOIN FETCH` it

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
@Transactional(readOnly = true)
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
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.ticketTypes WHERE e.domainId = :domainId AND e.status = :status")
    Optional<Event> findByDomainIdAndStatus(@Param("domainId") UUID domainId, @Param("status") EventStatusEnum status);
}
```

This method explicitly filters by both domain ID and status. `GetPublishedEventDetailsResponseDto` needs both `venue` and `ticketTypes`, and since this returns a single `Optional<Event>` rather than a `Page`, both are safe to fetch join directly -- same as `getEventForOrganizer` earlier.

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
    public QrCodeGenerationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public QrCodeGenerationException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public QrCodeGenerationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

No `GlobalExceptionHandler` update needed -- `QR_CODE_GENERATION_FAILED` (see "Error Codes") is already mapped to `HttpStatus.INTERNAL_SERVER_ERROR`, and the single `EventTicketException` handler picks that status up automatically. 500 is the right code here since QR code generation failures are server-side issues, not something the client did wrong.

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

```gradle
implementation 'com.google.zxing:core:3.5.1'
implementation 'com.google.zxing:javase:3.5.1'
```

`core` is the actual encode/decode engine (`QRCodeWriter`, used below). `javase` is a small Java SE integration layer on top of it -- specifically `MatrixToImageWriter`, which converts ZXing's `BitMatrix` output into a `java.awt.image.BufferedImage` so it can be written out as a PNG via `ImageIO`. Both are needed for the flow below to compile.

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
            ticket.addQrCode(qrCode);

            return qrCodeRepository.saveAndFlush(qrCode);
        } catch(IOException | WriterException ex) {
            throw new QrCodeGenerationException(ErrorCode.QR_CODE_GENERATION_FAILED, ex);
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

`ticket.addQrCode(qrCode)` (see "Bidirectional Relationship Helpers") replaces a plain `qrCode.setTicket(ticket)` -- it also adds `qrCode` to `ticket.getQrCodes()`, so the in-memory `ticket` object stays consistent with what just got persisted, in case anything later in the same transaction reads its QR codes back.

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
            .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // Get ticket type with pessimistic lock
        TicketType ticketType = ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)
            .orElseThrow(() -> new TicketTypeNotFoundException(ErrorCode.TICKET_TYPE_NOT_FOUND, ticketTypeId));

        // Check ticket availability -- ticketType.getId() here is the resolved entity's
        // internal sequential id, used purely as an internal join key against tickets.ticket_type_id
        int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
        Integer totalAvailable = ticketType.getTotalAvailable();

        if(purchasedTickets + 1 > totalAvailable) {
            throw new TicketsSoldOutException(ErrorCode.TICKET_SOLD_OUT, ticketTypeId);
        }

        // Create new ticket
        Ticket ticket = new Ticket();
        ticket.setDomainId(UUID.randomUUID());
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setPurchaser(user);
        ticketType.addTicket(ticket);

        // Save and generate QR code
        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);

        return ticketRepository.save(savedTicket);
    }
}
```

`ticketType.addTicket(ticket)` (see "Bidirectional Relationship Helpers") replaces `ticket.setTicketType(ticketType)` -- there's no matching helper needed for `ticket.setPurchaser(user)` though, since `User` has no back-reference collection to `Ticket` (no `purchasedTickets` field), so that stays a plain setter.

`TicketsSoldOutException` also now needs its `ErrorCode`-based constructors, matching every other exception in the hierarchy:

```java
public class TicketsSoldOutException extends EventTicketException {
    public TicketsSoldOutException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketsSoldOutException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketsSoldOutException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

Note this is the first time we're throwing it -- unlike `UserNotFoundException`/`TicketTypeNotFoundException`, it wasn't defined back in "Error Codes"'s exception lessons, so this is its introduction. It maps to `ErrorCode.TICKET_SOLD_OUT`, which resolves to `409 Conflict` rather than the `400 Bad Request` everything else here uses -- running out of tickets is a different kind of failure than "the thing you asked for doesn't exist."

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

#### The Role Enum

Before writing the converter, let's give these three roles a proper type instead of passing bare `"ORGANIZER"`/`"STAFF"` strings around everywhere they're checked:

```java
public enum Role {
    ORGANIZER,
    ATTENDEE,
    STAFF;

    public static final String AUTHORITY_PREFIX = "ROLE_";

    public String getAuthority() {
        return AUTHORITY_PREFIX + name();
    }
}
```

The enum constant names match Spring Security's `hasRole(...)` convention (which adds the `ROLE_` prefix itself), while `getAuthority()` returns the full, Keycloak-facing authority string (`ROLE_ORGANIZER`) for anywhere we need that instead. `AUTHORITY_PREFIX` being `public static final` means the JWT converter below can reference it directly, rather than repeating the literal `"ROLE_"` string a second time.

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
        .filter(role -> role.startsWith(Role.AUTHORITY_PREFIX))
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

- Added the `Role` enum, giving `ORGANIZER`/`ATTENDEE`/`STAFF` a proper type instead of bare strings
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
        // Ticket purchase lives under /api/v1/events/** but is an attendee action, not
        // organizer management -- it must be matched before the broader rule below, or
        // that rule (evaluated first-match-wins) would catch it and lock it to organizers.
        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/ticket-types/*/tickets").authenticated()
        .requestMatchers("/api/v1/events/**").hasRole(Role.ORGANIZER.name())
        // Catch all rule
        .anyRequest().authenticated())
```

The `.hasRole(Role.ORGANIZER.name())` method is used to restrict access to users with the organizer role -- `Role.ORGANIZER.name()` is just `"ORGANIZER"`, but going through the enum means there's one place, not a scattered handful of string literals, that defines what the valid role names are.

When using `hasRole()`, Spring Security automatically adds the `ROLE_` prefix to the role name, so we don't need to include it in our configuration.

Note this is `/api/v1/events/**` with a wildcard, not the bare `/api/v1/events` path -- without it, only the exact list/create path (`POST`/`GET /api/v1/events`) would be organizer-only, and `GET`/`PUT`/`DELETE /api/v1/events/{eventId}` would fall through to the generic `.anyRequest().authenticated()` rule at the bottom, letting *any* authenticated user (not just the organizer who owns the event) reach them. The service layer's own `organizerId` checks (`getEventForOrganizer`, `updateEventForOrganizer`, `deleteEventForOrganizer` all filter by the caller's domain ID) still stop a different organizer from reading or modifying someone else's event, but an attendee account shouldn't be able to reach those endpoints at all.

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
@Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.ticketType WHERE t.purchaser.domainId = :purchaserDomainId")
Page<Ticket> findByPurchaserDomainId(@Param("purchaserDomainId") UUID purchaserDomainId, Pageable pageable);
```

`userId` here comes from the JWT subject, which is the purchaser's `domainId` -- so the query needs to navigate from `Ticket` into the `purchaser` relationship and filter on its `domainId`, not `Ticket`'s own internal `id`.

`ListTicketResponseDto` needs `ticket.getTicketType()`, and just like `Event.venue`, `Ticket.ticketType` is a `@ManyToOne` -- fetch-joining it is safe together with `Pageable`, for the same reason `venue` was safe to fetch join in "List Event Service": a to-one join can't multiply result rows the way a to-many one can.

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
  @Transactional(readOnly = true)
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
@Query("SELECT t FROM Ticket t " +
        "LEFT JOIN FETCH t.ticketType tt " +
        "LEFT JOIN FETCH tt.event e " +
        "LEFT JOIN FETCH e.venue " +
        "WHERE t.domainId = :domainId AND t.purchaser.domainId = :purchaserDomainId")
Optional<Ticket> findByDomainIdAndPurchaserDomainId(@Param("domainId") UUID domainId, @Param("purchaserDomainId") UUID purchaserDomainId);
```

`GetTicketResponseDto` (below) walks `ticket -> ticketType -> event -> venue` -- three hops, all `@ManyToOne`. Chaining `LEFT JOIN FETCH` across all three loads the whole chain in one query; since every hop is to-one, none of them can multiply result rows, so unlike the `ticketTypes` case in "List Event Service", there's no `DISTINCT` needed and no two-step pattern required here either.

#### Implementing the Service Layer

Now we'll create a method in the `TicketService` that uses our new repository method.

This method will act as a pass-through to the repository, maintaining the same return type and validation logic.

```java
@Override
@Transactional(readOnly = true)
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
        .orElseThrow(() -> new QrCodeNotFoundException(ErrorCode.QR_CODE_NOT_FOUND, ticketId));

    try {
      return Base64.getDecoder().decode(qrCode.getValue());
    } catch(IllegalArgumentException ex) {
      log.error("Invalid base64 QR Code for ticket ID: {}", ticketId, ex);
      throw new QrCodeNotFoundException(ErrorCode.QR_CODE_NOT_FOUND, ticketId);
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

#### Two More Exceptions

Both lookups below can fail, and neither exception has been defined yet -- both follow the same `ErrorCode`-based pattern as every other exception in the hierarchy:

```java
public class QrCodeNotFoundException extends EventTicketException {
    public QrCodeNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public QrCodeNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public QrCodeNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

```java
public class TicketNotFoundException extends EventTicketException {
    public TicketNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

They map to `ErrorCode.QR_CODE_NOT_FOUND` and `ErrorCode.TICKET_NOT_FOUND` respectively (see "Error Codes") -- both already `400 Bad Request`, and both already covered by `GlobalExceptionHandler`'s single `EventTicketException` handler with no further changes needed.

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
        .orElseThrow(() -> new QrCodeNotFoundException(ErrorCode.QR_CODE_NOT_FOUND, qrCodeId));

    Ticket ticket = qrCode.getTicket();

    return validateTicket(ticket, TicketValidationMethod.QR_SCAN);
  }

  @Override
  public TicketValidation validateTicketManually(UUID ticketId) {
    Ticket ticket = ticketRepository.findByDomainId(ticketId)
        .orElseThrow(() -> new TicketNotFoundException(ErrorCode.TICKET_NOT_FOUND, ticketId));
    return validateTicket(ticket, TicketValidationMethod.MANUAL);
  }

  private TicketValidation validateTicket(Ticket ticket, TicketValidationMethod method) {
    TicketValidationStatusEnum ticketValidationStatus = ticket.getValidations().stream()
        .filter(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()))
        .findFirst()
        .map(v -> TicketValidationStatusEnum.INVALID)
        .orElse(TicketValidationStatusEnum.VALID);

    TicketValidation ticketValidation = new TicketValidation();
    ticketValidation.setDomainId(UUID.randomUUID());
    ticketValidation.setValidationMethod(method);
    ticketValidation.setStatus(ticketValidationStatus);
    ticket.addValidation(ticketValidation);

    return ticketValidationRepository.save(ticketValidation);
  }
}
```

Three things changed from a first pass at this method:

- `validateTicket` now takes the `method` it was called with, instead of hardcoding `TicketValidationMethod.QR_SCAN` -- a shared private helper hardcoding one caller's value is an easy bug to introduce and an easy one to miss, since `validateTicketManually` would silently record every manual validation as a QR scan.
- `ticket.addValidation(ticketValidation)` (see "Bidirectional Relationship Helpers") replaces `ticketValidation.setTicket(ticket)`, and moves to *after* the status is computed. `addValidation` also adds `ticketValidation` to `ticket.getValidations()` -- doing that before reading `ticket.getValidations().stream()...` to compute the status would mean the stream sees the not-yet-valid validation we're currently building, which happens to be harmless here (its `status` is still `null` at that point, so the `VALID` filter never matches it), but ordering it after removes the question entirely.
- `qrCode.getTicket()` is a lazy `@ManyToOne` proxy, not a fully-loaded `Ticket` -- but since `validateTicket` immediately calls `ticket.getValidations()` on it (still inside this method's `@Transactional`), that access forces the proxy to initialize before the method returns, so nothing later touches an uninitialized reference outside the session.

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

- The `validateTicketByQrCode` method looks up an active QR code and validates the associated ticket, recording the method as `QR_SCAN`.

- The `validateTicketManually` method looks up a ticket directly by domain ID and validates it, recording the method as `MANUAL`.

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
            .requestMatchers("/api/v1/ticket-validations").hasRole(Role.STAFF.name())
            // Catch all rule
            .anyRequest().authenticated())
```

`/api/v1/ticket-validations` has just the one `POST` endpoint, so there's no wildcard needed here the way `/api/v1/events/**` needed one -- an exact path match already covers everything under this controller.

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

## Venue Management

Every event so far has referenced a `Venue` by `venueId`, but nothing has ever actually created one -- the `venues` table has only ever been populated by hand, directly in the database. This section closes that gap: organizers get `create`/`update`/`list`/`get` endpoints for venues, following the same DTO-to-internal-model-to-entity pattern as everything else in this build. There's no dedicated frontend screen for venue management in this build yet, so unlike the earlier feature sections, there's no "Ui Testing" lesson here -- these endpoints are exercised directly (Postman, curl, or similar) until a UI exists for them.

### Venue Design

#### Internal Request Models

Following the same split we used for events -- a validation-free internal model the service layer works with, separate from the validated DTO the controller receives -- `Venue` gets its own pair:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVenueRequest {
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
    private Integer capacity;
    private String accessibilityInfo;
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVenueRequest {
    private UUID id;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
    private Integer capacity;
    private String accessibilityInfo;
}
```

`UpdateVenueRequest.id` exists for the same reason `UpdateEventRequest.id` does -- so the service layer can confirm the ID in the request body matches the ID in the URL path before applying any changes.

#### Service Interface

```java
public interface VenueService {
    Venue createVenue(CreateVenueRequest request);

    Venue updateVenue(UUID id, UpdateVenueRequest request);

    Page<Venue> listVenues(Pageable pageable);

    Optional<Venue> getVenue(UUID id);
}
```

We're including `listVenues` and `getVenue` from the start, not just `createVenue`/`updateVenue` -- an organizer creating an event needs some way to discover existing venues to pick a `venueId` from, and there was previously no API for that at all.

#### Summary

- Added the `CreateVenueRequest`/`UpdateVenueRequest` internal models
- Created the `VenueService` interface with `createVenue`, `updateVenue`, `listVenues`, and `getVenue`

### Venue Exceptions

Updating a venue needs the same ID-consistency guard `updateEventForOrganizer` uses -- a dedicated exception, with its own `ErrorCode`s, rather than reusing `EventUpdateException` (which would be a confusing name for a venue-related failure):

```java
public class VenueUpdateException extends EventTicketException {
    public VenueUpdateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VenueUpdateException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public VenueUpdateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
```

`VENUE_ID_REQUIRED` and `VENUE_ID_MISMATCH` (see "Error Codes") are already defined -- this is the first lesson that actually throws them. `VenueNotFoundException` already exists too, from resolving `venueId` when creating/updating events; venue management reuses it as-is.

#### Summary

- Added the `VenueUpdateException` exception, mirroring `EventUpdateException`'s two-code (`VENUE_ID_REQUIRED`/`VENUE_ID_MISMATCH`) pattern
- Reused the existing `VenueNotFoundException` for lookups by domain ID

### Venue Service Implementation

```java
@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    @Override
    @Transactional
    public Venue createVenue(CreateVenueRequest request) {
        Venue venueToCreate = new Venue();
        venueToCreate.setDomainId(UUID.randomUUID());
        venueToCreate.setName(request.getName());
        venueToCreate.setAddressLine1(request.getAddressLine1());
        venueToCreate.setAddressLine2(request.getAddressLine2());
        venueToCreate.setCity(request.getCity());
        venueToCreate.setPostalCode(request.getPostalCode());
        venueToCreate.setCountry(request.getCountry());
        venueToCreate.setLatitude(request.getLatitude());
        venueToCreate.setLongitude(request.getLongitude());
        venueToCreate.setCapacity(request.getCapacity());
        venueToCreate.setAccessibilityInfo(request.getAccessibilityInfo());

        return venueRepository.save(venueToCreate);
    }

    @Override
    @Transactional
    public Venue updateVenue(UUID id, UpdateVenueRequest request) {
        if (null == request.getId()) {
            throw new VenueUpdateException(ErrorCode.VENUE_ID_REQUIRED);
        }

        if (!id.equals(request.getId())) {
            throw new VenueUpdateException(ErrorCode.VENUE_ID_MISMATCH, id);
        }

        Venue existingVenue = venueRepository.findByDomainId(id)
                .orElseThrow(() -> new VenueNotFoundException(ErrorCode.VENUE_NOT_FOUND, id));

        existingVenue.setName(request.getName());
        existingVenue.setAddressLine1(request.getAddressLine1());
        existingVenue.setAddressLine2(request.getAddressLine2());
        existingVenue.setCity(request.getCity());
        existingVenue.setPostalCode(request.getPostalCode());
        existingVenue.setCountry(request.getCountry());
        existingVenue.setLatitude(request.getLatitude());
        existingVenue.setLongitude(request.getLongitude());
        existingVenue.setCapacity(request.getCapacity());
        existingVenue.setAccessibilityInfo(request.getAccessibilityInfo());

        return venueRepository.save(existingVenue);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Venue> listVenues(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venue> getVenue(UUID id) {
        return venueRepository.findByDomainId(id);
    }
}
```

`listVenues` doesn't need a custom `@Query` -- `findAll(Pageable)` already comes from `JpaRepository`, and `Venue` has no lazy associations that `VenueResponseDto` touches (it never serializes `venue.getEvents()`), so there's no `LazyInitializationException` risk to design around here the way there was for `Event`.

#### Summary

- Implemented `createVenue`, `updateVenue`, `listVenues`, and `getVenue` in `VenueServiceImpl`
- `updateVenue` follows the same ID-consistency-guard shape as `updateEventForOrganizer`
- No repository changes needed for `listVenues`/`getVenue` -- `findAll` is inherited, and `findByDomainId` already existed

### Venue Dtos And Conversion Methods

#### Request DTOs

Validation mirrors the entity's `nullable = false` columns -- `name`, `addressLine1`, `city`, `postalCode`, and `country` are required; `addressLine2`, `latitude`, `longitude`, `capacity`, and `accessibilityInfo` are all optional:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVenueRequestDto {
    @NotBlank(message = "{validation.venue.name.required}")
    private String name;

    @NotBlank(message = "{validation.venue.address-line-1.required}")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "{validation.venue.city.required}")
    private String city;

    @NotBlank(message = "{validation.venue.postal-code.required}")
    private String postalCode;

    @NotBlank(message = "{validation.venue.country.required}")
    private String country;

    private Double latitude;
    private Double longitude;

    @PositiveOrZero(message = "{validation.venue.capacity.positive-or-zero}")
    private Integer capacity;

    private String accessibilityInfo;
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVenueRequestDto {
    @NotNull(message = "{validation.venue.id.required}")
    private UUID id;

    @NotBlank(message = "{validation.venue.name.required}")
    private String name;

    @NotBlank(message = "{validation.venue.address-line-1.required}")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "{validation.venue.city.required}")
    private String city;

    @NotBlank(message = "{validation.venue.postal-code.required}")
    private String postalCode;

    @NotBlank(message = "{validation.venue.country.required}")
    private String country;

    private Double latitude;
    private Double longitude;

    @PositiveOrZero(message = "{validation.venue.capacity.positive-or-zero}")
    private Integer capacity;

    private String accessibilityInfo;
}
```

#### Extending VenueResponseDto

`VenueResponseDto` already exists -- it's been reused across every event-related response since "Create Event" -- but it was only ever missing two fields the entity actually has: `capacity` and `accessibilityInfo`. Now that `Venue` is a first-class managed resource in its own right, its response shape should include everything the entity carries:

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
    private Integer capacity;
    private String accessibilityInfo;
}
```

Since it's the same class already embedded in `CreateEventResponseDto`, `ListEventResponseDto`, and every other event response, `EventService.convertToVenueResponseDto` needs the two new `dto.setXxx(...)` calls too, so event responses pick up the new fields on their nested `venue` object as well:

```java
// In EventServiceImpl.convertToVenueResponseDto
dto.setCapacity(venue.getCapacity());
dto.setAccessibilityInfo(venue.getAccessibilityInfo());
```

#### Conversion Methods

```java
public interface VenueService {
    // Existing methods...

    CreateVenueRequest convertFromDto(CreateVenueRequestDto dto);

    UpdateVenueRequest convertFromDto(UpdateVenueRequestDto dto);

    VenueResponseDto convertToVenueResponseDto(Venue venue);
}
```

```java
@Override
public CreateVenueRequest convertFromDto(CreateVenueRequestDto dto) {
    CreateVenueRequest request = new CreateVenueRequest();
    request.setName(dto.getName());
    request.setAddressLine1(dto.getAddressLine1());
    request.setAddressLine2(dto.getAddressLine2());
    request.setCity(dto.getCity());
    request.setPostalCode(dto.getPostalCode());
    request.setCountry(dto.getCountry());
    request.setLatitude(dto.getLatitude());
    request.setLongitude(dto.getLongitude());
    request.setCapacity(dto.getCapacity());
    request.setAccessibilityInfo(dto.getAccessibilityInfo());
    return request;
}

@Override
public UpdateVenueRequest convertFromDto(UpdateVenueRequestDto dto) {
    UpdateVenueRequest request = new UpdateVenueRequest();
    request.setId(dto.getId());
    request.setName(dto.getName());
    request.setAddressLine1(dto.getAddressLine1());
    request.setAddressLine2(dto.getAddressLine2());
    request.setCity(dto.getCity());
    request.setPostalCode(dto.getPostalCode());
    request.setCountry(dto.getCountry());
    request.setLatitude(dto.getLatitude());
    request.setLongitude(dto.getLongitude());
    request.setCapacity(dto.getCapacity());
    request.setAccessibilityInfo(dto.getAccessibilityInfo());
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
    dto.setCapacity(venue.getCapacity());
    dto.setAccessibilityInfo(venue.getAccessibilityInfo());
    return dto;
}
```

`VenueService` gets its own `convertToVenueResponseDto`, separate from `EventService`'s -- we're not reaching across services to share a conversion method, the same way `ListEventTicketTypeResponseDto` and `ListTicketTicketTypeResponseDto` each got their own `TicketType`-to-DTO method in earlier lessons rather than one shared between `EventService` and `TicketService`. A few lines of duplication per service is the trade-off this build has made consistently, in exchange for every service being usable on its own.

#### Summary

- Created `CreateVenueRequestDto`/`UpdateVenueRequestDto`, validated against the entity's `nullable = false` columns
- Extended `VenueResponseDto` with `capacity`/`accessibilityInfo`, and updated `EventService.convertToVenueResponseDto` to populate them
- Added `convertFromDto`/`convertToVenueResponseDto` to `VenueService`, kept separate from `EventService`'s own copy

### Venue Controller

```java
@RestController
@RequestMapping(path = "/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueResponseDto> createVenue(
            @Valid @RequestBody CreateVenueRequestDto createVenueRequestDto) {
        CreateVenueRequest createVenueRequest = venueService.convertFromDto(createVenueRequestDto);
        Venue createdVenue = venueService.createVenue(createVenueRequest);
        VenueResponseDto venueResponseDto = venueService.convertToVenueResponseDto(createdVenue);
        return new ResponseEntity<>(venueResponseDto, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{venueId}")
    public ResponseEntity<VenueResponseDto> updateVenue(
            @PathVariable UUID venueId,
            @Valid @RequestBody UpdateVenueRequestDto updateVenueRequestDto) {
        UpdateVenueRequest updateVenueRequest = venueService.convertFromDto(updateVenueRequestDto);
        Venue updatedVenue = venueService.updateVenue(venueId, updateVenueRequest);
        VenueResponseDto venueResponseDto = venueService.convertToVenueResponseDto(updatedVenue);
        return ResponseEntity.ok(venueResponseDto);
    }

    @GetMapping
    public ResponseEntity<Page<VenueResponseDto>> listVenues(Pageable pageable) {
        Page<Venue> venues = venueService.listVenues(pageable);
        return ResponseEntity.ok(venues.map(venueService::convertToVenueResponseDto));
    }

    @GetMapping(path = "/{venueId}")
    public ResponseEntity<VenueResponseDto> getVenue(@PathVariable UUID venueId) {
        return venueService.getVenue(venueId)
                .map(venueService::convertToVenueResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

No `parseUserId`/`@AuthenticationPrincipal Jwt` anywhere in this controller -- `Venue` has no `organizer`/owner field, so unlike `EventController`, there's no per-caller filtering to do here. Access is still restricted (organizers only), but that's handled entirely at the security layer, next.

#### Summary

- Implemented `VenueController` with create, update, list, and get-by-id endpoints
- No JWT-derived filtering needed, since venues aren't owned by a specific organizer

### Securing Venue Endpoints

Venue management is organizer-only, the same as event management:

```java
.authorizeHttpRequests(authorize ->
    authorize
        .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/ticket-types/*/tickets").authenticated()
        .requestMatchers("/api/v1/events/**").hasRole(Role.ORGANIZER.name())
        .requestMatchers("/api/v1/venues/**").hasRole(Role.ORGANIZER.name())
        .requestMatchers("/api/v1/ticket-validations").hasRole(Role.STAFF.name())
        // Catch all rule
        .anyRequest().authenticated())
```

This is the complete `authorizeHttpRequests` block, gathering every rule introduced across the build: published events are public, ticket purchase just needs authentication (any role), events and venues are organizer-only, ticket validation is staff-only, and everything else falls through to "any authenticated user" -- which is exactly right for `/api/v1/tickets/**`, since an attendee viewing their own purchased tickets doesn't need a special role, just to be logged in.

#### Summary

- Added `/api/v1/venues/**` to `SecurityConfig`, restricted to `Role.ORGANIZER`
- This is the final state of `authorizeHttpRequests`, consolidating every rule from earlier lessons in one place

## Frontend Project Setup

Everything so far has been backend. It's time to actually build the frontend the earlier lessons in this document kept assuming was already there -- it wasn't; see "Project Status" at the end of this document for how that gap got tracked in the meantime. We're building it as `frontend/`, a new top-level directory alongside `ticket-service` and `analytics-service` in the same repository, not a separate one.

### Scaffold the Project

The TanStack Start ecosystem's scaffolding tools have moved fast enough that the commands you'll find in most tutorials are already out of date. We confirmed the current path against the live docs rather than guessing, and used the TanStack CLI directly:

```bash
npx @tanstack/cli@latest create frontend --package-manager npm --add-ons shadcn,tanstack-query,eslint
```

This one command produces a working TanStack Start app (React 19, Vite, file-based routing) with three things wired in from the start:

- **Tailwind CSS v4** and **shadcn/ui** (the `shadcn` add-on), pre-configured with the `new-york` style
- **TanStack Query**, already integrated with the router -- `src/router.tsx` builds a `QueryClient` and threads it into the router's context, and `src/routes/__root.tsx` already declares `queryClient` on `MyRouterContext`. There was no manual `QueryClientProvider` wiring left to do.
- **ESLint + Prettier**

One naming quirk worth knowing: this scaffold uses Node's native subpath imports (`"imports": { "#/*": "./src/*" }` in `package.json`) as its import alias, not the `@/*` convention older tutorials show. `components.json` (shadcn's config file) is already set up to match, so every `npx shadcn add <component>` generates imports using `#/*` automatically -- we followed that convention throughout rather than fighting it.

#### Feature-Based Folders

Rather than organizing by technical layer the way `ticket-service` does (`controller/`, `domain/service/`, `domain/dto/`), the frontend is organized by feature, one directory per backend entity:

```
src/features/
  events/{api.ts,types.ts,hooks.ts,components/}
  ticket-types/{api.ts,types.ts,hooks.ts,components/}
  tickets/{api.ts,types.ts,hooks.ts,components/}
  venues/{api.ts,types.ts,hooks.ts,components/}
  auth/{api.ts,types.ts,hooks.ts,components/}
  analytics/{api.ts,types.ts,hooks.ts,components/}
```

Each feature's `api.ts` holds typed fetch functions, `types.ts` mirrors the backend's DTOs field-for-field, and `hooks.ts` wraps `api.ts` in named React Query hooks (`useEvents()`, `useCreateEvent()`, and so on) -- the query keys and cache-invalidation rules live in exactly one place, rather than being copy-pasted at every call site that needs the same data. `src/routes/` (TanStack Start's file-based routes) stays thin: it composes screens from features, it doesn't own data-fetching logic itself.

Shared, non-shadcn components go in a flat `src/components/` -- but only once a second feature actually needs one. A component that's only used by one feature stays inside that feature's own `components/` folder; it gets promoted to the shared folder on its second real use, not preemptively.

#### Environment Configuration

`.env.local` (git-ignored) holds the two backend URLs and the Keycloak settings:

```bash
VITE_TICKET_SERVICE_URL=http://localhost:4005/ticket-service
VITE_ANALYTICS_SERVICE_URL=http://localhost:3001
VITE_KEYCLOAK_ISSUER=http://localhost:9090/realms/event-ticket-platform
VITE_KEYCLOAK_CLIENT_ID=event-ticket-platform-app
```

`ticket-service`'s base URL isn't just a port -- it also carries `server.servlet.context-path=/ticket-service` from `application.properties`, so every API call is actually `/ticket-service/api/v1/...`. Baking the context path into the env var here means individual features' `api.ts` files never need to know it exists; they just append `/api/v1/...`.

#### Summary

- Scaffolded `frontend/` via the TanStack CLI with the `shadcn`, `tanstack-query`, and `eslint` add-ons -- Tailwind, shadcn/ui, and React Query all came pre-wired
- Adopted the scaffold's own `#/*` import alias rather than the more common `@/*`, since shadcn's generated components already assume it
- Organized the app by feature (`src/features/<entity>/`), not by technical layer, mirroring the backend's domain vocabulary rather than its folder structure
- `.env.local` holds both backend base URLs (including `ticket-service`'s `/ticket-service` context path) and the Keycloak issuer/client id

## Frontend Authentication

With the project scaffolded, the next thing it needs before any real screen makes sense is a working login. Keycloak already has a client registered for this app (`event-ticket-platform-app`, from "Running Keycloak" earlier) -- we're wiring the frontend to actually use it.

### Configure the OIDC Client

```bash
npm install react-oidc-context oidc-client-ts
```

`oidc-client-ts` does the actual protocol work (`react-oidc-context` is a thin React wrapper around it), so the `UserManager` is constructed by hand rather than letting `react-oidc-context` build one implicitly from settings -- that's what makes it possible to also reach it from outside React, which the route guards need:

```typescript
// src/lib/oidc.ts
import { UserManager, WebStorageStateStore, type User } from 'oidc-client-ts';

let userManagerInstance: UserManager | undefined;

export function getUserManager(): UserManager {
  if (typeof window === 'undefined') {
    throw new Error('getUserManager() must only be called on the client');
  }

  if (!userManagerInstance) {
    userManagerInstance = new UserManager({
      authority: import.meta.env.VITE_KEYCLOAK_ISSUER,
      client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
      redirect_uri: `${window.location.origin}/callback`,
      post_logout_redirect_uri: window.location.origin,
      response_type: 'code',
      scope: 'openid profile email',
      userStore: new WebStorageStateStore({ store: window.sessionStorage }),
      stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
      automaticSilentRenew: true,
    });
  }

  return userManagerInstance;
}
```

This went through two live-bug-driven revisions before landing here -- worth walking through both, since neither was hypothetical and the reasoning explains why plain "keep it in memory" doesn't actually work for either store.

**First attempt: a custom in-memory store for both `userStore` and `stateStore`.** The theory was that "in-memory tokens" meant everything should be in-memory, full stop. It broke login entirely: `signinRedirect()` does a real, full-page browser navigation to Keycloak and back, which wipes all JS memory in the tab. By the time the browser lands back on `/callback`, the module has reloaded from scratch and has no record of the PKCE verifier or state param it started with -- `signinCallback()` throws, and the app hangs on `/callback` forever with no visible error. Fix at the time: move just `stateStore` (a short-lived, single-use handshake value that only needs to survive one redirect round-trip) to `sessionStorage`, keep `userStore` (the actual tokens) on the in-memory store.

**Second bug: a real page reload doesn't survive on `userStore` either.** With the actual tokens in memory, refreshing the browser while logged in bounced straight back to `/` with no way back to a protected page -- indistinguishable from being logged out. The cause: `signinSilent()`'s refresh-token path (confirmed by reading its actual source, not assumed) starts by loading the *existing* stored user and checking for a `refresh_token` on it; if there isn't one already in storage, there's nothing to renew. A real reload wipes in-memory storage just as completely as the redirect round-trip did, so after a reload there was no refresh token anywhere to use -- `signinSilent()` had nothing to work with, every time.

That leaves three honest options: keep an iframe around purely for bootstrap recovery (reintroduces the Safari-ITP fragility this design was trying to avoid), accept that a reload always requires clicking "Log In" again (a fast round-trip since Keycloak's own session cookie is usually still valid, just not silent), or store the tokens somewhere that survives a reload. We went with the third: both `userStore` and `stateStore` on `sessionStorage`. The tokens are in browser storage while the tab is open now, not purely in-memory -- but tab-scoped and cleared on tab close, not `localStorage`, and the refresh token (the actually long-lived, sensitive one of the two) was always going to be the harder thing to protect either way. This isn't a compromise made lightly -- it's the standard trade-off most production SPAs land on, for exactly this reason.

Two settings still worth calling out:

- **There's no `useRefreshToken` setting.** It doesn't exist on `oidc-client-ts`'s `UserManagerSettings` -- `signinSilent()`'s own behavior is "via refresh token or an iframe," trying the refresh token first whenever one is available. Since `silent_redirect_uri` is never set here, there's no iframe fallback path at all -- renewal is refresh-token-only by construction, not by a flag.
- **`getUserManager()` is lazy and guarded.** TanStack Start renders both server and client from the same `router.tsx`, and `oidc-client-ts`'s `UserManager` needs `window`. Constructing it eagerly at module scope would crash the very first server render; constructing it lazily behind a client-only guard means the module can be imported anywhere without incident, and only actually builds a `UserManager` the first time client-side code calls it.

### Wire the Auth Provider and Router Context

`react-oidc-context`'s `AuthProvider` accepts an existing `UserManager` instance directly, rather than only building its own from settings -- that's the hook that lets the hand-constructed one above become the single source of truth everywhere, including outside React:

```tsx
// src/routes/__root.tsx (inside RootDocument)
const userManager = typeof window !== 'undefined' ? getUserManager() : undefined;

const onSigninCallback = (user: User | undefined) => {
  void router.navigate({ to: getRoleHomeRoute(getRoles(user)), replace: true });
};

return (
  // ...
  <AuthProvider userManager={userManager} onSigninCallback={onSigninCallback}>
    <Header />
    {children}
    <Footer />
  </AuthProvider>
  // ...
);
```

`AuthProvider` tolerates `userManager` being `undefined` -- it falls back to an inert stub rather than throwing, and its real initialization work (detecting a login callback, loading the current user) is deferred to a `useEffect`, which never runs during SSR anyway. That's what makes wrapping the whole app in `AuthProvider` safe even though the manager itself only ever really exists client-side.

`AuthProvider` also auto-detects when the current URL has `code`/`state` query params (i.e. Keycloak just redirected back after a login) and processes the callback itself -- there's no need to manually call `signinRedirectCallback()` anywhere. `onSigninCallback` is just the hook for what happens next: it reads the signed-in user's roles and navigates to whichever screen is theirs.

The router's own context gets a small `auth` accessor alongside the existing `queryClient`, so route guards (which run outside the React tree, in `beforeLoad`) can ask "who's logged in right now":

```typescript
// src/router.tsx
const router = createTanStackRouter({
  routeTree,
  context: {
    ...context,
    auth: {
      getUser: () =>
        typeof window !== 'undefined' ? getUserManager().getUser() : Promise.resolve(null),
    },
  },
  // ...
});

if (typeof window !== 'undefined') {
  const userManager = getUserManager();
  userManager.events.addUserLoaded(() => router.invalidate());
  userManager.events.addUserUnloaded(() => router.invalidate());
}
```

That last part matters: without it, a route guard only re-checks auth state on the *next* navigation. Subscribing to `UserManager`'s own load/unload events and calling `router.invalidate()` means a guard re-evaluates the instant a login or logout actually happens, not whenever the user next happens to click a link.

### Guard Routes by Role

Each of the three roles gets a pathless layout route -- a route file that contributes no URL segment of its own, just a shared `beforeLoad` for whatever's nested underneath it:

```tsx
// src/routes/_organizer.tsx
export const Route = createFileRoute('/_organizer')({
  ssr: false,
  beforeLoad: async ({ context }) => {
    const user = await context.auth.getUser();

    if (!user) {
      throw redirect({ to: '/' });
    }

    const roles = getRoles(user);
    if (!roles.includes(ROLE_ORGANIZER)) {
      throw redirect({ to: getRoleHomeRoute(roles) });
    }
  },
  component: () => <Outlet />,
});
```

`_attendee.tsx` and `_staff.tsx` are the same shape, checking their own role. `ssr: false` is what makes this safe to write without a manual `typeof window` check inside the guard itself -- TanStack Start's Selective SSR means `beforeLoad` simply never runs server-side for a route marked this way, so the client-only `getUserManager()` call inside it never executes anywhere that would crash.

A user with the wrong role for a given layout gets redirected to *their own* role's home (`getRoleHomeRoute`), not shown a Forbidden page -- organizer, attendee, and staff are effectively three different apps sharing one codebase, so bouncing someone back to the app that's actually theirs is the more honest response than an error screen.

The "no user" branch used to do more -- it called `signinRedirect()` directly, on the theory that a guard catching an unauthenticated visitor should immediately send them to log in rather than just showing a dead end. That caused a third live bug, and this one broke sign-out entirely rather than sign-in: clicking "Log Out" appeared to do nothing -- the browser never even reached Keycloak's logout page, and the user was back on `/dashboard`, still logged in, every time. The cause was a race this guard didn't know it was part of. `signoutRedirect()` clears the local user before it finishes navigating to Keycloak's logout endpoint; clearing the user fires the `addUserUnloaded` event, which -- per "Wire the Auth Provider and Router Context" above -- calls `router.invalidate()`. Since the browser hadn't actually left `/dashboard` yet at that instant, `router.invalidate()` re-ran this exact guard right then, mid-page. It saw "no user" and did exactly what it was told to do: called `signinRedirect()` immediately. That fresh login redirect won the race against sign-out's own pending navigation, so the browser went straight into a new login instead of ever reaching the logout page -- indistinguishable, from the user's side, from logout just not working.

The fix removes `signinRedirect()` from the guard entirely -- "no user" just redirects to `/` and stops, the same page `post_logout_redirect_uri` already sends you to. The existing "Log In" button there is what actually starts a session; the guard's only job is deciding whether the *current* page is allowed to render, not deciding to kick off an entirely separate auth flow as a side effect. That also closes off the whole category of race this bug came from -- nothing in a route guard triggers a browser-level redirect on its own anymore, so there's nothing left for a sign-out (or anything else that changes auth state reactively) to race against.

`getRoles(user)` had its own live bug worth recording. The obvious place to look for Keycloak's realm roles is `user.profile.realm_access.roles` -- `oidc-client-ts`'s `user.profile` is populated from the ID token's claims, and that's where a tutorial would point you. It came back empty for every user, every time, and everyone still ended up on `/` (`getRoleHomeRoute`'s fallback for "no roles matched") instead of their actual role's page. Checking the realm's "roles" client scope directly via Keycloak's admin API showed why: the realm-roles protocol mapper is configured with `access.token.claim: true` and nothing else -- no `id.token.claim`, no `userinfo.token.claim`. `realm_access.roles` only ever lands on the **access token** in this realm, never the ID token, so `user.profile` was never going to have it no matter what the frontend did. This is exactly how `ticket-service`'s own `JwtAuthenticationConverter` already reads roles too (see "Extract Roles" earlier) -- off the access token, not the ID token -- so the fix was to make the frontend consistent with the backend instead of consistent with a generic OIDC tutorial:

```typescript
// src/lib/oidc.ts
function decodeJwtPayload(token: string): Record<string, unknown> {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
  return JSON.parse(atob(padded));
}

export function getRoles(user: User | null | undefined): string[] {
  if (!user?.access_token) return [];
  try {
    const payload = decodeJwtPayload(user.access_token);
    const realmAccess = payload.realm_access as { roles?: string[] } | undefined;
    return realmAccess?.roles ?? [];
  } catch {
    return [];
  }
}
```

Decoding the access token's own payload directly sidesteps the ID-token-mapper question entirely -- frontend and backend now agree on where roles live because they're reading the identical claim off the identical token, not because both happen to be configured the same way today.

### Keep the App's Role Model Out of `oidc.ts`

`decodeJwtPayload()`, `getRoles()`, and the `ROLE_ORGANIZER`/`ROLE_ATTENDEE`/`ROLE_STAFF` constants all lived in `src/lib/oidc.ts` at first, right next to the `UserManager` -- convenient while it was the only file that touched tokens at all, but the file was quietly becoming two different things wearing one name. Only `getUserManager()` and `decodeJwtPayload()` are actually generic OIDC/JWT mechanics; "which roles exist" and "which route is home for which role" aren't OIDC concepts at all -- they're this app's own identity model, built on top of a generic JWT decode the same way any feature builds on `apiFetch()`. Once that was obvious, the split was mechanical:

```typescript
// src/lib/oidc.ts -- generic OIDC mechanics only, nothing app-specific
export function decodeJwtPayload(token: string): Record<string, unknown> {
  /* unchanged */
}
```

```typescript
// src/features/auth/types.ts -- this app's identity model: what roles exist, which URL each owns
export const ROLE_ORGANIZER = 'ROLE_ORGANIZER';
export const ROLE_ATTENDEE = 'ROLE_ATTENDEE';
export const ROLE_STAFF = 'ROLE_STAFF';

// Literal union, not a bare string -- TanStack Router's navigate()/redirect() are typed
// against known route paths, so this has to match one of them exactly to type-check.
export type RoleHomeRoute = '/dashboard' | '/browse' | '/scan' | '/';
```

```typescript
// src/features/auth/roles.ts -- the functions that connect the two
import type { User } from 'oidc-client-ts';
import { decodeJwtPayload } from '#/lib/oidc';
import { ROLE_ATTENDEE, ROLE_ORGANIZER, ROLE_STAFF, type RoleHomeRoute } from './types';

export function getRoles(user: User | null | undefined): string[] {
  /* same body as before, just moved */
}

export function getRoleHomeRoute(roles: string[]): RoleHomeRoute {
  if (roles.includes(ROLE_ORGANIZER)) return '/dashboard';
  if (roles.includes(ROLE_ATTENDEE)) return '/browse';
  if (roles.includes(ROLE_STAFF)) return '/scan';
  return '/';
}
```

Every call site -- `__root.tsx`'s `onSigninCallback`, and all three `_organizer.tsx`/`_attendee.tsx`/`_staff.tsx` guards -- now imports the functions from `#/features/auth/roles` and the constants/type from `#/features/auth/types`, instead of `#/lib/oidc`. `oidc.ts` is left holding exactly what its name promises: the `UserManager` and one generic token-decoding helper, nothing that knows this app has three roles.

Each layout currently has one placeholder screen underneath it -- `/dashboard` for organizers, `/browse` for attendees, `/scan` for staff -- just enough to confirm the guard actually works. The real screens for each of these are separate pieces of future work (see "Frontend Venue Management" below for the first one).

### Sign In, Sign Out, and Session Recovery

There's no custom sign-up or login page anywhere in this app. "Log In" is a plain button:

```tsx
<button onClick={() => auth.signinRedirect()}>Log In</button>
```

That's a full-page redirect to Keycloak's own hosted login screen -- the same one an admin sees logging into the Keycloak console, just themed for this realm. "Log Out" is the mirror image, `auth.signoutRedirect()`, which clears local state and round-trips through Keycloak's end-session endpoint back to the app.

A plain page reload survives on its own now, since `userStore` is `sessionStorage`-backed (see "Configure the OIDC Client" above for why that took two attempts to get right) -- the stored user, including its refresh token, is still there after a reload the moment the app re-reads it. One extra call at app bootstrap is still worth making on top of that, though:

```tsx
useEffect(() => {
  getUserManager()
    .signinSilent()
    .catch(() => {
      // No existing session to recover -- expected for a first visit or a logged-out user.
    });
}, []);
```

This isn't what makes a reload survive -- `sessionStorage` already does that structurally. What this adds is a proactive refresh: if the stored access token happens to be expired or close to it by the time the tab reopens, this renews it immediately using the stored refresh token, rather than waiting for the first real API call to hit a 401 and trigger `apiFetch()`'s own reactive retry. If Keycloak's own SSO session cookie is also still valid, this recovers cleanly with no redirect the user ever sees; if neither the stored refresh token nor the SSO cookie is still good, it just fails silently and the user looks logged out until they click "Log In" -- correct behavior either way, not an error.

### The Shared `apiFetch` Wrapper

Every feature's `api.ts` calls this instead of raw `fetch`, so the auth header and 401 handling live in exactly one place:

```typescript
// src/lib/api-client.ts
export async function apiFetch(input: string, init: RequestInit = {}): Promise<Response> {
  const userManager = getUserManager();
  const user = await userManager.getUser();

  const headers = new Headers(init.headers);
  if (user?.access_token) {
    headers.set('Authorization', `Bearer ${user.access_token}`);
  }
  headers.set('Accept-Language', 'en');

  const response = await fetch(input, { ...init, headers });
  if (response.status !== 401) return response;

  try {
    await userManager.signinSilent();
  } catch {
    await userManager.signinRedirect();
    return response;
  }

  const retryUser = await userManager.getUser();
  const retryHeaders = new Headers(init.headers);
  if (retryUser?.access_token) retryHeaders.set('Authorization', `Bearer ${retryUser.access_token}`);
  retryHeaders.set('Accept-Language', 'en');
  return fetch(input, { ...init, headers: retryHeaders });
}
```

A 401 gets one retry after a silent token refresh before giving up and sending the user back through `signinRedirect()` -- the same fallback the bootstrap recovery uses. `Accept-Language` is hardcoded to `en` for now; see "Frontend Internationalization" below for where that stops being hardcoded.

#### Summary

- Installed `react-oidc-context` + `oidc-client-ts`; the `UserManager` is hand-constructed in `src/lib/oidc.ts` rather than left to `react-oidc-context` to build implicitly, so route guards outside React can reach the same instance
- Both `userStore` and `stateStore` end up `sessionStorage`-backed, after two different in-memory attempts each broke on contact with a real browser round-trip: a shared in-memory store broke login (`signinRedirect()`'s full-page navigation wipes JS memory before the handshake state can be read back), and keeping just `userStore` in-memory afterwards broke reload (`signinSilent()`'s refresh-token path needs an existing refresh token already in storage, which a real reload wipes just as completely)
- `getRoles()` decodes the access token's own JWT payload rather than reading `user.profile.realm_access` -- this realm's role mapper only ever puts that claim on the access token, matching how `ticket-service`'s `JwtAuthenticationConverter` already reads the exact same claim off the exact same token
- Split the app's own role model out of `oidc.ts` afterward: `oidc.ts` now holds only `getUserManager()` and the generic `decodeJwtPayload()`; the `ROLE_*` constants and `RoleHomeRoute` type moved to `src/features/auth/types.ts`, and `getRoles()`/`getRoleHomeRoute()` moved to `src/features/auth/roles.ts` -- roles aren't an OIDC concept, they're this app's own identity model
- Refresh-token-only silent renewal, plus a `signinSilent()` bootstrap call as an extra proactive refresh on top of what `sessionStorage` already recovers on its own
- `AuthProvider` wraps the whole app and auto-detects the OIDC callback itself; `onSigninCallback` just decides where to send the user afterward
- The router's context carries an `auth.getUser()` accessor, and `UserManager`'s load/unload events call `router.invalidate()` so guards react the instant login/logout happens
- Three pathless layout routes (`_organizer`, `_attendee`, `_staff`), each `ssr: false`, each with one `beforeLoad` checking its own role and bouncing a mismatched user to their own home instead of a Forbidden page -- the "no user" branch redirects to `/` rather than calling `signinRedirect()` itself, after doing exactly that raced (and won against) sign-out's own navigation, making "Log Out" silently log the user right back in
- No custom sign-up/login pages -- both redirect straight to Keycloak's hosted screens
- Added `src/lib/api-client.ts`'s `apiFetch()` wrapper: attaches `Authorization`, handles a 401 with one silent-refresh retry before falling back to a full sign-in redirect
- Verified live so far: logging in as `skaran` (organizer) redirects to Keycloak, back through `/callback`, and lands on `/dashboard`. The sign-out fix and the reload fix are both in and match the diagnosed root cause in each case, but neither had been re-confirmed against a live click-through as of this writing -- along with the `attendee`/`staff` logins and the role-mismatch bounce, still open to independently confirm

## Frontend Venue Management

With auth and routing working, the first real screen (not a placeholder) is the organizer's Venues section -- per the resolved design in [Venue management screens design](https://github.com/SolonKarandanis/event-ticketing-platform/issues/7): a dedicated "Venues" area under the organizer dashboard (list, create, edit), not an inline quick-add inside the future event form. It was picked first, ahead of the event form itself, for an ordering reason that has nothing to do with the map: `ticket-service`'s `Event` entity has a required `venue` foreign key, so there's no way to build a working "Create Event" screen until there's at least one venue to attach it to.

### Install React Hook Form, Zod, and shadcn's Form Primitives

The map's locked stack always called for React Hook Form + Zod, but neither had actually been installed yet -- nothing needed them until now:

```bash
npm install react-hook-form zod @hookform/resolvers
```

Plain `npm install zod` here pulls zod's current major version, which broke the install: `@hookform/resolvers` optionally depends on `@typeschema/zod`, which peer-depends on `zod@^3.23.8` -- a version range the newer major doesn't satisfy. The fix was pinning zod to the 3.x line explicitly (`npm install react-hook-form "zod@^3.24.1" @hookform/resolvers`) rather than forcing the install with `--legacy-peer-deps`, since the actual problem was a real version mismatch, not npm being overly strict.

The `shadcn` add-on from "Scaffold the Project" earlier only ever set up `components.json` and the CSS variable theme -- it never actually generated any component files, since nothing had needed one yet either. First real use:

```bash
npx shadcn@latest add button input label textarea table form
```

This is a plain HTTP fetch against shadcn's registry, not a git clone -- unlike `shadcn init -t start`, which needed git's sparse-checkout support (see "Scaffold the Project"'s workaround), `add` has no such git-version dependency and worked without issue.

### Venue Types, API Client, and Query Hooks

`src/features/venues/types.ts` mirrors `VenueResponseDto`/`CreateVenueRequestDto`/`UpdateVenueRequestDto` field-for-field, same as every other feature's `types.ts`. `src/features/venues/api.ts` adds the four calls against `/api/v1/venues`:

```typescript
// src/features/venues/api.ts
const BASE_URL = `${import.meta.env.VITE_TICKET_SERVICE_URL}/api/v1/venues`;

export async function listVenues(): Promise<Venue[]> {
  const response = await apiFetch(`${BASE_URL}?size=100`);
  const page = await parseJsonOrThrow<{ content: Venue[] }>(response);
  return page.content;
}
// getVenue, createVenue, updateVenue follow the same apiFetch + parseJsonOrThrow shape
```

`listVenues()` reads `?size=100` and just returns `.content`, ignoring the rest of Spring Data's `Page` envelope -- issue #7's design has no pagination controls (a plain table), so a generous page size stands in for "list everything" rather than building pagination UI nothing asked for yet.

This is also the first feature to actually need error messages from the backend, which surfaced a gap: `apiFetch()` returns a raw `Response` and leaves error handling to the caller, but every feature calling it was going to need the exact same "parse the body, throw something useful" logic. Rather than write that once inside `venues/api.ts` and duplicate it at the next feature, it went into `api-client.ts` next to `apiFetch()` itself:

```typescript
// src/lib/api-client.ts
export class ApiError extends Error {
  constructor(public status: number, message: string) { super(message); }
}

export async function parseJsonOrThrow<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body: unknown = await response.json().catch(() => null);
    const message = body && typeof body === 'object' && 'error' in body && typeof body.error === 'string'
      ? body.error
      : `Request failed with status ${response.status}`;
    throw new ApiError(response.status, message);
  }
  return response.json() as Promise<T>;
}
```

This leans on something already true on the backend: `GlobalExceptionHandler` (see "Global Exception Handler" earlier) always responds with `{ error: string }` on 4xx/5xx, already localized server-side via whatever `Accept-Language` `apiFetch()` sent. `parseJsonOrThrow()` is the one place that shape gets parsed, so a validation failure like a blank venue name surfaces as a real, readable message in the form instead of a generic "request failed."

`src/features/venues/hooks.ts` wraps all four calls in named React Query hooks (`useVenues`, `useVenue`, `useCreateVenue`, `useUpdateVenue`), with both mutations invalidating the `['venues']` query key on success so the list picks up a create or edit immediately.

### The Venue Form

One `VenueForm` component (`src/features/venues/components/VenueForm.tsx`) serves both the create and edit routes, matching issue #7's single-page-all-fields design (Name, Address Line 1, Address Line 2, City, Postal Code, Country, Capacity, Latitude, Longitude, Accessibility Info). The numeric fields (`capacity`, `latitude`, `longitude`) are kept as plain strings in the form's Zod schema, not numbers:

```typescript
const venueFormSchema = z.object({
  // ...
  capacity: z.string().trim().refine((v) => v === '' || /^\d+$/.test(v), 'Must be a whole number'),
  latitude: z.string().trim().refine((v) => v === '' || /^-?\d+(\.\d+)?$/.test(v), 'Must be a number'),
  // ...
});
```

Native inputs always hand back strings, and all three of these fields are optional per issue #7 (no map picker, since PostGIS is out of scope for now -- see "Not yet specified" on the map). `z.coerce.number().optional()` looks like the obvious fit, but `Number('')` is `0`, not `NaN` -- an empty optional field would silently coerce to a real zero. Validating the string shape with a regex and converting to a number only when building the actual request (in `formValuesToRequest`, right before `createVenue`/`updateVenue`) sidesteps that entirely, at the cost of one small conversion function instead of fighting zod's coercion around an empty string.

### Routes and Reachability

Three routes under the organizer layout, all pulling from the shared `VenueForm`:

- `/venues` -- the list, a `shadcn` `Table` (Name, City, Capacity, Edit)
- `/venues/new` -- create
- `/venues/$venueId` -- edit, prefilling the form from `useVenue(venueId)`

None of this was reachable before now -- the three role layouts had no navigation between their own pages at all, since each only ever had one placeholder screen. `_organizer.tsx`'s layout component grew a small two-link nav (Dashboard, Venues) above its `<Outlet />` so the new section is actually clickable, not just reachable by typing the URL.

### A Missing CORS Configuration

Clicking through the Venues screens for the first time surfaced one more real bug, and it wasn't in the frontend at all: every request from the browser to `ticket-service` failed as a CORS error, `Access-Control-Allow-Origin` missing entirely. `ticket-service`'s `SecurityConfig` (see "Spring Security Configuration" earlier) had never configured CORS in any form, because nothing had ever needed it to -- every "Ui Testing" lesson throughout this document verifies its endpoint with `curl` or Postman, and CORS is a browser-enforced mechanism that simply doesn't apply to those clients. The Venues screen is the first real browser `fetch()` this API has ever received, so it's also the first time the gap was reachable at all.

The fix registers a `CorsConfigurationSource` and wires it into the filter chain ahead of the existing authorization rules:

```java
// src/main/java/com/etp/ticketservice/config/SecurityConfig.java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .authorizeHttpRequests(authorize -> /* unchanged */ )
    // ...

private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:3000"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept-Language"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

Registering a `CorsConfigurationSource` this way also gets the `OPTIONS` preflight handled for free: Spring Security auto-permits preflight requests once CORS is configured through `.cors(...)`, ahead of the `authorizeHttpRequests` rules -- there's no need for a separate `permitAll()` matcher just for `OPTIONS`, which would otherwise have needed its own carve-out since preflight requests never carry the `Authorization` header the rest of this config requires. The allowed origin is hardcoded to `http://localhost:3000` rather than pulled from a property, matching the map's "no deployment plan -- local dev only" scope; this is the only origin that exists right now.

This applies to `/**`, not just `/api/v1/venues/**` -- every future frontend feature calling `ticket-service` from the browser would have hit the exact same error on its first real request otherwise.

#### Summary

- Installed `react-hook-form`, `zod` (pinned to the 3.x line for `@hookform/resolvers` compatibility), and `@hookform/resolvers`
- Added the first real shadcn components (`button`, `input`, `label`, `textarea`, `table`, `form`) via `npx shadcn add` -- a plain registry fetch, unlike `init -t start`'s git-sparse-checkout requirement
- Built out `src/features/venues/` (`types.ts`, `api.ts`, `hooks.ts`, `components/VenueForm.tsx`) following the same feature-folder shape as every other entity
- Added `ApiError`/`parseJsonOrThrow` to the shared `api-client.ts` so every feature gets real, localized error messages from `GlobalExceptionHandler`'s `{ error: string }` responses, instead of each `api.ts` parsing that shape itself
- One `VenueForm` component serves both create and edit; numeric fields stay strings through validation and only convert to numbers when building the wire request, avoiding `zod`'s number-coercion behavior on empty optional inputs
- Three routes (`/venues`, `/venues/new`, `/venues/$venueId`) plus a small Dashboard/Venues nav in the organizer layout, since nothing before this was reachable except by typing a URL directly
- Venues was built before the event creation form (issue #6), even though the map lists the event form first, because `Event` has a required venue foreign key on the backend -- there was nothing for an event form's venue picker to pick from otherwise
- Found and fixed a real gap in `ticket-service`, not the frontend: `SecurityConfig` had no CORS configuration at all, since every prior "Ui Testing" lesson verified endpoints with `curl`/Postman rather than a browser -- added a `CorsConfigurationSource` for `http://localhost:3000` covering `/**`, which also fixes it for every future frontend feature, not just venues
- Verified via `tsc --noEmit`, `npm run build` (client + SSR), and `npm run lint`, all clean -- and confirmed live: create, list, and edit all work end-to-end against the running `ticket-service` in the browser

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

`ticket-service` resolves validation and error messages from the request's `Accept-Language` header. If the frontend switches language but keeps sending requests without that header, or with the browser's default language instead of whatever the user picked in the UI, the two will drift -- English form labels next to a Greek validation error, or vice versa. `apiFetch()` (see "Frontend Authentication" earlier) already sets this header on every request, just hardcoded to `en` for lack of anywhere else to read it from at the time. Now that i18next exists, that's a one-line change:

```typescript
// src/lib/api-client.ts
import i18next from '../i18n';

// ...
headers.set('Accept-Language', i18next.language); // was hardcoded to 'en'
```

Both places `apiFetch()` sets the header -- the initial request and the retry after a silent-refresh 401 -- need the same change, or the retry would still send the stale hardcoded value.

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
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin
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
        return new JacksonJsonMessageConverter();
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

`JacksonJsonMessageConverter`, not `Jackson2JsonMessageConverter` -- Spring AMQP 4.0 deprecated the latter (the "2" existed to distinguish it from an old Jackson 1.x-based converter that's long gone) in favor of the former, which is what actually ships on the classpath at this project's Spring Boot version. Most tutorials and Stack Overflow answers still show the deprecated name.

#### Two Kinds of Event, Not One

It's tempting to just inject `RabbitTemplate` straight into `TicketTypeServiceImpl` and call `convertAndSend(...)` at the end of `purchaseTicket`. The problem is timing: that call happens *inside* the same `@Transactional` method that does the purchase. If RabbitMQ is briefly unreachable when it runs, the exception rolls back the whole purchase -- a downstream messaging hiccup destroying a perfectly good ticket sale is the opposite of the decoupling we wanted from pulling analytics out into its own service in the first place.

Spring's own application event support fixes this cleanly, with no new infrastructure. We publish a plain in-process `ApplicationEvent` from inside `purchaseTicket`, and hand the actual RabbitMQ publish to a separate listener registered with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` -- which Spring only invokes once the surrounding transaction has actually committed. Two consequences fall out of that for free:

- If the purchase transaction rolls back (sold out, a constraint violation, anything), the listener never runs at all -- no message goes out for a purchase that didn't happen.
- If RabbitMQ is unreachable, the exception happens *after* the ticket is already safely committed to the database. The purchase succeeds either way; only the message delivery fails.

That second point is still not perfect delivery guarantee -- if the app crashes in the narrow window between the transaction committing and the listener's `convertAndSend` call, the message is lost with no retry. Closing that gap fully needs the transactional outbox pattern (write the event to a table in the same transaction as the ticket, have a separate poller publish it), which we're still not building. But the window we're now exposed to is much smaller than "any RabbitMQ hiccup can roll back a real purchase," which is what a direct `RabbitTemplate` call from inside the transaction would give us.

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

Every field here is a `domainId` or a plain value -- never an internal sequential `id`, and never a live entity reference. That matters even more now than it did for the message alone: the listener that eventually reads this record runs *after* the transaction (and its Hibernate session) has already closed, so if this carried the `Ticket` entity itself instead of plain values, touching any of its lazy associations from the listener would throw `LazyInitializationException`. Extracting everything into flat fields while the entity is still attached -- inside `TicketEventPublisher`, below -- sidesteps that entirely; there's nothing left in the record that could ever need a session.

This same record does double duty as both the in-process Spring event and the RabbitMQ message body -- there's no separate mapping step, since the shape a downstream consumer needs (`ticket.purchased`'s payload) and the shape our own listener needs are identical.

```java
@Service
@RequiredArgsConstructor
public class TicketEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishTicketPurchased(Ticket ticket) {
        applicationEventPublisher.publishEvent(new TicketPurchasedEvent(
                ticket.getDomainId(),
                ticket.getTicketType().getDomainId(),
                ticket.getTicketType().getEvent().getDomainId(),
                ticket.getPurchaser().getDomainId(),
                ticket.getTicketType().getPrice(),
                ticket.getCreatedAt()
        ));
    }
}
```

This is `TicketTypeServiceImpl`'s only touchpoint with any of this -- it still just calls `ticketEventPublisher.publishTicketPurchased(savedTicket)`, exactly as before. `TicketEventPublisher` no longer knows RabbitMQ exists at all; it hands a plain Java object to Spring's `ApplicationEventPublisher` and is done.

#### Listen for the Event and Publish to RabbitMQ

```java
@Component
@RequiredArgsConstructor
public class RabbitMqTicketEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketPurchased(TicketPurchasedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EVENTS_EXCHANGE, "ticket.purchased", event);
    }
}
```

`@TransactionalEventListener` only fires this way when the event is published from inside an active transaction -- by default, if `publishEvent(...)` is ever called with no transaction in progress, the event is silently dropped rather than delivered synchronously. That's not a concern here, since `TicketEventPublisher.publishTicketPurchased` is always called from inside `purchaseTicket`'s `@Transactional` boundary, but it's worth knowing if this pattern gets reused somewhere that isn't already transactional.

#### Hook It Into the Purchase Flow

`purchaseTicket` itself doesn't change from how it already looked:

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

That's the point of routing this through `TicketEventPublisher` in the first place -- the purchase flow's only obligation is to say "a ticket was purchased," not to know or care that a `TransactionPhase.AFTER_COMMIT` listener and a topic exchange are involved downstream.

#### Summary

- Added `spring-boot-starter-amqp` and configured the RabbitMQ connection
- Declared a `ticket-platform.events` topic exchange, with no queue of its own
- Split ticket-purchase eventing into two stages: `TicketEventPublisher` publishes an in-process `TicketPurchasedEvent` from inside the purchase transaction; `RabbitMqTicketEventListener` forwards it to RabbitMQ via `@TransactionalEventListener(phase = AFTER_COMMIT)`, only after that transaction commits
- This means a RabbitMQ outage can no longer roll back a successful ticket purchase -- only the message delivery is at risk, not the sale itself
- A transactional outbox is still the fully rigorous fix for the narrow crash-between-commit-and-publish window; still not building it yet

### Scaffold the NestJS Project

```bash
nest new analytics-service --strict
cd analytics-service
npm install drizzle-orm postgres
npm install -D drizzle-kit
npm install @nestjs/config
npm install amqp-connection-manager amqplib
npm install @nestjs/passport passport passport-jwt jwks-rsa
npm install -D @types/passport-jwt
```

`--strict` enables TypeScript's strict mode, matching the level of rigor we've held `ticket-service` to throughout -- it has one small consequence worth knowing about upfront: `process.env.SOMETHING` is typed as `string | undefined`, not `string`, so anywhere we pass an env var straight into something expecting a `string` (the Drizzle connection factory, below), it needs an explicit `as string`.

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

Everything database- and schema-related lives under its own `src/db/` folder, rather than scattered at the top of `src/` -- `drizzle.provider.ts` and `schema.ts` (next lesson) both go there.

Unlike TypeORM, there's no module to register -- Drizzle's client is just a value, provided to Nest's DI container like any other:

```typescript
// src/db/drizzle.provider.ts
import postgres from 'postgres';
import { drizzle } from 'drizzle-orm/postgres-js';
import * as schema from './schema';

export const DRIZZLE = Symbol('DRIZZLE');

export const drizzleProvider = {
  provide: DRIZZLE,
  useFactory: () => drizzle(postgres(process.env.ANALYTICS_DATABASE_URL as string), { schema }),
};
```

That `as string` is the strict-mode consequence flagged above -- `process.env.ANALYTICS_DATABASE_URL` is `string | undefined` as far as the compiler knows, even though we know it'll be set at runtime.

For it to actually *be* set at runtime, something needs to load `.env` into `process.env` -- that's what the `@nestjs/config` dependency installed earlier is for. Register `ConfigModule.forRoot()` alongside the Drizzle provider in `AppModule`:

```typescript
// src/app.module.ts
import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { drizzleProvider } from './db/drizzle.provider';

@Module({
  imports: [ConfigModule.forRoot()],
  controllers: [AppController],
  providers: [AppService, drizzleProvider],
})
export class AppModule {}
```

`ConfigModule.forRoot()` with no arguments reads `.env` from the project root by default -- put `ANALYTICS_DATABASE_URL` (and `RABBITMQ_URL`, needed a couple of lessons from now) there. With the provider registered, any service can inject the Drizzle client with `@Inject(DRIZZLE)`.

#### Summary

- Scaffolded a new NestJS project, `analytics-service`, with TypeScript strict mode enabled
- Added Drizzle, RabbitMQ client, and JWT auth dependencies
- Added a dedicated `analytics-db` Postgres container, isolated from `ticket-service`'s database
- Registered `ConfigModule.forRoot()` so `.env` actually populates `process.env`, and registered the Drizzle client as a plain DI provider, `DRIZZLE`, both in `AppModule`

### Consume Ticket Purchase Events

#### The Sales Fact Table

```typescript
// src/db/schema.ts
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

Both new files live under `src/ticket-sales/`, alongside each other -- everything about recording and reporting on sales, kept separate from `src/db/`'s connection/schema concerns and `src/rabbitmq/`'s transport concerns.

```typescript
// src/ticket-sales/ticket-purchased.event.ts
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
// src/ticket-sales/ticket-sales.service.ts
import { Injectable, Inject } from '@nestjs/common';
import { eq, sql } from 'drizzle-orm';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { DRIZZLE } from '../db/drizzle.provider';
import { ticketSales } from '../db/schema';
import { TicketPurchasedEvent } from './ticket-purchased.event';
import * as schema from '../db/schema';

@Injectable()
export class TicketSalesService {
  constructor(
    @Inject(DRIZZLE) private readonly db: PostgresJsDatabase<typeof schema>,
  ) {}

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
  schema: './src/db/schema.ts',
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
// src/main.ts
import { NestFactory } from '@nestjs/core';
import { migrate } from 'drizzle-orm/postgres-js/migrator';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { AppModule } from './app.module';
import { DRIZZLE } from './db/drizzle.provider';
import * as schema from './db/schema';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const db = app.get<PostgresJsDatabase<typeof schema>>(DRIZZLE);

  await migrate(db, { migrationsFolder: './drizzle' });

  await app.listen(process.env.PORT ?? 3001);
}
void bootstrap();
```

Two strict-mode details: `app.get(DRIZZLE)` alone would come back typed `any`, since `DRIZZLE` is a `Symbol` token rather than a class Nest can infer a return type from -- the explicit `app.get<PostgresJsDatabase<typeof schema>>(...)` generic fixes that. And `void bootstrap()` (not just `bootstrap()`) explicitly marks the top-level floating promise as intentionally unawaited, satisfying `@typescript-eslint/no-floating-promises` -- this is what `nest new`'s own generated `main.ts` already does, and it's easy to lose when rewriting the file by hand.

#### Wire Up the Consumer

This one lives under `src/rabbitmq/` -- it's the only file in the project that knows `amqp-connection-manager` exists.

```typescript
// src/rabbitmq/rabbitmq-consumer.service.ts
import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import * as amqp from 'amqp-connection-manager';
import { ConfirmChannel, ConsumeMessage } from 'amqplib';
import { TicketSalesService } from '../ticket-sales/ticket-sales.service';
import { TicketPurchasedEvent } from '../ticket-sales/ticket-purchased.event';

const EVENTS_EXCHANGE = 'ticket-platform.events';
const QUEUE_NAME = 'analytics-service.ticket-events';

@Injectable()
export class RabbitMqConsumerService implements OnModuleInit {
  private readonly logger = new Logger(RabbitMqConsumerService.name);

  constructor(private readonly ticketSalesService: TicketSalesService) {}

  onModuleInit() {
    const connection = amqp.connect([
      process.env.RABBITMQ_URL ?? 'amqp://admin:admin@localhost:5672',
    ]);

    connection.createChannel({
      setup: async (channel: ConfirmChannel) => {
        await channel.assertExchange(EVENTS_EXCHANGE, 'topic', { durable: true });
        await channel.assertQueue(QUEUE_NAME, { durable: true });
        await channel.bindQueue(QUEUE_NAME, EVENTS_EXCHANGE, 'ticket.purchased');

        await channel.consume(
          QUEUE_NAME,
          (message: ConsumeMessage | null) =>
            void this.handleMessage(channel, message),
        );
      },
    });
  }

  private async handleMessage(channel: ConfirmChannel, message: ConsumeMessage | null) {
    if (!message) {
      return;
    }

    try {
      const event = JSON.parse(
        message.content.toString(),
      ) as TicketPurchasedEvent;
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

Two strict-lint details worth knowing about, both flagged by `@typescript-eslint`'s type-checked rules once this is wired into `AppModule`: `channel.consume`'s callback is expected to return `void`, but `handleMessage` is `async` (so calling it returns a `Promise`) -- wrapping the call as `void this.handleMessage(...)` explicitly marks that promise as intentionally un-awaited (`@typescript-eslint/no-misused-promises`), which is safe here since `handleMessage` already catches its own errors internally and never lets a rejection escape. And `JSON.parse(...)` returns `any` by design -- it can't know the shape of what it parsed -- so `as TicketPurchasedEvent` makes that trust explicit (`@typescript-eslint/no-unsafe-assignment`) rather than letting an untyped value flow silently into `recordSale`.

#### Summary

- Defined the `ticketSales` table as a Drizzle schema, with a unique `ticketId` for idempotency
- Implemented `TicketSalesService.recordSale` as an atomic `ON CONFLICT DO NOTHING` upsert, and `getSummaryForEvent` with a fully-typed result
- Set up Drizzle Kit migrations, applied automatically on startup
- Implemented `RabbitMqConsumerService`, which declares and binds its own queue to `ticket-service`'s exchange
- Failed messages are dropped rather than looped forever, with a noted follow-up (dead-lettering) we're not building yet
- `src/ticket-sales/` and `src/rabbitmq/` join `src/db/` as the project's feature folders -- nothing lives loose at the top of `src/` beyond `app.module.ts` and `main.ts`

### Expose the Reporting API

`analytics-service` doesn't get its own identity system -- it validates the same Keycloak-issued JWTs `ticket-service` does, against the same realm's JWKS endpoint. That JWKS URI goes in `.env` as `KEYCLOAK_JWKS_URI`, alongside `ANALYTICS_DATABASE_URL` and `RABBITMQ_URL` -- nothing in this project hardcodes another service's location.

```typescript
// src/auth/keycloak-jwt.strategy.ts
import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { Strategy, ExtractJwt } from 'passport-jwt';
import * as jwksRsa from 'jwks-rsa';

interface KeycloakJwtPayload {
  sub: string;
  realm_access?: { roles: string[] };
}

@Injectable()
export class KeycloakJwtStrategy extends PassportStrategy(Strategy) {
  constructor() {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      secretOrKeyProvider: jwksRsa.passportJwtSecret({
        jwksUri: process.env.KEYCLOAK_JWKS_URI as string,
      }),
      algorithms: ['RS256'],
    });
  }

  validate(payload: KeycloakJwtPayload) {
    const roles: string[] = payload.realm_access?.roles ?? [];
    return { userId: payload.sub, roles };
  }
}
```

The decoded JWT payload gets a small local `KeycloakJwtPayload` interface rather than `any` -- `passport-jwt`'s own types leave it untyped since the shape is entirely up to whoever issued the token, but we know what Keycloak puts in it, so there's no reason to give up type safety here.

With that in place, a summary endpoint is a thin controller on top of the service we already wrote:

```typescript
// src/ticket-sales/event-analytics.controller.ts
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

#### Wire Everything Into AppModule

By this point `AppModule` has grown to register every provider and controller introduced across this section -- the default `AppController`/`AppService` from the initial scaffold were never doing anything for this service and can go entirely:

```typescript
// src/app.module.ts
import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PassportModule } from '@nestjs/passport';
import { drizzleProvider } from './db/drizzle.provider';
import { TicketSalesService } from './ticket-sales/ticket-sales.service';
import { EventAnalyticsController } from './ticket-sales/event-analytics.controller';
import { RabbitMqConsumerService } from './rabbitmq/rabbitmq-consumer.service';
import { KeycloakJwtStrategy } from './auth/keycloak-jwt.strategy';

@Module({
  imports: [ConfigModule.forRoot(), PassportModule],
  controllers: [EventAnalyticsController],
  providers: [
    drizzleProvider,
    TicketSalesService,
    RabbitMqConsumerService,
    KeycloakJwtStrategy,
  ],
})
export class AppModule {}
```

`PassportModule` needs to be imported for `AuthGuard('jwt')` to resolve the `'jwt'` strategy by name; `KeycloakJwtStrategy` registers itself under that name simply by being instantiated (that's what extending `PassportStrategy(Strategy)` does internally), so there's no separate step to tell Nest which class backs which guard string.

A quick way to confirm the guard is actually doing something, before wiring up a real Keycloak login: hit the endpoint with no `Authorization` header, and with a bearer token that isn't a real JWT. Both should come back `401` -- the second case matters more than it looks, since it confirms invalid tokens are rejected outright rather than silently falling through unauthenticated.

#### Summary

- Added a `KeycloakJwtStrategy` validating against the same realm as `ticket-service`, no separate identity system -- reads its JWKS URI from `.env`, and types the decoded payload instead of using `any`
- Exposed `GET /analytics/events/:eventId/summary` as the first reporting endpoint
- Registered `PassportModule`, the strategy, and the controller in `AppModule`, and removed the unused default `AppController`/`AppService`
- `analytics-service` now has no direct dependency on `ticket-service` in either direction -- RabbitMQ in, a read API out

## Frontend Event Management

With Venues built, the event creation/edit form (issue #6) is the natural next screen -- `Event` has a required venue foreign key, so this always had to come second. It's a bigger build than Venues in every dimension: more fields, a nested array of ticket types instead of flat fields, a real lifecycle (`DRAFT -> PUBLISHED -> {CANCELLED|COMPLETED}`) instead of plain CRUD, and two genuinely different submit flows (create vs. edit) instead of one shared one.

### Event Types, API, and Hooks

`features/events/types.ts` mirrors every DTO shape the backend actually has, not one collapsed shape -- `CreateEventResponse`, `UpdateEventResponse`, `GetEventDetailsResponse`, and `ListEventResponse` each get their own interface (and their own nested ticket-type interface), the same "no MapStruct-style collapsing" discipline the backend itself follows. `GetEventDetailsTicketTypesResponse` is the one that carries `ticketsSold`, since that field only exists on the get-details endpoint -- a newly-created ticket type can't have sales against it yet, so `CreateTicketTypeResponse` never needed the field in the first place.

One bug worth calling out from `api.ts`: `deleteEvent`/`publishEvent`/`cancelEvent`/`completeEvent` all hit endpoints that return `204 No Content`, and the original code ran them through the same `parseJsonOrThrow<void>` every other call used. That's wrong in a way that only shows up on the success path: `response.json()` on an empty body throws `SyntaxError: Unexpected end of JSON input`, so every one of those calls would have failed *after* the backend had already done what was asked. Fixed by adding a dedicated helper to `api-client.ts`:

```typescript
// For endpoints that succeed with 204 No Content -- same failure handling as
// parseJsonOrThrow, but never calls response.json() on an empty success body.
export async function throwIfNotOk(response: Response): Promise<void> {
  if (!response.ok) {
    return throwApiError(response)
  }
}
```

`events/hooks.ts` follows the same named-hooks-with-toasts shape venues established (`useEvents`, `useEvent`, `useCreateEvent`, `useUpdateEvent`, `useDeleteEvent`, `usePublishEvent`, `useCancelEvent`, `useCompleteEvent`), each invalidating the `['events']` query key on success.

### The Event Form's Validation Schema

`EventForm`'s Zod schema mirrors the backend's validation exactly, not approximately -- including the cross-field date rules, checked directly against `EventServiceImpl.validateEventDates` rather than assumed:

```typescript
.superRefine((event, ctx) => {
  const { start, end, salesStart, salesEnd } = event

  if (start && end && end <= start) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['end'], message: 'End must be after start' })
  }
  if (start && salesEnd && salesEnd > start) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['salesEnd'], message: 'Sales must end by the event start' })
  }
  if (salesStart && salesEnd && salesEnd <= salesStart) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['salesEnd'], message: 'Sales end must be after sales start' })
  }
})
```

Getting the comparisons strict-vs-non-strict right mattered: the backend rejects `end <= start` but only rejects `salesEnd` when it's *after* `start` (equal is fine), so a looser client-side check would either block something the backend allows or let through something it rejects.

Ticket type price needed a validation helper that didn't already exist. `#/lib/validation.ts` already had `isIntegerOrEmpty`/`isDecimalOrEmpty` from Venues, both of which treat a blank value as valid -- correct for optional fields like `capacity`/`latitude`, wrong for `price`, which is `@NotNull` on the backend. Added `isNonNegativeDecimal` (required, no leading minus sign) alongside them rather than stretching the "OrEmpty" helpers to cover a case they were never meant for.

`totalAvailable` isn't simply optional -- it's conditionally required, driven by a form-only field with no backend equivalent:

```typescript
const ticketTypeFormSchema = z
  .object({
    id: z.string().optional(),
    name: z.string().trim().min(1, 'Name is required'),
    price: z.string().trim().refine(isNonNegativeDecimal, 'Must be a non-negative number'),
    description: z.string().trim(),
    limitedQuantity: z.boolean(),
    totalAvailable: z.string().trim(),
  })
  .superRefine((ticketType, ctx) => {
    if (ticketType.limitedQuantity && ticketType.totalAvailable === '') {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['totalAvailable'], message: 'Required when quantity is limited' })
      return
    }
    if (!isIntegerOrEmpty(ticketType.totalAvailable)) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['totalAvailable'], message: 'Must be a whole number' })
    }
  })
```

`limitedQuantity` is the "Limited quantity" toggle from issue #6's design -- it exists purely to decide, at submit time, whether `totalAvailable` gets sent to the backend at all (`totalAvailable: undefined` when off, meaning unlimited). `eventToFormValues` reconstructs the toggle's state the other direction when loading an existing event: `limitedQuantity: ticketType.totalAvailable !== null`.

### The Ticket-Type Field Array

`ticketTypes` is a real `useFieldArray`, and its per-row UI is its own component, `TicketTypeRow`, not inlined in `EventForm`'s `.map()`. The reason is a subtle React Query/react-hook-form performance trap: `limitedQuantity` needs to be watched to conditionally show `totalAvailable`, and calling `form.watch()` directly inside a loop inside `EventForm` would re-render the *entire* form -- every row, every field -- on every keystroke in *any* row. `TicketTypeRow` uses `useWatch({ control, name: 'ticketTypes.${index}.limitedQuantity' })` scoped to just its own index instead, so only the row whose toggle actually changed re-renders.

`emptyTicketType`/`emptyValues` seed the create form (and the "+ Add Ticket Type" button) with one blank row rather than an empty array -- `ticketTypes` has a `.min(1)` constraint matching the backend's `@NotEmpty`, so "no ticket types yet" has to look like one blank row, not zero rows. The "Remove" button on each row is disabled once it's the last one left, for the same reason.

### Multiple Submit Actions: Draft, Publish, and Save Changes

`VenueForm` only ever needed one submit button. `EventForm` needs a genuinely variable number: one ("Save Changes") on the edit page, two ("Save as Draft" / "Publish") on the create page. That reshaped `EventForm`'s props from `VenueForm`'s `{ onSubmit, isSubmitting, submitLabel }` into an array:

```typescript
export interface EventFormAction {
  label: string
  onSubmit: (values: EventFormValues) => void
  isSubmitting: boolean
  variant?: 'default' | 'outline'
}
```

The first action renders as the form's native `type="submit"` button (so pressing Enter does the sensible default thing); any further actions are `type="button"`, each triggering `form.handleSubmit(action.onSubmit)` directly so every action gets independently validated against the same form state. All actions disable together while any one is submitting.

An empty `actions` array means something specific: read-only. Every field gets `disabled={readOnly}` threaded down to it (including into `TicketTypeRow` and `VenueCombobox`, both of which needed a new `disabled` prop for this), the "+ Add Ticket Type" button and each row's "Remove" button disappear, and no submit buttons render at all. This is what powers the terminal-status edit page below -- not a special case bolted on, just the natural zero-actions state of the same component.

Extracting `primaryAction` from `actions` surfaced a real type-safety gap, caught by ESLint rather than assumed: `const [primaryAction] = actions` types `primaryAction` as always-defined, because TypeScript doesn't add `| undefined` to a destructured array element unless `noUncheckedIndexedAccess` is on -- even though `actions` can genuinely be empty. Switched to `actions.at(0)`, whose type signature is `T | undefined` regardless of that flag, so the "is there actually a primary action" check the code needs is one the type checker can actually verify instead of silently trusting.

On the create page, "Save as Draft" and "Publish" turned out to need two independent `useCreateEvent()` instances, not one shared between both buttons -- calling the same hook twice gives two fully separate `isPending` states, so clicking Publish doesn't make the Draft button's label flicker to "Saving..." too. Publish itself is create-then-publish, confirmed against `EventServiceImpl.createEvent` directly: it always creates in `DRAFT` status, there's no create-as-published path. The existing `usePublishEvent(eventId)` hook doesn't fit this flow either -- it takes `eventId` as a hook argument, which has to be known before the hook runs, and a not-yet-created event has no id yet. The create page calls the raw `publishEvent` API function directly instead, inside `onSuccess` of the create call, with the id the create response just returned.

### The Venue Picker: From a Capped Dropdown to a Searchable Combobox

The first version of the venue field was a plain `<Select>` backed by `useVenues({ page: 0, size: 100 })` -- the same "a big page stands in for everything" trick Venues itself used before it had real pagination. The problem is the same one that trick always has: past 100 venues, anything further is silently unreachable, with no error and no indication anything's missing.

The fix, `VenueCombobox`, is a `Popover` + `Command` combobox: a debounced (300ms) search box driving `useInfiniteQuery`, with an `onScroll` handler on the results list that calls `fetchNextPage()` once the user scrolls near the bottom.

```typescript
export function useInfiniteVenues(searchTerm: string) {
  return useInfiniteQuery({
    queryKey: [...venuesKey, 'search', searchTerm],
    queryFn: ({ pageParam }) =>
      listVenues({ page: pageParam, size: 20, q: searchTerm || undefined }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.number + 1 < lastPage.totalPages ? lastPage.number + 1 : undefined,
  })
}
```

`Command`'s own client-side filtering is turned off (`shouldFilter={false}`) -- filtering here is the server's job, driven by the debounced term in the query key, not `cmdk`'s built-in string matching.

This needed a real backend search endpoint that didn't exist: `VenueRepository` only had `findAll`/`findByDomainId` before. Added:

```java
@Query("SELECT v FROM Venue v WHERE :searchTerm IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
Page<Venue> search(@Param("searchTerm") String searchTerm, Pageable pageable);
```

This immediately hit a PostgreSQL failure that's worth understanding, since it recurred later: `function lower(bytea) does not exist`. `:searchTerm` appears twice in the query, and its *only* type context anywhere is `:searchTerm IS NULL` -- which is valid for any type, so it gives Postgres nothing to infer from. Left unconstrained, the `||`/`CONCAT` operator resolution picked a `bytea` overload for the untyped parameter instead of `text`, and `LOWER(bytea)` then failed outright. The fix is an explicit cast on every occurrence:

```java
@Query("SELECT v FROM Venue v WHERE CAST(:searchTerm AS string) IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%'))")
Page<Venue> search(@Param("searchTerm") String searchTerm, Pageable pageable);
```

### Routes: Create (Draft/Publish) and Edit (Status-Conditional Footer)

The edit page's footer is genuinely status-conditional, checked against every lifecycle method in `EventServiceImpl` directly rather than guessed at:

- **`DRAFT`** -- the form is editable (`updateEventForOrganizer` allows updates here), plus standalone `Publish` and `Delete` buttons.
- **`PUBLISHED`** -- still editable (the backend allows updates on published events too, confirmed by reading the code, not assumed), plus standalone `Complete` and `Cancel Event` buttons.
- **`CANCELLED`/`COMPLETED`** -- fully read-only (`actions={[]}`), since `updateEventForOrganizer` rejects any update once an event is terminal.

`Publish`/`Cancel`/`Complete`/`Delete` are deliberately independent of the form's own save action -- none of those endpoints take a request body, so they act on whatever's already persisted, not on unsaved edits sitting in the form. `Delete` and `Cancel Event` go through a small reusable `ConfirmButton` (an `AlertDialog` wrapper) first, since both are effectively one-way doors with real consequences; `Publish`/`Complete` are plain buttons, since they're the expected forward path.

One gap worth flagging rather than silently working around: `deleteEventForOrganizer` has no status guard at all on the backend -- it'll delete a `PUBLISHED` event with real ticket sales exactly as readily as a `DRAFT` one. The frontend keeps `Delete` Draft-only per the decided design, but that's a UI-level restriction sitting on top of a backend that doesn't actually enforce it.

`EventStatus` is a `const` object with a matching type of the same name (`EventStatus.DRAFT`, `EventStatus.PUBLISHED`, ...) rather than a bare string union or a real TypeScript `enum` -- named references instead of magic strings scattered through the status checks, without introducing `enum` into a codebase that doesn't use it anywhere else.

### Reusing `PaginatedTable` for the Events List

`events/index.tsx`'s first draft (hand-written before `PaginatedTable` existed) had its empty-state condition inverted -- the table only rendered when `content.length === 0`, backwards from what was intended, so it showed nothing once there actually were events. Rebuilding it on `PaginatedTable` (same component Venues uses, extracted specifically because this exact class of copy-paste drift kept happening between the two lists) fixed it structurally: that conditional logic doesn't exist in the route anymore to get out of sync in the first place.

#### Summary

- `events/types.ts`/`api.ts`/`hooks.ts` mirror every backend DTO/endpoint individually; fixed a live bug where 204-returning calls (`delete`/`publish`/`cancel`/`complete`) would throw on success, via a new `throwIfNotOk` helper
- `EventForm`'s Zod schema mirrors `EventServiceImpl.validateEventDates` exactly, including strict-vs-non-strict comparisons; added `isNonNegativeDecimal` to `#/lib/validation.ts` for the required (not optional) price field
- Ticket types are a `useFieldArray`; `TicketTypeRow` is its own component so the "Limited quantity" toggle's `useWatch` only re-renders its own row
- `EventForm` takes an `actions: EventFormAction[]` array instead of one `onSubmit` -- supports the create page's two independent actions and doubles as the read-only mode (`actions={[]}`) for terminal events
- `VenueCombobox` replaced a capped-at-100 `<Select>` with search + infinite scroll; needed a new `VenueRepository.search` endpoint, which hit (and fixed) a Postgres parameter-type-inference bug on its first try
- The edit page's Publish/Cancel/Complete/Delete buttons are independent of the form's save, checked directly against `EventServiceImpl`'s real transition rules; `Delete`/`Cancel Event` get a confirmation step, `Publish`/`Complete` don't
- Rebuilding `events/index.tsx` on `PaginatedTable` fixed an inverted empty-state condition that would have shown an empty table whenever events actually existed

## Frontend Published Events Browse & Search

Issue #10's design (search box + filter row above a text-only event-card grid, numbered pagination, two distinct empty states) needed its own feature folder, `features/published-events/`, kept separate from `features/events/` on purpose -- it's a different backend resource (`PublishedEventController`, not `EventController`), different DTOs, and no auth required, the same reasoning that already justified `ticket-types` having its own folder apart from `events`.

### Published Events Types, API, and Hooks

`types.ts` mirrors `ListPublishedEventResponseDto`/`GetPublishedEventDetailsResponseDto` (read from the Java source directly, not assumed from memory), plus `PublishedEventsSort = 'soonest' | 'priceAsc' | 'priceDesc'`, matching `EventServiceImpl.findPublishedEvents`'s sort switch exactly. `api.ts`'s `ListPublishedEventsParams` covers all seven backend filter params (`q`, `from`, `to`, `minPrice`, `maxPrice`, `city`, `sortBy`) plus pagination.

Building `buildQuery` for this surfaced a TypeScript gotcha worth knowing generally: a plain `interface` without its own index signature isn't assignable to a `Record<string, V>` parameter type, even when every one of its properties would fit `V`. The fix was typing the helper directly against `ListPublishedEventsParams` rather than reaching for a generic `Record` signature.

### Fixing PostgreSQL's Bind-Parameter Inference Again

The published-events native query (`EventRepository.PUBLISHED_EVENTS_WHERE`, shared by all three sort variants plus the count query) hit the exact same class of bug the venue search fix had already found, just failing louder: `ERROR: could not determine data type of parameter $5`. `:from`'s only appearance with any type context at all is `:from IS NULL`, same root cause as before, just a native query this time instead of JPQL -- and apparently enough different context elsewhere in the statement that Postgres refused outright rather than silently guessing wrong. The fix is the same shape, applied to every optional parameter in the shared `WHERE` clause -- `text` for `searchTerm`/`city`, `timestamp` for `from`/`to`, `double precision` for `minPrice`/`maxPrice`:

```java
String PUBLISHED_EVENTS_WHERE = "e.status = 'PUBLISHED' " +
        "AND (CAST(:searchTerm AS text) IS NULL OR to_tsvector(...) @@ plainto_tsquery('english', CAST(:searchTerm AS text))) " +
        "AND (CAST(:city AS text) IS NULL OR v.city = CAST(:city AS text)) " +
        "AND (CAST(:from AS timestamp) IS NULL OR e.event_start >= CAST(:from AS timestamp)) " +
        "AND (CAST(:to AS timestamp) IS NULL OR e.event_start <= CAST(:to AS timestamp)) " +
        "AND (CAST(:minPrice AS double precision) IS NULL OR (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) >= CAST(:minPrice AS double precision)) " +
        "AND (CAST(:maxPrice AS double precision) IS NULL OR (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) <= CAST(:maxPrice AS double precision))";
```

Worth remembering as a standing pattern for this stack: any future query using the `(:param IS NULL OR ...)` optional-filter idiom needs the same treatment the moment `:param` doesn't already have unambiguous type context from somewhere else in the same query.

### A Public City Filter, Not the Organizer Venues Endpoint

The City filter's first version reused `useVenues` to populate its options, the same hook the organizer Venues screens use. That's a real bug on a public page: `GET /api/v1/venues` requires `ROLE_ORGANIZER`, so for anyone not logged in as an organizer -- which is everyone on a page that's supposed to need no login at all -- it returns `401`. `apiFetch`'s automatic 401-handling then tries a silent token refresh, which fails since there's no session to refresh, and falls back to a full `signinRedirect()`. The net effect: loading a page that was explicitly built to not require login redirected straight to the Keycloak login screen, because of an unrelated background request for filter options.

The fix is a genuinely public endpoint, and a more correct one than the workaround it replaced -- it only returns cities that actually have a published event, not every venue an organizer has ever created (some of which may have zero published events, which would have been a misleading filter option regardless of the auth bug):

```java
@Query("SELECT DISTINCT e.venue.city FROM Event e WHERE e.status = 'PUBLISHED' ORDER BY e.venue.city")
List<String> findDistinctPublishedEventCities();
```

Exposed as `GET /api/v1/published-events/cities` on `PublishedEventController` -- it falls under the controller's existing `GET /api/v1/published-events/**` -> `permitAll()` wildcard, so no `SecurityConfig` change was needed. Adding it as a new `@GetMapping(path = "/cities")` also needed a full backend restart to pick up, not a hot-swap-only reload -- a partial reload left the running handler mapping without the new route, so `/cities` fell through to `/{eventId}`'s `UUID.fromString("cities")`, throwing `IllegalArgumentException: Invalid UUID string`.

### The Browse Page: Search, Filters, and Numbered Pagination

All filter state lives in the URL (`?q=&page=&city=&date=&price=&sort=`), the same "shareable, back-button-friendly" pattern the organizer list pages already use for `page`/`size`. The search box is the one field that doesn't navigate immediately -- it's debounced (400ms via `useDebouncedValue`) into a `useEffect` that only commits to the URL once typing settles, while every `Select` filter navigates on change immediately, since a dropdown selection isn't firing once per keystroke the way a text input is.

Date and price are preset dropdowns (Any/Today/This week/This month; Any/Free/Under $25/$25-$50/$50+) rather than raw range pickers, translated to the backend's `from`/`to`/`minPrice`/`maxPrice` params client-side:

```typescript
function dateRangeFor(preset: DatePreset | undefined): { from?: string; to?: string } {
  if (!preset || preset === 'any') {
    return {} // omitting both defaults to upcoming-only server-side
  }
  const now = new Date()
  const to = new Date(now)
  // ...advance `to` by a day / a week / a month depending on preset
  return { from: toLocalDateTimeString(now), to: toLocalDateTimeString(to) }
}
```

`toLocalDateTimeString` matters here, not `Date#toISOString()`: `toISOString()` produces a `Z`-suffixed UTC instant string, which Spring's default `LocalDateTime` request-param binder doesn't accept. The helper formats the same wall-clock components a `datetime-local` input would produce instead.

Every preset here is *forward-looking from now*, including "This week" -- it's `now()` through `now() + 7 days`, not "the current calendar week." That was surprising in practice: testing against a real event whose start had rolled into the past showed it wasn't reachable through *any* filter, "This week" included, since none of the presets let `from` fall before the present moment. That's consistent with the resolved design (nobody browsing to buy a ticket wants to filter for something that already happened), not a bug to fix -- but it means there's currently no way to browse a past published event at all, worth knowing if that's ever needed.

Numbered pagination is a second, distinct component from the organizer tables' `PaginationControls` (Previous/Next + a page-size select) -- issue #10 specifically decided page-number links for this screen, matching the endpoint's plain `Page` response shape. `NumberedPagination` doesn't reuse `ui/pagination.tsx`'s own `PaginationLink`/`PaginationPrevious`/`PaginationNext` components directly, for the same reason `PaginationControls` didn't: those render plain `<a>` tags with no `to`/`search` props, so the interactive elements are built with TanStack Router's own `Link`, styled with `buttonVariants` to match.

The two empty states from the design are distinguished by whether any filter is actually active, not just by "zero results":

```typescript
const hasActiveFilters = Boolean(
  search.q || search.city ||
  (search.date && search.date !== 'any') ||
  (search.price && search.price !== 'any'),
)
```

Zero results with no active filter reads "No events published yet"; zero results with one active reads "No events match your search" plus a "Clear filters" button that resets the URL to `/browse` with no search params at all.

### The Event Detail Page

`browse/$eventId.tsx` shows name, dates, venue, and the list of ticket types with price/description -- no purchase button yet, since that's issues #4/#5, deliberately not started as part of this build.

### Making `/browse` Actually Public

The original placeholder for `/browse` lived under the `_attendee` layout route, which requires an attendee login before rendering anything underneath it -- directly contradicting the backend's own intent (`GET /api/v1/published-events` has always been `permitAll()`, specifically so attendees can browse before creating an account). Moving `/browse` to a top-level public route surfaced a route-generation conflict that wasn't obvious in advance: with `browse.tsx` removed, `_attendee` had zero remaining child routes, and TanStack Router's file-based generator treats a pathless layout route with no children as collapsing onto its parent's path (`/`) -- which then collided with the real `index.tsx` at `/`.

The fix wasn't a workaround, it was the thing `_attendee` was always going to need anyway: `_attendee/tickets.tsx`, a placeholder for the attendee's actual "My Tickets" screen (the `tickets` feature's `useTickets`/`useTicket`/`useTicketQrCode` hooks already existed for it, unused until now). `getRoleHomeRoute` now sends a freshly-logged-in attendee to `/tickets` instead of `/browse` -- landing someone back on the one page that no longer needs a login, right after they just logged in, would have been a strange first impression.

With `/browse` public, it also needed to be *reachable* by someone with no reason to be logged in yet -- there was no link to it anywhere. `Header.tsx` gained a "Browse Events" link, visible unconditionally (not gated behind `auth.isAuthenticated`), sitting next to the logo rather than inside the login/logout controls group.

#### Summary

- New `features/published-events/` folder (types/api/hooks), kept separate from `features/events/` -- different resource, different DTOs, no auth
- Hit the same Postgres bind-parameter-type bug a second time, this time failing outright rather than silently mis-resolving; fixed with the same explicit-`CAST`-on-every-occurrence approach across the whole shared `PUBLISHED_EVENTS_WHERE` clause
- The City filter's first version reused the organizer-only `/api/v1/venues` endpoint, which 401'd for anyone without an organizer login and silently redirected them to Keycloak via `apiFetch`'s automatic 401-handling; fixed with a new public `GET /api/v1/published-events/cities` endpoint that's also more correct (only cities with actual published events)
- `browse/index.tsx`: URL-driven filter state, a debounced search box, preset Date/Price dropdowns translated to backend params client-side, a new `NumberedPagination` component (distinct from the organizer tables' Previous/Next one), and two empty states distinguished by whether a filter is actually active
- Discovered while testing: every date filter (including the default) is forward-looking from *now*, so there's currently no way to browse a published event whose date has already passed -- consistent with the resolved design, not treated as a gap
- `browse/$eventId.tsx` shows event details and ticket types; no purchase flow yet (issues #4/#5)
- Moved `/browse` out of the `_attendee` login-gated layout to a public top-level route, which required adding `_attendee/tickets.tsx` (the attendee's real authenticated landing page, not a placeholder-for-its-own-sake) to keep the layout route from colliding with `/` in TanStack Router's file-based route generation; added a "Browse Events" link to `Header.tsx` so the now-public page is actually reachable without a direct URL

## Frontend Purchase Flow & My Tickets

With Browse built, `browse/$eventId.tsx` could show ticket types but not actually sell one. Issues #4 and #5 already decided the shape of this before any code was written: the backend's purchase endpoint takes no quantity, so a multi-ticket purchase has to be a client-side loop, not a single request; and the result lands on its own confirmation page rather than a toast, since a purchase is significant enough to deserve a full screen.

### Purchase Is Gated Client-Side, Not Just Server-Side

`POST /api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets` requires `.authenticated()` in `SecurityConfig` -- any logged-in user, not role-restricted to attendees specifically. Rather than let an anonymous click 401 and fall through to `apiFetch`'s automatic `signinRedirect()`, the purchase handler checks `auth.isAuthenticated` itself first and redirects explicitly:

```typescript
async function handlePurchase(ticketTypeId: string, quantity: number) {
  if (!auth.isAuthenticated) {
    void auth.signinRedirect()
    return
  }
  // ...
}
```

Functionally identical to letting the 401 handle it, but it means a logged-out visitor gets sent to Keycloak the moment they click Buy, not after a request round-trips and fails first.

### The Sequential Purchase Loop

Each ticket type row gets a quantity input (default 1, no artificial max -- the backend enforces its own cap via `TicketsSoldOutException`) and a Buy button. Since there's no bulk-purchase endpoint, quantity > 1 fires the single-ticket endpoint N times in a row, awaited one at a time, with live progress:

```typescript
let purchased = 0
let errorMessage: string | undefined
for (let i = 0; i < quantity; i++) {
  try {
    await purchaseTicket.mutateAsync({ eventId, ticketTypeId })
    purchased += 1
    setProgress({ done: purchased, total: quantity })
  } catch (error) {
    errorMessage = toastErrorMessage(error, 'Something went wrong')
    break
  }
}
```

Stopping at the first failure (rather than firing all N at once and reconciling a mixed-result pile afterward) matters concretely when a ticket type sells out mid-loop: `TicketTypeServiceImpl.purchaseTicket` takes a pessimistic lock and throws `TicketsSoldOutException` (409, "No tickets remaining for this ticket type") the moment `purchasedTickets + 1 > totalAvailable` -- a real scenario if two people are buying the last few tickets to the same event at once, not a hypothetical.

### The Confirmation Page

`/browse/confirmation` is a sibling route, not nested under `$eventId` -- `$eventId.tsx` has no children today, and nesting a route under it purely for this one page would turn it into a layout route for no other reason. Instead it's search-param driven (`?eventId=&ticketTypeId=&requested=&purchased=&errorMessage=`), since by the time the purchase loop finishes, the calling page already knows everything the confirmation page needs to show -- there's no server-side "order" resource to look up.

```typescript
const isFullSuccess = purchased === requested
```

Success (`purchased === requested`) and Partial (`purchased < requested`, including the `purchased === 0` case) are the two variants issue #5 called for, both offering "View My Tickets" and "Back to Event" -- Partial additionally surfaces the captured `errorMessage` (the same message `usePurchaseTicket`'s `onError` toast already showed, kept here so it's still visible after the toast fades).

### My Tickets: List and Detail

`_attendee/tickets.tsx` had been a placeholder since the `/browse` work, existing only to keep TanStack Router's file-based generator happy. It's now a real folder, `_attendee/tickets/{index,$ticketId}.tsx`, the same list/detail split Events already uses. The list (`useTickets` + `PaginatedTable`) shows ticket type, price, and status; the detail page (`useTicket`) shows the event context `ListTicketResponse` doesn't carry -- event name, venue, dates -- plus the QR code image.

The QR code needed `useObjectUrl`, a small hook built earlier in this project specifically for this: `useTicketQrCode` returns a raw `Blob` (the endpoint's success body is `image/png`, not JSON), and a `Blob` isn't usable directly as an `<img src>`. `useObjectUrl` wraps it in `URL.createObjectURL`, revoking the previous URL whenever the blob changes or the component unmounts.

A cancelled ticket shows "This ticket has been cancelled and can no longer be used for entry" instead of rendering the QR at all -- the QR code itself still exists and would still decode, but showing it as if it were current would be actively misleading.

One real gap surfaced while building this, not fixed: `GetTicketResponse` has no ticket-type `name` field, only `description` (nullable) and `price` -- confirmed directly against `TicketServiceImpl.convertToGetTicketResponseDto`, which simply never sets one. The detail page can show what a ticket type costs and its free-text description, but not the name you'd recognize it by (e.g. "General Admission"). That's the backend's existing shape, not something this build changed.

### `TicketStatus` Becomes an Enum

Building the detail page's cancelled-ticket branch initially compared `ticket.status === 'CANCELLED'` directly. Per feedback on avoiding magic strings, `features/tickets/types.ts` gained the same const-object-as-enum pattern `EventStatus` already established:

```typescript
export const TicketStatus = {
  PURCHASED: 'PURCHASED',
  CANCELLED: 'CANCELLED',
} as const

export type TicketStatus = (typeof TicketStatus)[keyof typeof TicketStatus]
```

#### Summary

- Purchase is gated by `auth.isAuthenticated` client-side before any request fires, not just left to the backend's `.authenticated()` 401 to redirect
- Multi-ticket purchases loop the single-ticket endpoint sequentially, awaiting each call, with a live "Purchasing... (X of Y)" progress state; stops at the first failure (e.g. a real `TicketsSoldOutException` mid-loop) rather than firing every request at once
- `/browse/confirmation` is a search-param-driven sibling route (not nested under `$eventId`), rendering Success/Partial variants per issue #5's design, both offering "View My Tickets"/"Back to Event"
- `_attendee/tickets.tsx`'s placeholder became a real `index`/`$ticketId` list-detail pair; the detail page renders the QR code via the existing `useObjectUrl` hook, and shows a plain "cancelled" message instead of a QR for a cancelled ticket
- Found (not fixed) a real backend DTO gap: `GetTicketResponse` has no ticket-type name field, only description/price
- `TicketStatus` converted to the same const-object-enum pattern as `EventStatus`, per feedback on magic-string status comparisons

## Frontend Staff Ticket Validation

Issue #9's decision was specific about the library and the interaction shape: `qr-scanner` (nimiq), Web Worker-based, continuous auto-scan, a full-screen colored result that auto-resumes -- not a "point camera, tap to confirm" flow.

### What's Actually Encoded in the QR Image

Rather than guess at the QR payload's format, `QrCodeServiceImpl.generateQrCodeImage` settled it directly: it encodes `uniqueId.toString()`, where `uniqueId` is the `QrCode` entity's own `domainId` -- a bare UUID string, not a URL and not JSON. That means a scanned frame's decoded text can be POSTed as-is:

```typescript
(scanResult) => void handleValidate(scanResult.data, TicketValidationMethod.QR_SCAN)
```

no parsing step needed between "what the camera read" and "what the validation endpoint expects" for `id`.

### Reading the Validation Semantics From the Source, Not Assuming Them

`TicketValidationServiceImpl.validateTicket` has no explicit "already used" flag -- it's derived from history:

```java
TicketValidationStatusEnum ticketValidationStatus = ticket.getValidations().stream()
        .filter(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()))
        .findFirst()
        .map(v -> TicketValidationStatusEnum.INVALID)
        .orElse(TicketValidationStatusEnum.VALID);
```

The first scan of a ticket is always `VALID`; every scan after that is `INVALID`, because a prior `VALID` validation now exists. A not-found ticket or QR code (`TicketNotFoundException`/`QrCodeNotFoundException`) is a `400`, not a `404`, with a real localized message ("Ticket not found"/"QR code not found") -- confirmed against `ErrorCode`, not assumed. That shaped the screen's three-way outcome directly: ADMIT and ALREADY USED come from the response body's `status` field on a `200`; NOT FOUND comes from the mutation's *error* path, since the backend expresses "doesn't exist" as a thrown exception, not a response value.

### The Scan Screen

A `<video>` ref feeds a `QrScanner` instance (installed fresh -- nothing in this project touched a camera before); its decode callback and a manual reference-code form both funnel into one `handleValidate(id, method)`, so ADMIT/ALREADY-USED/NOT-FOUND rendering only exists in one place regardless of how the ticket got scanned:

```typescript
async function handleValidate(id: string, method: TicketValidationMethod) {
  if (isProcessingRef.current || !id) {
    return
  }
  isProcessingRef.current = true
  void scannerRef.current?.pause()
  // ...validate, setResult(...), then a 2s window before resuming
}
```

`isProcessingRef` is a ref, not state, deliberately -- it has to block re-triggering synchronously for extra frames scanned while a result is still on screen (a `useState` update wouldn't apply until the next render, which is too late for a callback firing multiple times per second). The result itself (`ADMIT` green / `ALREADY_USED` amber / `NOT_FOUND` red) fills the whole video area and clears itself after 2000ms, resuming the scanner.

One real bug caught before it shipped, not after: the manual form's `disabled` prop was first wired to that same `isProcessingRef.current`. A ref mutation doesn't trigger a re-render, so the input would never actually *look* disabled even while a validation was in flight or a result was showing. Fixed by deriving a proper piece of state for anything the render needs to react to:

```typescript
const isBusy = result !== null || validateTicket.isPending
```

keeping the ref for its one real job -- the synchronous guard inside the scan callback -- and using `isBusy` everywhere the UI itself needs to know.

`TicketValidationMethod` and `TicketValidationStatus` picked up the same const-object-enum treatment as `TicketStatus` while writing this screen's fresh `=== 'VALID'`/`=== 'QR_SCAN'`-style comparisons, for the same reason.

No browser was available to click through this screen directly in this environment; a full `npm run build` was run instead as a stand-in check, confirming `qr-scanner`'s Web Worker actually split into its own chunk (`qr-scanner-worker.min-*.js`) rather than just type-checking cleanly -- a bundling failure in a worker import wouldn't necessarily show up in `tsc --noEmit`.

#### Summary

- `qr-scanner` (nimiq) installed for continuous, Web Worker-based scanning per issue #9's decision
- Confirmed from `QrCodeServiceImpl` that a QR's raw payload is a bare UUID string (the `QrCode` entity's own `domainId`) -- no parsing needed between scan and API call
- Confirmed from `TicketValidationServiceImpl`/`ErrorCode` that "already used" is derived from validation history (not a stored flag) and "not found" is a `400` thrown exception, not a `404` response value -- shaping ADMIT/ALREADY-USED as response-body outcomes and NOT-FOUND as an error-path outcome
- One `handleValidate` function serves both the camera callback and the manual reference-code form
- Caught and fixed a real bug pre-ship: a ref-driven `disabled` prop never actually re-rendered; replaced with `isBusy` state for anything the render needs, keeping the ref only for its synchronous re-entrancy guard
- `TicketValidationMethod`/`TicketValidationStatus` converted to the same const-object-enum pattern as `TicketStatus`
- No browser available to test interactively; verified the client bundle (not just types) via a full production build instead

## Frontend Reports Dashboard

Issue #8's own notes state the real constraint plainly: analytics-service's summary endpoint is per-event only (`GET /analytics/events/{eventId}/summary` -> `{eventId, ticketsSold, revenue}`), with no organizer-wide rollup. The dashboard's design was decided against that shape, not against a richer one that doesn't exist yet -- a chart, not a table, one sorted horizontal bar per event.

### One Summary Call Per Event

`useEvents({ page: 0, size: 100 })` gets the organizer's events (capped at the shared pagination system's largest allowed page size -- a real "fetch everything" endpoint doesn't exist, so this is "as many as one page can hold," not truly unbounded). A new hook fans out one summary query per event:

```typescript
export function useEventAnalyticsSummaries(eventIds: string[]) {
  return useQueries({
    queries: eventIds.map((eventId) => ({
      queryKey: [...analyticsKey, 'events', eventId, 'summary'],
      queryFn: () => getEventAnalyticsSummary(eventId),
    })),
  })
}
```

matching each event to its summary by array index, then sorting by revenue descending client-side. This is genuinely N requests for N events, not one -- an accepted tradeoff given the endpoint that exists, not an oversight.

### Going Through `/dataviz` Before Touching Color

The wayfinder map's own notes call for the `/dataviz` skill before designing any chart, so this went through it rather than picking a look by eye. "Compare magnitude, low to high" maps directly to a sequential, single-hue bar -- confirming the ticket's own instinct rather than second-guessing it. The one wrinkle: the skill's color validator is built to check *categorical* palettes (telling several series apart by hue), and every bar here shares the exact same color -- there's no identity being color-coded, since each event is already labeled by its position and its own text label. Running the categorical checks against it FAILed on chroma floor, but the validator's own documented scope says that check doesn't apply to a lone repeated color; the right check for that case is a WCAG *text*-contrast check against the surface it sits on. The app's existing `--lagoon-deep` accent (already used as text elsewhere in the app) clears that comfortably, so no new color was introduced.

### Bar Anatomy, Not Guessed

The skill's mark spec is specific: bars capped at 24px thick (rendered at 20px here), square at the baseline, 4px rounded at the data end, with a 2px surface gap between adjacent marks:

```jsx
<div className="h-5 flex-1 rounded-sm bg-(--line)">
  <div
    className="h-5 rounded-r-[4px] bg-(--lagoon-deep) ..."
    style={{ width: `${widthPercent}%` }}
  />
</div>
```

Per issue #8's explicit accessibility requirement ("no value lives only in a hover tooltip"), every bar is direct-labeled with both `revenue` and `ticketsSold` as plain text at the tip -- which, per the skill's own rule that axis ticks "carry the values you didn't directly label," meant the chart doesn't need a numeric x-axis or gridlines at all, since every value here already has one. A single-hue, single-series chart also doesn't need a legend -- the section heading ("Revenue by event") already says what's plotted, and a legend box with one swatch would just restate it.

Built as plain divs sized by percentage width, not a charting library -- `recharts`/shadcn's chart wrapper weren't already installed, and a single sorted-bar form doesn't need what they'd add.

#### Summary

- New `useEventAnalyticsSummaries` hook fans out one summary query per organizer event (`useQueries`), since analytics-service has no rollup endpoint to call instead
- Went through `/dataviz` per the wayfinder map's own instruction before picking a chart form or a color
- The skill's categorical chroma/CVD checks don't apply to this chart (one repeated hue, not several colors distinguishing categories); validated with a WCAG contrast check against the card surface instead, reusing the app's existing `--lagoon-deep` accent
- Bars follow the skill's fixed anatomy (≤24px, square baseline, 4px rounded tip); every bar direct-labels both revenue and tickets sold, so the chart skips a numeric axis entirely and needs no legend
- Built with plain HTML/CSS, not a charting library

## Backend: PostGIS Proximity Search

"PostGIS / events near me" had sat in the wayfinder map's Out of scope list since planning, flagged specifically as cheap to add later because `Venue` already stores plain `latitude`/`longitude`. Picking it up meant finding out whether "cheap" actually held up against this project's real Postgres instance, not just the abstract idea of it.

### Installing PostGIS Was the Actual Blocker

`SELECT name FROM pg_available_extensions WHERE name LIKE '%postgis%'` against the live database (Postgres 14.18 on a bare-metal host, not a container) came back with zero rows -- not "disabled," genuinely not installed at the package level. `apt-get install postgresql-14-postgis-3` failed with a wall of unmet dependencies (`libc6 >= 2.35`, `libstdc++6 >= 11`, ...) -- version floors that belong to Ubuntu 22.04 (jammy), not the 20.04 (focal) base this Postgres install actually runs on. The `pgdg.list` apt source was pointed at `jammy-pgdg`, on a machine running elementary OS 6.1 -- whose own `lsb_release -c` reports `jolnir`, not the Ubuntu codename underneath it, which is presumably how the mismatch got there in the first place.

Fixing the codename to `focal-pgdg` didn't immediately work either: `apt.postgresql.org` returned a real, edge-cached `404` for `dists/focal-pgdg/Release` (confirmed via the response headers -- `x-cache: HIT` with hundreds of prior hits and an `age` over 12 hours, not a one-off blip). Fetching PGDG's own `dists/` directory listing confirmed why: `focal-pgdg` isn't in the current set at all (`bookworm`, `jammy`, `noble`, ...) -- PGDG had dropped it from the live repo entirely, following Ubuntu 20.04's end of standard support. It wasn't gone for good, though: `apt-archive.postgresql.org` still serves it, including a `postgresql-14-postgis-3` build (`3.5.3+dfsg-1~exp1.pgdg20.04+1`) whose dependencies (`libgdal26`, `libjson-c4`, `libproj15`, `libc6 >= 2.29`, ...) are genuinely focal-era versions, not jammy's. Swapping the apt source to the archive host, keeping the same `focal-pgdg` codename, installed cleanly.

`CREATE EXTENSION postgis;` still needed a superuser -- the `ticketservice` app role (confirmed non-superuser, and not the `ticketservice` database's owner either, which is `postgres`) can't run it. That step stays a documented manual prerequisite; a fresh environment's own Liquibase run can't set this up for itself, which shapes the migration below.

### The Liquibase Migration

`003-add-venue-geography.xml` adds a `geography(Point,4326)` column to `venues`, backfills it from the existing `latitude`/`longitude` for every row that already has both, and adds a GiST index -- three changesets, continuing the existing changeset-id numbering (`12`/`13`/`14`, after `11-add-ticket-reference-code`):

```java
<changeSet id="12-add-venue-location" author="event-ticket-platform" dbms="postgresql">
    <preConditions onFail="HALT" onFailMessage="PostGIS is not enabled on this database -- run `CREATE EXTENSION postgis;` as a superuser first.">
        <sqlCheck expectedResult="1">SELECT count(*) FROM pg_extension WHERE extname = 'postgis'</sqlCheck>
    </preConditions>
    <addColumn tableName="venues">
        <column name="location" type="geography(Point,4326)"/>
    </addColumn>
</changeSet>
```

The `preConditions`/`sqlCheck` exists because `CREATE EXTENSION` can't safely be a changeset itself (see above) -- without it, a fresh environment missing the extension would fail with a cryptic "type geography does not exist" partway through, instead of a clear halt naming the actual fix. The backfill and index are plain `<sql>` changesets (`ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)`, `CREATE INDEX ... USING GIST (location)`) -- PostGIS functions aren't expressible through Liquibase's portable tags. Verified against the live database afterward, not just trusted: the column, both venues' correctly-backfilled points, the GiST index, and all three changelog rows were all confirmed present via direct queries.

### Keeping `location` in Sync

Mapping the new column onto the entity needed `hibernate-spatial` (`org.hibernate.orm:hibernate-spatial`, unpinned -- Spring Boot's dependency management resolved it to the exact version matching `hibernate-core`, `7.4.1.Final`, confirmed via `./gradlew dependencies`). `Venue` gained a `Point location` field and one new method, rather than leaving `latitude`/`longitude`/`location` as three independently-settable fields that could drift out of sync:

```java
public void setCoordinates(Double latitude, Double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.location = (null != latitude && null != longitude)
            ? GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude))
            : null;
}
```

`Coordinate`'s constructor takes `(x, y)` -- i.e. `(longitude, latitude)` -- matching the migration's own `ST_MakePoint(longitude, latitude)` order exactly; getting this backwards would have silently placed every venue at its coordinates' mirror image. `VenueServiceImpl`'s `createVenue`/`updateVenue` both switched from two separate `setLatitude`/`setLongitude` calls to this one method, so `location` now rides along for free on every future create/update instead of only reflecting the migration's one-time backfill.

`VenueRepository` gained a matching read path, `findWithinRadius` -- a native `ST_DWithin`/`ST_Distance` query, nearest-first, taking plain `latitude`/`longitude` doubles rather than constructing a JTS `Point` at the call site (consistent with how every other geo-adjacent value in this codebase is handled). Confirmed directly against the two real venues: 0m for the origin venue, ~11.87km for the other, correctly ordered.

### Wiring the Geo Filter Into Published Events

The actual "near me" filter lives in `EventRepository.PUBLISHED_EVENTS_WHERE`, extended with the same "any-null-means-no-op" shape every other optional filter there already uses, just spread across three co-dependent parameters instead of one:

```java
"AND (CAST(:latitude AS double precision) IS NULL OR CAST(:longitude AS double precision) IS NULL OR CAST(:radiusMeters AS double precision) IS NULL " +
"OR ST_DWithin(v.location, ST_SetSRID(ST_MakePoint(CAST(:longitude AS double precision), CAST(:latitude AS double precision)), 4326)::geography, CAST(:radiusMeters AS double precision)))"
```

No explicit `v.location IS NOT NULL` guard is needed: `ST_DWithin` against a null geography evaluates to `NULL`, and a `NULL` in a `WHERE` clause already excludes the row, so a venue with no coordinates is filtered out by the same expression that includes nearby ones. A fourth native query variant, `findPublishedEventsSortedByDistance`, joins the existing three (soonest/priceAsc/priceDesc) -- `EventServiceImpl.findPublishedEvents` only routes to it when all three geo parameters are actually present, falling back to the default sort otherwise, the same way an unrecognized `sortBy` value already did, rather than sorting by distance to nowhere.

Tested live against the one real published event, since this endpoint is public and needs no JWT: an origin at the event's own venue with a 1km radius returned it; an origin in New York with the same radius returned nothing; `sortBy=distance` (as it was named before the request-body redesign below) returned it correctly ordered; and a request with no geo parameters at all was unaffected, confirming the filter is additive.

One deliberate boundary: the response DTO doesn't carry a computed distance value. Showing "2.3km away" on a card would mean restructuring the native queries into a projection instead of returning `Event` entities directly -- left for whenever the frontend's actual "Near me" UI gets built, alongside this.

### From Query Params to a Search Request Body

`listPublishedEvents` had grown to nine independent optional filters plus `Pageable` -- past the point a query string stays a reasonable way to call it. The fix: `POST /api/v1/published-events/search` with a JSON body, not `GET` with an ever-longer parameter list. Not a bare `POST /published-events` either -- this codebase's own convention (`VenueController`, `EventController`) already uses `POST` on a collection root to mean "create," which doesn't apply to a public, read-only search; `/search` is a distinct sub-path instead, the same way a POST-based search endpoint is conventionally modeled elsewhere. This needed its own `permitAll` rule in `SecurityConfig` -- the existing one only matched `GET`, and HTTP method is part of what a `requestMatcher` matches on, not just the path; missing this would have silently 401'd every anonymous browse request.

The request body itself became `ListPublishedEventsRequestDto extends SearchRequestDTO`, built on top of a small reusable paging framework rather than a one-off DTO:

```java
public abstract class AbstractPaging {
    @JsonProperty("page")
    protected Integer pagingStart;
    @JsonProperty("limit")
    protected Integer pagingSize;
    @JsonProperty("sortField")
    protected String sortingColumn;
    @JsonProperty("sortOrder")
    protected String sortingDirection;
}
```

This raised a real design question rather than a mechanical one: the four named sorts (soonest/priceAsc/priceDesc/distance) aren't a generic column-plus-direction pair -- "distance" only ever makes sense ascending, "soonest" sorts by `event_start`, not a price column -- so `sortField`/`sortOrder` couldn't reproduce them automatically. Resolved by keeping `sortField` (`paging.getSortingColumn()`) carrying the exact same four strings the old standalone `sortBy` field did, leaving `sortOrder` unused for this endpoint, rather than guessing and silently dropping the price/distance sort options the browse page already depends on.

`SearchRequestDTO.paging` wasn't defaulted in its own constructor at first, which would have pushed a null-check onto every future controller built on it. Given a proper home instead:

```java
public SearchRequestDTO() {
    this.paging = new Paging();
}
```

with `Paging`'s own no-arg constructor already defaulting `page=0`/`limit=10`. `PublishedEventController` no longer needs its own fallback for a missing `paging` object as a result -- confirmed by re-testing both a `{}` body and a request with no body at all, both still returning correctly defaulted, paginated results.

The frontend's `published-events/api.ts` needed matching updates at each step -- GET-with-query-string to POST-with-body, then flattening `page`/`size`/`sortBy` into a nested `paging: { page, limit, sortField }` object -- since leaving it on the old contract would have silently broken the browse page's pagination and sort, not just left a feature unbuilt.

#### Summary

- PostGIS wasn't installed on the live Postgres server at all; fixing it meant correcting a `jammy` vs `focal` apt codename mismatch (traced to elementary OS reporting its own codename, not its Ubuntu base) and then discovering PGDG had moved focal's repo to an archive host after Ubuntu 20.04's EOL
- `003-add-venue-geography.xml`: a `geography(Point,4326)` column, a backfill from existing `latitude`/`longitude`, and a GiST index -- guarded by a `preConditions` halt naming the fix if PostGIS isn't enabled, since `CREATE EXTENSION` can't safely be a changeset itself (the app's DB role is neither superuser nor the database owner)
- `Venue.setCoordinates(latitude, longitude)` is now the one path that keeps `latitude`/`longitude`/`location` in sync, replacing two independent setters; `VenueRepository.findWithinRadius` added a nearest-first proximity query, both verified against real venue coordinates
- `EventRepository`/`EventService`/`PublishedEventController` gained a geo filter (any of latitude/longitude/radiusMeters absent is a no-op, matching every other optional filter's shape) and a fourth sort variant, distance -- tested live against the one real published event from multiple origins
- Rebuilt `listPublishedEvents` as `POST /published-events/search` with a `ListPublishedEventsRequestDto` body extending a new reusable `SearchRequestDTO`/`Paging`/`AbstractPaging` framework, resolving a real ambiguity (the four named sorts aren't a generic column+direction pair) by keeping `sortField` as their carrier and leaving `sortOrder` unused; updated the frontend to match at each contract change so the browse page kept working throughout

## Frontend: Near Me Search

With the geo filter live on the backend, the remaining piece was actually surfacing it -- a control on `/browse` to request it, and a sort option that means something once it's active.

### Sort Selection Becomes an Enum

Before touching the frontend, `EventServiceImpl.findPublishedEvents`'s sort switch went from a raw `String` to a real enum, on request: a plain `switch (sortBy) { case "priceAsc" -> ...; default -> ... }` was easy to typo and had no compiler backstop if a fifth sort option ever needed a query variant. `PublishedEventsSortBy` (`SOONEST`, `PRICE_ASC`, `PRICE_DESC`, `DISTANCE`) replaced it, converted from the wire-level string exactly once, at the controller boundary:

```java
PublishedEventsSortBy sortBy = PublishedEventsSortBy.fromWireValue(paging.getSortingColumn());
```

`EventService.findPublishedEvents` now takes the enum, not a `String` -- `EventServiceImpl`'s switch is exhaustive over all four constants with no `default` branch, so a future fifth sort option that forgets its query variant fails to compile instead of silently falling through at runtime. Verified live across every branch: no sort field, `priceAsc`, `distance` without an origin (falls back to soonest), `distance` with one, and a garbage value (falls back too) -- all `200`, nothing errors on bad input.

### A Real Navigation Gap, Found by Actually Using the App

Testing surfaced a genuine bug, not a missing feature: as an organizer, leaving `/dashboard`, `/venues`, or `/events` for the (now-public, unauthenticated) `/browse` page left no way back -- `Header.tsx` had never carried any role-specific navigation at all, and `_organizer.tsx`'s own layout-local `<nav>` (Dashboard/Venues/Events links) only ever rendered *inside* the organizer section, doing nothing once you'd already left it.

The fix moved that navigation to the one place every route actually shares -- the global header -- reading roles the same way `getRoleHomeRoute` already does:

```tsx
const roles = getRoles(auth.user)
// ...
{roles.includes(ROLE_ORGANIZER) && (
  <>
    <Link to="/dashboard">Dashboard</Link>
    <Link to="/venues">Venues</Link>
    <Link to="/events">Events</Link>
  </>
)}
```

with matching conditional links for staff (`/scan`) and attendees (`/tickets`), alongside the unconditional "Browse Events" link that already lived there. `_organizer.tsx`'s now-redundant layout-local nav was deleted -- its `component` collapsed back to a plain `() => <Outlet />`, matching `_attendee.tsx`/`_staff.tsx`, which never had a nav row of their own.

### The Browse Page's "Near Me" Control

A "Use my location" button calls the browser's Geolocation API; on success, `lat`/`lng` land in the URL like every other filter on this page (shareable/bookmarkable, not just component state), defaulting the radius to 25km. Once active, the button becomes "Clear location," a radius preset dropdown appears (10/25/50/100km, matching the existing Date/Price preset-dropdown pattern rather than a raw number input), and "Nearest" becomes a selectable Sort option -- hidden until there's an actual origin to measure from, since offering it earlier would silently do nothing:

```tsx
{hasLocation && <SelectItem value="distance">Nearest</SelectItem>}
```

One real bug caught before shipping, the same class as `browse/$eventId.tsx`'s staff-scan fix earlier: the first draft guarded `navigator.geolocation` for existence before calling it. ESLint's `no-unnecessary-condition` -- a genuinely active, configured rule here, unlike the couple of unconfigured `jsx-a11y`/`react-hooks` rules hit earlier in this build -- correctly flagged it as dead code: TypeScript's own DOM types declare `navigator.geolocation` as always present. Removed rather than suppressed, per this project's own "don't guard against what can't happen" convention.

### Diagnosing "100km Away, But It's Showing Athens"

A live report -- "I asked for events within 100km and got two results, but I'm in Mytilene, not Athens" -- looked like a filter bug at first glance, so it got investigated rather than assumed. The actual query, tested directly against both venues from genuine Mytilene coordinates, correctly excluded them (271km and 281km away, both `false` for "within 100km"). The reported URL's actual coordinates (`lat=37.9842&lng=23.7353`) told the real story: tested against the same two venues, those coordinates sit 5.3km and 6.6km away -- literally in central Athens, not Mytilene at all.

The filter had done exactly what it was asked; the browser's `getCurrentPosition()` had simply returned a bad fix. Without GPS hardware (a desktop/laptop, most likely), browsers fall back to network-based positioning -- Wi-Fi access point databases or, worst case, the ISP's registered IP address block -- which can be off by hundreds of kilometers, especially over a connection routed through infrastructure registered somewhere else. Nothing in this app's code can correct for that; it only ever sees whatever coordinates the browser reports. (A separate, smaller question from the same report -- the URL's `radius=%22100%22`, with visible quote characters -- turned out to be harmless: TanStack Router's default search serialization JSON-encodes every string search param the same way, and reads them back the same way it writes them, so it round-trips correctly regardless of how it looks in a raw address bar.)

#### Summary

- `EventServiceImpl.findPublishedEvents`'s sort selection is now `PublishedEventsSortBy`, a real enum with an exhaustive switch (no `default` branch) instead of a string switch with no compiler backstop
- Found and fixed a real navigation gap: no page anywhere linked back to an organizer's own section once they left it for `/browse`. Moved Dashboard/Venues/Events (and Staff/Attendee equivalents) into the global `Header.tsx`, reading roles the same way `getRoleHomeRoute` does; deleted the now-redundant layout-local nav from `_organizer.tsx`
- `/browse` gained a "Use my location" control -- URL-persisted `lat`/`lng`, a radius preset dropdown matching the page's existing filter style, and a "Nearest" sort option that only appears once it can mean something
- Caught a real bug pre-ship (an unnecessary, ESLint-flagged `navigator.geolocation` existence check) rather than suppressing the lint rule
- Investigated a user-reported "wrong location" result down to its actual cause: not a filter bug, but the browser's own network-based (non-GPS) geolocation returning an inaccurate fix -- confirmed by testing the exact reported coordinates directly against the database

## Frontend: Venue Location Picker

The last piece of "PostGIS was flagged as cheap to add a map picker for later" (issue #7's own notes) -- now that a real geography column exists, typing decimal latitude/longitude by hand no longer has to be the only way to set a venue's location.

### A Click-to-Place-Pin Map, Not a Replacement

`LocationPicker` (`features/venues/components/LocationPicker.tsx`) is a Leaflet + OpenStreetMap map added to `VenueForm`, directly above the existing latitude/longitude inputs -- an additional way to fill those same two fields, not a replacement for the ones already there:

```tsx
<LocationPicker
  latitude={toNumberOrUndefined(latitude)}
  longitude={toNumberOrUndefined(longitude)}
  onChange={(nextLatitude, nextLongitude) => {
    form.setValue('latitude', nextLatitude.toFixed(6), { shouldValidate: true, shouldDirty: true })
    form.setValue('longitude', nextLongitude.toFixed(6), { shouldValidate: true, shouldDirty: true })
  }}
/>
```

Clicking the map sets both fields via `form.setValue`; anyone who already knows their venue's exact coordinates can still just type them. `toNumberOrUndefined` guards against a mid-typed or invalid string (e.g. `"-"`) ever reaching the picker as `NaN` -- `LocationPicker` treats "no position yet" as `undefined`, not a number that happens to be broken.

### The Leaflet-in-Vite Gotcha, Checked Rather Than Assumed

Leaflet's default marker icon is a well-known bundler trap: it references relative image paths that resolve fine served as static files but break once a bundler hashes and relocates them. The fix is re-pointing the icon at the actual bundled URLs via Vite's `?url` imports:

```tsx
import markerIconUrl from 'leaflet/dist/images/marker-icon.png?url'
// ...
const markerIcon = L.icon({ iconUrl: markerIconUrl, /* ...retina + shadow variants */ })
```

Rather than trust that this worked, the production build's actual output got inspected: `VenueForm-*.css` came back as its own code-split chunk (confirming `leaflet/dist/leaflet.css` bundled correctly), and grepping the built `VenueForm-*.js` chunk for `data:image/png;base64` found exactly three matches -- one per marker asset, all under 2.5KB and so base64-inlined by Vite's default asset threshold rather than emitted as separate files. `react-leaflet` 5.0.0 was the deliberate pick for React 19 compatibility (checked before installing, not after); `@types/leaflet` was needed separately since `leaflet` itself ships no types of its own.

#### Summary

- `LocationPicker`: a Leaflet + OpenStreetMap click-to-place-pin map, added to `VenueForm` alongside (not replacing) the existing latitude/longitude number inputs
- `toNumberOrUndefined` keeps a mid-typed or invalid coordinate string from ever reaching the map as `NaN`
- Verified the classic Leaflet-in-Vite marker-icon bundling trap actually resolved correctly by inspecting the real build output (a dedicated CSS chunk, three correctly base64-inlined marker images), not by assuming the `?url` import pattern would just work
- `react-leaflet` 5.0.0 chosen for React 19 compatibility; `@types/leaflet` added since `leaflet` ships no bundled types

## Ticket Cancellation & Refund

Issue #1's wayfinder map flagged this explicitly out of scope: `TicketStatusEnum.CANCELLED` had existed since the very first ticket lesson, but nothing ever set it, and a real feature (who's allowed to cancel, what happens to inventory and reporting, how staff at the door find out) was judged well beyond what surfaced it -- issue #16's short reference-code work. Built after that map closed, the same way PostGIS was. There's still no payment gateway (see issue #1's other out-of-scope entry), so "refund" here means an audit trail -- `cancelledAt`/`cancelReason`/`cancelNote` -- not money changing hands.

### Two Cancel Paths, One Shared Guard

An attendee can cancel their own ticket (`POST /api/v1/tickets/{ticketId}/cancel`); an organizer can cancel any ticket sold for one of their events (`POST /api/v1/events/{eventId}/tickets/{ticketId}/cancel`). Both are thin wrappers around the same private `TicketServiceImpl` logic:

```java
private void guardCancellable(Ticket ticket) {
    if (TicketStatusEnum.CANCELLED.equals(ticket.getStatus())) {
        throw new TicketAlreadyCancelledException(ErrorCode.TICKET_ALREADY_CANCELLED, ticket.getDomainId());
    }

    if (EventStatusEnum.COMPLETED.equals(ticket.getTicketType().getEvent().getStatus())) {
        throw new TicketEventAlreadyCompletedException(ErrorCode.TICKET_EVENT_ALREADY_COMPLETED, ticket.getDomainId());
    }

    boolean alreadyValidated = ticket.getValidations().stream()
            .anyMatch(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()));
    if (alreadyValidated) {
        throw new TicketAlreadyValidatedException(ErrorCode.TICKET_ALREADY_VALIDATED, ticket.getDomainId());
    }
}

private Ticket cancelTicket(Ticket ticket, TicketCancelReasonEnum reason, String note) {
    ticket.setStatus(TicketStatusEnum.CANCELLED);
    ticket.setCancelledAt(LocalDateTime.now());
    ticket.setCancelReason(reason);
    ticket.setCancelNote(note);

    Ticket savedTicket = ticketRepository.save(ticket);
    ticketEventPublisher.publishTicketCancelled(savedTicket);

    return savedTicket;
}
```

Three ways a cancel gets rejected, each its own `ErrorCode` (`409 Conflict`, not `400` -- these are state conflicts, not malformed requests): already cancelled, or the ticket's already gotten someone through the door -- the same "is there already a `VALID` entry in this ticket's validation history" check three different places now make independently (`TicketValidationServiceImpl`'s own ADMIT/ALREADY-USED logic, this guard, and the event-cancellation cascade below, rather than one shared helper), or the event's already `COMPLETED`. `cancelReason` (`ATTENDEE_REQUEST`/`ORGANIZER_ACTION`/`EVENT_CANCELLED`) is never client-supplied -- `CancelTicketRequestDto` carries only an optional `note`, and which reason gets recorded is inferred entirely from which endpoint was called. All three cancellation columns (`Ticket.cancelledAt`/`cancelReason`/`cancelNote`) are set together by `cancelTicket`, and nowhere else -- there's no path that sets one without the others.

### Cancelling an Event Cascades to Its Tickets

Before this, `EventServiceImpl#cancelEvent` was a plain status flip -- an attendee holding a ticket to a cancelled event would never find out from the ticket itself, since nothing about the ticket ever changed. It now cascades:

```java
private void cancelTicketsForCancelledEvent(Event event) {
    List<Ticket> cancellableTickets =
            ticketRepository.findByEventIdAndStatusNotWithValidations(event.getId(), TicketStatusEnum.CANCELLED);

    for (Ticket ticket : cancellableTickets) {
        boolean alreadyValidated = ticket.getValidations().stream()
                .anyMatch(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()));
        if (alreadyValidated) {
            continue;
        }

        ticket.setStatus(TicketStatusEnum.CANCELLED);
        ticket.setCancelledAt(LocalDateTime.now());
        ticket.setCancelReason(TicketCancelReasonEnum.EVENT_CANCELLED);
        Ticket savedTicket = ticketRepository.save(ticket);
        ticketEventPublisher.publishTicketCancelled(savedTicket);
    }
}
```

An already-validated ticket is skipped, not an error that aborts the whole cascade -- the same "can't cancel after entry" rule `guardCancellable` enforces for a single ticket, just applied per-ticket here so one already-admitted attendee doesn't block every other ticket on the event from being cancelled too.

Confirmed live: cancelling an event flips its own sold tickets to `CANCELLED` too.

### Availability and Reporting Both Switch to an "Active" Count

`TicketRepository.countByTicketTypeId` -- the raw, unfiltered count from the very first purchase-flow lesson -- stays exactly as it was, still backing issue #12's ticket-type-removal safeguard: a cancelled ticket is still a real historical sale, and orphan-deleting its ticket type would destroy that row's own cancellation audit trail along with it. A new sibling, `countActiveByTicketTypeId`, excludes `CANCELLED` and takes over everywhere "sold" actually means "still holds a slot":

```java
@Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status <> :cancelledStatus")
int countActiveByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId, @Param("cancelledStatus") TicketStatusEnum cancelledStatus);
```

`TicketTypeServiceImpl#purchaseTicket`'s sold-out check switched to it -- a cancelled ticket now genuinely frees its slot back up for someone else to buy -- and so did the `ticketsSold` figure `EventServiceImpl` shows organizers (issue #12's own field), so a cancelled sale stops counting as sold there too.

### A Distinct Outcome at the Scanner

`TicketValidationStatusEnum` gained a fourth value, `CANCELLED`, checked ahead of the existing VALID/INVALID validation-history logic in `TicketValidationServiceImpl#validateTicket` rather than folded into it -- a cancelled ticket should read as specifically cancelled to staff at the door, not as `INVALID` (which already means something different: "already used"). The frontend's `/scan` screen picked up a matching fourth full-screen result, slate-grey to sit apart from both the amber "ALREADY USED" (a usage conflict) and the red "NOT FOUND" (an error) it's neither of.

### New Organizer Ticket-Sales Screens

Nothing before this let an organizer see who'd bought tickets to their event, let alone cancel one. Two new read endpoints on `EventController` -- `GET /api/v1/events/{eventId}/tickets` (per-event) and `GET /api/v1/events/tickets` (every event the organizer owns) -- share one `TicketSaleResponseDto`, the same reuse-across-endpoints precedent `VenueResponseDto` set back in "Venue Management", rather than one DTO per endpoint: the per-event screen just doesn't render the `eventName` column the cross-event one needs.

The frontend gained its own `features/ticket-sales/` folder, kept apart from the attendee-scoped `features/tickets/` -- the same reasoning that already kept `published-events` separate from `events`: different backend resource, different auth (organizer-only here). Two routes: `/sales` (cross-event -- named that and not `/tickets`, since the attendee-facing "My Tickets" route already owns that pathless URL, `_attendee/tickets/`) and `/events/$eventId/tickets` (per-event, a sibling of the edit page rather than a dot-nested child of it, the same directory-of-siblings shape `browse/index.tsx`/`browse/$eventId.tsx` already uses). That sibling relationship is what forced `events/$eventId.tsx` to become `events/$eventId/index.tsx` -- a dot-nested `$eventId.tickets.tsx` would have made the edit route an implicit layout it was never meant to be, needing an `<Outlet/>` it doesn't have.

Confirmed live: an organizer can cancel a sold ticket end-to-end from both `/sales` and `/events/{id}/tickets`.

### Propagating Cancellation to `analytics-service`

`ticket_sales` gains a nullable `cancelledAt` column. Cancelling a sale is a plain `UPDATE`, not a delete -- the row, and its original `price`/`purchasedAt`, stays exactly as recorded, just marked cancelled, preserving the full history for a later gross-vs-net breakdown without another schema change:

```typescript
async recordCancellation(event: TicketCancelledEvent): Promise<void> {
  await this.db
    .update(ticketSales)
    .set({ cancelledAt: new Date(event.cancelledAt) })
    .where(eq(ticketSales.ticketId, event.ticketId));
}
```

`getSummaryForEvent` filters cancelled rows out of both `revenue` and `ticketsSold` via `isNull(ticketSales.cancelledAt)`. On the `ticket-service` side, cancellation publishes `ticket.cancelled` onto the same `ticket-platform.events` exchange, through the same in-process-event-then-`AFTER_COMMIT`-listener split "Two Kinds of Event, Not One" already set up for `ticket.purchased`. `analytics-service`'s existing queue just binds a second routing key rather than declaring a second queue, so the consumer's `onModuleInit` now dispatches on `message.fields.routingKey` once a message arrives -- the routing key, not a discriminator field duplicated into the payload, is what tells the two message shapes apart once they land in the same queue, exactly what a topic exchange's routing key is for.

### Attendee and Staff-Facing UI

The attendee ticket-detail page (`_attendee/tickets/$ticketId.tsx`) gets an optional-note textarea and a `ConfirmButton` "Cancel Ticket", shown whenever the ticket isn't already `CANCELLED`. `GetTicketResponse` doesn't expose whether a ticket's already been validated or its event's already completed -- the other two `guardCancellable` checks -- so those two failure modes surface as an error toast on attempt rather than a disabled button up front. The staff scan screen's outcome type and color table both grew the fourth `CANCELLED` case described above.

Confirmed live: an attendee can cancel their own ticket end-to-end from this page.

#### Summary

- `Ticket` gained `cancelledAt`/`cancelReason` (`TicketCancelReasonEnum`: `ATTENDEE_REQUEST`/`ORGANIZER_ACTION`/`EVENT_CANCELLED`)/`cancelNote` -- an audit trail, not a real refund, since no payment gateway exists (Liquibase changeset `15-add-ticket-cancellation`)
- Two cancel endpoints (`TicketController`/`EventController`) share `TicketServiceImpl#guardCancellable`, rejecting (409) an already-cancelled ticket, an already-validated one, or one whose event has already completed; `cancelReason` is always inferred server-side, never client-supplied
- `EventServiceImpl#cancelEvent` now cascades onto every un-validated ticket sold for the event, skipping (not erroring on) any ticket that's already gotten someone in
- A new `countActiveByTicketTypeId` (excludes `CANCELLED`) replaces the old `countByTicketTypeId` in the sold-out check and the `ticketsSold` figure; the old, unfiltered count stays, still guarding issue #12's ticket-type-removal safeguard
- `TicketValidationStatusEnum` gained `CANCELLED`, checked ahead of the existing VALID/INVALID history logic, so staff at the door see a ticket's cancelled specifically, not merely "already used"
- New organizer screens, `/sales` (cross-event) and `/events/$eventId/tickets` (per-event), backed by a new `features/ticket-sales/` folder and a shared `TicketSaleResponseDto`; forced `events/$eventId.tsx` to become `events/$eventId/index.tsx` so the per-event ticket-sales route could live as a sibling file
- `analytics-service` binds its existing queue to a second `ticket.cancelled` routing key on the same exchange, dispatching on the routing key rather than a payload field; `recordCancellation` is an idempotent `UPDATE` that keeps the sale row (not a delete), and `getSummaryForEvent` excludes cancelled rows from revenue/`ticketsSold`
- Confirmed live: attendee self-cancel (`/tickets/{id}`), organizer cancel (`/sales`, `/events/{id}/tickets`), and the event-cancellation cascade (cancelling an event flips its own tickets to `CANCELLED` too) all work end-to-end in the browser. Not yet confirmed live: the scan screen's `CANCELLED` outcome, and whether `analytics-service` actually picks the cancellation up -- see "`frontend`"/"`analytics-service` loose ends" below

## Event Images

Another item flagged out of scope in issue #1's wayfinder map, resurfaced while resolving issue #17 ("Backend: published events filtering + reliable sort"): `Event` had no image field at all, and the `/browse` card grid was explicitly text-only by design. Built after that map closed, the same way PostGIS and ticket cancellation were.

### A New Entity, Not a New Column on Event

An event can have up to eight images, not one -- so this isn't a single `imageUrl` column, it's a real child entity:

```java
@Entity
@Table(name = "event_images")
public class EventImage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventImageGenerator")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "alt_text")
    private String altText;
    // ... createdAt/updatedAt, same as every other entity
}
```

No `filename`/`content-type` column -- every upload gets re-encoded to JPEG on the way in (see below), so the file on disk is always deterministically `{domainId}.jpg`; there's nothing else about it worth storing. `position` is the gallery's display order (0 is the cover image `/browse` shows), and `altText` is organizer-supplied and optional -- never derived from the original filename, which gets discarded entirely. `Event` gained a matching `images: Set<EventImage>` with `cascade = ALL, orphanRemoval = true` and `addImage`/`removeImage` helpers, the exact same shape `ticketTypes` already has.

### One Multipart Request, Not a Separate Upload Endpoint

The first design draft had dedicated `POST`/`DELETE`/`PUT reorder` endpoints under `/events/{eventId}/images`. That got replaced with something more consistent with how this form already works: `createEvent`/`updateEvent` themselves became multipart requests, carrying the image files alongside the same JSON body they always sent. An image entry is one of two shapes, the same "id present = keep, absent = create" pattern `UpdateTicketTypeRequestDto` already established:

```java
public class EventImageRequestDto {
    private UUID id;             // present: keep this existing image
    private Integer newImageIndex; // absent id: index into the request's "newImages" file parts
    private String altText;
}
```

There's no `position` field at all -- an entry's index in the submitted `images` array *is* its gallery position, and there's no separate field for it. `updateEventForOrganizer` diffs the submitted list against what's already there, the same shape the ticket-type diffing right above it in the file already uses:

```java
private void applyImageChanges(Event existingEvent, List<EventImageRequest> requestedImages, List<MultipartFile> newImages) {
    Set<UUID> requestImageDomainIds = requestedImages.stream()
            .map(EventImageRequest::getId).filter(Objects::nonNull).collect(Collectors.toSet());

    Set<EventImage> imagesToRemove = existingEvent.getImages().stream()
            .filter(existingImage -> !requestImageDomainIds.contains(existingImage.getDomainId()))
            .collect(Collectors.toSet());
    for (EventImage imageToRemove : imagesToRemove) {
        eventImageService.deleteImage(imageToRemove.getDomainId());
        existingEvent.removeImage(imageToRemove);
    }

    int position = 0;
    for (EventImageRequest imageRequest : requestedImages) {
        if (null == imageRequest.getId()) {
            // Create -- resolveNewImageFile pulls the actual bytes out of newImages
        } else if (existingImagesIndex.containsKey(imageRequest.getId())) {
            // Keep -- possibly at a new position and/or with new alt text
        } else {
            throw new EventImageNotFoundException(ErrorCode.EVENT_IMAGE_NOT_FOUND, imageRequest.getId());
        }
        position++;
    }
}
```

An existing image not present in the resubmitted list gets its file deleted and the row orphan-removed; resubmitting the same ids in a different order already *is* a reorder, since position is just array index. `createEvent`'s version is simpler -- every entry is necessarily new, since nothing exists yet to keep.

### Resizing, Re-Encoding, and Storage

A new `EventImageService`/`EventImageServiceImpl`, deliberately narrow -- pure filesystem I/O and image processing, no database or entity knowledge at all. Uploads are validated (declared `Content-Type` as a fast-path rejection, a 5MB cap), resized to a 1600px bounding box, and re-encoded to JPEG via a new `thumbnailator` dependency:

```java
Thumbnails.of(file.getInputStream())
        .size(MAX_DIMENSION, MAX_DIMENSION)
        .outputFormat("jpg")
        .toFile(target.toFile());
```

The declared-header check is only a fast path -- the real content-sniffing check is Thumbnailator itself failing to decode bytes that aren't actually an image, regardless of what a spoofed header claimed. Storage is the local filesystem, at a path from a new `app.event-images.storage-dir` property (defaulting to `./data/event-images`) -- matching this project's "no deployment plan, local dev only" scope, the same reasoning that put PostGIS on this same bare-metal host rather than a managed service.

### Two Raw-Bytes Endpoints, Mirroring the Existing Public/Organizer Split

Serving the bytes back out needed the same public/organizer duality `EventController`/`PublishedEventController` already have for event data itself: `GET /api/v1/events/{eventId}/images/{imageId}` (organizer-only, works on a still-DRAFT event, for the edit page's own gallery) and `GET /api/v1/published-events/{eventId}/images/{imageId}` (public, only ever resolves an image belonging to a `PUBLISHED` event). No `SecurityConfig` change needed for either -- both already fall under existing wildcard rules. Response DTOs (`GetEventDetailsResponseDto`, `GetPublishedEventDetailsResponseDto`, `ListPublishedEventResponseDto`'s `coverImageId`) carry only `{id, altText}` per image, no `url` field -- the frontend already knows how to build a fetch URL from an id it has, the same precedent the ticket QR code endpoint already set.

### Frontend: EventForm Becomes a Two-Step Form

`EventForm` gained a local `step` state: step 1 is every field it already had, step 2 is a new `EventImageGallery`. Switching steps is a pure view toggle -- it never touches the server, and it's independent of Save as Draft/Publish/Save Changes, which stay visible on both steps and submit the whole form regardless of which one is currently showing. Nothing about images hits the backend until that submit: picking a file just appends a row holding the raw `File` object to a `useFieldArray`, and removing an existing row just drops it from the array. Reordering is drag-and-drop via `@dnd-kit` (a new dependency -- nothing in this frontend did drag-and-drop before), keyed off `useFieldArray`'s own generated `field.id` rather than a redundant custom key.

### Frontend: Building the Multipart Request

`formValuesToRequest` now returns `{ request, newImageFiles }` instead of just a request body -- it walks the staged `images` array once, turning a row with a `File` into `{newImageIndex, altText}` (pushing the file onto `newImageFiles`) and a row without one into `{id, altText}`. `api.ts`'s `createEvent`/`updateEvent` build a `FormData` from that pair:

```typescript
const formData = new FormData()
formData.append('event', new Blob([JSON.stringify(request)], { type: 'application/json' }))
newImageFiles.forEach((file) => formData.append('newImages', file))
```

The JSON part has to be a `Blob` with an explicit `application/json` type, not a raw string -- a raw string part arrives at Spring tagged `text/plain`, which `@RequestPart` can't deserialize into the DTO. An existing image's thumbnail on the edit page is fetched authenticated (`getEventImage` + a new `useEventImage` hook, the same `Blob` + `useObjectUrl` shape the ticket QR code image already uses); a public image is just a plain `<img src>` pointing at `publishedEventImageUrl(eventId, imageId)`, no fetch wrapper needed since that endpoint requires no auth.

Confirmed working live: creating and editing an event with images, and seeing them show up on the `/browse` card grid and the event detail page.

#### Summary

- New `EventImage` entity (`id`/`domainId`/`event`/`position`/`altText`), up to 8 per event, `cascade = ALL, orphanRemoval = true` on `Event.images` -- no new column on `Event` itself
- `createEvent`/`updateEvent` became multipart requests carrying image files alongside the existing JSON body, rather than a separate upload/delete/reorder endpoint trio; images are diffed create/keep/delete-by-`id` the same way `ticketTypes` already are, with array order doubling as gallery position
- Uploads are resized to a 1600px bounding box and re-encoded to JPEG via a new `thumbnailator` dependency; the real content-type check is Thumbnailator failing to decode non-image bytes, not just trusting the declared header
- Local filesystem storage (`app.event-images.storage-dir`), matching this project's local-dev-only scope
- Two raw-bytes GET endpoints mirror the existing `EventController`/`PublishedEventController` organizer/public split; no `SecurityConfig` changes needed
- `EventForm` became a two-step form (core fields, then an image gallery), with `@dnd-kit` powering drag-to-reorder; nothing about images hits the server until the form's real submit button is clicked
- Confirmed working live end-to-end: creating/editing an event with images, and seeing them on both the `/browse` card grid and the event detail page

## Project Status

A snapshot of where the real build stands relative to this document, kept here as a running reference rather than a lesson -- update it as items get resolved.

### Frontend: every screen on the wayfinder map is built

`frontend/` is a real, running TanStack Start app with Tailwind, shadcn/ui, and React Query wired in, a working login against Keycloak (session-storage-backed tokens, refresh-token silent renewal, role-guarded routing for organizer/attendee/staff), and a shared `apiFetch()` wrapper every feature calls into. Every feature area on the wayfinder map (issue #1) is now built, not a placeholder:

- **Venues** (organizer, `/venues`) -- list/create/edit, backed by `react-hook-form` + `zod` and `ticket-service`'s `/api/v1/venues`. The list is genuinely paginated (`PaginatedTable`/`PaginationControls`, not the size=100-stands-in-for-everything trick it started with), and the venue count in an event form's picker is now a searchable, infinite-scrolling combobox (`VenueCombobox`, debounced search + `useInfiniteQuery`) rather than a single capped page.
- **Events** (organizer, `/events`) -- list/create/edit with the full `DRAFT -> PUBLISHED -> {CANCELLED|COMPLETED}` lifecycle: the create page has separate "Save as Draft"/"Publish" actions (publish is a second, chained API call, not a status field); the edit page's action row is status-conditional (Draft: Publish + Delete-with-confirmation; Published: Complete + Cancel-with-confirmation; terminal: the whole form renders read-only, matching the backend rejecting any update on a terminal event). `EventForm`'s ticket-type rows are a real `useFieldArray`, with a "Limited quantity" toggle controlling whether `totalAvailable` is sent at all. `EventForm` is now a two-step form (see "Event Images") -- step 2 is an up-to-8-image gallery with drag-to-reorder, staged locally until the same submit that saves everything else.
- **Published events browse** (public, `/browse` -- deliberately *not* behind the `_attendee` login guard, since the backend endpoint never required one) -- debounced search, Date/Price/City/Sort filter selects, a "Use my location" control (URL-persisted `lat`/`lng`, a radius preset dropdown, a "Nearest" sort option once active), an event-card grid (each card showing its cover image, once event images shipped -- see "Event Images"), and *numbered* pagination (a second, distinct pagination component from the organizer tables' Previous/Next one, per the resolved UX design), plus an event detail page listing ticket types and its own image gallery.
- **Venue location picker** -- `VenueForm` gained a Leaflet + OpenStreetMap click-to-place-pin map alongside the existing latitude/longitude number inputs, per issue #7's own notes flagging this as cheap to add once PostGIS existed.
- **Purchase flow** (attendee, from `/browse/$eventId`) -- a quantity selector and Buy button per ticket type, looping the single-ticket purchase endpoint sequentially with live "Purchasing... (X of Y)" progress, landing on a dedicated Success/Partial confirmation page (`/browse/confirmation`) per issues #4/#5.
- **My Tickets** (attendee, `/tickets`) -- a real list/detail pair (`_attendee/tickets/{index,$ticketId}.tsx`) replacing the old auth-check placeholder; the detail page renders the purchased ticket's QR code image and reference code, plus (once ticket cancellation shipped -- see "Ticket Cancellation & Refund") an optional-note self-cancel action.
- **Staff ticket validation** (staff, `/scan`) -- continuous camera QR scanning (`qr-scanner`, Web Worker-based) plus a manual reference-code fallback, both funnelling into one full-screen colored result per issue #9 -- ADMIT/ALREADY-USED/NOT-FOUND, plus a fourth CANCELLED outcome once ticket cancellation shipped.
- **Reports dashboard** (organizer, `/dashboard`) -- a sorted, single-hue horizontal bar chart of revenue by event, each bar direct-labeled with both revenue and tickets sold, per issue #8.
- **Ticket sales & cancellation** (organizer, `/sales` cross-event, `/events/{id}/tickets` per-event) -- paginated tables of every ticket sold, with an inline per-row Cancel action; not part of the original wayfinder map, built afterward alongside the rest of ticket cancellation -- see "Ticket Cancellation & Refund".

Deliberately not built, matching resolved decisions rather than gaps: `i18next` and translation files still aren't installed -- issue #11 explicitly decided i18n "lands alongside whichever feature first needs it," and nothing has needed it yet, so `apiFetch()` forwarding `Accept-Language` remains the only i18n-adjacent code that exists. Everything else called out as out of scope in the wayfinder map (payment gateway integration, automated tests, etc.) remains exactly that -- see "Explicitly out of scope, not oversights" below.

PostGIS proximity search is now built end to end -- backend geo filter (see "Backend: PostGIS Proximity Search"), the `/browse` "Use my location" control, and the `VenueForm` map picker (see "Frontend: Near Me Search" and "Frontend: Venue Location Picker"). The one piece still missing is a per-card distance readout ("2.3km away") -- the response DTO doesn't carry a computed distance value, which would mean restructuring the native queries into a projection instead of returning `Event` entities directly. Left for whenever that's actually wanted; the filter and "Nearest" sort work without it.

Ticket cancellation/refund (also originally out of scope) is now built too -- see "Ticket Cancellation & Refund" above for the full build: both an attendee self-cancel and an organizer-cancel path, an event-cancellation cascade onto its own tickets, freed-up inventory, a distinct scanner outcome, and `analytics-service` excluding cancelled sales from reporting. There's still no real payment gateway, so this is audit-trail bookkeeping (`cancelledAt`/`cancelReason`/`cancelNote`), not money movement -- that half of "cancellation/refund" remains genuinely out of scope.

Event images (also originally out of scope, flagged while resolving issue #17) are built too -- see "Event Images" above: a real `EventImage` gallery (up to 8 per event, resized/re-encoded on upload, local filesystem storage), `createEvent`/`updateEvent` carrying the files as part of one multipart request rather than a separate upload endpoint, and both the `/browse` card grid and the event detail page rendering real images now.

### `frontend` loose ends

- No automated tests, matching the rest of this project's all-manual-verification pattern
- The full login round-trip, Venues, Events, the public browse/search page, and the purchase-to-confirmation flow are all confirmed working live in the browser against the running `ticket-service` -- a real ticket was bought and landed on the Success confirmation page during this build
- My Tickets (list/detail/QR), staff scanning, and the reports dashboard are built and pass `tsc --noEmit`/`npm run lint`/`npm run build` (the staff screen's `npm run build` specifically confirmed `qr-scanner`'s Web Worker split into its own chunk, not just that the types checked out), but haven't yet been confirmed live in a browser -- no headless-browser tooling was wired into this build, so anything not explicitly called out as browser-tested above is verified by build/type/lint checks only, not by clicking through it
- The browse page's date filters (including the default "upcoming-only" one) are all relative to *now* -- there's no filter that can surface a published event whose start date has already passed. Discovered by testing against a real event whose date had rolled into the past; left as-is since it matches the resolved design (nobody browsing to buy a ticket wants an event that already happened), not treated as a gap to close
- The City filter on `/browse` intentionally does not call `/api/v1/venues` (organizer-only) -- it hits a new public `GET /api/v1/published-events/cities` endpoint instead. An earlier version called the organizer endpoint, which 401'd for anyone not logged in as an organizer and (via `apiFetch`'s automatic 401-handling) silently redirected an anonymous visitor straight to the Keycloak login screen on page load
- `GetTicketResponse` (the ticket detail DTO) has no ticket-type name field, only description/price -- found while building the My Tickets detail page, not fixed, since it's the backend's existing shape and wasn't reported as a bug
- The reports dashboard fetches one analytics summary per event (`useEventAnalyticsSummaries`, N requests) rather than one bulk call, since analytics-service has no organizer-wide rollup endpoint -- an accepted tradeoff against the endpoint that actually exists, not an oversight
- A real navigation gap was found and fixed by actually using the app as an organizer: no page linked back to `/dashboard`/`/venues`/`/events` once you'd left for `/browse`. Role-aware links now live in the global `Header.tsx`; `_organizer.tsx`'s now-redundant layout-local nav was removed
- The `/browse` "Use my location" control and the `VenueForm` map picker are both confirmed working live in the browser. A user-reported "wrong location" result along the way was diagnosed down to the browser's own network-based geolocation returning an inaccurate fix (confirmed by testing the exact reported coordinates directly against the database), not a bug in the filter itself
- Attendee self-cancel (`/tickets/{id}`), organizer cancel (`/sales`, `/events/{id}/tickets`), and the event-cancellation cascade (cancelling a whole event and watching its tickets flip to CANCELLED too) are all confirmed working live in the browser. The staff scan screen's new CANCELLED outcome hasn't been triggered against a real cancelled ticket yet
- Event images (see "Event Images") -- creating/editing an event with images, drag-to-reorder, and both the `/browse` card grid and the event detail page rendering them are all confirmed working live in the browser

### `ticket-service` loose ends

- No automated tests anywhere (unit or integration) -- everything's been verified manually so far, via `bootRun` plus `psql`/`curl`/browser checks, not a test suite
- Keycloak now has all three users (`organizer`, `attendee`, `staff`), so the purchase flow and ticket validation flow both have accounts to test with
- CORS was never configured until the frontend's Venues screen needed it (see "A Missing CORS Configuration") -- `SecurityConfig` now allows `http://localhost:3000` across `/**`
- Hit the same PostgreSQL failure mode twice while wiring up search: a bind parameter used *only* in an `X IS NULL OR ...` comparison (the standard "optional filter" pattern) gives Postgres zero type context to resolve it against, since `IS NULL` is valid for any type. Depending on the rest of the query, this either silently resolves to the wrong type (`VenueRepository.search`'s city `LIKE` picked `bytea`, so `LOWER(bytea)` blew up) or fails outright ("could not determine data type of parameter", `EventRepository`'s published-events native query). Both are fixed the same way: wrap every occurrence of the parameter in an explicit `CAST(:param AS <type>)`. Worth remembering as a standing gotcha for any future nullable-filter query in this stack, not just those two
- `VenueRepository`/`VenueController`/`VenueService` gained search (`?q=`), and `EventRepository`/`EventService`/`PublishedEventController` gained a `GET /api/v1/published-events/cities` endpoint (distinct cities with at least one published event) -- both purely additive, no existing endpoint's behavior changed
- PostGIS is now installed and enabled on the live database (`postgis` 3.5.3 -- see "Backend: PostGIS Proximity Search" for what installing it on this specific host actually took); `venues.location` is backfilled and kept in sync via `Venue.setCoordinates()`, and published-events search accepts a geo origin + radius plus a `distance` sort, all live-tested against the one real published event
- `listPublishedEvents` is now `POST /api/v1/published-events/search` with a JSON body (`ListPublishedEventsRequestDto extends SearchRequestDTO`), not `GET` with query params -- needed its own `permitAll` rule in `SecurityConfig` (the existing one only matched `GET`) and a matching frontend update, both done in the same pass so the browse page never broke
- Discovered, not fixed: hitting a genuinely nonexistent route (like the now-removed `GET /api/v1/published-events`) returns a generic `500` (`{"error":"An unexpected error occurred"}`) instead of a proper `404` -- `GlobalExceptionHandler` catches `NoResourceFoundException` the same as any other unhandled exception. Pre-existing for any mistyped URL on this API, not introduced by this change
- Ticket cancellation (see "Ticket Cancellation & Refund") added a new Liquibase changeset (`004-add-ticket-cancellation.xml`, `15-add-ticket-cancellation`) and four new endpoints across `TicketController`/`EventController` -- purely additive; the only existing behavior it changes is the sold-out check and the `ticketsSold` figure now excluding cancelled tickets. Both direct cancel endpoints and `EventServiceImpl#cancelEvent`'s cascade onto its own tickets are confirmed working live
- Event images (see "Event Images") added a new Liquibase changeset (`005-add-event-images.xml`, `16-add-event-images`), a new `thumbnailator` dependency, and changed `createEvent`/`updateEvent` from JSON to multipart requests -- the one existing-behavior change of consequence, since every other client of those two endpoints needs to send multipart now too. Confirmed working live end-to-end

### `analytics-service` loose ends

- A message has never actually been watched flowing publish → consume → DB row end-to-end -- the consumer's setup (exchange/queue/binding) is confirmed via the RabbitMQ management API, but a live message hasn't been traced through `recordSale` yet; this is expected to get exercised naturally once real purchases are flowing through `ticket-service`
- The reporting endpoint is now called for real by the frontend's `/dashboard` (see "Frontend Reports Dashboard"), but that screen hasn't been confirmed live in a browser yet -- so it's still only been directly confirmed rejecting an unauthenticated/bad-token request (`401`), not returning real summary data end-to-end through the UI
- Ticket cancellation (see "Ticket Cancellation & Refund") added a second `ticket.cancelled` binding to the existing queue and a nullable `cancelled_at` column on `ticket_sales` (migration `0002_lean_black_tarantula`); like the original `ticket.purchased` flow, a cancellation hasn't been watched flowing publish -> consume -> DB row end-to-end yet either
- No automated tests

### Explicitly out of scope, not oversights

These are called out directly in the relevant lessons as deliberate simplifications, not gaps to close by accident:

- Dead-lettering failed RabbitMQ messages (currently dropped with a log line -- see "Wire Up the Consumer")
- The transactional outbox pattern, for the narrow crash-between-commit-and-publish window (see "Two Kinds of Event, Not One")
- Additional reporting endpoints beyond the one summary endpoint -- sales-over-time, organizer-level rollups (see "Expose the Reporting API")
- A `notifications-service` third consumer on the `ticket-platform.events` exchange
- Any way to browse a published event whose date has already passed -- every date filter on `/browse` (including the default) is upcoming-relative-to-now by design, not an oversight in the filter UI
