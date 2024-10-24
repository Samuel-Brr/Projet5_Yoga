import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let mockAuthService: jest.Mocked<AuthService>;
  let mockRouter: jest.Mocked<Router>;

  beforeEach(async () => {
    // Create mocks
    mockAuthService = {
      register: jest.fn()
    } as any;

    mockRouter = {
      navigate: jest.fn()
    } as any;

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [
        ReactiveFormsModule,
        BrowserAnimationsModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Validation', () => {
    it('should initialize with invalid form', () => {
      expect(component.form.valid).toBeFalsy();
    });

    describe('Email field', () => {
      it('should be invalid when empty', () => {
        const email = component.form.controls['email'];
        expect(email.valid).toBeFalsy();
        expect(email.errors?.['required']).toBeTruthy();
      });

      it('should be invalid with incorrect email format', () => {
        const email = component.form.controls['email'];
        email.setValue('invalid-email');
        expect(email.valid).toBeFalsy();
        expect(email.errors?.['email']).toBeTruthy();
      });

      it('should be valid with correct email format', () => {
        const email = component.form.controls['email'];
        email.setValue('test@example.com');
        expect(email.valid).toBeTruthy();
      });
    });

    describe('firstName field', () => {
      it('should be invalid when empty', () => {
        const firstName = component.form.controls['firstName'];
        expect(firstName.valid).toBeFalsy();
        expect(firstName.errors?.['required']).toBeTruthy();
      });

      it('should be invalid when too short', () => {
        const firstName = component.form.controls['firstName'];
        firstName.setValue('ab');
        expect(firstName.valid).toBeFalsy();
        expect(firstName.errors?.['minlength']).toBeTruthy();
      });

      it('should be invalid when too long', () => {
        const firstName = component.form.controls['firstName'];
        firstName.setValue('a'.repeat(21));
        expect(firstName.valid).toBeFalsy();
        expect(firstName.errors?.['maxlength']).toBeTruthy();
      });

      it('should be valid with correct length', () => {
        const firstName = component.form.controls['firstName'];
        firstName.setValue('John');
        expect(firstName.valid).toBeTruthy();
      });
    });

    describe('lastName field', () => {
      it('should be invalid when empty', () => {
        const lastName = component.form.controls['lastName'];
        expect(lastName.valid).toBeFalsy();
        expect(lastName.errors?.['required']).toBeTruthy();
      });

      it('should be invalid when too short', () => {
        const lastName = component.form.controls['lastName'];
        lastName.setValue('ab');
        expect(lastName.valid).toBeFalsy();
        expect(lastName.errors?.['minlength']).toBeTruthy();
      });

      it('should be invalid when too long', () => {
        const lastName = component.form.controls['lastName'];
        lastName.setValue('a'.repeat(21));
        expect(lastName.valid).toBeFalsy();
        expect(lastName.errors?.['maxlength']).toBeTruthy();
      });

      it('should be valid with correct length', () => {
        const lastName = component.form.controls['lastName'];
        lastName.setValue('Doe');
        expect(lastName.valid).toBeTruthy();
      });
    });

    describe('password field', () => {
      it('should be invalid when empty', () => {
        const password = component.form.controls['password'];
        expect(password.valid).toBeFalsy();
        expect(password.errors?.['required']).toBeTruthy();
      });

      it('should be invalid when too short', () => {
        const password = component.form.controls['password'];
        password.setValue('ab');
        expect(password.valid).toBeFalsy();
        expect(password.errors?.['minlength']).toBeTruthy();
      });

      it('should be invalid when too long', () => {
        const password = component.form.controls['password'];
        password.setValue('a'.repeat(41));
        expect(password.valid).toBeFalsy();
        expect(password.errors?.['maxlength']).toBeTruthy();
      });

      it('should be valid with correct length', () => {
        const password = component.form.controls['password'];
        password.setValue('password123');
        expect(password.valid).toBeTruthy();
      });
    });
  });

  describe('submit()', () => {
    beforeEach(() => {
      // Fill form with valid data
      component.form.patchValue({
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });
    });

    it('should call authService.register with form values when form is valid', fakeAsync(() => {
      mockAuthService.register.mockReturnValue(of(void 0));

      component.submit();
      tick();

      expect(mockAuthService.register).toHaveBeenCalledWith({
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });
    }));

    it('should navigate to login page on successful registration', fakeAsync(() => {
      mockAuthService.register.mockReturnValue(of(void 0));

      component.submit();
      tick();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    }));

    it('should set onError to true on registration failure', fakeAsync(() => {
      mockAuthService.register.mockReturnValue(throwError(() => new Error('Registration failed')));

      component.submit();
      tick();

      expect(component.onError).toBeTruthy();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    }));
  });

  describe('Template Integration', () => {
    it('should display error message when onError is true', () => {
      component.onError = true;
      fixture.detectChanges();

      const errorElement = fixture.nativeElement.querySelector('.error');
      expect(errorElement).toBeTruthy();
      expect(errorElement.textContent).toContain('An error occurred');
    });

    it('should not display error message when onError is false', () => {
      component.onError = false;
      fixture.detectChanges();

      const errorElement = fixture.nativeElement.querySelector('.error');
      expect(errorElement).toBeFalsy();
    });

    it('should disable submit button when form is invalid', () => {
      const submitButton = fixture.nativeElement.querySelector('button[type="submit"]');
      expect(submitButton.disabled).toBeTruthy();
    });

    it('should enable submit button when form is valid', () => {
      component.form.patchValue({
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        password: 'password123'
      });
      fixture.detectChanges();

      const submitButton = fixture.nativeElement.querySelector('button[type="submit"]');
      expect(submitButton.disabled).toBeFalsy();
    });
  });
});
