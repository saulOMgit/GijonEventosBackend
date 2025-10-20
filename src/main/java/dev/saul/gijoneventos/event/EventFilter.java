package dev.saul.gijoneventos.event;

public enum EventFilter {
    ALL("Todos"),
    ATTENDING("Mis Asistencias"),
    ORGANIZED("Organizados");

    private final String value;

    EventFilter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}