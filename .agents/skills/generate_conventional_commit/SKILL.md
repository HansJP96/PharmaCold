---
name: generate_conventional_commit
description: >-
  Inspects working directory changes in Git using token-efficient multi-step
  strategies and drafts commit messages strictly complying with the Conventional
  Commits v1.0.0 specification.
---

# Skill: Git Diff Inspection & Conventional Commit Generation

## Metadata
- **Language:** English (Commit outputs and internal reasoning must always be generated in English).
- **Target Audience:** Autonomous coding agents, Git workflow automation, CLI assistants.

---

## 1. Parameters & Inputs

| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `staged_only` | `boolean` | Optional (default: `true`) | Inspect only staged changes (`--cached`). If `false`, inspect working tree. |
| `context_hint` | `string` | Optional | External reference (e.g., ticket ID `PROJ-123`, PR number, or explicit user intent). |
| `max_header_length` | `integer` | Optional (default: `72`) | Maximum character limit for the commit subject line (ideal range: 50–72). |

---

## 2. Execution Pipeline: Change Inspection Strategy

Agents MUST NOT execute an unbounded `git diff` as the first action when hundreds or thousands of lines could be modified. Follow this tiered protocol:

**NOTE**: Set focus changes on Staged changes over untracked files, if no staged changes exist then process all changes available.

### Step 2.1: Volume and Scope Assessment
Execute lightweight commands first to gauge impact and file distribution:

```bash
# 1. Inspect status summary
git status --short

# 2. Get line count balance and affected files
git diff --cached --stat
```
*(If `staged_only: false`, replace `--cached` with `HEAD` or omit the flag).*

### Step 2.2: Context-Aware Diff Collection
Select the inspection command based on the result of `--stat`:

- **Path A: Small to Medium Diffs (< 200 lines modified):**
  ```bash
  git diff --cached
  ```

- **Path B: Large Diffs (> 200 lines modified, lockfiles, or generated assets):**
  1. **Exclude noisy generated files and lockfiles:**
     ```bash
     git diff --cached -- ":(exclude)*lock*" ":(exclude)*.min.*" ":(exclude)dist" ":(exclude)build" ":(exclude)out"
     ```
  2. **Strip whitespace changes:**
     ```bash
     git diff --cached -w --minimal
     ```
  3. **Chunked / Hierarchical inspection (if total lines still exceed context budget):**
     Inspect source code directory by directory in order of business priority:
     ```bash
     # Priority 1: Domain/core application logic
     git diff --cached -- src/ core/ lib/ app/

     # Priority 2: Tests and configuration
     git diff --cached -- tests/ spec/ config/
     ```

---

## 3. Commit Message Syntax Specification

The output must strictly conform to:

```text
<type>[optional scope]: <imperative description>

[optional body explaining motivation and impact]

[optional footer(s)]
```

### Formatting Constraints:
1. **Header Line:**
   - Imperative mood, present tense: `"add"`, `"fix"`, `"refactor"` (NEVER `"added"`, `"adding"`, `"fixes"`).
   - All lowercase for `<type>`, `<scope>`, and the first letter of `<description>`.
   - **No trailing period/dot** (`.`) at the end of the header.
   - Hard limit: 72 characters max (50 recommended).
2. **Blank Lines:**
   - Exactly one blank line between header and body.
   - Exactly one blank line between body and footer(s).
3. **Body:**
   - Wrapped at 72 characters per line.
   - Focus on **WHY** the change was made and **WHAT IMPACT** it introduces, rather than re-listing the diff line by line.
   - Mandatory when changes involve > 3 files or non-obvious architecture shifts.
4. **Breaking Changes:**
   - Denote with a `!` after type/scope (e.g., `feat(api)!: remove v1 endpoints`) OR include a footer starting with `BREAKING CHANGE: <explanation>`.

---

## 4. Type Taxonomy

| Type | Intended Use Case |
| :--- | :--- |
| **`feat`** | Introduces a new feature or public capability. |
| **`fix`** | Patches a bug or addresses unintended behavior. |
| **`docs`** | Documentation-only updates (README, markdown guides, inline docstrings). |
| **`style`** | Code formatting, missing semicolons, linting fixes (no runtime code logic changes). |
| **`refactor`** | Code alterations that neither fix a bug nor add a feature. |
| **`perf`** | Code adjustments specifically targeting execution speed or memory efficiency. |
| **`test`** | Adding, refactoring, or correcting test suites. |
| **`build`** | Build system, packaging scripts, tooling, or external dependency changes (npm, pip, cargo). |
| **`ci`** | Modifications to CI/CD workflows and automated release scripts. |
| **`chore`** | Routine operational changes that do not touch `src` or test code (e.g., `.gitignore`). |

---

## 5. Decision Rules for the Agent

1. **Scope Determination:**
   - Infer the scope from the affected module, service, or directory name (e.g., `(auth)`, `(parser)`, `(deps)`, `(ui)`).
   - If changes span the entire codebase uniformly, omit the scope.
2. **Atomicity Rule:**
   - If the staged changes contain multiple unrelated concerns (e.g., a feature addition mixed with an unrelated bugfix and formatting), prioritize the primary business concern in the header and warn/suggest commit splitting in the body.
3. **Issue References:**
   - When a ticket reference is supplied via `context_hint` or branch naming, append it to the footer using recognized keywords: `Fixes #123`, `Closes PROJ-456`.

---

## 6. Validated Output Examples

### Example A: Single Feature
```text
feat(auth): implement refresh token rotation

Add short-lived access token renewal using cryptographically signed
refresh tokens stored in secure, HTTP-only cookies.

Closes #312
```

### Example B: Bug Fix with Contextual Body
```text
fix(checkout): avoid duplicate charges on slow network connections

Introduce client-side idempotency keys during transaction submission.
Subsequent network retries within a 60-second window are discarded
by the payment processor.

Fixes #1048
```

### Example C: Breaking Change
```text
feat(api)!: migrate auth token format to jwt v2

BREAKING CHANGE: Legacy opaque session tokens are no longer accepted.
API clients must provide RSA256-signed JWTs via the Authorization header.
```
