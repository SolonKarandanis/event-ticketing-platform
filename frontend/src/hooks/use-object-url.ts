import { useEffect, useState } from 'react'

// Wraps a fetched Blob (a QR code image, a future export, etc.) in an object URL
// usable directly as an <img src>/download href. Revokes the previous URL whenever
// the blob changes or the component unmounts, so it doesn't leak.
export function useObjectUrl(blob: Blob | undefined): string | undefined {
  const [url, setUrl] = useState<string | undefined>(undefined)

  useEffect(() => {
    if (!blob) {
      setUrl(undefined)
      return
    }

    const objectUrl = URL.createObjectURL(blob)
    setUrl(objectUrl)

    return () => {
      URL.revokeObjectURL(objectUrl)
    }
  }, [blob])

  return url
}
