import { HttpClientModule } from '@angular/common/http';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { SessionService } from 'src/app/services/session.service';
import { UserService } from 'src/app/services/user.service';
import { MeComponent } from './me.component';
import {User} from "../../interfaces/user.interface";

describe('MeComponent', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  let mockUserService: jest.Mocked<UserService>;
  let mockRouter: jest.Mocked<Router>;
  let mockMatSnackBar: jest.Mocked<MatSnackBar>;
  let mockSessionService: Partial<SessionService>;

  const mockUser: User = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    password: 'toto',
    email: 'john@example.com',
    admin: false,
    createdAt: new Date(),
    updatedAt: new Date()
  };

  beforeEach(async () => {
    // Create mocks
    mockUserService = {
      getById: jest.fn().mockReturnValue(of(mockUser)),
      delete: jest.fn().mockReturnValue(of({}))
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
        firstName: mockUser.firstName,
        lastName: mockUser.lastName,
        type: 'type',
        username: mockUser.email
      },
      logOut: jest.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [MeComponent],
      imports: [
        MatSnackBarModule,
        HttpClientModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter },
        { provide: MatSnackBar, useValue: mockMatSnackBar }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user data on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    expect(mockUserService.getById).toHaveBeenCalledWith('1');
    expect(component.user).toEqual(mockUser);
  }));

  it('should go back when back() is called', () => {
    const mockHistoryBack = jest.spyOn(window.history, 'back');

    component.back();

    expect(mockHistoryBack).toHaveBeenCalled();
  });

  describe('delete()', () => {
    beforeEach(() => {
      component.user = mockUser;
      fixture.detectChanges();
    });

    it('should delete user account and handle success', fakeAsync(() => {
      component.delete();
      tick();

      expect(mockUserService.delete).toHaveBeenCalledWith('1');
      expect(mockMatSnackBar.open).toHaveBeenCalledWith(
        'Your account has been deleted !',
        'Close',
        { duration: 3000 }
      );
      expect(mockSessionService.logOut).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    }));
  });

  describe('template tests', () => {
    beforeEach(() => {
      component.user = mockUser;
      fixture.detectChanges();
    });

    it('should display user information', () => {
      const compiled = fixture.nativeElement;
      expect(compiled.textContent).toContain('John');
      expect(compiled.textContent).toContain('DOE');
      expect(compiled.textContent).toContain('john@example.com');
    });

    it('should show delete button for non-admin users', () => {
      component.user = { ...mockUser, admin: false };
      fixture.detectChanges();

      const deleteButton = fixture.nativeElement.querySelector('[data-testid="delete-button"]');
      expect(deleteButton).toBeTruthy();
    });

    it('should hide delete button for admin users', () => {
      component.user = { ...mockUser, admin: true };
      fixture.detectChanges();

      const deleteButton = fixture.nativeElement.querySelector('[data-testid="delete-button"]');
      expect(deleteButton).toBeFalsy();
    });
  });
});
