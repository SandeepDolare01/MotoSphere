import { useState } from 'react'
import { Card, Button } from 'react-bootstrap'
import StatusBadge from '../common/StatusBadge'
import JobCardDetails from '../jobcard/JobCardDetails'
import InvoicePanel from '../jobcard/InvoicePanel'
import * as appointmentApi from '../../api/appointmentApi'
import * as jobCardApi from '../../api/jobCardApi'
import * as invoiceApi from '../../api/invoiceApi'
import useToast from '../../hooks/useToast'

export default function AppointmentCard({ appointment, onChanged }) {
  const [expanded, setExpanded] = useState(false)
  const [jobCard, setJobCard] = useState(undefined) // undefined = not fetched, null = none exists
  const [invoice, setInvoice] = useState(undefined)
  const [loadingJobCard, setLoadingJobCard] = useState(false)
  const toast = useToast()

  const canCancel = appointment.status === 'BOOKED' || appointment.status === 'ASSIGNED'
  const canViewJobCard = appointment.status === 'IN_PROGRESS' || appointment.status === 'COMPLETED'

  const cancel = async () => {
    try {
      await appointmentApi.cancelAppointment(appointment.appointmentId)
      toast.success('Appointment cancelled')
      onChanged?.()
    } catch (err) {
      toast.error(err.message)
    }
  }

  const toggle = async () => {
    if (expanded) { setExpanded(false); return }
    setExpanded(true)
    if (jobCard === undefined) {
      setLoadingJobCard(true)
      try {
        const jc = await jobCardApi.getJobCardByAppointmentId(appointment.appointmentId)
        setJobCard(jc)
        if (appointment.status === 'COMPLETED') {
          try {
            const inv = await invoiceApi.getInvoiceForJobCard(jc.jobCardId)
            setInvoice(inv)
          } catch {
            setInvoice(null)
          }
        }
      } catch {
        setJobCard(null)
      } finally {
        setLoadingJobCard(false)
      }
    }
  }

  const refreshInvoice = async () => {
    if (!jobCard) return
    try {
      const inv = await invoiceApi.getInvoiceForJobCard(jobCard.jobCardId)
      setInvoice(inv)
    } catch {
      // ignore
    }
  }

  return (
    <Card className="mb-3">
      <Card.Body>
        <div className="d-flex justify-content-between align-items-start flex-wrap gap-2">
          <div>
            <h3 className="h6 mb-1">{appointment.vehicleRegistrationNumber} · {appointment.garageName}</h3>
            <div className="text-secondary" style={{ fontSize: '.78rem' }}>
              {appointment.appointmentDate} at {appointment.appointmentTime}
              {appointment.mechanicName && <> · Mechanic: {appointment.mechanicName}</>}
            </div>
          </div>
          <StatusBadge status={appointment.status} />
        </div>
        {appointment.issueDescription && (
          <p className="mt-2 mb-0 text-secondary" style={{ fontSize: '.83rem' }}>{appointment.issueDescription}</p>
        )}
        <div className="d-flex gap-2 mt-3">
          {canCancel && <Button size="sm" variant="outline-danger" onClick={cancel}>Cancel booking</Button>}
          {canViewJobCard && (
            <Button size="sm" variant="outline-secondary" onClick={toggle}>
              {expanded ? 'Hide job card' : 'View job card'}
            </Button>
          )}
        </div>
        {expanded && (
          <div className="mt-3">
            {loadingJobCard && <p className="text-secondary small mb-0">Loading…</p>}
            {!loadingJobCard && jobCard === null && <p className="text-secondary small mb-0">No job card yet — the garage hasn't started work.</p>}
            {!loadingJobCard && jobCard && (
              <>
                <JobCardDetails jobCard={jobCard} />
                {appointment.status === 'COMPLETED' && invoice === undefined && <p className="text-secondary small mt-2 mb-0">Loading invoice…</p>}
                {appointment.status === 'COMPLETED' && invoice === null && <p className="text-secondary small mt-2 mb-0">Invoice not available yet.</p>}
                {appointment.status === 'COMPLETED' && invoice && <InvoicePanel invoice={invoice} onPaid={refreshInvoice} />}
              </>
            )}
          </div>
        )}
      </Card.Body>
    </Card>
  )
}
