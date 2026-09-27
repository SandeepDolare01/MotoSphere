import { useEffect, useState } from 'react'
import { Card, Form, Button } from 'react-bootstrap'
import * as garageApi from '../../api/garageApi'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

function ApplicationCard({ g, onChanged }) {
  const [commission, setCommission] = useState('')
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  const approve = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      await garageApi.approveGarage(g.garageId, { commissionPercentage: Number(commission) })
      toast.success('Garage approved')
      onChanged?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  const reject = async () => {
    const reason = window.prompt('Reason for rejection (optional):') || ''
    try {
      await garageApi.rejectGarage(g.garageId, { reason })
      toast.success('Application rejected')
      onChanged?.()
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <Card className="mb-3">
      <Card.Body>
        <h3 className="h6">{g.garageName}</h3>
        <div className="ms-kv"><span className="k">Owner</span><span className="v">{g.ownerName || '—'}</span></div>
        <div className="ms-kv"><span className="k">Address</span><span className="v">{g.address || '—'}</span></div>
        <div className="ms-kv"><span className="k">Applicant</span><span className="v">{g.applicantManagerName || '—'} · {g.applicantManagerEmail || '—'}</span></div>
        <hr />
        <Form onSubmit={approve} className="d-flex gap-2 align-items-end flex-wrap">
          <Form.Group className="mb-0" style={{ minWidth: 160 }}>
            <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Commission %</Form.Label>
            <Form.Control type="number" step="0.1" min="0" max="100" required value={commission} onChange={(e) => setCommission(e.target.value)} placeholder="10.0" />
          </Form.Group>
          <Button type="submit" variant="outline-success" disabled={busy}>Approve</Button>
          <Button type="button" variant="outline-danger" onClick={reject}>Reject</Button>
        </Form>
      </Card.Body>
    </Card>
  )
}

export default function PendingGaragesPage() {
  const [garages, setGarages] = useState(null)
  const toast = useToast()

  const load = () => garageApi.getPendingGarages().then(setGarages).catch((e) => toast.error(e.message))

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">Garage applications</h2>
          <p>Approve to set commission terms and activate the manager's account, or reject.</p>
        </div>
      </div>

      {garages === null ? (
        <LoadingBlock />
      ) : garages.length === 0 ? (
        <EmptyState title="No pending applications">You're all caught up.</EmptyState>
      ) : (
        garages.map((g) => <ApplicationCard key={g.garageId} g={g} onChanged={load} />)
      )}
    </>
  )
}
