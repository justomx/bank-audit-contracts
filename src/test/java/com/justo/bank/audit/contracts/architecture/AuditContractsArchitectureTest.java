package com.justo.bank.audit.contracts.architecture;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * Contract-shape rules for {@code audit-contracts} (see {@code ARCHITECTURE.md}).
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

    /** Rule C2: pure JDK. The class-graph twin of the enforcer ban and the empty {@code <dependencies>}. */
    @ArchTest
    static final ArchRule c2_only_jdk_and_own_package =
            noClasses().that().resideInAPackage(BASE_PACKAGE + "..")
                    .should().dependOnClassesThat()
                    .resideOutsideOfPackages("java..", "javax..", BASE_PACKAGE + "..")
                    .because("audit-contracts ships as a pure-JDK library; the pom's empty "
                            + "<dependencies> is load-bearing and this rule is its class-graph twin");

    /** Rule C4: contracts are records of fact; a non-final field would let one be edited after construction. */
    @ArchTest
    static final ArchRule c4_fields_are_final =
            fields().that().areDeclaredInClassesThat().resideInAPackage(BASE_PACKAGE + "..")
                    .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
                    .should().beFinal()
                    .because("a contract type is a record of fact; mutating a field after construction "
                            + "would falsify the trail it is meant to preserve");

    /**
     * Rule C5: no setter, ever. Scoped to public methods, which do not exist yet in this
     * module (only the anchor's field and private constructor do), so {@code allowEmptyShould}
     * is turned on for this rule alone: the class-level anchor (ADR-D5) guarantees a non-empty
     * package match, not a non-empty public-method match, and a vacuous match here is the
     * expected starting state, not a typo.
     */
    @ArchTest
    static final ArchRule c5_no_setters =
            noMethods().that().areDeclaredInClassesThat().resideInAPackage(BASE_PACKAGE + "..")
                    .and().arePublic()
                    .should().haveNameMatching("set[A-Z].*")
                    .because("a setter would reintroduce the mutability C4 already forbids on fields")
                    .allowEmptyShould(true);
}
