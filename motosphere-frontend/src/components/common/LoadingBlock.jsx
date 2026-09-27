import { Spinner } from 'react-bootstrap'

export default function LoadingBlock({ label = 'Loading…' }) {
  return (
    <div className="d-flex align-items-center gap-2 text-secondary py-4">
      <Spinner animation="border" size="sm" />
      <span>{label}</span>
    </div>
  )
}
