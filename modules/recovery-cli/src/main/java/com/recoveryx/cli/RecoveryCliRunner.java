package com.recoveryx.cli;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.core.domain.recovery.RecoveryRequest;
import com.recoveryx.core.domain.recovery.RecoveryResult;
import com.recoveryx.core.domain.recovery.RecoveryTarget;
import com.recoveryx.core.domain.scan.ScanProgress;
import com.recoveryx.core.model.device.StorageDevice;
import com.recoveryx.core.model.scan.ScanMode;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.engine.reconstruction.FileReconstructionService;
import com.recoveryx.engine.service.DefaultRecoveryOrchestratorPort;
import com.recoveryx.report.exporter.HtmlReportExporter;
import com.recoveryx.scanner.engine.ScanEngine;
import com.recoveryx.scanner.signature.SignatureDatabase;
import com.recoveryx.storage.service.RawSectorReader;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Command-Line Interface (CLI) runner for RecoveryX Pro.
 * Allows running drive scans, signature carving, file recovery, and HTML report exports from terminal commands.
 */
@Component
public class RecoveryCliRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RecoveryCliRunner.class);

    @Override
    public void run(String... args) throws Exception {
        if (args == null || args.length == 0) {
            log.info("RecoveryX Pro CLI ready. Usage: java -jar recovery-ui.jar --device <path> --mode <QUICK|DEEP> --output <dir>");
            return;
        }

        String deviceArg = null;
        String imageFileArg = null;
        String modeArg = "QUICK";
        String outputArg = null;

        for (int i = 0; i < args.length; i++) {
            if ("--device".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                deviceArg = args[++i];
            } else if ("--image-file".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                imageFileArg = args[++i];
            } else if ("--mode".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                modeArg = args[++i];
            } else if ("--output".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                outputArg = args[++i];
            }
        }

        if (deviceArg == null && imageFileArg == null) {
            log.warn("CLI execution: No --device or --image-file argument specified. Usage: java -jar recovery-ui.jar --image-file <path.img> --output <dir>");
            return;
        }

        String targetPath = imageFileArg != null ? imageFileArg : deviceArg;
        boolean isImageFile = imageFileArg != null;

        log.info("=== RecoveryX Pro CLI Execution ===");
        log.info("Target: {} (Type: {})", targetPath, isImageFile ? "Disk Image File" : "Physical Device");
        log.info("Scan Mode: {}", modeArg);

        ScanMode scanMode = "DEEP".equalsIgnoreCase(modeArg) || isImageFile ? ScanMode.DEEP : ScanMode.QUICK;
        
        long totalBytes = 100L * 1024 * 1024; // Default 100 MB
        if (isImageFile) {
            File f = new File(imageFileArg);
            if (f.exists()) {
                totalBytes = f.length();
            }
        }

        StorageDevice device = StorageDevice.builder()
                .deviceId("cli-target-1")
                .displayName(isImageFile ? "Disk Image: " + new File(targetPath).getName() : "CLI Target Drive")
                .devicePath(targetPath)
                .totalBytes(totalBytes)
                .freeBytes(0L)
                .readable(true)
                .build();

        ScanRequest scanRequest = ScanRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .storageDevice(device)
                .scanMode(scanMode)
                .build();

        RawSectorReader rawReader;
        if (isImageFile) {
            rawReader = new com.recoveryx.storage.service.impl.DiskImageSectorReader(Paths.get(targetPath));
        } else {
            rawReader = req -> new com.recoveryx.storage.model.SectorReadResult(req, new byte[512 * req.sectorCount()], Instant.now());
        }

        SectorReaderService sectorReaderService = new SectorReaderService(rawReader);
        com.recoveryx.filesystem.service.FileSystemService fileSystemService = new com.recoveryx.filesystem.service.FileSystemService(sectorReaderService);

        SignatureDatabase db = new SignatureDatabase();
        ScanEngine scanEngine = new ScanEngine(sectorReaderService, fileSystemService, db);

        List<RecoverableFile> foundFiles = Collections.synchronizedList(new ArrayList<>());
        ScanProgress initialProgress = scanEngine.startScan(scanRequest, foundFiles::add);

        log.info("CLI Scan initiated. Scanning target...");

        if (outputArg != null) {
            Path reportPath = Paths.get(outputArg, "cli_report_" + System.currentTimeMillis() + ".html");
            HtmlReportExporter.exportHtmlReport(scanRequest.getRequestId(), foundFiles, reportPath);
            log.info("Generated CLI diagnostic HTML report at: {}", reportPath);
        }
    }
}
