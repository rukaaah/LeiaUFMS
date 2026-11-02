# Prompt de implementação do LeiaUFMS (5 partes)

> Cole o bloco abaixo inteiro em uma sessão de agente com acesso à pasta `~/Desktop/LeiaUFMS` no Mac (Remote Control / Claude Code local), com Android Studio instalado.

---

## Papel

Você é um engenheiro Android sênior. Vai implementar o app **LeiaUFMS** (Java nativo, Views XML, Room) de ponta a ponta, em **5 partes sequenciais**, com build verde e commit entre elas. Responda e escreva commits em português.

## Ponto de restauração (antes de tudo)

- Commit base da documentação: **`357c50e`** (branch `main`, `github.com/rukaaah/LeiaUFMS`).
- Antes de começar, rode `git status` (precisa estar limpo) e `git rev-parse --short HEAD`. Se o HEAD não for `357c50e`, anote o valor real como base.
- Crie `docs/06-log-implementacao.md` com a tabela abaixo e mantenha-a atualizada a cada parte:

| Parte | Commit inicial | Commit final | Build | Verificação | Observações |
|---|---|---|---|---|---|
| Base | 357c50e | 357c50e | — | — | Documentação |

- **Para voltar atrás em uma parte que deu errado:** `git reset --hard <commit inicial da parte>` (só localmente, antes de dar push). Nunca use `push --force` sem o Pedro autorizar explicitamente na conversa.

## Leitura obrigatória antes de codar

Siga a **ordem de leitura do `AGENT.md` §2** e leia todos os arquivos indicados ali. O `AGENT.md` é a lei do projeto. Proibições (§4), regras de implementação (§5), design (§6) e definição de pronto (§8) valem em todas as partes.

## Decisões já tomadas pelo grupo (aplique e registre nos docs na Parte 1)

| Pendência | Decisão |
|---|---|
| P-01 | Tema não repete Avaliação 1/2. Marcar como resolvida |
| P-03 | E-mail de aluno termina em `@ufms.br` (já resolvida) |
| P-04 | `applicationId` e pacote: `br.ufms.leiaufms` |
| P-05 | O agente cria o ícone na Etapa D1 com `/design-md-library` (adaptive icon vetorial), declarado no `AndroidManifest.xml` (`android:icon` e `android:roundIcon`) |
| P-06 / PC-07 | **Nenhuma coordenada vem no seed.** Os pontos seguros são criados **dentro do app** pelo Moderador, tocando no mapa (RF-30). Anúncios do seed usam a localização escolhida no próprio app; enquanto não houver pontos, a tela "Marcar encontro" mostra um estado vazio explicando que o Moderador precisa cadastrar pontos |
| Seed | Autorizado. Criar contas, obras, disciplinas (nomes genéricos, sem códigos UFMS inventados), anúncios e pedidos de exemplo conforme `docs/04-arquitetura.md` §7, **sem coordenadas inventadas** |
| Testes caixa-preta | Executados pelo grupo. **Não preencha "Resultado obtido" nem "Status"** no `05-relatorio-testes.md` |
| Commits | Mensagens em conventional commits (`feat:`, `fix:`, `docs:`, `chore:`), em português, **sem linhas `Co-Authored-By` ou de sessão** |

## Regras de execução (valem para todas as partes)

1. **Ciclo por funcionalidade:** implementar → `./gradlew assembleDebug` → `./gradlew testDebugUnitTest` (quando houver teste) → corrigir → commit. Um commit por funcionalidade coerente, nunca um commit gigante por parte.
2. **Build verde é obrigatório antes de cada commit.** Se o build quebrar e você não souber resolver em 3 tentativas, pare, faça `git stash` ou `git reset --hard` para o último commit verde e relate o problema.
3. **Integridade entre partes:** ao iniciar a parte N, rode o build e os testes unitários das partes anteriores. Se algo regrediu, conserte antes de avançar.
4. **Testes unitários (JUnit) mínimos:** `Validators`, `PasswordHasher` (hash ≠ senha, verificação correta e incorreta, dois hashes da mesma senha diferentes), máquina de estados do pedido, cálculo de saldo/reserva de crédito, cálculo de distância/"a pé".
5. **Verificação funcional:** ao fim de cada parte, instale no emulador/aparelho (`./gradlew installDebug`) e confira manualmente o fluxo principal da parte. Se não houver emulador disponível, diga isso no log em vez de afirmar que funciona.
6. **Versões de dependências:** use a versão estável mais recente, conferida na fonte oficial; registre em `gradle/libs.versions.toml`. Não invente números de versão.
7. **Docs vivos:** qualquer mudança em regra, entidade ou tela atualiza o doc correspondente (`AGENT.md` §7) no mesmo commit ou no commit seguinte.
8. **Ao fim de cada parte:** atualize `docs/06-log-implementacao.md`, `docs/07-handoff.md` e `AGENT.md` §9 (Estado atual), faça o commit `docs: log da parte N`, faça `git push origin main` e reporte: commits criados, commit inicial/final, o que foi verificado e o que ficou pendente.
9. **Nunca** prossiga para a próxima parte com build vermelho.

## Design, usabilidade e uso de skills

Usabilidade é o **foco principal avaliado** (`02-especificacao.md` §6). O design é definido **pelas skills pedidas pelo grupo**, não por este prompt. Os valores visuais que estão hoje no `docs/03-design-system.md` (cores, fontes, princípios) são **provisórios** e serão substituídos pelo resultado das skills.

### Skills permitidas (somente estas três)

| Skill | Papel |
|---|---|
| `/design-md-library` | Estilo visual: princípios, paleta, tipografia, forma, ícone |
| `/design-taste-frontend` | Briefing de usabilidade de cada tela |
| `/web-design-guidelines` | Auditoria das telas prontas |

- **Proibido** usar qualquer outra skill de design como substituta (`design-tokens`, `design-brief`, `design-review`, `frontend-design` etc.).
- Antes da Parte 1, confirme que as três estão disponíveis. **Se alguma não estiver, pare e peça ao Pedro para instalá-la.** Não continue sem ela e não improvise o design manualmente.
- As skills podem ter sido feitas pensando em web. Traduza a saída para Android (XML, `dp`/`sp`, Material Components 3, `colors.xml`/`themes.xml`).

### Ordem obrigatória de uso

**Etapa D1: Direção visual com `/design-md-library` (antes de qualquer layout, no início da Parte 1).**

Peça à skill, nesta ordem:

1. **Princípios de design**, em tabela "Princípio | Na prática", com exemplos concretos do LeiaUFMS. O briefing do grupo é: *"a tranquilidade de um dia de leitura", minimalista, mobile-first, camada social sem toxicidade*. Exemplos do nível de concretude esperado (a skill decide os valores reais):
   - *Calma*: "fundo X, no máximo N cores de destaque, animações só de transição com até 200 ms".
   - *Clareza*: "uma ação principal por tela, sempre como botão preenchido na base da tela".
   - *Confiança*: "distância, nota média e ponto seguro visíveis no card antes de pedir o livro".
   - *Sem ruído social*: "reações mostradas como texto discreto, nunca contador grande ou badge vermelho".
2. **Paleta** para tema claro e escuro (primary, on-primary, background, surface, texto principal e secundário, erro e uma cor de destaque para créditos e "quero"). **Calcule e registre o contraste** de cada par texto/fundo: todos ≥ 4,5:1 (WCAG AA).
3. **Tipografia:** fonte de títulos de livro e fonte de corpo (licença livre), com escala em `sp`.
4. **Espaçamento, raios e elevação**, num grid de 4 dp, com alvo de toque ≥ 48 dp.
5. **Ícone do app:** conceito e cores. Gere o adaptive icon vetorial.

Saída: **reescrever `docs/03-design-system.md` §1 a §5** com o resultado, gerar `colors.xml`, `themes.xml` (DayNight), `dimens.xml` e a tipografia, e fazer commit (`docs: design system definido via design-md-library`) **antes** de criar telas.

**Etapa D2: Briefing de cada tela com `/design-taste-frontend` (antes de codar cada tela, em todas as partes).**

Para cada tela do mapa de telas (`03-design-system.md` §6), peça:

1. O objetivo do usuário naquela tela, em uma frase.
2. A ação principal e as secundárias, em ordem de importância.
3. A hierarquia visual, de cima para baixo.
4. Os estados vazio, carregando, erro e sem permissão, com o texto de cada um.
5. A microcopy dos erros, revisando a tabela §7.

Exemplo real para a tela **Detalhe do anúncio**: *objetivo: decidir se vale pedir este livro. Ação principal: "Pedir troca" ou "Resgatar". Hierarquia: capa → título/autor → estado de conservação → distância + "dá para ir a pé" → cartão do dono (nota, trocas) → histórico do exemplar. Vazio: não se aplica. Sem localização: esconder a distância e mostrar "Ative a localização para ver a distância".*

Registre o briefing resumido de cada tela em `03-design-system.md` §6 (uma linha ou subseção por tela).

**Etapa D3: Auditoria com `/web-design-guidelines` (fim de cada parte, antes do commit `docs: log da parte N`).**

1. Rodar a skill sobre todas as telas criadas na parte.
2. Somar à saída o checklist do `03-design-system.md` §9.
3. Corrigir tudo que reprovar, rodar o build e fazer commit (`fix: ajustes da auditoria de UI da parte N`).
4. Registrar no `06-log-implementacao.md` os itens reprovados e os corrigidos.

Na Parte 5, faça uma auditoria completa: todas as telas, tema escuro, fonte do sistema em 130% e TalkBack.

### Regras invioláveis de UI (independem das skills)

- Nenhuma cor, tamanho ou texto fixo no layout: só tokens e `strings.xml`.
- Contraste ≥ 4,5:1; alvo de toque ≥ 48 dp; texto em `sp`.
- Erro por campo com `TextInputLayout.setError`, nunca só um Toast.
- Toda lista tem estado vazio que ensina o próximo passo.
- Confirmação antes de ação destrutiva (sair, cancelar pedido, abrir disputa).
- Nada de feed infinito, seguidores, curtidas em massa, ranking ou badge vermelho chamativo.
- `contentDescription` em todo ícone e imagem com significado.
- Funciona em tema claro e escuro.

## Preparação para fim de contexto, tokens ou cota

A sessão pode acabar a qualquer momento: contexto cheio, limite de uso ou queda de conexão. Trate isso como uma condição normal. **O estado do trabalho vive no repositório, nunca só na conversa.**

1. **Commits pequenos e frequentes.** Nunca fique mais de uma funcionalidade sem commit. Trabalho não commitado é trabalho perdido.
2. **Push a cada commit verde** (`git push origin main`), não só no fim da parte. Se a máquina ou a sessão cair, o GitHub tem tudo.
3. **Arquivo de retomada: `docs/07-handoff.md`.** Atualize-o **antes** de cada commit, com este formato fixo:

   ```markdown
   # Handoff
   - Atualizado em: <data/hora>
   - Parte atual: N (<nome>)
   - Último commit verde: <hash curto>
   - Feito nesta parte: <lista curta de RFs concluídos>
   - Em andamento: <arquivo/classe e o que falta>
   - Próximo passo exato: <uma ação concreta, ex.: "criar PedidoDao.recusarOutros()">
   - Problemas conhecidos: <erros de build, decisões em aberto>
   ```

4. **Sinal de alerta:** quando perceber que o contexto está ficando longo (muitos arquivos lidos, respostas lentas ou um aviso de compactação), pare de abrir frentes novas. Termine a funcionalidade atual ou reverta para o último commit verde, atualize o handoff, faça commit e push, e só então continue.
5. **Nunca deixe o build vermelho no `main`.** Se precisar parar no meio de algo quebrado, use `git stash` ou uma branch `wip/<parte>` e registre isso no handoff.
6. **Protocolo de retomada (nova sessão):**
   1. Ler `AGENT.md`.
   2. Ler `docs/07-handoff.md` e `docs/06-log-implementacao.md`.
   3. Rodar `git status`, `git log --oneline -5` e `./gradlew assembleDebug`.
   4. Conferir se o HEAD bate com o "Último commit verde" do handoff. Se não bater, investigar antes de mexer em qualquer coisa.
   5. Continuar exatamente do "Próximo passo exato". **Não reler todos os docs** se o handoff já disser onde parar. Ler só o RF e a seção de arquitetura da tarefa atual, para poupar contexto.
7. **Economia de contexto:** leia arquivos por trechos (offset/limit, grep) em vez de inteiros quando já conhecer a estrutura; não cole logs de build inteiros, só as linhas de erro; não releia arquivos que você acabou de escrever.

Frase para retomar em uma conversa nova: *"Continue o LeiaUFMS seguindo o protocolo de retomada do `docs/referencias/prompt-implementacao.md`."*

## As 5 partes

### Parte 1: Fundação, Room, autenticação e splash

Escopo: RF-01 a RF-05, RN-AUTH-01 a 05, ADR-02, ADR-04.

- Criar o projeto Android (Empty Views Activity, Java, `br.ufms.leiaufms`, minSdk 26) **dentro da pasta atual**, sem apagar docs.
- **Antes de qualquer tela:** Etapa D1 (`/design-md-library`): definir o design system, gerar os recursos de tema e o ícone, e fazer commit.
- Configurar Gradle (version catalog), Material 3, os recursos de tema gerados na D1 e `strings.xml` pt-BR.
- Briefing D2 (`/design-taste-frontend`) para Splash, Login, Cadastro e a estrutura das 4 abas.
- `AppConfig`, `Validators`, `PasswordHasher` (BCrypt custo 12, `char[]`), `SessionManager` (token + hash SHA-256 no Room, 30 dias).
- `AppDatabase` com **todas** as entidades do `04-arquitetura.md` §4 (mesmo as usadas depois), DAOs básicos e `SeedData` (sem coordenadas).
- Splash com `core-splashscreen` e `setKeepOnScreenCondition`, `AuthActivity` (login/cadastro), `MainActivity` com `BottomNavigationView` (4 abas, placeholders) e guarda de sessão, logout com confirmação.
- Atualizar pendências P-01, P-04, P-05, P-06 e PC-07 nos docs.
- Verificar: cadastro, login inválido, sessão persistente ao reabrir, logout + Voltar.

### Parte 2: Perfil, mídia, permissões e notificações

Escopo: RF-06 a RF-10, RF-31 a RF-34 (infraestrutura), ADR-04.

- Tela Perfil (cartão de confiança), Editar perfil, Alterar senha (novo hash).
- Foto via câmera (`TakePicture` + `FileProvider`) e galeria (`PickVisualMedia`), cópia para `filesDir`, caminho no Room.
- `PermissionHelper` com concessão, negação e negação permanente (atalho para Configurações).
- `Notifier`: canais `trocas`, `desejos`, `pendencias`; `POST_NOTIFICATIONS` no API 33+; fallback para a tabela `notificacao` e a Caixa de avisos; entrega de avisos pendentes no login.
- Verificar: editar e persistir, trocar senha (antiga falha, nova funciona), foto pelas duas origens com permissão negada e concedida, notificação com permissão negada.

### Parte 3: Anúncios, trocas, créditos e geolocalização

Escopo: RF-12 a RF-14, RF-18 a RF-24, RF-27 a RF-30, RN-TROCA-*, RN-CRED-01 a 05, ADR-03, ADR-05.

- Novo/editar/encerrar anúncio (com foto do exemplar), Explorar com busca e filtros.
- Pedido de troca direta e de resgate (**com ou sem crédito**; saldo nunca negativo; doador sempre +1), aceitar/recusar/cancelar, confirmação dos dois lados, máquina de estados em transação Room, livro-razão de créditos.
- `LocationProvider` (`getCurrentLocation` sob demanda, prioridade balanceada), "Perto de mim" com raio, "a pé" (≤ 2 km, 80 m/min, aviso de linha reta).
- Mapa MapLibre + estilo OpenFreeMap: Moderador cadastra pontos seguros tocando no mapa; partes escolhem um ponto para o encontro; fallback em lista sem internet; estado vazio sem pontos.
- `DeadlineWorker` (WorkManager) para expiração (72 h) e cancelamento (7 dias) + lembretes.
- Verificar: fluxo completo de resgate sem crédito e com crédito alternando contas, limite de 5 pedidos, localização negada, GPS desligado, modo avião no mapa.

### Parte 4: Chat, estante, desejos e avaliações

Escopo: RF-25, RF-35 a RF-37, RF-40, RF-32.

- Chat dentro do pedido (`ACEITO` apenas; somente leitura depois), mensagens prontas, limite de 300 caracteres.
- Estante (Estou lendo / Lido / Passei adiante) com o diálogo "Quer passar este livro adiante?" e anúncio pré-preenchido.
- Lista de desejos com notificação quando a obra é anunciada.
- Avaliação 1 a 5 após conclusão, média no perfil.
- Verificar: chat bloqueado em pedido pendente, desejo → notificação ao logar como o outro usuário, avaliação atualiza a média.

### Parte 5: Camada social, moderação e polimento

Escopo: RF-11, RF-15 a RF-17, RF-26, RF-38, RF-39, RNF-04 a 07, `03-design-system.md` §8 e §9.

- Mural finito (14 dias, 30 itens, "Você está em dia"), posts com obra obrigatória, reações "útil" e "quero" (alternam, sem somar).
- Hubs de obra e disciplina, edições compatíveis, trajetória do exemplar ("passou por N mãos"), selos Doador/Veterano.
- Disputa e decisão do Moderador.
- Passe completo de usabilidade e acessibilidade: checklist do design system §9 em **todas** as telas, `contentDescription`, fonte 130%, tema escuro, estados vazio/carregando/erro.
- Preencher o README (como rodar, contas de demonstração com as senhas do seed).
- Rodar uma checagem final: buscar no projeto por "Livro a Livro", senhas em texto puro, `Thread.sleep`, `allowMainThreadQueries`, logs de dados sensíveis.
- Verificar: fluxo ponta a ponta com duas contas, do cadastro à avaliação.

## Saída final

Ao terminar as 5 partes, entregue um resumo com:
- tabela do `06-log-implementacao.md` completa (commits inicial/final de cada parte);
- RFs implementados × pendentes;
- o que foi verificado manualmente e o que não pôde ser verificado;
- lembrete de que os 59 testes caixa-preta devem ser executados pelo grupo e registrados no `05-relatorio-testes.md`.
