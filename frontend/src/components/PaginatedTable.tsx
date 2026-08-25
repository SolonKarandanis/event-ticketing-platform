import type { ReactNode } from 'react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '#/components/ui/table'
import { Skeleton } from '#/components/ui/skeleton'
import { PaginationControls } from '#/components/PaginationControls'
import { useMinimumDuration } from '#/hooks/use-minimum-duration'
import type { PageSize } from '#/lib/pagination'

// Cycled per column so skeleton rows don't read as one uniform grid.
const SKELETON_CELL_WIDTHS = ['w-3/4', 'w-1/2', 'w-2/3', 'w-1/3', 'w-full']

// Loading caps out at a handful of rows regardless of page size -- a size=100 page
// doesn't need 100 shimmering placeholders to communicate "this is loading".
const MAX_SKELETON_ROWS = 8

// A cached/fast response can resolve in well under a frame, which reads as a flicker
// rather than a loading state -- holding the skeleton for at least this long makes it
// register as deliberate instead.
const MIN_SKELETON_DURATION_MS = 400

export interface PaginatedTableColumn<T> {
  header: string
  cell: (item: T) => ReactNode
  // e.g. 'text-right' for an Actions column -- applied to both the header cell and
  // every row's cell for that column.
  className?: string
}

interface PaginatedTableProps<T> {
  items: T[]
  isPending: boolean
  isError: boolean
  // 1-based, matching the page/size search params every paginated list route shares.
  page: number
  size: PageSize
  totalPages: number
  totalElements: number
  itemLabel: string
  columns: PaginatedTableColumn<T>[]
  getRowKey: (item: T) => string
  emptyMessage: ReactNode
  // Visually hidden (screen readers only) while the skeleton renders -- the skeleton
  // itself is purely decorative, so this is the only thing announcing "loading" to
  // assistive tech.
  loadingMessage?: string
  errorMessage?: ReactNode
}

function ColumnHeaderRow<T>({ columns }: { columns: PaginatedTableColumn<T>[] }) {
  return (
    <TableRow>
      {columns.map((column) => (
        <TableHead key={column.header} className={column.className}>
          {column.header}
        </TableHead>
      ))}
    </TableRow>
  )
}

// Shared by every paginated list route (venues today, events/tickets later): the
// loading/error/empty/table/pagination chain, which drifted out of sync between
// hand-copied versions of it (mismatched empty-state conditions, a missing Actions
// cell). A route only ever needs to supply its data and column definitions -- this is
// the one place the chain itself can go wrong.
export function PaginatedTable<T>({
  items,
  isPending,
  isError,
  page,
  size,
  totalPages,
  totalElements,
  itemLabel,
  columns,
  getRowKey,
  emptyMessage,
  loadingMessage = 'Loading...',
  errorMessage = 'Something went wrong. Try refreshing.',
}: PaginatedTableProps<T>) {
  const showSkeleton = useMinimumDuration(isPending, MIN_SKELETON_DURATION_MS)

  if (showSkeleton) {
    const skeletonRowCount = Math.min(size, MAX_SKELETON_ROWS)
    return (
      <div className="island-shell rounded-xl" role="status">
        <span className="sr-only">{loadingMessage}</span>
        <Table>
          <TableHeader>
            <ColumnHeaderRow columns={columns} />
          </TableHeader>
          <TableBody>
            {Array.from({ length: skeletonRowCount }).map((_, rowIndex) => (
              // Index as key is fine here: a fixed-count list of placeholder rows with
              // no real identity, never reordered.
              <TableRow key={rowIndex} aria-hidden="true">
                {columns.map((column, columnIndex) => (
                  <TableCell key={column.header} className={column.className}>
                    <Skeleton
                      className={`h-4 ${SKELETON_CELL_WIDTHS[columnIndex % SKELETON_CELL_WIDTHS.length]}`}
                    />
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    )
  }

  if (isError) {
    return <p className="text-sm text-destructive">{errorMessage}</p>
  }

  if (items.length === 0 && page === 1) {
    return <p className="text-sm text-(--sea-ink-soft)">{emptyMessage}</p>
  }

  return (
    <>
      <div className="island-shell rounded-xl">
        <Table>
          <TableHeader>
            <ColumnHeaderRow columns={columns} />
          </TableHeader>
          <TableBody>
            {items.map((item) => (
              <TableRow key={getRowKey(item)}>
                {columns.map((column) => (
                  <TableCell key={column.header} className={column.className}>
                    {column.cell(item)}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <PaginationControls
        page={page}
        size={size}
        totalPages={totalPages}
        totalElements={totalElements}
        itemLabel={itemLabel}
      />
    </>
  )
}
