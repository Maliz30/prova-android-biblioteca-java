package biblioteca;

import biblioteca.cli.Command;
import biblioteca.cli.Console;
import biblioteca.data.Library;
import biblioteca.model.Book;
import biblioteca.service.LibraryService;
import biblioteca.service.LoanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        LibraryService service = new LibraryService(new Library());
        Scanner scanner = new Scanner(System.in);

        Console.title("Biblioteca");
        Console.info("Digite 'ajuda' para ver os comandos, 'sair' para encerrar.");

        while (true) {
            System.out.print("\n> ");
            if (!scanner.hasNextLine()) {
                return;
            }

            Optional<Command> parsed = Command.parse(scanner.nextLine());
            if (parsed.isEmpty()) {
                continue;
            }
            Command command = parsed.get();

            switch (command.name()) {
                case "ajuda" -> showHelp();
                case "listar" -> showCatalog(service);
                case "sair" -> {
                    Console.info("Até mais.");
                    return;
                }
                // TODO (Tarefa 4): implementar os comandos novos aqui.
                case "membro" ->
                        Console.error("comando '" + command.name() + "' ainda não implementado.");
                case "buscar" -> {
                    String arguments = String.join(" ", command.arguments());
                    searchBookCatalog(service, arguments);
                }
                case "emprestar" -> {
                    Optional<String> bookId = command.argument(0);
                    Optional<String> memberId = command.argument(1);
                    handleBorrowBook(service, bookId, memberId);
                }
                case "devolver" -> {
                    Optional<String> bookId = command.argument(0);
                    Optional<String> memberId = command.argument(1);
                    handleReturnBook(service, bookId, memberId);
                }
                default ->
                        Console.error("não conheço o comando '" + command.name() + "'. Tente 'ajuda'.");
            }
        }
    }

    private static void showHelp() {
        Console.title("Comandos");
        Console.table(
                List.of("comando", "o que faz"),
                List.of(
                        List.of("listar", "mostra o acervo"),
                        List.of("buscar <termo>", "procura por título, autor ou gênero"),
                        List.of("emprestar <livro> <membro>", "empresta um exemplar a um membro"),
                        List.of("devolver <livro> <membro>", "devolve um exemplar"),
                        List.of("membro <id>", "mostra os empréstimos de um membro"),
                        List.of("ajuda", "mostra esta lista"),
                        List.of("sair", "encerra o programa")));
    }

    /**
     * Comando de referência: se ficar em dúvida sobre estilo, copie o que está
     * aqui.
     */
    private static void showCatalog(LibraryService service) {
        Console.title("Acervo");

        printBookList(service, service.catalog());
    }

    /**
     * Livros que batem com o termo buscado.
     *
     * @param searchText termo buscado.
     */
    private static void searchBookCatalog(LibraryService service, String searchText) {
        if (searchText.isBlank()) {
            Console.error("É necessário informar ao menos uma palavra para realizar a busca.");
            return;
        }

        List<Book> foundBooks = service.searchBook(searchText);
        if (foundBooks.isEmpty()) {
            Console.error("Não foram encontrados exemplares com os parâmetros informados: " + searchText + ".");
            return;
        }

        Console.title("Exemplares encontrados");

        printBookList(service, foundBooks);
    }

    /**
     * Monta e imprime a tabela de livros.
     */
    private static void printBookList(LibraryService service, List<Book> books) {
        List<List<String>> rows = new ArrayList<>();

        for (Book book : books) {
            rows.add(List.of(
                    String.valueOf(book.id()),
                    book.title(),
                    book.author(),
                    book.genre(),
                    String.valueOf(service.availableCopies(book))));
        }

        Console.table(List.of("id", "título", "autor", "gênero", "exemplares disponíveis"), rows);
    }

    private static boolean isInteger(String str) {
        return str.matches("^[0-9]+$");
    }

    /**
     * Empresta um livro a um membro.
     */
    private static void handleBorrowBook(LibraryService service, Optional<String> bookId, Optional<String> memberId) {
        if (!validateIds(bookId, memberId)){
            return;
        }

        LoanResult loanResult = service.borrowBook(Integer.parseInt(bookId.get()), Integer.parseInt(memberId.get()));

        printLoanMessage(loanResult);
    }

    /**
     * Devolve um livro emprestado por um membro.
     */
    private static void handleReturnBook(LibraryService service, Optional<String> bookId, Optional<String> memberId) {
        if (!validateIds(bookId, memberId)){
            return;
        }

        LoanResult loanResult = service.returnBook(Integer.parseInt(bookId.get()), Integer.parseInt(memberId.get()));

        printLoanMessage(loanResult);
    }

    /**
     * Valida os ids de livro e membro recebidos do comando, reportando o
     * motivo com {@code Console.error} quando algum deles está ausente ou
     * não é um número.
     */
    private static boolean validateIds(Optional<String> bookId, Optional<String> memberId) {
        if (bookId.isEmpty()) {
            Console.error("É necessário informar o id do livro.");
            return false;
        }
        if (!isInteger(bookId.get())) {
            Console.error("O id informado para o livro é inválido.");
            return false;
        }

        if (memberId.isEmpty()) {
            Console.error("É necessário informar o id do membro.");
            return false;
        }
        if (!isInteger(memberId.get())) {
            Console.error("O id informado para o membro é inválido.");
            return false;
        }

        return true;
    }

    /**
     * Mostra o resultado de uma operação de empréstimo ou devolução.
     */
    private static void printLoanMessage(LoanResult loanResult) {
        if (loanResult.success()) {
            Console.info(loanResult.message());
        } else {
            Console.error(loanResult.message());
        }
    }
}
