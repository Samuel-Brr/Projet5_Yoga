describe('Authentication', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('should register a new user successfully', () => {
    cy.intercept('POST', '/api/auth/register', {statusCode: 200})

    const email = `test${Date.now()}@example.com`;
    cy.register(email, 'John', 'Doe', 'password123');
    cy.url().should('include', '/login');
  });

  it('should show error message for existing email during registration', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 400,
      body: { message: 'Email already exists' }
    });

    cy.register('existing@example.com', 'John', 'Doe', 'password123');
    cy.get('[data-testid=error-message]').should('be.visible');
  });

  it('should login successfully', () => {
    cy.interceptLoginSuccess(true);
    cy.login('user@example.com', 'password123');
    cy.url().should('include', '/sessions');
    cy.get('[data-testid=user-menu]').should('be.visible');
  });

  it('should show error message for invalid credentials', () => {
    cy.interceptLoginError()
    cy.login('wrong@example.com', 'wrongpassword');
    cy.get('[data-testid=error-message]').should('be.visible');
  });

  it('should logout successfully', () => {
    cy.interceptLoginSuccess(true);
    cy.login('user@example.com', 'password123');
    cy.logout();
    cy.url().should('eq', Cypress.config().baseUrl);
    cy.get('[data-testid=login-link]').should('be.visible');
  });
});
