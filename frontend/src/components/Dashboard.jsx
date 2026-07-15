import React, { useEffect, useState } from "react";
import { listOrders } from "../api";

export default function Dashboard() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const data = await listOrders();
        setOrders(data.sort((a, b) => b.id - a.id));
        setError(null);
      } catch (err) {
        setError("Could not reach order-service (is it running on :8081?)");
      }
    };
    fetchOrders();
    const interval = setInterval(fetchOrders, 2000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="card">
      <h3>Order dashboard</h3>
      {error && <div className="error-text">{error}</div>}
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Customer</th>
            <th>Items</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Updated</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td>{o.id}</td>
              <td>{o.customerName}</td>
              <td>{o.items}</td>
              <td>${Number(o.amount).toFixed(2)}</td>
              <td><span className={`badge ${o.status}`}>{o.status}</span></td>
              <td>{o.updatedAt ? new Date(o.updatedAt).toLocaleTimeString() : "-"}</td>
            </tr>
          ))}
          {orders.length === 0 && !error && (
            <tr><td colSpan={6} style={{ color: "#999", textAlign: "center", padding: "16px" }}>
              No orders yet — place one above.
            </td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
