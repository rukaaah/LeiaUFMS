# LeiaUFMS: Jornada do aluno e diferencial de usabilidade

> **Status: aprovado e implementado (03/11/2026).** Escrito depois do feedback de que o app ficou "morno, padrão, com abas iguais e sem incentivo". Estrutura das telas pela `/design-md-library` (Airbnb na troca, Pinterest no social) e cores pela `/mobile-design-md-library` (Nova Mobile; meio-termo Playful no mural), nas cores da UFMS. Ver `DESIGN.md`.

## 1. Diferencial em uma frase

**O LeiaUFMS sempre te diz qual é o próximo livro e qual é o próximo passo.** O aluno nunca abre o app e fica sem saber o que fazer: a tela inicial mostra o que mudou para ele, e cada troca mostra em que etapa está e o que falta.

## 2. Base de psicologia (sem manipulação)

| Modelo | O que diz | Como aparece no app | O que **não** fazemos |
|---|---|---|---|
| **Modelo de Fogg** (comportamento = motivação + habilidade + gatilho) | A ação acontece quando a pessoa quer, consegue com facilidade e recebe um gatilho na hora certa | Pedir em 1 toque a partir do card; anunciar pré-preenchido a partir da estante; aviso só em evento relevante (desejo disponível, pedido respondido, encontro amanhã) | Notificação de "sentimos sua falta", alerta sem ação |
| **Efeito de progresso** (goal-gradient) | Ver o progresso aumenta a chance de terminar | Barra de 4 etapas na troca: Pedido → Aceito → Encontro → Concluído, com a etapa atual em destaque | Contagem regressiva que pressiona |
| **Reciprocidade** | Quem recebe tende a devolver | Crédito ganho em destaque e "você já passou adiante N livros"; sugestão de anunciar o que você terminou de ler | Punir quem está sem crédito (pedir continua liberado, RN-CRED-04) |
| **Prova social leve** | Saber que outros querem algo dá valor e confiança | "4 pessoas querem este livro", "passou por 3 mãos", nota do dono | Ranking, seguidores, contador grande, badge vermelho |
| **Conclusão e recompensa** | Fechar um ciclo com um momento positivo consolida o hábito | Tela de troca concluída com o crédito ganho, convite para avaliar e para postar no mural | Pop-ups de conquista em sequência |
| **Redução de incerteza** | Encontro com estranho gera ansiedade | Ponto seguro com nome, distância a pé, nota e trocas do outro antes de pedir | Esconder quem é a outra pessoa |

## 3. Jornada do aluno

| Momento | O que o aluno sente/quer | Tela | O que o app faz |
|---|---|---|---|
| 1. Chega pela primeira vez | "Isso serve pra mim?" | Login/cadastro | Promessa clara: livros da sua disciplina, perto de você, sem dinheiro |
| 2. Primeiro uso | "Por onde começo?" | **Início** | Bloco "Comece por aqui": adicione o que está lendo, um desejo e seu curso; mostra livros a pé |
| 3. Procura um livro | "Tem o que eu preciso?" | Início → Buscar → Livro | Busca por título, autor, ISBN e disciplina; resultado mostra distância e "a pé" |
| 4. Decide pedir | "Vale a pena? É confiável?" | **Livro** | Capa grande, estado, quem anuncia (nota, trocas), ponto seguro, outras cópias e edições, para qual disciplina serve, quantos querem |
| 5. Espera resposta | "Será que vão aceitar?" | **Trocas** | Barra de progresso; prazo de resposta em texto; aviso quando responderem |
| 6. Combina e encontra | "Onde e quando?" | Pedido → Mapa/Chat | Ponto seguro, mensagens prontas, lembrete na véspera |
| 7. Conclui | "Deu certo!" | **Momento de conclusão** | Crédito ganho (doador), convite para avaliar e para contar no mural |
| 8. Termina de ler | "E agora?" | Estante | "Quer passar adiante?" → anúncio pré-preenchido em 1 toque |
| 9. Volta outro dia | "Mudou algo pra mim?" | **Início** | "Pra você agora": desejos que apareceram, pedidos esperando você, novidades da sua disciplina |

## 4. Identidade de cada aba (fim das "abas iguais")

| Aba | Pergunta que responde | Destaque principal | Cor de acento |
|---|---|---|---|
| **Início** (antes Explorar) | "O que tem pra mim agora?" | Carrossel de capas "Perto de você, a pé" + cartões de ação "Pra você agora" | Azul UFMS |
| **Mural** | "O que a turma está lendo?" | Posts com capa grande, tipo do post em etiqueta colorida, reações em texto; termina em "Você está em dia" | Âmbar |
| **Trocas** | "Qual troca precisa de mim?" | Card da troca ativa com barra de 4 etapas no topo; histórico abaixo | Verde-água |
| **Perfil** | "Como a comunidade me vê?" | Cabeçalho colorido com foto, créditos e "livros passados adiante"; estante em capas | Azul escuro |

Cor nunca é o único sinal: toda etiqueta de estado tem texto e ícone, e todos os pares passam de 4,5:1.

## 5. Livro (detalhe) com mais opções

Ação principal: **Pedir troca** ou **Resgatar**. Ações secundárias visíveis, em linha de ícones com rótulo:

- Salvar nos desejos (avisamos quando houver outra cópia)
- Outras cópias e edições compatíveis
- Disciplinas que indicam o livro
- Trajetória do exemplar ("passou por N mãos")
- Ver perfil de quem anuncia

## 6. Critérios das listas do Início (pergunta do Pedro, 03/11)

Todas as listas mostram só anúncios ativos de **outras** pessoas e escondem os que já estão num pedido aceito ou em disputa. Nenhuma usa popularidade ou ranking.

| Lista | Critério | Ordem | Some quando |
|---|---|---|---|
| Pra você agora | Pedidos que esperam sua resposta, encontros a propor/aceitar/de hoje, entregas a confirmar, desejos que já têm cópia | Pela urgência da ação | Não há nada para fazer |
| Perto de você, a pé | Distância até 2 km da sua posição (ponto seguro do anúncio ou posição do anúncio); 80 m/min a pé | Mais perto primeiro | Sem permissão de localização (aparece o convite) |
| Para o seu curso | Livros ligados a disciplinas cujo curso contém a primeira palavra do seu curso | Mais novos primeiro | Curso vazio ou nada indicado |
| Porque você leu X | X = último livro da sua estante (lendo, lido ou passado adiante); mostra o mesmo autor primeiro, depois o mesmo gênero | Autor, depois data | Estante vazia |
| Mais de {gênero} | Gênero que mais aparece na sua estante, nos seus desejos e nos livros que já passaram por você | Mais novos primeiro | Nenhum livro com gênero |
| Explore por gênero | Os 7 gêneros fixos do app | Fixa | Nunca |
| Chegaram agora | Anúncios mais recentes | Mais novos primeiro | Nunca |
| Desejos → Talvez você queira | Mesmo autor de um desejo ou mesma disciplina de um desejo, sem repetir o que já é desejo | Mais novos primeiro | Nada parecido |

## 7. Decisões de 03/11 (Ajustes 2)

| Pedido do Pedro | Decisão | Por quê |
|---|---|---|
| Início em destaque na barra | Bola azul central, dentro da barra (sem sobrar por cima do conteúdo), só ícone | Uma forma única marca "voltar para casa"; dentro da barra para não cobrir botões nem a faixa do encontro |
| Desejos: aba ou coração? | Primeiro virou coração no Início; depois o Pedro preferiu Desejos na barra no lugar de Anunciar, com a página repensada | Desejo é a motivação de voltar (Fogg: gatilho); anunciar já tem o botão no Início |
| Página de desejos | Três perguntas em ordem: "Disponíveis agora" (capas grandes, quantas cópias), "Esperando aparecer" (lista com aviso ligado e "mais N pessoas querem"), "Talvez você queira". Adicionar acontece ali mesmo | Prova social leve sem ranking; a pessoa fica onde tocou |
| Trocas além de um ponto | Número âmbar na aba (quantos pedidos precisam de você); Desejos ganha número verde (quantos já dá para pedir) | Número diz "quanto", a cor diz "o quê"; nunca vermelho |
| Tocar de novo na aba | Volta ao começo da aba | Padrão de apps de abas |
| Encontro aceito na hora | Agora é proposta; o outro aceita ou sugere outro (RF-44) | Os dois lados precisam concordar com local e hora |
| Faixa do encontro | Mostra também "Você chega em ~N min a pé" (ou a distância se passar de 2 km) | Responde "dá tempo?" sem abrir o mapa |
| Post do mural | Página do post com texto inteiro, Útil, Quero, comentários e compartilhar (RF-43) | Ler e conversar sem sair do contexto |
| Compartilhar | Livro, Obra e post pela folha do Android, em texto | Não há site para link; o texto diz como achar no app |
| Notificação do sistema com o app aberto | Cartão do próprio app, no tema, que desce do topo e some em 6 s; fora do app continua a notificação do sistema | Coerência visual e menos interrupção |
| Recebidos/Enviados confusos | "Pedidos pra mim" (bandeja) e "Meus pedidos" (avião), com uma frase explicando cada aba | Nome diz quem pediu para quem |
| Páginas abertas simplistas e seta padrão | Livro e Obra com capa de ponta a ponta e botões redondos próprios (voltar, compartilhar, desejo), sem barra de título; fatos do exemplar em linhas com ícone (estrutura Airbnb no celular) | A capa faz o trabalho visual; a seta própria combina com o resto |
| Ícones | Conjunto próprio com `/icon-designer` (`docs/09-icones.md`); no card do mural só o ícone do tipo | Ícone com significado; texto só onde apoia |

## 8. Próximos passos (depois da aprovação)

1. Copiar `playful-color.md` para a raiz como `DESIGN.md`, adaptado ao azul UFMS, e reescrever `03-design-system.md` §1 a §5.
2. Refazer Início, Livro, Trocas, Perfil e Mural nessa ordem, com a jornada acima como critério de pronto.
3. Auditoria com `/web-design-guidelines` + checklist §9 em cada tela refeita.
