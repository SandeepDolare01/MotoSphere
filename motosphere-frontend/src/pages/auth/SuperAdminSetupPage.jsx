import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Form, Button, Alert, Row, Col } from 'react-bootstrap'
import * as authApi from '../../api/authApi'
import FormField from '../../components/common/FormField'
import useAuth from '../../hooks/useAuth'
import useToast from '../../hooks/useToast'
import { HOME_BY_ROLE } from '../../utils/roleNav'

const EMPTY = { firstName: '', lastName: '', email: '', password: '' }

// Deliberately NOT one of the three tabs in AuthLayout - this only works
// once, ever (the backend rejects every call after the first SUPER_ADMIN
// exists), so it isn't a normal everyday auth option. It's reachable via a
// small link from the login screen instead.
export default function SuperAdminSetupPage() {
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const { applyAuthResponse } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const resp = await authApi.registerSuperAdmin(form)
      const { role } = await applyAuthResponse(resp)
      toast.success('Super admin created — you are now signed in')
      navigate(HOME_BY_ROLE[role] || '/')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <Form onSubmit={onSubmit}>
      <Alert variant="warning" className="py-2 small">
        This creates the platform's <strong>first</strong> super admin account and only ever works
        once — every call after that is rejected, regardless of who makes it. Use it now if this is
        a fresh install; otherwise sign in normally.
      </Alert>
      {error && <Alert variant="danger" className="py-2 small">{error}</Alert>}
      <Row>
        <Col><FormField label="First name" name="firstName" required value={form.firstName} onChange={onChange} /></Col>
        <Col><FormField label="Last name" name="lastName" value={form.lastName} onChange={onChange} /></Col>
      </Row>
      <FormField label="Email" name="email" type="email" required value={form.email} onChange={onChange} />
      <FormField label="Password" name="password" type="password" required value={form.password} onChange={onChange} />
      <Button type="submit" variant="dark" className="w-100" disabled={busy}>
        {busy ? 'Creating…' : 'Create the first super admin'}
      </Button>
      <p className="text-center mt-3 mb-0">
        <Link to="/login" className="small text-decoration-none">Back to sign in</Link>
      </p>
    </Form>
  )
}
