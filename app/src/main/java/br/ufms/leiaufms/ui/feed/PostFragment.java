package br.ufms.leiaufms.ui.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.PostMural;
import br.ufms.leiaufms.data.local.entity.Reacao;
import br.ufms.leiaufms.data.local.model.PostRow;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.FragmentPostBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.TimeText;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;
import br.ufms.leiaufms.ui.profile.ProfileFragment;

/** Post do mural aberto (feedback 03/11): texto inteiro, reações, comentários (RF-43) e compartilhar. */
public class PostFragment extends Fragment {

    public static final String ARG_ID = "postId";

    private FragmentPostBinding binding;
    private PostRow current;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long postId = requireArguments().getLong(ARG_ID);
        long me = SessionManager.get(requireContext()).currentUserId();
        Context app = requireContext().getApplicationContext();
        AppDatabase db = AppDatabase.get(requireContext());

        CommentAdapter comments = new CommentAdapter();
        binding.comments.setAdapter(comments);
        db.socialDao().post(me, postId).observe(getViewLifecycleOwner(), p -> {
            if (p == null) {
                Snackbar.make(binding.getRoot(), R.string.post_gone, Snackbar.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).popBackStack();
                return;
            }
            current = p;
            render(p);
        });
        db.socialDao().comments(postId).observe(getViewLifecycleOwner(), list -> {
            comments.submit(list);
            boolean empty = list == null || list.isEmpty();
            binding.commentsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        binding.reactUseful.setOnClickListener(v -> {
            PostAdapter.bounce(v);
            AppExecutors.io().execute(() -> new SocialRepository(app).toggleReaction(postId, me, Reacao.TIPO_UTIL));
        });
        binding.reactWant.setOnClickListener(v -> {
            PostAdapter.bounce(v);
            AppExecutors.io().execute(() -> new SocialRepository(app).toggleReaction(postId, me, Reacao.TIPO_QUERO));
        });
        binding.reactComments.setOnClickListener(v -> {
            binding.inputComment.requestFocus();
            android.view.inputmethod.InputMethodManager imm = requireContext().getSystemService(
                    android.view.inputmethod.InputMethodManager.class);
            if (imm != null) imm.showSoftInput(binding.inputComment, 0);
        });
        binding.buttonShare.setOnClickListener(v -> share());
        binding.inputComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                binding.buttonSend.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.buttonSend.setOnClickListener(v -> {
            String text = binding.inputComment.getText() == null ? "" : binding.inputComment.getText().toString();
            binding.buttonSend.setEnabled(false);
            AppExecutors.io().execute(() -> {
                SocialRepository.Result r = new SocialRepository(app).comment(postId, me, text);
                AppExecutors.main(() -> {
                    if (binding == null) return;
                    if (r == SocialRepository.Result.OK) {
                        binding.inputComment.setText("");
                        binding.scroll.post(() -> binding.scroll.fullScroll(View.FOCUS_DOWN));
                    } else {
                        binding.buttonSend.setEnabled(true);
                        Snackbar.make(binding.getRoot(), R.string.comment_error, Snackbar.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    private void render(PostRow p) {
        Context c = requireContext();
        int[] col = PostAdapter.colors(p.tipo);
        int on = c.getColor(col[1]);
        android.content.res.ColorStateList onList = android.content.res.ColorStateList.valueOf(on);
        binding.card.setCardBackgroundColor(c.getColor(col[0]));
        binding.tag.setText(col[2]);
        binding.tag.setTextColor(on);
        binding.tag.setCompoundDrawablesRelativeWithIntrinsicBounds(PostAdapter.icon(p.tipo), 0, 0, 0);
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(binding.tag, onList);
        Covers.bind(binding.cover.coverImage, binding.cover.coverText, p.capaPath, p.titulo);
        binding.book.setText(p.titulo);
        binding.book.setTextColor(on);
        binding.bookAuthor.setText(p.autor);
        binding.bookAuthor.setTextColor(on);
        binding.text.setText(p.texto);
        binding.text.setTextColor(on);
        binding.meta.setText(getString(R.string.post_by, p.autorNome, TimeText.ago(c, p.criadoEm)));
        binding.meta.setTextColor(on);
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(binding.meta, onList);
        binding.meta.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(ProfileFragment.ARG_USER_ID, p.autorId);
            NavHostFragment.findNavController(this).navigate(R.id.userProfileFragment, args);
        });
        boolean hasListing = p.anuncioId != null;
        binding.buttonListing.setVisibility(hasListing ? View.VISIBLE : View.GONE);
        View.OnClickListener open = v -> {
            Bundle args = new Bundle();
            if (hasListing) {
                args.putLong(ListingDetailFragment.ARG_ID, p.anuncioId);
                NavHostFragment.findNavController(this).navigate(R.id.listingDetailFragment, args);
            } else {
                args.putLong(br.ufms.leiaufms.ui.book.ObraFragment.ARG_ID, p.obraId);
                NavHostFragment.findNavController(this).navigate(R.id.obraFragment, args);
            }
        };
        binding.buttonListing.setOnClickListener(open);
        binding.bookRow.setOnClickListener(open);
        int base = c.getColor(R.color.leia_on_surface_variant);
        PostAdapter.bindReaction(binding.reactUseful, p.meuUtil, p.uteis, R.string.react_useful,
                R.plurals.react_useful_count, R.drawable.ic_thumb_up, R.drawable.ic_thumb_up, c,
                p.meuUtil ? c.getColor(R.color.leia_primary) : base);
        PostAdapter.bindReaction(binding.reactWant, p.meuQuero, p.queros, R.string.react_want,
                R.plurals.react_want_count, R.drawable.ic_favorite, R.drawable.ic_favorite_border, c,
                p.meuQuero ? c.getColor(R.color.leia_primary) : base);
        binding.reactComments.setText(p.comentarios > 0 ? String.valueOf(p.comentarios) : "");
        binding.reactComments.setContentDescription(p.comentarios > 0
                ? getResources().getQuantityString(R.plurals.comment_count, p.comentarios, p.comentarios)
                : getString(R.string.action_comment));
        binding.reactComments.setTextColor(base);
        binding.reactComments.setIconTint(android.content.res.ColorStateList.valueOf(base));
        binding.buttonShare.setIconTint(android.content.res.ColorStateList.valueOf(base));
    }

    /** Compartilhar com amigos fora do app (folha de compartilhamento do Android). */
    private void share() {
        if (current == null) return;
        String type = getString(PostAdapter.colors(current.tipo)[2]);
        String text = getString(R.string.share_post_text, type, current.titulo, current.autor, current.texto, current.autorNome);
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, getString(R.string.action_share)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
