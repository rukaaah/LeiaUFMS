package br.ufms.leiaufms.ui.common;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.ui.profile.ProfileFragment;

/** Abre a tela de destino de um aviso. Destinos de pedido e anúncio entram na Parte 3. */
public final class Navigator {

    private Navigator() {
    }

    public static void openDestination(Fragment from, String tipo, Long id) {
        if (tipo == null || id == null) return;
        Bundle args = new Bundle();
        if (Notificacao.DESTINO_PERFIL.equals(tipo)) {
            args.putLong(ProfileFragment.ARG_USER_ID, id);
            NavHostFragment.findNavController(from).navigate(R.id.userProfileFragment, args);
        }
    }
}
