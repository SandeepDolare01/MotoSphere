import { useEffect, useState } from 'react'
import { Row, Col, Card, Table } from 'react-bootstrap'
import * as dashboardApi from '../../api/dashboardApi'
import Money from '../../components/common/Money'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

export default function AdminDashboardPage() {
  const [data, setData] = useState(null)
  const toast = useToast()

  useEffect(() => {
    dashboardApi.getAdminDashboard().then(setData).catch((e) => toast.error(e.message))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">Dashboard</h2>
          <p>Platform-wide commission and transactions across every garage.</p>
        </div>
      </div>

      {data === null ? (
        <LoadingBlock />
      ) : (
        <>
          <Row className="g-3 mb-4">
            <Col md={4}>
              <div className="ms-stat-card">
                <div className="label">Today's commission</div>
                <div className="value"><Money value={data.todaysCommissionEarned} /></div>
              </div>
            </Col>
            <Col md={4}>
              <div className="ms-stat-card">
                <div className="label">Total commission earned</div>
                <div className="value"><Money value={data.totalCommissionEarned} /></div>
              </div>
            </Col>
            <Col md={4}>
              <div className="ms-stat-card">
                <div className="label">Total revenue processed</div>
                <div className="value"><Money value={data.totalRevenue} /></div>
              </div>
            </Col>
          </Row>

          <h3 className="h6 mb-2">Earnings by garage</h3>
          <Card className="mb-4">
            <Card.Body className="p-0">
              <Table responsive hover className="mb-0">
                <thead>
                  <tr>
                    <th>Garage</th>
                    <th>Transactions</th>
                    <th>Revenue</th>
                    <th>Commission</th>
                    <th>Garage earnings</th>
                  </tr>
                </thead>
                <tbody>
                  {data.earningsByGarage.length === 0 ? (
                    <tr><td colSpan={5} className="text-secondary text-center py-4">No paid transactions yet</td></tr>
                  ) : (
                    data.earningsByGarage.map((g) => (
                      <tr key={g.garageId}>
                        <td>{g.garageName}</td>
                        <td>{g.transactionCount}</td>
                        <td><Money value={g.totalRevenue} /></td>
                        <td><Money value={g.totalCommission} /></td>
                        <td><Money value={g.totalGarageAmount} /></td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Table>
            </Card.Body>
          </Card>

          <h3 className="h6 mb-2">All transactions</h3>
          <Card>
            <Card.Body className="p-0">
              <Table responsive hover className="mb-0">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Invoice</th>
                    <th>Garage</th>
                    <th>Customer</th>
                    <th>Vehicle</th>
                    <th>Method</th>
                    <th>Amount</th>
                    <th>Commission</th>
                  </tr>
                </thead>
                <tbody>
                  {data.transactions.length === 0 ? (
                    <tr><td colSpan={8} className="text-secondary text-center py-4">No transactions yet</td></tr>
                  ) : (
                    data.transactions.map((t) => (
                      <tr key={t.paymentId}>
                        <td className="ms-mono" style={{ fontSize: '.78rem' }}>{formatDate(t.paymentDate)}</td>
                        <td className="ms-mono" style={{ fontSize: '.78rem' }}>{t.invoiceNumber}</td>
                        <td>{t.garageName}</td>
                        <td>{t.customerName}</td>
                        <td>{t.vehicleRegistrationNumber}</td>
                        <td>{t.paymentMethod}</td>
                        <td><Money value={t.amountPaid} /></td>
                        <td><Money value={t.commissionAmount} /></td>
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
