package com.justo.bank.audit.contracts;

/**
 * Carries the audit event/record schema version this module ships.
 *
 * <p>Every event type this module later defines is versioned against this constant, so
 * producers ({@code audit-client}) and the consumer ({@code audit-ingestion-worker}) can
 * detect a schema drift instead of silently deserializing a mismatched payload.
 *
 * <p>This is also the module's non-contrived anchor type (ADR-D5): a Maven module with
 * zero main sources builds fine, but every ArchUnit rule scoped to its package would then
 * be vacuous. This constant makes non-vacuity a property of the source tree.
 */
public final class AuditContractVersion {

    /** Current schema version for all audit contract types in this module. */
    public static final int CURRENT = 1;

    private AuditContractVersion() {
    }
}
