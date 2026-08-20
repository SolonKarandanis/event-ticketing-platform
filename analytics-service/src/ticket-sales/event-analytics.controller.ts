import { Controller, Get, Param, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { TicketSalesService } from './ticket-sales.service';

@Controller('analytics/events')
@UseGuards(AuthGuard('jwt'))
export class EventAnalyticsController {
  constructor(private readonly ticketSalesService: TicketSalesService) {}

  @Get(':eventId/summary')
  getSummary(@Param('eventId') eventId: string) {
    return this.ticketSalesService.getSummaryForEvent(eventId);
  }
}
