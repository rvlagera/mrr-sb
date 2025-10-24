# Sub-task 1.2.1: Create JPA Entities

**Completed:** 2025-10-24 08:18:45

## Overview

Created JPA entity classes to map the existing PostgreSQL database schema for Person and OutboundSms tables with proper annotations, relationships, and column mappings.

## What Was Done

### 1. Person Entity

Created `Person.kt` entity class with the following features:

#### Table Mapping
- Maps to `person` table in database
- Uses `@Entity` and `@Table` annotations

#### Fields
- **personId**: Primary key (maps to `id` column)
  - Auto-generated using IDENTITY strategy
  - Type: Int, default value 0

- **username**: User's login name
  - Unique, not null
  - Max length: 100 characters

- **password**: User's password
  - Max length: 255 characters (supports BCrypt hashes)

- **name**: User's display name
  - Not null
  - Max length: 200 characters

- **mobileNo**: User's mobile number
  - Nullable (optional)
  - Max length: 20 characters

- **active**: User account status
  - Default: true
  - Type: Boolean

- **dateCreated**: Account creation timestamp
  - Nullable
  - Type: LocalDateTime

- **outboundMessages**: Related SMS messages
  - One-to-many relationship with OutboundSms
  - Lazy loaded
  - Default: empty list

### 2. OutboundSms Entity

Created `OutboundSms.kt` entity class with the following features:

#### Table Mapping
- Maps to `outbound_sms` table in database
- Uses `@Entity` and `@Table` annotations

#### Fields
- **smsId**: Primary key (maps to `id` column)
  - Auto-generated using IDENTITY strategy
  - Type: Int, default value 0

- **person**: Related user
  - Many-to-one relationship with Person
  - Lazy loaded
  - Join column: person_id
  - Nullable

- **message**: SMS message content
  - TEXT column type (unlimited length)
  - Not null

- **alertLevel**: Message priority/severity
  - Type: Int, default value 0
  - 0 = normal, higher = more urgent

- **dateRequested**: When message was created
  - Not null
  - Type: LocalDateTime

- **dateSent**: When message was sent
  - Nullable (null = not sent yet)
  - Type: LocalDateTime

- **mobileNo**: Recipient's mobile number
  - Not null
  - Max length: 20 characters

- **status**: Message status
  - Not null
  - Max length: 50 characters
  - Examples: PENDING, SENT, FAILED

- **requestedBy**: Who created the message
  - Not null
  - Max length: 100 characters

### 3. Entity Relationships

Established bidirectional relationship between entities:

- **Person → OutboundSms**: One-to-many
  - A person can have multiple messages
  - Mapped by "person" field in OutboundSms
  - Lazy loaded to prevent N+1 queries

- **OutboundSms → Person**: Many-to-one
  - A message belongs to one person
  - Foreign key: person_id
  - Lazy loaded to prevent N+1 queries

### 4. Comprehensive Testing

Created test classes to verify entity behavior:

#### PersonEntityTest.kt (3 tests)
- ✅ Should create Person entity with required fields
- ✅ Should create Person with default values
- ✅ Should support inactive user

#### OutboundSmsEntityTest.kt (5 tests)
- ✅ Should create OutboundSms entity with required fields
- ✅ Should create OutboundSms with default alert level
- ✅ Should support sent SMS with dateSent
- ✅ Should support high alert level messages
- ✅ Should associate OutboundSms with Person

**Total: 8 entity tests + 1 application context test = 9 tests, all passing**

## Files Created

1. **src/main/kotlin/dev/themobileapps/mrrsb/entity/Person.kt**
   - JPA entity for person table
   - 7 fields + 1 relationship
   - Proper annotations and defaults

2. **src/main/kotlin/dev/themobileapps/mrrsb/entity/OutboundSms.kt**
   - JPA entity for outbound_sms table
   - 8 fields + 1 relationship
   - TEXT column for message content

3. **src/test/kotlin/dev/themobileapps/mrrsb/entity/PersonEntityTest.kt**
   - Unit tests for Person entity
   - 3 test cases

4. **src/test/kotlin/dev/themobileapps/mrrsb/entity/OutboundSmsEntityTest.kt**
   - Unit tests for OutboundSms entity
   - 5 test cases

## Files Modified

- `plan_sb.md` - Marked Sub-task 1.2.1 as completed (✅)
- `tasks-summary.md` - Added entry for Sub-task 1.2.1

## Key Technical Decisions

### 1. Data Classes
- Used Kotlin data classes for automatic equals(), hashCode(), toString()
- Simplifies entity usage and debugging

### 2. Nullable Types
- Kotlin's nullable types (?) match database nullable columns
- Compile-time null safety

### 3. Default Values
- Provided sensible defaults (active=true, alertLevel=0)
- Simplifies entity creation in code

### 4. Lazy Loading
- All relationships use FetchType.LAZY
- Prevents performance issues from eager loading
- Developers must explicitly fetch related data

### 5. Column Definitions
- Explicit column names for clarity
- Length constraints match database schema
- TEXT column type for message content

## Important Notes for Future Tasks

### Database Schema Alignment
- Entities match existing PostgreSQL schema exactly
- No schema changes will be made (ddl-auto=none)
- All column names explicitly mapped

### ID Generation
- IDENTITY strategy used (PostgreSQL SERIAL)
- Database auto-generates IDs on insert
- Default value of 0 indicates unsaved entity

### Relationship Loading
- Always use JOIN FETCH in queries when related data needed
- Prevents N+1 query problems
- Lazy loading requires active transaction

### Password Storage
- password field supports both plain text and BCrypt
- Max length 255 supports BCrypt hashes ($2a$10$...)
- Migration strategy: accept both, gradually encrypt

### Timestamp Handling
- LocalDateTime used for all timestamps
- No timezone information in entity
- DateTimeUtils will handle UTC+8 conversion

## Next Steps

The next subtask (1.2.2) will create JPA repositories:
- PersonRepository with custom queries
- OutboundSmsRepository with pagination and filtering
- Query methods for common operations
- Integration with Spring Data JPA
