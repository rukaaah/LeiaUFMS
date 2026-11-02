package br.ufms.leiaufms.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import br.ufms.leiaufms.data.local.entity.Pedido;

@Dao
public interface PedidoDao {

    @Insert
    long insert(Pedido pedido);

    @Update
    void update(Pedido pedido);

    @Query("SELECT * FROM pedido WHERE id = :id")
    Pedido findById(long id);
}
