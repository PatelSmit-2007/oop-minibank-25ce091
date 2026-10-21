package project.mini_bank.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class ReportGenerator {

    public static String generate(Path logsFolder, Path reportFile) throws IOException {
        if (!Files.exists(logsFolder)) {
            Files.createDirectories(logsFolder);
        }

        long totalDeposits = 0;
        long totalWithdrawals = 0;
        int fileCount = 0;
        StringBuilder fileDetails = new StringBuilder();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsFolder)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    fileCount++;
                    BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                    fileDetails.append(" - File: ").append(entry.getFileName())
                            .append(" | Size: ").append(attrs.size()).append(" bytes")
                            .append(" | Modified: ").append(attrs.lastModifiedTime())
                            .append("\n");

                    try (BufferedReader reader = Files.newBufferedReader(entry, StandardCharsets.UTF_8)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String trimmed = line.trim();
                            if (trimmed.isEmpty()) {
                                continue;
                            }

                            String[] parts = trimmed.split("\\s+");
                            if (parts.length >= 2) {
                                String type = parts[0].toUpperCase();
                                try {
                                    long amount = Long.parseLong(parts[parts.length - 1]);
                                    if (type.contains("DEPOSIT")) {
                                        totalDeposits += amount;
                                    } else if (type.contains("WITHDRAW")) {
                                        totalWithdrawals += amount;
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    }
                }
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("================ MINI BANK END-OF-DAY REPORT ================\n");
        report.append("Logs Scanned       : ").append(fileCount).append(" file(s)\n");
        report.append("Total Deposits     : ₹").append(totalDeposits).append("\n");
        report.append("Total Withdrawals  : ₹").append(totalWithdrawals).append("\n");
        report.append("Net Cashflow       : ₹").append(totalDeposits - totalWithdrawals).append("\n");
        report.append("Log Files Details  :\n").append(fileDetails);
        report.append("=============================================================");

        String reportContent = report.toString();
        if (reportFile.getParent() != null && !Files.exists(reportFile.getParent())) {
            Files.createDirectories(reportFile.getParent());
        }
        Files.writeString(reportFile, reportContent, StandardCharsets.UTF_8);

        return reportContent;
    }
}
