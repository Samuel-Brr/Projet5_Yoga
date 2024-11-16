import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {DetailComponent} from './detail.component';
import {SessionApiService} from '../../services/session-api.service';
import {TeacherService} from '../../../../services/teacher.service';
import {SessionService} from '../../../../services/session.service';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {Session} from '../../interfaces/session.interface';
import {Teacher} from '../../../../interfaces/teacher.interface';
import {HttpClientModule} from "@angular/common/http";
import {MatCardModule} from "@angular/material/card";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

describe('DetailComponent', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  let mockSessionApiService: jest.Mocked<SessionApiService>;
  let mockTeacherService: jest.Mocked<TeacherService>;
  let mockMatSnackBar: jest.Mocked<MatSnackBar>;
  let mockRouter: jest.Mocked<Router>;
  let mockSessionService: Partial<SessionService>;

  // Mock data
  const mockSession: Session = {
    id: 1,
    name: 'Yoga Class',
    description: 'Beginner friendly yoga class',
    date: new Date(),
    teacher_id: 1,
    users: [1, 2, 3],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  const mockTeacher: Teacher = {
    id: 1,
    lastName: 'Doe',
    firstName: 'John',
    createdAt: new Date(),
    updatedAt: new Date()
  };

  beforeEach(async () => {
    // Create mocks
    mockSessionApiService = {
      detail: jest.fn().mockReturnValue(of(mockSession)),
      delete: jest.fn().mockReturnValue(of({})),
      participate: jest.fn().mockReturnValue(of({})),
      unParticipate: jest.fn().mockReturnValue(of({}))
    } as any;

    mockTeacherService = {
      detail: jest.fn().mockReturnValue(of(mockTeacher))
    } as any;

    mockRouter = {
      navigate: jest.fn()
    } as any;

    mockMatSnackBar = {
      open: jest.fn()
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

    await TestBed.configureTestingModule({
      declarations: [DetailComponent],
      imports: [
        RouterTestingModule,
        HttpClientModule,
        BrowserAnimationsModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatSnackBarModule,
        FlexLayoutModule,
        ReactiveFormsModule
      ],
      providers: [
        FormBuilder,
        {provide: SessionApiService, useValue: mockSessionApiService},
        {provide: TeacherService, useValue: mockTeacherService},
        {provide: MatSnackBar, useValue: mockMatSnackBar},
        {provide: Router, useValue: mockRouter},
        {provide: SessionService, useValue: mockSessionService},
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
  });

  describe('initialization', () => {
    it('should fetch session and teacher data on init', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(mockSessionApiService.detail).toHaveBeenCalledWith('1');
      expect(mockTeacherService.detail).toHaveBeenCalledWith('1');
      expect(component.session).toEqual(mockSession);
      expect(component.teacher).toEqual(mockTeacher);

      //should set isParticipate to true when user is in session users array
      expect(component.isParticipate).toBeTruthy();

      //should set isAdmin based on session service
      expect(component.isAdmin).toBeTruthy();
    }));
  });

  describe('navigation', () => {
    it('should call window.history.back() when back() is called', () => {
      const mockHistoryBack = jest.spyOn(window.history, 'back');
      component.back();
      expect(mockHistoryBack).toHaveBeenCalled();
    });
  });

  describe('delete()', () => {
    it('should delete session and navigate to sessions page', fakeAsync(() => {
      component.delete();
      tick();

      expect(mockSessionApiService.delete).toHaveBeenCalledWith('1');
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Session deleted !',
        'Close',
        {duration: 3000}
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
    }));
  });

  describe('participate() & unParticipate()', () => {
    it('should call participate/ unparticipate and refresh session data', fakeAsync(() => {
      component.participate();
      tick();

      expect(mockSessionApiService.participate).toHaveBeenCalledWith('1', '1');
      expect(mockSessionApiService.detail).toHaveBeenCalled();

      component.unParticipate();
      tick();

      expect(mockSessionApiService.unParticipate).toHaveBeenCalledWith('1', '1');
      expect(mockSessionApiService.detail).toHaveBeenCalled();
    }));
  });
});
