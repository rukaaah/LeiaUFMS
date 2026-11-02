# LeiaUFMS

Comunidade de troca de livros para alunos da UFMS. Sem dinheiro: troque um livro por outro ou passe um livro adiante, ganhe 1 crédito e use para resgatar outro. Encontros presenciais em pontos seguros do campus, com geolocalização para achar livros perto de você.

Avaliação 3 de Programação Mobile (Android nativo, Java).

## Integrantes

- Pedro Lucas de Souza Cremonini
- Gabriel Mattos de Aquino
- Angelo Antonio Correia de Souza

## Documentação

| Documento | Conteúdo |
|---|---|
| [AGENT.md](AGENT.md) | Instruções para agentes e ordem de leitura |
| [Pré-documentação](docs/01-pre-documentacao.md) | Análise do enunciado, decisões, pendências, cronograma |
| [Especificação](docs/02-especificacao.md) | Documento entregue ao professor: visão geral, papéis, requisitos |
| [Design system](docs/03-design-system.md) | Cores, tipografia, componentes, mensagens |
| [Arquitetura](docs/04-arquitetura.md) | Stack, pacotes, modelo de dados Room |
| [Relatório de testes](docs/05-relatorio-testes.md) | Casos de teste caixa-preta e resultados |

## Como rodar

1. Instale o Android Studio (2026.1 ou mais novo).
2. Abra a pasta do repositório e espere a sincronização do Gradle.
3. Rode a configuração `app` em um emulador ou aparelho com Android 8.0 (API 26) ou mais novo.

Pela linha de comando: `./gradlew assembleDebug` (APK em `app/build/outputs/apk/debug/`) e `./gradlew testDebugUnitTest` (testes JUnit).

## Contas de demonstração

Criadas automaticamente na primeira abertura do app. Todas usam a senha **`Leia2026`**.

| Conta | Papel | Curso |
|---|---|---|
| `moderacao@ufms.br` | Moderador | — |
| `ana.souza@ufms.br` | Aluna (2 créditos, 1 reservado numa disputa) | Letras |
| `bruno.lima@ufms.br` | Aluno | Engenharia Civil |
| `carla.mendes@ufms.br` | Aluna | Ciência da Computação |
| `diego.rocha@ufms.br` | Aluno | Física |

Nenhum ponto seguro vem pronto: entre como Moderador e cadastre os pontos tocando no mapa.
