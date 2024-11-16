import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { ListComponent } from './list.component';
import { SessionService } from '../../../../services/session.service';
import { SessionApiService } from '../../services/session-api.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let mockSessionService: Partial<SessionService>;
  let mockSessionApiService: Partial<SessionApiService>;

  const mockSessions = [
    {
      id: 1,
      name: 'Yoga Session',
      description: 'Relaxing yoga session',
      date: new Date('2024-10-25'),
      teacher_id: 1,
      users: [1, 2, 3],
      createdAt: new Date(),
      updatedAt: new Date()
    },
    {
      id: 2,
      name: 'Pilates Session',
      description: 'Core strengthening',
      date: new Date('2024-10-26'),
      teacher_id: 2,
      users: [1, 4],
      createdAt: new Date(),
      updatedAt: new Date()
    }
  ];

  const mockSessionInfo = {
    token: 'fake-token',
    type: 'Bearer',
    id: 1,
    username: 'testuser',
    firstName: 'Test',
    lastName: 'User',
    admin: true
  };

  beforeEach(async () => {
    // Create mocks
    mockSessionService = {
      sessionInformation: mockSessionInfo
    };

    mockSessionApiService = {
      all: jest.fn().mockReturnValue(of(mockSessions))
    };

    await TestBed.configureTestingModule({
      declarations: [ListComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: SessionService, useValue: mockSessionService },
        { provide: SessionApiService, useValue: mockSessionApiService }
      ],
      schemas: [NO_ERRORS_SCHEMA] // This will ignore unknown elements and attributes
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
  });

  it('should fetch sessions on init', (done) => {
    component.sessions$.subscribe(sessions => {
      expect(sessions).toEqual(mockSessions);
      expect(sessions.length).toBe(2);
      expect(mockSessionApiService.all).toHaveBeenCalled();
      done();
    });

    //should return correct user information
    expect(component.user).toEqual(mockSessionInfo);
  });

  describe('template tests', () => {
    beforeEach(() => {
      fixture.detectChanges(); // Initial data binding
    });

    it('should render elements', (done) => {
      component.sessions$.subscribe(() => {
        fixture.detectChanges();
        const listElement = fixture.nativeElement.querySelector('.list');
        expect(listElement).toBeTruthy();

        const compiled = fixture.nativeElement;
        expect(compiled.textContent).toContain('Yoga Session');
        expect(compiled.textContent).toContain('Pilates Session');
        expect(compiled.textContent).toContain('Relaxing yoga session');
        expect(compiled.textContent).toContain('Core strengthening');

        const createButton = fixture.nativeElement.querySelector('[data-testid="create-button"]');
        expect(createButton).toBeTruthy();
        done();
      });
    });
  });

  describe('admin functionality', () => {
    it('should hide create button for non-admin users', (done) => {
      mockSessionService.sessionInformation = { ...mockSessionInfo, admin: false };
      fixture.detectChanges();

      component.sessions$.subscribe(() => {
        fixture.detectChanges();
        const createButton = fixture.nativeElement.querySelector('[data-testid="create-button"]');
        expect(createButton).toBeFalsy();
        done();
      });
    });
  });
});
