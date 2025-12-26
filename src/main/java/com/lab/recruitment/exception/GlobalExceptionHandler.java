package com.lab.recruitment.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<String> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        
        if (message.contains("Duplicate entry") && message.contains("username")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("DB_DUPLICATE_USERNAME");
        } else if (message.contains("Duplicate entry") && message.contains("email")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("DB_DUPLICATE_EMAIL");
        } else if (message.contains("Duplicate entry") && message.contains("phone")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("DB_DUPLICATE_PHONE");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("数据冲突，请检查输入信息");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
            return handleSQLIntegrityConstraintViolation((SQLIntegrityConstraintViolationException) e.getCause());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("数据完整性错误，请检查输入信息");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}