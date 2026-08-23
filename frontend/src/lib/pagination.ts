import { z } from 'zod'

// Shared across every paginated list route (venues today, events/tickets later) --
// not feature-specific.
export const PAGE_SIZES = [10, 20, 50, 100] as const
export type PageSize = (typeof PAGE_SIZES)[number]

export const DEFAULT_PAGE = 1
export const DEFAULT_SIZE: PageSize = 10

// Drop straight into any route's validateSearch. 1-based in the URL/UI -- translate to
// the backend's 0-based Pageable when calling the feature's list hook.
//
// Both fields stay optional (not defaulted via .catch()) so a Link to a paginated route
// from elsewhere doesn't have to supply page/size itself; the route applies
// DEFAULT_PAGE/DEFAULT_SIZE when reading. .catch(undefined) still recovers a malformed
// value (e.g. ?page=abc) to "absent" instead of leaving validateSearch throwing.
export const paginationSearchSchema = z.object({
  page: z.coerce.number().int().min(1).optional().catch(undefined),
  size: z.coerce
    .number()
    .refine((value): value is PageSize => (PAGE_SIZES as readonly number[]).includes(value))
    .optional()
    .catch(undefined),
})

export type PaginationSearch = z.infer<typeof paginationSearchSchema>
