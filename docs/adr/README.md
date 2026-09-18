# Architecture Decision Records (ADR)

An ADR records **the rationale** behind a decision, not its outcome, which is already
visible in the code. Its usefulness shows up when, some time later, someone questions
an existing structure and considers changing it.

## When to write one

Write an ADR when:

- Choosing between two or more reasonable options.
- Rejecting the obvious option for a concrete reason.
- Deliberately accepting a limitation.

Not needed when the decision had no alternatives.

## Format

```markdown
# NNNN — One-line title

- **Status:** proposed | accepted | superseded by [NNNN](...)
- **Date:** YYYY-MM-DD

## Context
Situation that motivates the decision.

## Decision
Content of the decision.

## Alternatives considered
Options evaluated and the reason for rejecting them.

## Consequences
Favorable and unfavorable effects. The latter require explicit attention, since they
tend to be the ones left out.
```

An accepted ADR **is not modified**. When a decision changes, a new one is written that
replaces the previous one, and the earlier one is marked as superseded.
