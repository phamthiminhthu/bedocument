package com.hust.edu.vn.common.type;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomResponse {
    private int responseCode;
    private String description;
    private Object content;

    public static ResponseEntity<CustomResponse> generateResponse(HttpStatus httpStatus, Object content){
        ResponseEntity.BodyBuilder bodyBuilder= ResponseEntity.status(httpStatus);
        CustomResponse customResponse = new CustomResponse(httpStatus.value(), httpStatus.getReasonPhrase(), content);
        return bodyBuilder.body(customResponse);
    }
    public static ResponseEntity<CustomResponse> generateResponse(HttpStatus httpStatus){
        ResponseEntity.BodyBuilder bodyBuilder= ResponseEntity.status(httpStatus);
        CustomResponse customResponse = CustomResponse.builder()
                .responseCode(httpStatus.value())
                .description(httpStatus.getReasonPhrase())
                .build();
        return bodyBuilder.body(customResponse);
    }
    public static ResponseEntity<CustomResponse> generateResponse(boolean status){
        HttpStatus httpStatus = status ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        ResponseEntity.BodyBuilder bodyBuilder= ResponseEntity.status(httpStatus);
        CustomResponse customResponse = CustomResponse.builder()
                .responseCode(httpStatus.value())
                .description(httpStatus.getReasonPhrase())
                .build();
        return bodyBuilder.body(customResponse);
    }
    public static ResponseEntity<CustomResponse> generateResponse(HttpStatus httpStatus, String description, Object content){
        ResponseEntity.BodyBuilder bodyBuilder= ResponseEntity.status(httpStatus);
        CustomResponse customResponse = CustomResponse.builder()
                .responseCode(httpStatus.value())
                .description(description)
                .content(content)
                .build();
        return bodyBuilder.body(customResponse);
    }
}
