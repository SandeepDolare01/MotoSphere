import { useEffect, useMemo, useState } from 'react'
import { Card, Button, Row, Col, Form, InputGroup } from 'react-bootstrap'
import * as garageApi from '../../api/garageApi'
import * as vehicleApi from '../../api/vehicleApi'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import BookAppointmentForm from '../../components/appointment/BookAppointmentForm'
import useToast from '../../hooks/useToast'

// Fallback for a garage that hasn't uploaded a real photo yet - stable per
// garage (seeded on garageId), so it doesn't change on every refresh.
function placeholderImageUrl(garageId) {
  return `https://picsum.photos/seed/motosphere-garage-${garageId}/480/280`
}

function GarageImage({ garage }) {
  const src = garage.imageUrl ? garageApi.garageImageUrl(garage.imageUrl) : placeholderImageUrl(garage.garageId)
  return (
    <Card.Img
      variant="top"
      src={src}
      alt={garage.garageName}
      style={{ height: 160, objectFit: 'cover' }}
    />
  )
}

export default function GaragesPage() {
  const [garages, setGarages] = useState(null)
  const [vehicles, setVehicles] = useState([])
  const [bookingGarageId, setBookingGarageId] = useState(null)
  const [query, setQuery] = useState('')
  const toast = useToast()

  const load = () => {
    garageApi.getApprovedGarages().then(setGarages).catch((e) => toast.error(e.message))
    vehicleApi.getMyVehicles().then(setVehicles).catch(() => {})
  }

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  // Single search bar matches against both garage name and address/location -
  // whichever the person typed, it's just a case-insensitive "contains" check
  // across both fields.
  const filteredGarages = useMemo(() => {
    if (!garages) return garages
    const q = query.trim().toLowerCase()
    if (!q) return garages
    return garages.filter((g) => {
      const name = (g.garageName || '').toLowerCase()
      const address = (g.address || '').toLowerCase()
      return name.includes(q) || address.includes(q)
    })
  }, [garages, query])

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">Browse garages</h2>
          <p>Approved garages open for booking.</p>
        </div>
      </div>

      {garages === null ? (
        <LoadingBlock />
      ) : garages.length === 0 ? (
        <EmptyState title="No garages listed yet">Check back soon.</EmptyState>
      ) : (
        <>
          <InputGroup className="mb-3" style={{ maxWidth: 420 }}>
            <InputGroup.Text>🔍</InputGroup.Text>
            <Form.Control
              placeholder="Search by garage name or location…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </InputGroup>

          {filteredGarages.length === 0 ? (
            <EmptyState title="No garages match your search">Try a different name or location.</EmptyState>
          ) : (
            <Row xs={1} md={2} lg={3} className="g-3">
              {filteredGarages.map((g) => (
                <Col key={g.garageId}>
                  <Card className="h-100">
                    <GarageImage garage={g} />
                    <Card.Body>
                      <h3 className="h6">{g.garageName}</h3>
                      <div className="ms-kv"><span className="k">Owner</span><span className="v">{g.ownerName || '—'}</span></div>
                      <div className="ms-kv"><span className="k">Address</span><span className="v">{g.address || '—'}</span></div>
                      <div className="ms-kv"><span className="k">Hours</span><span className="v">{g.openingTime || '—'} – {g.closingTime || '—'}</span></div>
                      <div className="ms-kv"><span className="k">Contact</span><span className="v">{g.contactNumber || '—'}</span></div>
                      <hr />
                      {bookingGarageId === g.garageId ? (
                        <BookAppointmentForm
                          garageId={g.garageId}
                          vehicles={vehicles}
                          onCancel={() => setBookingGarageId(null)}
                          onBooked={() => setBookingGarageId(null)}
                        />
                      ) : (
                        <Button variant="light" className="btn-amber w-100" onClick={() => setBookingGarageId(g.garageId)}>
                          Book appointment
                        </Button>
                      )}
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          )}
        </>
      )}
    </>
  )
}
