package org.example.utils.generateName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Random;

public class SlaNameGenerator {
    private static final Logger logger = LogManager.getLogger(SlaNameGenerator.class);
    private List<String> availableSlaName;
    private File storageFile;
    private Random random = new Random();

    protected SlaNameGenerator(List<String> slaName, String storagePath){
        storageFile = new File(storagePath);
        availableSlaName = new ArrayList<>();

//        buat kombinasi
        for(int i = 0; i < slaName.size(); i++){
            for(int j = 0; j < slaName.size(); j++){
                for (int k = 0; k < slaName.size(); k++){
                    if(i != j && j != k && i !=k){
                        availableSlaName.add(
                                slaName.get(i) + " " +
                                slaName.get(j) + " " +
                                slaName.get(k)
                        );
                    }
                }
            }
        }
        // hapus kombinasi yg sudah muncul
        removeUsedSlaName();
    }

    private void removeUsedSlaName(){
        if(!storageFile.exists()) return;

        try (
            BufferedReader reader = new BufferedReader(new FileReader(storageFile))){
                String line;
                while ((line = reader.readLine()) != null){
                    availableSlaName.remove(line.trim());
                }
        }
        catch (IOException e) {
            logger.error("Failed to read used SLA names from file: {}", storageFile.getAbsolutePath(), e);
        }

    }

    private void saveUsedSlaName(String slaName){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile,true))){
            writer.write(slaName);
            writer.newLine();
        } catch (IOException e){
            logger.error("Failed to save used SLA name '{}' to file: {}",
                    slaName, storageFile.getAbsolutePath(), e);
        }
    }

    public String generateSlaName(){
        if(availableSlaName.isEmpty()){
            throw new RuntimeException("Semua Kombinasi telah dipakai");
        }

        int index = random.nextInt(availableSlaName.size());
        String fullName = availableSlaName.get(index);
        availableSlaName.remove(index);
        saveUsedSlaName(fullName);
        return fullName;
    }

    public int getRemainingCountAvail(){
        return availableSlaName.size();
    }

}
