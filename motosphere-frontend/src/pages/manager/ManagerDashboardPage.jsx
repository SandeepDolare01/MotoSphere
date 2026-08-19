import { useEffect, useState } from 'react'
import { Row, Col, Card, Table } from 'react-bootstrap'
import * as dashboardApi from '../../api/dashboardApi'
import Money from '../../components/common/Money'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

export default function ManagerDashboardPage() {
  const [data, setData] = useState(null)
  const toast = useToast()

  useEffect(() => {
    dashboardApi.getManagerDashboard().then(setData).catch((e) => toast.error(e.message))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">Dashboard</h2>
          <p>{data ? data.garageName : 'Your garage\u2019s'} earnings and transactions.</p>
        </div>
      </div>

      {data === null ? (
        <LoadingBlock />
      ) : (
        <>
          <Row className="g-3 mb-4">
            <Col md={6}>
              <div className="ms-stat-card">
                <div className="label">Today's earnings</div>
                <div className="value"><Money value={data.todaysEarnings} /></div>
              </div>
            </Col>
            <Col md={6}>
              <div className="ms-stat-card">
                <div className="label">Total earnings</div>
                <div className="value"><Money value={data.totalEarnings} /></div>
              </div>
            </Col>
          </Row>

          <h3 className="h6 mb-2">Transactions</h3>
          <Card>
            <Card.Body className="p-0">
              <Table responsive hover className="mb-0">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Invoice</th>
                    <th>Customer</th>
                    <th>Vehicle</th>
                    <th>Method</th>
                    <th>Amount paid</th>
                    <th>Your earnings</th>
                  </tr>
                </thead>
                <tbody>
                  {data.transactions.length === 0 ? (
                    <tr><td colSpan={7} className="text-secondary text-center py-4">No transactions yet</td></tr>
                  ) : (
                    data.transactions.map((t) => (
                      <tr key={t.paymentId}>
                        <td className="ms-mono" style={{ fontSize: '.78rem' }}>{formatDate(t.paymentDate)}</td>
                        <td className="ms-mono" style={{ fontSize: '.78rem' }}>{t.invoiceNumber}</td>
                        <td>{t.customerName}</td>
                        <td>{t.vehicleRegistrationNumber}</td>
                        <td>{t.paymentMethod}</td>
                        <td><Money value={t.amountPaid} /></td>
                        <td><Money value={t.garageAmount} /></td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </>
      )}
    </>
  )
}

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString()
}
