package com.stocks.project.controller;

import com.stocks.project.exception.NoStockWithThisNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.ErrorModel;
import com.stocks.project.model.FavouriteStockManipulationDTO;
import com.stocks.project.model.StockValue;
import com.stocks.project.security.model.SecurityCredentials;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
import com.stocks.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/fav-stocks")
@SecurityRequirement(name = "Bearer Authentication")
public class FavouriteStocksController {
    private final UserService userService;
    private final SecurityCredentialsRepository credentialsRepository;

    @Autowired
    public FavouriteStocksController(UserService userService,
                                     SecurityCredentialsRepository credentialsRepository) {
        this.userService = userService;
        this.credentialsRepository = credentialsRepository;
    }

    @Operation(description = "Get favorite stocks for self.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "Not authenticated",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "Get the list of self favourite stocks.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockValue.class))),
            @ApiResponse(responseCode = "500",
                    description = "Something went wrong",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getFavouriteStocks(Principal principal) {
        String login = principal.getName();
        Optional<SecurityCredentials> byUserLogin = credentialsRepository.findByUserLogin(login);
        if (byUserLogin.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        int userId = byUserLogin.get().getId();
        return new ResponseEntity<>(userService.getAllFavouriteStocks(userId), HttpStatus.OK);
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
            @ApiResponse(responseCode = "201",
                    description = "For Admin and User: Successfully added stock to the list of fav. ",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> addStockToFavourite(@RequestBody FavouriteStockManipulationDTO dto)
            throws NoStockWithThisNameException, NoSuchUserException {
        int userId = dto.getUserId();
        String stockName = dto.getSymbol();
        userService.addStockToFavourite(userId, stockName);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
    @DeleteMapping
    public ResponseEntity<?> deleteFromFavourite(@RequestBody FavouriteStockManipulationDTO dto)
            throws NoStockWithThisNameException, NoSuchUserException {
        int userId = dto.getUserId();
        String stockName = dto.getSymbol();
        userService.deleteStockFromFavourite(userId, stockName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
