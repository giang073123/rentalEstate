# Rental Estate Management System

Hệ thống quản lý bất động sản cho thuê, được xây dựng với Spring Boot và React.

## Cấu trúc Project

- `rentalEstate/`: Ứng dụng Spring Boot (backend)
- `frontend/`: Ứng dụng React (frontend)

## Công nghệ sử dụng

### Backend

- Spring Boot 3.x
- Spring Security với JWT
- Spring Data JPA
- MySQL
- Google Drive API (lưu trữ hình ảnh)
- Swagger/OpenAPI (API documentation)
- Lombok
- Maven

### Frontend

- React
- React Router
- Axios

## Tính năng chính

1. Đăng ký, đăng nhập, xác thực và phân quyền người dùng
- Hỗ trợ xác thực bằng JWT.
- Phân quyền theo vai trò (người thuê, chủ nhà, admin).
2. Quản lý bất động sản (CRUD)
- Thêm, sửa, xóa, xem chi tiết bất động sản.
- Quản lý hình ảnh bất động sản (upload, lưu trữ qua Google Drive API).
- Quản lý trạng thái bất động sản: chờ duyệt, đã duyệt, đã cho thuê, bị từ chối.
3. Tìm kiếm & lọc bất động sản nâng cao
- Lọc theo giá, diện tích, vị trí (tỉnh/thành, quận/huyện, phường/xã), hướng nhà, số phòng ngủ, tiện ích, thời gian chuyển vào, v.v.
- Tìm kiếm theo từ khóa.
4. Quản lý yêu cầu thuê nhà
- Người thuê gửi yêu cầu thuê.
- Chủ nhà duyệt, từ chối, hoặc quản lý trạng thái yêu cầu thuê.
- Theo dõi lịch sử thuê, trạng thái yêu cầu.
5. Quản lý khách hàng (cho chủ nhà)
- Xem danh sách khách hàng đã gửi yêu cầu thuê.
- Xem thông tin chi tiết và các bất động sản khách hàng quan tâm.
6. Dashboard thống kê (cho admin & chủ nhà)
- Thống kê số lượng bất động sản, số lượng yêu cầu thuê, tỷ lệ đã cho thuê, số lượng bất động sản nổi bật (phổ biến).
- Biểu đồ trực quan hóa dữ liệu (sử dụng Chart.js, Ant Design Charts).
7. Quản lý thông báo
- Gửi và nhận thông báo liên quan đến trạng thái bất động sản, yêu cầu thuê, v.v.

## Bảo mật

- JWT Authentication
- Role-based Authorization
- Password Encryption
- CORS Configuration
- Input Validation
- Exception Handling

## API Documentation

Xem chi tiết API tại: [Rental Estate API Documentation](https://rentalestate.onrender.com/)

## Cài đặt và Chạy

### Backend

```bash
cd rentalEstate
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend/rentalestate
npm install
npm start
```

## Tác giả

- Giang
