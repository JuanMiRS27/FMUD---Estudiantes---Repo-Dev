package com.fmud.auth.infrastructure.adapter.in.web;

import com.fmud.auth.application.dto.AuthenticatedUserDto;
import com.fmud.auth.application.dto.LoginResultDto;
import com.fmud.auth.application.port.in.GetCurrentUserUseCase;
import com.fmud.auth.application.port.in.LoginUseCase;
import com.fmud.auth.application.port.out.PasswordHasherPort;
import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @MockitoBean
    private UserRepositoryPort userRepositoryPort;

    @MockitoBean
    private PasswordHasherPort passwordHasherPort;

    @Test
    void loginReturnsTokenAndUser() throws Exception {
        UUID id = UUID.randomUUID();
        when(loginUseCase.login(any())).thenReturn(new LoginResultDto(
                "jwt-token",
                "Bearer",
                3600,
                new AuthenticatedUserDto(id, "Usuario Secretaría", "secretario@fmud.local", UserRole.SECRETARIO)
        ));

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"secretario@fmud.local","password":"Cambiar123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.role").value("SECRETARIO"));
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointRejectsSecretario() throws Exception {
        mvc.perform(get("/api/auth/admin-check").with(user("secretario@fmud.local").roles("SECRETARIO")))
                .andExpect(status().isForbidden());
    }
}
