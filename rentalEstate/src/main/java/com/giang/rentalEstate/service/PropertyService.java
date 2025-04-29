package com.giang.rentalEstate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giang.rentalEstate.dto.PopularPropertyDTO;
import com.giang.rentalEstate.dto.PropertyCreateOrUpdateDTO;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.dto.PropertyFilterDTO;
import com.giang.rentalEstate.enums.PropertyStatus;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.User;

import java.util.List;

public interface PropertyService {
    Property createProperty(PropertyCreateOrUpdateDTO propertyCreateOrUpdateDTO, User user, List<String> imageUrls);
    Property updateProperty(PropertyCreateOrUpdateDTO propertyCreateOrUpdateDTO, Long id, User user, List<String> imageUrls, String keepImageIdsJson) throws JsonProcessingException;
    List<PropertyDTO> getPropertiesOfOwner(User user);
    PropertyDTO getPropertyFromId(Long id, User user);
    void deletePropertyFromId(Long id, User user);
    void updatePropertyStatus(Long id, PropertyStatus status, String rejectReason, User user);
    List<PropertyDTO> filterProperties(PropertyFilterDTO propertyFilterDTO);
    long getOwnerPropertiesCount(User user);
    long getApprovedPropertiesCount();
    long getPendingPropertiesCount();
    long getRentedPercentage();
    List<PopularPropertyDTO> getPopularProperties(int limit);
    List<PropertyDTO> searchProperties(String keyword);
    List<PropertyDTO> getAllProperties(User user);
}
