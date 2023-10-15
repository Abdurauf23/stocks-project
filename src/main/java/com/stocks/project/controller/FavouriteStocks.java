package com.stocks.project.controller;

import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class FavouriteStocks {
    private final UserService userService;

    public FavouriteStocks(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/fav-stocks")
    public ResponseEntity<?> getFavouriteStocks(@PathVariable int userId) {
        return new ResponseEntity<>(userService.getAllFavouriteStocks(userId), HttpStatus.OK);
    }

    @PostMapping("/{userId}/fav-stocks/{stockName}")
    public ResponseEntity<?> addStockToFavourite(@PathVariable int userId,
                                                 @PathVariable String stockName) {
        try {
            userService.addStockToFavourite(userId, stockName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoStockWithThisNameException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "error" : "No such stock with this name. Check spelling"\s
                            }
                            """);
        } catch (NoSuchUserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "error" : "No such user"\s
                            }
                            """);
        }
    }

    @DeleteMapping("/{userId}/fav-stocks/{stockName}")
    public ResponseEntity<?> deleteFromFavourite(@PathVariable int userId,
                                                 @PathVariable String stockName) {
        try {
            userService.deleteStockFromFavourite(userId, stockName);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoStockWithThisNameException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "error" : "No such stock with this name. Check spelling"\s
                            }
                            """);
        } catch (NoSuchUserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            {
                                "error" : "No such user"\s
                            }
                            """);
        }
    }

}
