describe('Navigation', () => {
  describe('Unauthenticated User', () => {
    beforeEach(() => {
      cy.visit('/');
    });

    it('should redirect to login when accessing protected routes', () => {
      cy.visit('/sessions');
      cy.url().should('include', '/login');
    });

    it('should show login and register links', () => {
      cy.get('[data-testid=login-link]').should('be.visible');
      cy.get('[data-testid=register-link]').should('be.visible');
    });
  });

  describe('Authenticated User', () => {
    beforeEach(() => {
      cy.interceptLoginSuccess(true)
      cy.login('user@example.com', 'userpass');
      cy.url().should('include', '/sessions');
    });

    it('should navigate between pages successfully', () => {
      cy.get('[data-testid=account-link]').click();
      cy.url().should('include', '/me');
      cy.get('[data-testid=sessions-link]').click();
      cy.url().should('include', '/sessions');
    });

    it('should show user menu items', () => {
      cy.get('[data-testid=sessions-link]').should('be.visible');
      cy.get('[data-testid=account-link]').should('be.visible');
      cy.get('[data-testid=logout-button]').should('be.visible');
    });
  });
});
