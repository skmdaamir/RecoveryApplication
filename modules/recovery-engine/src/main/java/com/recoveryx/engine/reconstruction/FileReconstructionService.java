package com.recoveryx.engine.reconstruction;

import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.core.domain.file.FileFragment;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.engine.integrity.FileIntegrityChecker;
import com.recoveryx.engine.integrity.IntegrityReport;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Objects;

/**
 * Reconstructs recoverable files by reading their fragments from disk sectors
 * and assembling the output byte stream to a target destination directory.
 */
public final class FileReconstructionService {

    private static final Logger log = LoggerFactory.getLogger(FileReconstructionService.class);

    private static final int BYTES_PER_SECTOR = 512;

    private final SectorReaderService sectorReaderService;

    public FileReconstructionService(SectorReaderService sectorReaderService) {
        this.sectorReaderService = Objects.requireNonNull(sectorReaderService, "sectorReaderService");
    }

    /**
     * Reads all fragments of a RecoverableFile from device sectors, stitches them together,
     * performs integrity checks, and writes the reconstructed file to targetDirectory.
     *
     * @param file            the candidate file to recover
     * @param devicePath      the source device path (e.g. "\\.\PhysicalDrive1" or "E:\")
     * @param targetDir       destination output folder path
     * @return Path to the newly written file
     */
    public Path reconstructFile(RecoverableFile file, String devicePath, String targetDir) throws IOException {
        ValidationUtils.requireNonNull(file, "file");
        ValidationUtils.requireNotBlank(devicePath, "devicePath");
        ValidationUtils.requireNotBlank(targetDir, "targetDir");

        // 1. Validate drive overwrite safety
        DestinationSafetyValidator.validateSafety(targetDir, devicePath);

        // 2. Read and assemble byte payload from fragments
        byte[] fileBytes = assembleFileContent(file, devicePath);

        // 3. Check integrity
        IntegrityReport report = FileIntegrityChecker.verify(fileBytes, file.extension());
        log.info("Reconstructed file [{}] ({} bytes) | MD5: {} | ValidHeader: {}",
                file.name(), fileBytes.length, report.md5Hash(), report.validHeader());

        // 4. Resolve output destination file path
        Path targetFolder = Paths.get(targetDir).toAbsolutePath().normalize();
        String safeName = sanitizeFileName(file.name());
        Path outputPath = targetFolder.resolve(safeName);

        // Avoid overwriting if file with same name exists
        int counter = 1;
        while (Files.exists(outputPath)) {
            String nameNoExt = safeName.contains(".") ? safeName.substring(0, safeName.lastIndexOf('.')) : safeName;
            String ext = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf('.')) : "";
            outputPath = targetFolder.resolve(nameNoExt + "_" + counter + ext);
            counter++;
        }

        // 5. Write to destination file
        Files.write(outputPath, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        // 6. Set original creation / modification timestamps if available
        if (file.modifiedDate() != null) {
            try {
                Files.setLastModifiedTime(outputPath, FileTime.from(file.modifiedDate()));
            } catch (Exception e) {
                log.debug("Could not set modification timestamp for {}: {}", outputPath, e.getMessage());
            }
        }

        return outputPath;
    }

    /**
     * Reads all fragments and stitches the byte content together in order.
     */
    public byte[] assembleFileContent(RecoverableFile file, String devicePath) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (file.fragments().isEmpty()) {
            log.warn("File [{}] has no physical fragments recorded", file.name());
            return new byte[0];
        }

        for (FileFragment fragment : file.fragments()) {
            long startByte = fragment.startOffsetBytes();
            long lenBytes = fragment.lengthBytes();

            if (startByte < 0 || lenBytes <= 0) {
                continue;
            }

            long startSector = startByte / BYTES_PER_SECTOR;
            int sectorCount = (int) Math.max(1, (lenBytes + BYTES_PER_SECTOR - 1) / BYTES_PER_SECTOR);

            try {
                SectorReadResult result = sectorReaderService.read(devicePath, startSector, sectorCount, BYTES_PER_SECTOR);
                byte[] data = result.data();

                int offsetInSector = (int) (startByte % BYTES_PER_SECTOR);
                int bytesToWrite = (int) Math.min(lenBytes, data.length - offsetInSector);

                if (bytesToWrite > 0 && offsetInSector < data.length) {
                    out.write(data, offsetInSector, bytesToWrite);
                }
            } catch (Exception e) {
                log.error("Error reading sector range {} count {} for file {}: {}",
                        startSector, sectorCount, file.name(), e.getMessage());
            }
        }

        byte[] rawBytes = out.toByteArray();

        // Truncate to exact fileSize if raw sector size is larger
        if (file.fileSize() > 0 && rawBytes.length > file.fileSize()) {
            byte[] truncated = new byte[(int) file.fileSize()];
            System.arraycopy(rawBytes, 0, truncated, 0, (int) file.fileSize());
            return truncated;
        }

        return rawBytes;
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "recovered_file.dat";
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
