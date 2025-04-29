package com.giang.rentalEstate.service;

import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.repository.UserRepository;
import com.giang.rentalEstate.repository.PropertyRepository;
import com.giang.rentalEstate.repository.RentalRequestRepository;
import com.giang.rentalEstate.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RentalRequestRepository rentalRequestRepository;

    public List<User> getCustomersByOwnerId(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ sở hữu"));
        // Lấy tất cả property của owner
        List<Property> ownerProperties = propertyRepository.findByOwner(owner);
        
        // Lấy tất cả rental request của các property đó
        List<RentalRequest> rentalRequests = rentalRequestRepository.findByPropertyIn(ownerProperties);
        
        // Lấy danh sách customer từ các rental request
        return rentalRequests.stream()
                .map(RentalRequest::getCustomer)
                .distinct()
                .collect(Collectors.toList());
    }


} 