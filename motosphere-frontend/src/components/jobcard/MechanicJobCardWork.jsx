import { useState } from 'react'
import { Form, Button, Row, Col } from 'react-bootstrap'
import JobCardDetails from '../jobcard/JobCardDetails'
import * as jobCardApi from '../../api/jobCardApi'
import useToast from '../../hooks/useToast'

const EMPTY_ITEM = { description: '', itemType: 'SERVICE', quantity: 1, unitPrice: '' }

export default function MechanicJobCardWork({ jobCard, onJobCardUpdated, onCompleted }) {
  const [item, setItem] = useState(EMPTY_ITEM)
  const [busyItem, setBusyItem] = useState(false)
  const [busyComplete, setBusyComplete] = useState(false)
  const toast = useToast()

  const onItemChange = (e) => setItem((f) => ({ ...f, [e.target.name]: e.target.value }))

  const addItem = async (e) => {
    e.preventDefault()
    setBusyItem(true)
    try {
      await jobCardApi.addJobCardItem(jobCard.jobCardId, {
        ...item,
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
      })
      toast.success('Item added')
      setItem(EMPTY_ITEM)
      onJobCardUpdated?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusyItem(false)
    }
  }

  const complete = async () => {
    setBusyComplete(true)
    try {
      await jobCardApi.completeJobCard(jobCard.jobCardId)
      toast.success('Job marked complete — invoice generated')
      onCompleted?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusyComplete(false)
    }
  }

  return (
    <div className="mt-3">
      <JobCardDetails jobCard={jobCard} />

      <div className="ms-subcard mt-2">
        <strong style={{ fontSize: '.8rem' }}>Add item</strong>
        <Form onSubmit={addItem} className="mt-2">
          <Row>
            <Col md={7}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Description</Form.Label>
                <Form.Control name="description" required value={item.description} onChange={onItemChange} />
              </Form.Group>
            </Col>
            <Col md={5}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Type</Form.Label>
                <Form.Select name="itemType" value={item.itemType} onChange={onItemChange}>
                  <option value="SERVICE">Service</option>
                  <option value="PART">Part</option>
                </Form.Select>
              </Form.Group>
            </Col>
          </Row>
          <Row>
            <Col md={6}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Quantity</Form.Label>
                <Form.Control type="number" name="quantity" min="1" required value={item.quantity} onChange={onItemChange} />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Unit price</Form.Label>
                <Form.Control type="number" step="0.01" min="0" name="unitPrice" required value={item.unitPrice} onChange={onItemChange} />
              </Form.Group>
            </Col>
          </Row>
          <Button type="submit" variant="outline-secondary" size="sm" disabled={busyItem}>
            {busyItem ? 'Adding…' : 'Add item'}
          </Button>
        </Form>
      </div>

      <Button variant="outline-success" className="mt-3" onClick={complete} disabled={busyComplete}>
        {busyComplete ? 'Completing…' : 'Mark job complete'}
      </Button>
    </div>
  )
}
