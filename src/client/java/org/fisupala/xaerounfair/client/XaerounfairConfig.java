/*
 * XaeroUnfair
 * Copyright (C) 2026 fisupala
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 only,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.fisupala.xaerounfair.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class XaerounfairConfig {
    public enum Mode {
        SERVER_ENFORCED,
        MANUAL
    }

    public record Profile(Mode mode, boolean honorFairxaero, boolean honorNether, boolean honorNoMinimap) {
        public Profile {
            if (mode == null) throw new IllegalArgumentException("mode cannot be null");
        }

        public static Profile defaults() {
            return new Profile(Mode.MANUAL, false, false, false);
        }
    }

    private static final String PREFIX = "xaerounfair.";
    private static final String GLOBAL_PREFIX = PREFIX + "global.";
    private static final String SERVER_PREFIX = PREFIX + "server.";
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xaerounfair.properties");

    private static Profile defaults = Profile.defaults();
    private static final Map<String, Profile> servers = new HashMap<>();
    private static volatile String activeServerAddress;
    private static volatile Profile activeProfile = Profile.defaults();

    private XaerounfairConfig() {}

    public static synchronized void load() {
        defaults = Profile.defaults();
        servers.clear();

        if (Files.exists(FILE)) {
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(FILE)) {
                properties.load(input);
                defaults = readProfile(properties, GLOBAL_PREFIX, Profile.defaults());
                String serverKeyPrefix = SERVER_PREFIX;
                for (String key : properties.stringPropertyNames()) {
                    if (!key.startsWith(serverKeyPrefix) || !key.endsWith(".mode")) continue;
                    String encodedAddress = key.substring(serverKeyPrefix.length(), key.length() - ".mode".length());
                    try {
                        String address = normalizeAddress(new String(Base64.getUrlDecoder().decode(encodedAddress), StandardCharsets.UTF_8));
                        if (address == null) {
                            XaerounfairClient.LOG.warn("Ignoring empty XaeroUnfair server profile key: {}", key);
                            continue;
                        }
                        servers.put(address, readProfile(properties, serverPrefix(address), defaults));
                    } catch (IllegalArgumentException exception) {
                        XaerounfairClient.LOG.warn("Ignoring malformed XaeroUnfair server profile key: {}", key);
                    }
                }
            } catch (IOException | IllegalArgumentException exception) {
                XaerounfairClient.LOG.error("Could not load XaeroUnfair settings from {}", FILE, exception);
            }
        }

        activeServerAddress = null;
        activeProfile = defaults;
    }

    public static Profile activeProfile() {
        return activeProfile;
    }

    public static Profile defaultsProfile() {
        synchronized (XaerounfairConfig.class) {
            return defaults;
        }
    }

    public static synchronized Profile serverProfile(String address) {
        String normalized = normalizeAddress(address);
        return servers.getOrDefault(normalized, defaults);
    }

    public static String activeServerAddress() {
        return activeServerAddress;
    }

    public static void activateServer(String address) {
        String normalized = normalizeAddress(address);
        activeServerAddress = normalized;
        synchronized (XaerounfairConfig.class) {
            activeProfile = normalized == null ? defaults : servers.getOrDefault(normalized, defaults);
        }
    }

    public static synchronized void setDefaults(Profile profile) {
        defaults = profile;
        if (activeServerAddress == null || !servers.containsKey(activeServerAddress)) activeProfile = profile;
        save();
    }

    public static synchronized void setServerProfile(String address, Profile profile) {
        String normalized = normalizeAddress(address);
        if (normalized == null) throw new IllegalArgumentException("Server address cannot be empty");
        servers.put(normalized, profile);
        if (normalized.equals(activeServerAddress)) activeProfile = profile;
        save();
    }

    private static Profile readProfile(Properties properties, String prefix, Profile fallback) {
        Mode mode = fallback.mode();
        String modeValue = properties.getProperty(prefix + "mode");
        if (modeValue != null) {
            try {
                mode = Mode.valueOf(modeValue);
            } catch (IllegalArgumentException exception) {
                XaerounfairClient.LOG.warn("Invalid setting {}mode: {}", prefix, modeValue);
            }
        }

        return new Profile(
                mode,
                readBoolean(properties, prefix + "honorFairxaero", fallback.honorFairxaero()),
                readBoolean(properties, prefix + "honorNether", fallback.honorNether()),
                readBoolean(properties, prefix + "honorNoMinimap", fallback.honorNoMinimap())
        );
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        XaerounfairClient.LOG.warn("Invalid boolean setting {}: {}", key, value);
        return fallback;
    }

    private static void save() {
        Properties properties = new Properties();
        writeProfile(properties, GLOBAL_PREFIX, defaults);
        servers.forEach((address, profile) -> writeProfile(properties, serverPrefix(address), profile));

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream output = Files.newOutputStream(FILE)) {
                properties.store(output, "XaeroUnfair settings");
            }
        } catch (IOException exception) {
            XaerounfairClient.LOG.error("Could not save XaeroUnfair settings to {}", FILE, exception);
        }
    }

    private static void writeProfile(Properties properties, String prefix, Profile profile) {
        properties.setProperty(prefix + "mode", profile.mode().name());
        properties.setProperty(prefix + "honorFairxaero", Boolean.toString(profile.honorFairxaero()));
        properties.setProperty(prefix + "honorNether", Boolean.toString(profile.honorNether()));
        properties.setProperty(prefix + "honorNoMinimap", Boolean.toString(profile.honorNoMinimap()));
    }

    private static String serverPrefix(String address) {
        String encodedAddress = Base64.getUrlEncoder().withoutPadding().encodeToString(address.getBytes(StandardCharsets.UTF_8));
        return SERVER_PREFIX + encodedAddress + ".";
    }

    private static String normalizeAddress(String address) {
        if (address == null || address.isBlank()) return null;
        return address.trim().toLowerCase(Locale.ROOT);
    }
}
