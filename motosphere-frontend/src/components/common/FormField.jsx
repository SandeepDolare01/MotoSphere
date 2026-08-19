import { Form } from 'react-bootstrap'

// A labeled Bootstrap form control with our house label style, so every
// form in the app looks and behaves the same way instead of each page
// re-deriving its own label markup.
export default function FormField({ label, name, as, children, ...controlProps }) {
  return (
    <Form.Group className="mb-3">
      {label && (
        <Form.Label
          className="small fw-semibold text-uppercase text-secondary"
          style={{ fontSize: '.7rem', letterSpacing: '.05em' }}
        >
          {label}
        </Form.Label>
      )}
      {as === 'select' ? (
        <Form.Select name={name} {...controlProps}>
          {children}
        </Form.Select>
      ) : (
        <Form.Control as={as} name={name} {...controlProps} />
      )}
    </Form.Group>
  )
}
