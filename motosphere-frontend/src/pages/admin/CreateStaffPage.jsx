import { useState } from 'react'
import { Card, Form, Button, Row, Col } from 'react-bootstrap'
import * as userApi from '../../api/userApi'
import FormField from '../../components/common/FormField'
import useToast from '../../hooks/useToast'

const EMPTY = { firstName: '', lastName: '', email: '', phoneNumber: '', password: '', role: 'GARAGE_MANAGER', garageId: '', specialization: '', experienceYears: '' }

export default function CreateStaffPage() {
  const [form, setForm] = useState(EMPTY)
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = {
        ...form,
        garageId: Number(form.garageId),
        experienceYears: form.experienceYears ? Number(form.experienceYears) : null,
      }
      await userApi.createStaff(payload)
      toast.success('Staff account created')
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
          <h2 className="h4">Add staff</h2>
          <p>Provision a garage manager or mechanic directly, at any garage, by its ID.</p>
        </div>
      </div>
      <Card>
        <Card.Body>
          <Form onSubmit={onSubmit}>
            <Row>
              <Col md={6}><FormField label="First name" name="firstName" required value={form.firstName} onChange={onChange} /></Col>
              <Col md={6}><FormField label="Last name" name="lastName" value={form.lastName} onChange={onChange} /></Col>
            </Row>
            <Row>
              <Col md={6}><FormField label="Email" name="email" type="email" required value={form.email} onChange={onChange} /></Col>
              <Col md={6}><FormField label="Phone" name="phoneNumber" value={form.phoneNumber} onChange={onChange} /></Col>
            </Row>
            <FormField label="Password" name="password" type="password" required value={form.password} onChange={onChange} />
            <Row>
              <Col md={6}>
                <FormField as="select" label="Role" name="role" value={form.role} onChange={onChange}>
                  <option value="GARAGE_MANAGER">Garage manager</option>
                  <option value="MECHANIC">Mechanic</option>
                </FormField>
              </Col>
              <Col md={6}><FormField label="Garage ID" name="garageId" type="number" required value={form.garageId} onChange={onChange} /></Col>
            </Row>
            {form.role === 'MECHANIC' && (
              <Row>
                <Col md={6}><FormField label="Specialization" name="specialization" value={form.specialization} onChange={onChange} /></Col>
                <Col md={6}><FormField label="Experience (years)" name="experienceYears" type="number" min="0" value={form.experienceYears} onChange={onChange} /></Col>
              </Row>
            )}
            <Button type="submit" variant="light" className="btn-amber" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</Button>
          </Form>
        </Card.Body>
      </Card>
    </>
  )
}
