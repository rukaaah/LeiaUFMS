package br.ufms.leiaufms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.ufms.leiaufms.data.pedido.PedidoEstado;

public class PedidoEstadoTest {

    @Test
    public void allowedTransitions() {
        assertTrue(PedidoEstado.canTransition(PedidoEstado.PENDENTE, PedidoEstado.ACEITO));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.PENDENTE, PedidoEstado.RECUSADO));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.PENDENTE, PedidoEstado.EXPIRADO));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.PENDENTE, PedidoEstado.CANCELADO));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.ACEITO, PedidoEstado.CONCLUIDO));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.ACEITO, PedidoEstado.CANCELADO));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.ACEITO, PedidoEstado.EM_DISPUTA));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.EM_DISPUTA, PedidoEstado.CONCLUIDO));
        assertTrue(PedidoEstado.canTransition(PedidoEstado.EM_DISPUTA, PedidoEstado.CANCELADO));
    }

    @Test
    public void forbiddenTransitions() {
        assertFalse(PedidoEstado.canTransition(PedidoEstado.PENDENTE, PedidoEstado.CONCLUIDO));
        assertFalse(PedidoEstado.canTransition(PedidoEstado.PENDENTE, PedidoEstado.EM_DISPUTA));
        assertFalse(PedidoEstado.canTransition(PedidoEstado.CONCLUIDO, PedidoEstado.CANCELADO));
        assertFalse(PedidoEstado.canTransition(PedidoEstado.RECUSADO, PedidoEstado.ACEITO));
        assertFalse(PedidoEstado.canTransition(PedidoEstado.EXPIRADO, PedidoEstado.ACEITO));
        assertFalse(PedidoEstado.canTransition(PedidoEstado.EM_DISPUTA, PedidoEstado.ACEITO));
        assertFalse(PedidoEstado.canTransition("X", PedidoEstado.ACEITO));
    }

    @Test(expected = IllegalStateException.class)
    public void requireThrows() {
        PedidoEstado.requireTransition(PedidoEstado.CANCELADO, PedidoEstado.CONCLUIDO);
    }

    @Test
    public void activeAndFinal() {
        assertTrue(PedidoEstado.isActive(PedidoEstado.PENDENTE));
        assertTrue(PedidoEstado.isActive(PedidoEstado.EM_DISPUTA));
        assertFalse(PedidoEstado.isActive(PedidoEstado.CONCLUIDO));
        assertTrue(PedidoEstado.isFinal(PedidoEstado.EXPIRADO));
        assertFalse(PedidoEstado.isFinal(PedidoEstado.ACEITO));
    }
}
