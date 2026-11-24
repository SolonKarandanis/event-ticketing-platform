import { Controller, Get, Res, UseGuards } from '@nestjs/common';
import { PrometheusController } from '@willsoto/nestjs-prometheus';
import type { Response } from 'express';
import { MetricsTokenGuard } from './metrics-token.guard';

// The library's own /metrics controller, behind the scraper token. PrometheusModule sets
// the route path (/metrics by default) on whichever controller it's given.
@Controller()
@UseGuards(MetricsTokenGuard)
export class MetricsController extends PrometheusController {
  @Get()
  index(@Res({ passthrough: true }) response: Response) {
    return super.index(response);
  }
}
