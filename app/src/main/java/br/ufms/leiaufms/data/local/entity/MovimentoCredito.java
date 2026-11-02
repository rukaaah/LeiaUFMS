package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Livro-razão de créditos (ADR-05). Saldo = soma dos valores. */
@Entity(tableName = "movimento_credito",
        indices = {@Index("usuarioId"), @Index("pedidoId")})
public class MovimentoCredito {

    public static final String TIPO_DOACAO = "DOACAO";
    public static final String TIPO_RESGATE = "RESGATE";
    public static final String TIPO_RESERVA = "RESERVA";
    public static final String TIPO_LIBERACAO = "LIBERACAO";


    @PrimaryKey(autoGenerate = true)
    public long id;

    public long usuarioId;

    public int valor;

    @NonNull
    public String tipo = "";

    public Long pedidoId;

    public long criadoEm;
}
