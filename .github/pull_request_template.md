## Change description

<!-- One or two sentences. The functional justification belongs in the ticket. -->

Ticket: <!-- JUS-000 -->

## Verification performed

<!-- What was run and which edge cases are covered. -->

- [ ] New or updated unit tests
- [ ] Integration tests, if the change affects an outbound adapter
- [ ] Manual check against the local environment

## Checklist

- [ ] `./mvnw verify` completes successfully locally
- [ ] The final diff was reviewed in full before opening the PR. If an agent produced the change, the review pass in `AGENTS.md` was run
- [ ] `domain/` remains free of framework annotations
- [ ] Every schema change is introduced in a new migration, without editing applied migrations
- [ ] No dependency versions have been declared in `pom.xml`, except ones Spring Boot does not manage
- [ ] The change does not introduce secrets, keys, or credentials
- [ ] Added log entries do not include PAN, CVV, tokens, or full personal data

## Mandatory human review

Check the applicable items. Any checked item means the integration **requires review
by a person, regardless of the code's origin**:

- [ ] Handling of card data (PAN, CVV, magnetic stripe) — PCI DSS scope
- [ ] Funds flow: payments, accounting, reconciliation
- [ ] Authorization or ownership verification of a resource
- [ ] Cryptography, key management, or secrets
