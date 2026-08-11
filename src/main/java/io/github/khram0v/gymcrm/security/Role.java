package io.github.khram0v.gymcrm.security;

public enum Role {
    TRAINEE,
    TRAINER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
