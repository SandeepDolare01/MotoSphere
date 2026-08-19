import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Button, Alert, Row, Col } from 'react-bootstrap'
import * as authApi from '../../api/authApi'
import FormField from '../../components/common/FormField'
import useToast from '../../hooks/useToast'

const EMPTY = {
  firstName: '', lastName: '', email: '', phoneNumber: '', password: '',
  garageName: '', ownerName: '', address: '', garageContactNumber: '', garageEmail: '',
}

export default function RegisterGaragePage() {
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const toast = useToast()
  const navigate = useNavigate()

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await authApi.registerGarageManager(form)
      toast.success('Application submitted — wait for approval, then sign in')
      navigate('/login')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Form onSubmit={onSubmit}>
      {error && <Alert variant="danger" className="py-2 small">{error}</Alert>}
      <p className="text-secondary" style={{ fontSize: '.78rem' }}>
        Submits your garage as a pending application. A super admin reviews it and sets your
        commission terms before your account can sign in.
      </p>
      <Row>
        <Col><FormField label="Your first name" name="firstName" required value={form.firstName} onChange={onChange} /></Col>
        <Col><FormField label="Your last name" name="lastName" value={form.lastName} onChange={onChange} /></Col>
      </Row>
      <FormField label="Your email" name="email" type="email" required value={form.email} onChange={onChange} />
      <FormField label="Your phone" name="phoneNumber" value={form.phoneNumber} onChange={onChange} />
      <FormField label="Your password" name="password" type="password" required value={form.password} onChange={onChange} />
      <hr />
      <FormField label="Garage name" name="garageName" required value={form.garageName} onChange={onChange} />
      <FormField label="Owner name" name="ownerName" value={form.ownerName} onChange={onChange} />
      <FormField label="Address" name="address" value={form.address} onChange={onChange} />
      <Row>
        <Col><FormField label="Garage contact number" name="garageContactNumber" value={form.garageContactNumber} onChange={onChange} /></Col>
        <Col><FormField label="Garage email" name="garageEmail" type="email" value={form.garageEmail} onChange={onChange} /></Col>
      </Row>
      <Button type="submit" variant="light" className="w-100 btn-amber" disabled={busy}>
        {busy ? 'Submitting…' : 'Submit application'}
      </Button>
    </Form>
  )
}
