import { ConfirmChannel, ConsumeMessage } from 'amqplib';
import { RabbitMqConsumerService } from './rabbitmq-consumer.service';
import { TicketSalesService } from '../ticket-sales/ticket-sales.service';

// onModuleInit's own amqp-connection-manager wiring (connect/assertExchange/
// assertQueue/bindQueue) is thin, declarative broker setup with no branching logic of
// its own -- exercised live once real messages flow, per the project's existing
// "loose ends" notes, not unit-tested here. What's actually worth a test is
// handleMessage's routing/ack/nack logic, which is real, private business logic --
// reached via bracket access since it has no public seam of its own.
describe('RabbitMqConsumerService', () => {
  let ticketSalesService: jest.Mocked<
    Pick<TicketSalesService, 'recordSale' | 'recordCancellation'>
  >;
  let service: RabbitMqConsumerService;
  let channel: jest.Mocked<Pick<ConfirmChannel, 'ack' | 'nack'>>;

  function messageFor(routingKey: string, payload: unknown): ConsumeMessage {
    return {
      content: Buffer.from(JSON.stringify(payload)),
      fields: { routingKey },
    } as unknown as ConsumeMessage;
  }

  function handle(message: ConsumeMessage | null) {
    return (
      service as unknown as {
        handleMessage: (
          channel: ConfirmChannel,
          message: ConsumeMessage | null,
        ) => Promise<void>;
      }
    ).handleMessage(channel as unknown as ConfirmChannel, message);
  }

  beforeEach(() => {
    ticketSalesService = {
      recordSale: jest.fn().mockResolvedValue(undefined),
      recordCancellation: jest.fn().mockResolvedValue(undefined),
    };
    channel = { ack: jest.fn(), nack: jest.fn() };
    service = new RabbitMqConsumerService(
      ticketSalesService as unknown as TicketSalesService,
    );
  });

  it('routes ticket.purchased to recordSale and acks on success', async () => {
    const event = { ticketId: 'ticket-1' };
    const message = messageFor('ticket.purchased', event);

    await handle(message);

    expect(ticketSalesService.recordSale).toHaveBeenCalledWith(event);
    expect(ticketSalesService.recordCancellation).not.toHaveBeenCalled();
    expect(channel.ack).toHaveBeenCalledWith(message);
    expect(channel.nack).not.toHaveBeenCalled();
  });

  it('routes ticket.cancelled to recordCancellation and acks on success', async () => {
    const event = {
      ticketId: 'ticket-1',
      cancelledAt: '2026-02-01T00:00:00.000Z',
    };
    const message = messageFor('ticket.cancelled', event);

    await handle(message);

    expect(ticketSalesService.recordCancellation).toHaveBeenCalledWith(event);
    expect(ticketSalesService.recordSale).not.toHaveBeenCalled();
    expect(channel.ack).toHaveBeenCalledWith(message);
  });

  it('nacks without requeue when the payload is not valid JSON', async () => {
    const message = {
      content: Buffer.from('not json'),
      fields: { routingKey: 'ticket.purchased' },
    } as unknown as ConsumeMessage;

    await handle(message);

    expect(ticketSalesService.recordSale).not.toHaveBeenCalled();
    expect(channel.ack).not.toHaveBeenCalled();
    expect(channel.nack).toHaveBeenCalledWith(message, false, false);
  });

  it('nacks without requeue when the service call itself fails', async () => {
    ticketSalesService.recordSale.mockRejectedValueOnce(new Error('db down'));
    const message = messageFor('ticket.purchased', { ticketId: 'ticket-1' });

    await handle(message);

    expect(channel.ack).not.toHaveBeenCalled();
    // false, false = no requeue -- a message that fails once shouldn't loop forever
    // through consume-fail-requeue (see the service's own comment on this).
    expect(channel.nack).toHaveBeenCalledWith(message, false, false);
  });

  it('does nothing for a null message', async () => {
    await handle(null);

    expect(ticketSalesService.recordSale).not.toHaveBeenCalled();
    expect(ticketSalesService.recordCancellation).not.toHaveBeenCalled();
    expect(channel.ack).not.toHaveBeenCalled();
    expect(channel.nack).not.toHaveBeenCalled();
  });
});
