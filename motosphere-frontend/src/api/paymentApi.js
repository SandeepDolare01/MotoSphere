import paymentAxiosClient from './paymentAxiosClient'

// All three of these now hit payment-service (a separate app/port), not the
// core backend - see paymentAxiosClient.js.
export const payInvoice = (invoiceId, payload) =>
  paymentAxiosClient.post(`/payments/invoice/${invoiceId}`, payload).then((r) => r.data)

export const createRazorpayOrder = (invoiceId) =>
  paymentAxiosClient.post(`/payments/invoice/${invoiceId}/razorpay-order`).then((r) => r.data)

export const verifyRazorpayPayment = (invoiceId, payload) =>
  paymentAxiosClient.post(`/payments/invoice/${invoiceId}/razorpay-verify`, payload).then((r) => r.data)
