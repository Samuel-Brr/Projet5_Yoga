describe('User Account', () => {
  describe('Regular User', () => {
    beforeEach(() => {
      cy.interceptLoginSuccess(false)
      cy.login('user@example.com', 'userpass');
      cy.intercept('api/user/*', {
        body: {
          id: 4,
          email: 'user@example.com',
          firstName: 'John',
          lastName: 'DOE',
          admin: false
        },
      })
    });

    it('should display user information correctly', () => {
      cy.get('[data-testid="account-link"]').click()
      cy.get('[data-testid=user-name]').should('contain', 'John DOE');
      cy.get('[data-testid=user-email]').should('contain', 'user@example.com');
    });

    it('should delete account successfully', () => {
      cy.intercept('DELETE', '/api/user/*', {
        statusCode: 200
      }).as('deleteAccount');

      cy.get('[data-testid="account-link"]').click()
      cy.get('[data-testid=delete-button]').click();

      cy.wait('@deleteAccount');
      cy.url().should('match', /^http:\/\/localhost:4200\/?$/);
      cy.get('[data-testid=login-link]').should('be.visible');
    });
  });

  describe('Admin User', () => {
    beforeEach(() => {
      cy.interceptLoginSuccess(true)
      cy.login('admin@example.com', 'adminpass');
      cy.intercept('api/user/*', {
        body: {
          id: 4,
          email: 'user@example.com',
          firstName: 'John',
          lastName: 'DOE',
          admin: true
        },
      })
    });

    it('should not show delete account button', () => {
      cy.get('[data-testid="account-link"]').click()
      cy.get('[data-testid=delete-button]').should('not.exist');
    });
  });
});
