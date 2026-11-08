package com.automation.framework.core.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.testng.annotations.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class CoreArchitectureTest {

    @Test(description = "Verify that framework-core is decoupled and has zero dependencies on consumer or sample modules")
    public void coreShouldNotDependOnConsumerPackages() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("com.automation.framework.core");

        noClasses().that().resideInAPackage("com.automation.framework.core..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.automation.framework.sample..",
                        "org.example..",
                        "com.company..",
                        "com.app.."
                )
                .because("Core framework library must remain completely agnostic to any consumer application or sample")
                .check(importedClasses);
    }
}
