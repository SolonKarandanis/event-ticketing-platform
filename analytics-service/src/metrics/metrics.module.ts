import { MiddlewareConsumer, Module, NestModule } from '@nestjs/common';
import {
  makeHistogramProvider,
  PrometheusModule,
} from '@willsoto/nestjs-prometheus';
import {
  HTTP_SERVER_REQUESTS,
  HttpMetricsMiddleware,
} from './http-metrics.middleware';

// GET /metrics for the Prometheus scraper: Node's default process metrics (memory,
// event-loop lag, GC) plus HTTP request timings from HttpMetricsMiddleware.
// No guard on purpose, like /health: auth is per-controller (@UseGuards), not global,
// so the library's controller is public. Keep the scraper on a private network.
@Module({
  imports: [PrometheusModule.register()],
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
