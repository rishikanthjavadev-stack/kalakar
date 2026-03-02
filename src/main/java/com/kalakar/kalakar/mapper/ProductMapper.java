package com.kalakar.kalakar.mapper;

import com.kalakar.kalakar.dto.ProductCreateDTO;
import com.kalakar.kalakar.dto.ProductDTO;
import com.kalakar.kalakar.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Entity → DTO
    ProductDTO toDTO(Product product);

    // List of entities → List of DTOs
    List<ProductDTO> toDTOList(List<Product> products);

    // CreateDTO → Entity (ignore imageUrl, set separately)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    Product toEntity(ProductCreateDTO dto);

    // Update existing entity from CreateDTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    void updateEntityFromDTO(ProductCreateDTO dto, @MappingTarget Product product);
}
