# Security

This library carries data for a regulated financial product. The following content is
trimmed to what applies to a contracts library: it does not run, store, or transmit
anything by itself, so most of the origin service's operational controls (logs,
runtime secrets, CDE services) do not apply here.

## Data excluded from any contract type

The restriction covers every file, including test fixtures and comments:

| Data | Reason |
|---|---|
| PAN (full card number) | PCI DSS scope |
| CVV / CVV2 | PCI DSS forbids storing it, even encrypted |
| Magnetic stripe | Same |
| Credentials for any environment | Never belongs in a shared type |
| AES, private, or signing keys | Never belongs in a shared type |
| Real personal data | CURP, RFC, address, phone number |

A contract type's field never carries a raw PAN, CVV, magnetic stripe, NIP, OTP, or
credential — see [`CONTEXT.md`](../CONTEXT.md) Invariants. Test fixtures use synthetic
data only.

Verification is performed by `gitleaks`, configured in
[`.gitleaks.toml`](../.gitleaks.toml) and run by the pre-commit hook
(`.githooks/pre-commit`).

If a secret is accidentally published, the order of action is: **immediate rotation**,
followed by history cleanup. A secret present in a repository is considered
compromised regardless of whether it is removed in the following commit.

## Mandatory human review

Changes affecting any of the following areas **are not merged without review by a
person**, regardless of the code's origin:

| Area | Risk |
|---|---|
| Handling of PAN, CVV, or magnetic stripe | PCI DSS scope; a leak is a reportable incident |
| Funds flow: payments, accounting, reconciliation | Direct financial loss |
| Authorization and resource ownership | BOLA/IDOR — top category in OWASP API Security |
| Cryptography, keys, and secrets | A mistake compromises every other control |

The requirement appears as a checklist item in the pull request template and as an
explicit restriction in `AGENTS.md`.

**Rationale:** code generation has sped up considerably; its review has not. In these
four areas the cost of a mistake remains unchanged, so the control stays in place.

## Dependencies

Adding a dependency is an explicit decision, not an incidental change within another
pull request. This library currently ships zero compile-scope dependencies by design
(see `ARCHITECTURE.md` rule C2); any addition is a build-shape change, not a routine
one.
