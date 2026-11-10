# LeiaUFMS: Pré-documentação

> Documento de planejamento interno do grupo. Registra as decisões de arquitetura e design e as pendências comunitárias. O documento de entrega é o [`10-especificacao-entrega.md`](10-especificacao-entrega.md).

| Campo | Valor |
|---|---|
| Disciplina | Programação Mobile, Avaliação 3 |
| Aplicativo | **LeiaUFMS** (nome anterior: "Livro a Livro") |
| Entrega | **09/11/2026 até 23:59**, via AVA |
| Compartilhar com | `matheus.albuquerque@ufms.br` (pasta do Drive ou repositório GitHub) |
| Integrantes | Pedro Lucas de Souza Cremonini, Gabriel Mattos de Aquino, Angelo Antonio Correia de Souza |
| Status | Implementação concluída (ver `06-log-implementacao.md`) |

---

## 1. Decisões de arquitetura (ADRs resumidos)

### ADR-01: Banco local Room, sem backend

- **Contexto:** o PDF exige Room e não exige servidor. Prazo de 7 dias.
- **Decisão:** toda a comunidade vive no banco Room do aparelho. A demonstração usa **contas semeadas** (seed) e **múltiplas contas no mesmo aparelho** (login/logout).
- **Consequências:**
  - Notificações para outro usuário viram registros na tabela `notificacao`; a notificação do sistema dispara quando o destinatário é a sessão ativa, ou no próximo login dele ("Você tem N pendências").
  - Chat entre usuários é demonstrado alternando contas.
  - Alternativa rejeitada: Firebase. Adiciona configuração, regras de segurança e risco de prazo sem ganho de nota.

### ADR-02: Java + MVVM + Navigation, uma Activity principal

- **Template:** Empty Views Activity (Java).
- **Camadas:** `ui` (Activity/Fragment + ViewModel + LiveData) → `data/repository` → `data/local` (Room DAO). Acesso ao banco fora da main thread via `ExecutorService`.
- **Navegação:** `MainActivity` com `BottomNavigationView` + Navigation Component (fragments). `AuthActivity` separada para login/cadastro.
- **Splash:** API `androidx.core:core-splashscreen` (`installSplashScreen()` + `setKeepOnScreenCondition` enquanto a sessão é verificada). Sem `Thread.sleep`/`Handler.postDelayed`.

### ADR-03: Mapa com MapLibre + OpenFreeMap; localização com Fused Location

| Opção | Chave/cobrança | Manutenção | Decisão |
|---|---|---|---|
| Google Maps SDK | Exige API key e billing ativado no projeto ([fonte](https://developers.google.com/maps/documentation/android-sdk/usage-and-billing)) | Ativa | Alternativa, se o grupo já tiver chave |
| osmdroid | Sem chave | **Arquivado em 20/11/2024**, última versão 6.1.20 ([fonte](https://github.com/osmdroid/osmdroid/blob/master/README.md)) | Rejeitado |
| MapLibre Native Android + estilo OpenFreeMap | Sem chave | Ativa | **Escolhido** |

- Verificar a versão estável atual do MapLibre Android e a URL de estilo do OpenFreeMap antes de configurar o Gradle (P-08).
- **Localização:** `FusedLocationProviderClient.getCurrentLocation()` com prioridade balanceada, sob demanda. Sem atualização contínua em segundo plano (economia de energia).
- **Distância:** `Location.distanceBetween()` (linha reta). A estimativa a pé é aproximada e o app avisa isso ao usuário.

### ADR-04: Segurança

| Item | Decisão |
|---|---|
| Hash de senha | BCrypt (`at.favre.lib:bcrypt`, [repositório](https://github.com/patrickfav/bcrypt)), custo 12, salt embutido no hash. Senha manipulada como `char[]` e zerada após uso |
| Troca de senha | Exige senha atual; gera **novo hash** com novo salt |
| Sessão | `SharedPreferences` guarda só `userId` + token aleatório; o Room guarda o **hash SHA-256 do token** e a expiração (30 dias). Logout apaga as duas pontas |
| Guarda de rotas | `MainActivity` e toda tela restrita validam a sessão no `onCreate`/`onResume`; sem sessão → `AuthActivity` com pilha limpa |
| Fotos | Salvas em `filesDir` (armazenamento interno do app); Room guarda o caminho. Câmera via `FileProvider` |
| Manifest | `android:allowBackup="false"`; apenas a Activity de entrada com `exported="true"` |
| Logs | Proibido logar senha, hash, token ou e-mail completo |

### ADR-05: Créditos como livro-razão

- Saldo = soma dos lançamentos na tabela `movimento_credito`. Nunca editar saldo diretamente.
- **Crédito não é pré-requisito para pedir.** Qualquer aluno pode pedir um livro com 0 créditos; quem decide é o anunciante, que vê o saldo do solicitante antes de aceitar.
- Se o solicitante tem saldo ≥ 1, o pedido de resgate reserva 1 crédito, consumido na conclusão. Com saldo 0, nada é reservado e o saldo **nunca fica negativo**.
- O doador ganha +1 em toda doação concluída, com ou sem crédito do outro lado. Assim a rede não trava por falta de saldo inicial e o crédito funciona como sinal de reciprocidade no perfil.

---

## 2. Design (decisões visuais)

Detalhamento completo em [`03-design-system.md`](03-design-system.md).

- **Conceito:** "a tranquilidade de um dia de leitura". Cores da UFMS (azul e branco), texto escuro, azul para ação, âmbar só para créditos, sem ruído visual.
- **Base:** Material Components for Android, tema `Theme.Material3.DayNight.NoActionBar`, com tema claro e escuro.
- **Contraste:** todos os pares texto/fundo da paleta passam WCAG AA (≥ 4,5:1), calculados na seção 2 do design system.
- **Tipografia:** Newsreader (serifada, OFL) em títulos de livros e cabeçalhos; Inter (OFL) no corpo.

---

## 3. Pendências comunitárias (regras de negócio a confirmar)

Cada item tem um **padrão adotado** para não travar o desenvolvimento. O grupo confirma ou altera.

| ID | Pergunta | Padrão adotado |
|---|---|---|
| PC-01 | Usuário novo ganha crédito de boas-vindas? Pode pedir sem crédito? | **Resolvido (02/11, grupo):** começa com 0 créditos e **pode pedir mesmo assim**; o anunciante decide se aceita |
| PC-02 | Quem resolve disputas? | Papel **Moderador** (conta semeada). Troca contestada fica "Em disputa" com créditos e exemplares congelados até o moderador decidir |
| PC-03 | Prazo para responder um pedido | **72 h**. Depois, o pedido expira e o crédito reservado é liberado |
| PC-04 | Prazo para concluir a entrega após aceite | **7 dias**. Lembrete no 5º dia; no 8º dia o pedido é cancelado automaticamente |
| PC-05 | Como a troca é dada por concluída | **Confirmação dos dois lados** ("Entreguei"/"Recebi"). Só então créditos e posse mudam |
| PC-06 | Limite anti-spam de pedidos | Máx. **5 pedidos pendentes** por usuário e **1 pedido ativo** por anúncio por usuário |
| PC-07 | Pontos de encontro seguros | **Resolvido (02/11, grupo):** somente pontos cadastrados pelo Moderador **dentro do app, tocando no mapa** (RF-30). O seed traz **3 pontos padrão** com coordenadas reais do campus (definidas em 09/11/2026): Biblioteca Central (-20.498414362206436, -54.61211807882261), Restaurante Universitário (-20.503137557358404, -54.614499880413405) e Concha Acústica (-20.500122768766932, -54.61304075872894). Sem pontos cadastrados, "Marcar encontro" mostra um estado vazio explicando que o Moderador precisa cadastrá-los |
| PC-08 | Raio "a pé" | ≤ **2 km** em linha reta, tempo estimado a 80 m/min (≈ 4,8 km/h); valores ajustáveis em `AppConfig` |
| PC-09 | Critério dos selos | **Doador:** ≥ 3 doações concluídas. **Veterano:** ≥ 10 trocas concluídas |
| PC-10 | Avaliação após troca | Nota 1 a 5, opcional, uma por lado, editável por 48 h |
| PC-11 | Mural | Cada post fica **12 h** no mural, mais 12 h a cada **25 corações** (0 = 12 h, 25 = 24 h, 50 = 36 h); máx. **30 itens**; termina com "Você está em dia". A regra não aparece na tela (decisão de 03/11: aprende-se com o uso). Posts salvos ficam no filtro "Salvos" sem prazo |
| PC-12 | Texto livre no chat | Permitido, máx. **300 caracteres**, só com pedido Aceito; somente leitura após encerramento |

## 4. Pendências técnicas e administrativas

| ID | Pendência | Responsável |
|---|---|---|
| P-01 | ~~Confirmar que o tema não repete Avaliação 1/2~~ **Resolvido em 02/11:** o tema não repete as Avaliações 1 e 2 | Grupo |
| P-02 | ~~Nomes completos dos integrantes~~ **Resolvido em 02/11** | Grupo |
| P-03 | ~~Formato do e-mail de aluno~~ **Resolvido em 02/11:** todo e-mail de aluno termina em `@ufms.br` | Grupo |
| P-04 | ~~Definir `applicationId`~~ **Resolvido em 02/11:** `br.ufms.leiaufms` (pacote e applicationId) | Grupo |
| P-05 | ~~Criar o ícone do app~~ **Resolvido em 02/11:** adaptive icon vetorial declarado no Manifest (`android:icon` e `android:roundIcon`). Ver `03-design-system.md` §5 | Agente |
| P-06 | ~~Coletar coordenadas dos pontos seguros~~ **Resolvido em 02/11:** os pontos são criados no app pelo Moderador (PC-07); nada é semeado | Grupo |
| P-07 | Definir dispositivo/emulador e versão do Android usados nos testes. O desenvolvimento usou o AVD `Pixel_10` (Android API 37) | Grupo |
| P-08 | ~~Verificar versões estáveis atuais das dependências~~ **Resolvido em 02/11:** conferidas no Google Maven e no Maven Central e registradas em `gradle/libs.versions.toml` | Agente |
| P-09 | Entrega no GitHub: https://github.com/rukaaah/LeiaUFMS. **Falta compartilhar com o professor** (adicionar como colaborador ou deixar público) | Grupo |

---

## 5. Riscos

| Risco | Probabilidade | Impacto | Mitigação |
|---|---|---|---|
| Escopo social grande para 7 dias | Alta | Alto | Priorização MoSCoW (seção 7); requisitos do PDF primeiro |
| Testes não executados a tempo | Média | Muito alto | Reservar o dia 08/11 só para executar e registrar testes |
| GPS impreciso em ambiente fechado/emulador | Alta | Médio | Usar localização simulada no emulador; mensagem clara quando não houver fix |
| Permissão negada permanentemente | Média | Médio | Tela explicativa + atalho para Configurações do app |
| Mapa sem internet | Média | Médio | Lista de pontos seguros funciona sem o mapa (fallback) |

## 6. Priorização e cronograma

| Prioridade | Itens |
|---|---|
| **Deve** (PDF) | Splash, auth com domínio, sessão/logout, BCrypt, edição de perfil e senha, foto câmera/galeria, permissões, notificações, geolocalização (livros próximos + a pé + mapa de ponto seguro), anúncios, pedidos de troca/resgate, créditos, ≥ 3 telas |
| **Deveria** | Chat vinculado a pedido, estante ("Estou lendo"), lista de desejos com notificação, avaliações |
| **Poderia** | Mural, hubs de obra/disciplina, selos, trajetória do exemplar, moderação |

| Data | Meta |
|---|---|
| 02/11 | Documentação (este pacote) |
| 03/11 | Projeto Android, Room (entidades + seed), auth + sessão + splash |
| 04/11 | Perfil, foto, senha, permissões, notificações |
| 05/11 | Anúncios, pedidos, créditos, geolocalização e mapa |
| 06/11 | Chat, estante, desejos, avaliações |
| 07/11 | Mural, hubs, selos, trajetória, polimento de usabilidade |
| 08/11 | **Execução e registro dos testes caixa-preta**, correções |
| 09/11 | Revisão final, especificação em PDF, entrega no AVA |
