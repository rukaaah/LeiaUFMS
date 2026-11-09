# Uso de IA no projeto

O grupo usou o Claude Code (Anthropic) como assistente. As decisões de produto e de design foram do grupo. O registro de cada etapa está em `docs/06-log-implementacao.md`.

## Onde a IA foi usada

| Área | Uso da IA | Decisão do grupo |
|---|---|---|
| Logo | Conceitos gerados por script (`tools/logo/`) com a skill `/logo-design`, em 4 rodadas: v1 (A, B, C), v2 (linguagem da marca UFMS), v3 (livro e Paliteiro) e v4 (Paliteiro em 3D) | Estilo do logo, clima visual e estudo de usabilidade da marca. Direção final: o Paliteiro da UFMS sobre um livro aberto, com palitos mais altos e respiro no ícone |
| Ícones | Conjunto próprio gerado por script (`tools/icons/`) com a skill `/icon-designer` (`docs/09-icones.md`) | Preferência por ícones para a informação ser entendida rápido e a interface ficar menos cheia; texto só onde o ícone não basta |
| Design das telas (redesenho v2) | Skill `/design-md-library` para a estrutura (modelo Airbnb nas trocas, Pinterest no mural) e `/mobile-design-md-library` para as cores (Nova Mobile; meio-termo Playful no mural) | O caminho do aluno no app, como cada tela seria feita, o que define a usabilidade do projeto e como desenhar as telas, nas cores da UFMS (jornada em `docs/08-jornada-e-usabilidade.md`) |
| Auditorias de UI | Skill `/web-design-guidelines` no fim de cada parte, conferida no emulador em tema claro, escuro e fonte 130% (tabela "Auditorias de UI" do `docs/06`) | Correção de todos os itens reprovados |
| Acessibilidade | Só a conferência da leitura do TalkBack pela árvore de acessibilidade (`uiautomator dump`) | Atender o básico: `contentDescription`, contraste, tamanho de toque e de fonte |
| Dados abertos | Formatação e preparação dos dados por script (`tools/`) | Escolha das fontes: catálogo de livros da Open Library (CC0) e cursos e disciplinas dos Dados Abertos UFMS (CC BY) |
| Documentação | Rascunho; junção das informações do projeto em Markdown para a escrita da especificação; formatação; apoio para não esquecer nenhum requisito do enunciado | Confecção geral da documentação e revisão |

## Pull requests com participação da IA

| PR | Conteúdo |
|---|---|
| #1 | Relatório de testes caixa-preta reduzido a 24 casos |
| #2 | Especificação resumida para entrega e o PDF |
