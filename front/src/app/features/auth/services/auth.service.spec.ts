import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { LoginRequest } from '../interfaces/loginRequest.interface';
import { RegisterRequest } from '../interfaces/registerRequest.interface';
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

  it('should be created', () => {
    expect(service).toBeTruthy();
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

    it('should handle register success', () => {
      service.register(mockRegisterRequest).subscribe({
        next: (response) => {
          expect(response).toBeUndefined();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/register`);
      req.flush(null);
    });

    it('should handle register error', () => {
      const errorMessage = 'Email already exists';

      service.register(mockRegisterRequest).subscribe({
        error: (error) => {
          expect(error.error).toBe(errorMessage);
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/register`);
      req.flush(errorMessage, { status: 400, statusText: 'Bad Request' });
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

    it('should handle login success', () => {
      service.login(mockLoginRequest).subscribe({
        next: (response) => {
          expect(response).toEqual(mockSessionInfo);
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/login`);
      req.flush(mockSessionInfo);
    });

    it('should handle login error', () => {
      const errorMessage = 'Invalid credentials';

      service.login(mockLoginRequest).subscribe({
        error: (error) => {
          expect(error.error).toBe(errorMessage);
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/login`);
      req.flush(errorMessage, { status: 401, statusText: 'Unauthorized' });
    });
  });
});
