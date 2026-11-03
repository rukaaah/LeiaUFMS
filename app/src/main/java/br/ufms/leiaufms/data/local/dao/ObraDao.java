package br.ufms.leiaufms.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.EdicaoCompativel;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.ObraDisciplina;

@Dao
public interface ObraDao {

    @Insert
    long insert(Obra obra);

    @androidx.room.Update
    void update(Obra obra);

    @Insert
    long insertDisciplina(Disciplina disciplina);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertObraDisciplina(ObraDisciplina link);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCompativel(EdicaoCompativel link);

    @Query("SELECT * FROM obra WHERE id = :id")
    Obra findById(long id);

    @Query("SELECT * FROM obra WHERE isbn13 = :isbn LIMIT 1")
    Obra findByIsbn(String isbn);

    @Query("SELECT * FROM obra WHERE lower(titulo) = lower(:titulo) AND lower(autor) = lower(:autor) LIMIT 1")
    Obra findByTitleAuthor(String titulo, String autor);

    @Query("SELECT * FROM obra ORDER BY titulo COLLATE NOCASE")
    List<Obra> all();

    @Query("SELECT * FROM obra WHERE titulo LIKE '%' || :q || '%' OR autor LIKE '%' || :q || '%' ORDER BY titulo COLLATE NOCASE LIMIT 30")
    List<Obra> search(String q);

    /** Obras sem capa ainda não tentadas (ou tentadas antes de :antesDe). */
    @Query("SELECT * FROM obra WHERE capaPath IS NULL AND (capaTentativaEm IS NULL OR capaTentativaEm < :antesDe) LIMIT 40")
    List<Obra> withoutCover(long antesDe);

    @Query("UPDATE obra SET capaPath = :path, capaTentativaEm = :quando WHERE id = :id")
    void setCover(long id, String path, long quando);

    @Query("SELECT * FROM disciplina ORDER BY nome COLLATE NOCASE")
    List<Disciplina> disciplinas();
}
