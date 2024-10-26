import {ComponentFixture, TestBed, fakeAsync, tick, flush} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {of} from 'rxjs';
import {AuthService} from "./services/auth.service";
import {SessionService} from "../../services/session.service";
import {AppComponent} from "../../app.component";
import {LoginComponent} from "./components/login/login.component";
import {RegisterComponent} from "./components/register/register.component";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatSelectModule} from "@angular/material/select";
import {MatButtonModule} from "@angular/material/button";

describe('Authentication Integration', () => {
  let router: Router;
  let authService: AuthService;
  let sessionService: SessionService;
  let appFixture: ComponentFixture<AppComponent>;
  let loginFixture: ComponentFixture<LoginComponent>;
  let registerFixture: ComponentFixture<RegisterComponent>;

  const mockSessionInfo = {
    token: 'mock-token',
    type: 'Bearer',
    id: 1,
    username: 'test@test.com',
    firstName: 'Test',
    lastName: 'User',
    admin: false
  };

  beforeEach(async () => {
    // Setup test environment for DOM operations
    Object.defineProperty(window, 'getComputedStyle', {
      value: () => ({
        getPropertyValue: () => {
          return '';
        }
      })
    });

    await TestBed.configureTestingModule({
      declarations: [
        AppComponent,
        LoginComponent,
        RegisterComponent
      ],
      imports: [
        RouterTestingModule.withRoutes([
          {path: 'login', component: LoginComponent},
          {path: 'register', component: RegisterComponent},
          {path: 'sessions', component: LoginComponent}
        ]),
        BrowserAnimationsModule,
        HttpClientTestingModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatToolbarModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatButtonModule,
        FlexLayoutModule,
        MatSelectModule
      ],
      providers: [
        AuthService,
        SessionService
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    authService = TestBed.inject(AuthService);
    sessionService = TestBed.inject(SessionService);

    appFixture = TestBed.createComponent(AppComponent);
    loginFixture = TestBed.createComponent(LoginComponent);
    registerFixture = TestBed.createComponent(RegisterComponent);
  });

  describe('Complete Authentication Flow', () => {
    it('should handle registration and login flow', fakeAsync(() => {
      // Start at registration
      router.navigate(['/register']);
      tick();

      const registerComponent = registerFixture.componentInstance;
      registerFixture.detectChanges();

      // Mock successful registration
      jest.spyOn(authService, 'register').mockReturnValue(of(void 0));

      // Fill registration form
      registerComponent.form.patchValue({
        email: 'test@test.com',
        firstName: 'Test',
        lastName: 'User',
        password: 'password123'
      });
      registerFixture.detectChanges();
      tick(); // Allow form validation to complete

      registerComponent.submit();
      tick();

      // Verify redirect to login
      expect(router.url).toBe('/login');

      // Handle login
      const loginComponent = loginFixture.componentInstance;
      loginFixture.detectChanges();
      tick(); // Allow component to initialize

      // Mock successful login
      jest.spyOn(authService, 'login').mockReturnValue(of(mockSessionInfo));

      // Fill login form
      loginComponent.form.patchValue({
        email: 'test@test.com',
        password: 'password123'
      });
      loginFixture.detectChanges();
      tick(); // Allow form validation to complete

      loginComponent.submit();
      tick();

      // Verify session creation and navigation
      expect(sessionService.isLogged).toBeTruthy();
      expect(sessionService.sessionInformation).toEqual(mockSessionInfo);
      expect(router.url).toBe('/sessions');

      // Verify app header state
      appFixture.detectChanges();
      tick(); // Allow changes to propagate

      const appElement = appFixture.nativeElement;
      expect(appElement.querySelector('[routerlink="sessions"]')).toBeTruthy();
      expect(appElement.querySelector('[routerlink="me"]')).toBeTruthy();

      // Clean up all pending timers
      flush();
    }));

    it('should handle logout flow', fakeAsync(() => {
      // Setup logged in state
      sessionService.logIn(mockSessionInfo);
      appFixture.detectChanges();

      const appComponent = appFixture.componentInstance;

      // Trigger logout
      appComponent.logout();
      tick();

      // Verify logout effects
      expect(sessionService.isLogged).toBeFalsy();
      expect(sessionService.sessionInformation).toBeUndefined();
      expect(router.url).toBe('/');

      // Verify app header state
      appFixture.detectChanges();
      const appElement = appFixture.nativeElement;
      expect(appElement.querySelector('[routerlink="login"]')).toBeTruthy();
      expect(appElement.querySelector('[routerlink="register"]')).toBeTruthy();
    }));
  });
});
