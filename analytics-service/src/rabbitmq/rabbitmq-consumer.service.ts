import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import * as amqp from 'amqp-connection-manager';
import { ConfirmChannel, ConsumeMessage } from 'amqplib';
import { TicketSalesService } from '../ticket-sales/ticket-sales.service';
import { TicketPurchasedEvent } from '../ticket-sales/ticket-purchased.event';

const EVENTS_EXCHANGE = 'ticket-platform.events';
const QUEUE_NAME = 'analytics-service.ticket-events';

@Injectable()
export class RabbitMqConsumerService implements OnModuleInit {
  private readonly logger = new Logger(RabbitMqConsumerService.name);

  constructor(private readonly ticketSalesService: TicketSalesService) {}

  onModuleInit() {
    const connection = amqp.connect([
      process.env.RABBITMQ_URL ?? 'amqp://admin:admin@localhost:5672',
    ]);

    connection.createChannel({
      setup: async (channel: ConfirmChannel) => {
        await channel.assertExchange(EVENTS_EXCHANGE, 'topic', {
          durable: true,
        });
        await channel.assertQueue(QUEUE_NAME, { durable: true });
        await channel.bindQueue(
          QUEUE_NAME,
          EVENTS_EXCHANGE,
          'ticket.purchased',
        );

        await channel.consume(
          QUEUE_NAME,
          (message: ConsumeMessage | null) =>
            void this.handleMessage(channel, message),
        );
      },
    });
  }

  private async handleMessage(
    channel: ConfirmChannel,
    message: ConsumeMessage | null,
  ) {
    if (!message) {
      return;
    }

    try {
      const event = JSON.parse(
        message.content.toString(),
      ) as TicketPurchasedEvent;
      await this.ticketSalesService.recordSale(event);
      channel.ack(message);
    } catch (error) {
      this.logger.error('Failed to process ticket.purchased message', error);
      channel.nack(message, false, false);
    }
  }
}
