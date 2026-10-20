# Codex project instructions

Before every task in either E-Commerce repository:

1. Read `PROJECT_CONTEXT.md` and `MASTER_IMPLEMENTATION_PLAN.md` from this workspace root. Treat the plan as the source of requirements and the context file as a summary that must be checked against the actual repositories.
2. Inspect the relevant repository's current files, Git status, branch, remotes, configuration, and existing implementation before proposing edits. For work spanning frontend and backend, inspect both repositories. Never assume repository state from an earlier task.
3. Follow the plan's phases in order. The active stage is Phase 1 (discovery and planning) until the user approves the discovery report. Do not begin major implementation before that approval.
4. Preserve all existing code and uncommitted work. Do not overwrite or delete existing files without the user's approval. Do not push, change GitHub settings, deploy, or make administrative database changes without explicit authorization.
5. Ask before destructive or potentially incompatible database changes and before significant architecture changes. Never expose or commit secrets. Do not read `.env` values into reports; use `.env.example` and configuration names instead.
6. Automated testing is deferred by the plan add-on: do not create test suites, install test frameworks, or run automated tests unless the user explicitly changes this instruction. Basic build/start/migration checks may be done when appropriate and authorized; report only checks actually performed.
7. Keep `PROJECT_CONTEXT.md` accurate as work proceeds. Update it after implementation changes with what exists, what remains planned, decisions, configuration placeholders, and verified versus unverified behavior. Never invent implementation status or test results.
8. Explain important choices and commands in beginner-friendly language. Keep frontend and backend work in their respective existing repositories.
9. At the end of each approved phase, summarize completed work, changed files, commands/checks actually run, remaining issues, and the next proposed phase.

If the plan, context, or repository state conflict, trust the user's latest instruction and actual repository state; explain the discrepancy before making consequential changes.

## Latest user authorization takes precedence

The phase gate and testing deferral above are defaults, not barriers to a later explicit user instruction. For the 2026-10-20 authentication completion request, the user explicitly authorized proceeding with implementation and automated tests. That work is recorded in the root `PROJECT_CONTEXT.md` and handover docs. Follow the latest direct user instruction when it changes phase approval, test authorization, or design preferences; preserve this file's safety rules about secrets, destructive database operations, and remote changes.
