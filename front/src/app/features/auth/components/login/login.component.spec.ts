import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RouterTestingModule} from '@angular/router/testing';
import {Router} from '@angular/router';
import {SessionService} from 'src/app/services/session.service';
import {LoginComponent} from './login.component';
import {of, throwError} from 'rxjs';
import {AuthService} from "../../services/auth.service";

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockAuthService: jest.Mocked<AuthService>;
  let mockRouter: jest.Mocked<Router>;
  let mockSessionService: jest.Mocked<SessionService>;

  const mockSessionInfo = {
    token: 'fake-token',
    type: 'Bearer',
    id: 1,
    username: 'test@test.com',
    firstName: 'Test',
    lastName: 'User',
    admin: false
  };

  beforeEach(async () => {
    // Create mocks
    mockAuthService = {
      login: jest.fn()
    } as any;

    mockRouter = {
      navigate: jest.fn()
    } as any;

    mockSessionService = {
      logIn: jest.fn()
    } as any;

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [
        {provide: AuthService, useValue: mockAuthService},
        {provide: Router, useValue: mockRouter},
        {provide: SessionService, useValue: mockSessionService}
      ],
      imports: [
        RouterTestingModule,
        BrowserAnimationsModule,
        HttpClientModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Form Validation', () => {
    it('should validate the form', () => {
      //should initialize with invalid form
      expect(component.form.valid).toBeFalsy();

      //should validate email required
      const email = component.form.controls['email'];
      expect(email.valid).toBeFalsy();
      expect(email.errors?.['required']).toBeTruthy();

      //should validate email format
      email.setValue('invalid-email');
      expect(email.errors?.['email']).toBeTruthy();

      email.setValue('valid@email.com');
      expect(email.errors).toBeFalsy();

      //should validate password required
      const password = component.form.controls['password'];
      expect(password.valid).toBeFalsy();
      expect(password.errors?.['required']).toBeTruthy();

      //should validate password minimum length
      password.setValue('12');
      expect(password.errors?.['minlength']).toBeTruthy();

      password.setValue('123');
      expect(password.errors).toBeFalsy();
    });
  });

  describe('Form Submission', () => {
    beforeEach(() => {
      // Set valid form values
      component.form.patchValue({
        email: 'test@test.com',
        password: 'password123'
      });
    });

    it('should call auth service and handle successful login', fakeAsync(() => {
      mockAuthService.login.mockReturnValue(of(mockSessionInfo));

      component.submit();
      tick();

      expect(mockAuthService.login).toHaveBeenCalledWith({
        email: 'test@test.com',
        password: 'password123'
      });
      expect(mockSessionService.logIn).toHaveBeenCalledWith(mockSessionInfo);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/sessions']);
      expect(component.onError).toBeFalsy();
    }));

    it('should handle login error', fakeAsync(() => {
      mockAuthService.login.mockReturnValue(throwError(() => new Error('Login failed')));

      component.submit();
      tick();

      expect(mockAuthService.login).toHaveBeenCalled();
      expect(mockSessionService.logIn).not.toHaveBeenCalled();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(component.onError).toBeTruthy();
    }));
  });

  /*describe('Password Visibility Toggle', () => {
    it('should toggle password visibility', () => {
      expect(component.hide).toBeTruthy();
      component.hide = !component.hide;
      expect(component.hide).toBeFalsy();
    });
  });*/

  describe('Template Integration', () => {
    it('should show error message when onError is true', () => {
      component.onError = true;
      fixture.detectChanges();

      const errorElement = fixture.nativeElement.querySelector('.error');
      expect(errorElement?.textContent).toContain('An error occurred');
    });

    /*it('should not show error message by default', () => {
      const errorElement = fixture.nativeElement.querySelector('.error');
      expect(errorElement).toBeFalsy();
    });

    it('should toggle password field type based on hide value', () => {
      const passwordField = fixture.nativeElement.querySelector('input[type="password"]');
      expect(passwordField.type).toBe('password');

      component.hide = false;
      fixture.detectChanges();

      expect(passwordField.type).toBe('text');
    });*/
  });
});
