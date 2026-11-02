package br.ufms.leiaufms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.ufms.leiaufms.core.security.PasswordHasher;

public class PasswordHasherTest {

    @Test
    public void hashIsNotThePassword() {
        String hash = PasswordHasher.hash("senha123".toCharArray());
        assertFalse(hash.contains("senha123"));
        assertTrue(hash.startsWith("$2a$12$"));
    }

    @Test
    public void verifyCorrectAndIncorrect() {
        String hash = PasswordHasher.hash("senha123".toCharArray());
        assertTrue(PasswordHasher.verify("senha123".toCharArray(), hash));
        assertFalse(PasswordHasher.verify("senha124".toCharArray(), hash));
    }

    @Test
    public void samePasswordTwoDifferentHashes() {
        String a = PasswordHasher.hash("senha123".toCharArray());
        String b = PasswordHasher.hash("senha123".toCharArray());
        assertNotEquals(a, b);
        assertTrue(PasswordHasher.verify("senha123".toCharArray(), a));
        assertTrue(PasswordHasher.verify("senha123".toCharArray(), b));
    }

    @Test
    public void passwordArrayIsWiped() {
        char[] senha = "senha123".toCharArray();
        PasswordHasher.hash(senha);
        for (char c : senha) assertTrue(c == '\0');
    }
}
