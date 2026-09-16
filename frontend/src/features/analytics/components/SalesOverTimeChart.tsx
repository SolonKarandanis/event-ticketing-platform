// Hand-rolled per the /dataviz skill: single-series line (2px, round join/cap,
// --lagoon-deep -- same accent the per-event bar chart uses for revenue), a
// crosshair+tooltip hover layer (required for line/area, unlike the bare stat tiles
// above this on the dashboard), and a table-view toggle -- the accessible equivalent
// for the daily values this chart can't fully direct-label without turning into 30
// overlapping numbers. No charting library: same "plain divs/SVG" precedent the bar
// chart set, since a single line doesn't need what one would add.
import { useId, useRef, useState } from 'react'
import type { KeyboardEvent, PointerEvent } from 'react'
import type { SalesOverTimePoint } from '../types'

interface SalesOverTimeChartProps {
  data: SalesOverTimePoint[]
}

const WIDTH = 600
const HEIGHT = 200
const PADDING_X = 8
const PADDING_TOP = 12
const PADDING_BOTTOM = 24
const PLOT_HEIGHT = HEIGHT - PADDING_TOP - PADDING_BOTTOM
const PLOT_WIDTH = WIDTH - PADDING_X * 2

function formatDateLabel(date: string): string {
  return new Date(`${date}T00:00:00Z`).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    timeZone: 'UTC',
  })
}

export function SalesOverTimeChart({ data }: SalesOverTimeChartProps) {
  const [activeIndex, setActiveIndex] = useState<number | null>(null)
  const [showTable, setShowTable] = useState(false)
  const svgRef = useRef<SVGSVGElement>(null)
  const tableId = useId()

  if (data.length === 0) return null

  // Floors at 1 for the same reason the per-event bar chart's maxRevenue does -- a
  // brand-new organizer with a flat zero line would otherwise divide by zero.
  const maxRevenue = Math.max(1, ...data.map((point) => point.revenue))
  const stepX = data.length > 1 ? PLOT_WIDTH / (data.length - 1) : 0

  function xFor(index: number) {
    return PADDING_X + index * stepX
  }
  function yFor(revenue: number) {
    return PADDING_TOP + PLOT_HEIGHT - (revenue / maxRevenue) * PLOT_HEIGHT
  }

  const linePath = data
    .map(
      (point, index) =>
        `${index === 0 ? 'M' : 'L'} ${xFor(index)} ${yFor(point.revenue)}`,
    )
    .join(' ')

  function setActiveFromClientX(clientX: number) {
    const svg = svgRef.current
    if (!svg) return
    const rect = svg.getBoundingClientRect()
    if (rect.width === 0) return
    const relativeX = ((clientX - rect.left) / rect.width) * WIDTH
    const index = Math.round((relativeX - PADDING_X) / (stepX || 1))
    setActiveIndex(Math.min(data.length - 1, Math.max(0, index)))
  }

  function handlePointerMove(event: PointerEvent<SVGSVGElement>) {
    setActiveFromClientX(event.clientX)
  }

  function handleKeyDown(event: KeyboardEvent<SVGSVGElement>) {
    // Same details on keyboard focus as on hover (see the skill's interaction spec) --
    // arrow keys move the crosshair one day at a time.
    if (event.key === 'ArrowRight') {
      event.preventDefault()
      setActiveIndex((current) =>
        Math.min(data.length - 1, (current ?? -1) + 1),
      )
    } else if (event.key === 'ArrowLeft') {
      event.preventDefault()
      setActiveIndex((current) => Math.max(0, (current ?? data.length) - 1))
    } else if (event.key === 'Escape') {
      setActiveIndex(null)
    }
  }

  const active = activeIndex !== null ? data[activeIndex] : undefined
  const tooltipLeftPercent =
    activeIndex !== null ? (xFor(activeIndex) / WIDTH) * 100 : 0
  const tooltipOnRightHalf =
    activeIndex !== null && xFor(activeIndex) > WIDTH / 2

  return (
    <div>
      <div className="relative">
        <svg
          ref={svgRef}
          viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
          className="w-full touch-none"
          role="img"
          aria-label={`Daily revenue for the last ${data.length} days`}
          tabIndex={0}
          onPointerMove={handlePointerMove}
          onPointerLeave={() => setActiveIndex(null)}
          onKeyDown={handleKeyDown}
        >
          <line
            x1={PADDING_X}
            y1={PADDING_TOP + PLOT_HEIGHT}
            x2={WIDTH - PADDING_X}
            y2={PADDING_TOP + PLOT_HEIGHT}
            stroke="var(--line)"
            strokeWidth={1}
          />
          <path
            d={linePath}
            fill="none"
            stroke="var(--lagoon-deep)"
            strokeWidth={2}
            strokeLinejoin="round"
            strokeLinecap="round"
          />
          {active && activeIndex !== null && (
            <>
              <line
                x1={xFor(activeIndex)}
                y1={PADDING_TOP}
                x2={xFor(activeIndex)}
                y2={PADDING_TOP + PLOT_HEIGHT}
                stroke="var(--sea-ink-soft)"
                strokeWidth={1}
              />
              <circle
                cx={xFor(activeIndex)}
                cy={yFor(active.revenue)}
                r={4}
                fill="var(--lagoon-deep)"
                stroke="var(--surface-strong)"
                strokeWidth={2}
              />
            </>
          )}
          <text
            x={PADDING_X}
            y={HEIGHT - 6}
            fontSize={10}
            fill="var(--sea-ink-soft)"
          >
            {formatDateLabel(data[0].date)}
          </text>
          <text
            x={WIDTH - PADDING_X}
            y={HEIGHT - 6}
            fontSize={10}
            fill="var(--sea-ink-soft)"
            textAnchor="end"
          >
            {formatDateLabel(data[data.length - 1].date)}
          </text>
        </svg>

        {active && (
          <div
            className="pointer-events-none absolute top-0 z-10 rounded-md border border-(--line) bg-(--popover) px-3 py-2 text-xs whitespace-nowrap text-(--popover-foreground) shadow-md"
            style={{
              left: `${tooltipLeftPercent}%`,
              transform: tooltipOnRightHalf ? 'translateX(-100%)' : undefined,
            }}
          >
            <p className="font-medium">{formatDateLabel(active.date)}</p>
            <p className="tabular-nums">${active.revenue.toFixed(2)} revenue</p>
            <p className="tabular-nums">{active.ticketsSold} sold</p>
          </div>
        )}
      </div>

      <button
        type="button"
        className="mt-2 text-xs text-(--sea-ink-soft) underline"
        aria-expanded={showTable}
        aria-controls={tableId}
        onClick={() => setShowTable((value) => !value)}
      >
        {showTable ? 'Hide table' : 'View as table'}
      </button>

      {showTable && (
        <div
          id={tableId}
          className="mt-2 max-h-48 overflow-y-auto rounded-md border border-(--line)"
        >
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b border-(--line) text-left text-(--sea-ink-soft)">
                <th className="p-2 font-medium">Date</th>
                <th className="p-2 text-right font-medium">Revenue</th>
                <th className="p-2 text-right font-medium">Tickets sold</th>
              </tr>
            </thead>
            <tbody>
              {data.map((point) => (
                <tr
                  key={point.date}
                  className="border-b border-(--line) last:border-0"
                >
                  <td className="p-2">{formatDateLabel(point.date)}</td>
                  <td className="p-2 text-right tabular-nums">
                    ${point.revenue.toFixed(2)}
                  </td>
                  <td className="p-2 text-right tabular-nums">
                    {point.ticketsSold}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
