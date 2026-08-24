import { NestFactory } from '@nestjs/core';
import { migrate } from 'drizzle-orm/postgres-js/migrator';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { AppModule } from './app.module';
import { DRIZZLE } from './db/drizzle.provider';
import * as schema from './db/schema';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Matches ticket-service's SecurityConfig CORS setup -- same origin, same allowed
  // headers (Accept-Language for locale-aware error messages, Authorization for the
  // Keycloak bearer token both services validate). No deployment plan yet (local dev
  // only), so this is the only origin that exists right now.
  app.enableCors({
    origin: 'http://localhost:3000',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Authorization', 'Content-Type', 'Accept-Language'],
  });

  const db = app.get<PostgresJsDatabase<typeof schema>>(DRIZZLE);
  await migrate(db, { migrationsFolder: './drizzle' });

  await app.listen(process.env.PORT ?? 3001);
}
void bootstrap();
