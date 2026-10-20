# Project guidance

This repository is part of the E-Commerce authentication project. Before every task, read `..AGENTS.md`, `..PROJECT_CONTEXT.md`, and `..MASTER_IMPLEMENTATION_PLAN.md`. The parent files contain shared requirements and current context; verify them against the actual code and Git status before acting. If those files are unavailable, ask the user for the current master plan instead of guessing.

Follow the phases in order. The project is still at Phase 1 discovery/planning until the user approves the discovery report. Preserve existing changes and never overwrite or delete work without approval. Keep frontend and backend changes in their respective repositories. Do not push, deploy, alter GitHub settings, or perform administrative/destructive database actions without explicit authorization.

Automated testing is deferred by the plan add-on. Do not create or run automated tests or set up testing frameworks unless the user explicitly changes that instruction. Keep `..PROJECT_CONTEXT.md` accurate after approved work and distinguish verified behavior from planned or unverified behavior. Do not expose secrets from `.env` files.

## Latest user authorization takes precedence

The phase gate and testing deferral above are defaults, not barriers to a later explicit user instruction. For the 2026-10-20 authentication completion request, the user explicitly authorized proceeding with implementation and automated tests. That work is recorded in the root `PROJECT_CONTEXT.md` and handover docs. Follow the latest direct user instruction when it changes phase approval, test authorization, or design preferences; preserve this file's safety rules about secrets, destructive database operations, and remote changes.
