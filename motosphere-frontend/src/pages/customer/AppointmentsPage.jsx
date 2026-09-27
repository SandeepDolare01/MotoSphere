import { useEffect, useState } from 'react'
import * as appointmentApi from '../../api/appointmentApi'
import AppointmentCard from '../../components/appointment/AppointmentCard'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState(null)
  const toast = useToast()

  const load = () => appointmentApi.getMyAppointments().then(setAppointments).catch((e) => toast.error(e.message))

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">My appointments</h2>
          <p>Track a booking from drop-off to payment.</p>
        </div>
      </div>

      {appointments === null ? (
        <LoadingBlock />
      ) : appointments.length === 0 ? (
        <EmptyState title="No appointments yet">Browse garages to book your first service.</EmptyState>
      ) : (
        appointments.map((a) => <AppointmentCard key={a.appointmentId} appointment={a} onChanged={load} />)
      )}
    </>
  )
}
