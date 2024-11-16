import {ComponentFixture, fakeAsync, flush, TestBed, tick} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {Router} from '@angular/router';
import {of} from 'rxjs';

import {UserService} from '../services/user.service';
import {SessionService} from '../services/session.service';
import {MeComponent} from "../components/me/me.component";
import {User} from "../interfaces/user.interface";

describe('User Profile Management Integration', () => {
  let router: Router;
  let userService: UserService;
  let sessionService: SessionService;
  let meFixture: ComponentFixture<MeComponent>;

  const mockRegularUser: User = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe ',
    email: 'john@test.com ',
    admin: false,
    createdAt: new Date(),
    updatedAt: new Date(),
    password: 'password123'
  };

  const mockAdminUser = {
    ...mockRegularUser,
    id: 2,
    email: 'admin@test.com',
    admin: true
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MeComponent],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'me', component: MeComponent },
          { path: '', redirectTo: 'login', pathMatch: 'full' }
        ]),
        HttpClientTestingModule,
        MatSnackBarModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        UserService,
        SessionService
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    userService = TestBed.inject(UserService);
    sessionService = TestBed.inject(SessionService);
    meFixture = TestBed.createComponent(MeComponent);
  });

  describe('Profile Viewing', () => {
    it('should display user profile information correctly', fakeAsync(() => {
      // Setup session with regular user
      sessionService.logIn({
        id: mockRegularUser.id,
        admin: mockRegularUser.admin,
        token: 'token',
        type: 'Bearer',
        username: mockRegularUser.email,
        firstName: mockRegularUser.firstName,
        lastName: mockRegularUser.lastName
      });

      // Mock user service response
      jest.spyOn(userService, 'getById')
        .mockReturnValue(of(mockRegularUser));

      // Navigate to profile
      router.navigate(['/me']);
      tick();

      const component = meFixture.componentInstance;
      component.ngOnInit();
      meFixture.detectChanges();

      // Verify profile information
      const element = meFixture.nativeElement;
      expect(element.textContent).toContain(mockRegularUser.firstName);
      expect(element.textContent).toContain("DOE");
      expect(element.textContent).toContain("john@test.com");

      flush();
    }));
  });

  describe('Account Deletion Flow', () => {
    it('should allow regular users to delete their account', fakeAsync(() => {
      const routerSpy = jest.spyOn(router, 'navigate');

      // Setup session with regular user
      sessionService.logIn({
        id: mockRegularUser.id,
        admin: mockRegularUser.admin,
        token: 'token',
        type: 'Bearer',
        username: mockRegularUser.email,
        firstName: mockRegularUser.firstName,
        lastName: mockRegularUser.lastName
      });

      // Mock service responses
      jest.spyOn(userService, 'getById').mockReturnValue(of(mockRegularUser));
      jest.spyOn(userService, 'delete').mockReturnValue(of(void 0));

      // Navigate to profile
      router.navigate(['/me']);
      tick();

      const component = meFixture.componentInstance;
      component.ngOnInit();
      meFixture.detectChanges();

      // Verify delete button is visible for regular users
      const deleteButton = meFixture.nativeElement.querySelector('[data-testid="delete-button"]');
      expect(deleteButton).toBeTruthy();

      // Trigger delete
      component.delete();
      tick();

      // Verify logout and navigation
      expect(sessionService.isLogged).toBeFalsy();
      expect(sessionService.sessionInformation).toBeUndefined();
      expect(routerSpy).toHaveBeenCalledWith(['/']);

      flush();
    }));

    it('should not show delete button for admin users', fakeAsync(() => {
      // Setup session with admin user
      sessionService.logIn({
        id: mockAdminUser.id,
        admin: mockAdminUser.admin,
        token: 'token',
        type: 'Bearer',
        username: mockAdminUser.email,
        firstName: mockAdminUser.firstName,
        lastName: mockAdminUser.lastName
      });

      // Mock service response
      jest.spyOn(userService, 'getById').mockReturnValue(of(mockAdminUser));

      // Navigate to profile
      router.navigate(['/me']);
      tick();

      const component = meFixture.componentInstance;
      component.ngOnInit();
      meFixture.detectChanges();

      // Verify delete button is not visible for admin users
      const deleteButton = meFixture.nativeElement.querySelector('[data-testid="delete-button"]');
      expect(deleteButton).toBeFalsy();

      flush();
    }));
  });
});
