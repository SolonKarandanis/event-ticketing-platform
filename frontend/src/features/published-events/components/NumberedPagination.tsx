import { Link } from '@tanstack/react-router'
import { ChevronLeftIcon, ChevronRightIcon } from 'lucide-react'
import { buttonVariants } from '#/components/ui/button'
import { Pagination, PaginationContent, PaginationEllipsis, PaginationItem } from '#/components/ui/pagination'

interface NumberedPaginationProps {
    page: number
    totalPages: number
}

function pageNumbers(current: number, total: number): (number | 'ellipsis')[] {
    if (total <= 7) {
        return Array.from({ length: total }, (_, i) => i + 1)
    }

    const pages: (number | 'ellipsis')[] = [1]
    if (current > 3) pages.push('ellipsis')

    const start = Math.max(2, current - 1)
    const end = Math.min(total - 1, current + 1)
    for (let page = start; page <= end; page++) {
        pages.push(page)
    }

    if (current < total - 2) pages.push('ellipsis')
    pages.push(total)
    return pages
}

// Distinct from #/components/PaginationControls (Previous/Next + page-size select, for
// the organizer CRUD lists) -- issue #10 specifically decided numbered pagination here
// instead, matching the endpoint's Page-based response. Doesn't use ui/pagination's own
// PaginationLink/Previous/Next: those render plain <a> tags with no to/search support,
// same reason PaginationControls builds its own Link-based buttons rather than using
// them directly. Reads/writes the current route's `page` search param via `to="."`.
export function NumberedPagination({ page, totalPages }: NumberedPaginationProps) {
    if (totalPages <= 1) {
        return null
    }

    return (
        <Pagination>
            <PaginationContent>
                <PaginationItem>
                    {page > 1 ? (
                        <Link
                            to="."
                            search={(prev) => ({ ...prev, page: page - 1 })}
                            aria-label="Go to previous page"
                            className={buttonVariants({ variant: 'ghost', size: 'icon' })}
                        >
                            <ChevronLeftIcon />
                        </Link>
                    ) : (
                        <span
                            aria-disabled
                            className={buttonVariants({
                                variant: 'ghost',
                                size: 'icon',
                                className: 'pointer-events-none opacity-50',
                            })}
                        >
                            <ChevronLeftIcon />
                        </span>
                    )}
                </PaginationItem>

                {pageNumbers(page, totalPages).map((entry, index) =>
                    entry === 'ellipsis' ? (
                        // Index as key is fine: a fixed-shape placeholder list, never reordered.
                        <PaginationItem key={`ellipsis-${index}`}>
                            <PaginationEllipsis />
                        </PaginationItem>
                    ) : (
                        <PaginationItem key={entry}>
                            <Link
                                to="."
                                search={(prev) => ({ ...prev, page: entry })}
                                aria-current={entry === page ? 'page' : undefined}
                                className={buttonVariants({
                                    variant: entry === page ? 'outline' : 'ghost',
                                    size: 'icon',
                                })}
                            >
                                {entry}
                            </Link>
                        </PaginationItem>
                    ),
                )}

                <PaginationItem>
                    {page < totalPages ? (
                        <Link
                            to="."
                            search={(prev) => ({ ...prev, page: page + 1 })}
                            aria-label="Go to next page"
                            className={buttonVariants({ variant: 'ghost', size: 'icon' })}
                        >
                            <ChevronRightIcon />
                        </Link>
                    ) : (
                        <span
                            aria-disabled
                            className={buttonVariants({
                                variant: 'ghost',
                                size: 'icon',
                                className: 'pointer-events-none opacity-50',
                            })}
                        >
                            <ChevronRightIcon />
                        </span>
                    )}
                </PaginationItem>
            </PaginationContent>
        </Pagination>
    )
}
