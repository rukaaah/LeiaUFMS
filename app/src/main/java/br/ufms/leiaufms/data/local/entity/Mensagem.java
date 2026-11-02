package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Mensagem do chat do pedido (RF-40). Só existe com pedido ACEITO. */
@Entity(tableName = "mensagem",
        indices = {@Index("pedidoId")})
public class Mensagem {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long pedidoId;

    public long autorId;

    @NonNull
    public String texto = "";

    public boolean pronta;

    public long enviadaEm;
}
