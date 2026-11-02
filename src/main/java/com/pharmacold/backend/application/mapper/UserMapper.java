package com.pharmacold.backend.application.mapper;

import com.pharmacold.backend.application.dto.response.UserResponse;
import com.pharmacold.backend.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "username", source = "user", qualifiedByName = "username")
    UserResponse toResponse(User user);

    @Named("username")
    default String mergeUserName(User user) {
        return "%s %s".formatted(user.getFirstName(), user.getLastName());
    }
}
