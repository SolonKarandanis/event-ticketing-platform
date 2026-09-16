import { Controller, Get, Req, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { Request } from 'express';
import { RolesGuard } from '../auth/roles.guard';
import { TicketSalesService } from './ticket-sales.service';

interface AuthenticatedRequest extends Request {
  user?: { userId: string; roles: string[] };
}

// Separate from EventAnalyticsController on purpose -- that one is scoped to a single
// event (organizerId is still read off the JWT, but eventId comes from the URL); this
// one is scoped to the calling organizer as a whole, with no id in the path at all.
// Same resource area (ticket-sales analytics), different shape, so it gets its own
// controller rather than a same-class route that only differs by segment count.
@Controller('analytics/organizer')
@UseGuards(AuthGuard('jwt'), RolesGuard)
export class OrganizerAnalyticsController {
  constructor(private readonly ticketSalesService: TicketSalesService) {}

  @Get('summary')
  getSummary(@Req() req: AuthenticatedRequest) {
    const organizerId = req.user!.userId;
    return this.ticketSalesService.getSummaryForOrganizer(organizerId);
  }

  // Fixed 30-day window, no query param -- matches the reports dashboard's existing
  // "chart only, no filter controls" scope (issue #8). getSalesOverTime's own `days`
  // parameter stays a plain method default, not wired to the request, so adding a real
  // range picker later is a controller change, not a service one.
  @Get('sales-over-time')
  getSalesOverTime(@Req() req: AuthenticatedRequest) {
    const organizerId = req.user!.userId;
    return this.ticketSalesService.getSalesOverTime(organizerId);
  }
}
