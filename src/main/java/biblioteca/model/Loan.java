package biblioteca.model;

import java.time.LocalDate;

/**
 * Um empréstimo.
 *
 * <p>Vem com o mínimo: quem pegou, o que pegou e quando. Se precisar de mais
 * informação para resolver as tarefas, sinta-se livre para mudar este tipo.
 */
public record Loan(int bookId, int memberId, LocalDate borrowedAt, boolean wasReturned) {
}
