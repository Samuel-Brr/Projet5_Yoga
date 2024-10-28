declare namespace Cypress {
  interface Chainable {
    login(email: string, password: string): void;
    register(email: string, firstName: string, lastName: string, password: string): void;
    logout(): void;
    interceptLoginSuccess(isAdmin: boolean): void;
    interceptLoginError(): void;
  }
}

// Custom commands
Cypress.Commands.add('login', (email: string, password: string) => {
  cy.visit('/login');
  cy.get('input[formControlName=email]').type(email);
  cy.get('input[formControlName=password]').type(password);
  cy.get('[data-testid=login-button]').click();
});

Cypress.Commands.add('register', (email: string, firstName: string, lastName: string, password: string) => {
  cy.visit('/register');
  cy.get('input[formControlName=email]').type(email);
  cy.get('input[formControlName=firstName]').type(firstName);
  cy.get('input[formControlName=lastName]').type(lastName);
  cy.get('input[formControlName=password]').type(password);
  cy.get('[data-testid=register-button]').click();
});

Cypress.Commands.add('logout', () => {
  cy.get('[data-testid=logout-button]').click();
});

Cypress.Commands.add('interceptLoginSuccess', (isAdmin: boolean) => {
  cy.intercept('POST', '/api/auth/login', {
    body: {
      id: 4,
      email: 'user@example.com',
      firstName: 'John',
      lastName: 'DOE',
      admin: isAdmin
    },
  })

  cy.intercept('GET', '/api/session', { fixture: 'sessions.json' }).as('getSessions');

})

Cypress.Commands.add('interceptLoginError', () => {
  cy.intercept('POST', '/api/auth/login', {
    statusCode: 400,
    body: { message: 'invalid credentials' }
  });
})
