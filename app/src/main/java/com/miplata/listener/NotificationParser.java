package com.miplata.listener;

import java.util.regex.*;
import com.miplata.data.Transaction;

public class NotificationParser {

    // Ej: $120.500 | $1,200.50 | $950000
    // Nota: en una clase de caracteres, el '.' no requiere escape.
    private static final Pattern MONEY = Pattern.compile(
            "\\$\\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+)"
    );

    private static final int MAX_DESC = 90;

    public static Transaction parse(String text) {
        if (text == null || text.isBlank()) return null;

        Matcher m = MONEY.matcher(text);
        if (!m.find()) return null;

        String g = m.group(1);
        if (g == null) return null; // (defensa extra, silencia el warning)

        // Normaliza 1.200,50 -> 1200.50
        String raw = g.replace(".", "").replace(",", ".");
        double amount;
        try { amount = Double.parseDouble(raw); }
        catch (NumberFormatException e) { return null; }

        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setDateMillis(System.currentTimeMillis());
        t.setDescription(shorten(text, MAX_DESC));
        t.setType(detectType(text));
        return t;
    }

    private static String detectType(String text) {
        String s = text.toLowerCase();
        if (s.contains("compra") || s.contains("pago") || s.contains("retiro") || s.contains("débito")) return "DEBIT";
        if (s.contains("abono")  || s.contains("recibo")|| s.contains("depósito")|| s.contains("crédito")) return "CREDIT";
        return "UNKNOWN";
    }

    private static String shorten(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max - 1) + "…";
    }
}
