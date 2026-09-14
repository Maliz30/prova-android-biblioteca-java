package biblioteca.service;

import biblioteca.data.Library;
import biblioteca.model.Book;
import biblioteca.model.Loan;

import java.util.List;

/**
 * Onde moram as regras da biblioteca.
 *
 * <p>Nada aqui dentro imprime na tela nem lê do teclado.
 * Esta classe recebe perguntas e devolve dados. Quem conversa com o usuário é a
 * camada de {@code cli}.
 */
public class LibraryService {

    private final Library library;

    public LibraryService(Library library) {
        this.library = library;
    }

    /**
     * O acervo inteiro, na ordem em que está cadastrado.
     */
    public List<Book> catalog() {
        return library.books();
    }

    /**
     * Quantos exemplares de um livro estão livres para empréstimo agora.
     *
     * @param book livro cuja disponibilidade será calculada.
     */
    public int availableCopies(Book book) {
        int availableUnits = book.copies();

        for (Loan loan : library.loans()) {
            if (loan.bookId() == book.id()) {
                availableUnits--;
            }
        }

        return availableUnits;
    }

    // TODO (Tarefa 2): busca por título, autor ou gênero.

    // TODO (Tarefa 3): emprestar e devolver, com as regras do enunciado.

    // TODO (Tarefa 4): o que um membro tem em mãos, e o que está atrasado.
}
