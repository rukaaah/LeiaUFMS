# LeiaUFMS: Design System e Usabilidade

> Fonte única das decisões visuais e de usabilidade. Todo layout XML deve usar os tokens daqui (via `colors.xml`, `themes.xml`, `dimens.xml`), nunca valores soltos.

## 1. Conceito

**"A tranquilidade de um dia de leitura."**

| Princípio | Na prática |
|---|---|
| Calma | Fundo papel creme, poucas cores, nenhuma animação chamativa |
| Clareza | Uma ação principal por tela, sempre no mesmo lugar |
| Confiança | Informação de segurança (ponto seguro, distância, notas) visível antes da decisão |
| Sem ruído social | Nada de contadores grandes, badges vermelhos piscando ou rankings |
| Mobile-first | Uso com uma mão; ação principal na metade inferior da tela |

## 2. Cores

Base: Material 3 (`Theme.Material3.DayNight.NoActionBar`). Contraste calculado pela fórmula de luminância relativa da WCAG 2.x.

### Tema claro

| Token (`colors.xml`) | Hex | Uso | Contraste |
|---|---|---|---|
| `leia_background` | `#FAF7F0` | Fundo das telas (papel) | — |
| `leia_surface` | `#FFFFFF` | Cards, folhas | — |
| `leia_on_background` | `#2B2A26` | Texto principal | 13,43:1 sobre background |
| `leia_on_surface_variant` | `#5E5A52` | Texto secundário | 6,41:1 sobre background |
| `leia_primary` | `#3F6B4F` | Botão principal, aba ativa, links (verde sálvia) | 5,73:1 sobre background |
| `leia_on_primary` | `#FFFFFF` | Texto sobre primary | 6,13:1 |
| `leia_primary_container` | `#E8EFE9` | Chips selecionados, selos | texto principal 12,28:1 |
| `leia_tertiary` | `#8A5A1C` | Créditos, reação "Quero" (âmbar) | 5,51:1 sobre background |
| `leia_on_tertiary` | `#FFFFFF` | Texto sobre tertiary | 5,90:1 |
| `leia_error` | `#B3261E` | Erros | 6,11:1 sobre background |

### Tema escuro

| Token | Hex | Contraste |
|---|---|---|
| `leia_background` | `#1C1B19` | — |
| `leia_surface` | `#262522` | — |
| `leia_on_background` | `#ECE8E0` | 14,08:1 (12,54:1 sobre surface) |
| `leia_on_surface_variant` | `#B8B2A7` | 8,17:1 |
| `leia_primary` | `#9BC4A8` | 8,90:1 sobre background |
| `leia_on_primary` | `#10291A` | 8,02:1 sobre primary |
| `leia_tertiary` | `#E6B36A` | 9,03:1 |
| `leia_error` | `#F2B8B5` | 10,08:1 |

Regra: cor nunca é o único indicador de estado. Status de pedido sempre tem texto + ícone.

## 3. Tipografia

| Estilo | Fonte | Tamanho | Uso |
|---|---|---|---|
| `TextAppearance.Leia.Display` | Literata (serifada) | 28 sp | Título da tela principal |
| `TextAppearance.Leia.BookTitle` | Literata | 20 sp | Título de livro em card e detalhe |
| `TextAppearance.Leia.Title` | Roboto Medium | 18 sp | Cabeçalho de seção |
| `TextAppearance.Leia.Body` | Roboto | 16 sp | Texto corrido |
| `TextAppearance.Leia.Caption` | Roboto | 13 sp | Metadados (distância, curso) |

- Literata é distribuída pelo Google Fonts (licença OFL); usar como fonte baixável ou em `res/font`. Confirmar licença no download.
- Sempre `sp`, nunca `dp`, para texto.
- Altura de linha do corpo ≈ 1,4×.

## 4. Espaçamento, forma e toque

| Token (`dimens.xml`) | Valor |
|---|---|
| `space_xs` / `space_s` / `space_m` / `space_l` / `space_xl` | 4 / 8 / 16 / 24 / 32 dp |
| Margem lateral da tela | 16 dp |
| Raio de card | 12 dp |
| Raio de botão | 24 dp (pílula) |
| Alvo de toque mínimo | 48 × 48 dp |
| Elevação de card | 0 dp com borda 1 dp `leia_on_surface_variant` a 12% (visual de papel, sem sombra) |

## 5. Componentes

| Componente | Base Material | Regra |
|---|---|---|
| Botão principal | `MaterialButton` filled | Um por tela |
| Botão secundário | `MaterialButton` outlined/text | Ações alternativas |
| Card de anúncio | `MaterialCardView` | Capa 64×96 dp, título (BookTitle), autor, distância + chip "a pé", modalidade |
| Campo | `TextInputLayout` outlined | Erro via `setError`, nunca Toast |
| Chips | `Chip` | Filtros (raio, modalidade) e status de estante |
| Navegação | `BottomNavigationView` | Explorar, Mural, Trocas, Perfil, com ícone + rótulo |
| Feedback curto | `Snackbar` | Confirmações ("Anúncio publicado") com "Desfazer" quando possível |
| Confirmação | `MaterialAlertDialogBuilder` | Sair, cancelar pedido, abrir disputa |
| Estado vazio | Ilustração simples + frase + botão | Toda lista |
| Carregando | `CircularProgressIndicator` | Só se a operação passar de ~300 ms |

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

Substitui a auditoria que seria feita pela skill `/web-design-guidelines` (ausente), adaptada para Android. Rodar em toda tela antes de marcar como pronta.

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
