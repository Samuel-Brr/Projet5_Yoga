import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { User } from '../interfaces/user.interface';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpTestingController: HttpTestingController;

  const mockUser: User = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    admin: false,
    createdAt: new Date(),
    updatedAt: new Date(),
    password: 'password123'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });

    // Inject both the service and the testing controller
    service = TestBed.inject(UserService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verify that no unmatched requests are outstanding
    httpTestingController.verify();
  });

  describe('getById', () => {
    const userId = '123';
    const expectedUrl = 'api/user/123';

    it('should make GET request to correct URL', () => {
      // Act
      service.getById(userId).subscribe(response => {
        expect(response).toEqual(mockUser);
      });

      // Assert
      const req = httpTestingController.expectOne(expectedUrl);
      expect(req.request.method).toBe('GET');

      // Respond with mock data
      req.flush(mockUser);
    });
  });

  describe('delete', () => {
    const userId = '123';
    const expectedUrl = 'api/user/123';

    it('should make DELETE request to correct URL', () => {
      // Act
      service.delete(userId).subscribe(response => {
        expect(response).toEqual({ message: 'Deleted successfully' });
      });

      // Assert
      const req = httpTestingController.expectOne(expectedUrl);
      expect(req.request.method).toBe('DELETE');

      // Respond with mock data
      req.flush({ message: 'Deleted successfully' });
    });
  });
});
