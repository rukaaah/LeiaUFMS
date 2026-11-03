# LeiaUFMS: Design System e Usabilidade

> **Status: v2, redefinido em 03/11/2026** depois do feedback do Pedro ("morno, padrão, abas iguais"). A v1 (referência Oura, tudo plano) foi **substituída**. Papéis das skills (Pedro, 03/11): `/design-md-library` define **organização, modelos e lógica** (troca: **Airbnb**; social: **Pinterest**; mydealz descartado por ser ranking por votação) e `/mobile-design-md-library` define **cores e combinações** (**Nova Mobile** no app todo; cores por tipo de post do meio-termo com **Playful Color** no Mural), sempre nas cores da UFMS. O resumo aplicável está em [`DESIGN.md`](../DESIGN.md); a jornada e a base de psicologia em [`08-jornada-e-usabilidade.md`](08-jornada-e-usabilidade.md). Briefings por tela (`/design-taste-frontend`) no §6 e auditoria (`/web-design-guidelines`) com o checklist do §9.

## 1. Conceito e princípios

**Diferencial de usabilidade: o LeiaUFMS sempre te diz qual é o próximo livro e qual é o próximo passo.**

| Princípio | Na prática |
|---|---|
| A troca é a protagonista | Início, Livro e Trocas têm a hierarquia mais forte; Mural e Perfil seguem o mesmo sistema, com o social bem estruturado mas em segundo plano |
| Capa primeiro (Airbnb) | Toda obra mostra a capa real (Open Library); a capa domina o card do carrossel (136×204 dp) e o topo do Livro (280 dp) |
| Sempre um próximo passo | Início abre com "Pra você agora" (pedidos esperando você, desejos que apareceram, encontros); o Pedido mostra a barra de 4 etapas e um único botão principal |
| Confiança antes do pedido | Cartão do dono com a nota em destaque, trocas concluídas, ponto seguro e "a pé" aparecem antes da barra de ação |
| Uma cor de marca | Azul UFMS em ação e destaque; âmbar só para créditos; verde só para "a pé" e "concluído" |
| Cada aba com identidade | Início (carrosséis de capas), Mural (posts coloridos por tipo), Trocas (troca ativa com progresso), Perfil (cabeçalho azul com créditos e livros passados adiante) |
| Social vivo, sem toxicidade | Etiqueta colorida por tipo de post, reações "Útil" e "Quero" em texto com micro-animação; nada de ranking, seguidores ou badge vermelho |

## 2. Cores

Contraste pela fórmula de luminância relativa da WCAG 2.x. **Todos os pares de texto ≥ 5,9:1.**

| Par (claro) | Hex | Contraste | Par (escuro) | Hex | Contraste |
|---|---|---|---|---|---|
| Texto / fundo | `#111827` / `#FFFFFF` | 17,74:1 | Texto / superfície | `#F3F4F6` / `#171B22` | 15,69:1 |
| Secundário / superfície suave | `#4B5563` / `#F4F6FA` | 6,98:1 | Secundário / superfície | `#A8B0BD` / `#171B22` | 7,90:1 |
| Primária / fundo | `#1B4F8F` / `#FFFFFF` | 8,20:1 | Primária / superfície | `#8DB8F2` / `#171B22` | 8,44:1 |
| Texto branco / primária (cabeçalho do Perfil) | `#FFFFFF` / `#1B4F8F` | 8,20:1 | Texto sobre primária | `#0A2547` / `#8DB8F2` | 7,51:1 |
| Contêiner primário | `#0D2A4D` / `#E3ECF8` | 12,11:1 | Contêiner primário | `#D6E4FA` / `#1D3A63` | 8,89:1 |
| Créditos (âmbar) / fundo | `#8A5A12` / `#FFFFFF` | 5,91:1 | Âmbar / superfície | `#F2C177` / `#171B22` | 10,42:1 |
| Sucesso / fundo | `#166534` / `#FFFFFF` | 7,13:1 | Sucesso / superfície | `#86EFAC` / `#171B22` | 12,30:1 |
| Erro / fundo | `#B42318` / `#FFFFFF` | 6,57:1 | Erro | `#F2B8B5` | 9,23:1 |
| Post Dica | `#3730A3` / `#E0E7FF` | 8,06:1 | Dica | `#C7D2FE` / `#2E2A63` | 8,65:1 |
| Post Procuro | `#115E59` / `#CCFBF1` | 6,73:1 | Procuro | `#99F6E4` / `#123B38` | 9,75:1 |
| Post Doação | `#7C2D12` / `#FFEDD5` | 8,18:1 | Doação | `#FED7AA` / `#45230F` | 10,34:1 |
| Post Troca concluída | `#14532D` / `#DCFCE7` | 8,30:1 | Troca concluída | `#BBF7D0` / `#12381F` | 10,75:1 |

Regra: cor nunca é o único indicador. Estado de pedido, tipo de post e "a pé" sempre têm texto (e ícone).

## 3. Tipografia

| Estilo | Fonte | Tamanho | Uso |
|---|---|---|---|
| `Display` | Inter SemiBold | 28 sp | Saudação do Início, nome no Perfil |
| `Headline` | Inter SemiBold | 22 sp | Título de tela e de seção grande |
| `BookTitleLarge` | Newsreader SemiBold | 24 sp | Título no Livro |
| `BookTitle` | Newsreader SemiBold | 18 sp | Título no card |
| `Title` | Inter SemiBold | 16 sp | Cabeçalho de seção |
| `Body` / `BodySmall` / `Caption` | Inter Regular | 16 / 14 / 13 sp | Texto, metadados |
| `Label` | Inter Medium | 15 sp | Botões, chips |

Newsreader aparece **só em títulos de livro** (o rosto do app). No máximo 3 pesos por tela.

## 4. Espaçamento, forma e profundidade

| Token | Valor | Uso |
|---|---|---|
| Grid | 4 dp | `space_xxs` 4 … `space_xxl` 48 |
| Margem de tela | 16 dp | Todas as telas |
| Alvo de toque | 48 dp | Mínimo |
| `radius_button` | 12 dp | Botões (Nova Mobile) |
| `radius_l` | 16 dp | Cards |
| `radius_social` | 20 dp | Cards do mural (Playful) |
| Pílula | 50% | Busca, chips, coração |
| `elevation_card` | 2 dp (claro) / 0 + borda (escuro) | Cards |
| Capa no carrossel / lista / Livro | 136×204 / 64×96 / altura 280 dp | |

## 5. Componentes

| Componente | Regra |
|---|---|
| Botão principal | Preenchido azul, raio 12, 52 dp; um por tela; no Livro e no Pedido fica numa barra fixa na base |
| Card de livro (carrossel) | Capa grande, selo "A pé · N min" sobre a capa (verde), coração de desejo no canto (48 dp), título em Newsreader, dono e distância |
| Linha de livro (lista) | Capa 64×96, título, autor, distância, modalidade |
| Cartão de confiança | Avatar, nome, curso, **nota em destaque**, trocas concluídas, selos |
| Barra de progresso da troca | 4 etapas: Pedido, Aceito, Encontro, Concluído; etapa atual preenchida em azul com rótulo; lida pelo TalkBack como "Etapa 2 de 4: Aceito" |
| Cartão "Pra você agora" | Ícone em contêiner colorido, frase de ação, seta; cor pelo tipo |
| Post do mural | Raio 20, etiqueta colorida do tipo, capa da obra, texto, reações com micro-animação |
| Campo | Rótulo acima, raio 12, erro com `setError` |
| Estado vazio | Ilustração/ícone, frase, próximo passo como botão |
| Navegação | 4 abas com ícone + rótulo |

### Ícone do app (P-05)

Livro aberto em páginas brancas sobre o azul da UFMS, com marcador âmbar. Adaptive icon vetorial com camada monocromática (`res/mipmap-anydpi`).

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

### 6.0 Briefings v2 (03/11/2026, `/design-taste-frontend` aplicada com a jornada)

A skill pede que cada tela **responda uma pergunta primeiro**, que o maior elemento seja o que responde essa pergunta e que o produto tenha **um elemento que só ele tem**. O elemento próprio do LeiaUFMS é a **capa do livro com o selo "a pé"** (distância real até o ponto seguro). Os briefings v1 abaixo continuam valendo para estados e microcopy; estes substituem a hierarquia.

| Tela | Pergunta que responde primeiro | Maior elemento | Ação principal → secundárias | Hierarquia (cima → baixo) | Estados |
|---|---|---|---|---|---|
| Onboarding (4 telas + preferências) | "Como isso funciona e é seguro?" | Ilustração (ícone grande em contêiner colorido) + frase de 1 linha | **Continuar** → Pular | 1. Troque ou passe adiante. 2. Créditos (doou ganhou 1; sem crédito também pode pedir). 3. Encontro sempre em ponto seguro, confirmação dos dois lados. 4. Localização só nos 10 min antes do encontro e só se você ativar. 5. Curso, o que está lendo e um desejo | Passos pulados podem ser feitos depois no Perfil |
| Início | "O que tem pra mim agora?" | Cartões "Pra você agora" (quando houver) e o carrossel de capas "Perto de você" | Abrir um livro → buscar, **Anunciar livro** | Saudação + créditos (âmbar) → busca em pílula → "Pra você agora" (pedidos esperando você, desejos que apareceram, encontro hoje) → "Perto de você, a pé" (carrossel de capas grandes com selo) → "Para o seu curso" (disciplinas) → "Chegaram agora" (carrossel) → "Todos os livros" (lista com filtros) | Sem localização: o carrossel "Perto de você" vira um convite "Ative a localização para ver o que dá para buscar a pé". Sem nada pra você: o bloco some (não mostra caixa vazia) |
| Livro | "Vale a pena pedir este?" | Capa grande (280 dp) | **Pedir troca / Resgatar** em barra fixa → Salvar nos desejos (coração), Ver perfil do dono | Capa → título/autor → chips (conservação, edição, modalidade) → bloco de local (mapa estático do ponto + "a pé ~N min") → cartão de confiança (nota em destaque) → "Quantos querem" + "Passou por N mãos" → Disciplinas que indicam → Outras cópias → barra fixa | Próprio anúncio, já pedido, encerrado (como v1) |
| Trocas | "Qual troca precisa de mim agora?" | Card da troca ativa com a barra de 4 etapas | Abrir a troca → Recebidos/Enviados | Troca(s) que pedem ação, cada uma com barra de etapas e o próximo passo → histórico compacto | Vazio com convite para Explorar |
| Pedido | "Qual é o próximo passo?" | Barra de 4 etapas + frase do próximo passo | Botão do passo atual em barra fixa → chat, remarcar, cancelar | Livro → etapas → encontro (ponto, hora, localização ao vivo na janela de 10 min) → outra parte → ações | Fora da janela: "A localização aparece 10 min antes do encontro" |
| Mural (estrutura Pinterest) | "O que a turma está lendo e procurando?" | Grade de capas em 2 colunas: a capa é o post | **Novo post** → filtrar por tipo, reagir "Útil" / "Quero" | Chips de filtro (Todos, Dica, Procuro, Doação, Troca concluída) → grade de posts (etiqueta colorida do tipo sobre a capa, livro, texto, autor e tempo, reações em texto) → fim "Você está em dia" | Vazio: "O mural está quieto. Compartilhe uma dica sobre um livro." + Novo post. Post de Doação: "Quero" leva ao anúncio |
| Perfil | "Como a comunidade me vê?" | Cabeçalho azul com foto, nome, nota e créditos | Editar perfil → estante, desejos, conta | Cabeçalho azul → três números (trocas, livros passados adiante, nota) → selos → estante em capas (carrossel) → conta | Sem foto: iniciais |

#### Ajustes de usabilidade (feedback do Pedro, 03/11/2026)

| Tela / elemento | Mudança | Por quê |
|---|---|---|
| Barra inferior | 5 abas: **Início, Desejos, Trocas, Mural, Perfil**; ícone cheio na aba ativa, indicador azul, ponto azul em Trocas quando algo precisa de você | Desejos é central num app de troca; a barra parecia simples demais |
| Faixa do próximo encontro | Fixa acima das abas, em qualquer tela: aparece 60 min antes, fica verde forte a 30 min e azul a 10 min; mostra capa, "Encontro em N min · 23:35", ponto e a distância da outra pessoa ao ponto (RF-41) | A entrega estava escondida em Trocas → Enviados |
| Desejos (aba) | Grade de capas com os disponíveis primeiro (selo verde), resumo "N disponíveis agora", sugestões do mesmo autor ou disciplina | Tornar o desejo um ponto de entrada |
| Início | Novas seções: "Porque você leu X" (mesmo autor ou gênero), "Mais de {gênero}" (o gênero que a pessoa mais lê, deseja ou troca) e "Explore por gênero" | Categorias ligadas ao usuário |
| Todos os livros | Grade de capas em 2 colunas com chips de gênero e modalidade | A lista parecia monótona |
| Mural | Card inteiro na cor da categoria, ícone por tipo, texto em 3 linhas, reações em ícone + número | O olho acha o tipo antes de ler |
| Trocas e Pedido | Ícones no lugar de frases: 👤 quem, 📍 onde (em destaque), 🕐 "Hoje 23:35 · em 18 min"; próximo passo como botão curto ("Confirmar entrega") | Menos leitura para achar onde e quando |
| Resgate | "Resgatar · 1 crédito" só com crédito; sem crédito o botão é "Pedir doação" (a regra RN-CRED-04 continua) | Evitar parecer que precisa de crédito que não tem |

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
