import { useEffect, useState } from 'react'
import { Form, Button, Spinner } from 'react-bootstrap'
import * as appointmentApi from '../../api/appointmentApi'
import useToast from '../../hooks/useToast'

// Backend sends each slot's start/end as ISO LocalTime strings, e.g.
// "09:00:00" - this just trims the seconds for display ("9:00").
const formatTime = (isoTime) => isoTime.slice(0, 5)

export default function BookAppointmentForm({ garageId, vehicles, onBooked, onCancel }) {
  const [form, setForm] = useState({ vehicleId: vehicles[0]?.vehicleId || '', appointmentDate: '', appointmentTime: '', issueDescription: '' })
  const [slots, setSlots] = useState([])
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
  // only returns slots that currently have a free mechanic across the
  // whole garage - if every mechanic is already booked into a given
  // 30-min window, that slot is simply absent from the array, never sent
  // down as a disabled option.
  useEffect(() => {
    setForm((f) => ({ ...f, appointmentTime: '' }))
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
    if (!form.appointmentTime) {
      toast.error('Please pick a time slot')
      return
    }
    setBusy(true)
    try {
      await appointmentApi.bookAppointment({
        vehicleId: Number(form.vehicleId),
        garageId,
        appointmentDate: form.appointmentDate,
        appointmentTime: form.appointmentTime,
        issueDescription: form.issueDescription,
      })
      toast.success('Appointment booked')
      onBooked?.()
    } catch (err) {
      toast.error(err.message)
      // the slot we picked may have just been taken by someone else -
      // refresh the list so the dropdown immediately reflects reality
      setForm((f) => ({ ...f, appointmentTime: '' }))
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

        {form.appointmentDate && slotsLoading && (
          <div className="d-flex align-items-center gap-2 text-secondary small mb-1">
            <Spinner animation="border" size="sm" /> Loading slots…
          </div>
        )}

        <Form.Select
          name="appointmentTime"
          value={form.appointmentTime}
          onChange={onChange}
          required
          disabled={!form.appointmentDate || slotsLoading || slots.length === 0}
        >
          {!form.appointmentDate && <option value="">Pick a date first</option>}

          {form.appointmentDate && slotsLoading && <option value="">Loading…</option>}

          {form.appointmentDate && !slotsLoading && slots.length === 0 && (
            <option value="">No slots available on this date</option>
          )}

          {form.appointmentDate && !slotsLoading && slots.length > 0 && (
            <>
              <option value="">Select a time slot</option>
              {slots.map((slot) => (
                <option key={slot.startTime} value={slot.startTime}>
                  {formatTime(slot.startTime)} to {formatTime(slot.endTime)}
                </option>
              ))}
            </>
          )}
        </Form.Select>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>What's wrong?</Form.Label>
        <Form.Control as="textarea" rows={2} name="issueDescription" value={form.issueDescription} onChange={onChange} placeholder="Describe the issue..." />
      </Form.Group>

      <div className="d-flex gap-2">
        <Button type="submit" variant="light" className="btn-amber" disabled={busy || !form.appointmentTime}>{busy ? 'Booking…' : 'Confirm booking'}</Button>
        <Button type="button" variant="outline-secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </Form>
  )
}
