import { useState } from 'react'
import { Card, Form, Button, Row, Col } from 'react-bootstrap'
import * as garageApi from '../../api/garageApi'
import FormField from '../../components/common/FormField'
import useToast from '../../hooks/useToast'

const EMPTY = { garageName: '', ownerName: '', contactNumber: '', address: '', email: '', openingTime: '', closingTime: '', commissionPercentage: '' }

export default function CreateGaragePage() {
  const [form, setForm] = useState(EMPTY)
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      await garageApi.createGarage({ ...form, commissionPercentage: Number(form.commissionPercentage) })
      toast.success('Garage created')
      setForm(EMPTY)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">Add a garage</h2>
          <p>Created directly by you — starts approved immediately, no review needed.</p>
        </div>
      </div>
      <Card>
        <Card.Body>
          <Form onSubmit={onSubmit}>
            <FormField label="Garage name" name="garageName" required value={form.garageName} onChange={onChange} />
            <Row>
              <Col md={6}><FormField label="Owner name" name="ownerName" value={form.ownerName} onChange={onChange} /></Col>
              <Col md={6}><FormField label="Contact number" name="contactNumber" value={form.contactNumber} onChange={onChange} /></Col>
            </Row>
            <FormField label="Address" name="address" value={form.address} onChange={onChange} />
            <FormField label="Email" name="email" type="email" value={form.email} onChange={onChange} />
            <Row>
              <Col md={6}><FormField label="Opening time" name="openingTime" type="time" value={form.openingTime} onChange={onChange} /></Col>
              <Col md={6}><FormField label="Closing time" name="closingTime" type="time" value={form.closingTime} onChange={onChange} /></Col>
            </Row>
            <FormField label="Commission %" name="commissionPercentage" type="number" step="0.1" min="0" max="100" required value={form.commissionPercentage} onChange={onChange} />
            <Button type="submit" variant="light" className="btn-amber" disabled={busy}>{busy ? 'Creating…' : 'Create garage'}</Button>
          </Form>
        </Card.Body>
      </Card>
    </>
  )
}
