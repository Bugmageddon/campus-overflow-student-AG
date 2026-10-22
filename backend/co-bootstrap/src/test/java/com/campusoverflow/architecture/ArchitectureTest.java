package com.campusoverflow.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import org.springframework.web.bind.annotation.RestController;

/**
 * 架构适应度函数（arc42 第 10 章 FF-1 ~ FF-6）——把架构约束写成自动化测试，
 * 让“模块化单体不退化成大泥球”这件事由 CI 持续验证，而不是靠评审时的自觉。
 */
@AnalyzeClasses(packages = "com.campusoverflow", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String[] CONTEXTS = {"identity", "qa", "reputation", "discovery"};
    private static final Map<String, String> TABLE_PREFIX = Map.of(
            "identity", "id_", "qa", "qa_", "reputation", "rep_", "discovery", "dis_");

    /** FF-1：领域层保持纯 Java，不依赖任何框架——这是可测试性与可演进性的基石。 */
    @ArchTest
    static final ArchRule domain_layer_is_framework_free = noClasses()
            .that().resideInAPackage("com.campusoverflow..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "jakarta.validation..",
                    "org.hibernate..", "com.fasterxml.jackson..", "io.swagger..")
            .because("领域模型必须能脱离 Spring/JPA 独立编译与单元测试（FF-1）");

    /** FF-4：六边形分层方向——依赖只能由外向内。 */
    @ArchTest
    static final ArchRule hexagonal_layers = layeredArchitecture().consideringAllDependencies()
            .layer("Interfaces").definedBy("com.campusoverflow.*.interfaces..")
            .layer("Application").definedBy("com.campusoverflow.*.application..")
            .layer("Domain").definedBy("com.campusoverflow.*.domain..")
            .layer("Infrastructure").definedBy("com.campusoverflow.*.infrastructure..")
            .layer("Bootstrap").definedBy("com.campusoverflow.bootstrap..")
            .whereLayer("Interfaces").mayOnlyBeAccessedByLayers("Bootstrap")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Bootstrap")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces", "Infrastructure", "Bootstrap")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Interfaces", "Bootstrap")
            .because("依赖方向必须由外向内：领域层不知道谁在调用它（FF-4）");

    /** FF-2：跨上下文只允许经由对方的 api 包（发布语言）或集成事件。 */
    @ArchTest
    static void contexts_communicate_only_through_published_language(JavaClasses classes) {
        for (String context : CONTEXTS) {
            for (String other : CONTEXTS) {
                if (context.equals(other)) {
                    continue;
                }
                noClasses().that().resideInAPackage("com.campusoverflow." + context + "..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                                "com.campusoverflow." + other + ".domain..",
                                "com.campusoverflow." + other + ".application..",
                                "com.campusoverflow." + other + ".infrastructure..",
                                "com.campusoverflow." + other + ".interfaces..")
                        .because(context + " 只能通过 " + other + ".api 与之交互（FF-2）")
                        .check(classes);
            }
        }
    }

    /** FF-5：api 包是“发布语言”，只能依赖 JDK 与共享内核，保证可以被任何上下文安全引用。 */
    @ArchTest
    static final ArchRule api_packages_are_dependency_free = classes()
            .that().resideInAPackage("com.campusoverflow.*.api..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..", "com.campusoverflow.shared..", "com.campusoverflow.*.api..")
            .because("发布语言必须保持稳定、轻量、无框架依赖（FF-5）");

    /** FF-3：上下文之间不允许出现循环依赖。 */
    @ArchTest
    static final ArchRule no_cycles_between_contexts = slices()
            .matching("com.campusoverflow.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule controllers_live_in_interfaces_layer = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..interfaces..");

    @ArchTest
    static final ArchRule entities_live_in_infrastructure_layer = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..infrastructure..")
            .because("JPA 实体是持久化模型，与领域模型分离（ADR-008）");

    /** FF-6：每个上下文的表必须使用自己的前缀，物理隔离数据所有权。 */
    @ArchTest
    static final ArchRule tables_use_context_prefix = classes()
            .that().areAnnotatedWith(Entity.class)
            .should(new ArchCondition<JavaClass>("使用本上下文的表名前缀") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    String expected = TABLE_PREFIX.entrySet().stream()
                            .filter(e -> item.getPackageName().startsWith("com.campusoverflow." + e.getKey() + "."))
                            .map(Map.Entry::getValue).findFirst().orElse(null);
                    if (expected == null || !item.isAnnotatedWith(Table.class)) {
                        return;
                    }
                    Table table = item.getAnnotationOfType(Table.class);
                    if (!table.name().startsWith(expected)) {
                        events.add(SimpleConditionEvent.violated(item,
                                item.getName() + " 的表名 " + table.name() + " 应以 " + expected + " 开头"));
                    }
                }
            });
}
