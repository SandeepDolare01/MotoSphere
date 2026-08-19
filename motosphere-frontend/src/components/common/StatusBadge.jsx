import './StatusBadge.css'

const STATUS_VARIANT = {
  BOOKED: 'amber',
  ASSIGNED: 'amber',
  IN_PROGRESS: 'amber',
  OPEN: 'amber',
  PENDING: 'amber',
  COMPLETED: 'grease',
  APPROVED: 'grease',
  PAID: 'grease',
  ACTIVE: 'grease',
  CANCELLED: 'torque',
  REJECTED: 'torque',
  FAILED: 'torque',
  INACTIVE: 'torque',
}

// The one signature element reused for every status in the app - appointment,
// job card, invoice, garage approval, and user active/inactive all render
// through this same "inspection stamp" component.
export default function StatusBadge({ status }) {
  const variant = STATUS_VARIANT[status] || 'steel'
  return <span className={`ms-stamp ms-stamp-${variant}`}>{status}</span>
}
