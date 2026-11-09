# LeiaUFMS: Documento de Especificação

Programação Mobile, Avaliação 3. Aplicativo Android nativo em Java (template Empty Views Activity), com banco Room.

## 1. Identificação

| Integrante |
|---|
| Pedro Lucas de Souza Cremonini |
| Gabriel Mattos de Aquino |
| Angelo Antonio Correia de Souza |

Repositório: https://github.com/rukaaah/LeiaUFMS

## 2. Visão geral do software

O LeiaUFMS é uma comunidade de troca de livros exclusiva para alunos da UFMS. Só entra quem tem e-mail `@ufms.br` e não existe dinheiro no app. O aluno troca um livro por outro ou doa um livro e ganha 1 crédito, que usa depois para resgatar outro livro. Quem não tem crédito também pode pedir: o anunciante vê o saldo e decide se aceita.

A entrega é presencial, em pontos seguros do campus. Por isso o recurso obrigatório escolhido é a **geolocalização**: o app mostra livros perto do aluno, diz se dá para ir a pé e oferece um mapa com os pontos de encontro. O app também consome a **API HTTP da Open Library** para capas e busca de livros.

Em volta das trocas há uma camada social pequena: perfil com selos e notas, estante, lista de desejos que avisa quando o livro aparece, mural restrito a livros e um chat que só existe dentro de um pedido aceito. Não há seguidores, ranking nem feed infinito.

**Utilidade:** reduz o custo de livros acadêmicos, reaproveita exemplares parados e aproxima alunos de cursos diferentes.

## 3. Papéis de usuários

| Papel | Quem é | Pode | Não pode |
|---|---|---|---|
| **Visitante** (não autenticado) | Quem abre o app sem sessão | Ver a splash, entrar, criar conta | Acessar qualquer tela interna |
| **Aluno** (autenticado) | Cadastrado com e-mail `@ufms.br` | Editar perfil, senha e foto; anunciar; pedir troca ou resgate; responder pedidos; marcar encontro; conversar no chat do pedido; usar o mapa; gerenciar estante e desejos; postar e reagir no mural; avaliar; abrir disputa; sair | Pedir o próprio anúncio; conversar sem pedido aceito; marcar encontro fora de ponto seguro; moderar |
| **Moderador** (autenticado, conta pré-cadastrada) | Conta `moderacao@ufms.br` criada na instalação | Tudo do Aluno, mais decidir disputas, cadastrar pontos seguros no mapa e ocultar posts | Ver senhas (só existe o hash) |

## 4. Requisitos funcionais

### 4.1 Autenticação, sessão e perfil

| ID | Ação | Usuário | Entradas | Processamento | Saídas |
|---|---|---|---|---|---|
| RF-01 | Abrir o app | Todos | Toque no ícone | Splash nativa (`core-splashscreen`) visível enquanto a sessão é verificada no Room, sem atraso artificial | Login (sem sessão) ou Início (com sessão) |
| RF-02 | Criar conta | Visitante | Formulário: nome, e-mail, curso (lista oficial da UFMS), semestre (1 a 12), senha e confirmação | Valida cada campo; e-mail único e terminado em `@ufms.br`; senha com 8+ caracteres, letra e número; gera **hash BCrypt com salt** (custo 12); grava no Room | Conta criada e sessão aberta; ou erro embaixo de cada campo inválido |
| RF-03 | Entrar | Visitante | E-mail e senha | Confere a senha com BCrypt; cria token aleatório de sessão (Room guarda só o hash SHA-256 do token, válido por 30 dias) | Início; ou "E-mail ou senha incorretos." |
| RF-04 | Manter sessão | Aluno | Reabrir o app | Valida token e expiração; toda tela interna revalida a sessão | Entra direto; sessão expirada volta ao login |
| RF-05 | Sair | Aluno | Toque em "Sair" e confirmação | Apaga o token no Room e no `SharedPreferences`; limpa a pilha de telas | Login; o botão Voltar não reabre telas internas |
| RF-06 | Editar perfil | Aluno | Nome, e-mail, curso, semestre, status | Mesmas validações do cadastro; grava no Room | "Perfil atualizado" |
| RF-07 | Alterar senha | Aluno | Senha atual, nova e confirmação | Confere a atual; gera **novo hash com novo salt**; a senha original nunca é gravada | "Senha alterada"; ou erro na senha atual |
| RF-08 | Trocar foto de perfil | Aluno | Toque em "Tirar foto" (câmera) ou "Escolher da galeria" | Pede a permissão `CAMERA` na hora do uso; galeria pelo Photo Picker (sem permissão); copia a imagem para o armazenamento interno e grava o caminho no perfil (Room) | Foto nova no perfil; câmera negada mostra explicação e, se negada de vez, botão "Abrir configurações" |

### 4.2 Anúncios e trocas

| ID | Ação | Usuário | Entradas | Processamento | Saídas |
|---|---|---|---|---|---|
| RF-09 | Anunciar livro | Aluno | Título (com sugestões do catálogo), autor, ISBN opcional, conservação, modalidade (troca, doação ou ambas), foto opcional, ponto seguro | Liga o anúncio à obra do catálogo (mesmo livro, mesma obra); grava exemplar e anúncio; avisa quem deseja a obra | "Anúncio publicado"; notificação "Um livro que você deseja chegou" para os interessados |
| RF-10 | Buscar livros | Aluno | Texto (título, autor, ISBN), filtro de modalidade | Consulta o Room | Lista com capa, título e distância |
| RF-11 | Pedir troca ou resgate | Aluno | Toque em "Pedir troca" (escolhendo um exemplar seu) ou "Pedir doação" | Bloqueia pedido do próprio anúncio e mais de 5 pendentes; no resgate com saldo ≥ 1 reserva 1 crédito; com saldo 0 marca "sem crédito" | "Pedido enviado"; notificação para o dono com o saldo do solicitante |
| RF-12 | Responder pedido | Dono do anúncio | "Aceitar" ou "Recusar" | Aceitar abre o chat e recusa os outros pedidos do anúncio; recusar libera a reserva | Notificação para o solicitante |
| RF-13 | Combinar encontro | Partes do pedido | Ponto seguro no mapa ou na lista, data e hora | Só pontos cadastrados e horário futuro; a outra parte aceita ou sugere outro horário | "Encontro combinado."; faixa fixa com a contagem até o encontro; lembrete na véspera |
| RF-14 | Concluir troca | Partes do pedido | "Entreguei o livro" e "Recebi o livro" | Com as duas confirmações, numa transação: muda a posse do exemplar, consome a reserva e dá +1 crédito ao doador; o saldo nunca fica negativo | Pedido "Concluído"; convite para avaliar |
| RF-15 | Cancelar, expirar, disputar | Partes / Sistema / Moderador | "Cancelar" ou "Abrir disputa" com motivo; decisão do Moderador | Pendente há 72 h expira; aceito há 7 dias é cancelado (WorkManager a cada 6 h e ao abrir o app); disputa congela créditos até a decisão | Notificação às partes; reserva devolvida quando o pedido não se conclui |
| RF-16 | Avaliar troca | Partes | Nota de 1 a 5 | Uma avaliação por lado por pedido | "Obrigado pela avaliação"; média no perfil |
| RF-17 | Conversar no chat | Partes | Mensagem pronta ou texto até 300 caracteres | Só com pedido aceito; somente leitura depois de encerrado | Mensagens no pedido; notificação de nova mensagem |

### 4.3 Geolocalização e API (recursos obrigatórios)

| ID | Ação | Usuário | Entradas | Processamento | Saídas |
|---|---|---|---|---|---|
| RF-18 | Ver livros perto de mim | Aluno | Toque em "Perto de mim" e raio (500 m a 5 km) | Pede localização na hora do uso; obtém a posição sob demanda (prioridade balanceada); calcula a distância até cada anúncio | Lista ordenada por distância; "Dá para ir a pé (~N min)" até 2 km; sem permissão, lista sem distância e aviso; GPS desligado, pedido para ligar |
| RF-19 | Mapa de pontos seguros | Partes / Moderador | Abrir "Propor encontro"; Moderador toca no mapa para cadastrar ponto | Mapa MapLibre com os pontos e a posição do aluno | Ponto escolhido; sem internet, lista de pontos no lugar do mapa |
| RF-20 | Mostrar posição no encontro | Partes | Ligar "Mostrar onde estou" | Só com encontro combinado, de 10 min antes a 30 min depois; leitura com o app aberto, sem rastreio em segundo plano | A outra parte vê "N m do ponto · atualizado há N min" |
| RF-21 | Capas e busca na Open Library | Sistema | Nova obra cadastrada; título digitado com poucas sugestões locais | Requisição HTTP à API da Open Library; processa o JSON; guarda a capa no aparelho; prefere edição em português | Capa real nos cards; sugestões de livros; sem internet, iniciais do título e só o catálogo local |

### 4.4 Camada social e avisos

| ID | Ação | Usuário | Entradas | Processamento | Saídas |
|---|---|---|---|---|---|
| RF-22 | Estante | Aluno | Adicionar livro como "Estou lendo"; tocar em "Terminei de ler" | Grava no Room; ao terminar, pergunta se quer passar adiante | Estante pública no perfil; "Anunciar" abre o anúncio já preenchido |
| RF-23 | Desejos | Aluno | "Adicionar desejo" com o título | Grava o desejo; dispara aviso quando a obra for anunciada | Listas "Disponíveis agora" e "Esperando aparecer" |
| RF-24 | Mural | Aluno / Moderador | Post (dica, procura, doação, troca concluída) com livro obrigatório; coração, salvar, comentário | Um coração e um salvar por pessoa (tocar de novo desfaz); mural finito; Moderador pode ocultar | Feed que termina em "Você está em dia"; filtro "Salvos"; aviso de comentário para quem postou |
| RF-25 | Notificações | Sistema | Eventos de pedido, desejo, encontro e prazo | Canais `trocas`, `desejos` e `pendencias`; pede `POST_NOTIFICATIONS` (Android 13+) na primeira ação relevante; aviso para outra conta é guardado e entregue no login dela | Notificação do sistema; se negada, aviso na Caixa de avisos com o botão "Ativar" |

## 5. Foco escolhido: Usabilidade

**Justificativa:** a troca depende de dois desconhecidos se encontrarem pessoalmente. Cada dúvida (onde, quando, o que falta) vira troca abandonada, então a usabilidade é o que mais afeta o número de trocas concluídas.

| Prática | Onde aparece |
|---|---|
| Navegação fixa em 5 abas (Início, Desejos, Trocas, Mural, Perfil) | Todo o app |
| Barra de 4 etapas (Pedido, Aceito, Encontro, Concluído) com o próximo passo | Tela do pedido |
| Erro embaixo de cada campo, em linguagem simples | Formulários |
| Estados vazios que dizem o que fazer | Listas |
| Confirmação antes de sair, cancelar ou descartar edição | Diálogos |
| Alternativa quando falta permissão ou internet, sem fechar o app | Mapa, distância, câmera, notificações |
| Mensagens prontas para combinar o encontro em um toque | Chat |

Acessibilidade e economia de energia não são focos, mas têm os cuidados básicos: `contentDescription` em ícones e capas, contraste mínimo de 4,5:1, alvos de 48 dp, textos em `sp`; localização só sob demanda e tarefas periódicas só com bateria não baixa.

## 6. Testes caixa-preta

Os 24 casos foram executados em 09/11/2026 e todos foram aprovados. O passo a passo completo e o resultado de cada caso estão em `docs/05-relatorio-testes.md`.

| Item | Valor |
|---|---|
| Dispositivo | Emulador Pixel_10 (Android Studio) |
| Android | Android 17 (API 37) |
| Versão do app | commit `eaa35bb` |
| Data | 09/11/2026 |
| Executado por | Gabriel Mattos de Aquino |
| Contas | Contas de demonstração com a senha `Leia2026` |

### Casos executados

| ID | Situação | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-01 | Campos | Tela Criar conta | Enviar com tudo vazio | Erro em cada campo obrigatório; app não fecha | Erro embaixo de cada campo ("Preencha o seu nome." etc.); app não fechou | Aprovado |
| CT-02 | Campos | Tela Criar conta | E-mail `@gmail.com`, semestre 99, senha "abc" | Erros de e-mail institucional, semestre e senha; conta não criada | Os três erros esperados; conta não criada; app não fechou | Aprovado |
| CT-03 | Campos | Carla, pedido aceito | Mensagem de 301 caracteres no chat | Texto limitado a 300; mensagem curta é enviada | Campo parou em 300 (contador "300/300"); mensagem curta enviada | Aprovado |
| CT-04 | Autenticação | Deslogado | Cadastro válido com `teste.parceiro@ufms.br` | Conta criada; apresentação e Início | Conta criada; apresentação de 5 telas e Início com "Olá, Nome" | Aprovado |
| CT-05 | Autenticação | Deslogado | Login da Carla com senha correta | Abre o Início | Início com "Olá, Carla" | Aprovado |
| CT-06 | Autenticação | Deslogado | Senha errada; e-mail inexistente | "E-mail ou senha incorretos." nos dois | "E-mail ou senha incorretos." nas duas tentativas | Aprovado |
| CT-07 | Perfil | Logado (CT-04) | Editar nome e curso; fechar e reabrir o app | "Perfil atualizado"; dados novos persistem e a sessão continua | "Perfil atualizado"; após reabrir, entrou direto com nome e curso novos | Aprovado |
| CT-08 | Logout | Logado | Sair; apertar Voltar; reabrir | Nenhuma tela interna aparece; app pede login | Voltou ao login; Voltar fechou o app; ao reabrir pediu login | Aprovado |
| CT-09 | Câmera | Permissão sem decisão | "Tirar foto" e Permitir | "Foto atualizada" | "Foto atualizada" e foto no perfil | Aprovado |
| CT-10 | Câmera | Permissão sem decisão | Negar duas vezes | Explicação; depois botão "Abrir configurações"; app não fecha | 1ª negação: explicação; depois da 2ª: aviso com "Abrir configurações"; foto não mudou | Aprovado |
| CT-11 | Galeria | Imagem no aparelho | "Escolher da galeria"; reabrir o app | Foto muda e persiste | "Foto atualizada"; foto mantida após reabrir | Aprovado |
| CT-12 | Notificação | Android 13+ | Carla pede "1984"; entra como Diego | Notificação "Novo pedido de 1984" | Notificação "Novo pedido de 1984" ao entrar como Diego | Aprovado |
| CT-13 | Notificação negada | Notificações desligadas | Bruno pede livro da Carla; Carla abre a Caixa de avisos | Aviso na Caixa com botão "Ativar"; app não fecha | Sem notificação do sistema; aviso na Caixa com o botão "Ativar notificações" | Aprovado |
| CT-14 | Geolocalização | Posição simulada (-20.4410, -54.6440) | "Perto de mim", raio 1 km | Só livros até 1 km, do mais perto ao mais longe; "a pé" no Dom Casmurro | 4 livros a 0 m e 3 a 710 m; os de 1,4 km ficaram fora; Dom Casmurro "A pé · ~9 min" | Aprovado |
| CT-15 | Geolocalização (falha) | Dados iniciais | Negar localização; depois GPS desligado | Mensagens de permissão e de GPS; lista continua; app não fecha | Mensagens de permissão negada e de GPS desligado; lista continuou; app não fechou | Aprovado |
| CT-16 | Anúncio e desejo | Carla deseja "Sapiens" | Ana anuncia "Sapiens"; entra Carla | "Anúncio publicado"; Carla recebe aviso do desejo | "Anúncio publicado"; Carla recebeu "Um livro que você deseja chegou" | Aprovado |
| CT-17 | Resgate sem crédito | Bruno com 0 crédito | Bruno pede doação de livro do Diego | Diego vê "Pedido sem crédito" e o saldo do Bruno | Aviso de pedido sem crédito; Diego viu "Saldo de Bruno Lima: 0 crédito(s)" | Aprovado |
| CT-18 | Troca completa | Continua do CT-17 | Aceitar, propor e aceitar encontro, confirmar entrega | "Concluído"; Diego +1 crédito; Bruno continua com 0 | 3 pontos no mapa; encontro aceito; "Concluído"; Diego 1 crédito, Bruno 0 | Aprovado |
| CT-19 | Estante | Ana lendo um livro | "Terminei de ler" > "Anunciar" | Anúncio abre preenchido | "Quer passar este livro adiante?"; anúncio abriu com título e autor preenchidos | Aprovado |
| CT-20 | Mural | Logado | Coração duas vezes; comentar; salvar | Coração alterna; comentário aparece; post em "Salvos" | Coração 0→1→0; comentário na lista; post em "Salvos" | Aprovado |
| CT-21 | Avaliação | Pedido do CT-18 | Bruno dá 5 estrelas | "Obrigado pela avaliação"; média do Diego atualizada | "Obrigado pela avaliação"; média do Diego passou a 5,0 | Aprovado |
| CT-22 | Disputa | Pedido aceito da Carla | Abrir disputa; Moderador cancela | "Disputa aberta"; pedido fica "Cancelado" | "Disputa aberta. O Moderador vai analisar."; pedido "Cancelado" | Aprovado |
| CT-23 | Posição no encontro | Encontro em 5 min | Carla liga "Mostrar onde estou"; Diego abre o pedido | Diego vê a distância da Carla ao ponto | Diego viu "Carla Mendes · 0 m do ponto · há 1 minuto" | Aprovado |
| CT-24 | API HTTP | Com internet, depois modo avião | Buscar "percy jackson" nos desejos | Online: sugestões da Open Library em português; offline: só catálogo local, sem erro | Online: "O ladrão de raios" (Rick Riordan); offline: só o catálogo local, sem erro | Aprovado |

### Diferenças entre o roteiro e o app

Nenhuma reprovou um caso. São textos ou telas que o roteiro descrevia de outro jeito.

| Caso | Diferença |
|---|---|
| CT-12, CT-17 | O botão do livro se chama "Pedir doação" (ou "Pedir troca"); não existe "Resgatar" |
| CT-13 | Com as notificações desligadas, a Caixa de avisos mostra só o botão "Ativar notificações" |
| CT-05 | A apresentação de 5 telas aparece só na primeira entrada no aparelho, não a cada conta |
| CT-10 | "Abrir configurações" aparece na tentativa seguinte à 2ª negação, quando o Android não pergunta mais |
| CT-19 | "Estante" fica na seção Conta, no fim do Perfil |
| CT-12, CT-16 | Com mais de um aviso pendente, a notificação do Android chega como resumo ("Você tem avisos novos"); o aviso específico fica na Caixa de avisos |

### Observações sobre o app

Comportamentos que não reprovaram casos, mas ficam registrados para melhoria.

| Caso | Observação |
|---|---|
| Busca | Diferencia acentos: "Algebra" não encontra "Álgebra Linear com Aplicações" |
| CT-23 | Se a pessoa liga "Mostrar onde estou" e sai da tela logo em seguida, a posição não é salva; a chave volta desligada ao reabrir a tela |
| CT-18 | Depois de confirmar "Recebi o livro", o botão "Cancelar pedido" volta a aparecer enquanto a outra parte não confirma |

### Ambiente de teste

| Caso | Ocorrência |
|---|---|
| CT-14, CT-23 | `adb emu geo fix` não mudou o GPS; a posição (-20.4410, -54.6440) foi simulada com um provedor de localização de teste |
| CT-09 | Foto tirada com a câmera virtual do emulador |
| CT-11 | Usada uma foto que já estava na galeria do emulador |
| CT-03 | Texto digitado pelo adb em vez de colado |
| CT-17, CT-18 | Os avisos rápidos "Pedido enviado" e "Encontro combinado." não foram lidos a tempo; o estado do pedido mudou como esperado |

### Resumo

| Total | Aprovados | Reprovados | Não executados |
|---|---|---|---|
| 24 | 24 | 0 | 0 |
