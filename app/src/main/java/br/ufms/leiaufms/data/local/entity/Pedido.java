package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Pedido de troca direta ou resgate. Estados em docs/04-arquitetura.md §5. */
@Entity(tableName = "pedido",
        indices = {@Index("anuncioId"), @Index("solicitanteId"), @Index("donoId"), @Index("estado")})
public class Pedido {

    public static final String TIPO_TROCA_DIRETA = "TROCA_DIRETA";
    public static final String TIPO_RESGATE = "RESGATE";


    @PrimaryKey(autoGenerate = true)
    public long id;

    public long anuncioId;

    public long solicitanteId;

    public long donoId;

    @NonNull
    public String tipo = "";

    public Long exemplarOferecidoId;

    @NonNull
    public String estado = "";

    public Long pontoEncontroId;

    public Long encontroEm;

    public boolean confirmadoSolicitante;

    public boolean confirmadoDono;

    public boolean semCredito;

    public String motivo;

    public long criadoEm;

    public long atualizadoEm;

    public Long aceitoEm;

    public boolean lembreteEnviado;
}
