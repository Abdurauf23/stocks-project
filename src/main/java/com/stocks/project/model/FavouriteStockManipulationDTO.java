package com.stocks.project.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FavouriteStockManipulationDTO {
    private String symbol;
    private int userId;
}
