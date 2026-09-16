import { OrganizerAnalyticsController } from './organizer-analytics.controller';
import { TicketSalesService } from './ticket-sales.service';

describe('OrganizerAnalyticsController', () => {
  it('reads organizerId off req.user and forwards it to the service', async () => {
    const getSummaryForOrganizer = jest
      .fn()
      .mockResolvedValue({ ticketsSold: 5, revenue: 120 });
    const ticketSalesService = {
      getSummaryForOrganizer,
    } as unknown as TicketSalesService;
    const controller = new OrganizerAnalyticsController(ticketSalesService);

    const result = await controller.getSummary({
      user: { userId: 'organizer-1', roles: ['ROLE_ORGANIZER'] },
    } as never);

    expect(getSummaryForOrganizer).toHaveBeenCalledWith('organizer-1');
    expect(result).toEqual({ ticketsSold: 5, revenue: 120 });
  });

  it('reads organizerId off req.user for the sales-over-time route too, with no days param', async () => {
    const series = [{ date: '2026-01-01', ticketsSold: 1, revenue: 10 }];
    const getSalesOverTime = jest.fn().mockResolvedValue(series);
    const ticketSalesService = {
      getSalesOverTime,
    } as unknown as TicketSalesService;
    const controller = new OrganizerAnalyticsController(ticketSalesService);

    const result = await controller.getSalesOverTime({
      user: { userId: 'organizer-1', roles: ['ROLE_ORGANIZER'] },
    } as never);

    // No second argument -- the fixed-30-day default lives on the service, the
    // controller never wires a query param to it (see the route's own comment).
    expect(getSalesOverTime).toHaveBeenCalledWith('organizer-1');
    expect(result).toEqual(series);
  });
});
