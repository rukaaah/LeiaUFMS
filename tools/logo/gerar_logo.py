"""Gera o logo LeiaUFMS (Paliteiro da UFMS em 3D sobre um livro aberto) para o Android e para docs/logo.

Uso: python3 tools/logo/gerar_logo.py
Regras e decisões em docs/logo/README.md.
"""
import os, sys
sys.path.insert(0, os.path.dirname(__file__))
from iso3 import scene, render

ROOT = os.path.join(os.path.dirname(__file__), '..', '..')
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res', 'drawable')
DOCS = os.path.join(ROOT, 'docs', 'logo')
ITEMS = scene(h=24, lift=3.0, thick=1.6, W=10.5, D=6.5)  # palitos altos como o monumento


def vector(items, fg, sep, comment, size=108, box=70, sepw=1.3):
    xs = [p[0] for _, pts in items for p in pts]
    ys = [p[1] for _, pts in items for p in pts]
    w = max(xs) - min(xs); h = max(ys) - min(ys); s = box / max(w, h)
    ox = size / 2 - (min(xs) + w / 2) * s; oy = size / 2 - (min(ys) + h / 2) * s
    out = ['<?xml version="1.0" encoding="utf-8"?>', f'<!-- {comment} -->',
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
           f'    android:width="{size}dp"', f'    android:height="{size}dp"',
           f'    android:viewportWidth="{size}"', f'    android:viewportHeight="{size}">']
    for op, pts in items:
        d = "M" + " L".join(f"{ox + a * s:.2f},{oy + b * s:.2f}" for a, b in pts) + "Z"
        if op == 'sep':
            out.append(f'    <path android:fillColor="{sep}" android:strokeColor="{sep}" android:strokeWidth="{sepw}" '
                       f'android:strokeLineJoin="round" android:pathData="{d}" />')
        else:
            out.append(f'    <path android:fillColor="{fg}" android:fillAlpha="{op}" android:pathData="{d}" />')
    out.append('</vector>\n')
    return "\n".join(out)


def flat_paths():
    """Versão plana de uma cor (grade 24): livro aberto + 7 palitos altos de mesma altura."""
    def rect(x, y, w, h): return f"M{x:.2f},{y:.2f}h{w:.2f}v{h:.2f}h{-w:.2f}Z"
    book = ["M2,17C5.2,15.9 8.8,15.9 11.3,17.3V22.6C8.8,21.4 5.2,21.2 2,21.9Z",
            "M22,17C18.8,15.9 15.2,15.9 12.7,17.3V22.6C15.2,21.4 18.8,21.2 22,21.9Z"]
    n, w, g = 7, 1.4, 0.9
    x0 = 12 - (n * w + (n - 1) * g) / 2
    return book + [rect(x0 + i * (w + g), 1.2, w, 14.1) for i in range(n)]


def flat_vector(comment, size, color, group=''):
    o = ['<?xml version="1.0" encoding="utf-8"?>', f'<!-- {comment} -->',
         '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
         f'    android:width="{size}dp"', f'    android:height="{size}dp"',
         f'    android:viewportWidth="{size}"', f'    android:viewportHeight="{size}">']
    if group: o.append(group)
    ind = '        ' if group else '    '
    o += [f'{ind}<path android:fillColor="{color}" android:pathData="{d}" />' for d in flat_paths()]
    if group: o.append('    </group>')
    o.append('</vector>\n')
    return "\n".join(o)


if __name__ == '__main__':
    open(os.path.join(RES, 'ic_notification.xml'), 'w').write(flat_vector(
        'Ícone de notificação: versão plana do logo (livro aberto + Paliteiro de topo reto), uma cor.', 24,
        '@android:color/white'))
    open(os.path.join(RES, 'ic_launcher_monochrome.xml'), 'w').write(flat_vector(
        'Camada monocromática do ícone (Android 13+): versão plana do logo.', 108, '#FFFFFFFF',
        '    <group android:translateX="24" android:translateY="24" android:scaleX="2.5" android:scaleY="2.5">'))
    open(os.path.join(DOCS, 'leiaufms-plano.svg'), 'w').write(
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="256" height="256"><title>LeiaUFMS plano</title>'
        + ''.join(f'<path fill="#000" d="{d}"/>' for d in flat_paths()) + '</svg>')
    open(os.path.join(RES, 'ic_launcher_foreground.xml'), 'w').write(vector(
        ITEMS, '@color/leia_icon_page', '@color/leia_icon_background',
        'Logo LeiaUFMS: o Paliteiro da UFMS em 3D (colunas em 4 eixos, topo reto) de pé sobre um livro aberto. '
        'Gerado por tools/logo/gerar_logo.py', box=60))  # dentro da zona segura de 66 dp, com respiro
    open(os.path.join(RES, 'ic_splash.xml'), 'w').write(vector(
        ITEMS, '@color/leia_primary', '@color/leia_background',
        'Logo LeiaUFMS na splash e no login: azul sobre o fundo do tema. Gerado por tools/logo/gerar_logo.py', box=72))
    os.makedirs(DOCS, exist_ok=True)
    open(os.path.join(DOCS, 'leiaufms-icone.svg'), 'w').write(render(ITEMS, pad=0.13))
    open(os.path.join(DOCS, 'leiaufms-simbolo-azul.svg'), 'w').write(
        render(ITEMS, pad=0.06, fg='#1B4F8F', bg='#FFFFFF', tile=False))
    open(os.path.join(DOCS, 'leiaufms-simbolo-branco.svg'), 'w').write(
        render(ITEMS, pad=0.06, fg='#FFFFFF', bg='#1B4F8F', tile=True, rx=0))
    print('ok')
