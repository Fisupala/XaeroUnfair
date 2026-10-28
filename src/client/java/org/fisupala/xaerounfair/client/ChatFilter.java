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

import net.minecraft.network.chat.Component;

import java.util.List;

public final class ChatFilter {
    // Pätkät ilman §-merkkejä; koodi muodostaa niistä §-muodon automaattisesti
    private static final List<String> KEYS = List.of(
            "fairxaero",
            "xaerommnetherisfair",
            "nominimap",
            "xaerowmnetherisfair",
            "resetxaero"
    );

    private static final List<String> NEEDLES = KEYS.stream().map(ChatFilter::spaced).toList();

    /** Viesti, jonka simuloimme palvelimen lähettämänä. */
    public static final String RESET_MESSAGE = spaced("resetxaero");

    private ChatFilter() {}

    static String spaced(String key) {
        StringBuilder sb = new StringBuilder();
        for (char c : key.toCharArray()) sb.append('§').append(c);
        return sb.toString();
    }

    public static boolean matches(Component component) {
        return component != null && matches(component.getString());
    }

    public static boolean matches(String s) {
        return containsTargetedSequence(s);
    }

    public static boolean shouldFilter(Component component, XaerounfairConfig.Profile profile) {
        return component != null
                && !ResetSimulator.isSimulatedMessage(component)
                && shouldFilter(component.getString(), profile);
    }

    static boolean shouldFilter(String s, XaerounfairConfig.Profile profile) {
        if (profile.mode() == XaerounfairConfig.Mode.SERVER_ENFORCED || s == null || s.indexOf('§') < 0) return false;
        return containsTargetedSequence(s);
    }

    private static boolean containsTargetedSequence(String s) {
        if (s == null || s.indexOf('§') < 0) return false;
        for (String needle : NEEDLES) {
            if (s.contains(needle)) return true;
        }
        return false;
    }

}