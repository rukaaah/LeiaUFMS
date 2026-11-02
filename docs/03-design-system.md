# LeiaUFMS: Design System e Usabilidade

> **Status: definido em 02/11/2026 pela skill `/design-md-library` (Etapa D1).** Referência escolhida na biblioteca: **Oura** (serifa editorial para sentimento e grotesca para fatos, superfícies planas, uma única cor de ação). A referência foi adaptada ao LeiaUFMS e as cores seguem a identidade da UFMS (azul e branco, pedido do Pedro em 02/11): nada de logo, texto ou identidade da marca foi copiado. O §6 recebe os briefings da `/design-taste-frontend` (Etapa D2) e o §9 é usado junto com a `/web-design-guidelines` (Etapa D3).
>
> Fonte única das decisões visuais e de usabilidade. Todo layout XML usa os tokens daqui (via `colors.xml`, `themes.xml`, `type.xml`, `dimens.xml`), nunca valores soltos.

## 1. Conceito e princípios

**"A tranquilidade de um dia de leitura."** O app se parece com uma página bem diagramada nas cores da UFMS: fundo branco, texto escuro, azul institucional na ação, títulos de livro em serifa e silêncio visual entre os blocos. A cor aparece só quando significa algo.

| Princípio | Na prática |
|---|---|
| Calma | Fundo `leia_background` (branco #F6F8FB no claro, #15181D no escuro). No máximo **2 cores de destaque**: azul UFMS para ação e âmbar para créditos/"quero". Animações só de transição, com até **200 ms**; nada pisca nem pula |
| Clareza | **Uma ação principal por tela**, sempre como botão preenchido (pílula azul, 52 dp de altura) na base do conteúdo ou da tela. Ações secundárias são botões contornados ou de texto |
| Confiança | **Distância, "dá para ir a pé", nota média e trocas do dono** visíveis no card e no detalhe **antes** do botão "Pedir". Ponto de encontro sempre é um ponto seguro com nome |
| Sem ruído social | Reações aparecem como **texto discreto** ("3 acharam útil"), nunca contador grande, badge vermelho ou ranking. A Caixa de avisos usa um ponto neutro, não um número vermelho |
| Serifa para sentimento, sans para fatos | **Newsreader** só em títulos de livro e cabeçalhos de tela; **Inter** para tudo que se lê ou se toca (corpo, rótulos, botões, metadados) |
| Mobile-first, uma mão | Margem lateral de 16 dp, alvos de toque ≥ 48 dp, ação principal na metade inferior, navegação por 4 abas fixas com ícone + rótulo |
| Plano, não vidro | Superfícies planas: cards com borda fina (1 dp, `leia_outline_variant`) e **elevação 0**. Sombra só em folhas e diálogos |

## 2. Cores

Base Material 3 (`Theme.Material3.DayNight.NoActionBar`). **Revisão de 02/11/2026 (pedido do Pedro):** a paleta segue as cores institucionais da UFMS, **azul e branco**, para manter uniformidade com a universidade. O azul é a cor de ação; o branco e um cinza-azulado muito claro são as superfícies; o âmbar fica só para créditos e "quero". Contraste calculado pela fórmula de luminância relativa da WCAG 2.x. **Todos os pares de texto passam de 4,5:1** (AA). `leia_outline` é borda de componente (exige 3:1) e `leia_outline_variant` é decorativa.

### Tema claro (`values/colors.xml`)

| Token | Hex | Uso | Contraste |
|---|---|---|---|
| `leia_background` | `#F6F8FB` | Fundo das telas (branco levemente azulado) | — |
| `leia_surface` | `#FFFFFF` | Cards, campos, barra de abas | — |
| `leia_surface_container` | `#EDF1F6` | Blocos de destaque suaves | — |
| `leia_on_background` | `#1A1C20` | Texto principal | 16,04:1 sobre background; 17,06:1 sobre surface |
| `leia_on_surface_variant` | `#464B54` | Texto secundário, metadados | 8,24:1 sobre background; 8,77:1 sobre surface |
| `leia_primary` | `#1B4F8F` | Ação principal, aba ativa, links (azul UFMS) | 7,71:1 sobre background; 8,20:1 sobre surface |
| `leia_on_primary` | `#FFFFFF` | Texto sobre primary | 8,20:1 |
| `leia_primary_container` | `#DCE6F4` | Chips selecionados, selos, "a pé" | texto `#0D2A4D`: 11,45:1 |
| `leia_tertiary` | `#8A5A12` | Créditos, reação "Quero" (âmbar) | 5,56:1 sobre background; 5,91:1 sobre surface |
| `leia_on_tertiary` | `#FFFFFF` | Texto sobre tertiary | 5,91:1 |
| `leia_tertiary_container` | `#F3E3C3` | Fundo do saldo de créditos | texto `#3D2A08`: 10,82:1 |
| `leia_error` | `#B3261E` | Erros | 6,14:1 sobre background; 6,54:1 sobre surface |
| `leia_outline` | `#747A84` | Borda de campo e botão contornado | 4,06:1 (componente, ≥ 3:1) |
| `leia_outline_variant` | `#DDE2EA` | Borda de card, divisores | decorativa |

### Tema escuro (`values-night/colors.xml`)

| Token | Hex | Contraste |
|---|---|---|
| `leia_background` | `#15181D` | — |
| `leia_surface` | `#1F232A` | — |
| `leia_on_background` | `#E8ECF2` | 15,01:1 sobre background; 13,30:1 sobre surface |
| `leia_on_surface_variant` | `#BCC3CE` | 10,03:1 / 8,88:1 |
| `leia_primary` | `#A8C8F5` | 10,37:1 / 9,19:1 |
| `leia_on_primary` | `#0A2547` | 8,95:1 sobre primary |
| `leia_primary_container` | `#1D3A63` | texto `#D6E4FA`: 8,89:1 |
| `leia_tertiary` | `#E2B46C` | 9,30:1 / 8,24:1 |
| `leia_on_tertiary` | `#2E1F05` | 8,35:1 sobre tertiary |
| `leia_tertiary_container` | `#4A3615` | texto `#F6DFB6`: 8,83:1 |
| `leia_error` | `#F2B8B5` | 9,23:1 sobre surface |
| `leia_outline` | `#8C939E` | 5,09:1 sobre surface |

Regras:

- Cor nunca é o único indicador de estado. Status de pedido sempre tem texto + ícone.
- Azul é ação; âmbar é crédito e "quero". Nenhuma outra cor de destaque.
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
| Botão principal | `Widget.Leia.Button` (filled) | Pílula azul, 52 dp. **Um por tela** |
| Botão secundário | `Widget.Leia.Button.Outlined` / `.Text` | Ações alternativas; texto ≥ 48 dp |
| Card | `Widget.Leia.Card` (`MaterialCardView` outlined) | Superfície branca, borda 1 dp, raio 16 dp, sem sombra |
| Card de anúncio | `Widget.Leia.Card` | Capa 64×96 dp, título (BookTitle), autor, distância + chip "a pé", modalidade; lido pelo TalkBack como um único item |
| Campo | `Widget.Leia.TextInputLayout` (outlined) | Raio 12 dp, fundo surface; erro via `setError`, nunca Toast |
| Chips | `Widget.Leia.Chip` (filter) | Filtros e status de estante; alvo 48 dp |
| Navegação | `Widget.Leia.BottomNavigation` | 4 abas: Explorar, Mural, Trocas, Perfil; ícone + rótulo sempre visíveis |
| Barra superior | `Widget.Leia.Toolbar` | Fundo claro, título em Newsreader |
| Feedback curto | `Snackbar` | Confirmações ("Anúncio publicado"), com "Desfazer" quando possível |
| Confirmação | `MaterialAlertDialogBuilder` (`ThemeOverlay.Leia.Dialog`) | Sair, cancelar pedido, abrir disputa |
| Estado vazio | Ícone 64 dp + frase + botão | Toda lista ensina o próximo passo |
| Carregando | `CircularProgressIndicator` | Só se a operação passar de ~300 ms |

### Ícone do app (P-05)

- **Conceito:** um livro aberto em páginas brancas sobre o azul da UFMS, com um **marcador âmbar**: o livro que alguém deixou marcado para passar adiante.
- **Cores:** fundo azul `#1B4F8F`, páginas `#FFFFFF`, marcador `#DDAA61`.
- **Arquivos:** adaptive icon vetorial em `res/mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml` (fundo, frente e camada monocromática para ícones temáticos do Android 13+), declarado no `AndroidManifest.xml` (`android:icon` e `android:roundIcon`). A splash usa `drawable/ic_splash.xml`, o mesmo desenho em azul sobre o fundo claro.

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

### 6.1 Briefings de tela (Etapa D2, `/design-taste-frontend`)

A skill classifica todas as telas do LeiaUFMS como **telas de app** (trabalhadas, não lidas), então valem as regras de `references/app-screens.md` da skill, traduzidas para Android:

- **Cada tela responde uma pergunta primeiro**; o maior elemento da tela é o que responde essa pergunta.
- **Rótulo acima do campo**, nunca placeholder no lugar do rótulo. A regra que limita o campo (ex.: regra de senha) fica **visível como texto de ajuda** do campo desde o início, não só depois do erro.
- **Uma ação preenchida por tela**; secundárias contornadas ou de texto, todas com a mesma altura na mesma linha.
- **Estados honestos:** zero aparece como zero ("0 créditos"); lista vazia mantém o título da seção e mostra uma frase + próximo passo; carregando mostra o layout final (sem tela piscando); erro aparece **dentro do bloco** que falhou, com "Tentar de novo".
- **Palavras do ofício** nos rótulos e estados vazios ("Aguardando resposta", "Passei adiante"), nunca "Itens" ou "Registros".
- **No celular:** navegação por barra inferior de 4 destinos com rótulo curto; filtros em uma linha de chips; o primeiro item da lista começa nos 60% superiores da tela.
- **Proibido:** gradiente, brilho, sombra em card parado, ícone em círculo colorido, emoji, contador vermelho, rótulo minúsculo em caixa alta com espaçamento largo acima de títulos.

| Tela | Objetivo do usuário | Ação principal → secundárias | Hierarquia (de cima para baixo) | Estados (vazio, carregando, erro, sem permissão) |
|---|---|---|---|---|
| Splash | Abrir o app e cair no lugar certo sem esperar | Nenhuma (automática) | Fundo claro, ícone do livro centralizado | Carregando: a própria splash enquanto a sessão é conferida no Room (sem atraso artificial). Erro ao ler a sessão: segue para o Login. Sessão expirada: Login com o aviso "Sua sessão expirou. Entre de novo." |
| Login | Entrar com o e-mail da UFMS em poucos segundos | **Entrar** → "Criar conta" (texto) | Ícone pequeno + "LeiaUFMS" (Display) → frase "Troque livros com quem estuda com você." → rótulo + campo E-mail → rótulo + campo Senha (com mostrar/ocultar) → botão Entrar → linha "Ainda não tem conta? Criar conta" | Carregando: botão desabilitado com "Entrando…" e indicador; campos bloqueados. Erro de credencial: "E-mail ou senha incorretos." no campo Senha (mensagem única). Campo vazio: "Preencha o seu e-mail." / "Preencha a sua senha." |
| Cadastro | Criar a conta e já entrar | **Criar conta** → "Já tenho conta" (texto), Voltar | Barra com Voltar e "Criar conta" (Headline) → frase "Só para quem tem e-mail da UFMS." → Nome → E-mail institucional (ajuda: "Termina em @ufms.br") → Curso → Semestre (1 a 12) → Senha (ajuda sempre visível: regra da senha) → Confirmar senha → botão Criar conta | Carregando: botão "Criando conta…" desabilitado. Erros por campo (§7), foco vai para o primeiro campo com erro. E-mail duplicado: no campo E-mail, "Já existe uma conta com este e-mail. Quer entrar?" com ação "Entrar" na Snackbar |
| Estrutura de abas | Saber onde está e trocar de área com um toque | Trocar de aba → Caixa de avisos (ícone na barra superior, com ponto neutro quando há avisos novos) | Barra superior com o nome da aba (Headline) e ícone da Caixa de avisos → conteúdo → barra inferior fixa: **Explorar, Mural, Trocas, Perfil** (ícone + rótulo sempre visíveis) | Cada aba tem seu estado vazio próprio; a aba ativa preserva a rolagem ao voltar. Voltar na aba inicial fecha o app; nas outras abas volta para Explorar |
| Explorar (aba) | Achar um livro que eu quero e que esteja perto | Abrir um anúncio → buscar, filtrar (modalidade, raio), "Perto de mim", anunciar (botão preenchido flutuante "Anunciar livro") | Busca → linha de chips de filtro → lista de cards (capa, título, autor, distância, "a pé", modalidade) | Vazio: "Ainda não há livros anunciados. Que tal anunciar o seu?" + Anunciar livro. Busca sem resultado: "Nenhum livro com \"{termo}\". Tente o autor ou o ISBN." Sem localização: lista sem distância + aviso da §7 |
| Mural (aba) | Ver o que a comunidade está lendo e procurando, sem rolar para sempre | Novo post → reagir "Útil"/"Quero" | Posts dos últimos 14 dias (até 30) → fim "Você está em dia" | Vazio: "O mural está quieto. Compartilhe uma dica sobre um livro." + Novo post |
| Trocas (aba) | Saber qual pedido precisa de mim agora | Abrir o pedido → alternar "Recebidos" e "Enviados" | Pedidos que pedem ação primeiro, com o próximo passo em texto ("Responda o pedido", "Marque o encontro") → depois os demais | Vazio: "Nenhuma troca por aqui. Peça um livro em Explorar." + Explorar |
| Perfil (aba) | Ver e cuidar do meu cartão de confiança | Editar perfil → estante, desejos, alterar senha, sair | Foto, nome (Display), curso e semestre → saldo de créditos (âmbar) e métricas → estante → opções da conta → Sair (texto, com confirmação) | Sem foto: iniciais sobre `primary_container`. Estante vazia: "Sua estante está vazia. Adicione o livro que está lendo." |

#### Parte 2 (perfil, mídia e avisos)

| Tela | Objetivo do usuário | Ação principal → secundárias | Hierarquia (de cima para baixo) | Estados (vazio, carregando, erro, sem permissão) |
|---|---|---|---|---|
| Perfil (próprio) | Ver como a comunidade me vê e cuidar da minha conta | **Editar perfil** → alterar foto (toque na foto), alterar senha, Caixa de avisos, Sair | Foto 96 dp (ou iniciais) com botão "Alterar foto" → nome (Display) → curso e semestre → status → faixa âmbar "N créditos disponíveis" (+ "1 reservado" quando houver) → métricas em texto: trocas concluídas, nota média ("—" sem avaliações) → opções da conta em lista → Sair (texto, por último) | Sem foto: iniciais em `primary_container`. 0 créditos aparece como "0 créditos". Erro ao salvar foto: Snackbar "Não deu para salvar a foto. Tente de novo." e a foto anterior fica |
| Perfil (de outro aluno) | Decidir se confio nesta pessoa para uma troca | Nenhuma ação preenchida (só leitura) | Mesma ordem, **sem saldo** de créditos e sem opções da conta | Aluno sem trocas: "Ainda não concluiu trocas." |
| Alterar foto (folha inferior) | Trocar a foto com um toque | **Tirar foto** / **Escolher da galeria** (mesmo peso, lista) → Cancelar | Título "Foto do perfil" → duas linhas com ícone e texto | Câmera negada: "Para tirar uma foto, permita o uso da câmera nas configurações." com ação "Abrir configurações" quando a negação é permanente; a galeria continua disponível. Cancelar captura ou seletor: nada muda |
| Editar perfil | Corrigir meus dados sem perder o que já está certo | **Salvar** → Voltar (pergunta antes de descartar alterações) | Nome → E-mail institucional (ajuda "Termina em @ufms.br") → Curso → Semestre → Status (ajuda "Até 80 caracteres", contador) → Salvar | Carregando: "Salvando…". Erros por campo (§7). Sucesso: volta ao Perfil com Snackbar "Perfil atualizado" |
| Alterar senha | Trocar a senha com segurança | **Alterar senha** → Voltar | Senha atual → Nova senha (ajuda com a regra) → Confirmar nova senha → botão | Carregando: "Alterando…". Atual errada: "A senha atual não confere." no campo. Sucesso: Snackbar "Senha alterada" e as outras sessões são encerradas |
| Caixa de avisos | Ver o que aconteceu com minhas trocas enquanto eu não estava olhando | Abrir um aviso → "Marcar todos como lidos" | Faixa discreta quando a permissão de notificação está negada → lista do mais novo para o mais antigo (título, texto, há quanto tempo; não lido com ponto azul e título em peso maior) | Vazio: "Nenhum aviso por enquanto. Quando alguém pedir um livro seu, você fica sabendo aqui." Permissão negada: "Os avisos ficam na sua Caixa de avisos. Para recebê-los no celular, ative as notificações." + "Ativar" |

#### Parte 3 (anúncios, trocas e geolocalização)

| Tela | Objetivo do usuário | Ação principal → secundárias | Hierarquia (de cima para baixo) | Estados (vazio, carregando, erro, sem permissão) |
|---|---|---|---|---|
| Explorar | Achar um livro que eu quero e que esteja perto | Abrir um anúncio → buscar, filtrar por modalidade, "Perto de mim" com raio, **Anunciar livro** (botão estendido fixo no canto inferior) | Campo de busca → uma linha de chips (Todos, Troca, Doação, Perto de mim) → linha de raio só quando "Perto de mim" está ativo → aviso de localização (se houver) → cards: capa 64×96, título (serifa), autor, distância e "a pé" ou "longe para ir a pé", modalidade, dono | Vazio: "Ainda não há livros anunciados. Que tal anunciar o seu?". Busca sem resultado: "Nenhum livro com \"{termo}\". Tente o autor ou o ISBN.". Raio sem resultado: "Nenhum livro a até {raio}. Aumente o raio.". Localização negada / GPS desligado / sem posição: textos da §7, a lista continua sem distância |
| Detalhe do anúncio | Decidir se vale pedir este livro | **Pedir troca** ou **Resgatar** (no anúncio "Troca ou doação", os dois, com Resgatar contornado) → ver perfil do dono, histórico | Capa grande → título/autor → edição e conservação → modalidade → distância + "dá para ir a pé (~N min)" + nota "estimativa em linha reta" → local (ponto seguro ou "local informado pelo dono") → cartão do dono (foto, nome, curso, trocas, nota) → botão | Sem localização: esconder a distância e mostrar "Ative a localização para ver a distância". Anúncio próprio: "Este anúncio é seu." + Editar e Encerrar. Já pediu: "Você já pediu este livro." + Ver pedido. Anúncio encerrado: "Este anúncio foi encerrado." |
| Novo / editar anúncio | Publicar meu livro em menos de um minuto | **Publicar** (ou **Salvar** na edição) → foto do exemplar, local | Título → Autor → ISBN (opcional, ajuda "10 ou 13 dígitos") → Edição (opcional) → Conservação (chips) → Modalidade (chips) → Foto do exemplar (opcional) → Local: "Minha localização agora", "Um ponto seguro" ou "Sem local" → botão | Erros por campo. Sem pontos seguros: a opção mostra "Ainda não há pontos seguros cadastrados". Localização negada: volta para "Sem local" com a mensagem da §7 |
| Trocas | Saber qual pedido precisa de mim agora | Abrir o pedido → alternar **Recebidos** / **Enviados** | Alternador → pedidos que pedem minha ação primeiro, com o próximo passo em texto ("Responda o pedido", "Marque o encontro", "Confirme a entrega") → os demais por data | Vazio recebidos: "Ninguém pediu seus livros ainda. Anuncie um livro em Explorar.". Vazio enviados: "Você ainda não pediu nenhum livro. Peça um livro em Explorar." |
| Pedido | Fazer o próximo passo da troca | **O próximo passo do estado** (Aceitar, Marcar encontro, Confirmar entrega/recebimento) → Recusar, Cancelar pedido, Chat | Livro → estado com ícone + frase do próximo passo → outra parte (nome, curso, trocas, nota; **saldo do solicitante** para o dono num resgate) → encontro (ponto, data e hora) → ações | Sem crédito: "Pedido sem crédito. Quem anunciou decide se aceita." Cancelar e recusar pedem confirmação. Pedido encerrado: só leitura |
| Marcar encontro | Escolher um ponto seguro e um horário | **Confirmar encontro** → ver em lista, minha posição | Mapa com os pontos seguros (marcadores) → ponto escolhido (nome e descrição) → data e hora → botão | Sem pontos: "Ainda não há pontos seguros cadastrados. Peça ao Moderador para cadastrar os pontos do campus.". Sem internet: "O mapa precisa de internet. Veja os pontos seguros em lista.". Data no passado ou após o prazo: erro no campo |
| Pontos seguros (Moderador) | Cadastrar os pontos de encontro do campus | **Tocar no mapa** para adicionar → nome e descrição em diálogo | Mapa → instrução "Toque no mapa para marcar um ponto seguro." → lista dos pontos | Sem internet: o mapa não carrega e a tela explica que precisa de internet para cadastrar |

#### Parte 4 (chat, estante, desejos e avaliações)

| Tela | Objetivo do usuário | Ação principal → secundárias | Hierarquia (de cima para baixo) | Estados (vazio, carregando, erro, sem permissão) |
|---|---|---|---|---|
| Chat do pedido | Combinar a logística do encontro rápido, sem conversa solta | **Enviar** → mensagens prontas em um toque | Livro e outra parte no topo (uma linha) → mensagens (minhas à direita em azul-claro, da outra parte à esquerda em branco, hora em legenda) → linha de mensagens prontas (chips) → campo com contador "0/300" e botão Enviar | Pedido pendente: "O chat abre quando o pedido for aceito." e campo escondido. Pedido encerrado: "Este pedido foi encerrado. O chat fica só para leitura." Sem mensagens: "Nenhuma mensagem ainda. Use uma mensagem pronta para começar." Texto acima de 300: botão desabilitado e contador em vermelho |
| Estante e desejos | Mostrar o que estou lendo e lembrar o que quero, para ser avisado | **Adicionar livro** (na aba ativa) → mudar status, remover, "Passar adiante" | Abas Estante / Desejos → seções da estante em ordem: Estou lendo, Lido, Passei adiante → em Desejos, cada obra com "Disponível agora" quando houver anúncio | Estante vazia: "Sua estante está vazia. Adicione o livro que está lendo." Desejos vazio: "Sua lista de desejos está vazia. Adicione um livro e avisamos quando ele for anunciado." Ao marcar Lido: pergunta "Quer passar este livro adiante?" |
| Avaliar troca | Dar uma nota justa depois da troca | **Enviar nota** → Agora não | Pergunta "Como foi a troca com {nome}?" → 5 estrelas (alvo 48 dp cada, rótulo "{n} de 5") → botão | Só aparece em pedido concluído. Editável por 48 h; depois mostra "Sua nota: {n} de 5" |

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
| Chat encerrado | "Este pedido foi encerrado. O chat fica só para leitura." |
| Encontro fora de ponto seguro | "Escolha um dos pontos seguros marcados no mapa." |
| Sem pontos seguros | "Ainda não há pontos seguros cadastrados. Peça ao Moderador para cadastrar os pontos do campus." |
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
