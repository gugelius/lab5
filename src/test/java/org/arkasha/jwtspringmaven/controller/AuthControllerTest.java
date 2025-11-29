package org.arkasha.jwtspringmaven.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.arkasha.jwtspringmaven.dto.JwtAuthenticationDto;
import org.arkasha.jwtspringmaven.dto.UserCredentialsDto;
import org.arkasha.jwtspringmaven.security.jwt.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
    void singInTest() throws Exception {
        UserCredentialsDto userCredentialsDto = new UserCredentialsDto();
        userCredentialsDto.setEmail("test@gmail.com");
        userCredentialsDto.setPassword("12345dsafasfasfasfa");

        String userJson = objectMapper.writeValueAsString(userCredentialsDto);

        String tokenJson = mockMvc.perform(MockMvcRequestBuilders.post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtAuthenticationDto jwtAuthenticationDto = objectMapper.readValue(tokenJson, JwtAuthenticationDto.class);

        Assertions.assertEquals(userCredentialsDto.getEmail(), jwtService.getEmailFromToken(jwtAuthenticationDto.getToken()));
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
    void refresh() {
    }
}