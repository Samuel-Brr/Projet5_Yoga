import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TeacherService } from './teacher.service';
import { Teacher } from '../interfaces/teacher.interface';
import { HttpErrorResponse } from '@angular/common/http';

describe('TeacherService', () => {
  let service: TeacherService;
  let httpMock: HttpTestingController;

  // Mock data
  const mockTeachers: Teacher[] = [
    { id: 1, firstName: 'John', lastName: 'Doe', createdAt: new Date(), updatedAt: new Date() },
    { id: 2, firstName: 'Jane', lastName: 'Smith', createdAt: new Date(), updatedAt: new Date() }
  ];

  const mockTeacher: Teacher = mockTeachers[0];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TeacherService]
    });

    service = TestBed.inject(TeacherService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verify that no unmatched requests are outstanding
    httpMock.verify();
  });

  describe('Service initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should have correct API path', () => {
      expect((service as any).pathService).toBe('api/teacher');
    });
  });

  describe('all()', () => {
    it('should retrieve all teachers via GET', fakeAsync(() => {
      let actualTeachers: Teacher[] | undefined;

      service.all().subscribe(teachers => {
        actualTeachers = teachers;
      });

      const req = httpMock.expectOne('api/teacher');
      expect(req.request.method).toBe('GET');
      req.flush(mockTeachers);

      tick();

      expect(actualTeachers).toEqual(mockTeachers);
    }));

    it('should handle empty response', fakeAsync(() => {
      let actualTeachers: Teacher[] | undefined;

      service.all().subscribe(teachers => {
        actualTeachers = teachers;
      });

      const req = httpMock.expectOne('api/teacher');
      req.flush([]);

      tick();

      expect(actualTeachers).toEqual([]);
    }));

    it('should handle error response', fakeAsync(() => {
      const errorMessage = 'Server error';

      service.all().subscribe({
        next: () => fail('Should have failed'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(500);
          expect(error.error).toBe(errorMessage);
        }
      });

      const req = httpMock.expectOne('api/teacher');
      req.flush(errorMessage, { status: 500, statusText: 'Server Error' });

      tick();
    }));
  });

  describe('detail()', () => {
    it('should retrieve a single teacher via GET', fakeAsync(() => {
      let actualTeacher: Teacher | undefined;
      const teacherId = '1';

      service.detail(teacherId).subscribe(teacher => {
        actualTeacher = teacher;
      });

      const req = httpMock.expectOne(`api/teacher/${teacherId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockTeacher);

      tick();

      expect(actualTeacher).toEqual(mockTeacher);
    }));

    it('should handle 404 error when teacher not found', fakeAsync(() => {
      const teacherId = '999';
      const errorMessage = 'Teacher not found';

      service.detail(teacherId).subscribe({
        next: () => fail('Should have failed'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(404);
          expect(error.error).toBe(errorMessage);
        }
      });

      const req = httpMock.expectOne(`api/teacher/${teacherId}`);
      req.flush(errorMessage, { status: 404, statusText: 'Not Found' });

      tick();
    }));

    it('should construct correct URL with given ID', fakeAsync(() => {
      const teacherId = '1';

      service.detail(teacherId).subscribe();

      const req = httpMock.expectOne(`api/teacher/${teacherId}`);
      expect(req.request.url).toBe(`api/teacher/${teacherId}`);
      req.flush(mockTeacher);

      tick();
    }));
  });
});
