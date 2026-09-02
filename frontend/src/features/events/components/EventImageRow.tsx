import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVerticalIcon, XIcon } from 'lucide-react'
import { useWatch } from 'react-hook-form'
import type { Control } from 'react-hook-form'
import { Button } from '#/components/ui/button'
import {
  FormControl,
  FormField,
  FormItem,
  FormMessage,
} from '#/components/ui/form'
import { Input } from '#/components/ui/input'
import { useEventImage } from '#/features/events/hooks'
import { useObjectUrl } from '#/hooks/use-object-url'
import type { EventFormValues } from '../forms'

interface EventImageRowProps {
  // useFieldArray's own generated field.id, not a domain id -- stable across
  // reorders/removals, which is exactly what both React's key prop and dnd-kit's
  // sortable id need. Passed down rather than re-derived here since the parent
  // (EventImageGallery) already has it from fields.map.
  id: string
  control: Control<EventFormValues>
  index: number
  // Absent on the create page (nothing exists yet to fetch); present on the edit
  // page, where an existing image's thumbnail needs an authenticated fetch (see
  // useEventImage -- it works on a still-DRAFT event, unlike a public <img src>).
  eventId?: string
  onRemove: () => void
  disabled?: boolean
}

// One image's row in the gallery -- drag handle, thumbnail, an optional alt-text
// caption, and Remove. Watches its own row's leaf fields rather than reading
// useFieldArray's fields[index] directly, the same reactivity precedent TicketTypeRow
// already set for `limitedQuantity`.
export function EventImageRow({
  id,
  control,
  index,
  eventId,
  onRemove,
  disabled,
}: EventImageRowProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id,
    disabled,
  })

  // A brand-new image's bytes are already in hand (the picked File itself, held in
  // form state); an existing one has to be fetched by id. Exactly one of the two is
  // ever set for a given row -- see eventImageFormSchema.
  const file = useWatch({ control, name: `images.${index}.file` })
  const existingImageId = useWatch({ control, name: `images.${index}.id` })

  const { data: fetchedBlob } = useEventImage(
    eventId ?? '',
    existingImageId ?? '',
  )
  const thumbnailUrl = useObjectUrl(file ?? fetchedBlob)

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="island-shell flex items-center gap-3 rounded-xl p-3"
    >
      {disabled ? null : (
        <button
          type="button"
          className="cursor-grab touch-none text-(--sea-ink-soft)"
          aria-label="Drag to reorder"
          {...attributes}
          {...listeners}
        >
          <GripVerticalIcon className="size-5" />
        </button>
      )}

      {thumbnailUrl ? (
        <img
          src={thumbnailUrl}
          alt=""
          className="size-16 shrink-0 rounded-md object-cover"
        />
      ) : (
        <div className="size-16 shrink-0 rounded-md bg-(--line)" aria-hidden />
      )}

      <FormField
        control={control}
        name={`images.${index}.altText`}
        render={({ field }) => (
          <FormItem className="flex-1">
            <FormControl>
              <Input
                placeholder="Describe this image (optional)"
                disabled={disabled}
                {...field}
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      {disabled ? null : (
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={onRemove}
          aria-label="Remove image"
        >
          <XIcon className="size-4" />
        </Button>
      )}
    </div>
  )
}
