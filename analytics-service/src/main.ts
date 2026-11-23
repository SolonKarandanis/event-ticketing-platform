import { NestFactory } from '@nestjs/core';
import { migrate } from 'drizzle-orm/postgres-js/migrator';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { AppModule } from './app.module';
import { DRIZZLE } from './db/drizzle.provider';
import { setupSwagger } from './swagger';
import * as schema from './db/schema';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Matches ticket-service's SecurityConfig CORS setup -- same allowed headers
  // (Accept-Language for locale-aware error messages, Authorization for the
  // Keycloak bearer token both services validate). The origin is the frontend's
  // URL: localhost in dev, set via CORS_ORIGIN on deployed servers.
  app.enableCors({
    origin: process.env.CORS_ORIGIN ?? 'http://localhost:3000',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Authorization', 'Content-Type', 'Accept-Language'],
  });

  // Public spec + UI, no auth gate of its own. Extracted to swagger.ts so the
  // integration test exercises this exact setup, not a copy of it.
  setupSwagger(app);

  // Deployed servers set DB_MIGRATE_ON_STARTUP=false: CI runs the migrations once
  // before a rollout, because Drizzle has no lock and two nodes starting at the
  // same time would race. Locally it still migrates on startup.
  if (process.env.DB_MIGRATE_ON_STARTUP !== 'false') {
    const db = app.get<PostgresJsDatabase<typeof schema>>(DRIZZLE);
    await migrate(db, { migrationsFolder: './drizzle' });
  }

  await app.listen(process.env.PORT ?? 3001);
}
void bootstrap();
