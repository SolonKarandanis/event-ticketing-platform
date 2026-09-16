import { KeycloakJwtStrategy } from './keycloak-jwt.strategy';

describe('KeycloakJwtStrategy', () => {
  let strategy: KeycloakJwtStrategy;

  beforeAll(() => {
    // The strategy's constructor wires up jwks-rsa's secretOrKeyProvider from this env
    // var -- validate() itself never touches it, but passport-jwt's own base class
    // constructor still needs *something* here to build without throwing.
    process.env.KEYCLOAK_JWKS_URI = 'http://localhost:9090/realms/test/certs';
    strategy = new KeycloakJwtStrategy();
  });

  it('maps the JWT subject and realm roles to { userId, roles }', () => {
    const result = strategy.validate({
      sub: 'user-1',
      realm_access: { roles: ['ROLE_ORGANIZER'] },
    });

    expect(result).toEqual({ userId: 'user-1', roles: ['ROLE_ORGANIZER'] });
  });

  it('defaults roles to an empty array when realm_access is absent', () => {
    // This is the exact shape ticket-service's own realm-roles mapper produces when a
    // token has no realm_access claim at all (see JwtAuthenticationConverter) --
    // RolesGuard needs a real array to call .includes() on, never undefined.
    const result = strategy.validate({ sub: 'user-1' });

    expect(result).toEqual({ userId: 'user-1', roles: [] });
  });
});
