import {TestBed} from '@angular/core/testing';
import {SessionService} from './session.service';
import {SessionInformation} from '../interfaces/sessionInformation.interface';
import {firstValueFrom} from 'rxjs';

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
    it('should initialize with logged out state', async () => {
      expect(service.isLogged).toBeFalsy();
      expect(service.sessionInformation).toBeUndefined();
      const initialState = await firstValueFrom(service.$isLogged());
      expect(initialState).toBeFalsy();
    });
  });

  describe('Login Functionality', () => {

    it('should update login state when logging in', async () => {
      service.logIn(mockSessionInfo);
      expect(service.isLogged).toBeTruthy();
      expect(service.sessionInformation).toEqual(mockSessionInfo);
      const loggedInState = await firstValueFrom(service.$isLogged());
      expect(loggedInState).toBeTruthy();

      //should store session information correctly
      expect(service.sessionInformation).toBeDefined();
      expect(service.sessionInformation?.id).toBe(mockSessionInfo.id);
      expect(service.sessionInformation?.admin).toBe(mockSessionInfo.admin);
      expect(service.sessionInformation?.token).toBe(mockSessionInfo.token);
    });
  });

  describe('Logout Functionality', () => {
    it('should emit false in isLogged observable when logging out', async () => {
      service.logIn(mockSessionInfo);
      service.logOut();
      expect(service.sessionInformation).toBeUndefined();
      expect(service.isLogged).toBeFalsy();

      const loggedOutState = await firstValueFrom(service.$isLogged());
      expect(loggedOutState).toBeFalsy();
    });
  });
});
