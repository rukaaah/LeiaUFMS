# LeiaUFMS: Design System e Usabilidade

> **Status: v2, redefinido em 03/11/2026** depois do feedback do grupo ("morno, padrão, abas iguais"). A v1 (tudo plano) foi **substituída**. Organização das telas: troca no modelo de marketplace de confiança (capa primeiro, cartão de confiança, barra de ação fixa); social em grade de 2 colunas em que a capa é o post. Sempre nas cores da UFMS. A jornada e a base de psicologia estão em [`08-jornada-e-usabilidade.md`](08-jornada-e-usabilidade.md); o checklist de auditoria de UI está no §9.

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
| Social vivo, sem toxicidade | Etiqueta colorida por tipo de post, reações Coração (com número) e Salvar (sem número) com micro-animação; nada de ranking, seguidores ou badge vermelho |

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

Adaptado para Android. Rodar em toda tela antes de marcar como pronta.

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
