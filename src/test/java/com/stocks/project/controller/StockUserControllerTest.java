package com.stocks.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.project.repository.UserRepository;
import com.stocks.project.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class StockUserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() {
    }

    @Test
    public void testGetAllNotAuthenticated() throws Exception {
        MvcResult mvcResult = this.mockMvc
                .perform(get("/users"))
                .andExpect(status().isForbidden())
                .andReturn();

        System.out.println(mvcResult.getResponse().getStatus());
    }

    @Test
    public void testGetAllAdmin() throws Exception {
        // authenticate
        String body = this.mockMvc.perform(
                post("/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "admin",
                                    "password": "adminroot"
                                }"""))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // get JWT token from response
        String token = (String) objectMapper.readValue(body, Map.class).get("token");

        MvcResult mvcResult = this.mockMvc
                .perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }
}
