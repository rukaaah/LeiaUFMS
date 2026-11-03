"""Gera app/src/main/assets/catalogo.json: livros (Open Library, CC0), tópicos e disciplinas reais da UFMS.

Fontes:
- Disciplinas: Dados Abertos UFMS, "Graduação - Disciplinas das Graduações da UFMS", 1/2026 (CC BY),
  cópia em docs/referencias/ufms-disciplinas-graduacao-2026-1.csv.
- Livros: busca da Open Library (dados CC0) em português, por tópico (tools/catalogo/topicos.py).
Uso: python3 tools/catalogo/gerar_catalogo.py [pasta-de-cache]
"""
import csv, json, os, re, sys, time, unicodedata, urllib.parse, urllib.request

sys.path.insert(0, os.path.dirname(__file__))
from topicos import TOPICOS

ROOT = os.path.join(os.path.dirname(__file__), '..', '..')
DISC = os.path.join(ROOT, 'docs', 'referencias', 'ufms-disciplinas-graduacao-2026-1.csv')
OUT = os.path.join(ROOT, 'app', 'src', 'main', 'assets', 'catalogo.json')
CACHE = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, 'build', 'catalogo-cache')
MINOR = {'de', 'da', 'do', 'das', 'dos', 'e', 'em', 'a', 'o', 'as', 'os', 'para', 'na', 'no', 'com', 'ao', 'aos', 'à', 'às'}
ROMAN = {'i', 'ii', 'iii', 'iv', 'v', 'vi', 'vii', 'viii', 'ix', 'x'}


def plain(s):
    return unicodedata.normalize('NFD', s).encode('ascii', 'ignore').decode().upper()


def title(s):
    out = []
    for i, w in enumerate(s.strip().lower().split()):
        if w in ROMAN: out.append(w.upper())
        elif i > 0 and w in MINOR and not out[-1].endswith(':'): out.append(w)
        else: out.append(w[:1].upper() + w[1:])
    return ' '.join(out)


def clean_title(t):
    t = unicodedata.normalize('NFC', t)
    t = re.sub(r'\s+([\u0300-\u036f])', r'\1', t)  # acento solto da Open Library ("Lo gica")
    t = unicodedata.normalize('NFC', t)
    t = re.sub(r'\s*\((em )?portugu[eê]se?( do brasil)?\)\s*', ' ', t, flags=re.I)
    t = re.sub(r'\s*[-–]\s*$', '', t).strip()
    t = re.sub(r'\s+', ' ', t)
    return t[:1].upper() + t[1:]


def clean_author(a):
    a = unicodedata.normalize('NFC', a).strip().rstrip('.').strip()
    if ',' in a:  # "Sobrenome, Nome" -> "Nome Sobrenome"
        last, first = [p.strip() for p in a.split(',', 1)]
        if first and len(first.split()) <= 3: a = f'{first} {last}'
    return a


def fetch(q):
    os.makedirs(CACHE, exist_ok=True)
    path = os.path.join(CACHE, re.sub(r'\W+', '_', q) + '.json')
    if os.path.exists(path): return json.load(open(path))
    url = 'https://openlibrary.org/search.json?' + urllib.parse.urlencode({
        'q': q, 'language': 'por', 'limit': 40,
        'fields': 'key,title,author_name,isbn,first_publish_year,cover_i'})
    req = urllib.request.Request(url, headers={'User-Agent': 'LeiaUFMS/1.0 (projeto academico UFMS)'})
    for attempt in range(3):
        try:
            data = json.load(urllib.request.urlopen(req, timeout=30))
            json.dump(data, open(path, 'w'))
            time.sleep(0.6)
            return data
        except Exception as e:  # noqa: BLE001
            print('  falhou', q, e); time.sleep(3)
    return {'docs': []}


INSTITUCIONAL = re.compile(r'[\(\)\d]|SEMINARIO|CONGRES|SIMPOSIO|ENCONTRO|ESCOLA|SERVICE|S\. ?A\b|BRAZIL|BRASIL\b|UNIVERSIDADE|'
                           r'INSTITUTO|MINISTERIO|DEPARTAMENTO|CONFERENCE|ASSOCIA|SOCIEDADE|FUNDACAO|EDITORA|SECRETARIA|EMBRAPA', re.I)
RUIDO_TITULO = re.compile(r'ANAIS|ACTAS|SEMINARIO|CONGRESSO|SIMPOSIO|RELATORIO|BOLETIM|CATALOGO|MOCAMBIQUE|ANGOLA|PROCEEDINGS', re.I)
INGLES = re.compile(r'\b(the|of|and|for|with|to|in|your|how)\b', re.I)


def bom(t, a, ano, isbn):
    """Corta atas, instituições como autor, livros em inglês e registros antigos sem ISBN."""
    if INSTITUCIONAL.search(plain(a)) or RUIDO_TITULO.search(plain(t)): return False
    if len(INGLES.findall(t)) >= 2 and not re.search(r'[ãõçáéíóúâêô]', t.lower()): return False
    if not isbn and (not ano or ano < 1985): return False
    return True


def isbn13(isbns):
    for i in isbns or []:
        if len(i) == 13 and i.startswith(('978', '979')): return i
    return None


books, index = [], {}
for ti, (nome, genero, queries, _) in enumerate(TOPICOS):
    n = 0
    for q in queries:
        for d in fetch(q).get('docs', []):
            if not d.get('title') or not d.get('author_name'): continue
            t = clean_title(d['title']); a = clean_author(d['author_name'][0])
            if len(t) < 2 or len(t) > 110 or len(a) < 3: continue
            if not bom(t, a, d.get('first_publish_year'), isbn13(d.get('isbn'))): continue
            if re.search('[\u02b0-\u02ff\u0300-\u036f]', t + a): continue  # acentos quebrados na origem
            k = (plain(t), plain(a))
            if k in index:
                b = books[index[k]]
                if ti not in b['tp']: b['tp'].append(ti)
                continue
            index[k] = len(books); n += 1
            books.append({'t': t, 'a': a, 'i': isbn13(d.get('isbn')), 'y': d.get('first_publish_year'),
                          'c': d.get('cover_i'), 'g': genero, 'tp': [ti]})
    print(f'{nome}: {n} livros')

rows = list(csv.DictReader(open(DISC, encoding='utf-8-sig')))
disc, seen = [], set()
for r in rows:
    nome = r['nome_disciplina'].strip(); curso = title(r['nome_curso'].strip())
    key = (plain(nome), curso)
    if key in seen: continue
    seen.add(key)
    # Só o primeiro tópico que casa (a lista vai do mais específico ao mais geral): "Arquitetura de
    # Computadores" fica em Computação e não cai em Arquitetura e urbanismo. "Tópicos ..." genéricos ficam de fora.
    p = plain(nome)
    tps = [] if p.startswith('TOPICOS') else [i for i, (_, _, _, rx) in enumerate(TOPICOS) if re.search(rx, p)][:1]
    disc.append({'c': r['codigo_disciplina'].strip(), 'n': title(nome), 'cu': curso, 'tp': tps})
os.makedirs(os.path.dirname(OUT), exist_ok=True)
import hashlib
versao = hashlib.sha1(json.dumps([books, disc], ensure_ascii=False).encode()).hexdigest()[:12]
json.dump({'versao': versao, 'fonte': 'Livros: Open Library (CC0). Disciplinas: Dados Abertos UFMS 2026-1 (CC BY).',
           'topicos': [{'n': n, 'g': g} for n, g, _, _ in TOPICOS], 'livros': books, 'disciplinas': disc},
          open(OUT, 'w', encoding='utf-8'), ensure_ascii=False, separators=(',', ':'))
ligadas = sum(1 for d in disc if d['tp'])
print(f'{len(books)} livros, {len(disc)} disciplinas ({ligadas} ligadas a tópicos), {os.path.getsize(OUT)//1024} KB')
