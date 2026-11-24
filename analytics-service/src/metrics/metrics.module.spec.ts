import type { Server } from 'node:http';
import { Controller, Get, INestApplication, Param } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import request from 'supertest';
import { MetricsModule } from './metrics.module';

@Controller('things')
class ThingsController {
  @Get(':id')
  get(@Param('id') id: string) {
    return { id };
  }
}

const METRICS_TOKEN = 'test-metrics-token';

// Over real HTTP, like HealthController's spec: checks /metrics needs the scraper token
// and that requests are recorded by route pattern rather than raw URL.
describe('MetricsModule', () => {
  let app: INestApplication;
  const originalToken = process.env.METRICS_TOKEN;

  beforeAll(async () => {
    process.env.METRICS_TOKEN = METRICS_TOKEN;
    const moduleRef = await Test.createTestingModule({
      imports: [MetricsModule],
      controllers: [ThingsController],
    }).compile();

    app = moduleRef.createNestApplication({ logger: false });
    await app.init();
  });

  afterAll(async () => {
    await app.close();
    if (originalToken === undefined) {
      delete process.env.METRICS_TOKEN;
    } else {
      process.env.METRICS_TOKEN = originalToken;
    }
  });

  const scrape = async () =>
    (
      await request(app.getHttpServer() as Server)
        .get('/metrics')
        .set('Authorization', `Bearer ${METRICS_TOKEN}`)
        .expect(200)
    ).text;

  it('serves Node process metrics with the scraper token', async () => {
    const body = await scrape();

    expect(body).toContain('process_resident_memory_bytes');
    expect(body).toContain('nodejs_eventloop_lag_seconds');
    expect(body).toContain('nodejs_gc_duration_seconds');
  });

  it('records requests by route pattern, not by raw URL', async () => {
    const server = app.getHttpServer() as Server;
    await request(server).get('/things/42').expect(200);
    await request(server).get('/things/43').expect(200);

    const body = await scrape();

    expect(body).toMatch(
      /http_server_requests_seconds_count\{[^}]*route="\/things\/:id"[^}]*\} 2/,
    );
    expect(body).not.toContain('/things/42');
  });

  it('groups unmatched requests under one label', async () => {
    await request(app.getHttpServer() as Server)
      .get('/nope/123')
      .expect(404);

    const body = await scrape();

    expect(body).toContain('route="UNMATCHED"');
    expect(body).not.toContain('/nope/123');
  });

  it('does not record its own scrapes', async () => {
    await scrape();

    expect(await scrape()).not.toContain('route="/metrics"');
  });

  describe('without the right token', () => {
    const scrapeWith = (authorization?: string) => {
      const req = request(app.getHttpServer() as Server).get('/metrics');
      return authorization === undefined
        ? req
        : req.set('Authorization', authorization);
    };

    it('rejects a missing header', async () => {
      await scrapeWith().expect(401);
    });

    it('rejects a wrong token, including one of a different length', async () => {
      await scrapeWith('Bearer test-metrics-tokeX').expect(401);
      await scrapeWith('Bearer short').expect(401);
    });

    it('rejects the token without the Bearer scheme', async () => {
      await scrapeWith(METRICS_TOKEN).expect(401);
    });

    it('rejects everything when METRICS_TOKEN is empty', async () => {
      process.env.METRICS_TOKEN = '';
      try {
        await scrapeWith('Bearer ').expect(401);
        await scrapeWith(`Bearer ${METRICS_TOKEN}`).expect(401);
      } finally {
        process.env.METRICS_TOKEN = METRICS_TOKEN;
      }
    });
  });
});
