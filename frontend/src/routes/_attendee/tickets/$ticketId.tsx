import { Link, createFileRoute } from '@tanstack/react-router'
import { useTicket, useTicketQrCode } from '#/features/tickets/hooks'
import { TicketStatus } from '#/features/tickets/types'
import { useObjectUrl } from '#/hooks/use-object-url'

export const Route = createFileRoute('/_attendee/tickets/$ticketId')({
  component: TicketDetails,
})

function formatDateTime(value: string | null): string {
  if (!value) {
    return 'TBA'
  }
  return new Date(value).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function TicketDetails() {
  const { ticketId } = Route.useParams()
  const { data: ticket, isPending, isError } = useTicket(ticketId)
  const { data: qrCodeBlob, isPending: isQrPending } = useTicketQrCode(ticketId)
  const qrCodeUrl = useObjectUrl(qrCodeBlob)

  return (
    <main className="page-wrap px-4 py-12">
      <Link to="/tickets" className="nav-link mb-4 inline-block">
        &larr; Back to My Tickets
      </Link>
      {isPending && <p className="text-sm text-(--sea-ink-soft)">Loading ticket...</p>}
      {!isPending && isError && <p className="text-sm text-destructive">Couldn't find this ticket.</p>}

      {!isPending && !isError && (
        <div className="island-shell mx-auto max-w-md rounded-xl p-6 text-center">
          <p className="island-kicker mb-2">
            {ticket.status === TicketStatus.CANCELLED ? 'Cancelled' : 'Ticket'}
          </p>
          <h1 className="display-title mb-1 text-2xl font-bold text-(--sea-ink)">
            {ticket.eventName}
          </h1>
          <p className="text-sm text-(--sea-ink-soft)">
            {formatDateTime(ticket.eventStart)}
            {ticket.eventEnd ? ` - ${formatDateTime(ticket.eventEnd)}` : ''}
          </p>
          <p className="mb-6 text-sm text-(--sea-ink-soft)">
            {ticket.eventVenueName}
          </p>

          {ticket.status === TicketStatus.CANCELLED ? (
            <p className="text-sm text-destructive">
              This ticket has been cancelled and can no longer be used for
              entry.
            </p>
          ) : (
            <>
              {isQrPending && (
                <p className="text-sm text-(--sea-ink-soft)">
                  Loading QR code...
                </p>
              )}
              {!isQrPending && qrCodeUrl && (
                <img
                  src={qrCodeUrl}
                  alt="Ticket QR code"
                  className="mx-auto mb-4 h-48 w-48"
                />
              )}
            </>
          )}

          <div className="mt-4 border-t border-(--line) pt-4 text-left text-sm text-(--sea-ink-soft)">
            {ticket.description && <p className="mb-1">{ticket.description}</p>}
            <p className="mb-1">Price: ${ticket.price.toFixed(2)}</p>
            <p>Reference: {ticket.referenceCode}</p>
          </div>
        </div>
      )}
    </main>
  )
}
