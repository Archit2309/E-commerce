// All communication with the Java backend happens through these fetch() calls.
// The backend runs on the same origin (localhost:8080), so no CORS setup needed
// beyond what WebServer.java already sends.

const productGrid = document.getElementById('productGrid');
const cartPanel = document.getElementById('cartPanel');
const cartItemsDiv = document.getElementById('cartItems');
const cartSubtotalEl = document.getElementById('cartSubtotal');
const cartCountEl = document.getElementById('cartCount');
const orderConfirmationDiv = document.getElementById('orderConfirmation');
const orderHistoryList = document.getElementById('orderHistoryList');

// ----- Load and render products -----
async function loadProducts() {
  const res = await fetch('/api/products');
  const products = await res.json();

  productGrid.innerHTML = '';
  products.forEach(p => {
    const card = document.createElement('div');
    card.className = 'product-card';
    card.innerHTML = `
      <h3>${p.name}</h3>
      <p>${p.category}</p>
      <p class="price">Rs.${p.price.toFixed(2)}</p>
      <p>Stock: ${p.stock}</p>
      <input type="number" min="1" value="1" id="qty-${p.id}">
      <button onclick="addToCart(${p.id})">Add to Cart</button>
    `;
    productGrid.appendChild(card);
  });
}

// ----- Cart actions -----
async function addToCart(productId) {
  const qtyInput = document.getElementById(`qty-${productId}`);
  const quantity = parseInt(qtyInput.value) || 1;

  const res = await fetch('/api/cart/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId, quantity })
  });
  const data = await res.json();
  renderCart(data.cart);
  loadProducts(); // refresh stock numbers
}

async function removeFromCart(productId) {
  const res = await fetch('/api/cart/remove', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ productId })
  });
  const cart = await res.json();
  renderCart(cart);
}

async function loadCart() {
  const res = await fetch('/api/cart');
  const cart = await res.json();
  renderCart(cart);
}

function renderCart(cart) {
  cartItemsDiv.innerHTML = '';
  let count = 0;
  cart.items.forEach(item => {
    count += item.quantity;
    const div = document.createElement('div');
    div.className = 'cart-item';
    div.innerHTML = `
      <span>${item.name} x${item.quantity}</span>
      <span>Rs.${item.lineTotal.toFixed(2)}
        <button onclick="removeFromCart(${item.productId})">x</button>
      </span>
    `;
    cartItemsDiv.appendChild(div);
  });
  cartSubtotalEl.textContent = `Subtotal: Rs.${cart.subtotal.toFixed(2)}`;
  cartCountEl.textContent = count;
}

// ----- Checkout -----
async function checkout() {
  const res = await fetch('/api/checkout', { method: 'POST' });
  const data = await res.json();

  if (!res.ok) {
    orderConfirmationDiv.textContent = data.error || 'Checkout failed.';
    orderConfirmationDiv.style.color = '#e74c3c';
    return;
  }

  orderConfirmationDiv.style.color = '#27ae60';
  orderConfirmationDiv.innerHTML = `Order #${data.orderId} confirmed! Total: Rs.${data.total.toFixed(2)}`;
  loadCart();
  loadProducts();
}

// ----- Order history -----
async function loadOrderHistory() {
  const res = await fetch('/api/orders');
  const orders = await res.json();

  orderHistoryList.innerHTML = '';
  if (orders.length === 0) {
    orderHistoryList.textContent = 'No orders yet.';
    return;
  }
  orders.forEach(o => {
    const div = document.createElement('div');
    div.className = 'order-entry';
    div.textContent = `Order #${o.orderId} - Rs.${o.total.toFixed(2)} - ${o.status}`;
    orderHistoryList.appendChild(div);
  });
}

// ----- Event wiring -----
document.getElementById('cartToggleBtn').addEventListener('click', () => {
  cartPanel.classList.toggle('hidden');
});
document.getElementById('checkoutBtn').addEventListener('click', checkout);
document.getElementById('orderHistoryBtn').addEventListener('click', loadOrderHistory);

// ----- Initial load -----
loadProducts();
loadCart();
