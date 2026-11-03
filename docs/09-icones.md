# LeiaUFMS: Conjunto de ícones próprio

> Feito com a skill `/icon-designer` (03/11/2026), a pedido do Pedro: "ícones que passem todo o significado necessário; retire os textos dos quais o ícone é o suficiente, mantenha o texto de apoio". Substitui os Material Icons que vinham do Android Studio.

Fonte única: `tools/icons/gerar_icones.py` gera os vetores do Android (`app/src/main/res/drawable/ic_*.xml`), as fontes SVG (`docs/icones/svg/`) e a folha de uso (`docs/icones/folha.html` e `folha.png`, com cada ícone a 16, 24 e 48 px no claro e no escuro).

## 1. Regras do conjunto

| Regra | Valor | Por quê |
|---|---|---|
| Grade | 24 × 24, área útil de 20 (2 de respiro) | Mesmo tamanho dos ícones do Android; tudo alinhado às mesmas formas-chave |
| Formas-chave | Círculo de raio 9 a 10, quadrado de 18, retângulo em pé 16 × 20, deitado 20 × 16 | É o que faz o conjunto parecer uma família |
| Traço | 2 em todos, sem exceção | Peso único; a 2 o traço cai inteiro nos pixels em mdpi a xxxhdpi |
| Pontas e junções | Redondas | Combinam com os raios suaves do app (botão 12, card 16) e a leitura calma |
| Raio de canto | 2 nos retângulos (calendário, câmera, cadeado, galeria, balões) | Uma só curvatura em todo o conjunto |
| Estilo | Contorno por padrão; cheio só no estado ativo (abas Início, Mural, Desejos, Perfil; desejo salvo; nota) | Contorno é leve; cheio marca "selecionado" sem precisar de texto |
| Cor | Branco no vetor + `tint` do tema | Funciona no claro e no escuro sem cópias |
| Nome | `ic_<significado>` (ex.: `ic_lightbulb`, `ic_swap`) | Nome semântico, nunca `final_v2` |

## 2. Inventário por família e metáfora escolhida

| Família | Ícones | Metáforas que valem destacar |
|---|---|---|
| Navegação | home, home_filled, swap, forum, forum_filled, favorite, person, back, chevron_right, close, add, search, explore, settings, tune, logout | **Trocas** = duas setas em sentidos opostos (dar e receber). **Mural** = dois balões sobrepostos (conversa da turma). **Desejos** = coração. **Ajustes** = controles deslizantes; **filtros** = funil (antes eram iguais) |
| Livros e leitura | book, library, local_library, bookmark_border, school, donate | **Estante** = lombadas na prateleira, uma inclinada. **Doação** = mão segurando um coração (dar sem receber) |
| Encontro e lugar | place, location_off, my_location, map, walk, schedule, event, history | **A pé** = pessoa andando, o mesmo ícone do selo "A pé · N min" e da faixa do encontro |
| Social e mensagens | chat, comment, send, share, inbox, thumb_up, notifications, notifications_off, new, lightbulb, celebration | **Dica** = lâmpada (antes era o "i" de informação, que não dizia "dica"). **Pedidos pra mim** = bandeja recebendo; **Meus pedidos** = avião de papel saindo. **Útil** = polegar |
| Estado | check_circle, check_small, info, error, star, verified, badge, lock, visibility, wifi_off, gavel | **Disputa** = martelo do moderador. **Conquista** = medalha com fita |
| Mídia e edição | camera, gallery, edit, delete | Universais |

Metáforas marcadas para teste com usuários (podem confundir): `swap` para Trocas e `forum` para Mural. Por isso as abas mantêm o rótulo.

## 3. Onde o ícone substitui o texto

| Lugar | Antes | Agora |
|---|---|---|
| Card do mural | Ícone + nome do tipo ("Dica") | Só o ícone na cor do tipo; os filtros no topo ensinam a relação ícone ↔ nome |
| Reações (Útil, Quero, comentários) | Ícone + número | Igual (o texto completo vai para o TalkBack) |
| Bola do Início | Ícone + rótulo | Só o ícone (descrição para o TalkBack) |
| Abas da barra inferior, abas de Trocas, botões principais | Ícone + texto | Mantêm o texto: são decisões, e o texto é o apoio pedido |

## 4. Checagem feita

- Folha a 16 e 24 px, claro e escuro (`docs/icones/folha.png`): todos distinguíveis no teste de "apertar os olhos"; `donate` e `wifi_off` são os mais densos e ficam só em 24 px ou maior.
- No emulador: barra inferior, mural, post, pedido, Livro, Obra e faixa do encontro.
