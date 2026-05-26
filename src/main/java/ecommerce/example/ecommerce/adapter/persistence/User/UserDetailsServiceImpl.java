package ecommerce.example.ecommerce.adapter.persistence.User;

import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String roleName = "";
        if (user.getRole() != null) {
            roleName = user.getRole().getName();
            if (roleName != null && !roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
        }

        List<GrantedAuthority> authorities = roleName != null && !roleName.isEmpty() ?
                List.of(new SimpleGrantedAuthority(roleName)) :
                List.of();

        // Trả về UserDetails với email làm username chính của Spring Security Principal
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}