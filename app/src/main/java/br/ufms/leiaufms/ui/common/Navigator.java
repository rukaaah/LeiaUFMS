package br.ufms.leiaufms.ui.common;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;
import br.ufms.leiaufms.ui.profile.ProfileFragment;
import br.ufms.leiaufms.ui.trade.TradeDetailFragment;

/** Abre a tela de destino de um aviso (pedido, anúncio ou perfil). */
public final class Navigator {

    private Navigator() {
    }

    public static void openDestination(Fragment from, String tipo, Long id) {
        if (tipo == null || id == null) return;
        Bundle args = new Bundle();
        if (Notificacao.DESTINO_PEDIDO.equals(tipo)) {
            args.putLong(TradeDetailFragment.ARG_ID, id);
            NavHostFragment.findNavController(from).navigate(R.id.tradeDetailFragment, args);
        } else if (Notificacao.DESTINO_ANUNCIO.equals(tipo)) {
            args.putLong(ListingDetailFragment.ARG_ID, id);
            NavHostFragment.findNavController(from).navigate(R.id.listingDetailFragment, args);
        } else if (Notificacao.DESTINO_PERFIL.equals(tipo)) {
            args.putLong(ProfileFragment.ARG_USER_ID, id);
            NavHostFragment.findNavController(from).navigate(R.id.userProfileFragment, args);
        } else if (Notificacao.DESTINO_POST.equals(tipo)) {
            args.putLong(br.ufms.leiaufms.ui.feed.PostFragment.ARG_ID, id);
            NavHostFragment.findNavController(from).navigate(R.id.postFragment, args);
        }
    }
}
