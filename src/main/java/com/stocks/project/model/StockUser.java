package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.sql.Date;
import java.sql.Timestamp;

@AllArgsConstructor
@Data
@Builder
public class StockUser {
    private int userId;
    private String firstName;
    private String secondName;
    private Date birthday;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isDeleted;
}
