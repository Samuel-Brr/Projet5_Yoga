describe('Session Management', () => {
  beforeEach(() => {
    cy.intercept('GET', '/api/teacher', { fixture: 'teachers.json' });
    cy.intercept('GET', '/api/teacher/1', { fixture: 'teachers.json' });
    cy.intercept('GET', '/api/session/1', { fixture: 'session-detail.json' });
  });

  describe('Admin User', () => {
    beforeEach(() => {
      cy.interceptLoginSuccess(true)
      cy.login('admin@example.com', 'adminpass');
    });

    it('should create a new session', () => {
      cy.intercept('POST', '/api/session', {
        statusCode: 201,
        body: { id: 1, name: 'New Yoga Session' }
      }).as('createSession');

      cy.get('[data-testid=create-button]').click();
      cy.get('input[formControlName=name]').type('New Yoga Session');
      cy.get('input[formControlName=date]').type('2024-12-25');
      cy.get('#mat-select-value-1').click();
      cy.get('#mat-option-0 span').click();
      cy.get('textarea[formControlName=description]').type('Description');
      cy.get('[data-testid=save-session-button]').click();

      cy.wait('@createSession');
      cy.url().should('include', '/sessions');
    });

    it('should edit an existing session', () => {
      cy.intercept('PUT', '/api/session/*', {
        statusCode: 200,
        body: { id: 1, name: 'Updated Session' }
      }).as('updateSession');

      cy.get('[data-testid=edit-button]').click();
      cy.get('input[formControlName=name]').clear().type('Updated Session');
      cy.get('[data-testid=save-session-button]').click();

      cy.wait('@updateSession');
      cy.url().should('include', '/sessions');
    });

    it('should delete a session', () => {
      cy.intercept('DELETE', '/api/session/*', {
        statusCode: 200
      }).as('deleteSession');

      cy.get('[data-testid=detail-button]').click();
      cy.get('[data-testid=delete-button]').click();

      cy.wait('@deleteSession');
      cy.url().should('include', '/sessions');
    });
  });

  describe('Regular User', () => {
    beforeEach(() => {
      cy.interceptLoginSuccess(false)
      cy.login('user@example.com', 'userpass');
      cy.wait('@getSessions');
    });

    it('should view session details', () => {
      cy.get('[data-testid=detail-button]').click();

      cy.get('[data-testid=session-name]').should('be.visible');
      cy.get('[data-testid=session-description]').should('be.visible');
      cy.get('[data-testid=session-teacher]').should('be.visible');
    });

    it('should participate in a session', () => {
      cy.intercept('POST', '/api/session/*/participate/*', {
        statusCode: 200
      }).as('participate');

      cy.get('[data-testid=detail-button]').click();
      cy.intercept('GET', '/api/session/1', {
        "id": 1,
        "name": "Yoga Session",
        "description": "Beginner friendly yoga",
        "date": "2024-12-25T10:00:00",
        "teacher_id": 1,
        "users": [1, 2, 3, 4]
      });
      cy.get('[data-testid="participate-button"]').click();
      cy.wait('@participate');

      cy.get('[data-testid="participate-button"]').should('not.exist');
      cy.get('[data-testid="unparticipate-button"]').should('be.visible');
    });

    it('should un-participate from a session', () => {
      cy.intercept('DELETE', '/api/session/*/participate/*', {
        statusCode: 200
      }).as('unParticipate');
      cy.intercept('POST', '/api/session/*/participate/*', {
        statusCode: 200
      }).as('participate');
      cy.intercept('GET', '/api/session/1', {
        "id": 1,
        "name": "Yoga Session",
        "description": "Beginner friendly yoga",
        "date": "2024-12-25T10:00:00",
        "teacher_id": 1,
        "users": [1, 2, 3, 4]
      });

      cy.get('[data-testid=detail-button]').click();
      cy.intercept('GET', '/api/session/1', { fixture: 'session-detail.json' });
      cy.get('[data-testid="unparticipate-button"]').click();
      cy.wait('@unParticipate');

      cy.get('[data-testid="unparticipate-button"]').should('not.exist');
      cy.get('[data-testid="participate-button"]').should('be.visible');
    });
  });
});
