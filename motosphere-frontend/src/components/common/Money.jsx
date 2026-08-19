export default function Money({ value }) {
  if (value === null || value === undefined) return <span>—</span>
  return <span className="ms-mono ms-price">₹{Number(value).toFixed(2)}</span>
}
