package org.example;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.testng.annotations.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

    /**
     * Aturan ini memastikan bahwa Test Class di Front Office tidak boleh memanggil
     * constructor Page Object secara langsung (bypass factory).
     *
     * SALAH  : new DashboardManageOrganizationPage(driver)  <- garis merah saat run test
     * BENAR  : getFrontOfficePages().manageOrganization().getDashboardManageOrganizationPage() <- melalui factory
     */
    @Test(description = "Ensure Front Office Tests do not directly instantiate Page Objects")
    public void frontOfficeTestsShouldNotAccessPageObjectsDirectly() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.example");

        // DescribedPredicate dibutuhkan ArchUnit agar pesan pelanggaran menjadi jelas
        DescribedPredicate<JavaConstructorCall> callToFrontOfficePageObject =
                DescribedPredicate.describe(
                        "call to constructor of a Front Office Page Object",
                        call -> call.getTargetOwner().getPackageName().startsWith("org.example.pageObject.frontoffice")
                                && !call.getTargetOwner().getName().endsWith("TestData")
                );

        ArchRule rule = noClasses()
                .that().resideInAPackage("org.example.test.frontoffice..")
                .should().callConstructorWhere(callToFrontOfficePageObject)
                .because("Front Office test classes must access Page Objects via Factories " +
                         "(e.g. getFrontOfficePages().manageOrganization().getDashboardManageOrganizationPage()), " +
                         "not by calling 'new PageObject(driver)' directly.");

        rule.check(importedClasses);
    }

    @Test(description = "Ensure Back Office Tests do not directly instantiate Page Objects")
    public void backOfficeTestsShouldNotAccessPageObjectsDirectly() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.example");

        DescribedPredicate<JavaConstructorCall> callToBackOfficePageObject =
                DescribedPredicate.describe(
                        "call to constructor of a Back Office Page Object",
                        call -> call.getTargetOwner().getPackageName().startsWith("org.example.pageObject.backoffice")
                                && !call.getTargetOwner().getName().endsWith("TestData")
                );

        ArchRule rule = noClasses()
                .that().resideInAPackage("org.example.test.backoffice..")
                .should().callConstructorWhere(callToBackOfficePageObject)
                .because("Back Office test classes must access Page Objects via Factories " +
                         "(e.g. getMainPages().manageOrganization().getSlaSettingFactory().getSlaSettingPage()), " +
                         "not by calling 'new PageObject(driver)' directly.");

        rule.check(importedClasses);
    }

    @Test(description = "Ensure Utility classes do not directly instantiate Page Objects (except components)")
    public void utilityClassesShouldNotAccessPageObjectsDirectly() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.example");

        // Whitelist untuk class berakhiran 'Component'
        DescribedPredicate<JavaConstructorCall> isComponentCall =
                DescribedPredicate.describe("is a call to a Component constructor",
                        call -> call.getTargetOwner().getName().endsWith("Component"));

        ArchRule rule = noClasses()
                .that().resideInAPackage("org.example.utils..")
                .should().callConstructorWhere(
                        targetOwnerIsPackage("org.example.pageObject..")
                        .and(DescribedPredicate.not(targetOwnerIsClass("org.example.pageObject.base.BasePage")))
                        .and(DescribedPredicate.not(isComponentCall))
                )
                .because("Utility classes should receive Page Objects via constructor or parameters (Dependency Injection), " +
                         "not by calling 'new PageObject(driver)' directly. Internal Components (ending in 'Component') and BasePage are allowed.");

        rule.check(importedClasses);
    }

    /**
     * Helper untuk mengecek apakah target owner constructor adalah class tertentu.
     */
    private DescribedPredicate<JavaConstructorCall> targetOwnerIsClass(String className) {
        return DescribedPredicate.describe("target owner is class " + className,
                call -> call.getTargetOwner().getName().equals(className));
    }

    /**
     * Helper untuk mengecek apakah target owner constructor berada dalam package tertentu.
     */
    private DescribedPredicate<JavaConstructorCall> targetOwnerIsPackage(String packageIdentifier) {
        String cleanPackage = packageIdentifier.replace("..", "");
        return DescribedPredicate.describe("target owner is in package " + packageIdentifier,
                call -> call.getTargetOwner().getPackageName().startsWith(cleanPackage));
    }
}
