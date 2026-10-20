package com.example.demo.service;
import com.example.demo.entity.RecalledFood;
import com.example.demo.repository.RecalledFoodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private final RecalledFoodRepository recalledFoodRepository;

    public DatabaseSeeder(RecalledFoodRepository recalledFoodRepository) {
        this.recalledFoodRepository = recalledFoodRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (recalledFoodRepository.count() == 0) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("/fda_recalls.csv")))) {
                String line;
                boolean isFirstLine = true;

                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] data = line.split(",");
                    if (data.length >= 5) {
                        RecalledFood recall = new RecalledFood();
                        recall.setProduct(data[0].trim());
                        recall.setContaminant(data[1].trim());

                        recall.setRecallDate(LocalDate.parse(data[2].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
                        recall.setManuDate(LocalDate.parse(data[3].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
                        recall.setBatch(data[4].trim());
                        recalledFoodRepository.save(recall);
                    }
                }
                System.out.println("FDA recalls successfully imported!");
            } catch (Exception e) {
                System.err.println("Error seeding database: " + e.getMessage());
            }
        }
    }
}
