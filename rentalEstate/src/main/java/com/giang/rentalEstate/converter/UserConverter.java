package com.giang.rentalEstate.converter;

import com.giang.rentalEstate.dto.PropertyCreateOrUpdateDTO;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.dto.UserDTO;
import com.giang.rentalEstate.dto.UserUpdateDTO;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConverter {
    private final ModelMapper modelMapper;
    public UserDTO toUserDTO(User item){
        UserDTO user = modelMapper.map(item, UserDTO.class);
        return user;
    }
    public User toUserEntity(UserUpdateDTO userUpdateDTO){
        return modelMapper.map(userUpdateDTO, User.class);
    }
}
