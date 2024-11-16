import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { SessionApiService } from './session-api.service';
import { Session } from '../interfaces/session.interface';

describe('SessionApiService', () => {
  let service: SessionApiService;
  let httpClientSpy: jest.Mocked<HttpClient>;

  const mockSession: Session = {
    id: 1,
    name: 'Yoga Class',
    description: 'Relaxing yoga session',
    date: new Date(),
    teacher_id: 1,
    users: [1, 2, 3],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  beforeEach(() => {
    httpClientSpy = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
    } as any;

    TestBed.configureTestingModule({
      providers: [
        SessionApiService,
        { provide: HttpClient, useValue: httpClientSpy }
      ]
    });

    service = TestBed.inject(SessionApiService);
  });

  describe('all', () => {
    it('should return all sessions', (done) => {
      const expectedSessions: Session[] = [mockSession];
      httpClientSpy.get.mockReturnValue(of(expectedSessions));

      service.all().subscribe({
        next: sessions => {
          expect(sessions).toEqual(expectedSessions);
          expect(httpClientSpy.get).toHaveBeenCalledWith('api/session');
          done();
        },
        error: done.fail
      });
    });
  });

  describe('detail', () => {
    it('should return a single session', (done) => {
      httpClientSpy.get.mockReturnValue(of(mockSession));
      const sessionId = '1';

      service.detail(sessionId).subscribe({
        next: session => {
          expect(session).toEqual(mockSession);
          expect(httpClientSpy.get).toHaveBeenCalledWith(`api/session/${sessionId}`);
          done();
        },
        error: done.fail
      });
    });
  });

  describe('delete', () => {
    it('should delete a session', (done) => {
      const sessionId = '1';
      httpClientSpy.delete.mockReturnValue(of({}));

      service.delete(sessionId).subscribe({
        next: response => {
          expect(response).toEqual({});
          expect(httpClientSpy.delete).toHaveBeenCalledWith(`api/session/${sessionId}`);
          done();
        },
        error: done.fail
      });
    });
  });

  describe('create', () => {
    it('should create a new session', (done) => {
      const newSession: Session = { ...mockSession };
      delete newSession.id;
      httpClientSpy.post.mockReturnValue(of(mockSession));

      service.create(newSession).subscribe({
        next: session => {
          expect(session).toEqual(mockSession);
          expect(httpClientSpy.post).toHaveBeenCalledWith('api/session', newSession);
          done();
        },
        error: done.fail
      });
    });
  });

  describe('update', () => {
    it('should update an existing session', (done) => {
      const sessionId = '1';
      const updatedSession: Session = { ...mockSession, name: 'Updated Yoga Class' };
      httpClientSpy.put.mockReturnValue(of(updatedSession));

      service.update(sessionId, updatedSession).subscribe({
        next: session => {
          expect(session).toEqual(updatedSession);
          expect(httpClientSpy.put).toHaveBeenCalledWith(`api/session/${sessionId}`, updatedSession);
          done();
        },
        error: done.fail
      });
    });
  });

  describe('participate', () => {
    it('should add user participation to session', (done) => {
      const sessionId = '1';
      const userId = '2';
      httpClientSpy.post.mockReturnValue(of(void 0));

      service.participate(sessionId, userId).subscribe({
        next: () => {
          expect(httpClientSpy.post).toHaveBeenCalledWith(
            `api/session/${sessionId}/participate/${userId}`,
            null
          );
          done();
        },
        error: done.fail
      });
    });
  });

  describe('unParticipate', () => {
    it('should remove user participation from session', (done) => {
      const sessionId = '1';
      const userId = '2';
      httpClientSpy.delete.mockReturnValue(of(void 0));

      service.unParticipate(sessionId, userId).subscribe({
        next: () => {
          expect(httpClientSpy.delete).toHaveBeenCalledWith(
            `api/session/${sessionId}/participate/${userId}`
          );
          done();
        },
        error: done.fail
      });
    });
  });
});
