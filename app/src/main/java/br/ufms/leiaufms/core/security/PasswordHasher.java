package br.ufms.leiaufms.core.security;

import java.util.Arrays;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import br.ufms.leiaufms.AppConfig;

/**
 * Hash de senha com BCrypt (custo 12, salt aleatório embutido no hash). ADR-04, RN-AUTH-05.
 * A senha chega como char[] e é zerada aqui depois do uso; nunca vira String.
 */
public final class PasswordHasher {

    private static final BCrypt.Version VERSION = BCrypt.Version.VERSION_2A;

    private PasswordHasher() {
    }

    /** Gera um hash novo (salt novo a cada chamada) e zera o array da senha. */
    public static String hash(char[] password) {
        try {
            return BCrypt.with(VERSION, LongPasswordStrategies.truncate(VERSION))
                    .hashToString(AppConfig.BCRYPT_COST, password);
        } finally {
            wipe(password);
        }
    }

    /** Confere a senha contra o hash guardado e zera o array da senha. */
    public static boolean verify(char[] password, String storedHash) {
        try {
            if (storedHash == null || password == null) return false;
            return BCrypt.verifyer(VERSION, LongPasswordStrategies.truncate(VERSION))
                    .verify(password, storedHash.toCharArray()).verified;
        } finally {
            wipe(password);
        }
    }

    public static void wipe(char[] value) {
        if (value != null) Arrays.fill(value, '\0');
    }
}
