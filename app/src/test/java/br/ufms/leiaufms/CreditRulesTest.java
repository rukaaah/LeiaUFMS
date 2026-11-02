package br.ufms.leiaufms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.pedido.CreditRules;

/** RN-CRED-01 a 05: reserva, liberação, crédito do doador e saldo nunca negativo. */
public class CreditRulesTest {

    @Test
    public void rescueWithCreditReservesOne() {
        assertEquals(-1, CreditRules.reserveOnRequest(Pedido.TIPO_RESGATE, 1));
        assertEquals(-1, CreditRules.reserveOnRequest(Pedido.TIPO_RESGATE, 5));
        assertFalse(CreditRules.isWithoutCredit(Pedido.TIPO_RESGATE, 1));
    }

    @Test
    public void rescueWithoutCreditIsAllowedAndReservesNothing() {
        assertEquals(0, CreditRules.reserveOnRequest(Pedido.TIPO_RESGATE, 0));
        assertTrue(CreditRules.isWithoutCredit(Pedido.TIPO_RESGATE, 0));
    }

    @Test
    public void directTradeNeverTouchesCredits() {
        assertEquals(0, CreditRules.reserveOnRequest(Pedido.TIPO_TROCA_DIRETA, 3));
        assertFalse(CreditRules.isWithoutCredit(Pedido.TIPO_TROCA_DIRETA, 0));
        assertEquals(0, CreditRules.donorCreditOnConclusion(Pedido.TIPO_TROCA_DIRETA));
    }

    @Test
    public void releaseReturnsOnlyHeldReserve() {
        assertEquals(1, CreditRules.releaseAmount(-1));
        assertEquals(0, CreditRules.releaseAmount(0));
    }

    @Test
    public void donorAlwaysGetsOne() {
        assertEquals(1, CreditRules.donorCreditOnConclusion(Pedido.TIPO_RESGATE));
    }

    /** Simula o livro-razão: pedir sem crédito, concluir, pedir com crédito, cancelar. */
    @Test
    public void balanceNeverNegative() {
        List<Integer> requester = new ArrayList<>();
        List<Integer> donor = new ArrayList<>();
        // 1) Resgate sem crédito, concluído: só o doador ganha
        requester.add(CreditRules.reserveOnRequest(Pedido.TIPO_RESGATE, sum(requester)));
        donor.add(CreditRules.donorCreditOnConclusion(Pedido.TIPO_RESGATE));
        assertEquals(0, sum(requester));
        assertEquals(1, sum(donor));
        // 2) Doador agora pede com crédito: reserva 1
        int r = CreditRules.reserveOnRequest(Pedido.TIPO_RESGATE, sum(donor));
        donor.add(r);
        assertEquals(0, sum(donor));
        // 3) Pedido cancelado: reserva volta
        donor.add(CreditRules.releaseAmount(r));
        assertEquals(1, sum(donor));
        // 4) Outro resgate com saldo 0 não deixa negativo
        requester.add(CreditRules.reserveOnRequest(Pedido.TIPO_RESGATE, sum(requester)));
        assertTrue(sum(requester) >= 0);
    }

    private static int sum(List<Integer> l) {
        int s = 0;
        for (int v : l) s += v;
        return s;
    }
}
