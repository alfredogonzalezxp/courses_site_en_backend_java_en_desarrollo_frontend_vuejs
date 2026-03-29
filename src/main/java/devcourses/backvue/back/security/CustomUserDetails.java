package devcourses.backvue.back.security;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import devcourses.backvue.back.model.User;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Un rol simple
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // usamos el email como "username" para Spring Security
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public String getNombre() {
        return user.getNombre(); 
    }


    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public Long getId() {
        return user.getId();
    }
}