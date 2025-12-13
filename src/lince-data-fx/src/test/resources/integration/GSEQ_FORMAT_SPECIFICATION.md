# GSEQ File Format Specification

## Overview

GSEQ (Generalized Sequential Querier) is a software package for analyzing sequential data. This document specifies the format requirements for exporting observational data to GSEQ-compatible `.sds` files from the Lince application.

**Document Version**: 1.0
**Last Updated**: 2025-01-24
**Reference Implementation**: Lince PLUS v4.0+

## Table of Contents

1. [Common Format Elements](#common-format-elements)
2. [Format Types](#format-types)
   - [Event Format](#1-event-format)
   - [Multievent Format](#2-multievent-format)
   - [Timed Event Format](#3-timed-event-format)
   - [State Format](#4-state-format)
   - [Interval Format](#5-interval-format)
3. [Implementation Evaluation](#implementation-evaluation)
4. [Test Requirements](#test-requirements)

---

## Common Format Elements

All GSEQ formats share the following structural elements:

### 1. Line Endings
- **Requirement**: Windows-style CRLF (`\r\n`)
- **Constant**: `LINE_SEPARATOR = "\r\n"`

### 2. File Extension
- **Requirement**: `.sds` (Sequential Data Source)

### 3. Format Declaration
- **Location**: First line of file
- **Format**: Single keyword identifying the data type
- **Examples**: `Event`, `Multievent`, `Timed`, `State`, `Interval=N'`

### 4. Variable Declarations
Two distinct formats depending on GSEQ type:

#### Format A (Event, Multievent, Timed, State)
```
($CriterionName = code1 code2 code3);
```
- Parentheses wrap criterion declaration
- Dollar sign prefix before criterion name
- Space-separated codes
- Semicolon terminates all declarations
- Empty line after semicolon

**Example**:
```
Event

($Paso = PP RS PT AR1 MR E PE)
($Adaptacion = A NA)
($Exito = L NL);

```

#### Format B (Interval)
```
  fullname = code
```
- Two-space indentation
- Lowercase full name (description or name field)
- One declaration per line
- Followed by metadata line

**Example**:
```
Interval=10'
  ioffer = io
  iplay = ip
  ireceive = ir
* Age (3yr 5yr) Place (Home Outdoors);
```

### 5. Metadata Line
- **Format**: `* metadata;` or `* Group1 (val1 val2) Group2 (val3 val4);`
- **Optional**: Required for Interval, optional for others
- **Purpose**: Defines grouping variables and their values

### 6. End Marker
- **Requirement**: Forward slash `/`
- **Location**: End of data section
- **Line Ending**: Followed by `\r\n`

---

## Format Types

### 1. Event Format

**Purpose**: Records discrete events in sequence without timing information.

#### Structure
```
Event

($CriterionName = code1 code2 ...);


[code1]
[code2]
[code3];

[code4]
[code5]/

```

#### Specifications

| Element | Requirement | Example |
|---------|------------|---------|
| Header | `Event` | `Event` |
| Variable Format | Format A | `($SC = PP RS PT);` |
| Data Format | One code per line | `RS\r\nPT\r\nAR1` |
| Sequence Separator | Semicolon + blank line | `PE;\r\n\r\nRS` |
| End Marker | `/` with newlines | `/\r\n` |

#### Data Section Rules
1. Each observation on its own line
2. Empty lines allowed for readability
3. Sequences terminated with semicolon
4. Multiple sequences allowed

#### Example
```
Event

($SC = PP RS PT AR1 MR E PE);


RS
PT
AR1
PE;

RS
PT/

```

---

### 2. Multievent Format

**Purpose**: Records multiple simultaneous events occurring together in sequences.

#### Structure
```
Multievent

($CriterionName = code1 code2 ...);


[code1 code2].
[code3].
[code4 code5];

[code6]/

```

#### Specifications

| Element | Requirement | Example |
|---------|------------|---------|
| Header | `Multievent` | `Multievent` |
| Variable Format | Format A | `($Paso = RS PT);` |
| Data Format | Codes on same line = simultaneous | `RS A.` |
| Line Separator | Period (.) | `.` |
| Sequence Separator | Semicolon (;) | `;` |
| End Marker | `/` with double newlines | `/\r\n\r\n` |

#### Data Section Rules
1. Codes on same line separated by space indicate simultaneous events
2. Each line ends with period (`.`) except last line in sequence
3. Last line in sequence ends with semicolon (`;`)
4. Multiple sequences separated by blank line

#### Example
```
Multievent

($Paso = PP RS PT AR1)
($Adaptacion = A NA);


RS A.
PT A.
AR1 A;

RS NA/

```

---

### 3. Timed Event Format

**Purpose**: Records events with precise start and end times.

#### Structure
```
Timed
($CriterionName = FullName = code ...)
...
* Grouping (val1 val2);

<Session Label>

code,MM:SS-MM:SS code,MM:SS-
...
&
code,MM:SS- ...
&
...,MM:SS;

<Next Session>
.../
```

#### Specifications

| Element | Requirement | Example |
|---------|------------|---------|
| Header | `Timed` | `Timed` |
| Variable Format | Format A with descriptions | `($Mother = MPassive = mp ...)` |
| Time Format | `MM:SS` or `HH:MM:SS` | `01:07` or `22:34` |
| Range Separator | Dash (`-`) | `01:07-02:30` |
| Open-ended | Dash without end time | `01:07-` |
| Line Separator | Space | ` ` |
| Subject Separator | Ampersand (`&`) | `&` |
| Sequence Separator | Semicolon (`;`) | `;` |
| Session Marker | `<Label>` | `<Dyad Low 1>` |

#### Data Section Rules
1. Each line represents one subject/variable
2. Format: `code,start-end code,start-end ...`
3. Open-ended intervals use dash without end time
4. Subject data separated by `&`
5. Sessions separated by blank line
6. Final time can be standalone: `,MM:SS`

#### Example
```
Timed
($Mother = MActive = ma)
* Skill (Low High);

<Dyad Low 1>

ma,01:07-02:27 ma,03:33-
&
ia,00:00-01:25 ia,01:44-,22:45;

ma,00:00-00:19/
```

---

### 4. State Format

**Purpose**: Records state changes with durations.

#### Structure
```
State <units>
   (code1 code2) (code3 code4)
   * grouping (val1 val2);

<SessionID>,start code,time code,time ...
&
...
(metadata)/
```

#### Specifications

| Element | Requirement | Example |
|---------|------------|---------|
| Header | `State <units>` | `State <seconds>` |
| Variable Format | Grouped in parentheses | `(A P N) (GM GR)` |
| Time Format | `M:SS` or `MM:SS` | `0:30` or `12:45` |
| Start Time | First entry with `,start` | `,0:30 P` |
| State Changes | `code,time` | `A,1:44` |
| Subject Separator | Ampersand (`&`) | `&` |
| Session ID | `<ID>` | `<60606IA66>` |
| End Time | `,MM:SS` without code | `,6:30` |
| Session End | `(metadata)/` | `(male 6mo)/` |

#### Data Section Rules
1. States are mutually exclusive within a group
2. Only one state active at a time per group
3. Duration calculated from state changes
4. Initial time must be specified
5. Multiple subjects per session separated by `&`
6. Metadata in parentheses before `/`

#### Example
```
State <seconds>
   (A P N)
   * age (6mo);

<S01>,0:30 P,0:36 A,1:44 N,2:00 &
%<S01>%M,0:30 ,6:30 (6mo)/
```

---

### 5. Interval Format

**Purpose**: Records events occurring within fixed time intervals with time offsets.

#### Structure
```
Interval=N'
  fullname = code
  ...
* Grouping (val1 val2);

<Session>

*offset,code,*offset,code,...,*offset;

*offset,code.../
```

#### Specifications

| Element | Requirement | Example |
|---------|------------|---------|
| Header | `Interval=N'` where N is interval duration | `Interval=10'` |
| Variable Format | Format B (indented) | `  ioffer = io` |
| Offset Format | `*N` where N is time units | `*10` |
| Data Separator | Comma (`,`) | `,` |
| Simultaneous Codes | Space-separated | `io oa` |
| Empty Interval | Double comma (`,,`) | `,,` |
| Repetition | `code*N` | `ir*2` (ir repeated 2 times) |
| Sequence End | Semicolon (`;`) | `;` |
| Session Marker | `<SNN>` | `<S01>` |
| Session End | `(metadata)/` | `(3yr Home)/` |

#### Data Section Rules
1. `*N` indicates N time units elapsed since last event (or start)
2. Codes separated by comma represent sequential observations
3. Codes separated by space on same position are simultaneous
4. Empty observations represented by omitting code (double comma)
5. Repetition indicated by `*N` suffix after code
6. All data on continuous lines (newlines for readability only)
7. Session ends with metadata in parentheses

#### Example
```
Interval=10'
  ioffer = io
  ireceive = ir
* Age (3yr);

<S01>

*10,ir,*15,ir,*21,oo ir,*28,ir;

*2,ir,*3,ir/
```

---

## Implementation Evaluation

### Current Implementation Status

| Format | Implementation File | Status | Issues |
|--------|-------------------|--------|--------|
| Event | `exportToSdisGseqEvento` | ⚠️ Partial | Missing sequence separators |
| Multievent | `exportToSdisGseqMultievento` | ✅ Fixed | Format corrected |
| Timed | `exportToSdisGseqEventoConTiempo` | ⚠️ Needs Review | Time format verification needed |
| State | `exportToSdisGseqEstado` | ⚠️ Needs Review | Duration calculation needs verification |
| Interval | `exportToSdisGseqIntervalo` | ✅ Fixed | Format corrected, interval calculation added |

### Critical Issues Found

#### 1. Variable Declaration Format
- **Issue**: Interval format was using old-style declarations
- **Fix**: Created separate `exportToSdisGseqOldFormat()` and `exportToSdisGseq()` methods
- **Status**: ✅ Resolved

#### 2. Timing Placement (Interval)
- **Issue**: Timing markers appeared after data instead of before
- **Expected**: `*10,data1,*15,data2`
- **Actual**: `data1*15,data2*21`
- **Status**: ✅ Resolved

#### 3. Line Ending Constants
- **Issue**: Hardcoded `\r\n` throughout codebase
- **Fix**: Added `LINE_SEPARATOR` and `SEQUENCE_END` constants
- **Status**: ✅ Resolved

#### 4. Multievent Format
- **Issue**: Incorrect period/semicolon placement
- **Expected**: `data.` for non-last, `data;` for last
- **Actual**: Timing issues and wrong separators
- **Status**: ✅ Resolved

### Recommendations

1. **Code Organization**
   - ✅ Consolidate constants in base class
   - ✅ Separate variable declaration methods by format type
   - ⚠️ Create abstract base class for GSEQ exports
   - ⚠️ Implement format validators

2. **Testing**
   - ⚠️ Add integration tests comparing output to valid files
   - ⚠️ Add unit tests for each format type
   - ⚠️ Add edge case tests (empty data, single observation, etc.)

3. **Documentation**
   - ✅ Document format specifications
   - ⚠️ Add JavaDoc comments explaining format requirements
   - ⚠️ Include examples in method documentation

---

## Test Requirements

### Unit Test Coverage

Each export format must have tests covering:

1. **Format Structure**
   - Header line format
   - Variable declarations format
   - Data section format
   - End marker presence

2. **Edge Cases**
   - Empty observations list
   - Single observation
   - Empty criterion list
   - Null handling

3. **Data Integrity**
   - Correct timing calculations
   - Proper code formatting
   - Sequence separators
   - Session markers

4. **Consistency**
   - Deterministic output (same input = same output)
   - No exceptions thrown for valid data
   - Proper line endings throughout

### Integration Tests

1. **Format Validation**
   - Output matches valid reference files
   - GSEQ can successfully parse output
   - No data loss during export

2. **Performance**
   - Large datasets (1000+ observations)
   - Multiple sessions
   - Complex criterion structures

### Test Data Requirements

Each format needs:
- Valid reference file (in `/integration` folder)
- Test data fixtures (instrument + register files)
- Expected output samples
- Edge case scenarios

---

## Appendix

### Terminology

- **Code**: Short identifier for a category (e.g., `RS`, `PT`)
- **Criterion**: A dimension of observation (e.g., `Paso`, `Adaptacion`)
- **Category**: A specific value within a criterion
- **Session**: A distinct observation period
- **Sequence**: A series of related observations
- **State**: A mutually exclusive condition
- **Interval**: A fixed time window for observations

### References

1. GSEQ Software Documentation
2. Lince Plus User Manual
3. Sequential Analysis Methodology (Bakeman & Gottman)

### Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-01-24 | Initial specification based on analysis of valid files |

---

**End of Specification**