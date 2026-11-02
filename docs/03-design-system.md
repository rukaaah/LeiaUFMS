# LeiaUFMS: Design System e Usabilidade

> **Status: definido em 02/11/2026 pela skill `/design-md-library` (Etapa D1).** Referência escolhida na biblioteca: **Oura** (papel creme, serifa editorial para sentimento e grotesca para fatos, paleta terrosa, uma única cor de ação). A referência foi adaptada ao LeiaUFMS: nada de logo, texto ou identidade da marca foi copiado. O §6 recebe os briefings da `/design-taste-frontend` (Etapa D2) e o §9 é usado junto com a `/web-design-guidelines` (Etapa D3).
>
> Fonte única das decisões visuais e de usabilidade. Todo layout XML usa os tokens daqui (via `colors.xml`, `themes.xml`, `type.xml`, `dimens.xml`), nunca valores soltos.

## 1. Conceito e princípios

**"A tranquilidade de um dia de leitura."** O app se parece com um caderno de leitura em papel: fundo creme, texto em tinta escura, títulos de livro em serifa, e silêncio visual entre os blocos. A cor aparece só quando significa algo.

| Princípio | Na prática |
|---|---|
| Calma | Fundo `leia_background` (papel #F7F1E8 no claro, #1A1917 no escuro). No máximo **2 cores de destaque**: verde-floresta para ação e âmbar para créditos/"quero". Animações só de transição, com até **200 ms**; nada pisca nem pula |
| Clareza | **Uma ação principal por tela**, sempre como botão preenchido (pílula verde, 52 dp de altura) na base do conteúdo ou da tela. Ações secundárias são botões contornados ou de texto |
| Confiança | **Distância, "dá para ir a pé", nota média e trocas do dono** visíveis no card e no detalhe **antes** do botão "Pedir". Ponto de encontro sempre é um ponto seguro com nome |
| Sem ruído social | Reações aparecem como **texto discreto** ("3 acharam útil"), nunca contador grande, badge vermelho ou ranking. A Caixa de avisos usa um ponto neutro, não um número vermelho |
| Serifa para sentimento, sans para fatos | **Newsreader** só em títulos de livro e cabeçalhos de tela; **Inter** para tudo que se lê ou se toca (corpo, rótulos, botões, metadados) |
| Mobile-first, uma mão | Margem lateral de 16 dp, alvos de toque ≥ 48 dp, ação principal na metade inferior, navegação por 4 abas fixas com ícone + rótulo |
| Papel, não vidro | Superfícies planas: cards com borda fina (1 dp, `leia_outline_variant`) e **elevação 0**. Sombra só em folhas e diálogos |

## 2. Cores

Base Material 3 (`Theme.Material3.DayNight.NoActionBar`). Contraste calculado pela fórmula de luminância relativa da WCAG 2.x (script em Python na sessão de implementação). **Todos os pares de texto passam de 4,5:1** (AA). `leia_outline` é borda de componente (exige 3:1) e `leia_outline_variant` é decorativa.

### Tema claro (`values/colors.xml`)

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `leia_background` | `#F7F1E8` | Fundo das telas (papel) | — |
| `leia_surface` | `#FFFFFF` | Cards, campos, barra de abas | — |
| `leia_surface_container` | `#F1EADF` | Blocos de destaque suaves | — |
| `leia_on_background` | `#1C1B1A` | Texto principal (tinta) | 15,32:1 sobre background; 17,20:1 sobre surface |
| `leia_on_surface_variant` | `#4A4741` | Texto secundário, metadados | 8,24:1 sobre background; 9,26:1 sobre surface |
| `leia_primary` | `#2D5A3D` | Ação principal, aba ativa, links (verde-floresta) | 7,08:1 sobre background |
| `leia_on_primary` | `#FFFFFF` | Texto sobre primary | 7,95:1 |
| `leia_primary_container` | `#DDE5D3` | Chips selecionados, selos, "a pé" | texto `#1C3323`: 10,49:1 |
| `leia_tertiary` | `#8A5A12` | Créditos, reação "Quero" (âmbar) | 5,27:1 sobre background; 5,91:1 sobre surface |
| `leia_on_tertiary` | `#FFFFFF` | Texto sobre tertiary | 5,91:1 |
| `leia_tertiary_container` | `#F3E3C3` | Fundo do saldo de créditos | texto `#3D2A08`: 10,82:1 |
| `leia_error` | `#B3261E` | Erros | 5,82:1 sobre background; 6,54:1 sobre surface |
| `leia_outline` | `#7D786F` | Borda de campo e botão contornado | 3,91:1 (componente, ≥ 3:1) |
| `leia_outline_variant` | `#E1DCD3` | Borda de card, divisores | decorativa |

### Tema escuro (`values-night/colors.xml`)

| Token | Hex | Contraste |
|---|---|---|
| `leia_background` | `#1A1917` | — |
| `leia_surface` | `#262420` | — |
| `leia_on_background` | `#EFE9DF` | 14,55:1 sobre background; 12,83:1 sobre surface |
| `leia_on_surface_variant` | `#C2BBAF` | 9,22:1 / 8,13:1 |
| `leia_primary` | `#A8C9A9` | 9,70:1 / 8,55:1 |
| `leia_on_primary` | `#10281A` | 8,65:1 sobre primary |
| `leia_primary_container` | `#2C4433` | texto `#D3E6D2`: 8,08:1 |
| `leia_tertiary` | `#E2B46C` | 9,18:1 / 8,10:1 |
| `leia_on_tertiary` | `#2E1F05` | 8,35:1 sobre tertiary |
| `leia_tertiary_container` | `#4A3615` | texto `#F6DFB6`: 8,83:1 |
| `leia_error` | `#F2B8B5` | 10,29:1 / 9,07:1 |
| `leia_outline` | `#8F897F` | 5,06:1 / 4,46:1 |

Regras:

- Cor nunca é o único indicador de estado. Status de pedido sempre tem texto + ícone.
- Verde é ação; âmbar é crédito e "quero". Nenhuma outra cor de destaque.
- Ícone do app usa cores fixas (`leia_icon_*`), iguais nos dois temas.

## 3. Tipografia

Fontes empacotadas em `res/font` (licença **SIL Open Font License 1.1**, baixadas do Google Fonts): **Newsreader** (Production Type) e **Inter** (Rasmus Andersson). Estilos em `values/type.xml`.

| Estilo | Fonte | Tamanho / altura de linha | Uso |
|---|---|---|---|
| `TextAppearance.Leia.Display` | Newsreader Medium | 28 sp / 36 sp | Título grande (boas-vindas, perfil) |
| `TextAppearance.Leia.Headline` | Newsreader Medium | 22 sp / 28 sp | Título de tela e da barra superior |
| `TextAppearance.Leia.BookTitleLarge` | Newsreader SemiBold | 24 sp / 30 sp | Título do livro no detalhe |
| `TextAppearance.Leia.BookTitle` | Newsreader SemiBold | 18 sp / 24 sp | Título do livro em card |
| `TextAppearance.Leia.Title` | Inter SemiBold | 16 sp / 24 sp | Cabeçalho de seção |
| `TextAppearance.Leia.Body` | Inter Regular | 16 sp / 24 sp | Texto corrido, campos |
| `TextAppearance.Leia.BodySmall` | Inter Regular | 14 sp / 20 sp | Descrições, mensagens de chat |
| `TextAppearance.Leia.Label` | Inter Medium | 15 sp / 20 sp | Botões, chips |
| `TextAppearance.Leia.Caption` | Inter Regular | 13 sp / 18 sp | Metadados (distância, curso) |
| `TextAppearance.Leia.Overline` | Inter SemiBold, caixa alta, espaçamento 0,08 | 12 sp / 16 sp | Rótulos de seção discretos |

- Sempre `sp`. Nenhum texto abaixo de 12 sp.
- Hierarquia por tamanho e serifa, não por negrito pesado.

## 4. Espaçamento, forma, elevação e toque

Grid de **4 dp** (`values/dimens.xml`).

| Token | Valor | Uso |
|---|---|---|
| `space_xxs` / `space_xs` / `space_s` | 4 / 8 / 12 dp | Ícone-texto, chips, linhas de metadado |
| `space_m` | 16 dp | Padding de card, margem lateral (`screen_margin`) |
| `space_l` / `space_xl` / `space_xxl` | 24 / 32 / 48 dp | Separação entre seções, estados vazios |
| `touch_target` | 48 dp | Alvo de toque mínimo (`minTouchTargetSize`) |
| `button_height` | 52 dp | Botões preenchido e contornado |
| `radius_s` / `radius_m` / `radius_l` | 8 / 12 / 16 dp | Capa, campos, cards |
| `radius_pill` | 26 dp | Botões (pílula) |
| `stroke_hairline` | 1 dp | Borda de card |
| `elevation_flat` / `elevation_sheet` | 0 / 2 dp | Cards / folhas e diálogos |
| `cover_width` × `cover_height` | 64 × 96 dp | Capa no card |
| `cover_width_large` × `cover_height_large` | 120 × 180 dp | Capa no detalhe |
| `avatar_small` / `avatar_large` | 40 / 96 dp | Foto no card / no perfil |

## 5. Componentes e ícone

| Componente | Base Material | Regra |
|---|---|---|
| Botão principal | `Widget.Leia.Button` (filled) | Pílula verde, 52 dp. **Um por tela** |
| Botão secundário | `Widget.Leia.Button.Outlined` / `.Text` | Ações alternativas; texto ≥ 48 dp |
| Card | `Widget.Leia.Card` (`MaterialCardView` outlined) | Superfície branca, borda 1 dp, raio 16 dp, sem sombra |
| Card de anúncio | `Widget.Leia.Card` | Capa 64×96 dp, título (BookTitle), autor, distância + chip "a pé", modalidade; lido pelo TalkBack como um único item |
| Campo | `Widget.Leia.TextInputLayout` (outlined) | Raio 12 dp, fundo surface; erro via `setError`, nunca Toast |
| Chips | `Widget.Leia.Chip` (filter) | Filtros e status de estante; alvo 48 dp |
| Navegação | `Widget.Leia.BottomNavigation` | 4 abas: Explorar, Mural, Trocas, Perfil; ícone + rótulo sempre visíveis |
| Barra superior | `Widget.Leia.Toolbar` | Fundo papel, título em Newsreader |
| Feedback curto | `Snackbar` | Confirmações ("Anúncio publicado"), com "Desfazer" quando possível |
| Confirmação | `MaterialAlertDialogBuilder` (`ThemeOverlay.Leia.Dialog`) | Sair, cancelar pedido, abrir disputa |
| Estado vazio | Ícone 64 dp + frase + botão | Toda lista ensina o próximo passo |
| Carregando | `CircularProgressIndicator` | Só se a operação passar de ~300 ms |

### Ícone do app (P-05)

- **Conceito:** um livro aberto em papel creme com um **marcador âmbar**: o livro que alguém deixou marcado para passar adiante.
- **Cores:** fundo verde-floresta `#2D5A3D`, páginas `#F7F1E8`, marcador `#DDAA61`.
- **Arquivos:** adaptive icon vetorial em `res/mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml` (fundo, frente e camada monocromática para ícones temáticos do Android 13+), declarado no `AndroidManifest.xml` (`android:icon` e `android:roundIcon`). A splash usa `drawable/ic_splash.xml`, o mesmo desenho em tinta verde sobre papel.

## 6. Mapa de telas

| Tela | Tipo | Ação principal |
|---|---|---|
| Splash | SplashScreen API | — |
| Login | `AuthActivity` / fragment | Entrar |
| Cadastro | `AuthActivity` / fragment | Criar conta |
| **Explorar** | Fragment (aba) | Ver anúncio; alternar lista/"Perto de mim" |
| Detalhe do anúncio | Fragment | Pedir troca / Resgatar com crédito |
| Novo anúncio | Fragment | Publicar |
| Hub da obra / disciplina | Fragment | Ver anúncios da obra |
| **Mural** | Fragment (aba) | Novo post |
| **Trocas** | Fragment (aba) | Abrir pedido |
| Pedido (detalhe) | Fragment | Próximo passo do estado |
| Chat do pedido | Fragment | Enviar mensagem pronta |
| Marcar encontro | Fragment com mapa | Confirmar ponto |
| **Perfil** | Fragment (aba) | Editar perfil |
| Editar perfil / Alterar senha | Fragment | Salvar |
| Estante e desejos | Fragment | Adicionar livro |
| Caixa de avisos | Fragment | Abrir aviso |

## 7. Microcopy e mensagens de erro

Tom: segunda pessoa, frases curtas, diz o que aconteceu e como resolver. Nada de "Erro 500", "inválido" sem contexto ou culpa no usuário.

| Situação | Mensagem |
|---|---|
| Campo vazio | "Preencha o seu {campo}." |
| E-mail fora do domínio | "Use seu e-mail institucional da UFMS." |
| E-mail já cadastrado | "Já existe uma conta com este e-mail. Quer entrar?" |
| Senha fraca | "A senha precisa de 8 caracteres, com pelo menos 1 letra e 1 número." |
| Senhas diferentes | "As senhas não são iguais." |
| Login falhou | "E-mail ou senha incorretos." |
| Senha atual errada | "A senha atual não confere." |
| Pedido sem crédito | "Você está sem créditos, mas pode pedir. Quem anunciou decide se aceita." |
| Próprio anúncio | "Este anúncio é seu." |
| Limite de pedidos | "Você já tem 5 pedidos aguardando resposta. Espere algum ser respondido." |
| Localização negada | "Sem acesso à localização, não dá para mostrar a distância. Você ainda pode ver todos os livros." |
| GPS desligado | "Ligue a localização do aparelho para ver livros perto de você." |
| Sem posição | "Não conseguimos sua localização agora. Tente de novo em um lugar aberto." |
| Mapa sem internet | "O mapa precisa de internet. Veja os pontos seguros em lista." |
| Câmera negada | "Para tirar uma foto, permita o uso da câmera nas configurações." |
| Notificação negada | "Os avisos ficam na sua Caixa de avisos. Para recebê-los no celular, ative as notificações." |
| Chat bloqueado | "O chat abre quando o pedido for aceito." |
| Encontro fora de ponto seguro | "Escolha um dos pontos seguros marcados no mapa." |
| Sessão expirada | "Sua sessão expirou. Entre de novo." |

## 8. Acessibilidade (cuidados básicos)

- `contentDescription` em todo `ImageView`/`ImageButton` com significado; `@null` nos decorativos.
- Capa de livro: "Capa de {título}".
- Agrupar card de anúncio para o TalkBack ler "Título, autor, a 400 metros, dá para ir a pé".
- Não bloquear rotação nem tamanho de fonte do sistema; testar com fonte em 130%.

## 9. Checklist de auditoria de UI

Somado à saída da skill `/web-design-guidelines` na Etapa D3 (fim de cada parte), adaptado para Android. Rodar em toda tela antes de marcar como pronta.

- [ ] Usa só tokens de cor/dimensão/tipografia deste documento
- [ ] Uma ação principal clara
- [ ] Alvos de toque ≥ 48 dp
- [ ] Texto em `strings.xml`
- [ ] Erros por campo com mensagem da seção 7
- [ ] Estado vazio, carregando e erro tratados
- [ ] Funciona em tema claro e escuro
- [ ] Funciona com fonte do sistema em 130%
- [ ] TalkBack lê os elementos em ordem lógica
- [ ] Sem crash com permissão negada ou sem internet
- [ ] Botão Voltar se comporta como esperado
