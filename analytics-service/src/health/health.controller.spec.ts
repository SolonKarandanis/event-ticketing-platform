import type { Server } from 'node:http';
import { INestApplication, Logger } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import request from 'supertest';
import { DRIZZLE } from '../db/drizzle.provider';
import { HealthController } from './health.controller';

// Over real HTTP with a stubbed Drizzle client: checks the route, the status codes and
// that no auth is needed, without a Postgres container.
describe('HealthController', () => {
  let app: INestApplication;
  const execute = jest.fn();

  beforeAll(async () => {
    const moduleRef = await Test.createTestingModule({
      controllers: [HealthController],
      providers: [{ provide: DRIZZLE, useValue: { execute } }],
    }).compile();

    app = moduleRef.createNestApplication({ logger: false });
    await app.init();
  });

  afterAll(async () => {
    await app.close();
  });

  beforeEach(() => {
    execute.mockReset();
    jest.spyOn(Logger.prototype, 'error').mockImplementation(() => undefined);
  });

  it('returns 200 UP without a token when the database answers', async () => {
    execute.mockResolvedValue([{ '?column?': 1 }]);

    const response = await request(app.getHttpServer() as Server)
      .get('/health')
      .expect(200);

    expect(response.body).toEqual({ status: 'UP' });
    expect(execute).toHaveBeenCalledTimes(1);
  });

  it('returns 503 DOWN, without the error details, when the database is unreachable', async () => {
    execute.mockRejectedValue(new Error('connect ECONNREFUSED 10.0.0.5:5432'));

    const response = await request(app.getHttpServer() as Server)
      .get('/health')
      .expect(503);

    expect(response.body).toEqual({ status: 'DOWN' });
    expect(JSON.stringify(response.body)).not.toContain('ECONNREFUSED');
  });
});
