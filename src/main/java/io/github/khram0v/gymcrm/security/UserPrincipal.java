package io.github.khram0v.gymcrm.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final String username;
    private final String password;
    private final boolean enabled;
    private final Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }
}
