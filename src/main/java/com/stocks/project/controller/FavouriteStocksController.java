package com.stocks.project.controller;

import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.ErrorModel;
import com.stocks.project.model.StockValue;
import com.stocks.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/fav-stocks")
@SecurityRequirement(name = "Bearer Authentication")
public class FavouriteStocksController {
    private final UserService userService;

    @Autowired
    public FavouriteStocksController(UserService userService) {
        this.userService = userService;
    }

    @Operation(description = "Get favorite stocks for particular stockUser.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "For User: If stockUser wants to see others favourite stocks",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "For Admin and User: Admin can access anyone. " +
                            "User can access only his fav stocks.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockValue.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getFavouriteStocks(@PathVariable int userId, Principal principal) {
        String login = principal.getName();

        if (userService.isAdmin(login) || userService.isSame(login, userId)) {
            return new ResponseEntity<>(userService.getAllFavouriteStocks(userId), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Operation(description = "Add stocks to favourite.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "For User: If user wants to add fav stock for other user",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "For Admin: If user was not found in the database. Or also " +
                            "For Admin and User: If stock with provided name was not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "200",
                    description = "For Admin and User: Successfully added stock to the list of fav. ",
                    content = @Content)
    })
    @PostMapping("/{userId}/{stockName}")
    public ResponseEntity<?> addStockToFavourite(@PathVariable int userId,
                                                 @PathVariable String stockName,
                                                 Principal principal) {
        String login = principal.getName();

        if (!(userService.isAdmin(login) || userService.isSame(login, userId))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            userService.addStockToFavourite(userId, stockName);
            return new ResponseEntity<>(HttpStatus.CREATED);
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
                            }""");
        }
    }

    @Operation(description = "Delete stock from the list of favourites.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "For User: If user wants to delete fav stock for other user",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "For Admin: If user was not found in the database. Or also " +
                            "For Admin and User: If stock with provided name was not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "204",
                    description = "For Admin and User: Successfully deleted stock from the list of fav. ",
                    content = @Content)
    })
    @DeleteMapping("/{userId}/{stockName}")
    public ResponseEntity<?> deleteFromFavourite(@PathVariable int userId,
                                                 @PathVariable String stockName,
                                                 Principal principal) {
        String login = principal.getName();

        if (!(userService.isAdmin(login) || userService.isSame(login, userId))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
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
