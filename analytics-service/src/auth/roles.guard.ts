import {
  CanActivate,
  ExecutionContext,
  ForbiddenException,
  Injectable,
} from '@nestjs/common';
import { Request } from 'express';

interface AuthenticatedRequest extends Request {
  user?: { userId: string; roles: string[] };
}

// Deliberately simple -- no generic @Roles(...) decorator/Reflector metadata, since exactly
// one controller needs a role check today. KeycloakJwtStrategy passes realm_access.roles
// straight through unmodified, so the literal "ROLE_" prefix from the raw JWT claim is
// still present here (unlike ticket-service's converter, which strips it).
@Injectable()
export class RolesGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest<AuthenticatedRequest>();
    const roles = request.user?.roles ?? [];

    if (!roles.includes('ROLE_ORGANIZER')) {
      throw new ForbiddenException();
    }

    return true;
  }
}
