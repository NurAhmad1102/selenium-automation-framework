package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.nonExcel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.utils.UniqueSeedManager;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.model.MassiveOrgModel;
import org.example.utils.resource.ResourcePathResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MassiveOrgDataFactory {
    private static final Logger logger = LogManager.getLogger(MassiveOrgDataFactory.class);

    private static List<String> readLovFile(String fileName) {
        List<String> list = new ArrayList<>();
        File file = null;

        try {
            Path resolved = ResourcePathResolver.resolve(
                    "org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/data/" + fileName,
                    "src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/data/" + fileName,
                    "target/test-classes/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/data/" + fileName
            );
            file = resolved.toFile();
        } catch (Exception e) {
            File directFile = Paths.get("src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/data/", fileName).toFile();
            if (directFile.exists()) {
                file = directFile;
            }
        }

        if (file != null && file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        list.add(line);
                    }
                }
                logger.info("Loaded {} records from LOV file: {}", list.size(), fileName);
            } catch (Exception e) {
                logger.warn("Error reading LOV file [{}]: {}", fileName, e.getMessage());
            }
        } else {
            logger.warn("LOV file not found: [{}]. Using fallback defaults.", fileName);
        }

        return list;
    }

    public static List<MassiveOrgModel> generate15Organizations(boolean autoPopulateProceed,
                                                               String d0Org,
                                                               String d0Pos,
                                                               String positionNamingType) {
        return generateOrganizations(autoPopulateProceed, d0Org, d0Pos, positionNamingType, 15);
    }

    public static List<MassiveOrgModel> generateOrganizations(boolean autoPopulateProceed,
                                                             String d0Org,
                                                             String d0Pos,
                                                             String positionNamingType,
                                                             int limit) {
        List<String> jobs = readLovFile("JobLOV.txt");
        List<String> locations = readLovFile("LocationLOV.txt");
        List<String> rcCodes = readLovFile("Rc_codeLOV.txt");

        if (jobs.isEmpty()) jobs = List.of("Regulatory Reporting Officer", "IT Head", "Audit Head", "Project Manager", "System Analyst");
        if (locations.isEmpty()) locations = List.of("Kantor Pusat - Menara Asia", "Kantor Pusat - Medco Energy Tower", "Kantor Pusat - Graha CIMB Niaga");
        if (rcCodes.isEmpty()) rcCodes = List.of("1611.47077.1061", "1611.24015.0401", "1611.24008.1003");

        String runId = autoPopulateProceed ? UniqueSeedManager.generateUniqueSeed() : "";

        // Topologic metadata definition for up to 30 organizations (D1 to D6)
        class NodeSpec {
            final String level;
            final String domain;
            final int parentIndex; // -1 means D0

            NodeSpec(String level, String domain, int parentIndex) {
                this.level = level;
                this.domain = domain;
                this.parentIndex = parentIndex;
            }
        }

        List<NodeSpec> allSpecs = List.of(
                // Level D1 (2 orgs)
                new NodeSpec("D1", "Transformation Strategy & Governance", -1),
                new NodeSpec("D1", "Core Technology & Engineering", -1),

                // Level D2 (4 orgs)
                new NodeSpec("D2", "Enterprise Architecture & Platform", 0),
                new NodeSpec("D2", "Quality Governance & Compliance", 0),
                new NodeSpec("D2", "Software Engineering & Delivery", 1),
                new NodeSpec("D2", "Cloud & Infrastructure Operations", 1),

                // Level D3 (5 orgs)
                new NodeSpec("D3", "Automation Testing & COE", 2),
                new NodeSpec("D3", "Performance & Security Assurance", 3),
                new NodeSpec("D3", "Digital Service Engineering", 4),
                new NodeSpec("D3", "API & Middleware Integration", 4),
                new NodeSpec("D3", "Site Reliability & Infrastructure", 5),

                // Level D4 (6 orgs)
                new NodeSpec("D4", "Core Automation Framework Unit", 6),
                new NodeSpec("D4", "Performance Engineering Unit", 7),
                new NodeSpec("D4", "Cyber Security Audit Unit", 7),
                new NodeSpec("D4", "Web Application Platform Unit", 8),
                new NodeSpec("D4", "Microservices Integration Unit", 9),
                new NodeSpec("D4", "Cloud Infrastructure SRE Unit", 10),

                // Level D5 (6 orgs)
                new NodeSpec("D5", "UI Framework Sub-Unit", 11),
                new NodeSpec("D5", "API Automation Sub-Unit", 11),
                new NodeSpec("D5", "Load & Stress Testing Sub-Unit", 12),
                new NodeSpec("D5", "Frontend Platform Sub-Unit", 14),
                new NodeSpec("D5", "DevOps Pipeline Sub-Unit", 15),
                new NodeSpec("D5", "Container Operations Sub-Unit", 16),

                // Level D6 (7 orgs)
                new NodeSpec("D6", "Web Automation Execution Squad", 17),
                new NodeSpec("D6", "Mobile Automation Execution Squad", 17),
                new NodeSpec("D6", "REST API Testing Squad", 18),
                new NodeSpec("D6", "Capacity Planning Squad", 19),
                new NodeSpec("D6", "Design System Squad", 20),
                new NodeSpec("D6", "Continuous Deployment Squad", 21),
                new NodeSpec("D6", "Kubernetes Platform Squad", 22)
        );

        int targetLimit = (limit <= 0 || limit > allSpecs.size()) ? allSpecs.size() : limit;
        List<NodeSpec> specs = allSpecs.subList(0, targetLimit);

        List<MassiveOrgModel> result = new ArrayList<>();

        for (int i = 0; i < specs.size(); i++) {
            NodeSpec spec = specs.get(i);

            String orgName = spec.domain + " " + spec.level + runId;
            String posName = "Pos " + spec.domain + " " + spec.level + runId;

            String parentOrg = (spec.parentIndex == -1) ? d0Org : result.get(spec.parentIndex).getOrgName();
            String parentPos = (spec.parentIndex == -1) ? d0Pos : result.get(spec.parentIndex).getPositionName();

            String job = jobs.get(i % jobs.size());
            String loc = locations.get(i % locations.size());
            String rc = rcCodes.get(i % rcCodes.size());

            MassiveOrgModel model = new MassiveOrgModel(
                    spec.level,
                    orgName,
                    parentOrg,
                    rc,
                    job,
                    positionNamingType,
                    posName,
                    parentPos,
                    loc,
                    "1",
                    false
            );

            if (autoPopulateProceed) {
                UniqueSeedManager.registerOrgName(orgName);
                UniqueSeedManager.registerPosName(posName);
            }

            result.add(model);
        }

        return result;
    }
}
