# Technical debt

Known gaps specific to this library, versioned with the code so they are visible to
whoever works on the repository next, human or agent.

An item is removed in the same change that resolves it. A new item needs an id, the
impact of leaving it open, and what it blocks.

| Id | Item | Impact | Blocks | Origin |
|---|---|---|---|---|
| TD-01 | No mechanical binary/source compatibility check runs between released versions (e.g. `japicmp`). The additive-only rule in `ARCHITECTURE.md` is documentary only | A breaking change to a published contract type is caught by review, not by the build | Nothing today | AJTC-110 |
| TD-02 | The agent harness (docs + `DocsFreshnessTest`) is duplicated by hand into this repository instead of being backported to `bank-template-vertical-bank` | New repositories split off the template the same way will re-diverge this same harness | Nothing | AJTC-110 |
| TD-03 | `justo-app.properties` (service-catalog metadata: team, criticality, owner, links) was not created — the values are not derivable from the origin repository and must come from DevOps | Repository not yet registered in the service catalog | Jenkins wiring | AJTC-110 |
| TD-04 | Jenkins discovery depends on repository *topics* that only DevOps can set | Repository does not enter any pipeline until DevOps sets them | Publishing via Jenkins | AJTC-110 |
| TD-05 | `DocsFreshnessTest`'s backtick check dropped the sibling-module path prefixes (`audit-client/`, `audit-ingestion-worker/`) it had in the reactor, since this repository has no sibling modules. A stale reference to one of those paths in a doc is no longer caught | A doc could reference a since-renamed sibling-repository path without the test catching it | Nothing | AJTC-110 |
| TD-06 | ArchUnit rule C5 sets `allowEmptyShould(true)` because no public method exists yet in this module. Remove the flag once the first public method (record accessor or factory) is added | C5 gives no real guarantee yet | Nothing | AJTC-110 |
| TD-07 | The JaCoCo line-coverage gate (0.70) passes vacuously: the only main class is a constant holder with nothing to instrument, so JaCoCo analyses 0 classes and the check has no denominator. Same behaviour as in the origin reactor. | The coverage gate guarantees nothing until the first real contract type and its tests exist; do not read a green build as "70% covered" | Nothing | AJTC-110 |
