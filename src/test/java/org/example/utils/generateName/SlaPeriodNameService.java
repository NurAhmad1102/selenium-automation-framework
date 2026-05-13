package org.example.utils.generateName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlaPeriodNameService {
    private static final Logger logger = LogManager.getLogger(SlaPeriodNameService.class);
    private static SlaNameGenerator generator;

    static {
        String USED_NAMES_PATH = System.getProperty("user.dir") + "\\src\\test\\java\\org\\example\\utils\\generateName\\usedNames\\used_names.txt";

        SlaNameRepository repo = new SlaNameRepository();
        generator = new SlaNameGenerator(repo.getSlaNames(), USED_NAMES_PATH);
        logger.info("Remaining available SLA names: {}", generator.getRemainingCountAvail());
    }

    public static String getGenerateSlaName(){
        return generator.generateSlaName();
    }
}
