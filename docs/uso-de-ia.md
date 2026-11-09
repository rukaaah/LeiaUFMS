# Uso de IA no projeto

O grupo usou o Claude Code (Anthropic) como assistente. As decisões de produto e de design foram do grupo. O registro de cada etapa está em `docs/06-log-implementacao.md`.

## Onde a IA foi usada

| Área | Uso | Decisão do grupo |
|---|---|---|
| Logo | Conceitos gerados por script (`tools/logo/`) com a skill `/logo-design`, em 4 rodadas: v1 (A, B, C), v2 (linguagem da marca UFMS), v3 (livro e Paliteiro) e v4 (Paliteiro em 3D) | Direção final: o Paliteiro da UFMS sobre um livro aberto; palitos mais altos e respiro no ícone |
| Ícones | Conjunto próprio gerado por script (`tools/icons/`) com a skill `/icon-designer` (`docs/09-icones.md`) | Tirar textos onde o ícone basta |
| Design das telas (redesenho v2) | Skill `/design-md-library` para a estrutura (modelo Airbnb nas trocas, Pinterest no mural) e `/mobile-design-md-library` para as cores (Nova Mobile; meio-termo Playful no mural) | Cores da UFMS; jornada do aluno em `docs/08-jornada-e-usabilidade.md` |
| Auditorias de UI | Skill `/web-design-guidelines` no fim de cada parte, conferida no emulador em tema claro, escuro e fonte 130% (tabela "Auditorias de UI" do `docs/06`) | Correção de todos os itens reprovados |
| Acessibilidade | Conferência da leitura do TalkBack pela árvore de acessibilidade (`uiautomator dump`) | — |
| Dados abertos | Scripts para o catálogo de livros (Open Library, CC0) e a lista de cursos e disciplinas (Dados Abertos UFMS, CC BY) em `tools/` | Conferência das fontes |
| Documentação | Rascunho da especificação, do relatório de testes e do README | Revisão e cortes |

## Pull requests com participação da IA

| PR | Conteúdo |
|---|---|
| #1 | Relatório de testes caixa-preta reduzido a 24 casos |
| #2 | Especificação resumida para entrega e o PDF |
