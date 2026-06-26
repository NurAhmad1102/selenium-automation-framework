package org.example.utils.config;

import org.example.utils.excelReader.ExcelReader;
import org.example.utils.resource.ResourcePathResolver;

import java.nio.file.Path;

public final class LoginDataRepository {
    private static final ExcelReader.LoginData LOGIN_DATA = loadLoginData();

    private LoginDataRepository() {
    }

    public static ExcelReader.LoginData getLoginData() {
        return LOGIN_DATA;
    }

    private static ExcelReader.LoginData loadLoginData() {
        Path filePath = ResourcePathResolver.resolve(
                "data/Data Userlogin.xlsx",
                "src/test/resources/data/Data Userlogin.xlsx",
                "src/test/java/resources/data/Data Userlogin.xlsx"
        );
        return ExcelReader.readExcel(filePath.toString());
    }
}
