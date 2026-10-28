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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class XaerounfairConfig {
    public enum Mode {
        SERVER_ENFORCED,
        MANUAL
    }

    public enum ManualProfile {
        DEFAULT,
        MINIMAP_OFF,
        NETHER_CAVE,
        BASIC_FAIRPLAY;

        public ManualProfile next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public List<String> simulatedMessageKeys() {
            return switch (this) {
                case DEFAULT -> List.of();
                case MINIMAP_OFF -> List.of("nominimap");
                case NETHER_CAVE -> List.of("fairxaero", "xaerommnetherisfair");
                case BASIC_FAIRPLAY -> List.of("fairxaero");
            };
        }
    }

    public record Profile(Mode mode, ManualProfile manualProfile) {
        public Profile {
            if (mode == null) throw new IllegalArgumentException("mode cannot be null");
            if (manualProfile == null) throw new IllegalArgumentException("manualProfile cannot be null");
        }

        public static Profile defaults() {
            return new Profile(Mode.MANUAL, ManualProfile.DEFAULT);
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

        String manualProfileValue = properties.getProperty(prefix + "manualProfile");
        ManualProfile manualProfile = fallback.manualProfile();
        if (manualProfileValue != null) {
            try {
                manualProfile = ManualProfile.valueOf(manualProfileValue);
            } catch (IllegalArgumentException exception) {
                XaerounfairClient.LOG.warn("Invalid setting {}manualProfile: {}", prefix, manualProfileValue);
            }
        } else {
            String legacyTarget = properties.getProperty(prefix + "filterTarget");
            if (legacyTarget != null) {
                XaerounfairClient.LOG.warn(
                        "Migrating obsolete single-filter setting {}filterTarget to the Default profile", prefix);
                manualProfile = fallback.manualProfile();
            } else {
                manualProfile = migrateLegacyProfile(properties, prefix, fallback.manualProfile());
            }
        }
        return new Profile(mode, manualProfile);
    }

    private static ManualProfile migrateLegacyProfile(Properties properties, String prefix, ManualProfile fallback) {
        String fairxaeroSetting = properties.getProperty(prefix + "honorFairxaero");
        String netherSetting = properties.getProperty(prefix + "honorNether");
        String noMinimapSetting = properties.getProperty(prefix + "honorNoMinimap");
        if (fairxaeroSetting == null && netherSetting == null && noMinimapSetting == null) return fallback;

        boolean fairxaero = Boolean.parseBoolean(fairxaeroSetting);
        boolean nether = Boolean.parseBoolean(netherSetting);
        boolean noMinimap = Boolean.parseBoolean(noMinimapSetting);
        if (!fairxaero && !nether && !noMinimap) return ManualProfile.DEFAULT;
        if (fairxaero && nether && !noMinimap) return ManualProfile.NETHER_CAVE;
        if (fairxaero && !nether && !noMinimap) return ManualProfile.BASIC_FAIRPLAY;
        if (!fairxaero && !nether && noMinimap) return ManualProfile.MINIMAP_OFF;
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
        properties.setProperty(prefix + "manualProfile", profile.manualProfile().name());
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
