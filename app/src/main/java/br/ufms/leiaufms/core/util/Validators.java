package br.ufms.leiaufms.core.util;

import java.util.Locale;
import java.util.regex.Pattern;

import br.ufms.leiaufms.AppConfig;

/**
 * Validações de entrada. RN-AUTH-01 a RN-AUTH-04, RF-12 (ISBN).
 * Cada método devolve um {@link Result} que a tela traduz para a mensagem de strings.xml.
 */
public final class Validators {

    public enum Result {
        OK, EMPTY, INVALID, WRONG_DOMAIN, TOO_SHORT, TOO_LONG, WEAK, MISMATCH, OUT_OF_RANGE
    }

    private static final Pattern EMAIL_LOCAL = Pattern.compile("^[a-z0-9._%+-]+$");

    private Validators() {
    }

    /** E-mail normalizado: sem espaços nas pontas e em minúsculas. */
    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static Result email(String raw) {
        String email = normalizeEmail(raw);
        if (email.isEmpty()) return Result.EMPTY;
        if (email.length() > AppConfig.EMAIL_MAX) return Result.TOO_LONG;
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) return Result.INVALID;
        if (!EMAIL_LOCAL.matcher(email.substring(0, at)).matches()) return Result.INVALID;
        if (!email.endsWith(AppConfig.ALLOWED_DOMAIN)) {
            String domain = email.substring(at);
            return domain.contains(".") ? Result.WRONG_DOMAIN : Result.INVALID;
        }
        return Result.OK;
    }

    public static Result name(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) return Result.EMPTY;
        if (name.length() < AppConfig.NAME_MIN) return Result.TOO_SHORT;
        if (name.length() > AppConfig.NAME_MAX) return Result.TOO_LONG;
        return Result.OK;
    }

    public static Result required(String raw, int max) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return Result.EMPTY;
        if (value.length() > max) return Result.TOO_LONG;
        return Result.OK;
    }

    public static Result optional(String raw, int max) {
        String value = raw == null ? "" : raw.trim();
        return value.length() > max ? Result.TOO_LONG : Result.OK;
    }

    public static Result semester(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return Result.EMPTY;
        try {
            int s = Integer.parseInt(value);
            if (s < AppConfig.SEMESTER_MIN || s > AppConfig.SEMESTER_MAX) return Result.OUT_OF_RANGE;
            return Result.OK;
        } catch (NumberFormatException e) {
            return Result.OUT_OF_RANGE;
        }
    }

    /** RN-AUTH-03: 8 a 64 caracteres, pelo menos 1 letra e 1 número. */
    public static Result password(char[] password) {
        if (password == null || password.length == 0) return Result.EMPTY;
        if (password.length > AppConfig.PASSWORD_MAX) return Result.TOO_LONG;
        boolean letter = false;
        boolean digit = false;
        for (char c : password) {
            if (Character.isLetter(c)) letter = true;
            else if (Character.isDigit(c)) digit = true;
        }
        if (password.length < AppConfig.PASSWORD_MIN || !letter || !digit) return Result.WEAK;
        return Result.OK;
    }

    public static Result confirmation(char[] password, char[] confirmation) {
        if (confirmation == null || confirmation.length == 0) return Result.EMPTY;
        return java.util.Arrays.equals(password, confirmation) ? Result.OK : Result.MISMATCH;
    }

    /** RF-12: ISBN opcional com 10 ou 13 dígitos (hífens e espaços ignorados; X final no ISBN-10). */
    public static Result isbn(String raw) {
        String isbn = normalizeIsbn(raw);
        if (isbn.isEmpty()) return Result.OK;
        if (isbn.length() == 13 && isbn.matches("\\d{13}")) return Result.OK;
        if (isbn.length() == 10 && isbn.matches("\\d{9}[\\dX]")) return Result.OK;
        return Result.INVALID;
    }

    public static String normalizeIsbn(String raw) {
        return raw == null ? "" : raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }
}
