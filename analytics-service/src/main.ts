import { NestFactory } from '@nestjs/core';
import { migrate } from 'drizzle-orm/postgres-js/migrator';
import { AppModule } from './app.module';
import { DRIZZLE } from './db/drizzle.provider';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  const db = app.get(DRIZZLE);
  await migrate(db, { migrationsFolder: './drizzle' });

  await app.listen(process.env.PORT ?? 3001);
}
bootstrap();
