export default function EmptyState({ title, children }) {
  return (
    <div className="ms-empty">
      <strong>{title}</strong>
      {children && <div className="ms-empty-body">{children}</div>}
    </div>
  )
}
