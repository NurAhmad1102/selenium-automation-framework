package org.example.config.userLoginStructure;

public class UserloginConfig {
    private String nip;
    private String password;
    private String username;
    private String role;
    private String urlBackOffice;
    private String urlFrontOffice;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrlBackOffice() {
        return urlBackOffice;
    }

    public void setUrlBackOffice(String urlBackOffice) {
        this.urlBackOffice = urlBackOffice;
    }

    public String getUrlFrontOffice() {
        return urlFrontOffice;
    }

    public void setUrlFrontOffice(String urlFrontOffice) {
        this.urlFrontOffice = urlFrontOffice;
    }
}
