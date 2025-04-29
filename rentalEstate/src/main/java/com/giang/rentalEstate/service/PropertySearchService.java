package com.giang.rentalEstate.service;

import com.giang.rentalEstate.converter.PropertyConverter;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PropertySearchService {
    List<PropertyDTO> searchProperties(String keyword);
} 