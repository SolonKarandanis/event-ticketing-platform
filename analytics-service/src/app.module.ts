import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PassportModule } from '@nestjs/passport';
import { drizzleProvider } from './db/drizzle.provider';
import { TicketSalesService } from './ticket-sales/ticket-sales.service';
import { EventAnalyticsController } from './ticket-sales/event-analytics.controller';
import { OrganizerAnalyticsController } from './ticket-sales/organizer-analytics.controller';
import { RabbitMqConsumerService } from './rabbitmq/rabbitmq-consumer.service';
import { KeycloakJwtStrategy } from './auth/keycloak-jwt.strategy';
import { HealthController } from './health/health.controller';
import { MetricsModule } from './metrics/metrics.module';

@Module({
  imports: [ConfigModule.forRoot(), PassportModule, MetricsModule],
  controllers: [
    EventAnalyticsController,
    OrganizerAnalyticsController,
    HealthController,
  ],
  providers: [
    drizzleProvider,
    TicketSalesService,
    RabbitMqConsumerService,
    KeycloakJwtStrategy,
  ],
})
export class AppModule {}
