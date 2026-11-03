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
| [Log de implementação](docs/06-log-implementacao.md) | Commits e verificação de cada parte |
| [Jornada e usabilidade](docs/08-jornada-e-usabilidade.md) | Jornada do aluno, psicologia aplicada, identidade das abas |

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

Na primeira entrada de cada conta aparece o onboarding (créditos, encontro em ponto seguro, localização de 10 minutos e preferências).

O seed traz **3 pontos seguros fictícios** perto do centro de Campo Grande, só para a demonstração. Entre como Moderador (Perfil → Pontos seguros) para cadastrar os pontos reais tocando no mapa, e em Perfil → Disputas para decidir disputas.

### Roteiro curto de demonstração

1. Entre como **Carla**: o Início mostra "Pra você agora" e o carrossel "Perto de você, a pé".
2. Abra um livro, toque em **Resgatar** (sem crédito também pode).
3. Saia e entre como quem anunciou: aceite em **Trocas**, marque o encontro num ponto seguro e confirme a entrega.
4. Entre de novo como Carla e confirme o recebimento: a troca é concluída, o doador ganha 1 crédito e aparece o convite para avaliar e contar no mural.

## Recursos usados

- Room (19 entidades + localização no encontro), BCrypt custo 12, sessão com token (hash SHA-256 no banco).
- Geolocalização sob demanda (Fused Location), mapa MapLibre + OpenFreeMap.
- Consumo de API HTTP: capas da Open Library.
- Notificações em 3 canais, com Caixa de avisos quando a permissão é negada.
- Câmera e galeria (Photo Picker) para foto do perfil e do exemplar.

## Design

Ver [`DESIGN.md`](DESIGN.md) e [`docs/08-jornada-e-usabilidade.md`](docs/08-jornada-e-usabilidade.md).
