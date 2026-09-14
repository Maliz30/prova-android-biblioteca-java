package biblioteca.service;

import biblioteca.data.Library;
import biblioteca.model.Book;
import biblioteca.model.Loan;

import java.text.Normalizer;
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

    /**
     * Livros cujo título, autor ou gênero contém o termo buscado.
     *
     * @param searchText termo buscado.
     */
    public List<Book> searchBook(String searchText) {
        String normalizedSearch = normalize(searchText);

        return library.books().stream()
            .filter(book ->
                normalize(book.title()).contains(normalizedSearch) ||
                normalize(book.author()).contains(normalizedSearch) ||
                normalize(book.genre()).contains(normalizedSearch))
            .toList();
    }

    /**
     * Remove acentos e caixa alta de um texto, para comparação na busca.
     *
     * @param text texto a ser normalizado.
     */
    private String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase();
    }

    // TODO (Tarefa 3): emprestar e devolver, com as regras do enunciado.

    // TODO (Tarefa 4): o que um membro tem em mãos, e o que está atrasado.
}
