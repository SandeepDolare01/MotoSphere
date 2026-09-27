import { useEffect, useState } from "react";
import { Card, Form, Button, Row, Col, Table } from "react-bootstrap";
import * as vehicleApi from "../../api/vehicleApi";
import FormField from "../../components/common/FormField";
import EmptyState from "../../components/common/EmptyState";
import LoadingBlock from "../../components/common/LoadingBlock";
import useToast from "../../hooks/useToast";

const EMPTY = {
  registrationNumber: "",
  vehicleType: "CAR",
  manufacturer: "",
  model: "",
  manufacturingYear: "",
  fuelType: "",
};

export default function VehiclesPage() {
  const [vehicles, setVehicles] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [busy, setBusy] = useState(false);
  const toast = useToast();

  const load = () =>
    vehicleApi
      .getMyVehicles()
      .then(setVehicles)
      .catch((e) => toast.error(e.message));

  useEffect(() => {
    load();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const onChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setBusy(true);
    try {
      const payload = {
        ...form,
        manufacturingYear: form.manufacturingYear
          ? Number(form.manufacturingYear)
          : null,
        fuelType: form.fuelType || null,
      };
      await vehicleApi.addVehicle(payload);
      toast.success("Vehicle added");
      setForm(EMPTY);
      load();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <div className="ms-section-head">
        <div>
          <h2 className="h4">My vehicles</h2>
          <p>Register a vehicle before booking a service appointment for it.</p>
        </div>
      </div>

      <Card className="mb-4">
        <Card.Body>
          <h3 className="h6 mb-3">Add a vehicle</h3>
          <Form onSubmit={onSubmit}>
            <Row>
              <Col md={6}>
                <FormField
                  label="Registration number"
                  name="registrationNumber"
                  required
                  value={form.registrationNumber}
                  onChange={onChange}
                  placeholder="KA01AB1234"
                />
              </Col>
              <Col md={6}>
                <FormField
                  as="select"
                  label="Type"
                  name="vehicleType"
                  required
                  value={form.vehicleType}
                  onChange={onChange}>
                  <option value="CAR">Car</option>
                  <option value="BIKE">Bike</option>
                  <option value="TRUCK">Truck</option>
                </FormField>
              </Col>
            </Row>
            <Row>
              <Col md={6}>
                <FormField
                  label="Manufacturer"
                  name="manufacturer"
                  required
                  value={form.manufacturer}
                  onChange={onChange}
                />
              </Col>
              <Col md={6}>
                <FormField
                  label="Model"
                  name="model"
                  value={form.model}
                  onChange={onChange}
                />
              </Col>
            </Row>
            <Row>
              <Col md={6}>
                <FormField
                  label="Manufacturing year"
                  name="manufacturingYear"
                  type="number"
                  min="1980"
                  max="2100"
                  value={form.manufacturingYear}
                  onChange={onChange}
                />
              </Col>
              <Col md={6}>
                <FormField
                  as="select"
                  label="Fuel type"
                  name="fuelType"
                  value={form.fuelType}
                  onChange={onChange}>
                  <option value="">—</option>
                  <option value="PETROL">Petrol</option>
                  <option value="DIESEL">Diesel</option>
                </FormField>
              </Col>
            </Row>
            <Button
              type="submit"
              variant="light"
              className="btn-amber"
              disabled={busy}>
              {busy ? "Adding…" : "Add vehicle"}
            </Button>
          </Form>
        </Card.Body>
      </Card>

      {vehicles === null ? (
        <LoadingBlock />
      ) : vehicles.length === 0 ? (
        <EmptyState title="No vehicles yet">
          Add one above to start booking service appointments.
        </EmptyState>
      ) : (
        <Card>
          <Card.Body className="p-0">
            <Table responsive hover className="mb-0">
              <thead>
                <tr>
                  <th>Registration</th>
                  <th>Vehicle</th>
                  <th>Year</th>
                  <th>Type</th>
                  <th>Fuel</th>
                </tr>
              </thead>
              <tbody>
                {vehicles.map((v) => (
                  <tr key={v.vehicleId}>
                    <td className="ms-mono">{v.registrationNumber}</td>
                    <td>
                      {v.manufacturer} {v.model}
                    </td>
                    <td>{v.manufacturingYear || "—"}</td>
                    <td>{v.vehicleType}</td>
                    <td>{v.fuelType || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}
    </>
  );
}
