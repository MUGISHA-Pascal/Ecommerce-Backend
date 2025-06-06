package com.starter.mugisha.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CartItemDTO {
    private UUID productCode;
    private int quantity;
}
