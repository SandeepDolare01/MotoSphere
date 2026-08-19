import { useEffect, useState } from 'react'
import { Card, Button } from 'react-bootstrap'
import * as appointmentApi from '../../api/appointmentApi'
import * as jobCardApi from '../../api/jobCardApi'
import StatusBadge from '../../components/common/StatusBadge'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import CreateJobCardForm from '../../components/jobcard/CreateJobCardForm'
import MechanicJobCardWork from '../../components/jobcard/MechanicJobCardWork'
import useToast from '../../hooks/useToast'

function MechanicAppointmentCard({ appointment, onChanged }) {
  const [expanded, setExpanded] = useState(false)
  const [jobCard, setJobCard] = useState(undefined)

  const loadJobCard = async () => {
    try {
      const jc = await jobCardApi.getJobCardByAppointmentId(appointment.appointmentId)
      setJobCard(jc)
    } catch {
      setJobCard(null)
    }
  }

  const toggle = async () => {
    if (expanded) { setExpanded(false); return }
    setExpanded(true)
    if (jobCard === undefined) await loadJobCard()
  }

  return (
    <Card className="mb-3">
      <Card.Body>
        <div className="d-flex justify-content-between align-items-start flex-wrap gap-2">
          <div>
            <h3 className="h6 mb-1">{appointment.vehicleRegistrationNumber}</h3>
            <div className="text-secondary" style={{ fontSize: '.78rem' }}>{appointment.appointmentDate} at {appointment.appointmentTime}</div>
          </div>
          <StatusBadge status={appointment.status} />
        </div>
        {appointment.issueDescription && <p className="mt-2 mb-0 text-secondary" style={{ fontSize: '.83rem' }}>{appointment.issueDescription}</p>}

        {appointment.status === 'ASSIGNED' && (
          <CreateJobCardForm appointmentId={appointment.appointmentId} onCreated={onChanged} />
        )}

        {appointment.status === 'IN_PROGRESS' && (
          <>
            <div className="mt-3">
              <Button size="sm" variant="outline-secondary" onClick={toggle}>
                {expanded ? 'Hide job card' : 'Open job card'}
              </Button>
            </div>
            {expanded && jobCard === undefined && <p className="text-secondary small mt-2 mb-0">Loading…</p>}
            {expanded && jobCard === null && <p className="text-secondary small mt-2 mb-0">Job card not found.</p>}
            {expanded && jobCard && (
              <MechanicJobCardWork
                jobCard={jobCard}
                onJobCardUpdated={loadJobCard}
                onCompleted={() => { setExpanded(false); onChanged?.() }}
              />
            )}
          </>
        )}
      </Card.Body>
    </Card>
  )
}

export default function MyJobsPage() {
  const [appointments, setAppointments] = useState(null)
  const toast = useToast()

  const load = () => appointmentApi.getMechanicAppointments().then(setAppointments).catch((e) => toast.error(e.message))

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">My appointments</h2>
          <p>Appointments assigned to you.</p>
        </div>
      </div>

      {appointments === null ? (
        <LoadingBlock />
      ) : appointments.length === 0 ? (
        <EmptyState title="Nothing assigned yet">Your garage manager assigns appointments to you.</EmptyState>
      ) : (
        appointments.map((a) => <MechanicAppointmentCard key={a.appointmentId} appointment={a} onChanged={load} />)
      )}
    </>
  )
}
