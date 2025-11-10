package com.miplata.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationParser {

    // Regex para encontrar un monto numérico en el texto.
    private static final Pattern MONEY = Pattern.compile("(?:\\$\\s*|\\bvalor\\s+|\\bmonto\\s+)?([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+)");

    /**
     * Extrae únicamente el valor numérico de un texto.
     * @param text El texto de la notificación.
     * @return Un Double con el monto, o null si no se encuentra.
     */
    public static Double parseAmount(String text) {
        if (text == null || text.isBlank()) return null;

        Matcher m = MONEY.matcher(text.toLowerCase());
        if (!m.find()) return null;

        String g = m.group(1);
        if (g == null) return null;

        // Normaliza "1.200,50" -> "1200.50"
        String raw = g.replace(".", "").replace(",", ".");
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
