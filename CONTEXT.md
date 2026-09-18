# Business context

> **This document requires specific writing for each service.** The content that
> follows defines the expected structure and level of detail.
>
> This is the highest-value document in the repository for anyone onboarding, human or
> automated. `ARCHITECTURE.md` describes how the code is written; this one describes
> the domain it operates on. In its absence, whoever onboards will infer the
> terminology and business rules from the code, which is the mechanism through which
> misinterpretations propagate.

## Service purpose

<!-- Two or three sentences: what problem it solves and for which consumers. -->

## Out of scope

<!-- Equally relevant as the above: delimits the boundary with neighboring services.
     Example: "Interest calculation belongs to card-financials-service." -->

## Ubiquitous language

Domain terminology with the meaning it takes on in this service. When the provider
uses a different name for the same concept, the correspondence is documented: that
mapping prevents integration errors.

| Term | Meaning | Name in Evertec/SISCARD |
|---|---|---|
| <!-- Cuenta --> | <!-- ... --> | <!-- emisor(8) + cuenta(5) --> |

## Invariants

Permanently enforced rules, with their documentary source. An invariant without a
source is an assumption.

| # | Invariant | Source |
|---|---|---|
| 1 | <!-- ... --> | <!-- Evertec Manual §12.5 --> |

## Related services

| Service | Direction | Purpose | Contract |
|---|---|---|---|
| <!-- evertec-connector --> | <!-- outbound --> | <!-- ... --> | <!-- location --> |

## Sensitive data handled

The selection determines whether the service falls within PCI DSS scope and whether it
requires mandatory human review (see [`docs/SECURITY.md`](docs/SECURITY.md)).

- [ ] PAN (full card number)
- [ ] CVV / CVV2
- [ ] Customer personal data (name, CURP, RFC, address)
- [ ] Movements or balances
- [ ] None of the above

## Open questions

Unresolved questions that constrain the design. Recording them explicitly is
preferable to resolving them through an assumption buried in the code.

| Question | Owner of the answer | Blocked item |
|---|---|---|
| <!-- ... --> | <!-- ... --> | <!-- ... --> |
