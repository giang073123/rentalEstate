package com.giang.rentalEstate.service.impl;

import com.giang.rentalEstate.converter.PropertyConverter;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.exception.ResourceNotFoundException;
import com.giang.rentalEstate.exception.PropertyAlreadySavedException;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.SavedProperty;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.repository.PropertyRepository;
import com.giang.rentalEstate.repository.SavedPropertyRepository;
import com.giang.rentalEstate.repository.UserRepository;
import com.giang.rentalEstate.service.NotificationService;
import com.giang.rentalEstate.service.PropertyService;
import com.giang.rentalEstate.service.SavedPropertyService;
import com.giang.rentalEstate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedPropertyServiceImpl implements SavedPropertyService {
    private final SavedPropertyRepository savedPropertyRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyService propertyService;
    private final PropertyConverter propertyConverter;
    private final NotificationService notificationService;

    @Override
    public void saveProperty(User user, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bất động sản không tồn tại"));

        if (savedPropertyRepository.existsByUserAndProperty(user, property)) {
            throw new PropertyAlreadySavedException("Bất động sản đã được lưu trước đó");
        }

        SavedProperty savedProperty = SavedProperty.builder()
                .user(user)
                .property(property)
                .build();

        savedPropertyRepository.save(savedProperty);
        notificationService.createNotification("Lượt lưu mới", "Bạn đã lưu tin đăng " + property.getTitle(), user, property.getId(), null);
        notificationService.createNotification("Lượt lưu mới", "Người dùng " + user.getFullName() + " đã gửi quan tâm tới tin đăng " + property.getTitle(), property.getOwner(), property.getId(), null);
    }

    @Transactional
    public void unsaveProperty(User user, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));
        savedPropertyRepository.deleteByUserAndProperty(user, property);
        savedPropertyRepository.flush();
        notificationService.createNotification("Bỏ lưu", "Bạn đã bỏ lưu tin đăng " + property.getTitle(), user, property.getId(), null);

    }

    public List<PropertyDTO> getSavedProperties(User user) {
        List<Property> properties = savedPropertyRepository.findByUser(user)
                .stream()
                .map(SavedProperty::getProperty)
                .toList();
        List<PropertyDTO> propertyDTOList = new ArrayList<>();
        for(var property : properties){
            propertyDTOList.add(propertyConverter.toPropertyDTO(property));
        }
        return propertyDTOList;
    }

    public long getSavedPropertiesCount(User user) {
        return savedPropertyRepository.countByUser(user);
    }

    @Override
    public long getOwnerSavedCount(User user) {
        List<Property> properties = propertyRepository.findByOwner(user);
        long cnt = 0;
        for(var property : properties){
            cnt += savedPropertyRepository.countByProperty(property);
        }
        return cnt;
    }

    @Override
    public List<RentalRequest> getOwnerSaved(User user) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getSavedStatsByDayForOwner(User owner, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days - 1).withHour(0).withMinute(0).withSecond(0);
        
        // Lấy dữ liệu từ repository
        List<Object[]> stats = savedPropertyRepository.countSavedByDayForOwner(owner.getId(), startDate);
        
        // Chuyển đổi kết quả thành format phù hợp cho biểu đồ
        return stats.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", ((Date) row[0]).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM")));
            item.put("count", row[1]);
            return item;
        }).collect(Collectors.toList());
    }
}
