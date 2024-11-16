import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
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

  it('should handle all session operations', (done) => {
    // Setup test data
    const sessionId = '1';
    const userId = '2';
    const updatedSession = { ...mockSession, name: 'Updated Yoga Class' };
    const newSession = { ...mockSession };
    delete newSession.id;

    // Mock HTTP responses
    httpClientSpy.get
      .mockReturnValueOnce(of([mockSession])) // for all()
      .mockReturnValueOnce(of(mockSession));  // for detail()
    httpClientSpy.post
      .mockReturnValueOnce(of(mockSession))   // for create()
      .mockReturnValueOnce(of(void 0));       // for participate()
    httpClientSpy.put.mockReturnValue(of(updatedSession));
    httpClientSpy.delete
      .mockReturnValueOnce(of({}))            // for delete()
      .mockReturnValueOnce(of(void 0));       // for unParticipate()

    // Test 1: Get all sessions
    service.all().subscribe(sessions => {
      expect(sessions).toEqual([mockSession]);
      expect(httpClientSpy.get).toHaveBeenCalledWith('api/session');

      // Test 2: Get session detail
      service.detail(sessionId).subscribe(session => {
        expect(session).toEqual(mockSession);
        expect(httpClientSpy.get).toHaveBeenCalledWith(`api/session/${sessionId}`);

        // Test 3: Create new session
        service.create(newSession).subscribe(createdSession => {
          expect(createdSession).toEqual(mockSession);
          expect(httpClientSpy.post).toHaveBeenCalledWith('api/session', newSession);

          // Test 4: Update session
          service.update(sessionId, updatedSession).subscribe(updated => {
            expect(updated).toEqual(updatedSession);
            expect(httpClientSpy.put).toHaveBeenCalledWith(`api/session/${sessionId}`, updatedSession);

            // Test 5: Delete session
            service.delete(sessionId).subscribe(response => {
              expect(response).toEqual({});
              expect(httpClientSpy.delete).toHaveBeenCalledWith(`api/session/${sessionId}`);

              // Test 6: Participate in session
              service.participate(sessionId, userId).subscribe(() => {
                expect(httpClientSpy.post).toHaveBeenCalledWith(
                  `api/session/${sessionId}/participate/${userId}`,
                  null
                );

                // Test 7: Unparticipate from session
                service.unParticipate(sessionId, userId).subscribe(() => {
                  expect(httpClientSpy.delete).toHaveBeenCalledWith(
                    `api/session/${sessionId}/participate/${userId}`
                  );

                  // All tests complete
                  done();
                }, done.fail);
              }, done.fail);
            }, done.fail);
          }, done.fail);
        }, done.fail);
      }, done.fail);
    }, done.fail);
  });

  // Verify spy calls counts
  afterEach(() => {
    expect(httpClientSpy.get).toHaveBeenCalledTimes(2);  // all() and detail()
    expect(httpClientSpy.post).toHaveBeenCalledTimes(2); // create() and participate()
    expect(httpClientSpy.put).toHaveBeenCalledTimes(1);  // update()
    expect(httpClientSpy.delete).toHaveBeenCalledTimes(2); // delete() and unParticipate()
  });
});
