package com.justo.bank.template.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The rules from ARCHITECTURE.md, made executable.
 *
 * <p>This file is the reason the rules in ARCHITECTURE.md are not decorative: the
 * presence of a JPA annotation in the domain, or the injection of a service into a
 * controller, breaks the build while identifying the responsible class and the
 * reason.
 *
 * <p>A convention that no mechanism verifies degrades within weeks.
 *
 * <p>INIT: scripts/init-repo.sh updates BASE_PACKAGE to the service's package.
 */
@AnalyzeClasses(
        packages = HexagonalArchitectureTest.BASE_PACKAGE,
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    static final String BASE_PACKAGE = "com.justo.bank.template";

    private static final String DOMAIN = BASE_PACKAGE + ".domain..";
    private static final String APPLICATION = BASE_PACKAGE + ".application..";
    private static final String INFRASTRUCTURE = BASE_PACKAGE + ".infrastructure..";

    // ------------------------------------------------------------------
    // 1. Dependency direction: infrastructure -> application -> domain
    // ------------------------------------------------------------------

    @ArchTest
    static final ArchRule the_domain_does_not_depend_on_anyone =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(APPLICATION, INFRASTRUCTURE)
                    .because("the domain is the center of the hexagon: nothing from outside comes in");

    @ArchTest
    static final ArchRule the_application_does_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
                    .because("the application layer speaks through ports, not to concrete adapters");

    // ------------------------------------------------------------------
    // 2. The domain without a framework
    // ------------------------------------------------------------------

    @ArchTest
    static final ArchRule the_domain_does_not_know_about_frameworks =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "com.fasterxml.jackson..",
                            "io.swagger..")
                    .because("a Hibernate or Jackson version bump must not touch business rules");

    @ArchTest
    static final ArchRule ports_do_not_know_about_frameworks =
            noClasses().that().resideInAPackage(BASE_PACKAGE + ".application.port..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "software.amazon..")
                    .because("a port is a business contract, not a transport contract");

    // ------------------------------------------------------------------
    // 3. Where each annotation lives
    // ------------------------------------------------------------------

    /**
     * Two rules, because {@code @Transactional} can go on the class or on the method
     * and a single rule does not cover both cases. In practice it is used at the
     * method level.
     */
    @ArchTest
    static final ArchRule method_level_transactional_only_in_application_services =
            methods().that().areAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                    .should().beDeclaredInClassesThat()
                    .resideInAPackage(BASE_PACKAGE + ".application.service..")
                    .because("the transactional boundary is decided by the use case, not by an adapter");

    @ArchTest
    static final ArchRule class_level_transactional_only_in_application_services =
            noClasses().that().resideOutsideOfPackage(BASE_PACKAGE + ".application.service..")
                    .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                    .because("the transactional boundary is decided by the use case, not by an adapter");

    @ArchTest
    static final ArchRule jpa_entities_only_in_the_persistence_adapter =
            classes().that().areAnnotatedWith("jakarta.persistence.Entity")
                    .should().resideInAPackage(BASE_PACKAGE + ".infrastructure.adapter.out.persistence..")
                    .because("the JPA entity is a storage detail");

    @ArchTest
    static final ArchRule controllers_only_in_the_rest_adapter =
            classes().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().resideInAPackage(BASE_PACKAGE + ".infrastructure.adapter.in.rest..")
                    .because("a controller is an inbound adapter");

    // ------------------------------------------------------------------
    // 4. Inbound adapters only talk to ports
    // ------------------------------------------------------------------

    @ArchTest
    static final ArchRule incoming_adapters_do_not_use_application_services_directly =
            noClasses().that().resideInAPackage(BASE_PACKAGE + ".infrastructure.adapter.in..")
                    .should().dependOnClassesThat()
                    .resideInAPackage(BASE_PACKAGE + ".application.service..")
                    .because("they must depend on the inbound port, so adding a transport does not touch the application");

    // ------------------------------------------------------------------
    // 5. General hygiene
    // ------------------------------------------------------------------

    /**
     * Constructor injection. Both fields AND methods are checked: {@code @Autowired}
     * on a field or on a setter are the two forms that must be prevented.
     *
     * <p>Test code may use {@code @Autowired}: ArchUnit does not analyze the tests
     * (see {@code ImportOption.DoNotIncludeTests} above).
     */
    @ArchTest
    static final ArchRule no_autowired_on_fields =
            noFields().should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                    .because("injection is by explicit constructor, not by field");

    @ArchTest
    static final ArchRule no_autowired_on_setters =
            noMethods().should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                    .because("injection is by explicit constructor, not by setter");

    /** Standard ArchUnit rule: no System.out / System.err / printStackTrace. */
    @ArchTest
    static final ArchRule no_console_output =
            GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                    .because("everything goes through SLF4J: the log collector does not read System.out");

    @ArchTest
    static final ArchRule no_java_util_logging =
            GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
                    .because("the service's logging is SLF4J + Logback, and only that");
}
