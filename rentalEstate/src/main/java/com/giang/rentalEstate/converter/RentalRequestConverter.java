package com.giang.rentalEstate.converter;

import com.giang.rentalEstate.dto.PropertyCreateOrUpdateDTO;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.dto.RentalRequestDTO;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.PropertyImage;
import com.giang.rentalEstate.model.RentalRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RentalRequestConverter {
    private final ModelMapper modelMapper;

    public RentalRequestDTO toRentalRequestDTO(RentalRequest item){
        RentalRequestDTO rentalRequestDTO = modelMapper.map(item, RentalRequestDTO.class);
        // Map danh sách URL ảnh

        return rentalRequestDTO;
    }

//    public Property toPropertyEntity(PropertyCreateOrUpdateDTO propertyDTO){
//        return modelMapper.map(propertyDTO, Property.class);
//    }
}
