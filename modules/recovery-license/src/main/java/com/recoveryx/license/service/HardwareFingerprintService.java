package com.recoveryx.license.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HexFormat;

/**
 * Computes a unique hardware fingerprint (HWID) based on machine architecture, MAC address, and OS details.
 */
public final class HardwareFingerprintService {

    private static final Logger log = LoggerFactory.getLogger(HardwareFingerprintService.class);

    public HardwareFingerprintService() {
    }

    /**
     * Calculates the machine Hardware ID (HWID).
     */
    public String getHardwareFingerprint() {
        try {
            StringBuilder sb = new StringBuilder();

            // OS Name & Architecture
            sb.append(System.getProperty("os.name", "Windows"));
            sb.append(System.getProperty("os.arch", "amd64"));
            sb.append(System.getProperty("user.name", "user"));

            // MAC Address of primary network interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null && mac.length > 0 && !ni.isLoopback()) {
                        sb.append(HexFormat.of().formatHex(mac));
                        break;
                    }
                }
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes());
            String fullHash = HexFormat.of().formatHex(hash).toUpperCase();

            // Format as standard 16-char HWID: XXXX-XXXX-XXXX-XXXX
            return String.format("%s-%s-%s-%s",
                    fullHash.substring(0, 4),
                    fullHash.substring(4, 8),
                    fullHash.substring(8, 12),
                    fullHash.substring(12, 16));

        } catch (Exception e) {
            log.warn("Error computing HWID fingerprint, using fallback: {}", e.getMessage());
            return "RCX1-PRO0-HWID-0000";
        }
    }
}
