# LeiaUFMS: Arquitetura e Modelo de Dados

> Guia técnico para implementação. As justificativas das escolhas estão nos ADRs do [`01-pre-documentacao.md`](01-pre-documentacao.md#2-decisões-de-arquitetura-adrs-resumidos).

## 1. Stack

| Camada | Tecnologia | Observação |
|---|---|---|
| Linguagem | Java | Exigência do PDF |
| Template | Empty Views Activity | Exigência do PDF |
| UI | Views XML + Material Components (Material 3) | — |
| Navegação | AndroidX Navigation (fragment) + `BottomNavigationView` | — |
| Estado | AndroidX `ViewModel` + `LiveData` | — |
| Banco | Room | Exigência do PDF |
| Hash | `at.favre.lib:bcrypt` | Custo 12 |
| Splash | `androidx.core:core-splashscreen` | Sem atraso artificial |
| Localização | `com.google.android.gms:play-services-location` | `getCurrentLocation` sob demanda |
| Mapa | MapLibre Native Android + estilo OpenFreeMap (`tiles.openfreemap.org/styles/liberty`) | Sem chave de API. Câmera inicial no centro de Campo Grande (coordenada pública da cidade, não é ponto de encontro) |
| Tarefas | WorkManager | Expiração de pedidos e lembretes |
| Imagens | `ActivityResultContracts.TakePicture` / `PickVisualMedia` + `FileProvider` | — |

**Versões:** não fixadas aqui de propósito. Usar a versão estável mais recente de cada uma no momento do setup, registrar no `gradle/libs.versions.toml` e anotar no `AGENT.md` (seção Estado atual).

## 2. Estrutura de pacotes

Pacote base: `br.ufms.leiaufms` (P-04 resolvida).

```
app/src/main/java/br/ufms/leiaufms/
├── LeiaApp.java                  # Application: inicializa DB, canais de notificação, WorkManager
├── AppConfig.java                # Constantes de regra de negócio (domínios, prazos, raios, limites)
├── core/
│   ├── security/PasswordHasher.java   # BCrypt hash/verify com char[]
│   ├── security/SessionManager.java   # token, validação, logout
│   ├── permission/PermissionHelper.java
│   ├── notification/Notifier.java     # canais + post + fallback para caixa de avisos
│   ├── location/LocationProvider.java # getCurrentLocation + distância + "a pé"
│   └── util/Validators.java           # regras RN-AUTH-*
├── data/
│   ├── local/AppDatabase.java, Converters.java, SeedData.java
│   ├── local/entity/*.java
│   ├── local/dao/*.java
│   └── repository/*.java          # AuthRepository, UserRepository, ListingRepository, TradeRepository, CreditRepository, SocialRepository, MeetingRepository
├── work/DeadlineWorker.java
└── ui/
    ├── splash/ (lógica na MainActivity/EntryActivity)
    ├── auth/  AuthActivity, LoginFragment, RegisterFragment
    ├── main/  MainActivity (BottomNavigationView + NavHost)
    ├── explore/, listing/, book/, feed/, trade/, chat/, meeting/, profile/, shelf/, inbox/
```

## 3. Fluxo de inicialização

```
App abre
  └─ EntryActivity.onCreate
       ├─ installSplashScreen()
       ├─ setKeepOnScreenCondition(() -> !sessionChecked)
       ├─ SessionManager.validate() em background
       └─ resultado:
            ├─ válida   → MainActivity (FLAG_ACTIVITY_CLEAR_TASK)
            └─ inválida → AuthActivity (FLAG_ACTIVITY_CLEAR_TASK)
```

`MainActivity.onResume` revalida a sessão; se inválida, volta ao `AuthActivity` e limpa a pilha.

## 4. Modelo de dados (Room)

| Entidade | Campos principais | Notas |
|---|---|---|
| `usuario` | id, nome, email (único, minúsculo), senhaHash, curso, semestre, status, fotoPath, papel (`ALUNO`/`MODERADOR`), criadoEm | Nunca guardar senha |
| `sessao` | id, usuarioId, tokenHash, criadaEm, expiraEm | Token em claro só no `SharedPreferences` |
| `obra` | id, titulo, autor, isbn13 (nulo) | Agrupador do hub |
| `edicao_compativel` | obraId, obraCompativelId | Marcado por moderador |
| `disciplina` | id, codigo, nome, curso | Seed |
| `obra_disciplina` | obraId, disciplinaId | Recomendações |
| `exemplar` | id, obraId, donoAtualId, edicao, conservacao, fotoPath | Objeto físico |
| `posse_exemplar` | id, exemplarId, usuarioId, desde, ate, pedidoId | Trajetória (RF-17) |
| `anuncio` | id, exemplarId, donoId, modalidade (`TROCA`/`DOACAO`/`AMBAS`), lat, lng, pontoPreferidoId, ativo, criadoEm | — |
| `pedido` | id, anuncioId, solicitanteId, donoId, tipo (`TROCA_DIRETA`/`RESGATE`), exemplarOferecidoId (nulo), estado, pontoEncontroId, encontroEm, confirmadoSolicitante, confirmadoDono, semCredito, motivo, criadoEm, atualizadoEm, aceitoEm, lembreteEnviado, lembreteEncontroEnviado | Estados em §5. Banco v2 (migração 1 → 2 adiciona `lembreteEncontroEnviado`) |
| `mensagem` | id, pedidoId, autorId, texto, pronta (bool), enviadaEm | Só com pedido aceito |
| `movimento_credito` | id, usuarioId, valor (+1/−1), tipo (`DOACAO`/`RESGATE`/`RESERVA`/`LIBERACAO`), pedidoId, criadoEm | Livro-razão |
| `avaliacao` | id, pedidoId, avaliadorId, avaliadoId, nota, criadaEm | Única por (pedido, avaliador) |
| `item_estante` | id, usuarioId, obraId, status (`LENDO`/`LIDO`/`PASSADO_ADIANTE`) | — |
| `desejo` | id, usuarioId, obraId, criadoEm | Única por (usuário, obra) |
| `post_mural` | id, autorId, obraId (obrigatório), tipo (`DICA`/`PROCURO`/`DOACAO`/`TROCA_CONCLUIDA`), texto, oculto, criadoEm | — |
| `reacao` | postId, usuarioId, tipo (`UTIL`/`QUERO`) | PK composta |
| `ponto_encontro` | id, nome, descricao, lat, lng, ativo, criadoPor, criadoEm | Criado pelo Moderador tocando no mapa (RF-30); nenhum no seed (PC-07) |
| `notificacao` | id, usuarioId, canal, titulo, texto, destinoTipo, destinoId, lida, criadaEm | Caixa de avisos |

**Pedido sem crédito:** permitido (RN-CRED-04). Se o saldo disponível for 0, o pedido é criado sem `RESERVA` e com `semCredito = true` em `pedido`; na conclusão só o doador recebe `DOACAO` (+1).

**Reserva de crédito:** implementar como `movimento_credito` tipo `RESERVA` (−1) e, ao recusar/cancelar/expirar, `LIBERACAO` (+1). Na conclusão, a reserva vira o consumo definitivo e o doador recebe `DOACAO` (+1). Todas as mudanças de um pedido em **uma transação Room** (`@Transaction` / `runInTransaction`).

## 5. Máquina de estados do pedido

| De | Evento | Para | Efeitos |
|---|---|---|---|
| — | Solicitar | PENDENTE | Reserva 1 crédito se for resgate e houver saldo; notifica dono com o saldo do solicitante |
| PENDENTE | Dono aceita | ACEITO | Abre chat; recusa os outros pedidos do anúncio |
| PENDENTE | Dono recusa | RECUSADO | Libera reserva |
| PENDENTE | 72 h | EXPIRADO | Libera reserva |
| PENDENTE/ACEITO | Parte cancela | CANCELADO | Libera reserva; fecha chat |
| ACEITO | 7 dias sem conclusão | CANCELADO | Libera reserva |
| ACEITO | Ambos confirmam | CONCLUIDO | Troca posse, créditos, histórico, convite de avaliação, post opcional |
| ACEITO | Parte abre disputa | EM_DISPUTA | Congela |
| EM_DISPUTA | Moderador decide | CONCLUIDO ou CANCELADO | Efeitos do estado final |

Transição fora desta tabela = erro de programação. Validar no repositório e cobrir com teste.

## 6. Permissões

| Permissão | Quando pedir | Se negar |
|---|---|---|
| `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` | Ao tocar em "Perto de mim" ou abrir o mapa | Lista sem distância; mapa sem ponto do usuário |
| `CAMERA` | Ao tocar em "Tirar foto" | Mensagem + atalho para Configurações; galeria continua disponível |
| `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` | Não necessária com Photo Picker; declarar apenas se usar fallback antigo (`maxSdkVersion` adequado) | Galeria indisponível só no fallback |
| `POST_NOTIFICATIONS` (API 33+) | No primeiro pedido feito/recebido | Caixa de avisos interna + banner |
| `INTERNET` | Normal (sem runtime) | — |

Use `shouldShowRequestPermissionRationale` para diferenciar "negado uma vez" de "negado permanentemente".

## 7. Seed de demonstração

`SeedData` popula no primeiro uso (em background, numa transação, disparado pelo `LeiaApp`; login e cadastro esperam ele terminar):

- 1 Moderador e 4 alunos de cursos diferentes. Senha de demonstração só no README; o hash BCrypt é gerado no seed.
- 15 obras reais, 3 disciplinas com nomes genéricos (sem códigos UFMS inventados) e recomendações.
- 11 anúncios ativos **sem coordenadas**: o local é escolhido no próprio app (localização atual ou ponto seguro).
- **Nenhum ponto seguro.** O Moderador cadastra os pontos tocando no mapa (RF-30, PC-07). Sem pontos, "Marcar encontro" mostra um estado vazio.
- Histórico de 3 doações concluídas que geram os créditos no livro-razão (Ana 2, os outros 0) e 1 pedido em cada estado: pendente sem crédito, aceito (com mensagens no chat), recusado, cancelado (reserva e liberação), expirado e em disputa (reserva congelada).
- Estantes, desejos, 3 posts no mural e reações.
