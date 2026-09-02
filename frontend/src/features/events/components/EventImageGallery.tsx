import { useRef } from 'react'
import type { ChangeEvent } from 'react'
import {
  DndContext,
  KeyboardSensor,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import type { DragEndEvent } from '@dnd-kit/core'
import {
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { useFieldArray } from 'react-hook-form'
import type { Control } from 'react-hook-form'
import { toast } from 'sonner'
import { Button } from '#/components/ui/button'
import { MAX_EVENT_IMAGES } from '../forms'
import type { EventFormValues } from '../forms'
import { EventImageRow } from './EventImageRow'

interface EventImageGalleryProps {
  control: Control<EventFormValues>
  // Absent on the create page -- see EventImageRow for why that matters (an
  // existing-image thumbnail fetch needs an eventId, a new one doesn't).
  eventId?: string
  disabled?: boolean
}

// Step 2 of EventForm's stepper (see EventForm.tsx) -- nothing here touches the server.
// A picked file just becomes a new row holding that File directly; a removed existing
// row just disappears from the array. All of it -- new uploads, deletions, the final
// order -- is only ever applied when the form's own submit button is clicked, via
// formValuesToRequest building the images[]/newImageFiles the multipart request needs.
export function EventImageGallery({
  control,
  eventId,
  disabled,
}: EventImageGalleryProps) {
  const images = useFieldArray({ control, name: 'images' })
  const fileInputRef = useRef<HTMLInputElement>(null)

  // PointerSensor covers mouse/touch; KeyboardSensor is what makes the drag handle
  // operable without a pointer at all (arrow keys once it has focus) -- dnd-kit
  // doesn't wire either up automatically, both have to be requested explicitly.
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  )

  const remainingSlots = MAX_EVENT_IMAGES - images.fields.length

  function handleFilesSelected(event: ChangeEvent<HTMLInputElement>) {
    const files = event.target.files
    // Reset immediately, not just on the happy path -- without this, picking the
    // exact same file twice in a row wouldn't fire a second change event at all.
    event.target.value = ''
    if (!files || files.length === 0) {
      return
    }

    const filesToAdd = Array.from(files).slice(0, remainingSlots)
    filesToAdd.forEach((file) => {
      images.append({ file, altText: '' })
    })

    if (files.length > filesToAdd.length) {
      toast.error(
        `Only ${remainingSlots} more image(s) can be added (max ${MAX_EVENT_IMAGES}).`,
      )
    }
  }

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event
    if (!over || active.id === over.id) {
      return
    }
    const oldIndex = images.fields.findIndex((field) => field.id === active.id)
    const newIndex = images.fields.findIndex((field) => field.id === over.id)
    if (oldIndex !== -1 && newIndex !== -1) {
      images.move(oldIndex, newIndex)
    }
  }

  return (
    <div className="grid gap-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-(--sea-ink)">
          Images (optional)
        </h2>
        {disabled ? null : (
          <>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              multiple
              hidden
              onChange={handleFilesSelected}
            />
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={remainingSlots <= 0}
              onClick={() => fileInputRef.current?.click()}
            >
              + Add Image
            </Button>
          </>
        )}
      </div>

      {images.fields.length === 0 ? (
        <p className="text-sm text-(--sea-ink-soft)">No images yet.</p>
      ) : (
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext
            items={images.fields.map((field) => field.id)}
            strategy={verticalListSortingStrategy}
          >
            <div className="grid gap-3">
              {images.fields.map((field, index) => (
                <EventImageRow
                  key={field.id}
                  id={field.id}
                  control={control}
                  index={index}
                  eventId={eventId}
                  onRemove={() => images.remove(index)}
                  disabled={disabled}
                />
              ))}
            </div>
          </SortableContext>
        </DndContext>
      )}
    </div>
  )
}
