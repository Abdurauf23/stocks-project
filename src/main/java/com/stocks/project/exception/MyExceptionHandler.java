package com.stocks.project.exception;

import com.stocks.project.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoFirstNameException.class)
    public ResponseEntity<ErrorModel> handelNoFirstNameException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorModel(400, "'firstName' is required to create a user."));
    }

    @ExceptionHandler(NoSuchUserException.class)
    public ResponseEntity<ErrorModel> handelNoSuchUserException() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorModel(404, "User with given id does not exist."));
    }

    @ExceptionHandler(NoStockWithThisNameException.class)
    public ResponseEntity<ErrorModel> handelNoStockWithThisNameException() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorModel(404, "No stock with this name"));
    }

    @ExceptionHandler(NotEnoughDataException.class)
    public ResponseEntity<ErrorModel> handelNotEnoughDataException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorModel(400, "Not enough data"));
    }

    @ExceptionHandler(EmailOrUsernameIsAlreadyUsedException.class)
    public ResponseEntity<ErrorModel> handelEmailOrUsernameIsAlreadyUsedException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorModel(400, "Email or username is already used"));
    }
}
