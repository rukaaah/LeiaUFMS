# AGENT.md: Instruções para agentes no projeto LeiaUFMS

Leia este arquivo inteiro antes de qualquer ação. Ele vale para qualquer agente de IA (Claude Code, Copilot, Cursor etc.) e para humanos novos no projeto.

## 1. O que é o projeto

- **LeiaUFMS**: app Android nativo (Java) de troca de livros entre alunos da UFMS, só com e-mail institucional, sem dinheiro (troca direta ou resgate por crédito), com geolocalização como recurso central.
- É a **Avaliação 3 de Programação Mobile**. Entrega: **09/11/2026 até 23:59** no AVA, com o repositório/pasta compartilhado com `matheus.albuquerque@ufms.br`.
- O nome antigo era "Livro a Livro". **Não use mais esse nome** em código, strings, docs ou commits.

## 2. Lógica de leitura (ordem obrigatória)

Antes de planejar ou codar, leia nesta ordem. Não pule etapas: cada documento depende do anterior.

| # | Arquivo | Por quê |
|---|---|---|
| 1 | `docs/referencias/Avaliacao-3-prog-Mobile.pdf` | **Fonte da verdade** dos requisitos do professor. Ler na íntegra |
| 2 | `docs/01-pre-documentacao.md` | Análise do PDF, ADRs, pendências, riscos, prioridades e cronograma |
| 3 | `docs/02-especificacao.md` | Requisitos funcionais (RF), regras (RN), papéis: **o que** construir |
| 4 | `docs/04-arquitetura.md` | Stack, pacotes, entidades Room, máquina de estados: **como** construir |
| 5 | `docs/03-design-system.md` | Tokens, componentes, microcopy, checklist de auditoria de UI |
| 6 | `docs/05-relatorio-testes.md` | Casos de teste que validam cada RF |
| 7 | `docs/referencias/prompt-original.md` | Briefing original do grupo (contexto de domínio) |

**Hierarquia em caso de conflito:** PDF do professor > instrução explícita do grupo na conversa > `02-especificacao.md` > `04-arquitetura.md` > `03-design-system.md` > este arquivo. Se encontrar conflito, **pare e pergunte**; não escolha sozinho.

Para tarefas pequenas (ex.: corrigir um layout), basta ler este arquivo + o documento da área afetada + o RF citado.

## 3. Onde fica cada coisa

| Caminho | Conteúdo |
|---|---|
| `AGENT.md` | Este arquivo |
| `README.md` | Apresentação do projeto, como rodar, contas de demonstração |
| `docs/` | Toda a documentação (numerada na ordem de leitura) |
| `docs/referencias/` | PDF do enunciado e prompt original |
| `app/` | Módulo Android (ainda não criado) |
| `app/src/main/java/br/ufms/leiaufms/` | Código Java (estrutura em `docs/04-arquitetura.md` §2) |
| `app/src/main/res/values/` | `colors.xml`, `themes.xml`, `dimens.xml`, `strings.xml` (tokens do design system) |
| `.github/` | Templates e automações do GitHub (opcional) |

## 4. Proibições absolutas

1. **Nunca** armazenar senha em texto puro, Base64, criptografia reversível ou hash sem salt. Só BCrypt (`at.favre.lib:bcrypt`, custo 12).
2. **Nunca** logar senha, hash, token de sessão ou e-mail completo.
3. **Nunca** usar atraso artificial na splash (`Thread.sleep`, `postDelayed`, timer).
4. **Nunca** acessar Room ou disco na main thread.
5. **Nunca** permitir acesso a tela interna sem sessão válida.
6. **Nunca** abrir chat fora de um pedido `ACEITO` nem criar chat aberto entre desconhecidos.
7. **Nunca** criar seguidores, curtidas em massa, ranking ou feed infinito.
8. **Nunca** inventar dados: coordenadas de pontos do campus, versões de bibliotecas, nomes de integrantes ou **resultados de testes**. Se faltar, deixe `[PENDENTE]` e avise.
9. **Nunca** usar Kotlin, Compose ou template diferente de Empty/Basic Views Activity.
10. **Nunca** reutilizar tema ou código das Avaliações 1 e 2.

## 5. Regras de implementação

- **Linguagem:** Java. Views XML + Material Components (Material 3).
- **Arquitetura:** MVVM (`Fragment` → `ViewModel` → `Repository` → `DAO`). Detalhes em `docs/04-arquitetura.md`.
- **Regras de negócio** (prazos, raios, limites, domínio de e-mail `@ufms.br`) ficam **só** em `AppConfig.java`. Valores atuais nas pendências PC-01 a PC-12 do `01-pre-documentacao.md`.
- **Créditos:** livro-razão em `movimento_credito`; nunca um campo "saldo" editável. Crédito **não** bloqueia pedidos (RN-CRED-04) e o saldo nunca fica negativo (RN-CRED-05).
- **Pedidos:** só transições da tabela em `04-arquitetura.md` §5, sempre em transação Room.
- **Permissões:** pedir no momento do uso, tratar concessão, negação e negação permanente (`shouldShowRequestPermissionRationale`).
- **Strings:** tudo em `strings.xml` em pt-BR, usando a microcopy do design system §7.
- **IDs de rastreabilidade:** cite o RF/RN no commit e no comentário da classe principal (ex.: `// RF-19, RN-CRED-01`).
- **Dependências:** usar a versão estável mais recente no momento do setup, conferida na fonte oficial (Maven Central/Google Maven/GitHub releases). Registrar em `gradle/libs.versions.toml`.

## 6. Design e uso de skills

O design é definido **exclusivamente** por três skills, nesta ordem:

| Ordem | Skill | Quando | Resultado |
|---|---|---|---|
| D1 | `/design-md-library` | Uma vez, antes de qualquer tela | Princípios, paleta (claro e escuro, contraste ≥ 4,5:1), tipografia, espaçamento e ícone, que reescrevem `03-design-system.md` §1 a §5 |
| D2 | `/design-taste-frontend` | Antes de codar cada tela | Briefing da tela: objetivo, ações, hierarquia, estados e microcopy, registrado no §6 |
| D3 | `/web-design-guidelines` | Fim de cada parte | Auditoria das telas + checklist §9; correções commitadas |

- **Proibido** usar outras skills de design como substitutas. Se alguma das três não estiver disponível, pare e peça ao Pedro para instalá-la.
- Os valores visuais escritos em `03-design-system.md` antes da D1 são **provisórios**.
- O passo a passo detalhado, com exemplos, está em `docs/referencias/prompt-implementacao.md`, na seção "Design, usabilidade e uso de skills".

**Regras de design invioláveis:** só tokens do design system; contraste ≥ 4,5:1; alvo de toque ≥ 48 dp; uma ação principal por tela; erro por campo com `TextInputLayout.setError`, nunca só Toast; tema claro e escuro.

## 7. Como manter a documentação

| Mudou... | Atualize |
|---|---|
| Uma regra de negócio | `AppConfig.java` + tabela PC em `01-pre-documentacao.md` + RN em `02-especificacao.md` |
| Um requisito | `02-especificacao.md` + caso de teste em `05-relatorio-testes.md` |
| Entidade ou fluxo | `04-arquitetura.md` |
| Cor, fonte, componente, mensagem | `03-design-system.md` |
| Uma pendência foi resolvida | Marque-a como resolvida (com data) em `01-pre-documentacao.md` |
| Estado do projeto | Seção 9 deste arquivo |

- Documentação em português, Markdown, tabelas para dados e comparações.
- Não apague histórico de decisão: marque como "Substituído por ADR-XX".
- **Relatório de testes:** preencha "Resultado obtido" e "Status" **somente** após executar o teste no app. Reprovou? Registre na seção 6 (Falhas) e, ao corrigir, reexecute e anote o commit.

## 8. Definição de pronto (por funcionalidade)

- [ ] RF implementado conforme `02-especificacao.md`
- [ ] Sem crash com entrada inválida, permissão negada, sem GPS e sem internet
- [ ] Checklist de UI (`03-design-system.md` §9) aprovado
- [ ] Casos de teste correspondentes executados e registrados
- [ ] Documentação atualizada (seção 7)
- [ ] Nome "LeiaUFMS" em tudo; nenhum "Livro a Livro"

## 9. Estado atual

| Item | Situação |
|---|---|
| Documentação | Completa (02/11/2026) |
| Projeto Android | **Não criado** |
| Integrantes | Pedro Lucas de Souza Cremonini, Gabriel Mattos de Aquino, Angelo Antonio Correia de Souza |
| Repositório | https://github.com/rukaaah/LeiaUFMS (branch `main`) |
| Pendências abertas | P-01, P-04 a P-09, PC-01 a PC-12 (com padrão adotado) |
| Testes executados | 0 de 59 |
| Próximo passo | Criar o projeto Android (Empty Views Activity, Java, `br.ufms.leiaufms`) e implementar Room + auth + splash (cronograma em `01-pre-documentacao.md` §7) |

## 10. Antes de entregar (09/11)

Siga o checklist em `docs/01-pre-documentacao.md` §8. Exporte `02-especificacao.md` + `05-relatorio-testes.md` em PDF com os nomes completos dos integrantes.
