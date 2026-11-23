import {
  Controller,
  Get,
  Inject,
  Logger,
  ServiceUnavailableException,
} from '@nestjs/common';
import { sql } from 'drizzle-orm';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { DRIZZLE } from '../db/drizzle.provider';
import * as schema from '../db/schema';

// No guards on purpose: load balancers and the deploy pipeline call this without a token.
// Auth here is per-controller (@UseGuards), not global, so leaving it off is enough.
@Controller('health')
export class HealthController {
  private readonly logger = new Logger(HealthController.name);

  constructor(
    @Inject(DRIZZLE) private readonly db: PostgresJsDatabase<typeof schema>,
  ) {}

  // Used by the deploy pipeline: 200 only when the app runs AND reaches its database.
  // A failed query is a 503 with {"status":"DOWN"} -- the same shape ticket-service's
  // actuator returns -- rather than Nest's generic 500, and the error is only logged,
  // never sent to an unauthenticated caller.
  @Get()
  async check() {
    try {
      await this.db.execute(sql`select 1`);
    } catch (error) {
      this.logger.error('Health check: database unreachable', error);
      throw new ServiceUnavailableException({ status: 'DOWN' });
    }
    return { status: 'UP' };
  }
}
