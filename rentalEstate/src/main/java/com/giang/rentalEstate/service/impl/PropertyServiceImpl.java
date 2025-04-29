package com.giang.rentalEstate.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giang.rentalEstate.converter.PropertyConverter;
import com.giang.rentalEstate.dto.PropertyCreateOrUpdateDTO;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.dto.PropertyFilterDTO;
import com.giang.rentalEstate.dto.PopularPropertyDTO;
import com.giang.rentalEstate.enums.PropertyStatus;
import com.giang.rentalEstate.enums.Rolename;
import com.giang.rentalEstate.exception.PropertyAlreadyReviewedException;
import org.springframework.security.access.AccessDeniedException;
import com.giang.rentalEstate.exception.ResourceNotFoundException;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.PropertyImage;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.repository.NotificationRepository;
import com.giang.rentalEstate.repository.PropertyImageRepository;
import com.giang.rentalEstate.repository.PropertyRepository;
import com.giang.rentalEstate.repository.RentalRequestRepository;
import com.giang.rentalEstate.repository.SavedPropertyRepository;
import com.giang.rentalEstate.repository.UserRepository;
import com.giang.rentalEstate.service.NotificationService;
import com.giang.rentalEstate.service.PropertyService;
import com.giang.rentalEstate.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final ObjectMapper objectMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final PropertyConverter propertyConverter;
    private final GoogleDriveService googleDriveService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final SavedPropertyRepository savedPropertyRepository;

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);
    @Transactional
    @Override
    public Property createProperty(PropertyCreateOrUpdateDTO propertyCreateOrUpdateDTO, User user, List<String> imageUrls) {
        logger.info("Bắt đầu tạo property mới cho user: {}", user.getUsername());
        
        // Tạo property từ DTO
        Property property = propertyConverter.toPropertyEntity(propertyCreateOrUpdateDTO);
        property.setStatus(PropertyStatus.PENDING_REVIEW);
        property.setOwner(user);
        property.setImages(new ArrayList<>()); // Khởi tạo danh sách rỗng
        
        // Lưu property để lấy ID
        Property savedProperty = propertyRepository.save(property);
        
        // Tạo và thêm từng ảnh vào property
        for (String url : imageUrls) {
            PropertyImage image = PropertyImage.builder()
                    .imageUrl(url)
                    .property(savedProperty)
                    .build();
            
            // Thêm ảnh vào danh sách của property
            savedProperty.getImages().add(image);
        }
//        List<User> adminUsers = userRepository.findByRoleId();
        notificationService.createNotification("Yêu cầu đăng tin", "Bạn đã gửi yêu cầu đăng tin: " + property.getTitle() + ". Vui lòng chờ quản trị viên duyệt.", user, savedProperty.getId(), null);
        // Gửi thông báo cho tất cả admin
        List<User> admins = userRepository.findByRole_Name(Rolename.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    "Yêu cầu duyệt tin mới",
                    "Người dùng " + user.getFullName() + " đã gửi yêu cầu đăng tin: " + property.getTitle(),
                    admin,
                    savedProperty.getId(),
                    null
            );
        }
        // Lưu property với danh sách ảnh đã cập nhật
        return propertyRepository.save(savedProperty);
    }
    @Override
    @Transactional
    public Property updateProperty(PropertyCreateOrUpdateDTO propertyCreateOrUpdateDTO, Long id, User user, List<String> imageUrls, String keepImageIdsJson) throws JsonProcessingException {
        // Tìm property hiện tại
        Property existingProperty = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));

        // Kiểm tra quyền sở hữu
        if(existingProperty.getOwner().getId() != user.getId()){
            throw new AccessDeniedException("Bạn không phải chủ của BĐS này");
        }

        // Lưu trạng thái hiện tại
        String currentStatus = existingProperty.getStatus().toString();

        // try {
            // Xử lý danh sách ảnh cần giữ lại
            List<String> keepImageIds = new ArrayList<>();
            if (keepImageIdsJson != null && !keepImageIdsJson.isEmpty()) {
                keepImageIds = objectMapper.readValue(keepImageIdsJson, new TypeReference<List<String>>() {});
                logger.info("Keep image IDs from request: {}", keepImageIds);
            }

            // Lọc và giữ lại các ảnh được chọn
            List<PropertyImage> currentImages = existingProperty.getImages();
            List<PropertyImage> imagesToKeep = new ArrayList<>();
            List<PropertyImage> imagesToDelete = new ArrayList<>();

            for (PropertyImage image : currentImages) {
                String imageId = extractImageId(image.getImageUrl());
                logger.info("Processing image with ID: {}", imageId);
                if (keepImageIds.contains(imageId)) {
                    imagesToKeep.add(image);
                    logger.info("Keeping image: {}", image.getImageUrl());
                } else {
                    imagesToDelete.add(image);
                    logger.info("Marking image for deletion: {}", image.getImageUrl());
                }
            }

            // Xóa các ảnh không được giữ lại
            if (!imagesToDelete.isEmpty()) {
                logger.info("Deleting {} images", imagesToDelete.size());
                // Xóa trực tiếp từ danh sách images của property
                existingProperty.getImages().removeAll(imagesToDelete);
                
                // Xóa ảnh từ Google Drive và database
                for (PropertyImage image : imagesToDelete) {
                    try {
                        String imageId = extractImageId(image.getImageUrl());
                        if (!imageId.isEmpty()) {
                            logger.info("Deleting image from Google Drive: {}", imageId);
                            googleDriveService.deleteFile(imageId);
                        }
                    } catch (Exception e) {
                        logger.error("Error deleting image from Google Drive: {}", e.getMessage());
                        // Tiếp tục xóa các ảnh khác ngay cả khi có lỗi với một ảnh
                    }
                }
                
                // Xóa từ database
                propertyImageRepository.deleteAll(imagesToDelete);
                propertyImageRepository.flush();
            }
            notificationRepository.deleteByProperty(existingProperty);
            // 2. Xóa tất cả notifications liên quan đến rental requests của property này
            List<RentalRequest> rentalRequests = rentalRequestRepository.findByProperty(existingProperty);
            for (RentalRequest request : rentalRequests) {
                notificationRepository.deleteByRentalRequest(request);
            }
            // Xóa tất cả các rental request liên quan đến property
            rentalRequestRepository.deleteByPropertyId(existingProperty.getId());
             // 4. Xóa tất cả saved properties
             savedPropertyRepository.deleteByProperty(existingProperty);


            // Tạo property mới với thông tin cập nhật
            Property updatedProperty = propertyConverter.toPropertyEntity(propertyCreateOrUpdateDTO);
            updatedProperty.setId(existingProperty.getId());
            updatedProperty.setOwner(existingProperty.getOwner());
            updatedProperty.setCreatedAt(existingProperty.getCreatedAt());
            updatedProperty.setImages(new ArrayList<>(imagesToKeep));

            // Cập nhật trạng thái
            if(currentStatus.equals("APPROVED") || currentStatus.equals("REJECTED")){
                updatedProperty.setStatus(PropertyStatus.PENDING_REVIEW);
            } else {
                updatedProperty.setStatus(existingProperty.getStatus());
            }

            // Lưu property để có ID cho các ảnh mới
            Property savedProperty = propertyRepository.save(updatedProperty);
            propertyRepository.flush(); // Đảm bảo property được lưu trước khi thêm ảnh mới

            // Tạo và lưu ảnh mới nếu có
            if (imageUrls != null && !imageUrls.isEmpty()) {
                List<PropertyImage> newImages = imageUrls.stream()
                        .map(url -> {
                            PropertyImage image = PropertyImage.builder()
                                    .imageUrl(url)
                                    .property(savedProperty)
                                    .build();
                            logger.info("Creating new image: {}", url);
                            return image;
                        })
                        .collect(Collectors.toList());
                // Lưu các ảnh mới
                propertyImageRepository.saveAll(newImages);
                propertyImageRepository.flush();
                // Thêm ảnh mới vào danh sách ảnh của property
                savedProperty.getImages().addAll(newImages);
            }
            // Lưu lại property với danh sách ảnh đã cập nhật
            return propertyRepository.save(savedProperty);
        // } catch (Exception e) {
        //     logger.error("Error while updating property: {}", e.getMessage(), e);
        //     throw new UserException("Error while updating property: " + e.getMessage());
        // }
    }

    // Hàm hỗ trợ để lấy ID từ URL Google Drive
    private String extractImageId(String url) {
        if (url == null) return "";
        Pattern pattern = Pattern.compile("[-\\w]{25,}");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    @Override
    public List<PropertyDTO> getPropertiesOfOwner(User owner) {
        System.out.println("ADMIN check 0");

        List<Property> properties = new ArrayList<>();
        if(owner.getRole().getName().toString().equals("ADMIN")){
            System.out.println("ADMIN check 1");

            properties = propertyRepository.findAll();
            System.out.println("ADMIN check 2");
        } else {
            properties = propertyRepository.findByOwner(owner);
            if(properties.isEmpty()){
                throw new ResourceNotFoundException("Bạn không có BĐS nào");
            }
        }
        System.out.println("ADMIN check 3");

        for(Property property : properties){
            List<PropertyImage> propertyImages = propertyImageRepository.findByPropertyId(property.getId());

        }
        return properties.stream()
                .map(property -> {
                    PropertyDTO propertyDTO = propertyConverter.toPropertyDTO(property);
                    propertyDTO.setSavedCount(savedPropertyRepository.countByProperty(property));
                    return propertyDTO;
                })
                .collect(Collectors.toList());

    }

    @Override
    public PropertyDTO getPropertyFromId(Long id, User user) {
        Property property = propertyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));

        if(user.getRole().getName().toString().equals("CUSTOMER")){
            //neu khach hang khong duoc chon thue property nay thi se khong nhin thay chi tiet property
            if(property.getStatus().toString().equals("RENTED") && !property.getCustomer().getId().equals(user.getId())){
                throw new AccessDeniedException("Bạn không có quyền xem BĐS này");
            }
            if(property.getStatus().toString().equals("PENDING_REVIEW") || property.getStatus().toString().equals("REJECTED")) {
                throw new AccessDeniedException("Bạn không có quyền xem BĐS này");
            }
        }
        if(user.getRole().getName().toString().equals("OWNER")
                && !property.getOwner().getId().equals(user.getId())
                && !property.getStatus().toString().equals("APPROVED")){
            throw new AccessDeniedException("Bạn không có quyền xem BĐS này");
        }
        return propertyConverter.toPropertyDTO(property);
    }
    @Override
    @Transactional

    public void deletePropertyFromId(Long id, User user) {
        logger.info("Bắt đầu xóa property với ID: {}", id);
        
        // Tìm property cần xóa
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));

        
        // Kiểm tra quyền xóa
        if (!property.getOwner().getId().equals(user.getId()) && 
            !user.getRole().getName().equals(Rolename.ADMIN)) {
            throw new AccessDeniedException("Bạn không có quyền xóa BĐS này");
        }
            // Xóa tất cả ảnh trên Google Drive
            List<PropertyImage> images = property.getImages();
            if (images != null && !images.isEmpty()) {
                logger.info("Xóa {} ảnh từ Google Drive", images.size());
                for (PropertyImage image : images) {
                    try {
                        String imageId = extractImageId(image.getImageUrl());
                        if (!imageId.isEmpty()) {
                            logger.info("Xóa ảnh từ Google Drive với ID: {}", imageId);
                            googleDriveService.deleteFile(imageId);
                        }
                    } catch (Exception e) {
                        logger.error("Lỗi khi xóa ảnh từ Google Drive: {}", e.getMessage());
                        // Tiếp tục xóa các ảnh khác ngay cả khi có lỗi với một ảnh
                    }
                }
            }
            notificationRepository.deleteByProperty(property);
            // 2. Xóa tất cả notifications liên quan đến rental requests của property này
            List<RentalRequest> rentalRequests = rentalRequestRepository.findByProperty(property);
            for (RentalRequest request : rentalRequests) {
                notificationRepository.deleteByRentalRequest(request);
            }
            // Xóa tất cả các rental request liên quan đến property
            rentalRequestRepository.deleteByPropertyId(id);
             // 4. Xóa tất cả saved properties
             savedPropertyRepository.deleteByProperty(property);

            // Xóa property (bao gồm cả các thông tin liên quan do cascade)
            logger.info("Xóa property và các thông tin liên quan");
            propertyRepository.delete(property);
            propertyRepository.flush();

            logger.info("Xóa property thành công");
    }
//    @Override
//    public void updatePropertyStatus(Long id, PropertyStatus status) throws UserException {
//
//        logger.info("Cập nhật trạng thái property với ID: {} thành: {}", id, status);
//
//        Property property = propertyRepository.findById(id)
//                .orElseThrow(() -> new UserException("Không tìm thấy property"));
//
//        property.setStatus(status);
//        propertyRepository.save(property);
//
//        logger.info("Cập nhật trạng thái property thành công");
//    }
    @Override
    @Transactional
    public void updatePropertyStatus(Long id, PropertyStatus status, String rejectReason, User admin) {
        logger.info("Bắt đầu cập nhật trạng thái property ID: {} thành: {}", id, status);

        // Sử dụng lock để đảm bảo chỉ một admin có thể review tại một thời điểm
        Property property = propertyRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));

        // Kiểm tra nếu property đã được review
        if (property.getStatus() != PropertyStatus.PENDING_REVIEW) {
            String message = String.format(
                    "Bất động sản này %s bởi %s vào lúc %s",
                    getStatusName(property.getStatus()),
                    property.getReviewedBy().getFullName(),
                    property.getReviewedAt()
            );
            throw new PropertyAlreadyReviewedException(message);
        }

        // Cập nhật thông tin review
        property.setStatus(status);
        property.setReviewedBy(admin);
        property.setReviewedAt(LocalDateTime.now());
        if (status == PropertyStatus.REJECTED && rejectReason != null) {
            property.setRejectReason(rejectReason);
        }
        // Lưu thay đổi
        propertyRepository.save(property);

        // Gửi thông báo cho owner
        String notificationTitle = "Kết quả duyệt tin";
        String notificationContent = buildNotificationContent(property);
        notificationService.createNotification(
                notificationTitle,
                notificationContent,
                property.getOwner(),
                property.getId(),
                null
        );

        logger.info("Cập nhật trạng thái property thành công");
    }
    private String getStatusName(PropertyStatus propertyStatus){
        return switch(propertyStatus){
            case PENDING_REVIEW -> "đang chờ duyệt";
            case APPROVED -> "đã được duyệt";
            case REJECTED -> "đã bị từ chối";
            case EXPIRED -> "đã hết hạn";
            case RENTED -> "đã được thuê";
        };
    }
    private String buildNotificationContent(Property property) {
        String status = property.getStatus() == PropertyStatus.APPROVED ?
                "đã được duyệt" : "đã bị từ chối";

        String content = String.format("Bất động sản '%s' của bạn %s",
                property.getTitle(),
                status
        );

        if (property.getStatus() == PropertyStatus.REJECTED && property.getRejectReason() != null) {
            content += ". Lý do: " + property.getRejectReason();
        }

        return content;
    }
    @Override
    public List<PropertyDTO> filterProperties(PropertyFilterDTO filterDTO) {
        // Lấy giá trị min/max cho area và price từ filterDTO
        Double minArea = null;
        Double maxArea = null;
        if (filterDTO.getAreaRange() != null) {
            minArea = filterDTO.getAreaRange().get(0.0);
            maxArea = filterDTO.getAreaRange().get(1.0);
        }

        Double minPrice = null;
        Double maxPrice = null;
        if (filterDTO.getPriceRange() != null) {
            minPrice = filterDTO.getPriceRange().get(0.0);
            maxPrice = filterDTO.getPriceRange().get(1.0);
        }

        // Gọi phương thức tìm kiếm từ repository
        List<Property> filteredProperties = propertyRepository.findByFilter(
            filterDTO, minArea, maxArea, minPrice, maxPrice, PropertyStatus.APPROVED);

        // Chuyển đổi kết quả sang DTO
        return filteredProperties.stream()
            .map(propertyConverter::toPropertyDTO)
            .collect(Collectors.toList());
    }


    @Override
    public long getOwnerPropertiesCount(User user) {
        return propertyRepository.countByOwner(user);
    }

    @Override
    public long getApprovedPropertiesCount() {
        return propertyRepository.countByStatus(PropertyStatus.APPROVED);
    }

    @Override
    public long getPendingPropertiesCount() {
        return propertyRepository.countByStatus(PropertyStatus.PENDING_REVIEW);
    }

    @Override
    public long getRentedPercentage() {
        return 100 * propertyRepository.countByStatus(PropertyStatus.RENTED) / propertyRepository.count();
    }

    @Override
    public List<PopularPropertyDTO> getPopularProperties(int limit) {
        // Lấy danh sách bất động sản đã được duyệt
        List<Property> approvedProperties = propertyRepository.findByStatus(PropertyStatus.APPROVED);
        
        // Sắp xếp theo số lượt lưu giảm dần và chuyển đổi sang DTO
        return approvedProperties.stream()
                .map(property -> {
                    long savedCount = savedPropertyRepository.countByProperty(property);
                    return PopularPropertyDTO.builder()
                            .property(propertyConverter.toPropertyDTO(property))
                            .savedCount(savedCount)
                            .build();
                })
                .sorted((p1, p2) -> Long.compare(p2.getSavedCount(), p1.getSavedCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyDTO> searchProperties(String keyword) {
        List<Property> properties = propertyRepository.searchByKeyword(keyword);
        return properties.stream()
                .map(propertyConverter::toPropertyDTO)
                .toList();
    }

    @Override
    public List<PropertyDTO> getAllProperties(User user) {
        List<Property> properties = new ArrayList<>();
            properties = propertyRepository.findByStatus(PropertyStatus.APPROVED);
        return properties.stream()
                .map(propertyConverter::toPropertyDTO)
                .collect(Collectors.toList());
    }

}
