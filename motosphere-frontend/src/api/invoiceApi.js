import axiosClient from './axiosClient'

export const getInvoiceForJobCard = (jobCardId) =>
  axiosClient.get(`/invoices/jobcard/${jobCardId}`).then((r) => r.data)

// Fetches the invoice PDF as a blob and triggers a browser download. The
// backend only returns bytes once the invoice is actually PAID - any other
// status comes back as a normal JSON error, which axiosClient's interceptor
// still needs to read as text, so we ask for an arraybuffer and only turn it
// into a PDF blob once we know the request succeeded.
export const downloadInvoicePdf = async (invoiceId, filename) => {
  const response = await axiosClient.get(`/invoices/${invoiceId}/pdf`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
  const link = document.createElement('a')
  link.href = url
  link.download = filename || `invoice-${invoiceId}.pdf`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
