import {ComponentFixture, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {Router} from '@angular/router';
import {BehaviorSubject} from 'rxjs';
import {AppComponent} from './app.component';
import {SessionService} from './services/session.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let router: Router;
  let sessionService: SessionService;
  let isLoggedSubject: BehaviorSubject<boolean>;

  beforeEach(async () => {
    isLoggedSubject = new BehaviorSubject<boolean>(false);

    const mockSessionService = {
      $isLogged: () => isLoggedSubject.asObservable(),
      logOut: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    router = TestBed.inject(Router);
    sessionService = TestBed.inject(SessionService);

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should check if user is logged in', () => {
    const result = component.$isLogged();
    expect(result).toBeTruthy();
  });

  it('should handle logout', () => {
    // Arrange
    const navigateSpy = jest.spyOn(router, 'navigate');

    // Act
    component.logout();

    // Assert
    expect(sessionService.logOut).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['']);
  });

  it('should show login/register when logged out', () => {
    // Arrange
    isLoggedSubject.next(false);
    fixture.detectChanges();

    // Act & Assert
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('[routerlink="login"]')).toBeTruthy();
    expect(compiled.querySelector('[routerlink="register"]')).toBeTruthy();
  });

  it('should show sessions/account/logout when logged in', () => {
    // Arrange
    isLoggedSubject.next(true);
    fixture.detectChanges();

    // Act & Assert
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('[routerlink="sessions"]')).toBeTruthy();
    expect(compiled.querySelector('[routerlink="me"]')).toBeTruthy();
    expect(compiled.querySelector('.link')).toBeTruthy();
  });
});
