import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PassportModule } from '@nestjs/passport';
import { drizzleProvider } from './db/drizzle.provider';
import { TicketSalesService } from './ticket-sales/ticket-sales.service';
import { EventAnalyticsController } from './ticket-sales/event-analytics.controller';
import { RabbitMqConsumerService } from './rabbitmq/rabbitmq-consumer.service';
import { KeycloakJwtStrategy } from './auth/keycloak-jwt.strategy';

@Module({
  imports: [ConfigModule.forRoot(), PassportModule],
  controllers: [EventAnalyticsController],
  providers: [
    drizzleProvider,
    TicketSalesService,
    RabbitMqConsumerService,
    KeycloakJwtStrategy,
  ],
})
export class AppModule {}
