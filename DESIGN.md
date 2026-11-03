# DESIGN.md: LeiaUFMS (v2, 03/11/2026)

> **Papel de cada skill (definido pelo Pedro em 03/11/2026):**
> - `/design-md-library` = **organização, modelos e lógica** das telas. Troca (Início, Livro, Trocas, Perfil): **Airbnb** (marketplace de confiança, capa primeiro, cartão de confiança, barra de ação fixa, coração de desejo). Social (Mural): **Pinterest** (a capa é o post, grade de 2 colunas, "Quero" como o "salvar", chips de filtro). O **mydealz** foi avaliado e descartado porque seu modelo é votação quente/frio (ranking, proibido no projeto).
> - `/mobile-design-md-library` = **cores e combinações**. **Nova Mobile** no app todo; no Mural, as cores por tipo de post vêm do meio-termo com o **Playful Color**.
> - Cores da UFMS (azul e branco). Detalhes, contraste e briefings em `docs/03-design-system.md`; jornada em `docs/08-jornada-e-usabilidade.md`.

## 1. Princípios

- **A troca é a protagonista.** Início, Livro e Trocas têm a hierarquia mais forte; o social vem depois, bem estruturado.
- **Capa primeiro** (Airbnb: foto primeiro). A capa real do livro é o elemento visual dominante de card e detalhe.
- **Sempre um próximo passo.** Toda tela responde "o que eu faço agora?" com uma ação principal clara.
- **Confiança antes do pedido.** Nota, trocas, distância e ponto seguro aparecem antes do botão.
- **Uma cor de marca.** Azul UFMS em ações e destaques; âmbar só para créditos; verde para "a pé" e "concluído".
- **Social vivo, sem toxicidade.** Cor e ícone por tipo de post, reações em ícone + número, comentários sem curtida, sem ranking, seguidores ou badge vermelho.

## 2. Cores

| Papel | Claro | Escuro |
|---|---|---|
| Fundo | `#FFFFFF` | `#0E1116` |
| Superfície / card | `#FFFFFF` (sombra suave) | `#171B22` (borda fina, sem sombra) |
| Superfície suave | `#F4F6FA` | `#1E232C` |
| Texto | `#111827` | `#F3F4F6` |
| Texto secundário | `#4B5563` | `#A8B0BD` |
| Primária (azul UFMS) | `#1B4F8F` | `#8DB8F2` |
| Contêiner primário | `#E3ECF8` | `#1D3A63` |
| Créditos (âmbar) | `#8A5A12` / `#FDF0D5` | `#F2C177` / `#4A3615` |
| Sucesso ("a pé", concluído) | `#166534` / `#DCFCE7` | `#86EFAC` / `#12381F` |
| Erro | `#B42318` | `#F2B8B5` |
| Post Dica / Procuro / Doação / Troca concluída | índigo / verde-água / laranja / verde (contêiner + texto, ver `colors.xml`) | versões escuras |

Todos os pares de texto ≥ 5,9:1.

## 3. Tipografia

- **Inter** (OFL) para a interface: Display 28 sp SemiBold, Título de tela 22 sp SemiBold, Título 16 sp SemiBold, Corpo 16 sp, Pequeno 14 sp, Legenda 13 sp.
- **Newsreader** (OFL) só em títulos de livro: é o "rosto" do LeiaUFMS.
- No máximo 3 pesos por tela.

## 4. Espaço, forma e profundidade

- Grid de 4 dp; margem de tela 16 dp; alvo de toque ≥ 48 dp.
- Raios: botão 12 dp, card 16 dp, card social 20 dp, busca em pílula, avatar e coração circulares.
- Profundidade: uma camada de sombra suave (2 dp) em cards no claro; no escuro, sem sombra.

## 5. Navegação

- 5 posições: **Trocas, Mural, Início (bola azul central, só ícone), Desejos, Perfil**; ícone cheio na aba ativa; número âmbar em Trocas (pedidos que precisam de você) e verde em Desejos (já disponíveis); tocar de novo na aba volta ao começo dela.
- Livro e Obra sem barra de título: capa de ponta a ponta com botões redondos (voltar, compartilhar, desejo).
- Aviso do app no tema (cartão que desce do topo) com o app aberto; notificação do sistema só em segundo plano.
- Ícones próprios: grade 24, traço 2, pontas redondas, contorno com cheio no ativo (`docs/09-icones.md`).
- Faixa fixa do próximo encontro acima das abas (60/30/10 min), em qualquer tela.
- Pilha para detalhes; barra de ação fixa na base do Livro e do Pedido (como a barra de reserva da Airbnb).

## 6. Componentes-chave

- **Card de livro em carrossel:** capa 136×204 dp com selo "a pé" sobre a capa e coração de desejo no canto, título em Newsreader, distância e dono abaixo.
- **Linha de livro em lista:** capa 64×96 dp, título, autor, distância e modalidade.
- **Cartão de confiança:** avatar, nome, curso, nota grande (o número mais forte da tela), trocas concluídas.
- **Barra de progresso da troca:** 4 etapas (Pedido, Aceito, Encontro, Concluído) com ponto, rótulo e linha.
- **Cartão de ação "Pra você agora":** ícone em contêiner colorido, frase de ação, seta; cor pelo tipo (pedido = azul, desejo = âmbar, encontro = verde).
- **Post do mural (estrutura Pinterest):** grade de 2 colunas em que a capa da obra é o card (raio 20 dp), etiqueta colorida do tipo sobre a capa, texto curto, autor, coração com número e salvar (marcador, sem número; o "salvar" do Pinterest) com micro-animação; chips de filtro por tipo no topo; lista finita (14 dias, 30 itens) terminando em "Você está em dia".

## 7. Movimento

150 a 300 ms, ease-in-out (Android). Toque com leve escala 0,98. Reações com pequeno "salto" (Playful). Sem animação contínua.

## 8. Do / Don't

- **Do:** capa real sempre que existir; um botão principal por tela; texto + ícone em todo estado.
- **Don't:** texto que repete o que a tela já mostra (ex.: "Combinado pelos dois", "10 min antes", resumo de contagem visível, botão "+" explicado na aba Desejos); a mesma informação em dois lugares da mesma tela (faixa do encontro some dentro do próprio pedido e o Início não repete o encontro da faixa); abas com a mesma cara; listas sem destaque; contador vermelho; mais de 2 cores de acento por tela fora do mural.
