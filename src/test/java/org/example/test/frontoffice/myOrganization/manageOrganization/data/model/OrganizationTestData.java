package org.example.test.frontoffice.myOrganization.manageOrganization.data.model;

public class OrganizationTestData {
    // Organization Section Data
    private String primaryOrganization;
    private String mergeOrganizationWith; // MERGE_ORG_1
    private String newOrganizationName;
    private String rcCode;

    // Field Tambahan dari Sheet MOVE, SPLIT, MERGE, RENAME, ADD_NEW
    private String level;
    private String newParentOrganization;
    private String expandOrganization;
    private String newOrganizationName1; // Untuk SPLIT
    private String newOrganizationName2; // Untuk SPLIT
    private String newParentPosition;
    private String childOrganization;
    private String affected;
    private String employeeNumber;

    // Field dari Sheet SET_FILTER
    private String directorate;
    private String subDirectorate;
    private String group;
    private String division;
    private String department;

    // Field dari Sheet JOB_POSITION & DUPLICATE
    private String positionName;
    private String organizationName;
    private String fte;
    private String job;

    // Job Section Data Existing
    private String jobName;
    private int jobIndex = 0;

    // Position Section Data Existing
    private int positionIndex = 0;
    private String positionNamingType;
    private String location;
    private String parentPosition;
    private String fteNumbers;
    private boolean isDottedMatrix;

    public String getPrimaryOrganization() { return primaryOrganization; }
    public void setPrimaryOrganization(String primaryOrganization) { this.primaryOrganization = primaryOrganization; }

    public String getNewOrganizationName() { return newOrganizationName; }
    public void setNewOrganizationName(String newOrganizationName) { this.newOrganizationName = newOrganizationName; }

    public String getMergeOrganizationWith() { return mergeOrganizationWith; }
    public void setMergeOrganizationWith(String mergeOrganizationWith) { this.mergeOrganizationWith = mergeOrganizationWith; }

    public String getRcCode() { return rcCode; }
    public void setRcCode(String rcCode) { this.rcCode = rcCode; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getNewParentOrganization() { return newParentOrganization; }
    public void setNewParentOrganization(String newParentOrganization) { this.newParentOrganization = newParentOrganization; }

    public String getExpandOrganization() { return expandOrganization; }
    public void setExpandOrganization(String expandOrganization) { this.expandOrganization = expandOrganization; }

    public String getNewOrganizationName1() { return newOrganizationName1; }
    public void setNewOrganizationName1(String newOrganizationName1) { this.newOrganizationName1 = newOrganizationName1; }

    public String getNewOrganizationName2() { return newOrganizationName2; }
    public void setNewOrganizationName2(String newOrganizationName2) { this.newOrganizationName2 = newOrganizationName2; }

    public String getNewParentPosition() { return newParentPosition; }
    public void setNewParentPosition(String newParentPosition) { this.newParentPosition = newParentPosition; }

    public String getChildOrganization() { return childOrganization; }
    public void setChildOrganization(String childOrganization) { this.childOrganization = childOrganization; }

    public String getAffected() { return affected; }
    public void setAffected(String affected) { this.affected = affected; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getDirectorate() { return directorate; }
    public void setDirectorate(String directorate) { this.directorate = directorate; }

    public String getSubDirectorate() { return subDirectorate; }
    public void setSubDirectorate(String subDirectorate) { this.subDirectorate = subDirectorate; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getFte() { return fte; }
    public void setFte(String fte) { this.fte = fte; }

    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }

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