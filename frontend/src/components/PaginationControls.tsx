import { Link, useNavigate } from '@tanstack/react-router'
import { buttonVariants } from '#/components/ui/button'
import { Pagination, PaginationContent, PaginationItem } from '#/components/ui/pagination'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '#/components/ui/select'
import { DEFAULT_PAGE, PAGE_SIZES } from '#/lib/pagination'
import type { PageSize } from '#/lib/pagination'

interface PaginationControlsProps {
  // 1-based, matching the page/size search params every paginated list route shares.
  page: number
  size: PageSize
  totalPages: number
  totalElements: number
  // Singular noun for the count label ("venue" -> "3 venues") -- plain "s" pluralization
  // is enough for now, matching every other list screen's copy in this app.
  itemLabel: string
}

// Shared by every paginated list route (venues today, events/tickets later): a page-size
// Select plus Previous/Next controls, driven entirely by the page/size search params
// from #/lib/pagination. Reads/writes the current route's search via `to="."`, so it
// works unmodified wherever it's rendered.
export function PaginationControls({
  page,
  size,
  totalPages,
  totalElements,
  itemLabel,
}: PaginationControlsProps) {
  const navigate = useNavigate()
  const hasPrevious = page > 1
  const hasNext = page < totalPages

  function changeSize(nextSize: PageSize) {
    // Changing the page size while deep in the list can land past the new last page --
    // resetting to page 1 sidesteps that instead of showing an empty page.
    void navigate({
      to: '.',
      search: (prev) => ({ ...prev, page: DEFAULT_PAGE, size: nextSize }),
      viewTransition: true,
    })
  }

  return (
    <div className="mt-6 flex items-center justify-between gap-4">
      <div className="flex items-center gap-2 text-sm text-(--sea-ink-soft)">
        <span>Rows per page</span>
        <Select
          value={String(size)}
          onValueChange={(value) => changeSize(Number(value) as PageSize)}
        >
          <SelectTrigger size="sm" className="w-20">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {PAGE_SIZES.map((pageSize) => (
              <SelectItem key={pageSize} value={String(pageSize)}>
                {pageSize}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <span>
          · {totalElements} {itemLabel}
          {totalElements === 1 ? '' : 's'}
        </span>
      </div>

      <Pagination className="mx-0 w-auto">
        <PaginationContent>
          <PaginationItem>
            {hasPrevious ? (
              <Link
                to="."
                search={(prev) => ({ ...prev, page: page - 1 })}
                viewTransition
                className={buttonVariants({ variant: 'ghost', size: 'default' })}
              >
                Previous
              </Link>
            ) : (
              <span
                aria-disabled
                className={buttonVariants({
                  variant: 'ghost',
                  size: 'default',
                  className: 'pointer-events-none opacity-50',
                })}
              >
                Previous
              </span>
            )}
          </PaginationItem>
          <PaginationItem>
            <span className="px-2 text-sm text-(--sea-ink-soft)">
              Page {page} of {Math.max(totalPages, 1)}
            </span>
          </PaginationItem>
          <PaginationItem>
            {hasNext ? (
              <Link
                to="."
                search={(prev) => ({ ...prev, page: page + 1 })}
                viewTransition
                className={buttonVariants({ variant: 'ghost', size: 'default' })}
              >
                Next
              </Link>
            ) : (
              <span
                aria-disabled
                className={buttonVariants({
                  variant: 'ghost',
                  size: 'default',
                  className: 'pointer-events-none opacity-50',
                })}
              >
                Next
              </span>
            )}
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    </div>
  )
}
