// Shared Zod .refine() predicates for numeric fields that stay strings in form state
// (native inputs always hand back strings) and only convert to real numbers at submit
// time -- see VenueForm for why: z.coerce.number() turns an empty string into 0, not
// undefined, which would treat "left blank" as "typed zero" for an optional field.

const INTEGER_PATTERN = /^\d+$/
const DECIMAL_PATTERN = /^-?\d+(\.\d+)?$/
const NON_NEGATIVE_DECIMAL_PATTERN = /^\d+(\.\d+)?$/

// A whole, non-negative number as a string (e.g. capacity, totalAvailable), or blank
// for an optional field that hasn't been set.
export function isIntegerOrEmpty(value: string): boolean {
  return value === '' || INTEGER_PATTERN.test(value)
}

// A signed, optionally-decimal number as a string (e.g. latitude/longitude), or blank
// for an optional field that hasn't been set.
export function isDecimalOrEmpty(value: string): boolean {
  return value === '' || DECIMAL_PATTERN.test(value)
}

// A required, non-negative decimal as a string (e.g. ticket type price) -- unlike
// isDecimalOrEmpty, blank isn't valid here and neither is a leading minus sign.
export function isNonNegativeDecimal(value: string): boolean {
  return NON_NEGATIVE_DECIMAL_PATTERN.test(value)
}
