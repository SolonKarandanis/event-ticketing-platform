import { NestFactory } from '@nestjs/core';
import { migrate } from 'drizzle-orm/postgres-js/migrator';
import { PostgresJsDatabase } from 'drizzle-orm/postgres-js';
import { AppModule } from './app.module';
import { DRIZZLE } from './db/drizzle.provider';
import * as schema from './db/schema';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  const db = app.get<PostgresJsDatabase<typeof schema>>(DRIZZLE);
  await migrate(db, { migrationsFolder: './drizzle' });

  await app.listen(process.env.PORT ?? 3001);
}
void bootstrap();
