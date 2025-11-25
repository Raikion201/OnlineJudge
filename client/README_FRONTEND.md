# Frontend - Angular + Keycloak Integration

## 📋 Prerequisites

- Node.js (v18+)
- npm or yarn

## 🚀 Installation

```bash
# Install dependencies
npm install

# Install Keycloak JS adapter
npm install keycloak-js
```

## 🔧 Configuration

The app is configured to connect to:
- **Keycloak**: http://localhost:8321
- **Realm**: online-judge
- **Client ID**: oj-angular
- **API Gateway**: http://localhost:8085

## 🏃 Running the App

```bash
# Development server
npm start

# Navigate to
http://localhost:4200
```

## 📁 Project Structure

```
src/app/
├── components/
│   ├── login/           # Login page with Keycloak integration
│   └── home/            # Home page (protected)
├── services/
│   └── auth.service.ts  # Keycloak authentication service
├── interceptors/
│   └── auth.interceptor.ts  # HTTP interceptor to add JWT token
├── app.config.ts        # App configuration with Keycloak init
└── app.routes.ts        # Route configuration
```

## 🎨 Features

### Login Page
- Clean, modern UI
- Sign in with Keycloak
- Register new account (redirects to Keycloak)
- Responsive design

### Home Page
- User profile display
- Role-based UI (Admin badge)
- Stats dashboard
- Token preview
- Logout functionality

### Auth Service
- Automatic token refresh
- User profile management
- Role-based access control
- Signal-based reactive state

### HTTP Interceptor
- Automatically adds JWT token to API requests
- Only for requests to API Gateway (localhost:8085)

## 🔐 Authentication Flow

1. App initializes → Keycloak init
2. User clicks "Sign In" → Redirects to Keycloak login
3. After login → Redirects back to /home
4. Token stored in AuthService
5. HTTP requests automatically include Bearer token
6. Token auto-refreshes every minute

## 🧪 Testing

1. Start backend services:
```bash
docker-compose --profile infra --profile services up -d
```

2. Start frontend:
```bash
npm start
```

3. Navigate to http://localhost:4200
4. Click "Sign In"
5. Login with: **admin / admin**
6. You should see the home page with your profile

## 📝 Notes

- Keycloak must be running on port 8321
- API Gateway must be running on port 8085
- CORS is configured in API Gateway
- PKCE (S256) is enabled for security
- Tokens expire after 5 minutes (300s)
- Auto-refresh checks every minute

## 🎯 Next Steps

- Add problem list page
- Add submission page
- Add admin panel
- Add user rankings
- Add contest system
