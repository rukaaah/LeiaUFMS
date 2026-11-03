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
ITEMS = scene(h=14, lift=3.0, thick=1.6, W=10.5, D=6.5)


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


if __name__ == '__main__':
    open(os.path.join(RES, 'ic_launcher_foreground.xml'), 'w').write(vector(
        ITEMS, '@color/leia_icon_page', '@color/leia_icon_background',
        'Logo LeiaUFMS: o Paliteiro da UFMS em 3D (colunas em 4 eixos, topo reto) de pé sobre um livro aberto. '
        'Gerado por tools/logo/gerar_logo.py', box=76))
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
