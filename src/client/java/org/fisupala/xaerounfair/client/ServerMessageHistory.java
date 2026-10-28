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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class ServerMessageHistory {
    public static final int CAPACITY = 5;

    public record Message(String raw, String plainText) {}

    private final ArrayDeque<Message> messages = new ArrayDeque<>(CAPACITY);

    public synchronized void remember(String raw) {
        if (!ChatFilter.matches(raw)) return;
        messages.addLast(new Message(raw, decodeFormattedText(raw)));
        while (messages.size() > CAPACITY) {
            messages.removeFirst();
        }
    }

    private static String decodeFormattedText(String raw) {
        StringBuilder plainText = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char current = raw.charAt(i);
            if (current == '§' && i + 1 < raw.length()) {
                plainText.append(raw.charAt(++i));
            } else {
                plainText.append(current);
            }
        }
        return plainText.toString();
    }

    public synchronized List<Message> messagesOldestFirst() {
        return List.copyOf(new ArrayList<>(messages));
    }
}
