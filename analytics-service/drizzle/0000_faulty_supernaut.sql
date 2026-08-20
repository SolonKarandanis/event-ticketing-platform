CREATE TABLE "ticket_sales" (
	"id" bigserial PRIMARY KEY NOT NULL,
	"ticket_id" uuid NOT NULL,
	"ticket_type_id" uuid NOT NULL,
	"event_id" uuid NOT NULL,
	"purchaser_id" uuid NOT NULL,
	"price" double precision NOT NULL,
	"purchased_at" timestamp with time zone NOT NULL,
	"recorded_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "ticket_sales_ticket_id_unique" UNIQUE("ticket_id")
);
