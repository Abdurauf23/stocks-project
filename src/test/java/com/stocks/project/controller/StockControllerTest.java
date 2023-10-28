package com.stocks.project.controller;

import com.stocks.project.model.Meta;
import com.stocks.project.repository.StockRepository;
import io.jsonwebtoken.lang.Assert;
import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StockControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockRepository stockRepository;

    @Test
    public void testGetAllStocks() throws Exception {
        List<Meta> metaList = List.of(new Meta(), new Meta());
        given(stockRepository.findAllMeta())
                .willReturn(metaList);

        String body = this.mockMvc
                .perform(get("/stocks"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONArray array = new JSONArray(body);
        int arraySize = array.length();

        Assertions.assertNotNull(array);
        Assertions.assertEquals(metaList.size(), arraySize);
    }

    @Test
    public void testGetStockBySymbol() throws Exception {
        String symbol = "AAPL";
        MvcResult mvcResult = this.mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/stocks/" + symbol)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }
}
