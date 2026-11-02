package br.ufms.leiaufms.data.pedido;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados do pedido (docs/04-arquitetura.md §5).
 * Transição fora desta tabela é erro de programação.
 */
public final class PedidoEstado {

    public static final String PENDENTE = "PENDENTE";
    public static final String ACEITO = "ACEITO";
    public static final String RECUSADO = "RECUSADO";
    public static final String CANCELADO = "CANCELADO";
    public static final String EXPIRADO = "EXPIRADO";
    public static final String CONCLUIDO = "CONCLUIDO";
    public static final String EM_DISPUTA = "EM_DISPUTA";

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();

    static {
        TRANSITIONS.put(PENDENTE, set(ACEITO, RECUSADO, EXPIRADO, CANCELADO));
        TRANSITIONS.put(ACEITO, set(CANCELADO, CONCLUIDO, EM_DISPUTA));
        TRANSITIONS.put(EM_DISPUTA, set(CONCLUIDO, CANCELADO));
        TRANSITIONS.put(RECUSADO, Collections.emptySet());
        TRANSITIONS.put(CANCELADO, Collections.emptySet());
        TRANSITIONS.put(EXPIRADO, Collections.emptySet());
        TRANSITIONS.put(CONCLUIDO, Collections.emptySet());
    }

    private PedidoEstado() {
    }

    private static Set<String> set(String... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }

    public static boolean canTransition(String from, String to) {
        Set<String> targets = TRANSITIONS.get(from);
        return targets != null && targets.contains(to);
    }

    /** Lança se a transição não estiver na tabela. */
    public static void requireTransition(String from, String to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Transição inválida: " + from + " -> " + to);
        }
    }

    /** Estados em que o pedido ainda conta como ativo (RN-TROCA-02). */
    public static boolean isActive(String estado) {
        return PENDENTE.equals(estado) || ACEITO.equals(estado) || EM_DISPUTA.equals(estado);
    }

    public static boolean isFinal(String estado) {
        Set<String> targets = TRANSITIONS.get(estado);
        return targets != null && targets.isEmpty();
    }
}
