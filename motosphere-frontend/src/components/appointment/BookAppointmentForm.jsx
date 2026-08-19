import { useEffect, useState } from 'react'
import { Form, Button } from 'react-bootstrap'
import * as appointmentApi from '../../api/appointmentApi'
import useToast from '../../hooks/useToast'

export default function BookAppointmentForm({ garageId, vehicles, onBooked, onCancel }) {
  const [form, setForm] = useState({ vehicleId: vehicles[0]?.vehicleId || '', appointmentDate: '', appointmentTime: '', issueDescription: '' })
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

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      await appointmentApi.bookAppointment({ ...form, garageId, vehicleId: Number(form.vehicleId) })
      toast.success('Appointment booked')
      onBooked?.()
    } catch (err) {
      toast.error(err.message)
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
      <div className="row g-2">
        <div className="col-6">
          <Form.Group className="mb-2">
            <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Date</Form.Label>
            <Form.Control type="date" name="appointmentDate" value={form.appointmentDate} onChange={onChange} required />
          </Form.Group>
        </div>
        <div className="col-6">
          <Form.Group className="mb-2">
            <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Time</Form.Label>
            <Form.Control type="time" name="appointmentTime" value={form.appointmentTime} onChange={onChange} required />
          </Form.Group>
        </div>
      </div>
      <Form.Group className="mb-3">
        <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>What's wrong?</Form.Label>
        <Form.Control as="textarea" rows={2} name="issueDescription" value={form.issueDescription} onChange={onChange} placeholder="Describe the issue..." />
      </Form.Group>
      <div className="d-flex gap-2">
        <Button type="submit" variant="light" className="btn-amber" disabled={busy}>{busy ? 'Booking…' : 'Confirm booking'}</Button>
        <Button type="button" variant="outline-secondary" onClick={onCancel}>Cancel</Button>
      </div>
    </Form>
  )
}
