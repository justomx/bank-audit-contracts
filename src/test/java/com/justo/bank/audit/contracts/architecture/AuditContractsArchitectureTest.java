package com.justo.bank.audit.contracts.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Module-boundary rules for {@code audit-contracts} (see
 * {@code openspec/changes/adapt-bank-template-multi-module/specs/module-boundaries}).
 *
 * <p>{@code audit-contracts} MUST NOT depend on any other internal module and MUST stay
 * pure JDK: it is consumed by both {@code audit-client} and {@code audit-ingestion-worker}
 * without either depending on the other.
 *
 * <p>{@code archRule.failOnEmptyShould=true} (see {@code archunit.properties}): every
 * rule below is scoped to {@code BASE_PACKAGE}, guaranteed non-empty by the
 * {@code AuditContractVersion} anchor (ADR-D5), so an empty match is a build failure,
 * not a silent pass.
 */
@AnalyzeClasses(
        packages = AuditContractsArchitectureTest.BASE_PACKAGE,
        importOptions = ImportOption.DoNotIncludeTests.class)
class AuditContractsArchitectureTest {

    static final String BASE_PACKAGE = "com.justo.bank.audit.contracts";

    /** Rule C1: no dependency on the other two internal modules. */
    @ArchTest
    static final ArchRule c1_no_dependency_on_client_or_worker =
            noClasses().that().resideInAPackage(BASE_PACKAGE + "..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.justo.bank.audit.client..", "com.justo.bank.audit.worker..")
                    .because("audit-contracts is shared by both sides; depending on either producer or "
                            + "consumer would make the shared module couple back to one of its own consumers");

    /** Rule C2: pure JDK. Subsumes C1 and "no Spring"; kept separate for message clarity. */
    @ArchTest
    static final ArchRule c2_only_jdk_and_own_package =
            noClasses().that().resideInAPackage(BASE_PACKAGE + "..")
                    .should().dependOnClassesThat()
                    .resideOutsideOfPackages("java..", "javax..", BASE_PACKAGE + "..")
                    .because("audit-contracts ships as a pure-JDK library; the pom's empty "
                            + "<dependencies> is load-bearing and this rule is its class-graph twin");

    /** Rule C3: a library must never print. */
    @ArchTest
    static final ArchRule c3_no_console_output =
            GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                    .because("a shared contracts library has no logging framework to route through; "
                            + "printing here would leak into whichever consumer's stdout is watched");
}
