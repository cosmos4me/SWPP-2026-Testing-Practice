# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Describe a task's completion or due date with a compact D-day label. Count the total calendar days between dates, including across months, years, and leap days; completion takes precedence over any due date.

**Pre-conditions**
- `today` is a valid, non-null `LocalDate`; `dueDate` is a valid `LocalDate` or null.
- `isCompleted` indicates whether the task is complete. Dates carry no time of day or time zone.

**Post-conditions**
- Success cases
  - Completed tasks return exactly `"CLEAR"` whether the due date is absent, past, today, or future. `[Unit]`
    - Tip: Given today `2026-10-15`, isCompleted `true`, and dueDate separately `null`, `2026-10-14`, `2026-10-15`, and `2026-10-16` / When calling `dueDateLabel` for each input / Then each result is `"CLEAR"`.
  - An active task without a due date returns the empty string `""`. `[Unit]`
    - Tip: Given dueDate `null`, today `2026-10-15`, isCompleted `false` / When calling `dueDateLabel` / Then the result is `""`.
  - An active task due today returns exactly `"D-Day"`. `[Unit]`
    - Tip: Given dueDate `2026-10-15`, today `2026-10-15`, isCompleted `false` / When calling `dueDateLabel` / Then the result is `"D-Day"`.
  - An active task due N calendar days in the future returns `"D-N"` with the positive decimal day count and no padding. `[Unit]`
    - Tip: Given today `2026-10-15` and isCompleted `false` / When calling `dueDateLabel` separately with dueDate `2026-11-14`, `2026-11-15`, and `2026-11-16` / Then the results are `"D-30"`, `"D-31"`, and `"D-32"`, respectively, using total calendar days before, on, and after one full calendar month.
  - An active task overdue by N calendar days returns `"D+N"` with the positive decimal day count and no padding. `[Unit]`
    - Tip: Given dueDate `2026-09-15`, today `2026-10-15`, isCompleted `false` / When calling `dueDateLabel` / Then the result is `"D+30"`.
- Error cases
  - None. Every input satisfying the pre-conditions is valid, including a null due date and overdue dates.
- Edge cases
  - The due-today boundary distinguishes yesterday, today, and tomorrow. `[Unit]`
    - Tip: Given today `2026-10-15` and isCompleted `false` / When calling with dueDate `2026-10-14`, `2026-10-15`, and `2026-10-16` / Then results are `"D+1"`, `"D-Day"`, and `"D-1"`, respectively.
  - A month transition counts days continuously on both sides of the first day of the month. `[Unit]`
    - Tip: Given today `2026-01-30` and isCompleted `false` / When calling with dueDate `2026-01-31`, `2026-02-01`, and `2026-02-02` / Then results are `"D-1"`, `"D-2"`, and `"D-3"`, respectively.
  - A year transition counts days continuously on both sides of January 1. `[Unit]`
    - Tip: Given today `2025-12-30` and isCompleted `false` / When calling with dueDate `2025-12-31`, `2026-01-01`, and `2026-01-02` / Then results are `"D-1"`, `"D-2"`, and `"D-3"`, respectively.
  - Leap day contributes one calendar day, with correct values immediately before, on, and after it. `[Unit]`
    - Tip: Given today `2024-02-27` and isCompleted `false` / When calling with dueDate `2024-02-28`, `2024-02-29`, and `2024-03-01` / Then results are `"D-1"`, `"D-2"`, and `"D-3"`, respectively.
  - A non-leap February ends after February 28. `[Unit]`
    - Tip: Given today `2025-02-27` and isCompleted `false` / When calling with dueDate `2025-02-28`, `2025-03-01`, and `2025-03-02` / Then results are `"D-1"`, `"D-2"`, and `"D-3"`, respectively.
  - Past dates across calendar boundaries use the full overdue count. `[Unit]`
    - Tip: Given isCompleted `false` / When calling with (dueDate, today) equal to (`2026-01-30`, `2026-03-01`), (`2025-12-31`, `2026-01-02`), and (`2024-02-28`, `2024-03-01`) / Then results are `"D+30"`, `"D+2"`, and `"D+2"`, respectively.
  - Completion overrides the active label, including the no-date boundary. `[Unit]`
    - Tip: Given today `2026-10-15` / When calling with dueDate `null` and isCompleted `false` then `true`, and with dueDate `2026-10-16` and isCompleted `false` then `true` / Then results are `""`, `"CLEAR"`, `"D-1"`, and `"CLEAR"`, respectively.

**Undefined behavior** (optional)
- Null `today` or arguments that bypass Kotlin type safety are outside the contract.


