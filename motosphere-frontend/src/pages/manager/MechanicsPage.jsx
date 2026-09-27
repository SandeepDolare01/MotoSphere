import { useEffect, useState } from 'react'
import { Card, Form, Button, Row, Col, Table } from 'react-bootstrap'
import * as garageApi from '../../api/garageApi'
import FormField from '../../components/common/FormField'
import StatusBadge from '../../components/common/StatusBadge'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

const EMPTY = { firstName: '', lastName: '', email: '', phoneNumber: '', password: '', specialization: '', experienceYears: '' }

export default function MechanicsPage() {
  const [mechanics, setMechanics] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  const load = () => garageApi.getMyGarageMechanics().then(setMechanics).catch((e) => toast.error(e.message))

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = { ...form, experienceYears: form.experienceYears ? Number(form.experienceYears) : null }
      await garageApi.addMechanic(payload)
      toast.success('Mechanic added')
      setForm(EMPTY)
      load()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  const toggleActive = async (m) => {
    try {
      if (m.active) await garageApi.deactivateMechanic(m.userId)
      else await garageApi.reactivateMechanic(m.userId)
      toast.success(m.active ? 'Mechanic deactivated' : 'Mechanic reactivated')
      load()
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">My mechanics</h2>
          <p>Add or remove mechanics at your garage.</p>
        </div>
      </div>

      <Card className="mb-4">
        <Card.Body>
          <h3 className="h6 mb-3">Add a mechanic</h3>
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
              <Col md={6}><FormField label="Specialization" name="specialization" value={form.specialization} onChange={onChange} placeholder="Engine repair" /></Col>
              <Col md={6}><FormField label="Experience (years)" name="experienceYears" type="number" min="0" value={form.experienceYears} onChange={onChange} /></Col>
            </Row>
            <Button type="submit" variant="light" className="btn-amber" disabled={busy}>{busy ? 'Adding…' : 'Add mechanic'}</Button>
          </Form>
        </Card.Body>
      </Card>

      {mechanics === null ? (
        <LoadingBlock />
      ) : mechanics.length === 0 ? (
        <EmptyState title="No mechanics yet">Add your first one above.</EmptyState>
      ) : (
        <Card>
          <Card.Body className="p-0">
            <Table responsive hover className="mb-0">
              <thead>
                <tr><th>Name</th><th>Email</th><th>Specialization</th><th>Experience</th><th>Status</th><th></th></tr>
              </thead>
              <tbody>
                {mechanics.map((m) => (
                  <tr key={m.userId}>
                    <td>{m.firstName} {m.lastName}</td>
                    <td>{m.email}</td>
                    <td>{m.specialization || '—'}</td>
                    <td>{m.experienceYears != null ? `${m.experienceYears} yrs` : '—'}</td>
                    <td><StatusBadge status={m.active ? 'ACTIVE' : 'INACTIVE'} /></td>
                    <td>
                      <Button size="sm" variant={m.active ? 'outline-danger' : 'outline-success'} onClick={() => toggleActive(m)}>
                        {m.active ? 'Deactivate' : 'Reactivate'}
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}
    </>
  )
}
