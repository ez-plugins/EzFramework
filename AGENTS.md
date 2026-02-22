## AI Development & Documentation Rules

Purpose: give clear, deterministic guidance for any AI agent editing code or docs in this repository so changes are professional, minimal, and reviewable.

Principles
- Be conservative: prefer minimal, focused edits that preserve intent and style of surrounding code.
- Be explicit: always include a brief rationale for non-trivial edits in the commit message or PR description.
- Preserve history: do not rewrite unrelated commits or remove authorship; keep diffs small and atomic.
- Test-driven: when changing code, run existing tests or build steps locally; prefer small fixes verified by tooling.

Code Style and Edits
- Respect existing project style: follow the formatting, naming, and API patterns already in the repository.
- Small, single-purpose commits: one logical change per commit with a clear message (imperative tense).
- No large-scale reformatting unless requested: avoid mass whitespace or formatting changes that obscure functional diffs.
- Avoid adding new dependencies without rationale and version pinning in project config.
- Add unit tests for behavioral changes; if not possible, add a short manual verification step in the commit message.

Documentation
- Keep docs in `docs/` authoritative; update docs alongside code changes that affect behavior or public APIs.
- Use clear headings and one-sentence summaries at the top of each doc file.
- Prefer examples over long prose: include short, copy-pastable snippets where appropriate.
- Use consistent Markdown conventions: fenced code blocks with language, single trailing newline, no hard tabs.
- When documenting public APIs or breaking changes, include migration notes and examples.

Commit and CI
- Run linters and tests locally before committing. If fixes are automatic (formatters, linters), include those changes in the same commit as the code they fix.
- Keep CI green: if a change requires CI config updates, include them in the same PR and document why.

Interaction Guidelines for the AI
- Ask before pushing: when changes are invasive or a new branch/PR is required, request user confirmation before pushing.
- Use the repo's tooling: run Maven builds, tests, and linters present in the workspace to validate changes.
- When unsure, propose 1-2 safe options and pick the conservative default unless user instructs otherwise.
- Never manufacture secrets, tokens, or credentials; prompt the user if they are required to proceed.

Commit Message Guidelines for AI
- Format: `scope: short-description` (e.g., `docs: fix code fence languages in message docs`).
- Body (optional): 1–3 lines describing reason and verification steps.

Review and Rollback
- Add a short manual test or verification section when automated tests don't cover the change.
- Provide a rollback plan in the PR description for destructive or risky changes.

Privacy & Safety
- Do not include or infer private data, credentials, or personally identifiable information in commits or docs.

Operational Notes
- For large refactors or policy changes, open a draft PR and ask for review rather than pushing directly to `main/master`.
- Prefer using the repository's issue tracker to record rationale for substantial documentation or API changes.

---
These rules are intended to guide automated and human contributors; keep them concise and update as the project's workflow evolves.
