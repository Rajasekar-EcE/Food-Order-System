import React, { useState } from "react";
import { directPayment, directKitchenTicket, directDelivery } from "../api";

function ServicePanel({ title, port, topic, fields, onRun }) {
  const [values, setValues] = useState({});
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (name, value) => setValues((v) => ({ ...v, [name]: value }));

  const run = async () => {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const res = await onRun(values);
      setResult(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card service-panel">
      <h3>{title}</h3>
      <p style={{ fontSize: 12, color: "#888", marginTop: -8 }}>
        Port {port} · Camunda topic: <code>{topic}</code>
      </p>
      {fields.map((f) => (
        <div className="form-row" key={f.name}>
          <label>{f.label}</label>
          <input
            value={values[f.name] || ""}
            onChange={(e) => handleChange(f.name, e.target.value)}
            placeholder={f.placeholder}
          />
        </div>
      ))}
      <button className="secondary" onClick={run} disabled={loading}>
        {loading ? "Calling..." : "Call this service directly"}
      </button>
      {result && <div className="result-box">{JSON.stringify(result, null, 2)}</div>}
      {error && <div className="error-text">{error}</div>}
    </div>
  );
}

export default function ServiceTester() {
  return (
    <div>
      <p style={{ color: "#666", marginBottom: 16 }}>
        These call each service's REST API directly, bypassing Camunda entirely — useful for
        testing or demoing one piece at a time. Note: calls made here won't advance any real
        order's workflow, since no external task was dispatched.
      </p>
      <div className="service-grid">
        <ServicePanel
          title="Payment Service"
          port={8082}
          topic="payment-processing"
          fields={[
            { name: "orderId", label: "Order ID", placeholder: "1" },
            { name: "amount", label: "Amount", placeholder: "24.99" },
          ]}
          onRun={(v) => directPayment(Number(v.orderId), parseFloat(v.amount))}
        />
        <ServicePanel
          title="Kitchen Service"
          port={8083}
          topic="kitchen-preparation"
          fields={[
            { name: "orderId", label: "Order ID", placeholder: "1" },
            { name: "items", label: "Items", placeholder: "1x Pizza" },
          ]}
          onRun={(v) => directKitchenTicket(Number(v.orderId), v.items)}
        />
        <ServicePanel
          title="Delivery Service"
          port={8084}
          topic="delivery-assignment"
          fields={[
            { name: "orderId", label: "Order ID", placeholder: "1" },
          ]}
          onRun={(v) => directDelivery(Number(v.orderId))}
        />
      </div>
    </div>
  );
}
