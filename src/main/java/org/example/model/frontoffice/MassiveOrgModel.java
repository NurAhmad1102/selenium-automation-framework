package org.example.model.frontoffice;

public class MassiveOrgModel {
    private String action = "ADD_NEW";
    private String currentLevel;
    private String currentOrgName;
    private String currentParentOrgName;
    private String currentRcCode;

    private String level;
    private String orgName;
    private String parentOrgName;
    private String rcCode;
    private String jobName;
    private String positionNamingType = "Area of Responsibility / Job Scope";
    private String positionName;
    private String parentPositionName;
    private String location;
    private String fte = "1";
    private boolean dottedMatrix = false;

    public MassiveOrgModel() {}

    public MassiveOrgModel(String action, String currentLevel, String currentOrgName, String currentParentOrgName,
                           String currentRcCode, String level, String orgName, String parentOrgName,
                           String rcCode, String jobName, String positionNamingType, String positionName,
                           String parentPositionName, String location, String fte, boolean dottedMatrix) {
        this.action = action;
        this.currentLevel = currentLevel;
        this.currentOrgName = currentOrgName;
        this.currentParentOrgName = currentParentOrgName;
        this.currentRcCode = currentRcCode;
        this.level = level;
        this.orgName = orgName;
        this.parentOrgName = parentOrgName;
        this.rcCode = rcCode;
        this.jobName = jobName;
        this.positionNamingType = positionNamingType;
        this.positionName = positionName;
        this.parentPositionName = parentPositionName;
        this.location = location;
        this.fte = fte;
        this.dottedMatrix = dottedMatrix;
    }

    public MassiveOrgModel(String level, String orgName, String parentOrgName, String rcCode,
                           String jobName, String positionNamingType, String positionName,
                           String parentPositionName, String location, String fte, boolean dottedMatrix) {
        this.level = level;
        this.orgName = orgName;
        this.parentOrgName = parentOrgName;
        this.rcCode = rcCode;
        this.jobName = jobName;
        this.positionNamingType = positionNamingType;
        this.positionName = positionName;
        this.parentPositionName = parentPositionName;
        this.location = location;
        this.fte = fte;
        this.dottedMatrix = dottedMatrix;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getCurrentOrgName() {
        return currentOrgName;
    }

    public void setCurrentOrgName(String currentOrgName) {
        this.currentOrgName = currentOrgName;
    }

    public String getCurrentParentOrgName() {
        return currentParentOrgName;
    }

    public void setCurrentParentOrgName(String currentParentOrgName) {
        this.currentParentOrgName = currentParentOrgName;
    }

    public String getCurrentRcCode() {
        return currentRcCode;
    }

    public void setCurrentRcCode(String currentRcCode) {
        this.currentRcCode = currentRcCode;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getParentOrgName() {
        return parentOrgName;
    }

    public void setParentOrgName(String parentOrgName) {
        this.parentOrgName = parentOrgName;
    }

    public String getRcCode() {
        return rcCode;
    }

    public void setRcCode(String rcCode) {
        this.rcCode = rcCode;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getPositionNamingType() {
        return positionNamingType;
    }

    public void setPositionNamingType(String positionNamingType) {
        this.positionNamingType = positionNamingType;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getParentPositionName() {
        return parentPositionName;
    }

    public void setParentPositionName(String parentPositionName) {
        this.parentPositionName = parentPositionName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFte() {
        return fte;
    }

    public void setFte(String fte) {
        this.fte = fte;
    }

    public boolean isDottedMatrix() {
        return dottedMatrix;
    }

    public void setDottedMatrix(boolean dottedMatrix) {
        this.dottedMatrix = dottedMatrix;
    }

    @Override
    public String toString() {
        return "MassiveOrgModel{" +
                "level='" + level + '\'' +
                ", orgName='" + orgName + '\'' +
                ", parentOrgName='" + parentOrgName + '\'' +
                ", rcCode='" + rcCode + '\'' +
                ", jobName='" + jobName + '\'' +
                ", positionName='" + positionName + '\'' +
                ", parentPositionName='" + parentPositionName + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
