package com.miplata.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NotificationParser {

    private NotificationParser() {}

    // Acepta: "$1.500,00", "COP 150.000", "150000.00", "$ 2,345.67", "COP1500", "$100", "15000"
    private static final Pattern MONEY_PATTERN = Pattern.compile(
        "(?i)(?:\\$|cop\\s*)?\\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})|[0-9]+(?:[.,][0-9]{2})?|[0-9]+)"
    );

    public static Double parseAmount(String text) {
        if (text == null) return null;
        final Matcher m = MONEY_PATTERN.matcher(text);
        if (!m.find()) return null;

        String raw = m.group(1);
        if (raw == null || raw.isEmpty()) return null;

        String normalized = raw
            .replaceAll("\\.(?=\\d{3}(\\D|$))", "") // "1.234" -> "1234"
            .replace(',', '.');                      // "1,23" -> "1.23"

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
