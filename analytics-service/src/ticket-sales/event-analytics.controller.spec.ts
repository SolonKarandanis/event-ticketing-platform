import { EventAnalyticsController } from './event-analytics.controller';
import { TicketSalesService } from './ticket-sales.service';

describe('EventAnalyticsController', () => {
  it('reads organizerId off req.user, not the URL, and forwards it to the service', async () => {
    const getSummaryForEvent = jest.fn().mockResolvedValue({
      eventId: 'event-1',
      ticketsSold: 3,
      revenue: 75,
    });
    const ticketSalesService = {
      getSummaryForEvent,
    } as unknown as TicketSalesService;
    const controller = new EventAnalyticsController(ticketSalesService);

    const result = await controller.getSummary('event-1', {
      user: { userId: 'organizer-1', roles: ['ROLE_ORGANIZER'] },
    } as never);

    // The org id comes from the authenticated JWT (RolesGuard/KeycloakJwtStrategy
    // already ran by the time this method executes), not a request param -- there's no
    // way for a caller to ask for someone else's summary by editing a query string.
    expect(getSummaryForEvent).toHaveBeenCalledWith('event-1', 'organizer-1');
    expect(result).toEqual({ eventId: 'event-1', ticketsSold: 3, revenue: 75 });
  });
});
