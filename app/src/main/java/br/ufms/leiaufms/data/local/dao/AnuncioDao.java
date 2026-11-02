package br.ufms.leiaufms.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import br.ufms.leiaufms.data.local.entity.Anuncio;

@Dao
public interface AnuncioDao {

    @Insert
    long insert(Anuncio anuncio);

    @Update
    void update(Anuncio anuncio);

    @Query("SELECT * FROM anuncio WHERE id = :id")
    Anuncio findById(long id);
}
