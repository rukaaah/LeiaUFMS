package br.ufms.leiaufms.data.local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.ufms.leiaufms.data.local.entity.LivroCatalogo;
import br.ufms.leiaufms.data.local.model.GradeRow;

/**
 * Mistura os livros de "Da sua grade" (RF-45): os tópicos com mais disciplinas no curso vêm primeiro e
 * se alternam (1 livro de cada tópico por vez), para a lista não virar só um assunto.
 */
public final class GradeMix {

    private GradeMix() {
    }

    public static List<LivroCatalogo> mix(List<GradeRow> rows, int topics, int limit) {
        Map<Long, List<LivroCatalogo>> byTopic = new LinkedHashMap<>();
        if (rows != null) {
            for (GradeRow r : rows) {
                if (!byTopic.containsKey(r.topicoId)) {
                    if (byTopic.size() >= topics) continue;
                    byTopic.put(r.topicoId, new ArrayList<>());
                }
                byTopic.get(r.topicoId).add(r.livro);
            }
        }
        List<LivroCatalogo> out = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (int round = 0; out.size() < limit; round++) {
            boolean any = false;
            for (List<LivroCatalogo> list : byTopic.values()) {
                if (round < list.size()) {
                    any = true;
                    LivroCatalogo l = list.get(round);
                    if (seen.add(l.id)) out.add(l);
                    if (out.size() >= limit) break;
                }
            }
            if (!any) break;
        }
        return out;
    }
}
