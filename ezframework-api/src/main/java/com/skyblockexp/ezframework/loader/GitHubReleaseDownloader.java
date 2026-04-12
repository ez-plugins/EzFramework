package com.skyblockexp.ezframework.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Downloads a single file from an HTTPS URL and verifies its SHA-256 checksum
 * against a companion {@code .sha256} file hosted at {@code url + ".sha256"}.
 *
 * <p>The download is staged to a temp file. The target path is only replaced
 * when the checksum matches. If the checksum fails the temp file is deleted
 * and an {@link IOException} is thrown — the target is never modified.
 *
 * <p>This class has no dependencies outside the Java standard library (Java 17+).
 */
public final class GitHubReleaseDownloader {

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 60_000;
    private static final int BUFFER_SIZE = 8 * 1024;

    private final Logger logger;

    /**
     * Create a downloader that writes messages to the given logger.
     *
     * @param logger logger for progress / error messages (must not be null)
     */
    public GitHubReleaseDownloader(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    /**
     * Download a file from {@code url} to {@code target}, verifying the
     * SHA-256 checksum fetched from {@code url + ".sha256"}.
     *
     * @param url    HTTPS URL of the file to download
     * @param target destination path (parent directory must exist)
     * @throws IOException if the download, checksum fetch, or verification fails
     */
    public void download(String url, Path target) throws IOException {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(target, "target");
        if (!url.startsWith("https://")) {
            throw new IOException("Only HTTPS URLs are permitted. Got: " + url);
        }

        logger.info("Downloading " + url);
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");

        try {
            // Download the main artifact to a temp file
            fetchToFile(url, tmp);

            // Fetch and parse the expected checksum
            String expectedHex = fetchText(url + ".sha256").trim();

            // Compute actual checksum
            String actualHex = sha256Hex(tmp);

            if (!expectedHex.equalsIgnoreCase(actualHex)) {
                throw new IOException(
                        "SHA-256 mismatch for " + url
                                + "\n  expected: " + expectedHex
                                + "\n  actual:   " + actualHex);
            }

            // Atomically replace target only after successful verification
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            logger.info("Downloaded and verified: " + target.getFileName());
        } catch (IOException e) {
            // Clean up temp file on any failure
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            throw e;
        }
    }

    /**
     * Fetch the text content at the given HTTPS URL (e.g. a {@code .sha256} file).
     *
     * @param url HTTPS URL
     * @return UTF-8 decoded response body
     * @throws IOException on network or HTTP errors
     */
    public String fetchText(String url) throws IOException {
        if (!url.startsWith("https://")) {
            throw new IOException("Only HTTPS URLs are permitted. Got: " + url);
        }
        HttpURLConnection conn = openConnection(url);
        try (InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            conn.disconnect();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void fetchToFile(String url, Path dest) throws IOException {
        HttpURLConnection conn = openConnection(url);
        try (InputStream is = conn.getInputStream();
             OutputStream os = Files.newOutputStream(dest)) {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = is.read(buf)) != -1) {
                os.write(buf, 0, n);
            }
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection openConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "EzFramework/Loader");
        conn.setRequestProperty("Accept", "application/octet-stream");
        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            conn.disconnect();
            throw new IOException("HTTP " + status + " fetching " + url);
        }
        return conn;
    }

    private static String sha256Hex(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file)) {
                byte[] buf = new byte[BUFFER_SIZE];
                int n;
                while ((n = is.read(buf)) != -1) {
                    md.update(buf, 0, n);
                }
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not available", e);
        }
    }
}
