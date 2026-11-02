package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.Usuario;

@Dao
public interface UsuarioDao {

    @Insert
    long insert(Usuario usuario);

    @Update
    void update(Usuario usuario);

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    Usuario findByEmail(String email);

    @Query("SELECT * FROM usuario WHERE id = :id")
    Usuario findById(long id);

    @Query("SELECT * FROM usuario WHERE id = :id")
    LiveData<Usuario> observe(long id);

    @Query("SELECT COUNT(*) FROM usuario WHERE email = :email AND id != :exceptId")
    int countEmail(String email, long exceptId);

    @Query("UPDATE usuario SET senhaHash = :hash WHERE id = :id")
    void updatePasswordHash(long id, String hash);

    @Query("UPDATE usuario SET fotoPath = :path WHERE id = :id")
    void updatePhoto(long id, String path);

    @Query("SELECT * FROM usuario WHERE papel = 'MODERADOR'")
    List<Usuario> moderators();

    @Query("SELECT COUNT(*) FROM usuario")
    int count();
}
