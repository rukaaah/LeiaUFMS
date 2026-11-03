package br.ufms.leiaufms.ui.common;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.pedido.PedidoEstado;

/**
 * Barra de 4 etapas da troca (efeito de progresso, docs/08 §2): Pedido → Aceito → Encontro → Concluído.
 * Lida pelo TalkBack como "Etapa N de 4: nome".
 */
public class TradeProgressView extends LinearLayout {

    private static final int[] LABELS = {R.string.step_request, R.string.step_accepted, R.string.step_meeting, R.string.step_done};

    public TradeProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setFocusable(true);
    }

    /** Índice da etapa atual (0 a 3) ou -1 quando o pedido foi encerrado sem troca. */
    public static int stepOf(Pedido p) {
        switch (p.estado) {
            case PedidoEstado.PENDENTE:
                return 0;
            case PedidoEstado.ACEITO:
                return p.encontroEm == null || !p.encontroAceito ? 1 : 2;
            case PedidoEstado.EM_DISPUTA:
                return 2;
            case PedidoEstado.CONCLUIDO:
                return 3;
            default:
                return -1;
        }
    }

    public void setStep(int step, boolean done) {
        removeAllViews();
        Context c = getContext();
        int dot = getResources().getDimensionPixelSize(R.dimen.progress_dot);
        for (int i = 0; i < LABELS.length; i++) {
            LinearLayout col = new LinearLayout(c);
            col.setOrientation(VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            col.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            boolean reached = i < step || (i == step) || done;
            boolean current = i == step && !done;
            FrameLayout circle = new FrameLayout(c);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            if (reached) {
                bg.setColor(c.getColor(R.color.leia_primary));
            } else {
                bg.setColor(c.getColor(R.color.leia_surface));
                bg.setStroke(dp(2), c.getColor(R.color.leia_outline));
            }
            circle.setBackground(bg);
            if (i < step || done) {
                ImageView check = new ImageView(c);
                check.setImageResource(R.drawable.ic_check_small);
                check.setColorFilter(c.getColor(R.color.leia_on_primary));
                circle.addView(check, new FrameLayout.LayoutParams(dp(16), dp(16), Gravity.CENTER));
            } else if (current) {
                TextView n = new TextView(c);
                n.setText(String.valueOf(i + 1));
                n.setTextAppearance(R.style.TextAppearance_Leia_Label);
                n.setTextColor(c.getColor(R.color.leia_on_primary));
                circle.addView(n, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            }
            col.addView(circle, new LayoutParams(dot, dot));
            TextView label = new TextView(c);
            label.setText(LABELS[i]);
            label.setTextAppearance(current ? R.style.TextAppearance_Leia_Label : R.style.TextAppearance_Leia_Caption);
            label.setTextColor(c.getColor(reached ? R.color.leia_on_background : R.color.leia_on_surface_variant));
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            // Fonte do sistema em 130%: o rótulo encolhe em vez de quebrar no meio da palavra
            label.setMaxLines(1);
            label.setGravity(Gravity.CENTER);
            androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(label, 8, 12, 1,
                    TypedValue.COMPLEX_UNIT_SP);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(4);
            col.addView(label, lp);
            addView(col, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            if (i < LABELS.length - 1) {
                View line = new View(c);
                line.setBackgroundColor(c.getColor(i < step || done ? R.color.leia_primary : R.color.leia_outline_variant));
                LayoutParams llp = new LayoutParams(0, dp(2), 0.6f);
                llp.topMargin = dot / 2 - dp(1);
                addView(line, llp);
            }
        }
        int shown = done ? 4 : step + 1;
        setContentDescription(c.getString(R.string.steps_description, shown, c.getString(LABELS[Math.min(shown, 4) - 1])));
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
