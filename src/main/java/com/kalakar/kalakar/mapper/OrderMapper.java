package com.kalakar.kalakar.mapper;

import com.kalakar.kalakar.dto.OrderDTO;
import com.kalakar.kalakar.dto.OrderItemDTO;
import com.kalakar.kalakar.model.Order;
import com.kalakar.kalakar.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", source = "status")
    OrderDTO toDTO(Order order);

    List<OrderDTO> toDTOList(List<Order> orders);

    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    OrderItemDTO itemToDTO(OrderItem item);
}
