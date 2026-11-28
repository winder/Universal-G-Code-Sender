/*
    Copyright 2024 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures SSL/TLS for the Jetty pendant server, automatically generating a
 * self-signed PKCS12 keystore when no custom keystore is provided.
 */
public final class PendantSslHelper {
    private static final Logger LOG = Logger.getLogger(PendantSslHelper.class.getSimpleName());
    private static final String DEFAULT_KEYSTORE_FILENAME = "pendant-keystore.p12";

    private PendantSslHelper() {
    }

    public static SslContextFactory.Server createSslContextFactory(Settings settings) {
        String keystorePath = settings.getPendantKeystorePath();
        String keystorePassword = settings.getPendantKeystorePassword();

        File keystoreFile;
        if (keystorePath != null && !keystorePath.trim().isEmpty()) {
            keystoreFile = new File(keystorePath.trim());
        } else {
            keystoreFile = new File(SettingsFactory.getSettingsDirectory(), DEFAULT_KEYSTORE_FILENAME);
            if (!keystoreFile.exists()) {
                generateSelfSignedKeystore(keystoreFile, keystorePassword);
            }
        }

        if (!keystoreFile.exists()) {
            throw new IllegalStateException("Keystore file not found: " + keystoreFile.getAbsolutePath());
        }

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyManagerPassword(keystorePassword);
        return sslContextFactory;
    }

    private static void generateSelfSignedKeystore(File keystoreFile, String password) {
        File parent = keystoreFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        String keytoolBin = findKeytoolBinary();
        String san = buildSubjectAlternativeNames();

        List<String> command = new ArrayList<>();
        command.add(keytoolBin);
        command.add("-genkeypair");
        command.add("-alias");
        command.add("ugs-pendant");
        command.add("-keyalg");
        command.add("RSA");
        command.add("-keysize");
        command.add("2048");
        command.add("-validity");
        command.add("3650");
        command.add("-storetype");
        command.add("PKCS12");
        command.add("-keystore");
        command.add(keystoreFile.getAbsolutePath());
        command.add("-storepass");
        command.add(password);
        command.add("-keypass");
        command.add(password);
        command.add("-dname");
        command.add("CN=UGS Pendant, OU=UGS, O=Universal Gcode Sender, C=US");
        command.add("-ext");
        command.add("SAN=" + san);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("keytool exited with code " + exitCode);
            }
            LOG.info(() -> "Generated self-signed pendant keystore at: " + keystoreFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to generate self-signed keystore", e);
            throw new RuntimeException("Failed to generate self-signed SSL keystore", e);
        }
    }

    private static String findKeytoolBinary() {
        String javaHome = System.getProperty("java.home");
        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String exeName = isWindows ? "keytool.exe" : "keytool";
        if (javaHome != null) {
            File binFile = new File(new File(javaHome, "bin"), exeName);
            if (binFile.exists()) {
                return binFile.getAbsolutePath();
            }
        }
        return exeName;
    }

    private static String buildSubjectAlternativeNames() {
        List<String> sans = new ArrayList<>();
        sans.add("DNS:localhost");
        sans.add("IP:127.0.0.1");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String host = addr.getHostAddress();
                    if (!host.contains(":") && !"127.0.0.1".equals(host)) {
                        sans.add("IP:" + host);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Could not enumerate local IP addresses for SAN", e);
        }
        return String.join(",", sans);
    }
}
