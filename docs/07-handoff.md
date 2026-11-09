# Handoff
- Atualizado em: 03/11/2026 (madrugada, horário de Campo Grande)
- Parte atual: 5 concluída + redesenho v2 + ajustes de usabilidade 1 e 2 de 03/11 concluídos
- Último commit verde: commit `docs: ajustes de usabilidade`
- Feito: RF-01 a RF-44 (novos: RF-41 localização no encontro, RF-42 capas, RF-43 post aberto e comentários, RF-44 encontro combinado pelos dois), onboarding, redesenho v2, barra com Início em bola, aviso do app no tema, ícones próprios (`docs/09-icones.md`)
- Em andamento: nada
- Próximo passo exato: os 24 casos de teste caixa-preta foram executados no emulador em 09/11/2026 e estão registrados em `docs/05-relatorio-testes.md` (24 aprovados). Falta exportar `02-especificacao.md` + `05-relatorio-testes.md` em PDF
- Problemas conhecidos: APK debug ~60 MB (MapLibre com todas as ABIs). Marcadores do mapa com o ícone padrão. TalkBack não foi testado com o leitor ligado (só pela árvore de acessibilidade). No emulador, "Perto de mim" precisa de posição injetada (ver log da Parte 3). Bancos antigos migram até a v6, mas para ver os pontos fictícios e o seed novo é preciso reinstalar o app
