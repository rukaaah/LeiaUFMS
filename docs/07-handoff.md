# Handoff
- Atualizado em: 02/11/2026 20:58 (horário de Campo Grande)
- Parte atual: 4 (Chat, estante, desejos e avaliações), em andamento
- Último commit verde: commit `feat: chat do pedido, estante, desejos e avaliações (RF-25, RF-35 a RF-37, RF-40)`
- Feito na Parte 4 (código pronto, build verde, ainda NÃO verificado no emulador): briefings D2 (03-design-system §6.1), ChatFragment (só com pedido ACEITO, somente leitura depois, mensagens prontas, 300 caracteres), ShelfFragment (Estante/Desejos, "Terminei de ler" → anúncio pré-preenchido), estante pública no Perfil, avaliação 1–5 no pedido concluído (editável por 48 h), SocialRepository
- Em andamento: verificação manual da Parte 4. Já visto: chat de pedido encerrado abre somente leitura
- Próximo passo exato: no emulador, entrar como carla.mendes@ufms.br (senha alterada no teste para NovaSenha1), Trocas → Enviados → Fundamentos de Física (Aceito) → Abrir chat e mandar mensagem pronta e texto; depois avaliar a troca concluída de Álgebra Linear; depois estante/desejos. Em seguida: auditoria D3 da Parte 4, `docs: log da parte 4` e Parte 5
- Problemas conhecidos: no chat encerrado, o texto vazio ainda diz "Use uma mensagem pronta para começar" (trocar por "Nenhuma mensagem."). O script de logout por adb às vezes não acha o botão "Sair" quando a tela não está no Perfil. Marcadores do mapa com ícone padrão. APK debug de ~60 MB
