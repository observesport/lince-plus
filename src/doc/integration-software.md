# Integration Software Export Formats

Lince PLUS exports observation data in several formats for external analysis tools.
This document describes each format, reverse-engineered from validated sample files.

## VVT - Instrument Export (.vvt)

Used by Theme software. Exports the observational instrument (criteria and categories).

**Structure:**
- No headers
- Each criterion name on its own line (no indentation)
- Each category code indented with one space, one per line
- No trailing newline required

**Example:**
```
PASO\r\n
 PP\r\n
 RS\r\n
 PT\r\n
FUNCIONAL\r\n
 A\r\n
 NA\r\n
EXITO\r\n
 L\r\n
 NL
```

## Theme 5 - Register Export (.csv)

Semicolon-separated CSV format for Theme 5 software.

**Structure:**
- Header line: `TIME;EVENT\r\n`
- Sequence start marker: `<frame>;:\r\n` (frame = first observation frame - 1)
- Data rows: `<frame>;<code1>,<code2>,...\r\n`
- Sequence end marker: `<frame>;&\r\n` (frame = last observation frame + 1)
- Multiple sequences supported

**Notes:**
- No DATANAME column (only TIME and EVENT)
- Frame values are integer frame numbers from the register
- Codes within a row are comma-separated

**Example:**
```
TIME;EVENT\r\n
1059;:\r\n
1060;ZI20,ZF20,FFSP\r\n
1061;ZI20,ZF30,C1\r\n
...
1617;&\r\n
```

## Theme 6 - Register Export (.txt)

Tab-separated format for Theme 6 software.

**Structure:**
- Header line: `TIME\tEVENT\r\n`
- Sequence start marker: `<time>\t:\r\n`
- Data rows: `<time>\t<code1>,<code2>,...\r\n`
- Sequence end marker: `<time>\t&\r\n`

**Notes:**
- Time values are sequential integers (1, 2, 3, ...) NOT frame numbers
- Tab-separated (not semicolons)
- Codes within a row are comma-separated
- Each sequence gets its own start/end markers

**Example:**
```
TIME\tEVENT\r\n
1\t:\r\n
2\tRS,A\r\n
3\tPT,A\r\n
...
7\t&\r\n
8\t:\r\n
9\tRS,A\r\n
...
```

## GSEQ Event (.sds)

SDIS Event Sequential Data format. Each observation is one code on its own line.

**Structure:**
- Header: `Event\r\n`
- Blank line
- Format A variable declarations: `($CriterionName = code1 code2 ...);\r\n`
- Two blank lines
- Data: one code per line
- Sequences separated by `;` on last line; final sequence ends with `/`

**Variable declarations (Format A):**
```
($CriterionName = code1 code2 code3);\r\n
```
Multiple criteria: last one gets the `;`.

**Data section:**
- One code per observation line
- Last line of non-final sequence: append `;` after code
- Last line of final sequence: append `/` after code
- Blank line between sequences

**Example:**
```
Event\r\n
\r\n
($SC = PP RS PT AR1 MR E PE);\r\n
\r\n
\r\n
RS\r\n
PT\r\n
AR1\r\n
PE;\r\n
\r\n
RS\r\n
PT;\r\n
\r\n
RS\r\n
PT\r\n
AR1\r\n
E\r\n
PE/\r\n
```

## GSEQ Multievent (.sds)

SDIS Multievent format. Multiple codes can occur simultaneously (space-separated on same line).

**Structure:**
- Header: `Multievent\r\n`
- Blank line
- Format A variable declarations (one per criterion, last ends `;`)
- Two blank lines
- Data: space-separated codes per line, line endings indicate position in sequence

**Data line endings:**
- `.` (period): non-last observation in a sequence
- `;` (semicolon): last observation in a non-final sequence
- `/` (forward slash): last observation in the final sequence

**Example:**
```
Multievent\r\n
\r\n
($Paso = PP RS PT AR1 MR E PE)\r\n
($Adaptacion = A NA)\r\n
($Exito = L NL);\r\n
\r\n
\r\n
RS A.\r\n
PT A.\r\n
AR1 A.\r\n
PE A.\r\n
L;\r\n
\r\n
RS A.\r\n
PT A.\r\n
AR1 A.\r\n
E A.\r\n
PE A.\r\n
L/\r\n
```

## GSEQ Timed (.sds)

SDIS Timed Event format. Codes with time ranges, multi-stream per criterion with `&` separator.

**Structure:**
- Header: `Timed\r\n` (may have trailing spaces)
- Format A variable declarations with optional comments (`% comment`)
- Optional `* FactorName (level1 level2);` lines for between-subjects factors
- Blank line
- Session markers: `<SessionName>\r\n`
- Data streams separated by `&\r\n`
- Each stream: `code,MM:SS-MM:SS code,MM:SS- code,MM:SS-MM:SS`
- Open-ended ranges use trailing `-` (no end time)
- Sequence metadata in parentheses: `(factor_values)/\r\n` or `(factor_values);\r\n`

**Time format:** `MM:SS` or `HH:MM:SS`

**Example:**
```
Timed\r\n
($Mother = MPassive = mp  MActive = ma  MRedirect = mr)\r\n
($Infant = IQuiet = iq  IActive = ia  IFuss = if)\r\n
* Skill (Low High);\r\n
\r\n
<Dyad Low 1>\r\n
\r\n
mr,00:00-01:07 mp,01:25- ma,01:37- mp,01:48-...\r\n
&\r\n
ia,00:00- iq,01:25- ... ,22:45;\r\n
```

## GSEQ State (.sds)

SDIS State Sequential Data format. Grouped parenthesized codes with `code,M:SS` transitions.

**Structure:**
- Header: `State <seconds>\r\n`
- Grouped code declarations in parentheses: `(code1 code2 code3)`
- Optional `* factor (level1 level2);` lines
- Blank line
- Session markers: `<SessionID>`
- Data: `code,M:SS` transitions separated by spaces
- Streams separated by `&\r\n`
- Comments with `%`
- Sequence metadata: `(factor_values)/\r\n`

**Example:**
```
State <seconds>\r\n
   (A P N) (GM GR GS GN) (MM MR MS MN) (C X M G U)\r\n
   * sex (male female) age (6mo 12mo);\r\n
\r\n
<60606IA66>,0:30 P,0:30 A,0:36 N,0:39 ... &\r\n
```

## GSEQ Interval (.sds)

SDIS Interval format. Time-sampled data with fixed interval duration.

**Structure:**
- Header: `Interval=N'\r\n` where N is interval duration in seconds
- Format B variable declarations (indented): `  fullname = code\r\n`
- Optional `* FactorName (level1 level2);` lines
- Blank line
- Session markers: `<S01>\r\n`
- Blank line
- Data: `*offset,code` entries (comma-separated within line, semicolon at end of sequence)
- `*N` = skip N intervals
- `code*N` = repeat code N times
- Multiple codes per interval: space-separated (e.g., `io oa`)
- Session metadata: `(factor_values)/\r\n`

**Example:**
```
Interval=10'\r\n
  ioffer = io\r\n
  iplay = ip\r\n
  ireceive = ir\r\n
* Age (3yr 5yr) Place (Home Outdoors);\r\n
\r\n
<S01>\r\n
\r\n
*10,ir,*15,ir,...;\r\n
\r\n
*2,ir,...;\r\n
\r\n
(3yr Home)/\r\n
```
