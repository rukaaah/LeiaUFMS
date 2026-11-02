package br.ufms.leiaufms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.core.util.Validators.Result;

public class ValidatorsTest {

    @Test
    public void email() {
        assertEquals(Result.OK, Validators.email("aluno@ufms.br"));
        assertEquals(Result.OK, Validators.email("  Aluno.Nome@UFMS.BR "));
        assertEquals(Result.EMPTY, Validators.email("   "));
        assertEquals(Result.WRONG_DOMAIN, Validators.email("aluno@gmail.com"));
        assertEquals(Result.INVALID, Validators.email("aluno@ufms"));
        assertEquals(Result.INVALID, Validators.email("@ufms.br"));
        assertEquals(Result.INVALID, Validators.email("a@b@ufms.br"));
        assertEquals(Result.WRONG_DOMAIN, Validators.email("aluno@ufms.br.com"));
    }

    @Test
    public void password() {
        assertEquals(Result.OK, Validators.password("senha123".toCharArray()));
        assertEquals(Result.EMPTY, Validators.password(new char[0]));
        assertEquals(Result.WEAK, Validators.password("abc".toCharArray()));
        assertEquals(Result.WEAK, Validators.password("somenteletras".toCharArray()));
        assertEquals(Result.WEAK, Validators.password("12345678".toCharArray()));
        assertEquals(Result.TOO_LONG, Validators.password(new String(new char[65]).replace('\0', 'a').toCharArray()));
    }

    @Test
    public void confirmation() {
        assertEquals(Result.OK, Validators.confirmation("abc12345".toCharArray(), "abc12345".toCharArray()));
        assertEquals(Result.MISMATCH, Validators.confirmation("abc12345".toCharArray(), "abc12346".toCharArray()));
        assertEquals(Result.EMPTY, Validators.confirmation("abc12345".toCharArray(), new char[0]));
    }

    @Test
    public void nameAndSemester() {
        assertEquals(Result.EMPTY, Validators.name(" "));
        assertEquals(Result.TOO_SHORT, Validators.name("A"));
        assertEquals(Result.OK, Validators.name("Ana"));
        assertEquals(Result.TOO_LONG, Validators.name(new String(new char[81]).replace('\0', 'a')));
        assertEquals(Result.OUT_OF_RANGE, Validators.semester("0"));
        assertEquals(Result.OUT_OF_RANGE, Validators.semester("99"));
        assertEquals(Result.OUT_OF_RANGE, Validators.semester("dois"));
        assertEquals(Result.OK, Validators.semester("12"));
        assertEquals(Result.EMPTY, Validators.semester(""));
    }

    @Test
    public void isbn() {
        assertEquals(Result.OK, Validators.isbn(""));
        assertEquals(Result.OK, Validators.isbn("978-85-359-0277-5"));
        assertEquals(Result.OK, Validators.isbn("85-359-0277-X"));
        assertEquals(Result.INVALID, Validators.isbn("123"));
    }
}
