"""Gera a lista de cursos de graduação da UFMS usada no cadastro e no perfil.

Fonte: Portal de Dados Abertos da UFMS, conjunto "Graduação - Cursos da UFMS", recurso 2026
(https://dadosabertos.ufms.br/dataset/graduacao), licença CC BY. Cópia em docs/referencias/ufms-cursos-graduacao-2026.csv.
Uso: python3 tools/cursos/gerar_cursos.py
"""
import csv
import os
import unicodedata

ROOT = os.path.join(os.path.dirname(__file__), '..', '..')
SRC = os.path.join(ROOT, 'docs', 'referencias', 'ufms-cursos-graduacao-2026.csv')
OUT = os.path.join(ROOT, 'app', 'src', 'main', 'res', 'values', 'cursos_ufms.xml')
MINOR = {'de', 'da', 'do', 'das', 'dos', 'e', 'em'}


def title(name):
    words = name.strip().lower().split()
    out = []
    for i, w in enumerate(words):
        prev = out[-1] if out else ''
        if i > 0 and w in MINOR and not prev.endswith(':'):
            out.append(w)
        else:
            out.append(w[:1].upper() + w[1:])
    return ' '.join(out)


rows = list(csv.DictReader(open(SRC, encoding='utf-8-sig')))
def key(n):  # ordem alfabética sem acento e sem diferença de maiúscula
    return unicodedata.normalize('NFD', n).encode('ascii', 'ignore').decode().lower()


names = sorted({title(r['Curso']) for r in rows if r['CursoSituacao'].strip() == 'Ativo'}, key=key)
with open(OUT, 'w', encoding='utf-8') as f:
    f.write('<?xml version="1.0" encoding="utf-8"?>\n')
    f.write('<!-- Cursos de graduação ativos da UFMS (Dados Abertos UFMS 2026, CC BY). '
            'Gerado por tools/cursos/gerar_cursos.py; não editar à mão. -->\n')
    f.write('<resources>\n    <string-array name="cursos_ufms" translatable="false">\n')
    for n in names:
        f.write(f'        <item>{n.replace("&", "&amp;")}</item>\n')
    f.write('    </string-array>\n</resources>\n')
print(len(names), 'cursos')
