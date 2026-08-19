import { useEffect, useState } from 'react'
import { Card, Table, Button } from 'react-bootstrap'
import * as userApi from '../../api/userApi'
import StatusBadge from '../../components/common/StatusBadge'
import LoadingBlock from '../../components/common/LoadingBlock'
import useAuth from '../../hooks/useAuth'
import useToast from '../../hooks/useToast'
import { ROLE_LABELS } from '../../utils/roleNav'

export default function AllUsersPage() {
  const [users, setUsers] = useState(null)
  const { auth } = useAuth()
  const toast = useToast()

  const load = () => userApi.getAllUsers().then(setUsers).catch((e) => toast.error(e.message))

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const toggleActive = async (u) => {
    try {
      if (u.active) await userApi.deactivateUser(u.userId)
      else await userApi.reactivateUser(u.userId)
      toast.success(u.active ? 'User deactivated' : 'User reactivated')
      load()
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">All users</h2>
          <p>Every account on the platform.</p>
        </div>
      </div>

      {users === null ? (
        <LoadingBlock />
      ) : (
        <Card>
          <Card.Body className="p-0">
            <Table responsive hover className="mb-0">
              <thead>
                <tr><th>Name</th><th>Email</th><th>Role</th><th>Garage</th><th>Status</th><th></th></tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.userId}>
                    <td>{u.firstName} {u.lastName}</td>
                    <td>{u.email}</td>
                    <td>{ROLE_LABELS[u.role] || u.role}</td>
                    <td>{u.garageName || '—'}</td>
                    <td><StatusBadge status={u.active ? 'ACTIVE' : 'INACTIVE'} /></td>
                    <td>
                      {u.userId !== auth.userId && (
                        <Button size="sm" variant={u.active ? 'outline-danger' : 'outline-success'} onClick={() => toggleActive(u)}>
                          {u.active ? 'Deactivate' : 'Reactivate'}
                        </Button>
                      )}
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
