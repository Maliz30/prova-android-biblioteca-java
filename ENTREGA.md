# Entrega prova teste prático android

Maria Alice Bernardo da Costa Silva

**Link do repositório:** https://github.com/Maliz30/prova-android-biblioteca-java

> O repositório está privado e enviei convite para o @ViniciusEvo como colaborador. Caso desejem que torne o repositório público ou adicione algum outro avaliador como membro para que possa visualizar o repositório além do .zip enviado, basta me contactar via email marialice3003@gmail.com ou whatsapp (61) 9 8466-2347. 

## Decisões técnicas tomadas

Antes de começar, analisei a arquitetura em camadas já existente no projeto (`cli` → `service` → `data`/`model`) e decidi mantê-la no formato original. As responsabilidades de cada camada já estavam claras e bem definidas, então não vi necessidade de mudar a estrutura pra resolver as quatro tarefas. Essa separação me lembrou, em conceito, o padrão MVVM (`cli` fazendo o papel da View, `service` o do ViewModel, `data`/`model` o do Model). Não domino o padrão, mas reconheço a ideia de separar quem fala com o usuário, de quem tem a regra de negócio, e de quem guarda os dados.

**Tarefa 1 — disponibilidade no `listar`**

Calculei os exemplares disponíveis com Stream (`filter` + contagem), descontando os empréstimos ainda não devolvidos do total de cópias (`!wasReturned()`). No começo usei um `for-each` contando a quantidade de empréstimos em Loan e decrescendo da quantidade total de cópias, porque `!wasReturned()` ainda não havia sido implementado e nunca havia usado Stream. Dessa forma, preferi entregar algo que dominava com segurança. Entretanto, troquei para Stream depois de fechar a Tarefa 2, após ficar mais confiante com o uso do método e de forma a ficar consistente com `searchBook`/`findBook`/`findMember`.

**Tarefa 2 — `buscar`**
- Busca por título, autor ou gênero com Stream.
- Ignora maiúsculas/minúsculas e acentuação: normalizo o texto com `java.text.Normalizer` (biblioteca padrão do Java), decompondo letras acentuadas (NFD) e removendo as marcas de acento.
- O termo pode ter mais de uma palavra, pois foi feita a reconstrução dos argumentos utilizando `String.join(" ", command.arguments())`.
- Mensagens de erro específicas para dois casos, em vez de deixar cair no "(nada para mostrar)" genérico do `Console.table`:
  - argumentos ausentes: "É necessário informar ao menos uma palavra para realizar a busca."
  - termo que não encontra nenhum livro (Inclui o termo buscado, pra deixar claro o que foi procurado.): "Não foram encontrados exemplares com os parâmetros informados: `<termo>`.". 
  
  Nos dois casos o método retorna antes de montar a tabela, que ficaria vazia.

**Tarefa 3 — `emprestar`/`devolver`**
- **Regra de negócio adicional**: o dia do empréstimo não conta na contagem dos 14 dias de prazo.
- Uso o **id** de livro e membro (não o nome), já que nomes podem se repetir entre membros/livros diferentes.
- Mantenho o histórico de empréstimos em vez de apagar ao devolver, pois em um sistema real de empréstimos esses dados podem ser úteis. Na implementação realizada um empréstimo devolvido vira um `Loan` com `wasReturned = true`.
- Guardo um booleano (`wasReturned`) em vez da data esperada de devolução, porque essa data é um dado calculado e violaria a 3ª Forma Normal num banco relacional.
- Como `Loan` é um `record` (imutável), devolver um livro cria uma cópia com `wasReturned = true` e substitui a posição antiga na lista (`Library.loans()`, que é mutável); o objeto antigo fica sem referência e é liberado pelo Garbage Collector.
- Inicialmente, o retorno de `borrowBook`/`returnBook` usava uma `String` só, sem distinguir sucesso de recusa. Criei o `record LoanResult(boolean success, String message)` para isso. Fiquei em dúvida se deveria criar um pacote próprio pra esse tipo (um DTO), mas, dado o tamanho do projeto, decidi mantê-lo dentro do pacote `service` mesmo. Ele não é uma entidade do domínio, por isso não deveria ficar em `model`.

**Tarefa 4 — `membro <id>`**
- Adicionei `findBook`/`findMember` na `LibraryService`, espelhando os métodos que já existiam em `Library`, para o `Main` não acessar a camada de dados diretamente.
- Unifiquei o cálculo de atraso: extraí `calculateReturnDate` (data do empréstimo + 15 dias, considerando a nova regra de negócio sobre o dia de emprestimo não entrar na contagem de dias) e `isLoanLate` para a `LibraryService`, e fiz `borrowBook` (Tarefa 3) usar `isLoanLate` em vez de recalcular a mesma coisa na mão. Assim as duas tarefas usam a mesma regra e temos uma única fonte da verdade.
- Formato a data com `DateTimeFormatter` (`dd/MM/yyyy`, biblioteca padrão do Java), mais legível que o padrão ISO do `LocalDate`.
- Valido se o membro existe (não só se o id é numérico), consistente com `emprestar`/`devolver`.

## O que mudou no que já existia (e o que incomodou)

- `Main.java`: a coluna "exemplares" do `listar` passou a mostrar "exemplares disponíveis" em vez do total e foi adicionado o comando `relatorio` para exibir todos os empréstimos em atraso.
- Extraí a extrutura de montagem da tabela para `printBookList` para reaproveitar entre `listar` e `buscar`.
- `Loan.java`: adicionei o campo `wasReturned` (booleano) pra viabilizar a devolução (Tarefa 3). O record original, do projeto base, não tinha esse campo. Isso também exigiu atualizar os dados de exemplo em `Library.java`, já que cada `Loan` do seed passou a precisar do quarto argumento.
- `.gitignore`: adicionei `bin/`, pasta de build da IDE que não deveria ser versionada.


## Testes automatizados

Cobri a `LibraryService` com testes em JUnit 5 (`LibraryServiceTest.java`), validando as regras de empréstimo, devolução, atraso e busca.

Um cenário que vale citar é o limite exato do prazo de devolução: empréstimo feito há 15 dias (ainda não atrasado) e outro feito há 16 dias (já atrasado). Essa é a parte da regra de negócio mais propensa a um erro de contagem por um dia.

Deixei de fora métodos simples de delegação, como `catalog`, `findBook` e `findMember`, já que acabam sendo validados pelos outros testes de qualquer forma.

## O que ficou de fora (e o motivo)

O item de bônus de persistência em arquivo, ficou de fora porque considero a tarefa mais extensa. Além disso, precisaria pesquisar sobre, já que nunca realizei essa implementação em java. Dessa forma, escolhi focar em terminar as 4 tarefas obrigatórias, realizar os testes automatizados e implementar a geração do relatório.

## O que eu faria diferente com mais tempo

- Incluiria o nome do membro e do livro nas mensagens de erro para ficarem mais claras (atualmente várias citam somente o motivo da recusa, sem retornar ao usuário o dado que foi utilizado, ex: "O membro informado não foi encontrado").
- Analisando minha implementação, vejo que talvez fosse melhor guardar a data real da devolução em vez de um booleano, permitindo saber se um empréstimo já devolvido foi devolvido em atraso ou se ainda não foi devolvido (para a data de devolução `null`). Acredito que em um banco de dados de um sistema real esse dado poderia ter utilidade, porém isso varia de acordo com as regras de negócios definidas para o sistema.
- Implementaria a persistência de dados em arquivo. 
