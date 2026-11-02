# LeiaUFMS: Log de implementação

> Atualizado ao fim de cada parte. Para voltar atrás em uma parte que deu errado: `git reset --hard <commit inicial da parte>` (só localmente, antes de dar push).

| Parte | Commit inicial | Commit final | Build | Verificação | Observações |
|---|---|---|---|---|---|
| Base | 162ad7c | 162ad7c | — | — | Documentação (o prompt citava `357c50e`; a base real ficou `162ad7c` após commitar as edições pendentes de docs) |

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
