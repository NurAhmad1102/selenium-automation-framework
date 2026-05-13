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
     * BENAR  : pages.manageOrganization().getDashboardPage() <- melalui factory
     */
    @Test(description = "Ensure Front Office Tests do not directly instantiate Page Objects")
    public void frontOfficeTestsShouldNotAccessPageObjectsDirectly() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.example");

        // DescribedPredicate dibutuhkan ArchUnit agar pesan pelanggaran menjadi jelas
        DescribedPredicate<JavaConstructorCall> callToFrontOfficePageObject =
                DescribedPredicate.describe(
                        "call to constructor of a Front Office Page Object",
                        // 'call' = JavaConstructorCall, getTargetOwner() = class yang di-new
                        call -> call.getTargetOwner().getPackageName()
                                    .startsWith("org.example.pageObject.frontoffice")
                );

        ArchRule rule = noClasses()
                .that().resideInAPackage("org.example.test.frontoffice..")
                .should().callConstructorWhere(callToFrontOfficePageObject)
                .because("Front Office test classes must access Page Objects via Factories " +
                         "(e.g. pages.manageOrganization().getDashboardPage()), " +
                         "not by calling 'new PageObject(driver)' directly.");

        rule.check(importedClasses);
    }
}
