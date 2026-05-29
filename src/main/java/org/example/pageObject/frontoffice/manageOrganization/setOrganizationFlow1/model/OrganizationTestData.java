package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.model;

public class OrganizationTestData {
    // Organization Section Data
    private String primaryOrganization;
    private String mergeOrganizationWith;
    private String newOrganizationName;
    private String rcCode;
    
    // Job Section Data
    private String jobName;
    private int jobIndex = 0;
    
    // Position Section Data
    private int positionIndex = 0;
    private String positionNamingType;
    private String location;
    private String parentPosition;
    private String fteNumbers;
    private boolean isDottedMatrix;

    // Getters and Setters
    public String getPrimaryOrganization() { return primaryOrganization; }
    public void setPrimaryOrganization(String primaryOrganization) { this.primaryOrganization = primaryOrganization; }

    public String getNewOrganizationName() { return newOrganizationName; }
    public void setNewOrganizationName(String newOrganizationName) { this.newOrganizationName = newOrganizationName; }

    public String getMergeOrganizationWith() {return mergeOrganizationWith;}
    public void setMergeOrganizationWith(String mergeOrganizationWith) { this.mergeOrganizationWith = mergeOrganizationWith; }

    public String getRcCode() { return rcCode; }
    public void setRcCode(String rcCode) { this.rcCode = rcCode; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public int getJobIndex() { return jobIndex; }
    public void setJobIndex(int jobIndex) { this.jobIndex = jobIndex; }

    public int getPositionIndex() { return positionIndex; }
    public void setPositionIndex(int positionIndex) { this.positionIndex = positionIndex; }

    public String getPositionNamingType() { return positionNamingType; }
    public void setPositionNamingType(String positionNamingType) { this.positionNamingType = positionNamingType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getParentPosition() { return parentPosition; }
    public void setParentPosition(String parentPosition) { this.parentPosition = parentPosition; }

    public String getFteNumbers() { return fteNumbers; }
    public void setFteNumbers(String fteNumbers) { this.fteNumbers = fteNumbers; }

    public boolean isDottedMatrix() { return isDottedMatrix; }
    public void setDottedMatrix(boolean dottedMatrix) { isDottedMatrix = dottedMatrix; }
}
