package br.com.ams.rent.security;

import br.com.ams.rent.entities.Role;
import br.com.ams.rent.entities.User;
import br.com.ams.rent.repositories.RoleRepository;
import br.com.ams.rent.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public AdminUserConfig(UserRepository userRepository ,
                           RoleRepository roleRepository ,
                           BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = bCryptPasswordEncoder;
    }


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        var roleAdmin = roleRepository
                .findByName (  Role.Values.ADMIN.name () );
        var userAdmin = userRepository.findByUsername ( "admin" );
        userAdmin.ifPresentOrElse (
                uswer -> {

                    System.out.println ( "adminja existe" );
                } ,

                () -> {
                    var user = new User ();
                    user.setUsername ( "admin" );
                    user.setPassword ( passwordEncoder.encode ( "123" ) );
                    user.setRoles ( Set.of ( roleAdmin ) );
                    userRepository.save ( user );
                }

        );
    }
}
