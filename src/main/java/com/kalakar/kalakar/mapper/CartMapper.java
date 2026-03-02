package com.kalakar.kalakar.mapper;

import com.kalakar.kalakar.dto.CartDTO;
import com.kalakar.kalakar.dto.CartItemDTO;
import com.kalakar.kalakar.model.Cart;
import com.kalakar.kalakar.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "subtotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    CartDTO toDTO(Cart cart);

    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    CartItemDTO itemToDTO(CartItem item);
}
