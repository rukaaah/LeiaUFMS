# LeiaUFMS: Documento de Especificação

> Documento exigido na seção 2 do enunciado da Avaliação 3. Este é o arquivo que será exportado em PDF e entregue no AVA junto com o relatório de testes ([`05-relatorio-testes.md`](05-relatorio-testes.md)).

## 1. Identificação

| Item | Valor |
|---|---|
| Disciplina | Programação Mobile, Avaliação 3 |
| Aplicativo | LeiaUFMS |
| Plataforma | Android nativo, Java, template Empty Views Activity |
| Integrante 1 | Pedro Lucas de Souza Cremonini |
| Integrante 2 | Gabriel Mattos de Aquino |
| Integrante 3 | Angelo Antonio Correia de Souza |
| Repositório/pasta | https://github.com/rukaaah/LeiaUFMS |

## 2. Visão geral do software

O **LeiaUFMS** é uma comunidade de troca de livros exclusiva para a UFMS. Só entra quem tem e-mail institucional. Não existe dinheiro no app: o aluno troca um livro por outro, ou passa um livro adiante e ganha 1 crédito, que usa depois para resgatar outro livro. Ter crédito ajuda, mas não é obrigatório: quem não tem pode pedir, e o anunciante decide se aceita.

A troca acontece presencialmente. Por isso a **geolocalização** é o recurso central: o app mostra os livros anunciados perto do aluno, diz se dá para ir a pé e oferece um mapa com pontos de encontro seguros dentro da universidade.

Em volta das trocas existe uma camada social pequena e sem toxicidade: perfil como "cartão de confiança" (curso, semestre, selos, notas, estante pública), lista de desejos que avisa quando o livro aparece, mural restrito a livros com reações simples ("útil" e "quero"), páginas por obra e disciplina, e um chat que só existe dentro de um pedido de troca aceito. Não há seguidores, curtidas em massa, feed infinito nem chat com desconhecidos.

**Utilidade:** reduz o custo de livros acadêmicos, reaproveita exemplares parados e aproxima alunos de cursos e semestres diferentes.

## 3. Papéis de usuários

| Papel | Quem é | Pode | Não pode |
|---|---|---|---|
| **Visitante** (não autenticado) | Quem abriu o app sem sessão | Ver splash, fazer login, criar conta, ver mensagens de ajuda | Acessar qualquer tela interna (anúncios, mapa, perfil, mural, chat) |
| **Aluno** (autenticado) | Cadastrado com e-mail institucional | Editar perfil, senha e foto; anunciar; pedir troca ou resgate; aceitar/recusar pedidos; conversar no chat do pedido; usar o mapa; gerenciar estante e desejos; postar e reagir no mural; avaliar trocas; abrir disputa; sair (logout) | Pedir o próprio anúncio; conversar sem pedido aceito; ficar com saldo negativo; marcar encontro fora de ponto seguro; moderar |
| **Moderador** (autenticado, conta pré-cadastrada) | Conta semeada no banco | Tudo do Aluno + resolver disputas, cadastrar pontos de encontro seguros, marcar edições compatíveis, ocultar posts do mural | Ver senha de qualquer usuário (só existe hash) |

## 4. Requisitos funcionais

Legenda de prioridade: **D** = Deve (exigência do PDF), **De** = Deveria, **P** = Poderia.

### 4.1 Autenticação e sessão

| ID | Requisito | Usuário | Entradas | Processamento | Saídas | Prio |
|---|---|---|---|---|---|---|
| RF-01 | Splash de abertura | Todos | Abertura do app | Verifica sessão válida no Room enquanto a splash nativa está visível, sem atraso artificial | Encaminha para Login (sem sessão) ou Início (com sessão) | D |
| RF-02 | Cadastro | Visitante | Nome, e-mail institucional, curso, semestre (1 a 12), senha, confirmação | Valida campos (RN-AUTH-01 a 04); gera hash BCrypt com salt; grava `usuario` | Conta criada e sessão iniciada; ou mensagem de erro por campo | D |
| RF-03 | Login | Visitante | E-mail, senha | Busca usuário; verifica senha com BCrypt; cria token de sessão (hash no Room) | Tela Início; ou "E-mail ou senha incorretos" (mensagem única, sem revelar qual) | D |
| RF-04 | Sessão ativa | Aluno | Reabertura do app | Valida token e expiração (30 dias) | Entra direto; sessão expirada volta ao login com aviso | D |
| RF-05 | Logout | Aluno | Toque em "Sair" + confirmação | Apaga token no Room e no `SharedPreferences`; limpa a pilha de telas | Tela de login; botão Voltar não reabre telas internas | D |

**Regras**

- RN-AUTH-01: e-mail deve terminar em `@ufms.br` (constante `AppConfig.ALLOWED_DOMAIN`), sem diferenciar maiúsculas.
- RN-AUTH-02: e-mail único no banco.
- RN-AUTH-03: senha com 8 a 64 caracteres, pelo menos 1 letra e 1 número.
- RN-AUTH-04: nome com 2 a 80 caracteres; curso obrigatório.
- RN-AUTH-05: senha nunca é gravada, logada ou exibida. Só o hash BCrypt (custo 12).

### 4.2 Perfil (Cartão de Confiança)

| ID | Requisito | Usuário | Entradas | Processamento | Saídas | Prio |
|---|---|---|---|---|---|---|
| RF-06 | Ver perfil | Aluno | Toque na aba Perfil ou no nome de outro aluno | Calcula métricas (trocas concluídas, média de notas, saldo de créditos só no próprio perfil) | Cartão: foto, nome, curso, semestre, status, selos, métricas, estante pública | D |
| RF-07 | Editar dados | Aluno | Nome, curso, semestre, status (máx. 80 caracteres), e-mail | Valida com as mesmas regras do cadastro; e-mail novo deve ser único e institucional | "Perfil atualizado"; dados persistidos no Room | D |
| RF-08 | Alterar senha | Aluno | Senha atual, nova, confirmação | Verifica a atual com BCrypt; valida RN-AUTH-03; gera **novo hash com novo salt** | "Senha alterada"; erro se a atual estiver errada | D |
| RF-09 | Foto pela câmera | Aluno | Toque em "Tirar foto" | Pede permissão `CAMERA`; abre câmera via `TakePicture` + `FileProvider`; salva em `filesDir`; grava caminho em `usuario.fotoPath` | Foto nova no perfil; se negar, explicação + atalho para Configurações | D |
| RF-10 | Foto pela galeria | Aluno | Toque em "Escolher da galeria" | Photo Picker (`PickVisualMedia`); copia a imagem para `filesDir`; grava caminho | Foto nova; cancelar não altera a foto atual | D |
| RF-11 | Selos | Sistema | Conclusão de trocas/doações | Doador: ≥ 3 doações; Veterano: ≥ 10 trocas (PC-09) | Selo no perfil + notificação ao ganhar | P |

### 4.3 Anúncios, obras e exemplares

| ID | Requisito | Usuário | Entradas | Processamento | Saídas | Prio |
|---|---|---|---|---|---|---|
| RF-12 | Anunciar livro | Aluno | Título, autor, ISBN (opcional, 10 ou 13 dígitos), edição, estado de conservação, modalidade (Troca, Doação ou ambas), foto do exemplar (opcional), local (atual ou ponto seguro preferido) | Vincula a uma `obra` existente (por ISBN ou título+autor) ou cria uma; cria `exemplar` e `anuncio` ativo; dispara RF-27 | Anúncio visível em Explorar e no hub da obra | D |
| RF-13 | Editar/encerrar anúncio | Dono | Campos do anúncio | Bloqueado se houver pedido aceito | Anúncio atualizado ou removido da busca | D |
| RF-14 | Explorar anúncios | Aluno | Busca por título/autor/ISBN, filtro de modalidade e raio | Consulta Room; ordena por distância quando houver localização | Lista com capa, título, distância, "a pé" ou não | D |
| RF-15 | Hub da obra | Aluno | Toque no título | Agrupa anúncios da mesma obra; lista edições compatíveis e disciplinas que recomendam | Página da obra | P |
| RF-16 | Hub da disciplina | Aluno | Busca por disciplina | Lista obras recomendadas para a disciplina | Página da disciplina | P |
| RF-17 | Trajetória do exemplar | Aluno | Toque em "Histórico" no anúncio | Lê `posse_exemplar` | "Este exemplar passou por N mãos" + linha do tempo (curso/semestre, sem dados pessoais além do nome) | P |

### 4.4 Pedidos, trocas e créditos

**Estados do pedido:** `PENDENTE → ACEITO → CONCLUIDO`; desvios: `RECUSADO`, `CANCELADO`, `EXPIRADO`, `EM_DISPUTA`.

| ID | Requisito | Usuário | Entradas | Processamento | Saídas | Prio |
|---|---|---|---|---|---|---|
| RF-18 | Pedir troca direta | Aluno | Anúncio + exemplar próprio oferecido | Valida RN-TROCA-01 a 03; cria pedido `PENDENTE` | Notificação para o dono; pedido em "Minhas trocas" | D |
| RF-19 | Pedir resgate (doação) | Aluno | Anúncio de doação | Permitido com qualquer saldo. Saldo disponível ≥ 1: **reserva** 1 crédito. Saldo 0: pedido segue sem reserva, marcado "sem crédito" | Notificação para o dono com o saldo do solicitante; saldo disponível atualizado quando houver reserva | D |
| RF-20 | Responder pedido | Dono | Aceitar ou recusar (vendo saldo, notas e trocas do solicitante) | Aceitar: abre o chat, recusa automaticamente outros pedidos do mesmo anúncio e libera reservas deles. Recusar: libera reserva | Notificação para o solicitante | D |
| RF-21 | Marcar encontro | Partes | Ponto seguro (mapa ou lista), data e hora | Ponto precisa estar na tabela `ponto_encontro`; data no futuro e dentro do prazo PC-04 | Encontro no pedido + mensagem automática no chat | D |
| RF-22 | Concluir troca | Partes | "Entreguei" / "Recebi" | Quando os dois confirmam: muda posse dos exemplares, grava `posse_exemplar`, lança créditos (doador +1 sempre; resgatador consome a reserva, se houver) | Notificação; post automático **opcional** no mural; convite para avaliar | D |
| RF-23 | Cancelar pedido | Partes | "Cancelar" + motivo | Permitido antes da conclusão; libera reservas | Notificação para a outra parte | D |
| RF-24 | Expiração automática | Sistema | Verificação ao abrir o app e via WorkManager periódico | PENDENTE > 72 h → `EXPIRADO`; ACEITO > 7 dias → `CANCELADO` (PC-03, PC-04) | Notificação de pendência/expiração | De |
| RF-25 | Avaliar troca | Partes | Nota 1 a 5 | Uma avaliação por lado por pedido | Média atualizada no perfil | De |
| RF-26 | Disputa | Partes / Moderador | "Abrir disputa" + motivo / decisão | Pedido vai a `EM_DISPUTA`; créditos congelados; moderador conclui ou cancela | Notificação às partes | P |

**Regras**

- RN-TROCA-01: não é possível pedir o próprio anúncio.
- RN-TROCA-02: no máximo 1 pedido ativo por anúncio por usuário e 5 pedidos pendentes por usuário (PC-06).
- RN-TROCA-03: o exemplar oferecido não pode estar em outro pedido aceito.
- RN-CRED-01: saldo = soma de `movimento_credito`; saldo disponível = saldo − reservas ativas.
- RN-CRED-02: crédito só é criado na conclusão confirmada pelos dois lados.
- RN-CRED-03: usuário novo começa com 0 créditos (PC-01).
- RN-CRED-04: crédito **não** é pré-requisito para pedir; a decisão é do anunciante.
- RN-CRED-05: o saldo nunca fica negativo; sem saldo, o resgate concluído não debita nada.

### 4.5 Geolocalização (recurso obrigatório escolhido)

| ID | Requisito | Usuário | Entradas | Processamento | Saídas | Prio |
|---|---|---|---|---|---|---|
| RF-27 | Livros próximos | Aluno | Toque em "Perto de mim" + raio (500 m, 1 km, 2 km, 5 km) | Pede `ACCESS_FINE_LOCATION`/`COARSE`; obtém posição atual sob demanda; calcula distância até cada anúncio | Lista ordenada por distância; sem permissão, lista sem distância e aviso | D |
| RF-28 | Viabilidade a pé | Aluno | Abrir um anúncio | Distância em linha reta ≤ 2 km → "Dá para ir a pé (~N min)"; tempo = distância ÷ 80 m/min (PC-08) | Selo "a pé" ou "Longe para ir a pé", com aviso de que é estimativa em linha reta | D |
| RF-29 | Mapa de pontos seguros | Partes | Abrir "Marcar encontro" | Mapa MapLibre com marcadores dos pontos cadastrados e posição do usuário (se permitida) | Seleção de um ponto; sem internet, lista dos pontos como alternativa | D |
| RF-30 | Cadastrar ponto seguro | Moderador | Nome, descrição, toque no mapa | Valida nome e coordenadas | Novo ponto disponível para todos | P |

### 4.6 Notificações

| ID | Evento | Destinatário | Prio |
|---|---|---|---|
| RF-31 | Novo pedido, aceite, recusa, cancelamento, conclusão | Partes do pedido | D |
| RF-32 | Livro da lista de desejos anunciado | Quem deseja a obra | D |
| RF-33 | Pendências: pedido sem resposta, encontro amanhã, prazo de entrega no 5º dia, avaliação pendente | Partes | D |
| RF-34 | Permissão de notificação negada | Aluno | D |

- Canais: `trocas`, `desejos`, `pendencias`.
- Android 13+: pede `POST_NOTIFICATIONS` na primeira ação relevante (não na abertura do app).
- Se negada (RF-34): o app continua funcionando, os avisos ficam na **Caixa de avisos** interna e um banner discreto oferece reativar nas Configurações.
- Destinatário não logado: aviso gravado em `notificacao` e entregue no próximo login (ADR-01).

### 4.7 Camada social

| ID | Requisito | Usuário | Entradas | Processamento | Saídas | Prio |
|---|---|---|---|---|---|---|
| RF-35 | Estante | Aluno | Adicionar obra como "Estou lendo", "Quero ler" (desejo) ou "Passei adiante" (automático) | Grava `item_estante` | Estante pública no perfil | De |
| RF-36 | Terminei de ler | Aluno | Mudar "Estou lendo" para "Lido" | Pergunta "Quer passar este livro adiante?" | Sim → formulário de anúncio pré-preenchido | De |
| RF-37 | Lista de desejos | Aluno | Adicionar obra | Grava `desejo`; RF-32 dispara quando houver anúncio da obra | Lista com indicação "disponível" | De |
| RF-38 | Mural | Aluno | Post do tipo Dica, Procuro, Doação ou Troca concluída (texto até 500 caracteres + obra vinculada **obrigatória**) | Rejeita post sem obra; aplica janela PC-11 | Feed finito que termina em "Você está em dia" | P |
| RF-39 | Reações | Aluno | "Útil" ou "Quero" | Uma de cada tipo por usuário por post; "Quero" em post de Doação leva ao anúncio | Contagem discreta (sem ranking) | P |
| RF-40 | Chat do pedido | Partes | Mensagens prontas ("Posso no RU às 12h?", "Cheguei", "Vou me atrasar 10 min", "Podemos remarcar?") ou texto até 300 caracteres | Só existe com pedido `ACEITO`; somente leitura após encerramento | Conversa dentro do pedido; notificação de nova mensagem | De |

## 5. Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Android nativo em Java; minSdk 26 sugerido, targetSdk na versão estável atual (confirmar no Android Studio) |
| RNF-02 | Banco local Room; nenhum dado sai do aparelho além do download de tiles do mapa |
| RNF-03 | Nenhuma operação de banco ou disco na main thread |
| RNF-04 | O app não pode encerrar (crash) com entrada inválida, permissão negada, falta de GPS ou de internet |
| RNF-05 | Textos em `strings.xml` (pt-BR); nenhum texto fixo no layout |
| RNF-06 | Alvos de toque ≥ 48 dp; contraste ≥ 4,5:1; `contentDescription` em ícones e imagens |
| RNF-07 | Localização somente sob demanda, sem rastreamento em segundo plano |

## 6. Conceitos aplicados e diferenciais

### Foco principal: Usabilidade

**Justificativa:** o LeiaUFMS depende de pessoas desconhecidas confiarem umas nas outras para um encontro presencial. Cada passo confuso (onde encontrar, quanto falta, o que fazer agora) vira troca abandonada. Usabilidade é o foco que mais afeta a taxa de trocas concluídas.

| Prática | Onde aparece |
|---|---|
| Navegação consistente: 4 abas fixas (Explorar, Mural, Trocas, Perfil) | Todo o app |
| Próximo passo explícito em cada pedido ("Aguardando resposta", "Marque o encontro", "Confirme a entrega") | Tela do pedido |
| Erros por campo, em linguagem simples, com como corrigir | Formulários |
| Estados vazios que ensinam ("Sua estante está vazia. Adicione o livro que está lendo.") | Listas |
| Confirmação antes de ação destrutiva (sair, cancelar pedido) | Diálogos |
| Fallback sem permissão/sem internet em vez de tela quebrada | Mapa, distância, notificações |
| Mensagens prontas no chat para combinar logística em 1 toque | Chat |

### Cuidados básicos (não são foco, mas são atendidos)

- **Acessibilidade:** contraste AA medido, `contentDescription`, tamanhos em `sp`, ordem de foco lógica para TalkBack.
- **Economia de energia:** localização sob demanda com prioridade balanceada, sem `requestLocationUpdates` contínuo, verificação de prazos agrupada em WorkManager.
