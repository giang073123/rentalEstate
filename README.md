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

### Backend

- Xác thực và phân quyền (JWT)
- Quản lý bất động sản (CRUD)
- Quản lý yêu cầu thuê nhà
- Quản lý thông báo
- Upload và quản lý hình ảnh (Google Drive)
- Tìm kiếm và lọc bất động sản
- Thống kê và báo cáo

### Frontend

- Đăng nhập/Đăng ký
- Quản lý bất động sản
- Tìm kiếm và lọc nâng cao
- Quản lý yêu cầu thuê nhà
- Quản lý thông báo
- Dashboard cho admin và chủ nhà
- Responsive design

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
