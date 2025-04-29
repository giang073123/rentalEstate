package com.giang.rentalEstate.service;

import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;

import java.util.List;
import java.util.Map;

public interface SavedPropertyService {
    void saveProperty(User user, Long propertyId);
    void unsaveProperty(User user, Long propertyId);
    List<PropertyDTO> getSavedProperties(User user);
    long getSavedPropertiesCount(User user);
    long getOwnerSavedCount(User user);
    List<RentalRequest> getOwnerSaved(User user);
//    List<Map<String, Object>> getSavedStatsByDay(int days);
    List<Map<String, Object>> getSavedStatsByDayForOwner(User owner, int days);
}
