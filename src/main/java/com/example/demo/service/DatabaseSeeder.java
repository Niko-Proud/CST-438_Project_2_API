package com.example.demo.service;
import com.example.demo.entity.RecalledFood;
import com.example.demo.repository.RecalledFoodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private static final int SEED_COLUMN_COUNT = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final RecalledFoodRepository recalledFoodRepository;

    public DatabaseSeeder(RecalledFoodRepository recalledFoodRepository) {
        this.recalledFoodRepository = recalledFoodRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (recalledFoodRepository.count() == 0) {
            ClassPathResource csv = new ClassPathResource("fda_recalls.csv");
            if (!csv.exists()) {
                LOGGER.warn("FDA recall seed file was not found; skipping import.");
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8))) {
                String header = br.readLine();
                if (header == null || !isExpectedSeedFormat(parseCsvLine(header))) {
                    LOGGER.warn("FDA recall seed file does not match the API seed format; skipping import.");
                    return;
                }

                String line = br.readLine();
                while (line != null) {
                    if (!StringUtils.hasText(line)) {
                        line = br.readLine();
                        continue;
                    }
                    List<String> data = parseCsvLine(line);
                    if (data.size() != SEED_COLUMN_COUNT) {
                        LOGGER.warn("Skipping malformed FDA recall seed row.");
                        line = br.readLine();
                        continue;
                    }

                    RecalledFood recall = new RecalledFood();
                    recall.setProduct(data.get(0));
                    recall.setContaminant(data.get(1));
                    recall.setRecallDate(LocalDate.parse(data.get(2), DateTimeFormatter.ISO_LOCAL_DATE));
                    recall.setManuDate(LocalDate.parse(data.get(3), DateTimeFormatter.ISO_LOCAL_DATE));
                    recall.setBatch(StringUtils.hasText(data.get(4)) ? data.get(4) : null);
                    recalledFoodRepository.save(recall);
                    line = br.readLine();
                }
                LOGGER.info("FDA recalls successfully imported.");
            } catch (Exception e) {
                LOGGER.error("Error seeding database: {}", e.getMessage(), e);
            }
        }
    }

    private boolean isExpectedSeedFormat(List<String> header) {
        return header.size() == SEED_COLUMN_COUNT
                && "product".equalsIgnoreCase(header.get(0))
                && "contaminant".equalsIgnoreCase(header.get(1))
                && "recall_date".equalsIgnoreCase(header.get(2))
                && "manu_date".equalsIgnoreCase(header.get(3))
                && "batch".equalsIgnoreCase(header.get(4));
    }

    /**
     * Parses comma-separated values while preserving commas inside quoted fields.
     * This avoids treating ordinary FDA descriptions as multiple database columns.
     */
    private List<String> parseCsvLine(String line) throws IOException {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        int index = 0;
        while (index < line.length()) {
            char current = line.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                values.add(value.toString().trim());
                value.setLength(0);
            } else {
                value.append(current);
            }
            index++;
        }

        if (quoted) {
            throw new IOException("Unclosed quoted CSV field");
        }
        values.add(value.toString().trim());
        return values;
    }
}
