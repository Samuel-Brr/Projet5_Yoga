import {ComponentFixture, fakeAsync, flush, TestBed, tick} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ReactiveFormsModule} from '@angular/forms';
import {of} from 'rxjs';
import {ListComponent} from "./components/list/list.component";
import {DetailComponent} from "./components/detail/detail.component";
import {FormComponent} from "./components/form/form.component";
import {SessionApiService} from "./services/session-api.service";
import {TeacherService} from "../../services/teacher.service";
import {SessionService} from "../../services/session.service";
import {LoginComponent} from "../auth/components/login/login.component";
import {RegisterComponent} from "../auth/components/register/register.component";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {ActivatedRoute, convertToParamMap, Router} from "@angular/router";
import {MatSelectModule} from "@angular/material/select";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatButtonModule} from "@angular/material/button";

describe('Session Management Integration', () => {
  let listFixture: ComponentFixture<ListComponent>;
  let detailFixture: ComponentFixture<DetailComponent>;
  let formFixture: ComponentFixture<FormComponent>;
  let sessionApiService: SessionApiService;
  let teacherService: TeacherService;
  let sessionService: SessionService;
  let router: Router;

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
          { path: 'sessions/create', component: FormComponent },
          { path: 'sessions/detail/:id', component: DetailComponent },
          { path: 'sessions', component: ListComponent }
        ]),
        HttpClientTestingModule,
        NoopAnimationsModule,  // Use NoopAnimationsModule instead of BrowserAnimationsModule
        ReactiveFormsModule,
        MatToolbarModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatSelectModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatButtonModule,
        MatSnackBarModule
      ],
      providers: [
        SessionApiService,
        SessionService,
        TeacherService,
        MatDatepickerModule,
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

  describe('Session Creation and Management Flow', () => {
    it('should handle complete session creation and viewing flow', fakeAsync(() => {
      // Setup router spy
      const routerSpy = jest.spyOn(router, 'navigate');

      // Navigate to create form
      router.navigate(['/sessions/create']);
      tick();

      // Mock service responses
      jest.spyOn(sessionApiService, 'create').mockReturnValue(of(mockSession));
      jest.spyOn(sessionApiService, 'all').mockReturnValue(of([mockSession]));
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of(mockSession));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));
      jest.spyOn(teacherService, 'all').mockReturnValue(of([mockTeacher]));

      // Start with form component
      const formComponent = formFixture.componentInstance;
      formFixture.detectChanges();
      tick();

      // Fill form
      formComponent.sessionForm?.patchValue({
        name: mockSession.name,
        date: new Date(mockSession.date).toISOString().split('T')[0],
        teacher_id: mockSession.teacher_id,
        description: mockSession.description
      });
      formFixture.detectChanges();

      // Submit form
      formComponent.submit();
      tick();

      // Verify the component triggered navigation
      expect(routerSpy).toHaveBeenCalledWith(['sessions']);

      // Navigate to list
      router.navigate(['/sessions']);
      tick();

      // Verify session list update
      const listComponent = listFixture.componentInstance;
      listFixture.detectChanges();

      listComponent.sessions$.subscribe(sessions => {
        expect(sessions).toContain(mockSession);
      });

      // Navigate to detail
      router.navigate(['/sessions/detail', mockSession.id]);
      tick();

      // View session detail
      const detailComponent = detailFixture.componentInstance;
      detailFixture.detectChanges();
      tick();

      expect(detailComponent.session).toEqual(mockSession);
      expect(detailComponent.teacher).toEqual(mockTeacher);

      // Clear all pending timers
      flush();
    }));

    it('should handle session participation flow', fakeAsync(() => {
      // Set up component with route params
      const detailComponent = detailFixture.componentInstance;
      detailComponent.sessionId = mockSession.id!.toString();

      // Mock service responses
      jest.spyOn(sessionApiService, 'detail').mockReturnValue(of({...mockSession, id: 1}));
      jest.spyOn(teacherService, 'detail').mockReturnValue(of(mockTeacher));
      jest.spyOn(sessionApiService, 'participate').mockReturnValue(of(void 0));
      jest.spyOn(sessionApiService, 'unParticipate').mockReturnValue(of(void 0));

      // Initialize component
      detailComponent.ngOnInit();
      detailFixture.detectChanges();
      tick();

      // Participate in session
      detailComponent.participate();
      tick();

      expect(sessionApiService.participate).toHaveBeenCalledWith(
        "1",  // ensure string type
        sessionService.sessionInformation!.id.toString()
      );

      // Unparticipate from session
      detailComponent.unParticipate();
      tick();

      expect(sessionApiService.unParticipate).toHaveBeenCalledWith(
        "1",  // ensure string type
        sessionService.sessionInformation!.id.toString()
      );

      // Clear all pending timers
      flush();
    }));
  });
});
