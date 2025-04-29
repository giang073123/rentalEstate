package com.giang.rentalEstate.converter;

import com.giang.rentalEstate.dto.PropertyCreateOrUpdateDTO;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.PropertyImage;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PropertyConverter {
    private final ModelMapper modelMapper;
    
    public PropertyDTO toPropertyDTO(Property item){
        PropertyDTO property = modelMapper.map(item, PropertyDTO.class);
        // Map danh sách URL ảnh
        if (item.getImages() != null) {
            property.setImageUrls(
                item.getImages().stream()
                    .map(PropertyImage::getImageUrl)
                    .collect(Collectors.toList())
            );
        }
        return property;
    }
    
    public Property toPropertyEntity(PropertyCreateOrUpdateDTO propertyDTO){
        return modelMapper.map(propertyDTO, Property.class);
    }
}
