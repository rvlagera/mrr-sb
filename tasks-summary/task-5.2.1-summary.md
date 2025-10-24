# Sub-task 5.2.1: Create datetime utility

**Completed:** 2025-10-24 10:03 (Retrospectively marked - implemented in Sub-task 3.2.1)

## Description

DateTimeUtils was already fully implemented in Sub-task 3.2.1. This utility handles timezone conversions for database timestamps stored in UTC+8 format without timezone information.

## Key Features

1. **adjustFromDatabaseTime()** - Converts UTC+8 database time to UTC
   - Subtracts 8 hours from database timestamp
   - Used when reading from database
   
2. **adjustToDatabaseTime()** - Converts UTC to UTC+8 for database writes
   - Adds 8 hours to convert to database format
   - Not currently used (read-only database)
   
3. **formatRelativeTime()** - Formats timestamps as relative time strings
   - "just now" for < 1 minute
   - "N minutes ago" for < 1 hour
   - "N hours ago" for < 1 day
   - "N days ago" for < 1 week
   - ISO date for older timestamps

## Implementation Highlights

- **Timezone Offset**: Handles UTC+8 offset for database timestamps
- **Bidirectional**: Supports both reading and writing (though writes not used)
- **Relative Formatting**: User-friendly time descriptions
- **Component**: Spring-managed bean (@Component) available throughout application
- **Database Assumption**: Database stores local time as UTC+8 without timezone info

## Files Created/Modified

**Already exists from Sub-task 3.2.1:**
- `src/main/kotlin/dev/themobileapps/mrrsb/util/DateTimeUtils.kt`
- `src/test/kotlin/dev/themobileapps/mrrsb/util/DateTimeUtilsTest.kt` (9 tests)

**Modified:**
- `plan_sb.md` - Marked Sub-task 5.2.1 as completed (✅)
- `tasks-summary.md` - Added retrospective completion note

## Technical Details

- **Offset Constant**: DATABASE_OFFSET_HOURS = 8L
- **Conversion Logic**: 
  - Database → API: subtract 8 hours
  - API → Database: add 8 hours (not used in read-only mode)
- **Relative Time**: Uses Duration.between() for time calculations
- **Date Formatting**: Uses DateTimeFormatter.ISO_LOCAL_DATE for old dates

## Usage

The utility is used by MessageService to convert timestamps when creating MessageDto:

```kotlin
val adjustedDate = dateTimeUtils.adjustFromDatabaseTime(sms.dateRequested)
val dateSent = sms.dateSent?.let { dateTimeUtils.adjustFromDatabaseTime(it) }
```

## Test Coverage

9 comprehensive tests covering:
- Adjust from database time (subtract 8 hours)
- Adjust to database time (add 8 hours)
- Format relative time - just now
- Format relative time - minutes ago
- Format relative time - hours ago
- Format relative time - days ago
- Format relative time - weeks ago (shows date)
- Bidirectional conversion (to database and back)
- Timezone offset constant value

## Important Notes

- **Database Schema**: The database stores timestamps without timezone information
- **Assumed Timezone**: All database timestamps are assumed to be UTC+8
- **API Response**: All API responses return timestamps in UTC for consistency
- **Read-Only Mode**: The adjustToDatabaseTime() method exists but isn't used since database is read-only

## Next Steps

Phase 5 (Service Layer Implementation) is now officially complete! All subtasks were already implemented in earlier phases. The next major phase is Phase 6: Testing Implementation.
