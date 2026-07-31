package br.com.ams.rent.controller;

import br.com.ams.rent.controller.dto.CreateUserDto;
import br.com.ams.rent.entities.Role;
import br.com.ams.rent.entities.User;
import br.com.ams.rent.repositories.RoleRepository;
import br.com.ams.rent.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository , RoleRepository roleRepository ,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @PostMapping("/users")
    @Transactional
    public ResponseEntity<Void> newUser(@RequestBody CreateUserDto dto) {

        var basicRole = roleRepository.findByName ( Role.Values.BASIC.name () );
        var userFromDb = userRepository.findByUsername ( dto.username () );
        if ( userFromDb .isPresent () ) {
            throw new ResponseStatusException ( HttpStatus.UNPROCESSABLE_CONTENT, "Username already exists" );
        }

        var user = new User (  );
        user.setUsername ( dto.username () );
        user.setPassword ( passwordEncoder.encode ( dto.password()) );
        user.setRoles ( Set.of(basicRole) );
        userRepository.save( user );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    @PreAuthorize ( "hasAuthority('SCOPE_admin')" )
    public ResponseEntity<List<User>> getUsers() {

        var users = userRepository.findAll();
        return ResponseEntity.ok (  users );


    }
    @Autowired
    private JwtEncoder encoder;
    @Autowired
    private JwtDecoder decoder;

    @GetMapping("/test-token")
    public String testToken() {
        var claims = JwtClaimsSet.builder()
                .subject("123")
                .issuedAt(Instant.now())
                .expiresAt( Instant.now().plusSeconds(60))
                .build();
        String token = encoder.encode( JwtEncoderParameters.from(claims)).getTokenValue();
        Jwt decoded = decoder.decode(token); // Se falhar, problema nas chaves
        return "Decoded: " + decoded.getClaims();
    }
}
