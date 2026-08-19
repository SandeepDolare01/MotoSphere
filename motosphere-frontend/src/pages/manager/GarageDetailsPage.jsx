import { useEffect, useState } from 'react'
import { Card, Form, Button, Row, Col } from 'react-bootstrap'
import useAuth from '../../hooks/useAuth'
import * as garageApi from '../../api/garageApi'
import FormField from '../../components/common/FormField'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

// Registration only collects garageName/ownerName/address/contact/email -
// opening/closing time (and commission %, which is platform-controlled -
// see RegisterGarageManagerRequest on the backend) aren't known yet at
// that point. This page is where a manager fills those in afterwards and
// keeps their listing up to date over time.

export default function GarageDetailsPage() {
  const { auth } = useAuth()
  const [form, setForm] = useState(null) // null = loading
  const [busy, setBusy] = useState(false)
  const toast = useToast()

  const load = () => {
    if (!auth.profile?.garageId) return
    garageApi
      .getGarageById(auth.profile.garageId)
      .then((g) =>
        setForm({
          garageName: g.garageName || '',
          ownerName: g.ownerName || '',
          address: g.address || '',
          contactNumber: g.contactNumber || '',
          email: g.email || '',
          openingTime: g.openingTime || '',
          closingTime: g.closingTime || '',
          commissionPercentage: g.commissionPercentage ?? '',
        })
      )
      .catch((e) => toast.error(e.message))
  }

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const onSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      // commissionPercentage is sent back unchanged - it's set by MotoSphere
      // when the garage is approved, not something the manager edits here.
      await garageApi.updateGarage(auth.profile.garageId, {
        ...form,
        commissionPercentage: Number(form.commissionPercentage),
      })
      toast.success('Garage details updated')
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
          <h2 className="h4">Garage details</h2>
          <p>Keep your listing accurate - including hours, which aren&rsquo;t set until you fill them in here.</p>
        </div>
      </div>

      {form === null ? (
        <LoadingBlock />
      ) : (
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
              <FormField
                label="Commission % (set by MotoSphere)"
                name="commissionPercentage"
                type="number"
                value={form.commissionPercentage}
                disabled
                readOnly
              />
              <Button type="submit" variant="light" className="btn-amber" disabled={busy}>
                {busy ? 'Saving…' : 'Save changes'}
              </Button>
            </Form>
          </Card.Body>
        </Card>
      )}
    </>
  )
}
