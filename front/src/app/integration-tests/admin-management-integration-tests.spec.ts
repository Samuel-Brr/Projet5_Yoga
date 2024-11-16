import {ComponentFixture, fakeAsync, flush, TestBed, tick} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, convertToParamMap, Router} from '@angular/router';
import {of} from 'rxjs';
import {SessionApiService} from "../features/sessions/services/session-api.service";
import {TeacherService} from "../services/teacher.service";
import {SessionService} from "../services/session.service";
import {ListComponent} from "../features/sessions/components/list/list.component";
import {DetailComponent} from "../features/sessions/components/detail/detail.component";
import {FormComponent} from "../features/sessions/components/form/form.component";
import {MatCardModule} from "@angular/material/card";
import {LoginComponent} from "../features/auth/components/login/login.component";
import {RegisterComponent} from "../features/auth/components/register/register.component";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldControl, MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";

describe('Admin Management Integration', () => {
  let router: Router;
  let sessionApiService: SessionApiService;
  let teacherService: TeacherService;
  let sessionService: SessionService;
  let listFixture: ComponentFixture<ListComponent>;
  let detailFixture: ComponentFixture<DetailComponent>;
  let formFixture: ComponentFixture<FormComponent>;

  const mockSession = {
    id: 1,
    name: 'Yoga Class',
    description: 'Relaxing yoga session',
    date: new Date('2024-12-25'),
    teacher_id: 1,
    users: [1],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const mockTeacher = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    createdAt: new Date(),
    updatedAt: new Date()
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ListComponent,
        DetailComponent,
        FormComponent,
        LoginComponent,
        RegisterComponent
      ],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'sessions', component: ListComponent },
          { path: 'sessions/create', component: FormComponent },
          { path: 'sessions/update/:id', component: FormComponent },
          { path: 'sessions/detail/:id', component: DetailComponent }
        ]),
        HttpClientTestingModule,
        MatSnackBarModule,
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatCardModule,
        MatIconModule
      ],
      providers: [
        SessionApiService,
        TeacherService,
        SessionService,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '1' })
            }
          }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    sessionApiService = TestBed.inject(SessionApiService);
    teacherService = TestBed.inject(TeacherService);
    sessionService = TestBed.inject(SessionService);

    // Setup session service with admin user
    sessionService.logIn({
      id: 1,
      admin: true,
      token: 'mock-token',
      type: 'Bearer',
      username: 'admin@test.com',
      firstName: 'Admin',
      lastName: 'User'
    });

    listFixture = TestBed.createComponent(ListComponent);
    detailFixture = TestBed.createComponent(DetailComponent);
    formFixture = TestBed.createComponent(FormComponent);
  });

  describe('Admin Session Management', () => {

    it('should allow admins to modify sessions', fakeAsync(() => {
      // Mock service responses
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(sessionApiService, 'update').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'all').mockReturnValue(of([mockTeacher]));

      // Navigate to update form
      router.navigate(['/sessions/update', mockSession.id]);
      tick();

      const formComponent = formFixture.componentInstance;
      formComponent.ngOnInit();
      formFixture.detectChanges();
      tick();

      // Verify form is accessible
      expect(formComponent.sessionForm).toBeTruthy();
      expect(formComponent.onUpdate).toBeTruthy();

      // Update session
      formComponent.sessionForm?.patchValue({
        name: 'Updated Yoga Class',
        date: new Date(mockSession.date).toISOString().split('T')[0],
        teacher_id: mockSession.teacher_id,
        description: 'Updated description'
      });
      formFixture.detectChanges();

      formComponent.submit();
      tick();

      // Verify update was called
      expect(sessionApiService.update).toHaveBeenCalled();
      expect(router.url).toBe('/sessions');

      flush();
    }));

    it('should allow admins to delete sessions', fakeAsync(() => {
      // Mock service responses
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(sessionApiService, 'delete').mockReturnValue(of(void 0));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));

      // Navigate to detail
      router.navigate(['/sessions/detail', mockSession.id]);
      tick();

      const detailComponent = detailFixture.componentInstance;
      detailComponent.ngOnInit();
      detailFixture.detectChanges();
      tick();

      // Verify delete button is visible
      const deleteButton = detailFixture.nativeElement.querySelector('[data-testid="delete-button"]');
      expect(deleteButton).toBeTruthy();

      // Delete session
      detailComponent.delete();
      tick();

      // Verify delete was called and redirected
      expect(sessionApiService.delete).toHaveBeenCalledWith(mockSession.id!.toString());
      expect(router.url).toBe('/sessions');

      flush();
    }));
  });
});
