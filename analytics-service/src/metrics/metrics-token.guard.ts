import { timingSafeEqual } from 'node:crypto';
import {
  CanActivate,
  ExecutionContext,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { Request } from 'express';

const BEARER_PREFIX = 'Bearer ';

// Authenticates the Prometheus scraper by a static shared bearer token from METRICS_TOKEN,
// the same scheme as ticket-service's MetricsTokenFilter. Not a Keycloak JWT, so it never
// goes through AuthGuard('jwt'). An unset or empty METRICS_TOKEN fails closed: every
// scrape is rejected.
@Injectable()
export class MetricsTokenGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const expected = Buffer.from(process.env.METRICS_TOKEN ?? '');
    const header = context
      .switchToHttp()
      .getRequest<Request>()
      .header('authorization');

    if (
      expected.length === 0 ||
      header === undefined ||
      !header.startsWith(BEARER_PREFIX)
    ) {
      throw new UnauthorizedException();
    }

    const presented = Buffer.from(header.slice(BEARER_PREFIX.length));
    // timingSafeEqual throws on different lengths, so check that first; only the length
    // leaks, never how much of a guess was right.
    if (
      presented.length !== expected.length ||
      !timingSafeEqual(presented, expected)
    ) {
      throw new UnauthorizedException();
    }
    return true;
  }
}
