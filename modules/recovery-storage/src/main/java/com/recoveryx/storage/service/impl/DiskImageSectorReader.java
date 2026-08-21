package com.recoveryx.storage.service.impl;

import com.recoveryx.storage.model.SectorReadRequest;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.RawSectorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Reads raw sectors from a disk image file (.img, .dd, .iso, .raw, .bin).
 *
 * <p>This implementation allows the scan engine to treat a flat binary disk image
 * exactly the same as a physical drive — no physical device or card reader required.
 * The image must be a sector-aligned, uncompressed raw dump of the storage medium.</p>
 *
 * <p>Thread-safe: the underlying {@link FileChannel} is shared; concurrent reads are
 * co-ordinated via {@link FileChannel#read(ByteBuffer, long)} which is position-independent.</p>
 */
public final class DiskImageSectorReader implements RawSectorReader {

    private static final Logger log = LoggerFactory.getLogger(DiskImageSectorReader.class);

    private final Path imagePath;
    private final FileChannel channel;
    private final long imageSizeBytes;

    /**
     * Opens the given disk image file for reading.
     *
     * @param imagePath absolute path to the .img / .dd / .iso file
     * @throws IOException if the file cannot be opened or read
     */
    public DiskImageSectorReader(Path imagePath) throws IOException {
        this.imagePath = Objects.requireNonNull(imagePath, "imagePath must not be null");
        if (!Files.isRegularFile(imagePath)) {
            throw new IOException("Disk image not found or is not a regular file: " + imagePath);
        }
        RandomAccessFile raf = new RandomAccessFile(imagePath.toFile(), "r");
        this.channel = raf.getChannel();
        this.imageSizeBytes = channel.size();
        log.info("DiskImageSectorReader opened: {} ({} bytes / {} MB)",
                imagePath.getFileName(), imageSizeBytes, imageSizeBytes / (1024 * 1024));
    }

    @Override
    public SectorReadResult read(SectorReadRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        int sectorSize = request.bytesPerSector() > 0 ? request.bytesPerSector() : 512;
        long startByte = request.startSector() * sectorSize;
        int bytesToRead = request.sectorCount() * sectorSize;

        // Clamp to image bounds
        if (startByte >= imageSizeBytes) {
            log.debug("Read beyond image end at byte {}, returning empty sector", startByte);
            return new SectorReadResult(request, new byte[bytesToRead], Instant.now());
        }

        long available = imageSizeBytes - startByte;
        int actualRead = (int) Math.min(bytesToRead, available);

        ByteBuffer buffer = ByteBuffer.allocate(bytesToRead);
        try {
            int totalRead = 0;
            while (totalRead < actualRead) {
                int n = channel.read(buffer, startByte + totalRead);
                if (n == -1) break;
                totalRead += n;
            }
        } catch (IOException e) {
            log.warn("Read error at byte offset {} in image {}: {}", startByte, imagePath.getFileName(), e.getMessage());
        }

        return new SectorReadResult(request, buffer.array(), Instant.now());
    }

    /**
     * Total size of the disk image in bytes.
     */
    public long imageSizeBytes() {
        return imageSizeBytes;
    }

    /**
     * The path of the image file being read.
     */
    public Path imagePath() {
        return imagePath;
    }

    /**
     * Closes the underlying file channel. Must be called when scanning is finished.
     */
    public void close() {
        try {
            if (channel.isOpen()) {
                channel.close();
                log.info("DiskImageSectorReader closed: {}", imagePath.getFileName());
            }
        } catch (IOException e) {
            log.warn("Failed to close image file channel: {}", e.getMessage());
        }
    }
}
