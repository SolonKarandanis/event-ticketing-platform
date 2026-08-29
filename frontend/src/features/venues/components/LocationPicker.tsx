import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIconUrl from 'leaflet/dist/images/marker-icon.png?url'
import markerIcon2xUrl from 'leaflet/dist/images/marker-icon-2x.png?url'
import markerShadowUrl from 'leaflet/dist/images/marker-shadow.png?url'
import { MapContainer, Marker, TileLayer, useMapEvents } from 'react-leaflet'

// Leaflet's default marker icon references relative image paths that resolve fine
// served as static files, but break once Vite bundles/hashes them -- re-pointing at
// the actual bundled asset URLs (via Vite's ?url imports) is the standard fix.
const markerIcon = L.icon({
  iconUrl: markerIconUrl,
  iconRetinaUrl: markerIcon2xUrl,
  shadowUrl: markerShadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
})

// Athens -- a reasonable default center given every venue created so far is in
// Greece, not a claim that it's universally the right default.
const DEFAULT_CENTER: [number, number] = [37.9838, 23.7275]
const DEFAULT_ZOOM = 6
const SELECTED_ZOOM = 14

interface LocationPickerProps {
  latitude: number | undefined
  longitude: number | undefined
  onChange: (latitude: number, longitude: number) => void
}

function ClickHandler({
  onChange,
}: {
  onChange: (latitude: number, longitude: number) => void
}) {
  useMapEvents({
    click(event) {
      onChange(event.latlng.lat, event.latlng.lng)
    },
  })
  return null
}

// A visual, click-to-place-pin alternative to typing decimal latitude/longitude by
// hand -- the two Inputs next to this in VenueForm still work too, for anyone who
// already knows their venue's exact coordinates. MapContainer's center/zoom are
// react-leaflet's initial values only (it doesn't re-pan on prop changes after
// mount), which is fine here: VenueForm's defaultValues are already resolved by the
// time this first renders, so the initial center is never stale.
export function LocationPicker({
  latitude,
  longitude,
  onChange,
}: LocationPickerProps) {
  const hasPosition = latitude !== undefined && longitude !== undefined
  const center: [number, number] = hasPosition
    ? [latitude, longitude]
    : DEFAULT_CENTER

  return (
    <div className="overflow-hidden rounded-md border border-input">
      <MapContainer
        center={center}
        zoom={hasPosition ? SELECTED_ZOOM : DEFAULT_ZOOM}
        style={{ height: '260px', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ClickHandler onChange={onChange} />
        {hasPosition && <Marker position={center} icon={markerIcon} />}
      </MapContainer>
    </div>
  )
}
