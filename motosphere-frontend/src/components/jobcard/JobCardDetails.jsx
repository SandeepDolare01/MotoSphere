import Money from '../common/Money'

// Read-only rendering of a job card's diagnosis/remarks/labour/items -
// shared by the customer's "view job card" panel and the mechanic's
// "work on job card" panel (which wraps this with editing controls).
export default function JobCardDetails({ jobCard }) {
  return (
    <div className="ms-subcard">
      <div className="ms-kv"><span className="k">Diagnosis</span><span className="v">{jobCard.diagnosis || '—'}</span></div>
      <div className="ms-kv"><span className="k">Remarks</span><span className="v">{jobCard.remarks || '—'}</span></div>
      <div className="ms-kv"><span className="k">Labour charge</span><span className="v"><Money value={jobCard.labourCharge} /></span></div>
      {jobCard.items?.length > 0 && (
        <table className="table table-sm mt-2 mb-0">
          <thead>
            <tr>
              <th>Item</th><th>Type</th><th>Qty</th><th>Unit</th><th>Amount</th>
            </tr>
          </thead>
          <tbody>
            {jobCard.items.map((i) => (
              <tr key={i.jobCardItemId}>
                <td>{i.description}</td>
                <td>{i.itemType}</td>
                <td>{i.quantity}</td>
                <td><Money value={i.unitPrice} /></td>
                <td><Money value={i.amount} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {(!jobCard.items || jobCard.items.length === 0) && (
        <p className="text-secondary mt-2 mb-0" style={{ fontSize: '.78rem' }}>No parts/services logged yet.</p>
      )}
    </div>
  )
}
