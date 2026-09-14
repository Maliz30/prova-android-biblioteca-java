package biblioteca.service;

/**
 * Resultado de uma operação de empréstimo ou devolução.
 *
 * @param success indica se a operação foi realizada.
 * @param message mensagem de sucesso ou o motivo da recusa.
 */
public record LoanResult(boolean success, String message) {
}
