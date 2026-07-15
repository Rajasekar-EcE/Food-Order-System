import React, { useState } from "react";
import { placeOrder } from "../api";

export default function OrderForm({ onOrderPlaced }) {
  const [form, setForm] = useState({
    customerName: "",
    customerAddress: "",
    items: "",
    amount: "",
  });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const order = await placeOrder({
        ...form,
        amount: parseFloat(form.amount),
      });
      onOrderPlaced(order);
      setForm({ customerName: "", customerAddress: "", items: "", amount: "" });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="card">
      <h3>Place an order</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <label>Customer name</label>
          <input name="customerName" value={form.customerName} onChange={handleChange} required />
        </div>
        <div className="form-row">
          <label>Delivery address</label>
          <input name="customerAddress" value={form.customerAddress} onChange={handleChange} required />
        </div>
        <div className="form-row">
          <label>Items</label>
          <textarea name="items" value={form.items} onChange={handleChange} rows={2} required
            placeholder="e.g. 1x Margherita Pizza, 2x Garlic Bread" />
        </div>
        <div className="form-row">
          <label>Amount ($)</label>
          <input name="amount" type="number" step="0.01" value={form.amount} onChange={handleChange} required />
        </div>
        <button className="primary" type="submit" disabled={submitting}>
          {submitting ? "Placing..." : "Place Order"}
        </button>
        {error && <div className="error-text">{error}</div>}
      </form>
    </div>
  );
}
