package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LogMonitor {
    private static final Logger logger = Logger.getLogger(LogMonitor.class.getName());
    private static final Duration WARNING_THRESHOLD = Duration.ofMinutes(5);
    private static final Duration ERROR_THRESHOLD = Duration.ofMinutes(10);

    /**
     * Parses the log file and extracts job start and end times.
     */
    static Map<String, JobEntry> parseLogFile(String filePath) {
        Map<String, JobEntry> jobs = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // Assuming CSV format

                if (parts.length != 4) {
                    logger.warning("Malformed log entry: " + line);
                    continue;
                }

                try {
                    LocalTime timestamp = LocalTime.parse(parts[0].trim()); // Parse HH:MM:SS format
                    String jobDescription = parts[1].trim();
                    String pid = parts[2].trim();
                    String status = parts[3].trim();

                    jobs.putIfAbsent(pid, new JobEntry(jobDescription));

                    if ("START".equalsIgnoreCase(status)) {
                        jobs.get(pid).setStartTime(timestamp);
                    } else if ("END".equalsIgnoreCase(status)) {
                        jobs.get(pid).setEndTime(timestamp);
                    }
                } catch (Exception e) {
                    logger.warning("Error processing log entry: " + line + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.severe("Error reading log file: " + e.getMessage());
        }

        return jobs;
    }

    /**
     * Analyzes job durations and logs warnings/errors.
     */
    static void analyzeJobs(Map<String, JobEntry> jobs) {
        for (Map.Entry<String, JobEntry> entry : jobs.entrySet()) {
            String pid = entry.getKey();
            JobEntry job = entry.getValue();

            if (job.getStartTime() == null || job.getEndTime() == null) {
                logger.warning("Incomplete job data for PID: " + pid);
                continue;
            }

            Duration duration = Duration.between(job.getStartTime(), job.getEndTime());
            String message = "Job: " + job.getDescription() + " (PID: " + pid + ") took " + duration.toMinutes() + " minutes.";

            if (duration.compareTo(ERROR_THRESHOLD) > 0) {
                logger.severe("ERROR: " + message + " Exceeded error threshold!");
            } else if (duration.compareTo(WARNING_THRESHOLD) > 0) {
                logger.warning("WARNING: " + message + " Exceeded warning threshold!");
            } else {
                logger.info("INFO: " + message);
            }
        }
    }
}