import { INestApplication } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';

// Its own module (rather than inlined in main.ts) so the OpenAPI docs integration test
// can call the exact same setup main.ts runs at boot, instead of a copy that could
// silently drift out of sync with what's actually served.
export function setupSwagger(app: INestApplication): void {
  const document = SwaggerModule.createDocument(
    app,
    new DocumentBuilder()
      .setTitle('analytics-service')
      .setDescription('Read-only ticket sales reporting API')
      .setVersion('1.0')
      .addBearerAuth()
      .build(),
  );
  SwaggerModule.setup('api-docs', app, document);
}
