import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormComponent } from './form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { SessionService } from '../../../../services/session.service';
import { TeacherService } from '../../../../services/teacher.service';
import { SessionApiService } from '../../services/session-api.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { of, throwError } from 'rxjs';
import { HttpClientModule } from '@angular/common/http';

describe('FormComponent', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  let mockRouter: jest.Mocked<Router>;
  let mockSessionService: Partial<SessionService>;
  let mockSessionApiService: jest.Mocked<SessionApiService>;
  let mockTeacherService: jest.Mocked<TeacherService>;
  let mockMatSnackBar: jest.Mocked<MatSnackBar>;
  let mockActivatedRoute: Partial<ActivatedRoute>;

  const mockSession = {
    id: 1,
    name: 'Yoga Class',
    date: new Date('2024-12-25'),
    teacher_id: 1,
    description: 'Relaxing yoga session',
    users: [],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const mockTeachers = [
    { id: 1, name: 'John Doe' },
    { id: 2, name: 'Jane Smith' }
  ];

  beforeEach(async () => {
    // Create mocks
    mockRouter = {
      navigate: jest.fn(),
      url: '/sessions/create'
    } as any;

    mockSessionService = {
      sessionInformation: {
        admin: true,
        id: 1,
        token: 'toto',
        firstName: 'John',
        lastName: 'Doe',
        type: 'type',
        username: 'john@doe.com'
      },
      logOut: jest.fn()
    };

    mockSessionApiService = {
      create: jest.fn().mockReturnValue(of(mockSession)),
      update: jest.fn().mockReturnValue(of(mockSession)),
      detail: jest.fn().mockReturnValue(of(mockSession))
    } as any;

    mockTeacherService = {
      all: jest.fn().mockReturnValue(of(mockTeachers))
    } as any;

    mockMatSnackBar = {
      open: jest.fn()
    } as any;

    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jest.fn().mockReturnValue('1')
        }
      }
    } as any;

    await TestBed.configureTestingModule({
      declarations: [FormComponent],
      imports: [
        ReactiveFormsModule,
        BrowserAnimationsModule,
        HttpClientModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule
      ],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: SessionService, useValue: mockSessionService },
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: TeacherService, useValue: mockTeacherService },
        { provide: MatSnackBar, useValue: mockMatSnackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('Initialization', () => {
    it('should redirect non-admin users to sessions page', fakeAsync(() => {
      mockSessionService.sessionInformation = { admin: false, id: 1 } as any;
      fixture.detectChanges();
      tick();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/sessions']);
    }));

    it('should initialize form for create mode', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      expect(component.onUpdate).toBeFalsy();
      expect(component.sessionForm).toBeTruthy();
      expect(component.sessionForm?.get('name')?.value).toBe('');
    }));

    it('should initialize form for update mode', fakeAsync(() => {
      Object.defineProperty(mockRouter, 'url', { value: '/sessions/update/1' });
      fixture.detectChanges();
      tick();
      expect(component.onUpdate).toBeTruthy();
      expect(mockSessionApiService.detail).toHaveBeenCalledWith('1');
      expect(component.sessionForm?.get('name')?.value).toBe(mockSession.name);
    }));

    it('should load teachers on init', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      let teachers: any[] = [];
      component.teachers$.subscribe(t => teachers = t);
      expect(teachers).toEqual(mockTeachers);
    }));
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should be invalid when empty', () => {
      expect(component.sessionForm?.valid).toBeFalsy();
    });

    it('should be valid when all required fields are filled', () => {
      component.sessionForm?.patchValue({
        name: 'Test Session',
        date: '2024-12-25',
        teacher_id: 1,
        description: 'Test Description'
      });
      expect(component.sessionForm?.valid).toBeTruthy();
    });

    it('should validate description length', () => {
      const longDescription = 'a'.repeat(2001);
      component.sessionForm?.patchValue({
        description: longDescription
      });
      expect(component.sessionForm?.get('description')?.errors?.['maxlength']).toBeTruthy();
    });
  });

  describe('Form Submission', () => {
    beforeEach(() => {
      fixture.detectChanges();
      component.sessionForm?.patchValue({
        name: 'Test Session',
        date: '2024-12-25',
        teacher_id: 1,
        description: 'Test Description'
      });
    });

    it('should create new session in create mode', fakeAsync(() => {
      component.onUpdate = false;
      component.submit();
      tick();

      expect(mockSessionApiService.create).toHaveBeenCalled();
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Session created !',
        'Close',
        { duration: 3000 }
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
    }));

    it('should update session in update mode', fakeAsync(() => {
      component.onUpdate = true;
      component.submit();
      tick();

      expect(mockSessionApiService.update).toHaveBeenCalled();
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Session updated !',
        'Close',
        { duration: 3000 }
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
    }));

    it('should handle create error', fakeAsync(() => {
      component.onUpdate = false;
      mockSessionApiService.create.mockReturnValue(throwError(() => new Error('Create failed')));

      component.submit();
      tick();

      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Failed to create session',
        'Close',
        { duration: 3000 }
      );
    }));

    it('should handle update error', fakeAsync(() => {
      component.onUpdate = true;
      mockSessionApiService.update.mockReturnValue(throwError(() => new Error('Update failed')));

      component.submit();
      tick();

      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Failed to update session',
        'Close',
        { duration: 3000 }
      );
    }));
  });
});
