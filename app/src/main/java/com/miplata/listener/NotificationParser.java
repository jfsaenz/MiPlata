package com.miplata.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationParser {

    // Regex mejorada para capturar montos con y sin puntos/comas, y opcionalmente precedidos por '$'
    private static final Pattern MONEY_PATTERN = Pattern.compile("(?:\\$|\\bCOP\\s|\\bvalor\\s+)?([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+(?:[.,][0-9]{2})?)");

    /**
     * Analiza una cadena de texto y extrae el primer valor numérico que parece ser un monto de dinero.
     * Maneja formatos como "$1.500,00", "1500.00", "$ 1,500" etc.
     *
     * @param text El texto completo de la notificación.
     * @return Un objeto Double con el monto encontrado, o null si no se encuentra ningún monto válido.
     */
    public static Double parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = MONEY_PATTERN.matcher(text);

        if (matcher.find()) {
            String amountString = matcher.group(1);
            if (amountString != null) {
                try {
                    // Normaliza el string para que Java pueda entenderlo como un número.
                    // 1. Quita los puntos que se usan como separadores de miles.
                    // 2. Reemplaza la coma decimal por un punto decimal.
                    String normalizedAmount = amountString.replaceAll("[.,](?=\\d{3})", "").replace(',', '.');
                    return Double.parseDouble(normalizedAmount);
                } catch (NumberFormatException e) {
                    // El texto capturado parecía un número pero no lo era.
                    return null;
                }
            }
        }

        return null; // No se encontró ningún patrón de dinero.
    }
}
