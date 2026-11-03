package br.ufms.leiaufms.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.LocationProvider;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.data.repository.ListingRepository;
import br.ufms.leiaufms.databinding.FragmentNewListingBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.FieldErrors;
import br.ufms.leiaufms.ui.common.PhotoPicker;

/**
 * Novo anúncio e edição (RF-12, RF-13). O local é a posição atual (pedida só se a pessoa escolher),
 * um ponto seguro cadastrado pelo Moderador ou nenhum. Coordenadas nunca são digitadas.
 */
public class NewListingFragment extends Fragment {

    public static final String ARG_ID = "anuncioId";
    public static final String ARG_TITLE = "titulo";
    public static final String ARG_AUTHOR = "autor";

    private FragmentNewListingBinding binding;
    private NewListingViewModel viewModel;
    private PhotoPicker photoPicker;
    private PermissionHelper permissions;
    private LocationProvider locationProvider;
    private final List<PontoEncontro> points = new ArrayList<>();
    private long anuncioId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new PermissionHelper(this);
        locationProvider = new LocationProvider(requireContext());
        photoPicker = new PhotoPicker(this, uri -> {
            viewModel.photo = uri;
            showPhoto();
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNewListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(NewListingViewModel.class);
        anuncioId = getArguments() == null ? -1 : getArguments().getLong(ARG_ID, -1);
        if (anuncioId > 0) {
            binding.buttonPublish.setText(R.string.action_save);
            viewModel.load(anuncioId);
            viewModel.existing().observe(getViewLifecycleOwner(), r -> {
                if (r != null && savedInstanceState == null) fill(r);
            });
        } else if (getArguments() != null && savedInstanceState == null) {
            // RF-36: anúncio pré-preenchido a partir da estante
            binding.inputTitle.setText(getArguments().getString(ARG_TITLE));
            binding.inputAuthor.setText(getArguments().getString(ARG_AUTHOR));
        }
        Covers.bind(binding.cover.coverImage, binding.cover.coverText, null, "");
        // Sugere obras já cadastradas: evita duplicar a obra e faz o aviso de desejo (RF-32) funcionar
        android.content.Context app = requireContext().getApplicationContext();
        br.ufms.leiaufms.core.util.AppExecutors.io().execute(() -> {
            java.util.List<br.ufms.leiaufms.data.local.entity.Obra> obras =
                    br.ufms.leiaufms.data.local.AppDatabase.get(app).obraDao().all();
            br.ufms.leiaufms.core.util.AppExecutors.main(() -> {
                if (binding == null) return;
                String[] titles = new String[obras.size()];
                for (int i = 0; i < obras.size(); i++) titles[i] = obras.get(i).titulo;
                binding.inputTitle.setSimpleItems(titles);
                binding.inputTitle.setOnItemClickListener((parent, v, pos, id) -> {
                    String chosen = (String) parent.getItemAtPosition(pos);
                    for (br.ufms.leiaufms.data.local.entity.Obra o : obras) {
                        if (o.titulo.equals(chosen)) {
                            binding.inputAuthor.setText(o.autor);
                            if (o.isbn13 != null) binding.inputIsbn.setText(o.isbn13);
                        }
                    }
                });
            });
        });
        showPhoto();
        binding.buttonPhoto.setOnClickListener(v -> photoPicker.show(R.string.label_listing_photo));
        viewModel.points().observe(getViewLifecycleOwner(), list -> {
            points.clear();
            if (list != null) points.addAll(list);
            List<String> names = new ArrayList<>();
            for (PontoEncontro p : points) names.add(p.nome);
            binding.inputPoint.setSimpleItems(names.toArray(new String[0]));
            updateLocationViews();
        });
        binding.inputPoint.setOnItemClickListener((parent, v, position, id) -> {
            viewModel.pontoId = points.get(position).id;
            binding.layoutPoint.setError(null);
        });
        binding.radioLocation.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.loc_current && viewModel.lat == null) captureLocation();
            updateLocationViews();
        });
        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        binding.buttonPublish.setOnClickListener(v -> submit());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        boolean dirty = anuncioId <= 0 && (binding.inputTitle.length() > 0
                                || binding.inputAuthor.length() > 0 || viewModel.photo != null);
                        if (!dirty) {
                            NavHostFragment.findNavController(NewListingFragment.this).popBackStack();
                            return;
                        }
                        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.discard_changes_title)
                                .setMessage(R.string.discard_changes_message)
                                .setNegativeButton(R.string.action_keep_editing, null)
                                .setPositiveButton(R.string.action_discard, (d, w) ->
                                        NavHostFragment.findNavController(NewListingFragment.this).popBackStack())
                                .show();
                    }
                });
    }

    private void fill(ListingRow r) {
        binding.inputTitle.setText(r.titulo);
        binding.inputAuthor.setText(r.autor);
        binding.inputIsbn.setText(r.isbn13);
        binding.inputEdition.setText(r.edicao);
        int cond = Exemplar.CONSERVACAO_NOVO.equals(r.conservacao) ? R.id.cond_new
                : Exemplar.CONSERVACAO_BOM.equals(r.conservacao) ? R.id.cond_good
                : Exemplar.CONSERVACAO_GASTO.equals(r.conservacao) ? R.id.cond_worn : R.id.cond_used;
        binding.chipsCondition.check(cond);
        int mod = Anuncio.MODALIDADE_TROCA.equals(r.modalidade) ? R.id.mod_trade
                : Anuncio.MODALIDADE_DOACAO.equals(r.modalidade) ? R.id.mod_donation : R.id.mod_both;
        binding.chipsModality.check(mod);
        if (r.pontoPreferidoId != null) {
            viewModel.pontoId = r.pontoPreferidoId;
            binding.radioLocation.check(R.id.loc_point);
            binding.inputPoint.setText(r.pontoNome, false);
        } else if (r.lat != null) {
            viewModel.lat = r.lat;
            viewModel.lng = r.lng;
            binding.radioLocation.check(R.id.loc_current);
        }
        if (r.fotoPath != null) {
            Covers.bind(binding.cover.coverImage, binding.cover.coverText, r.fotoPath, r.titulo);
            binding.buttonPhoto.setText(R.string.action_change_listing_photo);
        }
    }

    private void showPhoto() {
        if (viewModel == null || viewModel.photo == null) return;
        binding.cover.coverText.setVisibility(View.GONE);
        binding.cover.coverImage.setImageURI(null);
        binding.cover.coverImage.setImageURI(viewModel.photo);
        binding.buttonPhoto.setText(R.string.action_change_listing_photo);
        binding.cover.getRoot().setContentDescription(getString(R.string.listing_photo_added));
        binding.cover.getRoot().setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    private void updateLocationViews() {
        int checked = binding.radioLocation.getCheckedRadioButtonId();
        boolean point = checked == R.id.loc_point;
        binding.layoutPoint.setVisibility(point && !points.isEmpty() ? View.VISIBLE : View.GONE);
        if (point && points.isEmpty()) {
            binding.locationStatus.setVisibility(View.VISIBLE);
            binding.locationStatus.setText(R.string.no_points_yet);
        } else if (checked == R.id.loc_current && viewModel.lat != null) {
            binding.locationStatus.setVisibility(View.VISIBLE);
            binding.locationStatus.setText(R.string.location_captured);
        } else if (checked != R.id.loc_current) {
            binding.locationStatus.setVisibility(View.GONE);
        }
    }

    private void captureLocation() {
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                binding.locationStatus.setVisibility(View.VISIBLE);
                binding.locationStatus.setText(R.string.location_loading);
                locationProvider.current(new LocationProvider.Callback() {
                    @Override
                    public void onLocation(android.location.Location l) {
                        if (binding == null) return;
                        viewModel.lat = l.getLatitude();
                        viewModel.lng = l.getLongitude();
                        updateLocationViews();
                    }

                    @Override
                    public void onFailure(LocationProvider.Failure f) {
                        if (binding == null) return;
                        fallbackToNone(f == LocationProvider.Failure.DISABLED ? R.string.location_disabled
                                : R.string.location_unavailable);
                    }
                });
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding != null) fallbackToNone(R.string.location_denied);
            }
        }, LocationProvider.PERMISSIONS);
    }

    private void fallbackToNone(int message) {
        binding.radioLocation.check(R.id.loc_none);
        binding.locationStatus.setVisibility(View.VISIBLE);
        binding.locationStatus.setText(message);
    }

    private void submit() {
        String titulo = FieldErrors.text(binding.inputTitle);
        String autor = FieldErrors.text(binding.inputAuthor);
        String isbn = FieldErrors.text(binding.inputIsbn);
        TextInputLayout first = null;
        if (!FieldErrors.show(binding.layoutTitle, Validators.required(titulo, AppConfig.TITLE_MAX),
                R.string.error_empty_title, AppConfig.TITLE_MAX)) first = binding.layoutTitle;
        if (!FieldErrors.show(binding.layoutAuthor, Validators.required(autor, AppConfig.AUTHOR_MAX),
                R.string.error_empty_author, AppConfig.AUTHOR_MAX) && first == null) first = binding.layoutAuthor;
        if (Validators.isbn(isbn) != Validators.Result.OK) {
            binding.layoutIsbn.setError(getString(R.string.error_isbn));
            if (first == null) first = binding.layoutIsbn;
        } else {
            binding.layoutIsbn.setError(null);
        }
        int cond = binding.chipsCondition.getCheckedChipId();
        int mod = binding.chipsModality.getCheckedChipId();
        boolean ok = first == null;
        binding.errorCondition.setVisibility(cond == View.NO_ID ? View.VISIBLE : View.GONE);
        binding.errorCondition.setText(R.string.error_choose_condition);
        binding.errorModality.setVisibility(mod == View.NO_ID ? View.VISIBLE : View.GONE);
        binding.errorModality.setText(R.string.error_choose_modality);
        if (cond == View.NO_ID || mod == View.NO_ID) ok = false;
        int loc = binding.radioLocation.getCheckedRadioButtonId();
        if (loc == R.id.loc_point && viewModel.pontoId == null) {
            binding.layoutPoint.setError(getString(R.string.error_choose_point));
            if (points.isEmpty()) {
                binding.locationStatus.setVisibility(View.VISIBLE);
                binding.locationStatus.setText(R.string.error_choose_point);
            }
            ok = false;
        }
        if (!ok) {
            if (first != null && first.getEditText() != null) first.getEditText().requestFocus();
            else if (cond == View.NO_ID) binding.errorCondition.requestFocus();
            return;
        }
        ListingRepository.Form f = new ListingRepository.Form();
        f.titulo = titulo;
        f.autor = autor;
        f.isbn = isbn;
        f.edicao = FieldErrors.text(binding.inputEdition);
        f.conservacao = cond == R.id.cond_new ? Exemplar.CONSERVACAO_NOVO : cond == R.id.cond_good
                ? Exemplar.CONSERVACAO_BOM : cond == R.id.cond_worn ? Exemplar.CONSERVACAO_GASTO : Exemplar.CONSERVACAO_USADO;
        f.modalidade = mod == R.id.mod_trade ? Anuncio.MODALIDADE_TROCA : mod == R.id.mod_donation
                ? Anuncio.MODALIDADE_DOACAO : Anuncio.MODALIDADE_AMBAS;
        f.foto = viewModel.photo;
        if (loc == R.id.loc_current) {
            f.lat = viewModel.lat;
            f.lng = viewModel.lng;
        } else if (loc == R.id.loc_point) {
            f.pontoId = viewModel.pontoId;
        }
        viewModel.save(anuncioId, f);
    }

    private void render(NewListingViewModel.State s) {
        boolean loading = s == NewListingViewModel.State.LOADING;
        binding.buttonPublish.setEnabled(!loading);
        binding.buttonPublish.setText(loading ? R.string.action_publishing
                : anuncioId > 0 ? R.string.action_save : R.string.action_publish);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        switch (s) {
            case CREATED:
            case SAVED:
                viewModel.consume();
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        s == NewListingViewModel.State.CREATED ? R.string.listing_published : R.string.listing_saved,
                        Snackbar.LENGTH_SHORT).setAnchorView(requireActivity().findViewById(R.id.bottom_nav)).show();
                NavHostFragment.findNavController(this).popBackStack();
                break;
            case BLOCKED:
                viewModel.consume();
                Snackbar.make(binding.getRoot(), R.string.listing_blocked, Snackbar.LENGTH_LONG).show();
                break;
            case ERROR:
                viewModel.consume();
                Snackbar.make(binding.getRoot(), R.string.error_generic, Snackbar.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationProvider.cancel();
        binding = null;
    }
}
