# LeiaUFMS: Log de implementação

> Atualizado ao fim de cada parte. Para voltar atrás em uma parte que deu errado: `git reset --hard <commit inicial da parte>` (só localmente, antes de dar push).

| Parte | Commit inicial | Commit final | Build | Verificação | Observações |
|---|---|---|---|---|---|
| Base | 162ad7c | 162ad7c | — | — | Documentação (o prompt citava `357c50e`; a base real ficou `162ad7c` após commitar as edições pendentes de docs) |
| 1. Fundação, Room, auth, splash | ca1fc28 | 0651eee | `assembleDebug` verde; 13 testes JUnit passando | Emulador Pixel_10 API 37: login vazio, login errado, login certo, sessão ao reabrir, logout com confirmação + Voltar, cadastro com domínio errado e semestre 99, cadastro válido, tema escuro e fonte 130% | Paleta trocada para azul e branco da UFMS a pedido do Pedro (`7f2cb7c`). Duplicidade de e-mail (CT-13) não foi exercitada no emulador |
| 2. Perfil, mídia, permissões, notificações | 5ee4dce | 133b042 | `assembleDebug` verde; 13 testes JUnit passando | Emulador API 37: perfil com créditos e métricas; câmera negada (mensagem), negada de vez (atalho para Configurações), concedida (foto salva em `filesDir/fotos` e caminho no Room); galeria pelo Photo Picker (foto antiga apagada); Caixa de avisos com banner de permissão negada; permissão concedida pelo banner; aviso pendente entregue como notificação no login de outra conta; editar perfil com e-mail fora do domínio e curso vazio, depois salvo; senha atual errada; troca de senha → senha antiga falha e nova entra; tema escuro e fonte 130% | Corrigido bug da Parte 1: na primeira abertura, o login podia rodar antes do seed terminar. Agora o seed roda no `LeiaApp` e login/cadastro esperam ele. A notificação com permissão negada no Android ≤ 12 não foi testada (emulador só API 37) |
| 3. Anúncios, trocas, créditos, geolocalização | 718f7ca | (commit `docs: log da parte 3`) | `assembleDebug` verde; 30 testes JUnit passando (novos: `CreditRulesTest`, `WalkEstimateTest`) | Emulador API 37, alternando 4 contas: Moderador cadastrou 2 pontos tocando no mapa; resgate **sem crédito** (Carla → Diego) aceito, encontro marcado no mapa, confirmado pelos dois → concluído, Diego +1, Carla continua 0; resgate **com crédito** (Diego → Ana) reserva 1 (perfil mostra "1 reservado"), aceite recusou automaticamente o pedido do Bruno, cancelamento devolveu a reserva (livro-razão conferido no banco); ISBN inválido; novo anúncio com ponto seguro; "Perto de mim" com localização negada, GPS desligado e posição simulada ("a 190 m · Dá para ir a pé (~3 min)"); modo avião no Marcar encontro (mapa some, lista continua); migração do banco v1 → v2 sem perda de dados; tema escuro e fonte 130% | Limite de 5 pedidos (RN-TROCA-02) e expiração por 72 h / 7 dias (WorkManager) não foram exercitados no emulador (dependem de tempo); a lógica está no `TradeRepository.runDeadlines` e roda também ao abrir o app. No emulador, a posição foi injetada com provedores de teste (`cmd location providers`) |

## Ambiente

| Item | Valor |
|---|---|
| Máquina | macOS (Mac do Pedro), Android Studio 2026.1 |
| JDK | JetBrains Runtime 25 (embutido no Android Studio) |
| Gradle / AGP | 9.6.0 / 9.4.1 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |
| Emulador | AVD `Pixel_10` (arm64) |

## Auditorias de UI (Etapa D3)

| Parte | Itens reprovados | Itens corrigidos |
|---|---|---|
| 1 | Barra de status com ícones claros sobre fundo claro; fonte serifada não aplicada (tema sobrescrevia `textAppearance`); destaque cinza de foco na `ScrollView`; borda dos campos invisível no tema escuro (contraste de componente < 3:1); diálogo com fundo azulado em vez de `surface`; e-mail com sugestões do teclado; título do estado vazio sem marcação de cabeçalho; cadastro preenchido descartado sem aviso ao voltar | Todos corrigidos em `44b4da4` e nos commits anteriores da parte. Conferido no emulador em tema claro, escuro e fonte 130% |
| 2 | Seta de voltar da barra ignorava o aviso de descartar alterações; Alterar senha sem aviso ao sair com campos preenchidos; foto do perfil clicável sem rótulo de ação para o TalkBack; tempo relativo em inglês quando o aparelho está em inglês; ação da Snackbar em lilás fora da paleta | Todos corrigidos (`fix: ajustes da auditoria de UI da parte 2` e `5ee4dce`). Conferido em tema escuro e fonte 130% |
| 3 | Placeholder da busca sem "…"; Novo anúncio sem aviso ao descartar; prévia da foto do exemplar escondida do TalkBack; cadastro de ponto seguro só por gesto no mapa (sem alternativa); alternador Recebidos/Enviados com bordas cortadas; diálogos com fundo tingido; "1 trocas concluídas"; aviso "sem crédito" depois do aceite; lista de Explorar não mostrava o anúncio recém-publicado no topo; localização sem resposta do Fused no emulador | Todos corrigidos (`a440113` e `fix: ajustes da auditoria de UI da parte 3`): busca com "…", aviso de descarte, foto anunciada pelo TalkBack, botão "Marcar ponto na minha localização", abas `TabLayout`, diálogo em `surface`, plural, aviso só no pendente, rolagem ao topo, fallback para a última posição do aparelho |
