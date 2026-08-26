import { useWatch } from 'react-hook-form'
import type { Control } from 'react-hook-form'
import { Button } from '#/components/ui/button'
import {
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from '#/components/ui/form'
import { Input } from '#/components/ui/input'
import { Switch } from '#/components/ui/switch'
import { Textarea } from '#/components/ui/textarea'
import type { EventFormValues } from './EventForm'

interface TicketTypeRowProps {
    control: Control<EventFormValues>
    index: number
    canRemove: boolean
    onRemove: () => void
}

// One ticket-type row's fields, split out of EventForm so `limitedQuantity`'s watch
// only re-renders this row -- calling form.watch() directly inside EventForm's
// ticketTypes.map would re-render the whole form on every row's every keystroke.
export function TicketTypeRow({ control, index, canRemove, onRemove }: TicketTypeRowProps) {
    const limitedQuantity = useWatch({ control, name: `ticketTypes.${index}.limitedQuantity` })

    return (
        <div className="island-shell grid gap-4 rounded-xl p-4">
            <div className="grid grid-cols-2 gap-4">
                <FormField
                    control={control}
                    name={`ticketTypes.${index}.name`}
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Name</FormLabel>
                            <FormControl>
                                <Input {...field} />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={control}
                    name={`ticketTypes.${index}.price`}
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Price</FormLabel>
                            <FormControl>
                                <Input inputMode="decimal" {...field} />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
            </div>

            <FormField
                control={control}
                name={`ticketTypes.${index}.description`}
                render={({ field }) => (
                    <FormItem>
                        <FormLabel>Description (optional)</FormLabel>
                        <FormControl>
                            <Textarea {...field} />
                        </FormControl>
                        <FormMessage />
                    </FormItem>
                )}
            />

            <FormField
                control={control}
                name={`ticketTypes.${index}.limitedQuantity`}
                render={({ field }) => (
                    <FormItem className="flex flex-row items-center justify-between">
                        <FormLabel>Limited quantity</FormLabel>
                        <FormControl>
                            <Switch checked={field.value} onCheckedChange={field.onChange} />
                        </FormControl>
                    </FormItem>
                )}
            />

            {limitedQuantity ? (
                <FormField
                    control={control}
                    name={`ticketTypes.${index}.totalAvailable`}
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Total Available</FormLabel>
                            <FormControl>
                                <Input inputMode="numeric" {...field} />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
            ) : null}

            <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={!canRemove}
                onClick={onRemove}
                className="justify-self-start"
            >
                Remove
            </Button>
        </div>
    )
}
