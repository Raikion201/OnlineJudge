# 🚀 Quick Start Guide - Angular Frontend

## Bước 1: Cài đặt dependencies

```powershell
cd client
npm install
```

## Bước 2: Đảm bảo backend đang chạy

```powershell
# Từ thư mục gốc OnlineJudge
docker-compose --profile infra --profile services ps

# Kiểm tra các services:
# - keycloak (port 8321)
# - api-gateway (port 8085)
# - user-service (port 8082)
```

Nếu chưa chạy:
```powershell
docker-compose --profile infra --profile services up -d
```

## Bước 3: Chạy Angular app

```powershell
cd client
npm start
```

App sẽ chạy tại: **http://localhost:4200**

## Bước 4: Test login

1. Mở browser: http://localhost:4200
2. Click **"Sign In"**
3. Sẽ redirect đến Keycloak login page
4. Đăng nhập với:
   - Username: **admin**
   - Password: **admin**
5. Sau khi đăng nhập thành công, sẽ redirect về /home
6. Xem profile và token của bạn

## Bước 5: Test register

1. Từ login page, click **"Create Account"**
2. Sẽ redirect đến Keycloak registration page
3. Điền thông tin user mới
4. Submit
5. Sau khi đăng ký, login với account vừa tạo

## 🎨 Giao diện

### Login Page
- Gradient purple background
- 2 buttons: Sign In và Create Account
- Icons từ Feather icons
- Responsive design

### Home Page
- Header với user info và logout button
- 4 stat cards (Problems Solved, Day Streak, Contests, Points)
- User profile details card
- Token preview
- Admin badge (nếu user là Admin)

## 🔧 Troubleshooting

### Lỗi: Cannot connect to Keycloak
```
Giải pháp:
1. Kiểm tra Keycloak đang chạy: docker ps | findstr keycloak
2. Kiểm tra Keycloak health: curl http://localhost:8321/realms/online-judge
3. Restart Keycloak: docker restart keycloak
```

### Lỗi: 401 Unauthorized khi gọi API
```
Giải pháp:
1. Kiểm tra token có được thêm vào request: F12 > Network > Headers
2. Kiểm tra API Gateway: docker logs api-gateway --tail 50
3. Token có thể đã hết hạn, logout và login lại
```

### Lỗi: npm không được nhận diện
```
Giải pháp:
1. Cài đặt Node.js: https://nodejs.org/
2. Restart terminal sau khi cài
3. Verify: node -v và npm -v
```

## 📦 Các file quan trọng đã tạo

```
client/
├── src/app/
│   ├── components/
│   │   ├── login/
│   │   │   └── login.component.ts        # Login UI
│   │   └── home/
│   │       └── home.component.ts         # Home page
│   ├── services/
│   │   └── auth.service.ts               # Keycloak service
│   ├── interceptors/
│   │   └── auth.interceptor.ts           # Auto add token
│   ├── app.config.ts                     # App config với Keycloak init
│   └── app.routes.ts                     # Routes
├── public/
│   └── silent-check-sso.html             # SSO check page
└── package.json                          # Dependencies
```

## ✅ Checklist hoàn thành

- [x] Auth Service với Keycloak integration
- [x] Login Component với modern UI
- [x] Home Component hiển thị user profile
- [x] HTTP Interceptor tự động thêm token
- [x] Routes configuration
- [x] App initialization với Keycloak
- [x] Silent SSO check
- [x] Register redirect
- [x] Logout functionality
- [x] Role-based UI (Admin badge)

## 🎯 Test Scenario

1. **Test Login Flow**
   - Access http://localhost:4200
   - Should redirect to /login
   - Click "Sign In"
   - Login with admin/admin
   - Should redirect to /home
   - See profile info

2. **Test Token**
   - Open DevTools (F12)
   - Go to Console
   - Check AuthService signals
   - Token should be visible

3. **Test API Call** (sau khi thêm API calls)
   - Login
   - Navigate to problems page
   - Check Network tab
   - Request should have Authorization header

4. **Test Logout**
   - Click Logout button
   - Should redirect to Keycloak logout
   - Then back to /login
   - Token should be cleared

## 🚀 Next Features to Add

1. **Problems Page**
   - List all problems
   - Filter by difficulty
   - Search functionality

2. **Problem Detail Page**
   - Problem description
   - Code editor
   - Submit solution

3. **Submissions Page**
   - View all submissions
   - Submission status
   - Code review

4. **Admin Panel**
   - Manage problems
   - Manage users
   - View statistics

5. **Profile Page**
   - Edit profile
   - View submission history
   - View statistics
