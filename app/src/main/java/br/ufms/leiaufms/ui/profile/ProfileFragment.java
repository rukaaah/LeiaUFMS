package br.ufms.leiaufms.ui.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.media.ImageStore;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.repository.UserRepository;
import br.ufms.leiaufms.databinding.FragmentProfileBinding;
import br.ufms.leiaufms.databinding.SheetPhotoBinding;
import br.ufms.leiaufms.ui.common.ImageLoader;
import br.ufms.leiaufms.ui.main.MainActivity;

/**
 * Cartão de confiança (RF-06), foto pela câmera (RF-09) e pela galeria (RF-10), logout (RF-05).
 * Com o argumento "userId" mostra o perfil de outro aluno, sem saldo e sem opções da conta.
 */
public class ProfileFragment extends Fragment {

    public static final String ARG_USER_ID = "userId";

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private PermissionHelper permissions;
    private ActivityResultLauncher<Uri> takePicture;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;
    private boolean own;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new PermissionHelper(this);
        takePicture = registerForActivityResult(new ActivityResultContracts.TakePicture(), saved -> {
            // Cancelar a câmera não muda a foto atual
            if (Boolean.TRUE.equals(saved)) viewModel.savePhoto(ImageStore.newCaptureUri(requireContext()));
        });
        pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) viewModel.savePhoto(uri);
        });
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

        binding.buttonChangePhoto.setVisibility(own ? View.VISIBLE : View.GONE);
        binding.creditsBox.setVisibility(own ? View.VISIBLE : View.GONE);
        binding.accountSection.setVisibility(own ? View.VISIBLE : View.GONE);
        if (own) {
            binding.buttonChangePhoto.setOnClickListener(v -> showPhotoSheet());
            binding.avatar.getRoot().setOnClickListener(v -> showPhotoSheet());
            binding.buttonEdit.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.editProfileFragment));
            binding.buttonPassword.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.changePasswordFragment));
            binding.buttonInbox.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.inboxFragment));
            binding.buttonLogout.setOnClickListener(v -> confirmLogout());
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
        int size = getResources().getDimensionPixelSize(R.dimen.avatar_large);
        boolean hasPhoto = ImageLoader.load(binding.avatar.avatarImage, u.fotoPath, size);
        binding.avatar.avatarInitials.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        binding.avatar.avatarInitials.setText(ImageLoader.initials(u.nome));
        binding.avatar.getRoot().setContentDescription(hasPhoto
                ? getString(R.string.photo_of, u.nome) : getString(R.string.initials_of, u.nome));
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
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        SheetPhotoBinding sb = SheetPhotoBinding.inflate(getLayoutInflater());
        sb.optionCamera.setOnClickListener(v -> {
            sheet.dismiss();
            openCamera();
        });
        sb.optionGallery.setOnClickListener(v -> {
            sheet.dismiss();
            pickImage.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        sheet.setContentView(sb.getRoot());
        sheet.show();
    }

    private void openCamera() {
        if (!requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Snackbar.make(binding.getRoot(), R.string.camera_unavailable, Snackbar.LENGTH_LONG).show();
            return;
        }
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                try {
                    takePicture.launch(ImageStore.newCaptureUri(requireContext()));
                } catch (android.content.ActivityNotFoundException e) {
                    Snackbar.make(binding.getRoot(), R.string.camera_unavailable, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding == null) return;
                if (permanently) {
                    Snackbar.make(binding.getRoot(), R.string.camera_denied, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open_settings,
                                    v -> PermissionHelper.openAppSettings(requireContext()))
                            .show();
                } else {
                    Snackbar.make(binding.getRoot(), R.string.camera_rationale, Snackbar.LENGTH_LONG).show();
                }
            }
        }, Manifest.permission.CAMERA);
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
