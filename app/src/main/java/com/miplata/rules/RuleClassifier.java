package com.miplata.rules;

import com.miplata.listener.NotificationParser;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Clasificador 100% por reglas (sin IA).
 * - Exige monto válido.
 * - Clasifica con verbos/expresiones de débito o crédito (ampliado).
 * - Descarta consultas de saldo/marketing/seguridad y mensajes ambiguos.
 */
public class RuleClassifier {

    public static final class Result {
        public boolean accept;
        public String type;    // "DEBIT" | "CREDIT"
        public Double amount;
        public String reason;
    }

    // Normaliza tildes para comparar de forma robusta: "pasó" == "paso"
    private static String deaccent(String s) {
        if (s == null) return null;
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "");
    }

    // === Palabras clave MEJORADAS ===

    /** Débito (resta). Incluye "paso/pasó", formas comunes y POS. */
    private static final String[] DEBIT_KW = deaccentAll(new String[]{
        "compra","compraste","pago","pagaste","pagado","consumo","cobro","cargo",
        "suscripcion","suscripcion","renovacion","domicilio","pos","datafono","qr","link de pago","pse",
        "retiro","retiraste","cajero","atm","efectivo",
        "comision","tarifa","cuota","intereses","impuesto","iva","gravamen","4x1000","gmf","seguro","mora",
        "enviaste","envio","enviado","transferiste","transferencia enviada","saliente","traspaso",
        "paso plata","paso","pasaste","trasladar","trasladado"
    });

    /** Crédito (suma). Se añade "recibio" para que coincida con "Recibió". */
    private static final String[] CREDIT_KW = deaccentAll(new String[]{
        "recibiste","recibido","recibio","te llego","llego","ingreso","ingreso a tu cuenta","ingreso realizado",
        "abono","abonado","acreditado","acreditacion","deposito","consignacion",
        "transferencia recibida","traspaso recibido","entrante",
        "reembolso","devolucion","cashback",
        "te enviaron","te transfirieron","te pasaron","te consignaron"
    });

    /** Frases que indican NO movimiento (informativas/seguridad/marketing). */
    private static final String[] IGNORE_KW = deaccentAll(new String[]{
        // saldo / consulta
        "saldo disponible","saldo a la fecha","saldo actual","saldo total","consulta de saldo","extracto","estado de cuenta",
        // marketing
        "promocion","oferta","beneficio","campana","publicidad",
        // seguridad / acceso
        "token","otp","codigo","codigo de verificacion","verificacion","cambio de clave","bloqueo","activacion","seguridad"
    });

    /** Palabras que anulan el movimiento (reversos/declinados). */
    private static final String[] CANCEL_KW = deaccentAll(new String[]{
        "reversado","reverso","revertido","anulado","cancelado","declinado","fallido","rechazado"
    });

    private static final Pattern CURRENCY_MARKER = Pattern.compile("(?i)(\\$|\\bCOP\\b)");

    private static String[] deaccentAll(String[] arr) {
        String[] out = new String[arr.length];
        for (int i = 0; i < arr.length; i++) out[i] = deaccent(arr[i]);
        return out;
    }

    public Result classify(String rawText) {
        final Result r = new Result();
        r.accept = false;
        r.type = "UNKNOWN";
        r.amount = null;
        r.reason = "init";

        if (rawText == null || rawText.trim().isEmpty()) {
            r.reason = "empty";
            return r;
        }

        final String textLow = rawText.toLowerCase(Locale.ROOT);
        final String text = deaccent(textLow);

        // 0) Ignorar info sin movimiento
        if (containsAny(text, IGNORE_KW)) {
            r.reason = "ignore_info";
            return r;
        }

        // 0.5) Cancelaciones / declinados anulan
        if (containsAny(text, CANCEL_KW)) {
            r.reason = "canceled_or_declined";
            return r;
        }

        // 1) Monto requerido
        Double amount = NotificationParser.parseAmount(rawText);
        if (amount == null || amount <= 0) {
            r.reason = "no_amount";
            return r;
        }

        // 2) Señales
        boolean debit = containsAny(text, DEBIT_KW);
        boolean credit = containsAny(text, CREDIT_KW);

        // 3) Si NO hay $/COP, exige verbo claro (evita "Nequi: 15000" sin contexto)
        boolean hasCurrencyMarker = CURRENCY_MARKER.matcher(rawText).find();
        if (!hasCurrencyMarker && !debit && !credit) {
            r.reason = "plain_amount_without_verb";
            return r;
        }

        // 4) Resolver tipo
        if (credit && !debit) {
            r.accept = true; r.type = "CREDIT"; r.amount = amount; r.reason = "credit_kw";
            return r;
        }
        if (debit && !credit) {
            r.accept = true; r.type = "DEBIT"; r.amount = amount; r.reason = "debit_kw";
            return r;
        }

        // 5) Empate: heurística (prioriza verbo más “fuerte”: pago/enviaste > te enviaron)
        if (debit && credit) {
            if (strongDebit(text) && !strongCredit(text)) {
                r.accept = true; r.type = "DEBIT"; r.amount = amount; r.reason = "tie_strong_debit";
                return r;
            }
            if (strongCredit(text) && !strongDebit(text)) {
                r.accept = true; r.type = "CREDIT"; r.amount = amount; r.reason = "tie_strong_credit";
                return r;
            }
            // Si sigue ambiguo, descartar
            r.reason = "ambiguous";
            return r;
        }

        r.reason = "no_signal";
        return r;
    }

    private boolean strongDebit(String t) {
        return t.contains("pago") || t.contains("pagaste") || t.contains("enviaste") || t.contains("transferiste") || t.contains("paso");
    }
    private boolean strongCredit(String t) {
        return t.contains("recibiste") || t.contains("te enviaron") || t.contains("te pasaron") || t.contains("abono");
    }

    private static boolean containsAny(String s, String[] kws) {
        if (s == null) return false;
        for (String k : kws) if (s.contains(k)) return true;
        return false;
    }
}
