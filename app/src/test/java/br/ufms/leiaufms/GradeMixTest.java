package br.ufms.leiaufms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.data.local.GradeMix;
import br.ufms.leiaufms.data.local.entity.LivroCatalogo;
import br.ufms.leiaufms.data.local.model.GradeRow;

public class GradeMixTest {

    private static GradeRow row(long livro, long topico, int peso) {
        GradeRow r = new GradeRow();
        r.livro = new LivroCatalogo();
        r.livro.id = livro;
        r.topicoId = topico;
        r.peso = peso;
        return r;
    }

    @Test
    public void alternaTopicosNaOrdemDoPeso() {
        List<GradeRow> rows = new ArrayList<>();
        rows.add(row(1, 10, 9));
        rows.add(row(2, 10, 9));
        rows.add(row(3, 10, 9));
        rows.add(row(4, 20, 3));
        rows.add(row(5, 20, 3));
        List<LivroCatalogo> out = GradeMix.mix(rows, 5, 4);
        assertEquals(1, out.get(0).id);
        assertEquals(4, out.get(1).id);
        assertEquals(2, out.get(2).id);
        assertEquals(5, out.get(3).id);
    }

    @Test
    public void limitaQuantidadeDeTopicosESemRepetir() {
        List<GradeRow> rows = new ArrayList<>();
        rows.add(row(1, 10, 9));
        rows.add(row(1, 20, 5));
        rows.add(row(2, 30, 1));
        List<LivroCatalogo> out = GradeMix.mix(rows, 2, 10);
        assertEquals(1, out.size());
    }
}
