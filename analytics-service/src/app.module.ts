import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { drizzleProvider } from './db/drizzle.provider';
import { TicketSalesService } from './ticket-sales/ticket-sales.service';
import { RabbitMqConsumerService } from './rabbitmq/rabbitmq-consumer.service';

@Module({
  imports: [ConfigModule.forRoot()],
  providers: [drizzleProvider, TicketSalesService, RabbitMqConsumerService],
})
export class AppModule {}
