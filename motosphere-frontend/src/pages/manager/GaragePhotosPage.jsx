import { useEffect, useRef, useState } from 'react'
import { Card, Button } from 'react-bootstrap'
import useAuth from '../../hooks/useAuth'
import * as garageApi from '../../api/garageApi'
import EmptyState from '../../components/common/EmptyState'
import LoadingBlock from '../../components/common/LoadingBlock'
import useToast from '../../hooks/useToast'

export default function GaragePhotosPage() {
  const { auth } = useAuth()
  const [imageUrl, setImageUrl] = useState(undefined) // undefined = loading, null = none
  const [uploading, setUploading] = useState(false)
  const [removing, setRemoving] = useState(false)
  const fileInputRef = useRef(null)
  const toast = useToast()

  // The manager's own garageId comes from their cached profile (set at
  // login/bootstrap) - getGarageById reuses the same public endpoint the
  // browse-garages page uses, which already returns imageUrl.
  const load = () => {
    if (!auth.profile?.garageId) return
    garageApi.getGarageById(auth.profile.garageId).then((g) => setImageUrl(g.imageUrl)).catch((e) => toast.error(e.message))
  }

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const onFileSelected = async (e) => {
    const file = e.target.files?.[0]
    e.target.value = '' // allow re-selecting the same file later
    if (!file) return

    setUploading(true)
    try {
      await garageApi.uploadMyGarageImage(file)
      toast.success('Photo uploaded')
      load()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setUploading(false)
    }
  }

  const remove = async () => {
    setRemoving(true)
    try {
      await garageApi.deleteMyGarageImage()
      toast.success('Photo removed')
      load()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setRemoving(false)
    }
  }

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">Garage photo</h2>
          <p>Shown to customers on the browse-garages page. JPEG/PNG/WEBP.</p>
        </div>
        <div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp"
            className="d-none"
            onChange={onFileSelected}
          />
          <Button
            variant="light"
            className="btn-amber"
            disabled={uploading}
            onClick={() => fileInputRef.current?.click()}
          >
            {uploading ? 'Uploading…' : imageUrl ? 'Replace photo' : 'Upload photo'}
          </Button>
        </div>
      </div>

      {imageUrl === undefined ? (
        <LoadingBlock />
      ) : !imageUrl ? (
        <EmptyState title="No photo yet">Upload one above.</EmptyState>
      ) : (
        <Card style={{ maxWidth: 420 }}>
          <Card.Img src={garageApi.garageImageUrl(imageUrl)} style={{ height: 240, objectFit: 'cover' }} />
          <Card.Body className="text-center">
            <Button size="sm" variant="outline-danger" className="w-100" disabled={removing} onClick={remove}>
              {removing ? 'Removing…' : 'Remove photo'}
            </Button>
          </Card.Body>
        </Card>
      )}
    </>
  )
}
