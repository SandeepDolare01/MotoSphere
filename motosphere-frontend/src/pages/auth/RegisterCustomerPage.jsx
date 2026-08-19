import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Button, Alert, Row, Col } from 'react-bootstrap'
import * as authApi from '../../api/authApi'
import FormField from '../../components/common/FormField'
import useToast from '../../hooks/useToast'

const EMPTY = { firstName: '', lastName: '', email: '', phoneNumber: '', password: '' }

export default function RegisterCustomerPage() {
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
      await authApi.registerCustomer(form)
      toast.success('Account created — sign in below')
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
      <Row>
        <Col><FormField label="First name" name="firstName" required value={form.firstName} onChange={onChange} /></Col>
        <Col><FormField label="Last name" name="lastName" value={form.lastName} onChange={onChange} /></Col>
      </Row>
      <FormField label="Email" name="email" type="email" required value={form.email} onChange={onChange} />
      <FormField label="Phone" name="phoneNumber" value={form.phoneNumber} onChange={onChange} />
      <FormField label="Password" name="password" type="password" required value={form.password} onChange={onChange} />
      <p className="text-secondary" style={{ fontSize: '.72rem', marginTop: '-0.5rem' }}>
        At least a digit, a lowercase letter, and one of # @ $ *, 5–20 characters.
      </p>
      <Button type="submit" variant="dark" className="w-100" disabled={busy}>
        {busy ? 'Creating account…' : 'Create account'}
      </Button>
    </Form>
  )
}
