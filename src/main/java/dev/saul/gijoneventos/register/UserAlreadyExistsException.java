package dev.saul.gijoneventos.register;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super("El usuario '" + username + "' ya existe.");
    }
}