import { useState } from 'react'
import { Form, Button } from 'react-bootstrap'
import Money from '../common/Money'
import StatusBadge from '../common/StatusBadge'
import * as paymentApi from '../../api/paymentApi'
import * as invoiceApi from '../../api/invoiceApi'
import useToast from '../../hooks/useToast'

// Shown inside the customer's job card view once the job is COMPLETED and an
// invoice exists. ONLINE payments go through Razorpay Checkout (order ->
// customer pays -> we verify the signature server-side); CASH is still
// recorded manually (e.g. by a garage manager). The parent just needs to
// re-fetch the invoice after a successful payment via onPaid. Once paid, a
// "Download Invoice" button appears - the backend only serves the PDF once
// paymentStatus is PAID, so there's no need to gate this any further here.
export default function InvoicePanel({ invoice, onPaid }) {
  const [method, setMethod] = useState('ONLINE')
  const [transactionId, setTransactionId] = useState('')
  const [busy, setBusy] = useState(false)
  const [downloading, setDownloading] = useState(false)
  const toast = useToast()

  const paid = invoice.paymentStatus === 'PAID'

  const downloadPdf = async () => {
    setDownloading(true)
    try {
      await invoiceApi.downloadInvoicePdf(invoice.invoiceId, `${invoice.invoiceNumber}.pdf`)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setDownloading(false)
    }
  }

  const payWithRazorpay = async () => {
    setBusy(true)
    try {
      const order = await paymentApi.createRazorpayOrder(invoice.invoiceId)

      if (typeof window.Razorpay === 'undefined') {
        toast.error('Payment widget failed to load. Please refresh and try again.')
        setBusy(false)
        return
      }

      const rzp = new window.Razorpay({
        key: order.razorpayKeyId,
        amount: order.amount,
        currency: order.currency,
        name: 'MotoSphere',
        description: `Invoice ${invoice.invoiceNumber}`,
        order_id: order.razorpayOrderId,
        handler: async (response) => {
          try {
            await paymentApi.verifyRazorpayPayment(invoice.invoiceId, {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            toast.success('Payment successful — invoice emailed to you')
            onPaid?.()
          } catch (err) {
            toast.error(err.message)
          } finally {
            setBusy(false)
          }
        },
        modal: {
          ondismiss: () => setBusy(false),
        },
        theme: { color: '#d97706' },
      })

      rzp.on('payment.failed', () => {
        toast.error('Payment failed. Please try again.')
        setBusy(false)
      })

      rzp.open()
    } catch (err) {
      toast.error(err.message)
      setBusy(false)
    }
  }

  const payCash = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      await paymentApi.payInvoice(invoice.invoiceId, { paymentMethod: 'CASH', transactionId: transactionId || null })
      toast.success('Payment recorded')
      onPaid?.()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="ms-subcard mt-3">
      <div className="d-flex justify-content-between align-items-center mb-2">
        <strong className="ms-mono" style={{ fontSize: '.78rem' }}>{invoice.invoiceNumber}</strong>
        <StatusBadge status={invoice.paymentStatus} />
      </div>
      <div className="ms-kv"><span className="k">Subtotal</span><span className="v"><Money value={invoice.subtotal} /></span></div>
      <div className="ms-kv"><span className="k">GST ({invoice.gstPercentage}%)</span><span className="v"><Money value={invoice.gstAmount} /></span></div>
      <div className="ms-kv"><span className="k">Total</span><span className="v"><Money value={invoice.totalAmount} /></span></div>

      {!paid && (
        <div className="mt-3 pt-3 border-top">
          <Form.Group className="mb-2">
            <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Payment method</Form.Label>
            <Form.Select value={method} onChange={(e) => setMethod(e.target.value)}>
              <option value="ONLINE">Online (Razorpay)</option>
              <option value="CASH">Cash</option>
            </Form.Select>
          </Form.Group>

          {method === 'ONLINE' ? (
            <Button variant="light" className="btn-amber" disabled={busy} onClick={payWithRazorpay}>
              {busy ? 'Opening payment…' : <>Pay <Money value={invoice.totalAmount} /></>}
            </Button>
          ) : (
            <Form onSubmit={payCash}>
              <Form.Group className="mb-2">
                <Form.Label className="small fw-semibold text-uppercase text-secondary" style={{ fontSize: '.68rem' }}>Transaction ID (optional)</Form.Label>
                <Form.Control value={transactionId} onChange={(e) => setTransactionId(e.target.value)} placeholder="Receipt / reference number" />
              </Form.Group>
              <Button type="submit" variant="light" className="btn-amber" disabled={busy}>
                {busy ? 'Recording…' : <>Mark paid <Money value={invoice.totalAmount} /></>}
              </Button>
            </Form>
          )}
        </div>
      )}

      {paid && (
        <div className="mt-3 pt-3 border-top">
          <Button variant="outline-secondary" size="sm" disabled={downloading} onClick={downloadPdf}>
            {downloading ? 'Preparing PDF…' : 'Download invoice (PDF)'}
          </Button>
        </div>
      )}
    </div>
  )
}
