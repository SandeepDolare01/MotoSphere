import { useState } from 'react'
import { Form, Button } from 'react-bootstrap'
import * as jobCardApi from '../../api/jobCardApi'
import useToast from '../../hooks/useToast'

export default function CreateJobCardForm({ appointmentId, onCreated }) {
  const [form, setForm] = useState({ diagnosis: '', remarks: '', labourCharge: '' })
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      await jobCardApi.createJobCard(appointmentId, { ...form, labourCharge: form.labourCharge ? Number(form.labourCharge) : 0 })
      toast.success('Job card created')
      onCreated?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="ms-subcard mt-3">
      <strong style={{ fontSize: '.8rem' }}>Create job card</strong>
      <Form onSubmit={onSubmit} className="mt-2">
        <Form.Group className="mb-2">
          <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Diagnosis</Form.Label>
          <Form.Control as="textarea" rows={2} name="diagnosis" value={form.diagnosis} onChange={onChange} placeholder="What's the issue?" />
        </Form.Group>
        <Form.Group className="mb-2">
          <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Remarks</Form.Label>
          <Form.Control as="textarea" rows={2} name="remarks" value={form.remarks} onChange={onChange} />
        </Form.Group>
        <Form.Group className="mb-3">
          <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Labour charge</Form.Label>
          <Form.Control type="number" step="0.01" min="0" name="labourCharge" value={form.labourCharge} onChange={onChange} placeholder="0.00" />
        </Form.Group>
        <Button type="submit" variant="light" className="btn-amber" disabled={busy}>
          {busy ? 'Creating…' : 'Create job card'}
        </Button>
      </Form>
    </div>
  )
}
