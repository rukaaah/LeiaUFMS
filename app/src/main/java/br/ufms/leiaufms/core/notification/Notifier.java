package br.ufms.leiaufms.core.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.ui.entry.EntryActivity;

/**
 * Notificações (RF-31 a RF-34, ADR-01). Todo aviso vai para a tabela `notificacao` (Caixa de avisos).
 * A notificação do sistema só sai quando o destinatário é a sessão ativa e a permissão existe;
 * senão o aviso fica pendente e é entregue no próximo login dele.
 */
public final class Notifier {

    public static final String CHANNEL_TRADES = "trocas";
    public static final String CHANNEL_WISHES = "desejos";
    public static final String CHANNEL_PENDING = "pendencias";
    public static final String EXTRA_OPEN_INBOX = "abrir_caixa_de_avisos";

    private static final int SUMMARY_ID = 1;

    private Notifier() {
    }

    public static void createChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        manager.createNotificationChannel(channel(context, CHANNEL_TRADES, R.string.channel_trades,
                R.string.channel_trades_desc, NotificationManager.IMPORTANCE_DEFAULT));
        manager.createNotificationChannel(channel(context, CHANNEL_WISHES, R.string.channel_wishes,
                R.string.channel_wishes_desc, NotificationManager.IMPORTANCE_DEFAULT));
        manager.createNotificationChannel(channel(context, CHANNEL_PENDING, R.string.channel_pending,
                R.string.channel_pending_desc, NotificationManager.IMPORTANCE_LOW));
    }

    private static NotificationChannel channel(Context c, String id, int name, int desc, int importance) {
        NotificationChannel ch = new NotificationChannel(id, c.getString(name), importance);
        ch.setDescription(c.getString(desc));
        return ch;
    }

    /** Permissão do Android 13+ e notificações do app ativadas nas configurações. */
    public static boolean canPost(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !PermissionHelper.isGranted(context, Manifest.permission.POST_NOTIFICATIONS)) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    @WorkerThread
    public static void notify(Context context, long usuarioId, String canal, String titulo, String texto,
                              String destinoTipo, Long destinoId) {
        Context app = context.getApplicationContext();
        Notificacao n = new Notificacao();
        n.usuarioId = usuarioId;
        n.canal = canal;
        n.titulo = titulo;
        n.texto = texto;
        n.destinoTipo = destinoTipo;
        n.destinoId = destinoId;
        n.criadaEm = System.currentTimeMillis();
        boolean isActiveUser = SessionManager.get(app).currentUserId() == usuarioId;
        n.entregue = isActiveUser;
        long id = AppDatabase.get(app).notificacaoDao().insert(n);
        if (isActiveUser && canPost(app)) post(app, (int) id, canal, titulo, texto);
    }

    /** Chamado no login e na abertura com sessão válida: entrega o que chegou enquanto a pessoa estava fora. */
    @WorkerThread
    public static void deliverPending(Context context, long usuarioId) {
        Context app = context.getApplicationContext();
        List<Notificacao> pending = AppDatabase.get(app).notificacaoDao().undelivered(usuarioId);
        if (pending.isEmpty()) return;
        AppDatabase.get(app).notificacaoDao().markDelivered(usuarioId);
        if (!canPost(app)) return;
        if (pending.size() == 1) {
            Notificacao n = pending.get(0);
            post(app, (int) n.id, n.canal, n.titulo, n.texto);
        } else {
            String text = app.getResources().getQuantityString(R.plurals.notif_pending_text,
                    pending.size(), pending.size());
            post(app, SUMMARY_ID, CHANNEL_PENDING, app.getString(R.string.notif_pending_title), text);
        }
    }

    @SuppressWarnings("MissingPermission")
    private static void post(Context app, int id, String canal, String titulo, String texto) {
        Intent intent = new Intent(app, EntryActivity.class);
        intent.putExtra(EXTRA_OPEN_INBOX, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent content = PendingIntent.getActivity(app, id, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, canal)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(app.getColor(R.color.leia_icon_background))
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setContentIntent(content)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL);
        try {
            NotificationManagerCompat.from(app).notify(id, builder.build());
        } catch (SecurityException ignored) {
            // Permissão revogada entre a checagem e o envio: o aviso continua na Caixa de avisos
        }
    }
}
