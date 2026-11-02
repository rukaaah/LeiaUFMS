package br.ufms.leiaufms.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.repository.UserRepository;
import br.ufms.leiaufms.databinding.FragmentProfileBinding;
import br.ufms.leiaufms.ui.common.ImageLoader;
import br.ufms.leiaufms.ui.common.PhotoPicker;
import br.ufms.leiaufms.ui.main.MainActivity;
import br.ufms.leiaufms.ui.meeting.MeetingFragment;

/**
 * Cartão de confiança (RF-06), foto pela câmera (RF-09) e pela galeria (RF-10), logout (RF-05).
 * Com o argumento "userId" mostra o perfil de outro aluno, sem saldo e sem opções da conta.
 */
public class ProfileFragment extends Fragment {

    public static final String ARG_USER_ID = "userId";

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private PhotoPicker photoPicker;
    private boolean own;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoPicker = new PhotoPicker(this, uri -> viewModel.savePhoto(uri));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long sessionUser = SessionManager.get(requireContext()).currentUserId();
        long argUser = getArguments() == null ? -1 : getArguments().getLong(ARG_USER_ID, -1);
        long userId = argUser > 0 ? argUser : sessionUser;
        own = userId == sessionUser;

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.init(userId, own);
        viewModel.user().observe(getViewLifecycleOwner(), this::renderUser);
        viewModel.metrics().observe(getViewLifecycleOwner(), this::renderMetrics);
        viewModel.photoState().observe(getViewLifecycleOwner(), this::renderPhotoState);
        // RF-35: estante pública no perfil (próprio ou de outro aluno)
        AppDatabase.get(requireContext()).socialDao().shelf(userId).observe(getViewLifecycleOwner(), this::renderShelf);

        binding.buttonChangePhoto.setVisibility(own ? View.VISIBLE : View.GONE);
        binding.creditsBox.setVisibility(own ? View.VISIBLE : View.GONE);
        binding.accountSection.setVisibility(own ? View.VISIBLE : View.GONE);
        if (own) {
            binding.buttonChangePhoto.setOnClickListener(v -> showPhotoSheet());
            binding.avatar.getRoot().setOnClickListener(v -> showPhotoSheet());
            ViewCompat.replaceAccessibilityAction(binding.avatar.getRoot(),
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK,
                    getString(R.string.action_change_photo), null);
            binding.buttonEdit.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.editProfileFragment));
            binding.buttonShelf.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.shelfFragment));
            binding.buttonPassword.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.changePasswordFragment));
            binding.buttonInbox.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.inboxFragment));
            binding.buttonLogout.setOnClickListener(v -> confirmLogout());
            binding.buttonPoints.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putBoolean(MeetingFragment.ARG_MANAGE, true);
                NavHostFragment.findNavController(this).navigate(R.id.meetingFragment, args);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshMetrics();
    }

    private void renderUser(Usuario u) {
        if (u == null) return;
        binding.textName.setText(u.nome);
        binding.textCourse.setText(getString(R.string.profile_course_semester, u.curso, u.semestre));
        boolean hasStatus = u.status != null && !u.status.isEmpty();
        binding.textStatus.setVisibility(hasStatus ? View.VISIBLE : View.GONE);
        binding.textStatus.setText(u.status);
        // RF-30: só o Moderador cadastra pontos seguros
        binding.buttonPoints.setVisibility(own && u.isModerador() ? View.VISIBLE : View.GONE);
        int size = getResources().getDimensionPixelSize(R.dimen.avatar_large);
        boolean hasPhoto = ImageLoader.load(binding.avatar.avatarImage, u.fotoPath, size);
        binding.avatar.avatarInitials.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        binding.avatar.avatarInitials.setText(ImageLoader.initials(u.nome));
        binding.avatar.getRoot().setContentDescription(hasPhoto
                ? getString(R.string.photo_of, u.nome) : getString(R.string.initials_of, u.nome));
    }

    private void renderShelf(java.util.List<br.ufms.leiaufms.data.local.model.ShelfRow> rows) {
        binding.shelfPreview.removeAllViews();
        if (rows == null || rows.isEmpty()) {
            android.widget.TextView t = line(getString(R.string.profile_shelf_empty));
            binding.shelfPreview.addView(t);
            return;
        }
        for (br.ufms.leiaufms.data.local.model.ShelfRow r : rows) {
            binding.shelfPreview.addView(line(getString(R.string.shelf_line, r.titulo,
                    br.ufms.leiaufms.ui.shelf.ShelfAdapter.statusLabel(requireContext(), r))));
        }
    }

    private android.widget.TextView line(String text) {
        android.widget.TextView t = new android.widget.TextView(requireContext());
        t.setText(text);
        t.setTextAppearance(R.style.TextAppearance_Leia_Body);
        t.setTextColor(requireContext().getColor(R.color.leia_on_background));
        int pad = getResources().getDimensionPixelSize(R.dimen.space_xxs);
        t.setPadding(0, pad, 0, pad);
        return t;
    }

    private void renderMetrics(UserRepository.Metrics m) {
        if (m == null) return;
        binding.valueTrades.setText(String.valueOf(m.trades));
        binding.metricTrades.setContentDescription(getString(R.string.metric_trades) + ": " + m.trades);
        String rating = m.rating == null ? getString(R.string.metric_none)
                : getString(R.string.metric_rating_value, m.rating, m.ratingCount);
        binding.valueRating.setText(rating);
        binding.metricRating.setContentDescription(getString(R.string.metric_rating) + ": "
                + (m.rating == null ? getString(R.string.metric_rating_none) : rating));
        binding.textNoTrades.setVisibility(!own && m.trades == 0 ? View.VISIBLE : View.GONE);
        if (own) {
            binding.textCredits.setText(getResources().getQuantityString(R.plurals.credits_available,
                    m.creditsAvailable, m.creditsAvailable));
            binding.textCreditsReserved.setVisibility(m.creditsReserved > 0 ? View.VISIBLE : View.GONE);
            binding.textCreditsReserved.setText(getResources().getQuantityString(R.plurals.credits_reserved,
                    m.creditsReserved, m.creditsReserved));
        }
    }

    private void renderPhotoState(ProfileViewModel.PhotoState state) {
        if (state == ProfileViewModel.PhotoState.SAVED) {
            Snackbar.make(binding.getRoot(), R.string.photo_updated, Snackbar.LENGTH_SHORT).show();
            viewModel.consumePhotoState();
        } else if (state == ProfileViewModel.PhotoState.ERROR) {
            Snackbar.make(binding.getRoot(), R.string.photo_error, Snackbar.LENGTH_LONG).show();
            viewModel.consumePhotoState();
        }
    }

    private void showPhotoSheet() {
        photoPicker.show(R.string.photo_sheet_title);
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_logout, (d, w) -> ((MainActivity) requireActivity()).logout())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
