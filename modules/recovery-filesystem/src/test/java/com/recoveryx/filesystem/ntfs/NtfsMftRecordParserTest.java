package com.recoveryx.filesystem.ntfs;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.core.domain.file.RecoverableFile;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class NtfsMftRecordParserTest {

    @Test
    void shouldParseMftRecordAndRecoverDeletedFile() {
        byte[] record = new byte[1024];
        ByteBuffer buffer = ByteBuffer.wrap(record).order(ByteOrder.LITTLE_ENDIAN);

        // Header: "FILE"
        System.arraycopy("FILE".getBytes(StandardCharsets.US_ASCII), 0, record, 0, 4);

        // Fixup array offset = 48 (0x30), count = 3 (1 seq + 2 sectors)
        buffer.putShort(0x04, (short) 48);
        buffer.putShort(0x06, (short) 3);
        buffer.putShort(48, (short) 0x1234); // Sequence number
        buffer.putShort(50, (short) 0xAAAA); // Sector 0 original last 2 bytes
        buffer.putShort(52, (short) 0xBBBB); // Sector 1 original last 2 bytes

        // Sector end markers matching fixup seq num
        buffer.putShort(510, (short) 0x1234);
        buffer.putShort(1022, (short) 0x1234);

        // First attribute offset = 56 (0x38), Record flags = 0x0000 (DELETED)
        buffer.putShort(0x14, (short) 56);
        buffer.putShort(0x16, (short) 0x0000);
        buffer.putInt(0x18, 400); // Bytes in use

        // Attribute 1: $FILE_NAME (0x30) at offset 56
        int fnAttrOffset = 56;
        int fnAttrLength = 128;
        buffer.putInt(fnAttrOffset, 0x30); // Attr Type = 0x30
        buffer.putInt(fnAttrOffset + 4, fnAttrLength); // Attr Length = 128
        buffer.put(fnAttrOffset + 8, (byte) 0); // Resident
        buffer.putInt(fnAttrOffset + 16, 104); // Value Length = 104 (66 header + 32 name bytes + 6 pad)
        buffer.putShort(fnAttrOffset + 20, (short) 24); // Value Offset = 24

        int fnValueOffset = fnAttrOffset + 24;
        buffer.putLong(fnValueOffset, 5L); // Parent dir = 5 (Root)
        buffer.putLong(fnValueOffset + 8, 133000000000000000L); // Created
        buffer.putLong(fnValueOffset + 16, 133000000000000000L); // Modified
        buffer.putLong(fnValueOffset + 40, 65536L); // Allocated size
        buffer.putLong(fnValueOffset + 48, 50000L); // Real size
        buffer.put(fnValueOffset + 64, (byte) 16); // Name length in chars: 16
        buffer.put(fnValueOffset + 65, (byte) 1); // Namespace: Win32

        byte[] fnBytes = "FinancialDoc.pdf".getBytes(StandardCharsets.UTF_16LE);
        System.arraycopy(fnBytes, 0, record, fnValueOffset + 66, fnBytes.length);

        // Attribute 2: $DATA (0x80) Non-Resident at offset 56 + 128 = 184
        int dataAttrOffset = fnAttrOffset + fnAttrLength;
        int dataAttrLength = 80;
        buffer.putInt(dataAttrOffset, 0x80); // Attr Type = 0x80
        buffer.putInt(dataAttrOffset + 4, dataAttrLength); // Attr Length = 80
        buffer.put(dataAttrOffset + 8, (byte) 1); // Non-Resident = 1
        buffer.putShort(dataAttrOffset + 32, (short) 64); // Data runs offset = 64
        buffer.putLong(dataAttrOffset + 48, 50000L); // Data size = 50,000 bytes

        // Data Run at 184 + 64 = 248: Length 13 clusters, Offset +5000 clusters
        int runOffset = dataAttrOffset + 64;
        record[runOffset] = (byte) 0x21; // 1 byte length, 2 bytes offset
        record[runOffset + 1] = (byte) 13; // 13 clusters
        buffer.putShort(runOffset + 2, (short) 5000); // cluster 5000
        record[runOffset + 4] = (byte) 0x00; // End of runs

        // End Attribute Marker at offset 184 + 80 = 264
        buffer.putInt(dataAttrOffset + dataAttrLength, 0xFFFFFFFF);

        NtfsMftRecordParser parser = new NtfsMftRecordParser();
        RecoverableFile file = parser.parseRecord(record, 42, 4096, 0);

        assertNotNull(file);
        assertEquals("FinancialDoc.pdf", file.name());
        assertEquals("pdf", file.extension());
        assertEquals(FileCategory.PDF, file.category());
        assertEquals(50000L, file.fileSize());
        assertEquals(RecoveryChance.HIGH, file.recoveryChance());
        assertEquals(HealthStatus.GOOD, file.healthStatus()); // Deleted file with fragments
        assertNotNull(file.deletedDate());
        assertEquals(1, file.fragments().size());
        assertEquals(5000L * 4096, file.fragments().get(0).startOffsetBytes());
        assertEquals(13L * 4096, file.fragments().get(0).lengthBytes());
    }
}
