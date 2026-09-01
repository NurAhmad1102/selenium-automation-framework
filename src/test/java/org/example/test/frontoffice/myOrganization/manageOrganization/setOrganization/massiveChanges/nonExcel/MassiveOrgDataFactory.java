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
        List<String> depts = readLovFile("DeptLOV.txt");
        List<String> jobs = readLovFile("JobLOV.txt");
        List<String> locations = readLovFile("LocationLOV.txt");
        List<String> rcCodes = readLovFile("Rc_codeLOV.txt");

        if (depts.isEmpty()) {
            depts = List.of("Transformation Office", "Core Technology", "Enterprise Architecture", "Quality Governance",
                    "Software Engineering", "Cloud Operations", "Automation COE", "Security Assurance", "Digital Banking", "Risk Analytics");
        }
        if (jobs.isEmpty()) jobs = List.of("Regulatory Reporting Officer", "IT Head", "Audit Head", "Project Manager", "System Analyst");
        if (locations.isEmpty()) locations = List.of("Kantor Pusat - Menara Asia", "Kantor Pusat - Medco Energy Tower", "Kantor Pusat - Graha CIMB Niaga");
        if (rcCodes.isEmpty()) rcCodes = List.of("1611.47077.1061", "1611.24015.0401", "1611.24008.1003");

        String runId = autoPopulateProceed ? UniqueSeedManager.generateUniqueSeed() : "";

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

        int target = Math.max(1, limit);
        int[] levelCounts = new int[6];
        if (target <= 6) {
            for (int i = 0; i < 6; i++) {
                levelCounts[i] = (i < target) ? 1 : 0;
            }
        } else {
            double[] weights = {0.08, 0.14, 0.20, 0.22, 0.18, 0.18};
            for (int i = 0; i < 6; i++) {
                levelCounts[i] = 1;
            }
            int rem = target - 6;
            int sumAlloc = 0;
            for (int i = 0; i < 6; i++) {
                int alloc = (int) (rem * weights[i]);
                levelCounts[i] += alloc;
                sumAlloc += levelCounts[i];
            }
            int leftover = target - sumAlloc;
            int[] priorityOrder = {3, 2, 4, 1, 5, 0}; // D4, D3, D5, D2, D6, D1
            for (int p : priorityOrder) {
                if (leftover <= 0) break;
                levelCounts[p]++;
                leftover--;
            }
        }

        String[] levelNames = {"D1", "D2", "D3", "D4", "D5", "D6"};
        java.util.Map<String, List<Integer>> createdByLevel = new java.util.HashMap<>();
        for (String lvl : levelNames) {
            createdByLevel.put(lvl, new ArrayList<>());
        }

        List<NodeSpec> specs = new ArrayList<>();
        int nodeSeq = 0;

        for (int l = 0; l < levelNames.length; l++) {
            String lvl = levelNames[l];
            int count = levelCounts[l];
            for (int k = 0; k < count; k++) {
                nodeSeq++;
                int deptIdx = (nodeSeq - 1) % depts.size();
                int deptCycle = ((nodeSeq - 1) / depts.size()) + 1;
                String deptName = depts.get(deptIdx);
                String domain = (deptCycle == 1) ? deptName : deptName + " " + deptCycle;

                int parentIndex = -1;
                if (!"D1".equals(lvl)) {
                    String parentLevel = levelNames[l - 1];
                    List<Integer> parentPool = createdByLevel.get(parentLevel);
                    if (parentPool != null && !parentPool.isEmpty()) {
                        parentIndex = parentPool.get(createdByLevel.get(lvl).size() % parentPool.size());
                    }
                }

                int currentIndex = specs.size();
                specs.add(new NodeSpec(lvl, domain, parentIndex));
                createdByLevel.get(lvl).add(currentIndex);
            }
        }

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
