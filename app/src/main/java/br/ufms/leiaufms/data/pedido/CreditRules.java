package br.ufms.leiaufms.data.pedido;

import br.ufms.leiaufms.data.local.entity.Pedido;

/**
 * Regras do livro-razão de créditos (ADR-05, RN-CRED-01 a 05), sem dependência do Android para
 * poderem ser testadas com JUnit.
 */
public final class CreditRules {

    private CreditRules() {
    }

    /** RF-19: resgate com saldo disponível reserva 1 crédito; sem saldo segue sem reserva (RN-CRED-04). */
    public static int reserveOnRequest(String tipo, int saldoDisponivel) {
        return Pedido.TIPO_RESGATE.equals(tipo) && saldoDisponivel >= 1 ? -1 : 0;
    }

    /** Pedido marcado "sem crédito" quando é resgate e não houve reserva. */
    public static boolean isWithoutCredit(String tipo, int saldoDisponivel) {
        return Pedido.TIPO_RESGATE.equals(tipo) && saldoDisponivel < 1;
    }

    /** Recusa, cancelamento ou expiração: devolve a reserva ainda presa (net = -1). */
    public static int releaseAmount(int netReserve) {
        return netReserve < 0 ? -netReserve : 0;
    }

    /** RN-CRED-02: o doador ganha +1 em toda doação concluída, com ou sem crédito do outro lado. */
    public static int donorCreditOnConclusion(String tipo) {
        return Pedido.TIPO_RESGATE.equals(tipo) ? 1 : 0;
    }
}
