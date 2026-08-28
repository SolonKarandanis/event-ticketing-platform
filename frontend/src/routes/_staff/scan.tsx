import { useEffect, useRef, useState } from 'react'
import { createFileRoute } from '@tanstack/react-router'
import QrScanner from 'qr-scanner'
import { Button } from '#/components/ui/button'
import { Input } from '#/components/ui/input'
import { toastErrorMessage } from '#/lib/api-client'
import { useValidateTicket } from '#/features/tickets/hooks'
import {
  TicketValidationMethod,
  TicketValidationStatus,
} from '#/features/tickets/types'

export const Route = createFileRoute('/_staff/scan')({
  component: StaffScan,
})

// Issue #9: full-screen colored result, auto-resumes scanning after this long.
const RESULT_DISPLAY_MS = 2000

type ScanOutcome = 'ADMIT' | 'ALREADY_USED' | 'NOT_FOUND'

interface ScanResultState {
  outcome: ScanOutcome
  message: string
}

const OUTCOME_STYLES: Record<
  ScanOutcome,
  { label: string; className: string }
> = {
  ADMIT: { label: 'ADMIT', className: 'bg-emerald-600' },
  ALREADY_USED: { label: 'ALREADY USED', className: 'bg-amber-600' },
  NOT_FOUND: { label: 'NOT FOUND', className: 'bg-red-700' },
}

function StaffScan() {
  const validateTicket = useValidateTicket()
  const videoRef = useRef<HTMLVideoElement>(null)
  const scannerRef = useRef<QrScanner | null>(null)
  // A ref, not state -- guards the decode callback against firing again for frames
  // scanned while a previous result is still on screen, without forcing a re-render
  // (and without the effect below needing to depend on it).
  const isProcessingRef = useRef(false)

  const [result, setResult] = useState<ScanResultState | null>(null)
  const [cameraError, setCameraError] = useState(false)
  const [manualCode, setManualCode] = useState('')

  async function handleValidate(id: string, method: TicketValidationMethod) {
    if (isProcessingRef.current || !id) {
      return
    }
    isProcessingRef.current = true
    void scannerRef.current?.pause()

    try {
      const response = await validateTicket.mutateAsync({ id, method })
      setResult(
        response.status === TicketValidationStatus.VALID
          ? { outcome: 'ADMIT', message: 'Ticket admitted' }
          : {
              outcome: 'ALREADY_USED',
              message: 'This ticket has already been used',
            },
      )
    } catch (error) {
      // useValidateTicket's own onError already toasts this -- the full-screen result
      // below is what staff are actually meant to react to at a glance.
      setResult({
        outcome: 'NOT_FOUND',
        message: toastErrorMessage(error, 'Ticket not found'),
      })
    }

    window.setTimeout(() => {
      setResult(null)
      setManualCode('')
      isProcessingRef.current = false
      void scannerRef.current?.start()
    }, RESULT_DISPLAY_MS)
  }

  useEffect(() => {
    const video = videoRef.current
    if (!video) {
      return
    }

    const scanner = new QrScanner(
      video,
      (scanResult) =>
        void handleValidate(scanResult.data, TicketValidationMethod.QR_SCAN),
      { highlightScanRegion: true, highlightCodeOutline: true },
    )
    scannerRef.current = scanner

    scanner.start().catch(() => setCameraError(true))

    return () => {
      scanner.destroy()
      scannerRef.current = null
    }
    // Intentionally once -- handleValidate only ever reads refs/setters, which are
    // stable across renders, so it never goes stale inside this scanner instance.
  }, [])

  function handleManualSubmit(event: React.FormEvent) {
    event.preventDefault()
    void handleValidate(manualCode.trim(), TicketValidationMethod.MANUAL)
  }

  // Distinct from isProcessingRef: that ref guards the scan callback synchronously
  // (including the post-result display window), while this drives the manual form's
  // disabled state and can safely lag a render behind.
  const isBusy = result !== null || validateTicket.isPending

  return (
    <main className="page-wrap px-4 py-12">
      <p className="island-kicker mb-2">Staff</p>
      <h1 className="display-title mb-6 text-3xl font-bold text-(--sea-ink)">
        Scan Tickets
      </h1>

      <div className="relative mx-auto aspect-square max-w-md overflow-hidden rounded-xl bg-black">
        {/* No captions track -- this is a live camera feed, not recorded media. */}
        <video ref={videoRef} className="h-full w-full object-cover" />

        {result && (
          <div
            className={`absolute inset-0 flex flex-col items-center justify-center gap-2 text-center text-white ${OUTCOME_STYLES[result.outcome].className}`}
          >
            <p className="text-4xl font-bold">
              {OUTCOME_STYLES[result.outcome].label}
            </p>
            <p className="text-sm">{result.message}</p>
          </div>
        )}
      </div>

      {cameraError && (
        <p className="mx-auto mt-4 max-w-md text-sm text-destructive">
          Couldn't access the camera. Use manual entry below instead.
        </p>
      )}

      <form
        onSubmit={handleManualSubmit}
        className="mx-auto mt-8 flex max-w-md gap-2"
      >
        <Input
          placeholder="Reference code (e.g. XY3P9KRT)"
          value={manualCode}
          onChange={(event) => setManualCode(event.target.value.toUpperCase())}
          disabled={isBusy}
        />
        <Button type="submit" disabled={isBusy || !manualCode.trim()}>
          Check In
        </Button>
      </form>
    </main>
  )
}
