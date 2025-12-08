package com.apitesting.all;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import com.apigateway.model.ERole;
import com.apigateway.model.Role;
import com.apigateway.model.User;
import com.apigateway.service.UserDetailsImpl;

import org.junit.jupiter.api.Test;

class UserDetailsImplTest {

    @Test
    void build_fromUser_setsAuthorities() {
        User u = new User();
        u.setId(2L);
        u.setUsername("charlie");
        u.setEmail("c@example.com");
        u.setPassword("pwd");

        Role r = new Role();
        r.setId((int) 1L);
        r.setName(ERole.ROLE_USER);

        u.setRoles(Set.of(r));

        UserDetailsImpl ud = UserDetailsImpl.build(u);
        assertTrue(ud.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}

