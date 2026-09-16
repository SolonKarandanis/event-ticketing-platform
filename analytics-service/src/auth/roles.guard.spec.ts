import { ExecutionContext, ForbiddenException } from '@nestjs/common';
import { RolesGuard } from './roles.guard';

function contextWithUser(
  user: { userId: string; roles: string[] } | undefined,
) {
  return {
    switchToHttp: () => ({
      getRequest: () => ({ user }),
    }),
  } as unknown as ExecutionContext;
}

describe('RolesGuard', () => {
  const guard = new RolesGuard();

  it('allows a request whose roles include ROLE_ORGANIZER', () => {
    const context = contextWithUser({
      userId: 'user-1',
      roles: ['ROLE_ATTENDEE', 'ROLE_ORGANIZER'],
    });

    expect(guard.canActivate(context)).toBe(true);
  });

  it('rejects a request missing ROLE_ORGANIZER', () => {
    const context = contextWithUser({
      userId: 'user-1',
      roles: ['ROLE_ATTENDEE'],
    });

    expect(() => guard.canActivate(context)).toThrow(ForbiddenException);
  });

  it('rejects a request with no roles at all', () => {
    const context = contextWithUser({ userId: 'user-1', roles: [] });

    expect(() => guard.canActivate(context)).toThrow(ForbiddenException);
  });

  it('rejects a request with no user on it', () => {
    const context = contextWithUser(undefined);

    expect(() => guard.canActivate(context)).toThrow(ForbiddenException);
  });
});
