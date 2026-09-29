import { useEffect, useState } from 'react'
import { Form, Button, Spinner } from 'react-bootstrap'
import * as appointmentApi from '../../api/appointmentApi'
import useToast from '../../hooks/useToast'

// Backend sends each slot's start/end as ISO LocalTime strings, e.g.
// "09:00:00" - this just trims the seconds for display ("9:00").
// Kept intentionally simple (no AM/PM) to match the 24h format used
// elsewhere in the app (see garage opening/closing hours).
const formatTime = (isoTime) => isoTime.slice(0, 5)

export default function BookAppointmentForm({ garageId, vehicles, onBooked, onCancel }) {
  const [form, setForm] = useState({ vehicleId: vehicles[0]?.vehicleId || '', appointmentDate: '', issueDescription: '' })
  const [slots, setSlots] = useState([])
  const [selectedSlot, setSelectedSlot] = useState(null)
  const [slotsLoading, setSlotsLoading] = useState(false)
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  // vehicles is fetched asynchronously in the parent and often resolves
  // AFTER this form mounts. useState's default only applies once, so if
  // we mounted with an empty list, form.vehicleId stays stuck at '' even
  // after vehicles arrive - submitting as vehicleId: 0. Keep it in sync.
  useEffect(() => {
    if (!form.vehicleId && vehicles.length > 0) {
      setForm((f) => ({ ...f, vehicleId: vehicles[0].vehicleId }))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vehicles])

  // Re-fetch the slot list every time the chosen date changes. The backend
  // only ever returns slots that currently have a free mechanic - a fully
  // booked slot simply isn't in the array, it's never sent down as
  // "disabled", per the requirement that unavailable slots not be shown.
  useEffect(() => {
    setSelectedSlot(null)
    if (!form.appointmentDate) {
      setSlots([])
      return
    }
    let cancelled = false
    setSlotsLoading(true)
    appointmentApi
      .getAvailableSlots(garageId, form.appointmentDate)
      .then((data) => {
        if (!cancelled) setSlots(data)
      })
      .catch((err) => {
        if (!cancelled) toast.error(err.message)
      })
      .finally(() => {
        if (!cancelled) setSlotsLoading(false)
      })
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [form.appointmentDate, garageId])

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    if (!selectedSlot) {
      toast.error('Please pick a time slot')
      return
    }
    setBusy(true)
    try {
      await appointmentApi.bookAppointment({
        vehicleId: Number(form.vehicleId),
        garageId,
        appointmentDate: form.appointmentDate,
        appointmentTime: selectedSlot.startTime,
        issueDescription: form.issueDescription,
      })
      toast.success('Appointment booked')
      onBooked?.()
    } catch (err) {
      toast.error(err.message)
      // the slot we picked may have just been taken by someone else -
      // refresh the list so the user immediately sees an accurate one
      // instead of retrying against a slot that's already gone
      setSelectedSlot(null)
      appointmentApi.getAvailableSlots(garageId, form.appointmentDate).then(setSlots).catch(() => {})
    } finally {
      setBusy(false)
    }
  }

  if (vehicles.length === 0) {
    return <p className="text-secondary small mb-0">Add a vehicle under "My vehicles" first.</p>
  }

  return (
    <Form onSubmit={onSubmit}>
      <Form.Group className="mb-2">
        <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Vehicle</Form.Label>
        <Form.Select name="vehicleId" value={form.vehicleId} onChange={onChange} required>
          {vehicles.map((v) => (
            <option key={v.vehicleId} value={v.vehicleId}>{v.registrationNumber} — {v.manufacturer}</option>
          ))}
        </Form.Select>
      </Form.Group>

      <Form.Group className="mb-2">
        <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Date</Form.Label>
        <Form.Control type="date" name="appointmentDate" value={form.appointmentDate} onChange={onChange} required />
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Time slot</Form.Label>

        {!form.appointmentDate && (
          <p className="text-secondary small mb-0">Pick a date to see available slots.</p>
        )}

        {form.appointmentDate && slotsLoading && (
          <div className="d-flex align-items-center gap-2 text-secondary small">
            <Spinner animation="border" size="sm" /> Loading slots…
          </div>
        )}

        {form.appointmentDate && !slotsLoading && slots.length === 0 && (
          <p className="text-secondary small mb-0">No slots available on this date — try another day.</p>
        )}

        {form.appointmentDate && !slotsLoading && slots.length > 0 && (
          <Form.Select
            value={selectedSlot?.startTime || ''}
            onChange={(e) => setSelectedSlot(slots.find((s) => s.startTime === e.target.value) || null)}
          >
            <option value="" disabled>Select a time slot</option>
            {slots.map((slot) => (
              <option key={slot.startTime} value={slot.startTime}>
                {formatTime(slot.startTime)} to {formatTime(slot.endTime)}
              </option>
            ))}
          </Form.Select>
        )}
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>What's wrong?</Form.Label>
        <Form.Control as="textarea" rows={2} name="issueDescription" value={form.issueDescription} onChange={onChange} placeholder="Describe the issue..." />
      </Form.Group>

      <div className="d-flex gap-2">
        <Button type="submit" variant="light" className="btn-amber" disabled={busy || !selectedSlot}>{busy ? 'Booking…' : 'Confirm booking'}</Button>
        <Button type="button" variant="outline-secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </Form>
  )
}
