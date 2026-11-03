package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.CatalogoTopico;
import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.DisciplinaTopico;
import br.ufms.leiaufms.data.local.entity.LivroCatalogo;
import br.ufms.leiaufms.data.local.entity.ObraTopico;
import br.ufms.leiaufms.data.local.entity.Topico;

/** Catálogo de livros, tópicos e disciplinas reais da UFMS (RF-45). */
@Dao
public interface CatalogoDao {

    @Query("SELECT COUNT(*) FROM topico")
    int topicCount();

    @Query("DELETE FROM catalogo_topico")
    void clearCatalogoTopicos();

    @Query("DELETE FROM disciplina_topico")
    void clearDisciplinaTopicos();

    @Query("DELETE FROM obra_topico")
    void clearObraTopicos();

    @Query("DELETE FROM livro_catalogo")
    void clearLivros();

    @Query("DELETE FROM topico")
    void clearTopicos();

    /** Disciplinas vindas do catálogo têm código; as do seed de demonstração, não. */
    @Query("DELETE FROM disciplina WHERE codigo IS NOT NULL")
    void clearDisciplinasDoCatalogo();

    @Query("UPDATE obra SET catalogoId = NULL")
    void unlinkObras();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTopicos(List<Topico> t);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLivros(List<LivroCatalogo> l);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCatalogoTopicos(List<CatalogoTopico> l);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDisciplinaTopicos(List<DisciplinaTopico> l);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertObraTopicos(List<ObraTopico> l);

    @Insert
    List<Long> insertDisciplinas(List<Disciplina> d);

    @Query("SELECT * FROM livro_catalogo WHERE chave = :chave LIMIT 1")
    LivroCatalogo byChave(String chave);

    @Query("SELECT * FROM livro_catalogo WHERE id = :id")
    LivroCatalogo byId(long id);

    @Query("SELECT topicoId FROM catalogo_topico WHERE livroId = :livroId")
    List<Long> topicsOfLivro(long livroId);

    /** Sugestões ao digitar o título: a chave não tem acento, então "calculo" acha "Cálculo". */
    @Query("SELECT * FROM livro_catalogo WHERE chave LIKE '%' || :q || '%' "
            + "ORDER BY CASE WHEN chave LIKE :q || '%' THEN 0 ELSE 1 END, capaOl IS NULL, titulo LIMIT 40")
    List<LivroCatalogo> search(String q);

    /**
     * "Da sua grade": livros do catálogo por tópico, com o peso do tópico no curso (quantas disciplinas do
     * curso caem nele). Sem os que a pessoa já deseja. A mistura entre tópicos é feita em GradeMix.
     */
    @Query("SELECT lc.*, ct.topicoId AS topicoId, t.n AS peso FROM livro_catalogo lc "
            + "JOIN catalogo_topico ct ON ct.livroId = lc.id "
            + "JOIN (SELECT dt.topicoId AS topicoId, COUNT(*) AS n FROM disciplina_topico dt "
            + " JOIN disciplina d ON d.id = dt.disciplinaId WHERE d.curso = :curso GROUP BY dt.topicoId) t "
            + " ON t.topicoId = ct.topicoId "
            + "WHERE lc.id NOT IN (SELECT o.catalogoId FROM desejo de JOIN obra o ON o.id = de.obraId "
            + " WHERE de.usuarioId = :me AND o.catalogoId IS NOT NULL) "
            + "ORDER BY t.n DESC, lc.capaOl IS NULL, (lc.id * 7919) % 997 LIMIT 400")
    LiveData<List<br.ufms.leiaufms.data.local.model.GradeRow>> forCourse(String curso, long me);

    /** Disciplinas do curso que têm livros recomendados (para a tela de disciplina e o Perfil). */
    @Query("SELECT d.* FROM disciplina d WHERE d.curso = :curso AND d.id IN (SELECT disciplinaId FROM disciplina_topico) "
            + "ORDER BY d.nome COLLATE NOCASE")
    LiveData<List<Disciplina>> disciplinasDoCurso(String curso);

    /** Livros do catálogo recomendados para uma disciplina. */
    @Query("SELECT lc.* FROM livro_catalogo lc JOIN catalogo_topico ct ON ct.livroId = lc.id "
            + "JOIN disciplina_topico dt ON dt.topicoId = ct.topicoId WHERE dt.disciplinaId = :disciplinaId "
            + "GROUP BY lc.id ORDER BY lc.capaOl IS NULL, lc.titulo LIMIT 60")
    List<LivroCatalogo> livrosDaDisciplina(long disciplinaId);
}
