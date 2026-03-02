package com.kalakar.kalakar.mapper;

import com.kalakar.kalakar.dto.UserDTO;
import com.kalakar.kalakar.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity → DTO (never expose password)
    @Mapping(target = "role", source = "role")
    UserDTO toDTO(User user);

    List<UserDTO> toDTOList(List<User> users);
}
