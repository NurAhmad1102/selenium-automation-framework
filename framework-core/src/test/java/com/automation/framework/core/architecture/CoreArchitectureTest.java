package com.automation.framework.core.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.testng.annotations.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class CoreArchitectureTest {

    @Test
    public void coreFrameworkMustBeAgnosticAndNotDependOnSpecificApplications() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("com.automation.framework.core");

        noClasses().that().resideInAPackage("com.automation.framework.core..")
                .should().dependOnClassesThat().resideInAnyPackage("org.example..", "com.arjuna..")
                .because("Core framework library must remain completely agnostic to any specific project")
                .check(importedClasses);
    }
}
