# LeiaUFMS: Log de implementação

> Atualizado ao fim de cada parte. Para voltar atrás em uma parte que deu errado: `git reset --hard <commit inicial da parte>` (só localmente, antes de dar push).

| Parte | Commit inicial | Commit final | Build | Verificação | Observações |
|---|---|---|---|---|---|
| Base | 162ad7c | 162ad7c | — | — | Documentação (o prompt citava `357c50e`; a base real ficou `162ad7c` após commitar as edições pendentes de docs) |
| 1. Fundação, Room, auth, splash | ca1fc28 | (commit `docs: log da parte 1`) | `assembleDebug` verde; 13 testes JUnit passando | Emulador Pixel_10 API 37: login vazio, login errado, login certo, sessão ao reabrir, logout com confirmação + Voltar, cadastro com domínio errado e semestre 99, cadastro válido, tema escuro e fonte 130% | Paleta trocada para azul e branco da UFMS a pedido do Pedro (`7f2cb7c`). Duplicidade de e-mail (CT-13) não foi exercitada no emulador |

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
