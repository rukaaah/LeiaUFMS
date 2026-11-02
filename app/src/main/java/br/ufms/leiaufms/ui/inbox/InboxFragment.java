package br.ufms.leiaufms.ui.inbox;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.databinding.FragmentInboxBinding;
import br.ufms.leiaufms.ui.common.Navigator;

/** Caixa de avisos (RF-31 a RF-34). Funciona com ou sem a permissão de notificação. */
public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;
    private PermissionHelper permissions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new PermissionHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long userId = SessionManager.get(requireContext()).currentUserId();
        AppDatabase db = AppDatabase.get(requireContext());
        NotificationAdapter adapter = new NotificationAdapter(n -> {
            AppExecutors.io().execute(() -> db.notificacaoDao().markRead(n.id));
            Navigator.openDestination(this, n.destinoTipo, n.destinoId);
        });
        binding.list.setAdapter(adapter);
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_inbox);
        binding.empty.emptyTitle.setText(R.string.inbox_empty_title);
        binding.empty.emptyMessage.setText(R.string.inbox_empty_message);
        db.notificacaoDao().observeAll(userId).observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            binding.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
            boolean anyUnread = false;
            if (list != null) for (int i = 0; i < list.size() && !anyUnread; i++) anyUnread = !list.get(i).lida;
            binding.buttonMarkAll.setVisibility(anyUnread ? View.VISIBLE : View.GONE);
        });
        binding.buttonMarkAll.setOnClickListener(v ->
                AppExecutors.io().execute(() -> db.notificacaoDao().markAllRead(userId)));
        binding.buttonEnable.setOnClickListener(v -> enableNotifications());
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.bannerPermission.setVisibility(Notifier.canPost(requireContext()) ? View.GONE : View.VISIBLE);
    }

    /** RF-34: pede a permissão (Android 13+) ou leva às configurações de notificação do app. */
    private void enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !PermissionHelper.isGranted(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                && !PermissionHelper.isPermanentlyDenied(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)) {
            permissions.request(new PermissionHelper.Callback() {
                @Override
                public void onGranted() {
                    if (binding != null) binding.bannerPermission.setVisibility(View.GONE);
                }

                @Override
                public void onDenied(boolean permanently) {
                    // O banner continua; o próximo toque leva às configurações
                }
            }, Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
