import { Injectable, signal } from '@angular/core';
import Keycloak from 'keycloak-js';

export interface UserProfile {
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  roles?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private keycloak: Keycloak;
  
  // Signals for reactive state
  isAuthenticated = signal<boolean>(false);
  userProfile = signal<UserProfile | null>(null);
  token = signal<string | null>(null);

  constructor() {
    this.keycloak = new Keycloak({
      url: 'http://localhost:8321',
      realm: 'online-judge',
      clientId: 'oj-angular'
    });
  }

  async init(): Promise<boolean> {
    try {
      // Check if running in browser
      if (typeof window === 'undefined') {
        return false;
      }

      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256'
      });

      this.isAuthenticated.set(authenticated);

      if (authenticated) {
        await this.loadUserProfile();
        this.token.set(this.keycloak.token || null);
        
        // Auto refresh token
        setInterval(() => {
          this.keycloak.updateToken(70).catch(() => {
            console.error('Failed to refresh token');
          });
        }, 60000); // Check every minute
      }

      return authenticated;
    } catch (error) {
      console.error('Keycloak init failed', error);
      return false;
    }
  }

  async login(): Promise<void> {
    await this.keycloak.login({
      redirectUri: window.location.origin + '/home'
    });
  }

  async logout(): Promise<void> {
    this.isAuthenticated.set(false);
    this.userProfile.set(null);
    this.token.set(null);
    await this.keycloak.logout({
      redirectUri: window.location.origin
    });
  }

  async register(): Promise<void> {
    await this.keycloak.register({
      redirectUri: window.location.origin + '/home'
    });
  }

  private async loadUserProfile(): Promise<void> {
    try {
      const profile = await this.keycloak.loadUserProfile();
      const tokenParsed = this.keycloak.tokenParsed;
      
      this.userProfile.set({
        username: profile.username,
        email: profile.email,
        firstName: profile.firstName,
        lastName: profile.lastName,
        roles: tokenParsed?.realm_access?.roles || []
      });
    } catch (error) {
      console.error('Failed to load user profile', error);
    }
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  hasRole(role: string): boolean {
    const roles = this.userProfile()?.roles || [];
    return roles.includes(role);
  }

  isAdmin(): boolean {
    return this.hasRole('Admin');
  }
}
