package com.pharmacold.backend.application.mapper;

import com.pharmacold.backend.application.dto.response.UserResponse;
import com.pharmacold.backend.domain.model.User;
import com.pharmacold.backend.application.mapper.config.ApplicationMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        config = ApplicationMapperConfig.class
)
public interface UserMapper {

    @Mapping(target = "fullName", source = "user", qualifiedByName = "fullName")
    UserResponse toResponse(User user);

    @Named("fullName")
    default String formatName(User user) {
        return "%s %s".formatted(user.getFirstName(), user.getLastName());
    }
}
