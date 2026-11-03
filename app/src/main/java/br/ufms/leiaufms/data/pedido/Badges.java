package br.ufms.leiaufms.data.pedido;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.data.local.entity.Pedido;

/** RF-11 / PC-09: selos Doador (≥ 3 doações) e Veterano (≥ 10 trocas concluídas). */
public final class Badges {

    private Badges() {
    }

    public static boolean isDonor(int doacoes) {
        return doacoes >= AppConfig.BADGE_DONOR_MIN;
    }

    public static boolean isVeteran(int trocas) {
        return trocas >= AppConfig.BADGE_VETERAN_MIN;
    }

    /** Doador recém-conquistado: a doação que acabou de concluir foi a de número 3. */
    public static boolean justEarnedDonor(int doacoes, Pedido p, long usuarioId) {
        return p.donoId == usuarioId && Pedido.TIPO_RESGATE.equals(p.tipo) && doacoes == AppConfig.BADGE_DONOR_MIN;
    }
}
