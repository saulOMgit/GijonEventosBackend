package dev.saul.gijoneventos.register;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("El usuario con DNI '" + username + "' ya existe.");
    }
}