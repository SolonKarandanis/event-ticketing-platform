import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '#/components/ui/button'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '#/components/ui/form'
import { Input } from '#/components/ui/input'
import { Textarea } from '#/components/ui/textarea'
import { isDecimalOrEmpty, isIntegerOrEmpty } from '#/lib/validation'
import type { CreateVenueRequest, Venue } from '../types'

// Numeric fields stay strings in form state (native inputs hand back strings) and get
// converted to numbers only when building the wire request in onSubmit -- simpler than
// fighting zod's number coercion around an empty optional input.
const venueFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required'),
  addressLine1: z.string().trim().min(1, 'Address line 1 is required'),
  addressLine2: z.string().trim(),
  city: z.string().trim().min(1, 'City is required'),
  postalCode: z.string().trim().min(1, 'Postal code is required'),
  country: z.string().trim().min(1, 'Country is required'),
  capacity: z.string().trim().refine(isIntegerOrEmpty, 'Must be a whole number'),
  latitude: z.string().trim().refine(isDecimalOrEmpty, 'Must be a number'),
  longitude: z.string().trim().refine(isDecimalOrEmpty, 'Must be a number'),
  accessibilityInfo: z.string().trim(),
})

export type VenueFormValues = z.infer<typeof venueFormSchema>

const emptyValues: VenueFormValues = {
  name: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  postalCode: '',
  country: '',
  capacity: '',
  latitude: '',
  longitude: '',
  accessibilityInfo: '',
}

export function venueToFormValues(venue: Venue): VenueFormValues {
  return {
    name: venue.name,
    addressLine1: venue.addressLine1,
    addressLine2: venue.addressLine2 ?? '',
    city: venue.city,
    postalCode: venue.postalCode,
    country: venue.country,
    capacity: venue.capacity?.toString() ?? '',
    latitude: venue.latitude?.toString() ?? '',
    longitude: venue.longitude?.toString() ?? '',
    accessibilityInfo: venue.accessibilityInfo ?? '',
  }
}

export function formValuesToRequest(values: VenueFormValues): CreateVenueRequest {
  return {
    name: values.name,
    addressLine1: values.addressLine1,
    addressLine2: values.addressLine2 || undefined,
    city: values.city,
    postalCode: values.postalCode,
    country: values.country,
    capacity: values.capacity ? Number(values.capacity) : undefined,
    latitude: values.latitude ? Number(values.latitude) : undefined,
    longitude: values.longitude ? Number(values.longitude) : undefined,
    accessibilityInfo: values.accessibilityInfo || undefined,
  }
}

interface VenueFormProps {
  defaultValues?: VenueFormValues
  onSubmit: (values: VenueFormValues) => void
  isSubmitting: boolean
  submitLabel: string
}

export function VenueForm({
  defaultValues,
  onSubmit,
  isSubmitting,
  submitLabel,
}: VenueFormProps) {
  const form = useForm<VenueFormValues>({
    resolver: zodResolver(venueFormSchema),
    defaultValues: defaultValues ?? emptyValues,
  })

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="grid max-w-xl gap-5">
        <FormField
          control={form.control}
          name="name"
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
          control={form.control}
          name="addressLine1"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Address Line 1</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="addressLine2"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Address Line 2 (optional)</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <div className="grid grid-cols-2 gap-5">
          <FormField
            control={form.control}
            name="city"
            render={({ field }) => (
              <FormItem>
                <FormLabel>City</FormLabel>
                <FormControl>
                  <Input {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="postalCode"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Postal Code</FormLabel>
                <FormControl>
                  <Input {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
        <FormField
          control={form.control}
          name="country"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Country</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="capacity"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Capacity (optional)</FormLabel>
              <FormControl>
                <Input inputMode="numeric" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <div className="grid grid-cols-2 gap-5">
          <FormField
            control={form.control}
            name="latitude"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Latitude (optional)</FormLabel>
                <FormControl>
                  <Input inputMode="decimal" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="longitude"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Longitude (optional)</FormLabel>
                <FormControl>
                  <Input inputMode="decimal" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
        <FormField
          control={form.control}
          name="accessibilityInfo"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Accessibility Info (optional)</FormLabel>
              <FormControl>
                <Textarea {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" disabled={isSubmitting} className="justify-self-start cursor-pointer">
          {isSubmitting ? 'Saving...' : submitLabel}
        </Button>
      </form>
    </Form>
  )
}
