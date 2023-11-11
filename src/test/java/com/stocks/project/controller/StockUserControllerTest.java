package com.stocks.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.project.model.StockUser;
import com.stocks.project.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase
public class StockUserControllerTest {
    static List<StockUser> users = new ArrayList<>();
    static StockUser stockUser = StockUser.builder().firstName("James").build();
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void beforeAll() {
        stockUser.setUserId(5);
        users.add(stockUser);
    }

    @Test
    public void getUsersTest() throws Exception {
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].userId", Matchers.equalTo(5)));
    }

    @Test
    public void createUserTest() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(Optional.of(stockUser));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockUser)))
                .andExpect(status().isCreated());
    }

    @Test
    public void updateUserTest() throws Exception {
        when(userService.updateUser(any(), anyInt()))
                .thenReturn(Optional.of(stockUser));
        when(userService.isAdmin("admin"))
                .thenReturn(true);

        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockUser)))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserTest() throws Exception {
        UserService mockUS = Mockito.mock(UserService.class);
        doNothing().when(mockUS).delete(anyInt());
        when(userService.isAdmin("admin"))
                .thenReturn(true);

        mockMvc.perform(delete("/users/10"))
                .andExpect(status().isNoContent());
    }

}
