import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Form, Button, Alert } from "react-bootstrap";
import useAuth from "../../hooks/useAuth";
import { HOME_BY_ROLE } from "../../utils/roleNav";
import FormField from "../../components/common/FormField";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const onChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      const { role } = await login(form);
      navigate(HOME_BY_ROLE[role] || "/");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Form onSubmit={onSubmit}>
      {error && (
        <Alert variant="danger" className="py-2 small">
          {error}
        </Alert>
      )}
      <FormField
        label="Email"
        name="email"
        type="email"
        required
        value={form.email}
        onChange={onChange}
        placeholder="you@example.com"
      />
      <FormField
        label="Password"
        name="password"
        type="password"
        required
        value={form.password}
        onChange={onChange}
      />
      <Button type="submit" variant="dark" className="w-100" disabled={busy}>
        {busy ? "Signing in…" : "Sign in"}
      </Button>
      <p
        className="text-secondary mt-3 mb-0 text-center"
        style={{ fontSize: ".75rem" }}>
        First time setting this up?{" "}
        <Link to="/setup-admin" className="text-decoration-none">
          Create the first super admin account
        </Link>
        .
      </p>
    </Form>
  );
}
