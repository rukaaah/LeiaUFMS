package br.ufms.leiaufms.ui.meeting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponent;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.LocationProvider;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.databinding.DialogPointBinding;
import br.ufms.leiaufms.databinding.FragmentMeetingBinding;
import br.ufms.leiaufms.ui.common.TimeText;

/**
 * Marcar encontro (RF-21, RF-29) e, para o Moderador, cadastrar pontos seguros tocando no mapa (RF-30).
 * MapLibre + OpenFreeMap (ADR-03). Sem internet, o mapa some e a lista de pontos continua.
 */
public class MeetingFragment extends Fragment {

    public static final String ARG_PEDIDO = "pedidoId";
    public static final String ARG_MANAGE = "gerenciar";

    private FragmentMeetingBinding binding;
    private MeetingViewModel viewModel;
    private PermissionHelper permissions;
    private LocationProvider locationProvider;
    private MapLibreMap map;
    private Style style;
    private boolean manage;
    private final List<PontoEncontro> points = new ArrayList<>();
    private final List<Marker> markers = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(requireContext().getApplicationContext());
        permissions = new PermissionHelper(this);
        locationProvider = new LocationProvider(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMeetingBinding.inflate(inflater, container, false);
        binding.map.onCreate(savedInstanceState);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        manage = getArguments() != null && getArguments().getBoolean(ARG_MANAGE, false);
        long pedidoId = getArguments() == null ? -1 : getArguments().getLong(ARG_PEDIDO, -1);
        viewModel = new ViewModelProvider(this).get(MeetingViewModel.class);
        viewModel.init(pedidoId);

        PointAdapter adapter = new PointAdapter(manage, new PointAdapter.Listener() {
            @Override
            public void onSelect(PontoEncontro p) {
                if (!manage) select(p);
                moveTo(p.lat, p.lng, 16);
            }

            @Override
            public void onRemove(PontoEncontro p) {
                confirmRemove(p);
            }
        });
        binding.points.setAdapter(adapter);
        binding.instructions.setText(manage ? R.string.points_instructions : R.string.choose_point_first);
        if (manage) {
            com.google.android.material.appbar.MaterialToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
            if (toolbar != null) toolbar.setTitle(R.string.title_points_manage);
        }
        binding.instructions.setVisibility(manage ? View.VISIBLE : View.GONE);
        binding.scheduleBox.setVisibility(manage ? View.GONE : View.VISIBLE);

        viewModel.points().observe(getViewLifecycleOwner(), list -> {
            points.clear();
            if (list != null) points.addAll(list);
            adapter.submit(points);
            boolean empty = points.isEmpty();
            binding.pointsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.pointsEmpty.setText(manage ? R.string.points_empty_moderator : R.string.points_empty);
            binding.buttonConfirm.setEnabled(!empty);
            drawMarkers();
            if (viewModel.selected != null) adapter.select(viewModel.selected.id);
        });
        viewModel.selection().observe(getViewLifecycleOwner(), p -> {
            adapter.select(p == null ? -1 : p.id);
            binding.selected.setText(p == null ? getString(R.string.choose_point_first)
                    : getString(R.string.point_selected, p.nome));
        });
        viewModel.when().observe(getViewLifecycleOwner(), t -> renderWhen());
        viewModel.events().observe(getViewLifecycleOwner(), this::onEvent);

        binding.buttonDate.setOnClickListener(v -> pickDate());
        binding.buttonTime.setOnClickListener(v -> pickTime());
        binding.buttonConfirm.setOnClickListener(v -> confirm());
        binding.buttonMyLocation.setOnClickListener(v -> showMyLocation());
        // Alternativa ao toque no mapa (TalkBack e quem está no próprio ponto): usa a posição atual
        binding.buttonPointHere.setVisibility(manage ? View.VISIBLE : View.GONE);
        binding.buttonPointHere.setOnClickListener(v -> addPointHere());

        if (!isOnline()) {
            showOffline();
        } else {
            binding.map.addOnDidFailLoadingMapListener(message -> showOffline());
            binding.map.getMapAsync(m -> {
                map = m;
                m.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(AppConfig.MAP_START_LAT, AppConfig.MAP_START_LNG), AppConfig.MAP_START_ZOOM));
                m.setStyle(new Style.Builder().fromUri(AppConfig.MAP_STYLE_URL), s -> {
                    style = s;
                    drawMarkers();
                    fitPoints();
                });
                m.setOnMarkerClickListener(marker -> {
                    int i = markers.indexOf(marker);
                    if (i >= 0 && !manage) select(points.get(i));
                    return false;
                });
                if (manage) {
                    m.addOnMapClickListener(latLng -> {
                        askPointName(latLng);
                        return true;
                    });
                }
            });
        }
    }

    private void select(PontoEncontro p) {
        viewModel.select(p);
        binding.errorDate.setVisibility(View.GONE);
    }

    private void drawMarkers() {
        if (map == null || style == null) return;
        map.removeAnnotations();
        markers.clear();
        for (PontoEncontro p : points) {
            markers.add(map.addMarker(new MarkerOptions().position(new LatLng(p.lat, p.lng))
                    .title(p.nome).snippet(p.descricao)));
        }
    }

    private void fitPoints() {
        if (points.isEmpty() || map == null) return;
        if (points.size() == 1) {
            moveTo(points.get(0).lat, points.get(0).lng, 16);
            return;
        }
        org.maplibre.android.geometry.LatLngBounds.Builder b = new org.maplibre.android.geometry.LatLngBounds.Builder();
        for (PontoEncontro p : points) b.include(new LatLng(p.lat, p.lng));
        int pad = getResources().getDimensionPixelSize(R.dimen.space_xxl);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), pad));
    }

    private void moveTo(double lat, double lng, double zoom) {
        if (map != null) map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }

    private void showOffline() {
        if (binding == null) return;
        binding.mapBox.setVisibility(View.GONE);
        binding.mapNotice.setVisibility(View.VISIBLE);
        binding.mapNotice.setText(R.string.map_offline);
        binding.instructions.setVisibility(View.GONE);
    }

    private boolean isOnline() {
        ConnectivityManager cm = requireContext().getSystemService(ConnectivityManager.class);
        if (cm == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /** RF-29: posição do usuário no mapa, só se ele pedir (sem rastreamento contínuo). */
    private void showMyLocation() {
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                locationProvider.current(new LocationProvider.Callback() {
                    @Override
                    @SuppressLint("MissingPermission")
                    public void onLocation(android.location.Location l) {
                        if (binding == null || map == null || style == null) return;
                        LocationComponent lc = map.getLocationComponent();
                        if (!lc.isLocationComponentActivated()) {
                            lc.activateLocationComponent(LocationComponentActivationOptions
                                    .builder(requireContext(), style).useDefaultLocationEngine(false).build());
                        }
                        lc.setLocationComponentEnabled(true);
                        lc.forceLocationUpdate(l);
                        moveTo(l.getLatitude(), l.getLongitude(), 16);
                    }

                    @Override
                    public void onFailure(LocationProvider.Failure f) {
                        if (binding == null) return;
                        snack(f == LocationProvider.Failure.DISABLED ? R.string.location_disabled
                                : R.string.location_unavailable);
                    }
                });
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding != null) snack(R.string.location_denied);
            }
        }, LocationProvider.PERMISSIONS);
    }

    private void addPointHere() {
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                locationProvider.current(new LocationProvider.Callback() {
                    @Override
                    public void onLocation(android.location.Location l) {
                        if (binding != null) askPointName(new LatLng(l.getLatitude(), l.getLongitude()));
                    }

                    @Override
                    public void onFailure(LocationProvider.Failure f) {
                        if (binding != null) snack(f == LocationProvider.Failure.DISABLED
                                ? R.string.location_disabled : R.string.location_unavailable);
                    }
                });
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding != null) snack(R.string.location_denied);
            }
        }, LocationProvider.PERMISSIONS);
    }

    private void pickDate() {
        long now = System.currentTimeMillis();
        long limit = viewModel.deadline();
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(now - 24L * 60 * 60 * 1000)
                .setEnd(limit)
                .setValidator(com.google.android.material.datepicker.CompositeDateValidator.allOf(Arrays.asList(
                        DateValidatorPointForward.from(now - 24L * 60 * 60 * 1000),
                        DateValidatorPointBackward.before(limit))))
                .build();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.choose_date)
                .setCalendarConstraints(constraints)
                .setSelection(viewModel.dateUtc() != null ? viewModel.dateUtc() : MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(viewModel::setDateUtc);
        picker.show(getChildFragmentManager(), "data");
    }

    private void pickTime() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(viewModel.hour())
                .setMinute(viewModel.minute())
                .setTitleText(R.string.choose_time)
                .build();
        picker.addOnPositiveButtonClickListener(v -> viewModel.setTime(picker.getHour(), picker.getMinute()));
        picker.show(getChildFragmentManager(), "hora");
    }

    private void renderWhen() {
        Long date = viewModel.dateUtc();
        if (date != null) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.setTimeInMillis(date);
            binding.buttonDate.setText(String.format(java.util.Locale.ROOT, "%02d/%02d/%d",
                    c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR)));
        }
        if (viewModel.timeChosen()) {
            binding.buttonTime.setText(String.format(java.util.Locale.ROOT, "%02d:%02d", viewModel.hour(), viewModel.minute()));
        }
    }

    private void confirm() {
        if (viewModel.selected == null) {
            binding.selected.setText(R.string.error_point);
            binding.selected.requestFocus();
            return;
        }
        Long when = viewModel.meetingTime();
        long now = System.currentTimeMillis();
        if (when == null || when <= now || when > viewModel.deadline()) {
            binding.errorDate.setVisibility(View.VISIBLE);
            binding.errorDate.setText(getString(R.string.error_meeting_date, TimeText.dateTime(viewModel.deadline())));
            return;
        }
        binding.errorDate.setVisibility(View.GONE);
        viewModel.schedule();
    }

    private void askPointName(LatLng latLng) {
        DialogPointBinding d = DialogPointBinding.inflate(getLayoutInflater());
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.new_point_title)
                .setView(d.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = d.inputName.getText() == null ? "" : d.inputName.getText().toString();
                    if (name.trim().isEmpty()) {
                        d.layoutName.setError(getString(R.string.error_point_name));
                        return;
                    }
                    String desc = d.inputDescription.getText() == null ? null : d.inputDescription.getText().toString();
                    viewModel.addPoint(name, desc, latLng.getLatitude(), latLng.getLongitude());
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void confirmRemove(PontoEncontro p) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.remove_point_title)
                .setMessage(R.string.remove_point_message)
                .setNegativeButton(R.string.action_keep, null)
                .setPositiveButton(R.string.action_remove, (d, w) -> viewModel.removePoint(p.id))
                .show();
    }

    private void onEvent(String e) {
        if (e == null) return;
        viewModel.consume();
        switch (e) {
            case "scheduled":
                snack(R.string.meeting_done);
                NavHostFragment.findNavController(this).popBackStack();
                break;
            case "point_saved":
                snack(R.string.point_saved);
                break;
            case "point_removed":
                snack(R.string.point_removed);
                break;
            case "bad_date":
                binding.errorDate.setVisibility(View.VISIBLE);
                binding.errorDate.setText(getString(R.string.error_meeting_date, TimeText.dateTime(viewModel.deadline())));
                break;
            default:
                snack(R.string.error_generic);
        }
    }

    private void snack(int text) {
        Snackbar.make(requireActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAnchorView(requireActivity().findViewById(R.id.bottom_nav)).show();
    }

    // Ciclo de vida do MapView (exigido pelo MapLibre)
    @Override
    public void onStart() {
        super.onStart();
        if (binding != null) binding.map.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.map.onResume();
    }

    @Override
    public void onPause() {
        if (binding != null) binding.map.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (binding != null) binding.map.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) binding.map.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null) binding.map.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        locationProvider.cancel();
        if (binding != null) binding.map.onDestroy();
        map = null;
        style = null;
        markers.clear();
        binding = null;
        super.onDestroyView();
    }
}
