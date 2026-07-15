import React, { useState } from "react";
import OrderForm from "./components/OrderForm";
import Dashboard from "./components/Dashboard";
import ServiceTester from "./components/ServiceTester";

export default function App() {
  const [tab, setTab] = useState("order");

  return (
    <div className="app">
      <h1>🍔 Food Order Processing System</h1>
      <p className="subtitle">React + Spring Boot + Camunda BPMN + ActiveMQ + MySQL</p>

      <div className="tabs">
        <button className={`tab ${tab === "order" ? "active" : ""}`} onClick={() => setTab("order")}>
          Place Order & Track
        </button>
        <button className={`tab ${tab === "services" ? "active" : ""}`} onClick={() => setTab("services")}>
          Test Services Independently
        </button>
      </div>

      {tab === "order" && (
        <>
          <OrderForm onOrderPlaced={() => {}} />
          <Dashboard />
        </>
      )}

      {tab === "services" && <ServiceTester />}
    </div>
  );
}
