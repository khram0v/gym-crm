package io.github.khram0v.gymcrm.security;

public enum Role {
    TRAINEE,
    TRAINER;

    public String authority() {
        return "ROLE_" + name();
    }
}
