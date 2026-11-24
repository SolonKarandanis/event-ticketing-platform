import { MiddlewareConsumer, Module, NestModule } from '@nestjs/common';
import {
  makeHistogramProvider,
  PrometheusModule,
} from '@willsoto/nestjs-prometheus';
import {
  HTTP_SERVER_REQUESTS,
  HttpMetricsMiddleware,
} from './http-metrics.middleware';
import { MetricsController } from './metrics.controller';

// GET /metrics for the Prometheus scraper: Node's default process metrics (memory,
// event-loop lag, GC) plus HTTP request timings from HttpMetricsMiddleware.
// MetricsController replaces the library's public controller so scrapes need the
// METRICS_TOKEN bearer token (see MetricsTokenGuard).
@Module({
  imports: [PrometheusModule.register({ controller: MetricsController })],
  providers: [
    makeHistogramProvider({
      name: HTTP_SERVER_REQUESTS,
      help: 'Duration of HTTP requests in seconds',
      labelNames: ['method', 'route', 'status_code'],
    }),
    HttpMetricsMiddleware,
  ],
})
export class MetricsModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer.apply(HttpMetricsMiddleware).forRoutes('{*path}');
  }
}
