import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {AuthService} from './auth.service';
import {LoginRequest} from '../interfaces/loginRequest.interface';
import {RegisterRequest} from '../interfaces/registerRequest.interface';
import {SessionInformation} from "../../../interfaces/sessionInformation.interface";

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const baseUrl = 'api/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Verifies that no unmatched requests are outstanding
  });

  describe('register', () => {
    const mockRegisterRequest: RegisterRequest = {
      email: 'test@test.com',
      firstName: 'Test',
      lastName: 'User',
      password: 'password123'
    };

    it('should send POST request to register endpoint', () => {
      service.register(mockRegisterRequest).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRegisterRequest);
      req.flush(null); // Simulating successful response with no content
    });
  });

  describe('login', () => {
    const mockLoginRequest: LoginRequest = {
      email: 'test@test.com',
      password: 'password123'
    };

    const mockSessionInfo: SessionInformation = {
      id: 1,
      token: 'mock-token',
      type: 'Bearer',
      username: 'test@test.com',
      admin: false
    } as any;

    it('should send POST request to login endpoint', () => {
      service.login(mockLoginRequest).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockLoginRequest);
      req.flush(mockSessionInfo);
    });
  });
});
