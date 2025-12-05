package com.example.universidadeESN3.exception;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private Integer status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
