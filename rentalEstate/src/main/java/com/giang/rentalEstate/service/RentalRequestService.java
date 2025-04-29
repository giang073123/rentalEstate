package com.giang.rentalEstate.service;

import com.giang.rentalEstate.dto.RentalRequestDTO;
import com.giang.rentalEstate.enums.RentalRequestStatus;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;

import java.util.List;

public interface RentalRequestService {
    RentalRequest createRentalRequest(Long propertyId, User user);
    void cancelRentalRequest(Long propertyId, User user);
    RentalRequest updateRentalRequestStatus(Long requestId, RentalRequestStatus status, User user);
    List<RentalRequestDTO> getRentalRequest(Long propertyId);
    RentalRequestStatus checkRentalRequestStatusOfCustomer(Long propertyId, User user);
    List<RentalRequestDTO> getCustomerRequestOfOwner(Long customerId, User owner);
    List<RentalRequestDTO> getCustomerRequest(User user);
    long getActiveRequestsCount(User user);
    long getPendingRequestsCount(User user);
    long getOwnerPendingRequestsCount(User user);
    List<RentalRequestDTO> getRecentRequestsForOwner(User owner);
    long getAcceptedRequestPercentage();
}