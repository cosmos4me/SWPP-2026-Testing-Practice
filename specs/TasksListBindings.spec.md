# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `setDueDateLabel(textView: TextView, task: Task?): Unit`

**Intent**: Show the current task's completion or D-day label, using the device's current local date. Clear and hide an absent label so a reused list item never displays another task's due date.

**Pre-conditions**
- `textView` is a valid Android `TextView` and the call occurs on the UI thread.
- `task` may be null; a non-null task may have a null due date.
- For the examples below, hold the device's current local date at `2026-10-15` throughout each call or sequence, except where a different date is stated.
- In examples, `Task(dueDate=..., isCompleted=...)` means title `"Task"`, description `"Description"`, id `"task-1"`, and the stated date and completion values.
- Empty labels must use `View.GONE` and text `""`. This specifies the intended no-label UI behavior, including view reuse.

**Post-conditions**
- Success cases
  - A completed task displays `"CLEAR"` and becomes `View.VISIBLE`, regardless of its due date. `[Integration]`
    - Tip: Given text `"old"` and visibility `View.GONE`, and Task with isCompleted `true` and dueDate separately `null`, `2026-10-14`, `2026-10-15`, and `2026-10-16` / When calling `setDueDateLabel` for each task / Then text is `"CLEAR"` and visibility is `View.VISIBLE` after each call.
  - An active task with a due date displays its correct D-day label and becomes `View.VISIBLE`. `[Integration]`
    - Tip: Given text `"old"` and visibility `View.INVISIBLE` / When binding active Tasks with dueDate `2026-10-12`, `2026-10-15`, and `2026-11-15` separately / Then text is `"D+3"`, `"D-Day"`, and `"D-31"`, respectively, and visibility is `View.VISIBLE` each time.
  - A null task clears any existing text and hides the view. `[Integration]`
    - Tip: Given text `"D-5"`, visibility `View.VISIBLE`, and task `null` / When calling `setDueDateLabel` / Then text is `""` and visibility is `View.GONE`.
  - An active task without a due date clears any existing text and hides the view. `[Integration]`
    - Tip: Given text `"CLEAR"`, visibility `View.VISIBLE`, and Task(dueDate=`null`, isCompleted=`false`) / When calling `setDueDateLabel` / Then text is `""` and visibility is `View.GONE`.
- Error cases
  - None. A null task and a null due date are supported inputs under the pre-conditions.
- Edge cases
  - Yesterday, today, and tomorrow produce distinct visible labels. `[Integration]`
    - Tip: Given a TextView with text `""` and visibility `View.GONE` / When binding active Tasks with dueDate `2026-10-14`, `2026-10-15`, and `2026-10-16` in sequence / Then text is `"D+1"`, `"D-Day"`, and `"D-1"`, respectively, and visibility is `View.VISIBLE` after each call.
  - Rebinding across the nonempty-to-empty and empty-to-nonempty boundaries removes stale labels and restores visibility. `[Integration]`
    - Tip: Given a TextView with text `""` and visibility `View.GONE` / When binding Task(dueDate=`2026-10-16`, isCompleted=`false`), then `null`, then Task(dueDate=`2026-10-15`, isCompleted=`false`) / Then successive (text, visibility) values are (`"D-1"`, `View.VISIBLE`), (`""`, `View.GONE`), and (`"D-Day"`, `View.VISIBLE`).
  - Rebinding the same view through completion and absence of a date refreshes both properties on every call. `[Integration]`
    - Tip: Given a TextView with text `""` and visibility `View.GONE` / When binding Tasks with (dueDate, isCompleted) equal to (`null`, `false`), (`null`, `true`), and (`null`, `false`) / Then successive (text, visibility) values are (`""`, `View.GONE`), (`"CLEAR"`, `View.VISIBLE`), and (`""`, `View.GONE`).
  - Checking and unchecking a task that has a due date switches the label between its D-day value and `"CLEAR"` both ways. `[Integration]`
    - Tip: Given a TextView with text `""` and visibility `View.GONE` / When binding Tasks with (dueDate, isCompleted) equal to (`2026-10-16`, `false`), (`2026-10-16`, `true`), and (`2026-10-16`, `false`) / Then successive (text, visibility) values are (`"D-1"`, `View.VISIBLE`), (`"CLEAR"`, `View.VISIBLE`), and (`"D-1"`, `View.VISIBLE`).
  - The current local date is evaluated for each binding, including across midnight. `[Integration]`
    - Tip: Given Task(dueDate=`2026-10-15`, isCompleted=`false`) and initial text `"old"` and visibility `View.GONE` / When binding on local dates `2026-10-14`, `2026-10-15`, and `2026-10-16` / Then text is `"D-1"`, `"D-Day"`, and `"D+1"`, respectively, and visibility is `View.VISIBLE` after each call.
  - Calendar boundaries retain the day-count meaning in the displayed UI. `[Integration]`
    - Tip: Given the local date fixed at `2024-02-27`, initial text `""`, visibility `View.GONE`, and active Tasks / When binding dueDate `2024-02-28`, `2024-02-29`, and `2024-03-01` / Then text is `"D-1"`, `"D-2"`, and `"D-3"`, respectively, and visibility is `View.VISIBLE` after each call.

**Undefined behavior** (optional)
- Calls outside the UI thread or with a null TextView supplied by bypassing Kotlin type safety are outside the contract.
- No automatic refresh at midnight is promised without another binding call.


