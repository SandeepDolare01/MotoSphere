import { useEffect, useState } from 'react'
import { Card, Form, Button } from 'react-bootstrap'
import * as appointmentApi from '../../api/appointmentApi'
import * as garageApi from '../../api/garageApi'
import StatusBadge from '../../components/common/StatusBadge'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

function AssignMechanicForm({ appointmentId, mechanics, onAssigned }) {
  const [mechanicId, setMechanicId] = useState(mechanics[0]?.userId || '')
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  // The mechanics list often finishes loading AFTER this form's initial
  // mount (it's fetched in a separate, unawaited call in the parent).
  // useState's default only applies once, so if we mounted with an empty
  // list, mechanicId gets stuck at '' even after mechanics arrive - which
  // then submits as mechanicId: 0 and the backend rejects it. Keep the
  // selection in sync whenever the mechanics list changes.
  useEffect(() => {
    if (!mechanicId && mechanics.length > 0) {
      setMechanicId(mechanics[0].userId)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mechanics])

  const onSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      await appointmentApi.assignMechanic(appointmentId, { mechanicId: Number(mechanicId) })
      toast.success('Mechanic assigned')
      onAssigned?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (mechanics.length === 0) {
    return <p className="text-secondary small mb-0">No active mechanics to assign — add one under "My mechanics".</p>
  }

  return (
    <Form onSubmit={onSubmit} className="d-flex gap-2 align-items-end flex-wrap">
      <Form.Group className="mb-0" style={{ minWidth: 200 }}>
        <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Assign mechanic</Form.Label>
        <Form.Select value={mechanicId} onChange={(e) => setMechanicId(e.target.value)}>
          {mechanics.map((m) => <option key={m.userId} value={m.userId}>{m.firstName} {m.lastName || ''}</option>)}
        </Form.Select>
      </Form.Group>
      <Button type="submit" variant="light" className="btn-amber" disabled={busy}>Assign</Button>
    </Form>
  )
}

export default function GarageQueuePage() {
  const [appointments, setAppointments] = useState(null)
  const [mechanics, setMechanics] = useState([])
  const toast = useToast()

  const load = () => {
    appointmentApi.getGarageAppointments().then(setAppointments).catch((e) => toast.error(e.message))
    garageApi.getMyGarageMechanics().then((list) => setMechanics(list.filter((m) => m.active))).catch(() => {})
  }

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">Garage queue</h2>
          <p>Every appointment booked at your garage.</p>
        </div>
      </div>

      {appointments === null ? (
        <LoadingBlock />
      ) : appointments.length === 0 ? (
        <EmptyState title="No appointments yet">They'll show up here once customers start booking.</EmptyState>
      ) : (
        appointments.map((a) => (
          <Card key={a.appointmentId} className="mb-3">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-start flex-wrap gap-2">
                <div>
                  <h3 className="h6 mb-1">{a.vehicleRegistrationNumber}</h3>
                  <div className="text-secondary" style={{ fontSize: '.78rem' }}>
                    {a.appointmentDate} at {a.appointmentTime}{a.mechanicName && <> · {a.mechanicName}</>}
                  </div>
                </div>
                <StatusBadge status={a.status} />
              </div>
              {a.issueDescription && <p className="mt-2 mb-0 text-secondary" style={{ fontSize: '.83rem' }}>{a.issueDescription}</p>}
              {a.status === 'BOOKED' && (
                <div className="ms-subcard mt-3">
                  <AssignMechanicForm appointmentId={a.appointmentId} mechanics={mechanics} onAssigned={load} />
                </div>
              )}
            </Card.Body>
          </Card>
        ))
      )}
    </>
  )
}
