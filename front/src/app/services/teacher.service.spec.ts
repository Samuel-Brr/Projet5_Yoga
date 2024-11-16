import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TeacherService} from './teacher.service';
import {Teacher} from '../interfaces/teacher.interface';
import {HttpErrorResponse} from '@angular/common/http';

describe('TeacherService', () => {
  let service: TeacherService;
  let httpMock: HttpTestingController;

  // Mock data
  const mockTeachers: Teacher[] = [
    {id: 1, firstName: 'John', lastName: 'Doe', createdAt: new Date(), updatedAt: new Date()},
    {id: 2, firstName: 'Jane', lastName: 'Smith', createdAt: new Date(), updatedAt: new Date()}
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
  });
});
