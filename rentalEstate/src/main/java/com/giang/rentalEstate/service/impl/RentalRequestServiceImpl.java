package com.giang.rentalEstate.service.impl;

import com.giang.rentalEstate.converter.RentalRequestConverter;
import com.giang.rentalEstate.dto.RentalRequestDTO;
import com.giang.rentalEstate.enums.PropertyStatus;
import com.giang.rentalEstate.enums.RentalRequestStatus;
import com.giang.rentalEstate.exception.DuplicateRentalRequestException;
import com.giang.rentalEstate.exception.RentalRequestLimitExceededException;
import com.giang.rentalEstate.exception.ResourceNotFoundException;
import com.giang.rentalEstate.exception.RentalRequestNotAuthorizedException;
import com.giang.rentalEstate.exception.RentalRequestInvalidStatusException;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.repository.PropertyRepository;
import com.giang.rentalEstate.repository.RentalRequestRepository;
import com.giang.rentalEstate.repository.NotificationRepository;
import com.giang.rentalEstate.repository.UserRepository;
import com.giang.rentalEstate.service.NotificationService;
import com.giang.rentalEstate.service.RentalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalRequestServiceImpl implements RentalRequestService {
    private final PropertyRepository propertyRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final UserRepository userRepository;
    private final RentalRequestConverter rentalRequestConverter;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // @Transactional
    // public void fixInconsistentData() {
    //     // Lấy tất cả các yêu cầu thuê
    //     List<RentalRequest> allRequests = rentalRequestRepository.findAll();

    //     // Nhóm theo property
    //     Map<Property, List<RentalRequest>> requestsByProperty = allRequests.stream()
    //             .collect(Collectors.groupingBy(RentalRequest::getProperty));

    //     // Kiểm tra và sửa từng property
    //     for (Map.Entry<Property, List<RentalRequest>> entry : requestsByProperty.entrySet()) {
    //         Property property = entry.getKey();
    //         List<RentalRequest> requests = entry.getValue();

    //         // Đếm số lượng yêu cầu hợp lệ
    //         long validRequestCount = requests.size();

    //         // Nếu số lượng không khớp với số lượng trong property
    //         if (property.getCurrentTenants() != validRequestCount) {
    //             // Cập nhật lại số lượng
    //             property.setCurrentTenants((int) validRequestCount);
    //             propertyRepository.save(property);
    //         }
    //     }
    // }
    @Override
    public RentalRequest createRentalRequest(Long propertyId, User user) {
        // Kiểm tra property có tồn tại không
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));

        // Kiểm tra số lượng yêu cầu đã đạt giới hạn chưa
        if (property.getRentalRequests().size() >= property.getMaxTenants()) {
            throw new RentalRequestLimitExceededException("BĐS này đã đạt giới hạn yêu cầu thuê tối đa");
        }

        // Kiểm tra user đã gửi yêu cầu cho property này chưa
        boolean hasExistingRequest = rentalRequestRepository.existsByPropertyAndCustomer(property, user);
        if (hasExistingRequest) {
            throw new DuplicateRentalRequestException("Bạn đã gửi yêu cầu thuê cho BĐS này rồi");
        }

        // Tạo yêu cầu mới
        RentalRequest request = RentalRequest.builder()
                .property(property)
                .customer(user)
                .status(RentalRequestStatus.PENDING)
                .build();

        RentalRequest savedRequest = rentalRequestRepository.save(request);
        // Tạo thông báo
        notificationService.createNotification("Yêu cầu thuê", "Bạn đã gửi yêu cầu thuê cho " + property.getTitle(), user, property.getId(), savedRequest.getId());
        notificationService.createNotification("Yêu cầu thuê", "Người dùng " + user.getFullName() + " đã gửi yêu cầu thuê cho " + property.getTitle(), property.getOwner(), property.getId(), savedRequest.getId());
        propertyRepository.save(property);

        // Lưu yêu cầu
        return savedRequest;
    }

    @Override
    public void cancelRentalRequest(Long propertyId, User user) {
        // Kiểm tra property có tồn tại không
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));

        // Tìm yêu cầu thuê của user cho property này
        RentalRequest request = rentalRequestRepository.findByPropertyAndCustomer(property, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không có yêu cầu thuê cho BĐS này"));

        // Kiểm tra xem yêu cầu có phải của user này không
        if (!request.getCustomer().getId().equals(user.getId())) {
            throw new RentalRequestNotAuthorizedException("Bạn không có quyền hủy yêu cầu này");
        }

        // Kiểm tra trạng thái yêu cầu
        if (request.getStatus() != RentalRequestStatus.PENDING) {
            throw new RentalRequestInvalidStatusException("Không thể hủy yêu cầu khi ở trạng thái hiện tại");
        }

        propertyRepository.save(property);

        // Tạo thông báo cho chủ sở hữu về việc hủy yêu cầu
        notificationService.createNotification(
            "Yêu cầu thuê đã bị hủy",
            "Người dùng " + user.getFullName() + " đã hủy yêu cầu thuê bất động sản " + property.getTitle(),
            property.getOwner(),
            property.getId(),
            null // Không liên kết với rental request vì nó sẽ bị xóa
        );

        // Xóa các thông báo liên quan đến rental request này
        notificationRepository.deleteByRentalRequest(request);

        // Xóa rental request
        rentalRequestRepository.delete(request);
    }

    @Override
    public List<RentalRequestDTO> getRentalRequest(Long propertyId) {
        // Kiểm tra property có tồn tại không
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));

        // Tìm yêu cầu thuê của user cho property này
        List<RentalRequest> rentalRequests = rentalRequestRepository.findByProperty(property);
        List<RentalRequestDTO> rentalRequestDTOS = rentalRequests.stream()
                .map(rentalRequestConverter::toRentalRequestDTO)
                .toList();

        return rentalRequestDTOS;
    }

    @Override
    public RentalRequestStatus checkRentalRequestStatusOfCustomer(Long propertyId, User user) {
        // Kiểm tra property có tồn tại không
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));
        // Tìm yêu cầu thuê của user cho property này
        RentalRequest request = rentalRequestRepository.findByPropertyAndCustomer(property, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không có yêu cầu thuê cho BĐS này"));

        return request.getStatus();
    }

    @Override
    public List<RentalRequestDTO> getCustomerRequestOfOwner(Long customerId, User owner) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        // Lấy tất cả yêu cầu thuê của khách hàng
        List<RentalRequest> customerRequests = rentalRequestRepository.findByCustomer(customer);
        System.out.println("Kiem tra");
        List<RentalRequestDTO> rentalRequestDTOS = new ArrayList<>();
        for(var request : customerRequests){
            System.out.println(request);
            if(request.getProperty().getOwner().getId().equals(owner.getId())) rentalRequestDTOS.add(rentalRequestConverter.toRentalRequestDTO(request));
        }
        return rentalRequestDTOS;
    }
    @Override
    public List<RentalRequestDTO> getCustomerRequest(User customer) {
        List<RentalRequest> customerRequests = rentalRequestRepository.findByCustomer(customer);
        System.out.println("Kiem tra");
        List<RentalRequestDTO> rentalRequestDTOS = new ArrayList<>();
        for(var request : customerRequests){
            System.out.println(request);
            rentalRequestDTOS.add(rentalRequestConverter.toRentalRequestDTO(request));
        }
        return rentalRequestDTOS;
    }

    @Override
    public RentalRequest updateRentalRequestStatus(Long requestId, RentalRequestStatus status, User user) {
        // Tìm yêu cầu thuê
        RentalRequest request = rentalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu thuê không tồn tại"));

        // Kiểm tra quyền thay đổi trạng thái
        Property property = request.getProperty();
        if (!property.getOwner().getId().equals(user.getId())) {
            throw new RentalRequestNotAuthorizedException("Bạn không có quyền cập nhật trạng thái của yêu cầu thuê này");
        }

        if (status.name() == RentalRequestStatus.SELECTED.toString()) {
            // Nếu chọn người thuê, từ chối tất cả các yêu cầu khác
            property.getRentalRequests().stream()
                    .filter(r -> r.getId() != requestId && r.getStatus() == RentalRequestStatus.PENDING)
                    .forEach(r -> {
                        r.setStatus(RentalRequestStatus.REJECTED);
                        rentalRequestRepository.save(r);
                    });
            
            // Cập nhật thông tin người thuê cho property
            property.setCustomer(request.getCustomer());
            property.setStatus(PropertyStatus.RENTED);
            propertyRepository.save(property);
        }

        // Cập nhật trạng thái yêu cầu
        request.setStatus(status);
        return rentalRequestRepository.save(request);
    }
    public long getActiveRequestsCount(User user) {
        return rentalRequestRepository.countByCustomerAndStatus(
                user,
                RentalRequestStatus.SELECTED  // Đã được chọn
        );
    }

    public long getPendingRequestsCount(User user) {
        return rentalRequestRepository.countByCustomerAndStatus(
                user,
                RentalRequestStatus.PENDING  // Đang chờ xử lý
        );
    }

    @Override
    public long getOwnerPendingRequestsCount(User user) {
        List<Property> properties = propertyRepository.findByOwner(user);
//        long cnt = 0;
        List<Long> propertyIds = new ArrayList<>();
        for(var property : properties){
            propertyIds.add(property.getId());
        }
        return rentalRequestRepository.countByPropertyIdInAndStatus(
                propertyIds,
                RentalRequestStatus.PENDING  // Đang chờ xử lý
        );
    }

    @Override
    public List<RentalRequestDTO> getRecentRequestsForOwner(User owner) {
        List<RentalRequest> rentalRequests = rentalRequestRepository.findRecentRequestsForOwner(owner, RentalRequestStatus.PENDING);
        List<RentalRequestDTO> rentalRequestDTOS = new ArrayList<>();
        for(var rentalRequest : rentalRequests){
            rentalRequestDTOS.add(rentalRequestConverter.toRentalRequestDTO(rentalRequest));
        }
        return rentalRequestDTOS;
    }

    @Override
    public long getAcceptedRequestPercentage() {
        return 100 * rentalRequestRepository.countByStatus(RentalRequestStatus.SELECTED) / rentalRequestRepository.count();
    }

} 