# Catálogo de livros e disciplinas (RF-45)

- `topicos.py`: 90 tópicos, cada um com gênero, buscas na Open Library e o padrão (regex) que liga disciplinas da UFMS ao tópico. A lista vai do mais específico ao mais geral; cada disciplina fica com o primeiro tópico que casa.
- `gerar_catalogo.py`: busca os livros (Open Library, dados CC0, só em português), limpa (tira atas, autores institucionais, livros em inglês, registros antigos sem ISBN, acentos quebrados) e gera `app/src/main/assets/catalogo.json` com uma `versao`. O app recarrega o catálogo quando a versão muda.
- Disciplinas: `docs/referencias/ufms-disciplinas-graduacao-2026-1.csv` (Dados Abertos UFMS, CC BY).

```bash
python3 tools/catalogo/gerar_catalogo.py /caminho/para/cache
```

Limites conhecidos: a Open Library tem pouca coisa em português para algumas áreas (anatomia, fisiologia). Livros fora do catálogo continuam aceitos no app; quando entram, viram obras e também passam a aparecer nas sugestões.
