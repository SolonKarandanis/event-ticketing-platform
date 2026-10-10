-- USING clause converts existing major-units values to minor units (ROUND(price * 100))
-- instead of drizzle-kit's naive generated cast, which would truncate 25.0 -> 25 rather
-- than converting it to 2500. Same reasoning as ticket-service's own #23 migration.
ALTER TABLE "ticket_sales" ALTER COLUMN "price" SET DATA TYPE integer USING ROUND(price * 100)::integer;--> statement-breakpoint
ALTER TABLE "ticket_sales" ADD COLUMN "order_id" uuid;--> statement-breakpoint
-- Added nullable, backfilled, then constrained NOT NULL -- a plain NOT NULL ADD COLUMN
-- (drizzle-kit's naive generated statement) would fail outright against any existing
-- row, same multi-step precedent as ticket-service's #23 migration.
ALTER TABLE "ticket_sales" ADD COLUMN "currency" varchar(3);--> statement-breakpoint
UPDATE "ticket_sales" SET "currency" = 'usd';--> statement-breakpoint
ALTER TABLE "ticket_sales" ALTER COLUMN "currency" SET NOT NULL;
