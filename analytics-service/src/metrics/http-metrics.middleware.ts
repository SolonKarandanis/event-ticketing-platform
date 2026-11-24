import { Injectable, NestMiddleware } from '@nestjs/common';
import { InjectMetric } from '@willsoto/nestjs-prometheus';
import { Histogram } from '@prometheus-io/client';
import { NextFunction, Request, Response } from 'express';

export const HTTP_SERVER_REQUESTS = 'http_server_requests_seconds';

// Scrapes and probes would otherwise swamp the histogram with their own traffic.
const UNTRACKED_ROUTES = new Set(['/metrics', '/health']);

// Times every request into http_server_requests_seconds, the same name ticket-service's
// Micrometer uses, so one set of dashboards covers both services.
@Injectable()
export class HttpMetricsMiddleware implements NestMiddleware {
  constructor(
    @InjectMetric(HTTP_SERVER_REQUESTS)
    private readonly histogram: Histogram<'method' | 'route' | 'status_code'>,
  ) {}

  use(req: Request, res: Response, next: NextFunction) {
    const end = this.histogram.startTimer();
    res.on('finish', () => {
      // The route pattern (/analytics/events/:eventId/summary), never req.url: raw URLs
      // carry IDs and would make one time series per event. req.route is only set once
      // Express has matched a handler, which is why this is read here and not up front.
      // Unmatched requests (404s) share one label for the same reason.
      const route = (req.route as { path?: string } | undefined)?.path;
      if (route !== undefined && UNTRACKED_ROUTES.has(route)) {
        return;
      }
      end({
        method: req.method,
        route: route ?? 'UNMATCHED',
        status_code: String(res.statusCode),
      });
    });
    next();
  }
}
