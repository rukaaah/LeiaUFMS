package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Caixa de avisos (RF-31 a RF-34, ADR-01). */
@Entity(tableName = "notificacao",
        indices = {@Index("usuarioId")})
public class Notificacao {

    public static final String DESTINO_PEDIDO = "PEDIDO";
    public static final String DESTINO_ANUNCIO = "ANUNCIO";
    public static final String DESTINO_PERFIL = "PERFIL";
    public static final String DESTINO_POST = "POST";


    @PrimaryKey(autoGenerate = true)
    public long id;

    public long usuarioId;

    @NonNull
    public String canal = "";

    @NonNull
    public String titulo = "";

    @NonNull
    public String texto = "";

    public String destinoTipo;

    public Long destinoId;

    public boolean lida;

    public boolean entregue;

    public long criadaEm;
}
