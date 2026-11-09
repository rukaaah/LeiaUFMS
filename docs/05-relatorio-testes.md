# LeiaUFMS: Relatório de Testes Caixa-Preta

> Seção 5 do enunciado. São 24 casos, o mínimo que cobre as cinco situações exigidas. **Os casos abaixo ainda NÃO foram executados.** "Resultado obtido" e "Status" só podem ser preenchidos depois de executar o teste no app real. A execução é registrada na planilha "LeiaUFMS - Testes caixa-preta" (Google Sheets), que tem o passo a passo para rodar o app, e os resultados são copiados para cá.

## Ambiente de execução

| Item | Valor |
|---|---|
| Dispositivo/emulador | `[PENDENTE]` |
| Versão do Android (API) | `[PENDENTE]` |
| Versão do app (commit) | `[PENDENTE]` |
| Data da execução | `[PENDENTE]` |
| Executado por | `[PENDENTE]` |

Status possíveis: **Aprovado**, **Reprovado**, **Não executado**. Contas de teste: senha `Leia2026` (ver README). "Dados iniciais" significa limpar os dados do app antes do caso (Configurações > Apps > LeiaUFMS > Armazenamento > Limpar armazenamento).

## 1. Validação de campos

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-01 | RF-02 | App na tela de login. | 1. Toque em "Criar conta".<br>2. Não preencha nada.<br>3. Role até o fim e toque em "Criar conta". | Cada campo obrigatório mostra um erro em vermelho embaixo dele (ex.: "Preencha o seu nome."). O app não fecha. | — | Não executado |
| CT-02 | RF-02 | Tela Criar conta. | 1. Preencha: Nome Teste Parceiro; E-mail aluno@gmail.com; Curso: digite "comp" e escolha Ciência da Computação; Semestre 99; Senha abc nos dois campos de senha.<br>2. Toque em "Criar conta". | Erros nos campos: e-mail "Use seu e-mail institucional da UFMS.", semestre "Use um semestre de 1 a 12." e senha "A senha precisa de 8 caracteres, com pelo menos 1 letra e 1 número." A conta não é criada e o app não fecha. | — | Não executado |
| CT-03 | RF-40 | Conta: Carla. Dados iniciais. O pedido "Fundamentos de Física" (com Diego) já vem aceito. | 1. Entre como Carla. Aba Trocas > "Meus pedidos" > Fundamentos de Física > "Abrir chat".<br>2. Cole o texto de 301 caracteres e toque em "Enviar".<br>3. Apague, escreva "Chego às 12h" e envie. | O texto de 301 caracteres não é enviado (o campo para em 300 ou aparece "A mensagem pode ter no máximo 300 caracteres."). A mensagem curta aparece no chat. | — | Não executado |

## 2. Autenticação e gerenciamento de perfil

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-04 | RF-02 | Conta nova: teste.parceiro@ufms.br. Deslogado. Esta conta ainda não existe. | 1. Toque em "Criar conta".<br>2. Nome Teste Parceiro; E-mail teste.parceiro@ufms.br; Curso: digite "comp" e escolha Ciência da Computação; Semestre 3; Senha Teste1234 nos dois campos.<br>3. Toque em "Criar conta". | Conta criada. Abre a apresentação de 5 telas e, ao tocar em Pular > Começar, o Início. | — | Não executado |
| CT-05 | RF-03 | Conta: Carla. Deslogado. | Entre com carla.mendes@ufms.br e a senha Leia2026. | Abre o Início (na primeira entrada da conta neste aparelho, antes aparece a apresentação). | — | Não executado |
| CT-06 | RF-03 | Conta: Carla. Deslogado. | 1. Entre com carla.mendes@ufms.br e a senha Errada123.<br>2. Entre com naoexiste@ufms.br e a senha Teste1234. | Nas duas vezes aparece "E-mail ou senha incorretos." e o app continua no login. | — | Não executado |
| CT-07 | RF-07 | Conta: teste.parceiro@ufms.br (CT-04). Logado com a conta criada no CT-04. | 1. Perfil > "Editar perfil".<br>2. Mude o nome para Teste Alterado e o curso para outro da lista.<br>3. Toque em "Salvar".<br>4. Feche o app (botão de apps recentes, arraste para cima) e abra de novo. | Aparece "Perfil atualizado". Depois de reabrir, o app entra direto (sem login) e o perfil mostra o nome e o curso novos. | — | Não executado |
| CT-08 | RF-05 | Conta: Carla. Logado como Carla. | 1. Perfil > "Sair" > confirme "Sair".<br>2. Aperte o botão Voltar do Android.<br>3. Abra o app de novo. | Fica no login ou o app fecha. Nenhuma tela interna aparece, e ao reabrir o app pede login. | — | Não executado |

## 3. Câmera e galeria

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-09 | RF-09 | Conta: Carla. Dados iniciais. A permissão de câmera fica sem decisão. | 1. Entre como Carla. Perfil > toque na foto ("Alterar foto") > "Tirar foto".<br>2. Toque em Permitir.<br>3. Tire a foto e confirme. | Aparece "Foto atualizada" e a foto aparece no perfil. | — | Não executado |
| CT-10 | RF-09 | Conta: Carla. Dados iniciais. | 1. Entre como Carla. Perfil > foto > "Tirar foto".<br>2. Toque em "Não permitir".<br>3. Repita e negue de novo. | Na 1ª negação: mensagem explicando que sem a câmera não dá para tirar a foto. Depois de negar de vez: aviso com o botão "Abrir configurações". O app não fecha e a foto continua a mesma. | — | Não executado |
| CT-11 | RF-10 | Conta: Carla. Ter uma imagem no emulador . | 1. Perfil > foto > "Escolher da galeria".<br>2. Escolha a imagem.<br>3. Feche e abra o app. | A foto muda e continua lá depois de reabrir o app. | — | Não executado |

## 4. Notificações e geolocalização (recurso escolhido)

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-12 | RF-31 | Conta: Carla, depois Diego. Dados iniciais. Android 13 ou mais novo. | 1. Entre como Carla. Busque "1984" e abra o livro do Diego.<br>2. Toque em "Resgatar" > "Enviar pedido". Se o app pedir permissão de notificações, toque em Permitir.<br>3. Saia (Perfil > Sair > Sair) e entre como Diego. | Ao entrar como Diego, chega uma notificação "Novo pedido de 1984". | — | Não executado |
| CT-13 | RF-34 | Conta: Bruno, depois Carla. Notificações do LeiaUFMS desligadas nas configurações do Android. | 1. Entre como Bruno. Abra "O Pequeno Príncipe" (da Carla) e envie um pedido.<br>2. Saia (Perfil > Sair > Sair) e entre como Carla.<br>3. Toque no sino (Caixa de avisos) no topo. | Nenhuma notificação do sistema e o app não fecha. O aviso do pedido está na Caixa de avisos, com "Notificações desligadas" e o botão "Ativar". | — | Não executado |
| CT-14 | RF-27, RF-28 | Conta: Carla. Dados iniciais. Posição simulada na Entrada da biblioteca: -20.4410, -54.6440. | 1. Entre como Carla. Toque na barra de busca do Início.<br>2. Toque em "Perto de mim" e permita a localização.<br>3. Escolha o raio "1 km".<br>4. Abra "Dom Casmurro". | Só aparecem livros a até 1 km, os mais perto primeiro: Vidas Secas, Cálculo, Algoritmos e 1984 (0 m), depois Dom Casmurro, Sapiens e Álgebra Linear (cerca de 710 m). Memórias Póstumas e O Pequeno Príncipe (cerca de 1,4 km) não aparecem. Dom Casmurro mostra que dá para ir a pé. | — | Não executado |
| CT-15 | RF-27 | Conta: Carla. Dados iniciais. | 1. Entre como Carla. Toque na barra de busca do Início > "Perto de mim".<br>2. Toque em "Não permitir".<br>3. Permita a localização nas configurações, desligue a localização do aparelho e toque em "Perto de mim" de novo. | Negada: "Sem acesso à localização, não dá para mostrar a distância. Você ainda pode ver todos os livros." e a lista continua. GPS desligado: "Ligue a localização do aparelho para ver livros perto de você." O app não fecha. | — | Não executado |

## 5. Demais funcionalidades

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-16 | RF-12, RF-32 | Conta: Ana, depois Carla. Dados iniciais. Carla já tem "Sapiens" nos Desejos. | 1. Entre como Ana. Início > "Anunciar livro".<br>2. Digite "Sapiens" e escolha a sugestão "Sapiens: Uma Breve História da Humanidade". Preencha conservação, modalidade e um ponto seguro.<br>3. Toque em "Publicar".<br>4. Saia (Perfil > Sair > Sair) e entre como Carla. | Ana vê "Anúncio publicado". Carla recebe "Um livro que você deseja chegou" (notificação ou Caixa de avisos). | — | Não executado |
| CT-17 | RF-19, RN-CRED-04 | Conta: Bruno, depois Diego. Dados iniciais. Bruno começa com 0 créditos. | 1. Entre como Bruno. Abra "Álgebra Linear com Aplicações" (do Diego) e toque em "Resgatar" > "Enviar pedido".<br>2. Saia (Perfil > Sair > Sair) e entre como Diego. Trocas > "Pedidos pra mim" > Álgebra Linear. | Bruno vê o aviso de pedido sem crédito e "Pedido enviado". Diego vê "Pedido sem crédito. Quem anunciou decide se aceita." e "Saldo de Bruno Lima: 0 crédito(s)". | — | Não executado |
| CT-18 | RF-20 a RF-22, RF-44 | Conta: Diego e Bruno. Continue do CT-17. | 1. Como Diego: "Aceitar" > "Propor encontro": escolha um ponto no mapa, hoje, daqui 1 hora > "Propor este horário".<br>2. Entre como Bruno, abra o pedido e toque em "Aceitar horário" > "Recebi o livro" > Confirmar.<br>3. Entre como Diego e toque em "Entreguei o livro" > Confirmar. | O mapa mostra os 3 pontos seguros. Depois do aceite do horário aparece "Encontro combinado.". Com as duas confirmações o pedido fica "Concluído": Diego ganha 1 crédito e o saldo do Bruno continua 0. | — | Não executado |
| CT-19 | RF-36 | Conta: Ana. Ana está lendo "Grande Sertão: Veredas" (dados iniciais). | 1. Entre como Ana. Perfil > "Estante".<br>2. Toque em Grande Sertão: Veredas > "Terminei de ler".<br>3. Na pergunta, toque em "Anunciar". | Aparece "Quer passar este livro adiante?". "Anunciar" abre o Anunciar livro já com título e autor preenchidos. | — | Não executado |
| CT-20 | RF-38, RF-39, RF-43 | Conta: Carla. Logado como Carla. | 1. No Mural, toque no coração de um post duas vezes.<br>2. Toque no post, escreva um comentário curto e envie.<br>3. Toque em Salvar e depois no filtro "Salvos". | O coração marca e desmarca (o número nunca sobe 2). O comentário aparece na lista. O post salvo aparece em "Salvos". | — | Não executado |
| CT-21 | RF-25 | Conta: Bruno. Pedido concluído do CT-18. | 1. Entre como Bruno, abra o pedido concluído e toque em "Avaliar a troca".<br>2. Dê 5 estrelas > "Enviar nota".<br>3. Abra o perfil do Diego. | Aparece "Obrigado pela avaliação". A nota média do Diego passa a contar essa nota. | — | Não executado |
| CT-22 | RF-26 | Conta: Carla, depois Moderação. Dados iniciais. | 1. Entre como Carla. Trocas > "Meus pedidos" > Fundamentos de Física > "Abrir disputa". Escreva o motivo e confirme.<br>2. Entre como moderacao@ufms.br. Perfil > "Disputas para analisar" > o pedido > "Cancelar o pedido". | Carla vê "Disputa aberta. O Moderador vai analisar.". Depois da decisão, o pedido fica "Cancelado". | — | Não executado |
| CT-23 | RF-41 | Conta: Diego e Carla. Dados iniciais. Posição simulada na Entrada da biblioteca. | 1. Entre como Diego. Trocas > "Pedidos pra mim" > Fundamentos de Física > "Propor encontro": Entrada da biblioteca, hoje, daqui 5 min.<br>2. Entre como Carla, abra o pedido > "Aceitar horário" e ligue "Mostrar onde estou para Diego".<br>3. Saia (Perfil > Sair > Sair), entre como Diego e abra o pedido. | Diego vê "Carla · N m do ponto · atualizado há N min". Fora dos 10 min antes do encontro, o botão não aparece. | — | Não executado |
| CT-24 | RF-45 | Conta: Carla. Com internet. | 1. Desejos > "Adicionar desejo" > digite "percy jackson".<br>2. Ligue o modo avião e digite de novo. | Com internet: sugestões em português da Open Library ("O ladrão de raios", Rick Riordan). Sem internet: só sugestões do catálogo do app e nenhum erro. | — | Não executado |

## 6. Falhas encontradas e correções

| ID da falha | Caso | Descrição | Corrigida? | Commit/observação |
|---|---|---|---|---|
| F-08 | CT-23 | Ao ligar "Mostrar onde estou" e sair logo da tela, a primeira leitura de posição era cancelada e nada era salvo; ao reabrir, a chave voltava desligada. | Sim | Ao sair, a leitura em andamento termina e é salva; o estado da chave fica guardado por pedido e horário do encontro. |
| F-09 | CT-18 | Depois de "Recebi o livro", o botão "Cancelar pedido" voltava a aparecer enquanto a outra parte não confirmava. | Sim | Quem já confirmou não vê mais "Cancelar pedido" e o cancelamento é recusado no repositório; resta "Abrir disputa". |

## 7. Resumo

| Total | Aprovados | Reprovados | Não executados |
|---|---|---|---|
| 24 | 0 | 0 | 24 |
