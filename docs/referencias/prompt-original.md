# Briefing original do grupo

> Prompt usado para iniciar o projeto, preservado como contexto de domínio. O nome do app foi atualizado de "Livro a Livro" para **LeiaUFMS**. Em caso de conflito, valem o PDF e os documentos numerados em `docs/`.


**Contexto e Papel do Agente**
Atue como um orientador e executor de engenharia de software especializado em programação mobile nativa. Sua tarefa é estruturar e desenvolver, de ponta a ponta (end-to-end), um aplicativo Android e sua respectiva documentação, garantindo o cumprimento estrito de todos os requisitos acadêmicos estipulados no documento.

**Fase Inicial Obrigatória: Leitura e Análise do Documento**
Antes de iniciar qualquer planejamento, arquitetura ou código, você **deve obrigatoriamente** acessar e ler na íntegra o arquivo de referência chamado `"Avaliação 3 prog Mobile.pdf"`.

* Analise o documento passo a passo para tirar suas próprias conclusões sobre o peso de cada requisito e a estrutura esperada pelo professor.
* Faça o cruzamento das informações do PDF com as diretrizes, proibições e regras de negócio listadas abaixo.
* Somente após essa análise crítica, inicie a elaboração da pré-documentação.

**Visão Geral do Domínio: Aplicativo LeiaUFMS e a Camada Social**
O projeto é uma comunidade universitária para troca de livros, restrita a alunos com e-mail institucional. O sistema opera sem dinheiro, focando em gerar confiança, reciprocidade e o desejo de troca por meio de uma **camada social objetiva e livre de toxicidade**.
Mecânicas principais de transação:

1. **Troca Direta:** Usuário pede um livro e oferece outro. Se aceito, trocam presencialmente.
2. **Resgate por Crédito:** Usa-se 1 crédito (ganho ao doar um livro) para resgatar.

**A Camada Social e Regras de Negócio**
A rede social interna foca apenas no que gera trocas e fortalece a comunidade:

* **Perfil (Cartão de Confiança):** Nome, curso, semestre, foto e status. Exibe selos (doador, veterano), métricas (trocas, notas) e a "Estante Pública" (lendo, desejos, passados adiante). A trajetória de cada exemplar (por quantas mãos passou) deve ser rastreável.
* **Estante e Desejos:** O app usa o status "Estou lendo" para, ao final, perguntar se o usuário quer anunciar. A "Lista de Desejos" gera notificações automáticas quando o livro é anunciado na rede.
* **Mural da Comunidade:** Feed restrito a livros. Aceita posts de trocas concluídas (automáticos e opcionais), dicas, procuras e doações. **Sem curtidas em massa, sem seguidores, sem feed infinito** (apenas reações simples como "útil" e "quero").
* **Páginas de Livros/Disciplinas:** Hubs para agrupar anúncios do mesmo livro, compatibilidade de edições e recomendações para turmas específicas.
* **Comunicação Restrita (Anti-spam):** O Chat é estritamente **privado e vinculado a um pedido de troca ativo**, com mensagens prontas para combinar logística. Não há chat aberto entre desconhecidos.

**Proibições Absolutas (Não Negociáveis)**

* É estritamente proibido o armazenamento de senhas em texto puro ou criptografia reversível.


* As senhas devem obrigatoriamente utilizar algoritmo de hash com salt (ex: BCrypt) no banco local Room.



**Requisitos Técnicos Obrigatórios (Critérios de Avaliação)**

* **Geolocalização (Recurso Principal):** Funcionalidade central da troca presencial. Usada para verificar livros próximos, viabilidade a pé, e embutir um mapa interativo para marcar pontos de encontro seguros na universidade.


* **Tecnologia:** Android nativo (Java), template *Empty Views* ou *Basic Views*.


* **Estrutura de Telas:** Mínimo de 3 telas/Fragments funcionais com navegação livre.


* **Splash Screen:** Tela de abertura nativa gerenciando fluxo (login/principal) sem atrasos artificiais.


* **Personalização:** Ícone e/ou imagem no `AndroidManifest.xml`.


* **Autenticação:** Login exclusivo via e-mail institucional, controle de sessão ativa e logout.


* **Gestão de Perfil:** Edição de dados, alteração de senha (novo hash) e foto (câmera/galeria persistida no Room).


* **Notificações:** Alertas de status de troca, novos livros da lista de desejos e pendências.


* **Permissões:** Declaração e tratamento em tempo de execução (Localização, Câmera, Galeria, Notificações).


* **Testes Caixa-Preta:** Documentação prática: validação de campos, autenticação, câmera, notificações, regras do mapa e restrições de chat/troca.



**Diferenciais e Uso de Skills**

* **Usabilidade (Foco Principal):** Interface intuitiva e tratamento de erros com mensagens claras. Tom minimalista, focado na "tranquilidade de um dia de leitura". Foco Mobile-First.


* Invoque `/design-md-library` (estilo visual), `/design-taste-frontend` (briefing de usabilidade) e `/web-design-guidelines` (auditoria).
* Decisões visuais devem constar na pré-documentação.

**Documentação Exigida**

* **Pré-documentação:** Registro do design, arquitetura e pendências comunitárias (ex: quem resolve disputas, prazo de entrega).
* **Identificação:** Nome de todos os integrantes (1 a 3 membros).


* **Visão Geral:** Resumo explicativo do aplicativo LeiaUFMS.


* **Papéis de Usuários:** Definição clara dos perfis e permissões.


* **Requisitos Funcionais Detalhados:** Regras de créditos, anúncios, trocas, geolocalização e mídias.


* **Relatório de Testes:** Casos de teste executados informando condições iniciais, entradas, resultados e status (aprovado/reprovado).

Aviso de Restrição de Execução (Meta-Diretiva): Esta instrução é um prompt de engenharia estruturado para leitura, organização e planejamento. NÃO inicie a execução de código, não crie o ambiente de desenvolvimento e não simule a aplicação antes de confirmar que compreendeu integralmente o documento PDF original e estas diretrizes operacionais.
