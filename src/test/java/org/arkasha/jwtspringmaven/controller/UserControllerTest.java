package org.arkasha.jwtspringmaven.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.arkasha.jwtspringmaven.dto.JwtAuthenticationDto;
import org.arkasha.jwtspringmaven.dto.UserCredentialsDto;
import org.arkasha.jwtspringmaven.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAccessToken() throws Exception {
        UserCredentialsDto userCredentialsDto = new UserCredentialsDto();
        userCredentialsDto.setEmail("test@gmail.com");
        /*** TEST BREAK ***/
        userCredentialsDto.setPassword("12345");

        String loginJson = objectMapper.writeValueAsString(userCredentialsDto);

        String tokens = mockMvc.perform(MockMvcRequestBuilders.post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtAuthenticationDto jwtAuthenticationDto = objectMapper.readValue(tokens, JwtAuthenticationDto.class);

        return jwtAuthenticationDto.getToken();
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql"})
    void createUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setFirstName("test");
        userDto.setLastName("test1");
        userDto.setEmail("te@gmail.com");
        userDto.setPassword("111222333");

        String userJson = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
    void getUserById() throws Exception {
        String token = getAccessToken();

        mockMvc.perform(MockMvcRequestBuilders.get("/user/550e8400-e29b-41d4-a716-446655440000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
    void getUserByEmail() throws Exception {
        String token = getAccessToken();

        mockMvc.perform(MockMvcRequestBuilders.get("/user/email/test@gmail.com")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));

    }
}