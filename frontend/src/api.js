const ORDER_SERVICE = "http://localhost:8081";
const PAYMENT_SERVICE = "http://localhost:8082";
const KITCHEN_SERVICE = "http://localhost:8083";
const DELIVERY_SERVICE = "http://localhost:8084";

async function request(url, options) {
  const res = await fetch(url, options);
  if (!res.ok) {
    throw new Error(`Request to ${url} failed with status ${res.status}`);
  }
  return res.json();
}

export function placeOrder(order) {
  return request(`${ORDER_SERVICE}/api/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
}

export function listOrders() {
  return request(`${ORDER_SERVICE}/api/orders`);
}

// Direct, standalone REST calls to each downstream service — for demoing/testing
// each piece independently of the Camunda workflow.
export function directPayment(orderId, amount) {
  return request(`${PAYMENT_SERVICE}/api/payments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderId, amount }),
  });
}

export function directKitchenTicket(orderId, items) {
  return request(`${KITCHEN_SERVICE}/api/kitchen/tickets`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderId, items }),
  });
}

export function directDelivery(orderId) {
  return request(`${DELIVERY_SERVICE}/api/deliveries`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderId }),
  });
}
