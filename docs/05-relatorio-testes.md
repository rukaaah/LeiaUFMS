# LeiaUFMS: Relatório de Testes Caixa-Preta

> Seção 5 do enunciado. **Os casos abaixo estão planejados e ainda NÃO foram executados.** As colunas "Resultado obtido" e "Status" só podem ser preenchidas depois de executar o teste no app real. Nunca preencher por suposição.

## Ambiente de execução

| Item | Valor |
|---|---|
| Dispositivo/emulador | `[PENDENTE]` |
| Versão do Android (API) | `[PENDENTE]` (recomendado: um aparelho/emulador com API 33+ e outro com API ≤ 32 para notificações e galeria) |
| Versão do app (commit/tag) | `[PENDENTE]` |
| Data da execução | `[PENDENTE]` |
| Executado por | `[PENDENTE]` |

Status possíveis: **Aprovado**, **Reprovado**, **Não executado**.

## 1. Validação de campos

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-01 | RF-02 | Tela de cadastro | Todos os campos vazios → Criar conta | Erro em cada campo obrigatório; sem crash | — | Não executado |
| CT-02 | RF-02 | Tela de cadastro | E-mail `aluno@gmail.com` | "Use seu e-mail institucional da UFMS." | — | Não executado |
| CT-03 | RF-02 | Tela de cadastro | E-mail `aluno@ufms` (sem domínio completo) | Erro de e-mail; conta não criada | — | Não executado |
| CT-04 | RF-02 | Tela de cadastro | Senha `abc` | Mensagem de senha fraca | — | Não executado |
| CT-05 | RF-02 | Tela de cadastro | Senha e confirmação diferentes | "As senhas não são iguais." | — | Não executado |
| CT-06 | RF-02 | Tela de cadastro | Semestre `0` e `99` | Erro de faixa (1 a 12) | — | Não executado |
| CT-07 | RF-12 | Novo anúncio | Título vazio | Erro no campo título | — | Não executado |
| CT-08 | RF-12 | Novo anúncio | ISBN `123` | Erro: ISBN com 10 ou 13 dígitos | — | Não executado |
| CT-09 | RF-38 | Novo post no mural | Post sem obra vinculada | Publicação bloqueada com mensagem | — | Não executado |
| CT-10 | RF-40 | Chat aberto | Texto com 301 caracteres | Bloqueio ou contador impede envio | — | Não executado |
| CT-11 | RNF-04 | Qualquer formulário | Colar 5.000 caracteres e emojis | Sem crash; limite respeitado | — | Não executado |

## 2. Autenticação e gerenciamento de perfil

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-12 | RF-02 | E-mail não cadastrado | Cadastro válido | Conta criada; abre a tela Início | — | Não executado |
| CT-13 | RF-02 | E-mail já cadastrado | Cadastro com o mesmo e-mail | "Já existe uma conta com este e-mail." | — | Não executado |
| CT-14 | RF-03 | Conta existente | Login com senha correta | Abre Início | — | Não executado |
| CT-15 | RF-03 | Conta existente | Login com senha errada | "E-mail ou senha incorretos."; continua no login | — | Não executado |
| CT-16 | RF-03 | — | Login com e-mail inexistente | Mesma mensagem do CT-15 | — | Não executado |
| CT-17 | RN-AUTH-05 | Conta criada | Inspecionar tabela `usuario` (App Inspection) | `senhaHash` começa com `$2` e não contém a senha | — | Não executado |
| CT-18 | RF-01/04 | Logado | Fechar e reabrir o app | Splash → Início, sem pedir login | — | Não executado |
| CT-19 | RF-01 | Deslogado | Abrir o app | Splash → Login | — | Não executado |
| CT-20 | RF-05 | Logado | Sair → confirmar → botão Voltar | Fica no login; telas internas inacessíveis | — | Não executado |
| CT-21 | RF-07 | Logado | Alterar nome e curso → Salvar → reabrir app | Dados novos persistidos | — | Não executado |
| CT-22 | RF-07 | Logado | Alterar e-mail para `x@gmail.com` | Erro de domínio; e-mail antigo mantido | — | Não executado |
| CT-23 | RF-08 | Logado | Senha atual errada | "A senha atual não confere." | — | Não executado |
| CT-24 | RF-08 | Logado | Trocar senha → sair → entrar com a antiga e com a nova | Antiga falha; nova funciona; hash mudou no banco | — | Não executado |

## 3. Câmera e galeria

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-25 | RF-09 | Permissão de câmera não decidida | Tirar foto → Permitir → fotografar | Foto aparece no perfil; caminho salvo no Room | — | Não executado |
| CT-26 | RF-09 | Permissão não decidida | Tirar foto → Negar | Mensagem explicativa; sem crash; foto anterior mantida | — | Não executado |
| CT-27 | RF-09 | Negada permanentemente | Tirar foto | Diálogo com atalho para Configurações | — | Não executado |
| CT-28 | RF-09 | Câmera aberta | Cancelar a captura | Foto anterior mantida | — | Não executado |
| CT-29 | RF-10 | — | Escolher da galeria → selecionar imagem | Foto atualizada e persistida após reabrir o app | — | Não executado |
| CT-30 | RF-10 | Seletor aberto | Cancelar | Foto anterior mantida; sem crash | — | Não executado |

## 4. Notificações e geolocalização

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-31 | RF-31 | API 33+, permissão concedida | Aluno B pede livro de A; logar como A | Notificação "Novo pedido" | — | Não executado |
| CT-32 | RF-34 | API 33+, permissão negada | Gerar evento de pedido | Sem crash; aviso na Caixa de avisos; banner para ativar | — | Não executado |
| CT-33 | RF-32 | A tem obra X na lista de desejos | B anuncia obra X; logar como A | Notificação/aviso "Livro da sua lista foi anunciado" | — | Não executado |
| CT-34 | RF-33 | Pedido pendente há > 72 h (ajustar relógio ou dado de teste) | Abrir app | Pedido `EXPIRADO`; crédito liberado; aviso | — | Não executado |
| CT-35 | RF-27 | Localização permitida, posição simulada | Perto de mim, raio 1 km | Só anúncios ≤ 1 km, ordenados por distância | — | Não executado |
| CT-36 | RF-27 | Permissão de localização negada | Perto de mim | Mensagem; lista completa sem distância; sem crash | — | Não executado |
| CT-37 | RF-27 | GPS do aparelho desligado | Perto de mim | "Ligue a localização do aparelho..." | — | Não executado |
| CT-38 | RF-28 | Anúncio a ~800 m | Abrir detalhe | "Dá para ir a pé (~10 min)" | — | Não executado |
| CT-39 | RF-28 | Anúncio a ~5 km | Abrir detalhe | "Longe para ir a pé" | — | Não executado |
| CT-40 | RF-29 | Pedido aceito, com internet | Marcar encontro | Mapa com marcadores dos pontos seguros | — | Não executado |
| CT-41 | RF-21 | Mapa aberto | Tentar confirmar sem escolher ponto seguro | "Escolha um dos pontos seguros marcados no mapa." | — | Não executado |
| CT-42 | RF-29 | Modo avião | Marcar encontro | Mensagem de mapa sem internet + lista de pontos | — | Não executado |

## 5. Demais funcionalidades (trocas, créditos, chat, social)

| ID | RF | Condição inicial | Entrada/ação | Resultado esperado | Resultado obtido | Status |
|---|---|---|---|---|---|---|
| CT-43 | RN-TROCA-01 | Logado como dono | Abrir o próprio anúncio | Botão de pedir indisponível: "Este anúncio é seu." | — | Não executado |
| CT-44 | RF-19, RN-CRED-04 | Saldo disponível 0 | Pedir resgate | Pedido criado como "sem crédito"; aviso informativo; dono vê o saldo 0 | — | Não executado |
| CT-44b | RN-CRED-05 | Pedido sem crédito aceito | Os dois confirmam | CONCLUIDO; doador +1; saldo do solicitante continua 0 (não negativo) | — | Não executado |
| CT-45 | RF-19 | Saldo 1 | Pedir resgate | Saldo disponível 0; pedido PENDENTE | — | Não executado |
| CT-46 | RF-20 | Pedido de resgate pendente | Dono recusa | Saldo do solicitante volta a 1 | — | Não executado |
| CT-47 | RF-22 | Pedido aceito | Só um lado confirma | Continua ACEITO; créditos inalterados | — | Não executado |
| CT-48 | RF-22 | Pedido de resgate aceito | Os dois confirmam | CONCLUIDO; doador +1; posse muda; histórico +1 mão | — | Não executado |
| CT-49 | RN-TROCA-02 | 5 pedidos pendentes | Fazer o 6º pedido | Bloqueado com mensagem de limite | — | Não executado |
| CT-50 | RF-40 | Pedido PENDENTE | Abrir chat | "O chat abre quando o pedido for aceito." | — | Não executado |
| CT-51 | RF-40 | Pedido CONCLUIDO | Tentar enviar mensagem | Chat somente leitura | — | Não executado |
| CT-52 | RF-40 | Usuário sem pedido com outro | Procurar forma de iniciar conversa | Nenhuma opção de chat fora de pedido | — | Não executado |
| CT-53 | RF-36 | Obra em "Estou lendo" | Marcar como "Lido" | Pergunta "Quer passar este livro adiante?"; Sim abre anúncio pré-preenchido | — | Não executado |
| CT-54 | RF-39 | Post no mural | Tocar o coração duas vezes; depois tocar em Salvar e abrir o filtro "Salvos" | Coração alterna (marca/desmarca), nunca soma 2; o post salvo aparece em "Salvos" | — | Não executado |
| CT-55 | RF-38 | Mural com mais de 30 posts recentes | Rolar até o fim | Para em 30 itens com "Você está em dia" | — | Não executado |
| CT-56 | RF-25 | Pedido concluído | Avaliar com nota 5 | Média do avaliado atualizada | — | Não executado |
| CT-57 | RF-17 | Exemplar com 2 trocas concluídas | Ver histórico | "Este exemplar passou por 3 mãos" | — | Não executado |
| CT-58 | RF-26 | Pedido aceito | Abrir disputa; logar como moderador e cancelar | Estado CANCELADO; reservas liberadas | — | Não executado |
| CT-60 | RF-41 | Pedido aceito com encontro daqui a 30 min | Abrir o pedido | Bloco "Localização no encontro" explica que só pode compartilhar a partir de 10 min antes; sem botão | — | Não executado |
| CT-61 | RF-41 | Encontro daqui a 5 min, conta A | Ligar "Compartilhar minha localização"; entrar como B e abrir o pedido | B vê "A está a N m do ponto · atualizado há N min" | — | Não executado |
| CT-62 | RF-41 | Compartilhamento ligado | Desligar o botão ou concluir o pedido | A outra parte deixa de ver a posição | — | Não executado |
| CT-63 | RF-42 | Aparelho com internet, obra nova anunciada | Esperar alguns segundos e abrir Início | Capa real aparece no card; sem internet, aparecem as iniciais do título | — | Não executado |
| CT-64 | Onboarding | Primeira entrada de uma conta | Entrar | 5 telas (créditos, encontro, localização de 10 min, preferências); "Pular" vai para a última; "Começar" abre o Início | — | Não executado |
| CT-65 | RF-44 | Pedido aceito sem encontro (conta A) | A propõe ponto, data e hora | Pedido mostra "Você propôs. Falta a outra pessoa aceitar."; barra de etapas continua em Aceito; sem botão de confirmar entrega | — | Não executado |
| CT-66 | RF-44 | Proposta de A (entrar como B) | Abrir o pedido → Aceitar horário | "Encontro combinado."; etapa Encontro acesa; "Combinado pelos dois"; A recebe aviso | — | Não executado |
| CT-67 | RF-44 | Proposta de A (entrar como B) | Sugerir outro → escolher outro horário | Vira proposta de B; agora A precisa aceitar | — | Não executado |
| CT-68 | RF-43 | Mural com posts | Tocar num post | Página com texto inteiro, capa, quem postou, coração, comentários, Salvar e Compartilhar | — | Não executado |
| CT-69 | RF-43 | Post aberto | Comentar com campo vazio / com 300 caracteres / com texto curto | Vazio: botão desligado; texto curto aparece na lista e o autor do post recebe aviso | — | Não executado |
| CT-70 | Compartilhar | Livro, Obra ou post aberto | Tocar em Compartilhar | Folha de compartilhamento do Android com título, autor e detalhes em texto | — | Não executado |
| CT-71 | Aviso no app | App aberto, chega um aviso (ex.: encontro em breve) | Esperar | Cartão do LeiaUFMS desce do topo com ícone do assunto, some em 6 s; tocar abre o pedido; com o app fechado, vira notificação do sistema | — | Não executado |
| CT-72 | Navegação | Dentro de um pedido aberto na aba Trocas | Tocar de novo em Trocas | Volta ao começo da aba | — | Não executado |
| CT-73 | RF-37 | Aba Desejos | Tocar em "Desejar um livro" e adicionar | Continua na aba Desejos; livro entra em "Esperando aparecer" | — | Não executado |
| CT-74 | RF-02 | Tela de cadastro | Digitar "comp" no curso; depois digitar "Curso Inventado" e enviar | A lista mostra Ciência da Computação e Engenharia de Computação; curso fora da lista mostra "Escolha um curso da lista da UFMS." | — | Não executado |

## 6. Falhas encontradas e correções

| ID da falha | Caso | Descrição | Corrigida? | Commit/observação |
|---|---|---|---|---|
| — | — | Nenhuma registrada ainda | — | — |

## 7. Resumo

| Total | Aprovados | Reprovados | Não executados |
|---|---|---|---|
| 74 | 0 | 0 | 74 |
