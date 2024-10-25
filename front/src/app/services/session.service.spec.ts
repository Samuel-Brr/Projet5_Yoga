import { TestBed } from '@angular/core/testing';
import { SessionService } from './session.service';
import { SessionInformation } from '../interfaces/sessionInformation.interface';
import { firstValueFrom } from 'rxjs';

describe('SessionService', () => {
  let service: SessionService;

  // Mock session information
  const mockSessionInfo: SessionInformation = {
    id: 1,
    admin: true,
    token: 'mock-token'
  } as any;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SessionService]
    });
    service = TestBed.inject(SessionService);
  });

  afterEach(() => {
    // Clean up after each test
    service.logOut();
  });

  describe('Initial State', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should initialize with logged out state', () => {
      expect(service.isLogged).toBeFalsy();
      expect(service.sessionInformation).toBeUndefined();
    });

    it('should initialize with false value in isLogged observable', async () => {
      const initialState = await firstValueFrom(service.$isLogged());
      expect(initialState).toBeFalsy();
    });
  });

  describe('Login Functionality', () => {
    it('should update login state when logging in', () => {
      service.logIn(mockSessionInfo);

      expect(service.isLogged).toBeTruthy();
      expect(service.sessionInformation).toEqual(mockSessionInfo);
    });

    it('should emit true in isLogged observable when logging in', async () => {
      service.logIn(mockSessionInfo);

      const loggedInState = await firstValueFrom(service.$isLogged());
      expect(loggedInState).toBeTruthy();
    });

    it('should store session information correctly', () => {
      service.logIn(mockSessionInfo);

      expect(service.sessionInformation).toBeDefined();
      expect(service.sessionInformation?.id).toBe(mockSessionInfo.id);
      expect(service.sessionInformation?.admin).toBe(mockSessionInfo.admin);
      expect(service.sessionInformation?.token).toBe(mockSessionInfo.token);
    });
  });

  describe('Logout Functionality', () => {
    beforeEach(() => {
      // Setup: login before testing logout
      service.logIn(mockSessionInfo);
    });

    it('should clear session information when logging out', () => {
      service.logOut();

      expect(service.sessionInformation).toBeUndefined();
    });

    it('should update login state when logging out', () => {
      service.logOut();

      expect(service.isLogged).toBeFalsy();
    });

    it('should emit false in isLogged observable when logging out', async () => {
      service.logOut();

      const loggedOutState = await firstValueFrom(service.$isLogged());
      expect(loggedOutState).toBeFalsy();
    });
  });

  describe('Observable Behavior', () => {
    it('should emit correct sequence of values when logging in and out', async () => {
      const emittedValues: boolean[] = [];

      // Subscribe to the observable
      const subscription = service.$isLogged().subscribe(value => {
        emittedValues.push(value);
      });

      // Perform login and logout operations
      service.logIn(mockSessionInfo);
      service.logOut();

      // Clean up subscription
      subscription.unsubscribe();

      // Check the sequence of emitted values
      expect(emittedValues).toEqual([false, true, false]);
    });

    it('should emit latest value to new subscribers', async () => {
      // Login first
      service.logIn(mockSessionInfo);

      // Subscribe after login and check value
      const currentState = await firstValueFrom(service.$isLogged());
      expect(currentState).toBeTruthy();
    });
  });

  describe('Edge Cases', () => {
    it('should handle multiple consecutive logins', () => {
      const secondMockSession = { ...mockSessionInfo, id: 2 };

      service.logIn(mockSessionInfo);
      service.logIn(secondMockSession);

      expect(service.sessionInformation).toEqual(secondMockSession);
      expect(service.isLogged).toBeTruthy();
    });

    it('should handle multiple consecutive logouts', () => {
      service.logIn(mockSessionInfo);
      service.logOut();
      service.logOut();

      expect(service.sessionInformation).toBeUndefined();
      expect(service.isLogged).toBeFalsy();
    });

    it('should handle logout when not logged in', () => {
      service.logOut();

      expect(service.sessionInformation).toBeUndefined();
      expect(service.isLogged).toBeFalsy();
    });
  });
});
