package com.bank;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ServiceResponse {

    private String service;
    private Integer code;
    private boolean success;
    private List<String> errors;
}
