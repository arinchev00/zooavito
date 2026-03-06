package com.example.zooavito.Exception;

public enum BusinessErrorType {
    EMAIL_ALREADY_EXISTS("email", "Пользователь с таким email уже существует"),
    PASSWORDS_DO_NOT_MATCH("confirmPassword", "Пароли не совпадают");

    private final String field;
    private final String defaultMessage;

    BusinessErrorType(String field, String defaultMessage) {
        this.field = field;
        this.defaultMessage = defaultMessage;
    }

    public String getField() {
        return field;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
