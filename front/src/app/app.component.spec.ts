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
        {provide: SessionService, useValue: mockSessionService},
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    router = TestBed.inject(Router);
    sessionService = TestBed.inject(SessionService);

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show login/register when logged out', () => {
    // Arrange
    component.logout();
    fixture.detectChanges();

    // Act & Assert
    const loggedOut = fixture.nativeElement as HTMLElement;
    expect(loggedOut.querySelector('[routerlink="login"]')).toBeTruthy();
    expect(loggedOut.querySelector('[routerlink="register"]')).toBeTruthy();

    isLoggedSubject.next(true);
    fixture.detectChanges();

    // Act & Assert
    const loggedIn = fixture.nativeElement as HTMLElement;
    expect(loggedIn.querySelector('[routerlink="sessions"]')).toBeTruthy();
    expect(loggedIn.querySelector('[routerlink="me"]')).toBeTruthy();
    expect(loggedIn.querySelector('.link')).toBeTruthy();
  });
});
