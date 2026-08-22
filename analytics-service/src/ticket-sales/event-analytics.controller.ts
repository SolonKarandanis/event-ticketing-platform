import { Controller, Get, Param, Req, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { Request } from 'express';
import { RolesGuard } from '../auth/roles.guard';
import { TicketSalesService } from './ticket-sales.service';

interface AuthenticatedRequest extends Request {
  user?: { userId: string; roles: string[] };
}

@Controller('analytics/events')
@UseGuards(AuthGuard('jwt'), RolesGuard)
export class EventAnalyticsController {
  constructor(private readonly ticketSalesService: TicketSalesService) {}

  @Get(':eventId/summary')
  getSummary(
    @Param('eventId') eventId: string,
    @Req() req: AuthenticatedRequest,
  ) {
    const organizerId = req.user!.userId;
    return this.ticketSalesService.getSummaryForEvent(eventId, organizerId);
  }
}
