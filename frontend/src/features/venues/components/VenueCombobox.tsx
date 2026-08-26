import { useState } from 'react'
import type { UIEvent } from 'react'
import { CheckIcon, ChevronsUpDownIcon } from 'lucide-react'
import { Link } from '@tanstack/react-router'
import { Button } from '#/components/ui/button'
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from '#/components/ui/command'
import { Popover, PopoverContent, PopoverTrigger } from '#/components/ui/popover'
import { useDebouncedValue } from '#/hooks/use-debounced-value'
import { cn } from '#/lib/utils'
import { useInfiniteVenues, useVenue } from '../hooks'

interface VenueComboboxProps {
    value: string
    onChange: (venueId: string) => void
    disabled?: boolean
}

// Search-as-you-type + infinite scroll instead of a plain <Select> loading a single
// page -- a fixed page size silently cuts off any venue past it (see the venues list's
// own history before it had real pagination). shouldFilter={false} on Command turns off
// cmdk's own client-side filtering: filtering is the server's job here, driven by the
// debounced search term in the query key.
export function VenueCombobox({ value, onChange, disabled }: VenueComboboxProps) {
    const [open, setOpen] = useState(false)
    const [search, setSearch] = useState('')
    const debouncedSearch = useDebouncedValue(search, 300)

    const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isPending } =
        useInfiniteVenues(debouncedSearch)
    const venues = data?.pages.flatMap((page) => page.content) ?? []

    // The selected venue may not be in the current (possibly filtered) result set at
    // all, so its display name is fetched independently by id.
    const { data: selectedVenue } = useVenue(value)

    function handleScroll(event: UIEvent<HTMLDivElement>) {
        const target = event.currentTarget
        const nearBottom = target.scrollHeight - target.scrollTop - target.clientHeight < 48
        if (nearBottom && hasNextPage && !isFetchingNextPage) {
            void fetchNextPage()
        }
    }

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    type="button"
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    disabled={disabled}
                    className="w-full justify-between font-normal"
                >
                    {value ? (selectedVenue?.name ?? 'Loading...') : 'Select a venue'}
                    <ChevronsUpDownIcon className="opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-(--radix-popover-trigger-width) p-0">
                <Command shouldFilter={false}>
                    <CommandInput
                        placeholder="Search venues..."
                        value={search}
                        onValueChange={setSearch}
                    />
                    <CommandList onScroll={handleScroll}>
                        {isPending ? (
                            <div className="p-4 text-sm text-(--sea-ink-soft)">Loading...</div>
                        ) : (
                            <>
                                <CommandEmpty>
                                    {venues.length === 0 && !search ? (
                                        <span>
                                            No venues yet.{' '}
                                            <Link
                                                to="/venues/new"
                                                className="underline"
                                                onClick={() => setOpen(false)}
                                            >
                                                Create one first
                                            </Link>
                                            .
                                        </span>
                                    ) : (
                                        'No venues found.'
                                    )}
                                </CommandEmpty>
                                <CommandGroup>
                                    {venues.map((venue) => (
                                        <CommandItem
                                            key={venue.id}
                                            value={venue.id}
                                            onSelect={() => {
                                                onChange(venue.id)
                                                setOpen(false)
                                            }}
                                        >
                                            <CheckIcon
                                                className={cn(
                                                    'size-4',
                                                    value === venue.id ? 'opacity-100' : 'opacity-0',
                                                )}
                                            />
                                            {venue.name}
                                        </CommandItem>
                                    ))}
                                </CommandGroup>
                                {isFetchingNextPage ? (
                                    <div className="p-2 text-center text-xs text-(--sea-ink-soft)">
                                        Loading more...
                                    </div>
                                ) : null}
                            </>
                        )}
                    </CommandList>
                </Command>
            </PopoverContent>
        </Popover>
    )
}
