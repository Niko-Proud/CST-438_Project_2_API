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
import java.time.format.DateTimeParseException;

/**
 * Loads the checked-in FDA export into the recall table when it is empty.
 * Codex was used to adapt this importer from the original five-column seed
 * format to the current 25-column FDA export and to document the field mapping.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {
    private static final int SEED_COLUMN_COUNT = 5;
    private static final int FDA_COLUMN_COUNT = 25;
    private static final int FDA_PRODUCT_INDEX = 17;
    private static final int FDA_CONTAMINANT_INDEX = 19;
    private static final int FDA_RECALL_DATE_INDEX = 20;
    private static final int FDA_MANU_DATE_INDEX = 21;
    private static final int FDA_BATCH_INDEX = 24;
    private static final int MAX_PRODUCT_LENGTH = 200;
    private static final int MAX_CONTAMINANT_LENGTH = 255;
    private static final int MAX_BATCH_LENGTH = 100;
    private static final int NEXT_CHARACTER_OFFSET = 1;
    private static final char QUOTE_CHARACTER = '"';
    private static final char CSV_SEPARATOR = ',';
    private static final DateTimeFormatter FDA_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");
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
                List<String> headerValues = header == null ? List.of() : parseCsvLine(header);
                boolean fdaFormat = isFdaSeedFormat(headerValues);
                if (header == null || (!isExpectedSeedFormat(headerValues) && !fdaFormat)) {
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
                    int expectedColumnCount = fdaFormat ? FDA_COLUMN_COUNT : SEED_COLUMN_COUNT;
                    if (data.size() != expectedColumnCount) {
                        LOGGER.warn("Skipping malformed FDA recall seed row.");
                        line = br.readLine();
                        continue;
                    }

                    RecalledFood recall = fdaFormat
                            ? createFromFdaRow(data)
                            : createFromSeedRow(data);
                    if (recall != null) {
                        recalledFoodRepository.save(recall);
                    }
                    line = br.readLine();
                }
                LOGGER.info("FDA recalls successfully imported.");
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error seeding database: {}", e.getMessage(), e);
                }
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

    private boolean isFdaSeedFormat(List<String> header) {
        return header.size() == FDA_COLUMN_COUNT
                && "Product Description".equalsIgnoreCase(header.get(FDA_PRODUCT_INDEX))
                && "Reason for Recall".equalsIgnoreCase(header.get(FDA_CONTAMINANT_INDEX))
                && "Recall Initiation Date".equalsIgnoreCase(header.get(FDA_RECALL_DATE_INDEX));
    }

    private RecalledFood createFromSeedRow(List<String> data) {
        RecalledFood recall = new RecalledFood();
        recall.setProduct(data.get(0));
        recall.setContaminant(data.get(1));
        recall.setRecallDate(LocalDate.parse(data.get(2), DateTimeFormatter.ISO_LOCAL_DATE));
        recall.setManuDate(LocalDate.parse(data.get(3), DateTimeFormatter.ISO_LOCAL_DATE));
        recall.setBatch(StringUtils.hasText(data.get(4)) ? data.get(4) : null);
        return recall;
    }

    private RecalledFood createFromFdaRow(List<String> data) {
        LocalDate recallDate = parseFdaDate(data.get(FDA_RECALL_DATE_INDEX), null);
        if (recallDate == null) {
            LOGGER.warn("Skipping FDA recall row without a valid recall date.");
            return null;
        }

        RecalledFood recall = new RecalledFood();
        recall.setProduct(limit(data.get(FDA_PRODUCT_INDEX), MAX_PRODUCT_LENGTH));
        recall.setContaminant(limit(data.get(FDA_CONTAMINANT_INDEX), MAX_CONTAMINANT_LENGTH));
        recall.setRecallDate(recallDate);
        // FDA does not provide a manufacturing date in this export. The
        // classification date is the closest available event date; when it is
        // unavailable, retaining the recall date keeps the required field valid.
        recall.setManuDate(parseFdaDate(data.get(FDA_MANU_DATE_INDEX), recallDate));
        recall.setBatch(limit(data.get(FDA_BATCH_INDEX), MAX_BATCH_LENGTH));
        return recall;
    }

    private LocalDate parseFdaDate(String value, LocalDate fallback) {
        if (!StringUtils.hasText(value) || "N/A".equalsIgnoreCase(value.trim())) {
            return fallback;
        }
        try {
            return LocalDate.parse(value.trim(), FDA_DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            return fallback;
        }
    }

    private String limit(String value, int maximumLength) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.length() > maximumLength ? trimmed.substring(0, maximumLength) : trimmed;
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
            if (current == QUOTE_CHARACTER) {
                if (quoted && index + NEXT_CHARACTER_OFFSET < line.length()
                        && line.charAt(index + NEXT_CHARACTER_OFFSET) == QUOTE_CHARACTER) {
                    value.append(QUOTE_CHARACTER);
                    index += NEXT_CHARACTER_OFFSET;
                } else {
                    quoted = !quoted;
                }
            } else if (current == CSV_SEPARATOR && !quoted) {
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
