const $ = (selector) => document.querySelector(selector);
const money = (value) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value));
const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[character]);
const availableIcons = new Set(['burger', 'ramen', 'pizza', 'fries', 'dumpling', 'pasta', 'dessert', 'plate', 'bag']);
const svgIcon = (name) => `<svg aria-hidden="true"><use href="icons.svg#${availableIcons.has(name) ? name : 'plate'}"></use></svg>`;
const statuses = ['PENDENTE', 'CONFIRMADO', 'EM_PREPARO', 'SAIU_PARA_ENTREGA', 'ENTREGUE'];
const statusLabels = { PENDENTE: 'Recebido', CONFIRMADO: 'Confirmado', EM_PREPARO: 'Em preparo', SAIU_PARA_ENTREGA: 'A caminho', ENTREGUE: 'Entregue', CANCELADO: 'Cancelado' };
const apiBase = new URLSearchParams(location.search).get('api')?.replace(/\/$/, '') || '';
const state = { demo: !apiBase && (location.hostname.endsWith('github.io') || new URLSearchParams(location.search).has('demo')), restaurants: [], dishes: [], orders: [], cart: new Map(), restaurantId: null, category: 'Todos', query: '', metadata: null };
const apiUrl = (path) => `${apiBase}${path}`;
let toastTimer;

function toast(message) {
  const element = $('#toast');
  element.textContent = message;
  element.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => element.classList.remove('show'), 4000);
}

async function request(url, options = {}) {
  const response = await fetch(url, { ...options, headers: { Accept: 'application/json', ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers } });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || error.detail || `Erro ${response.status}`);
  }
  const body = await response.text();
  return body ? JSON.parse(body) : null;
}

async function loadCatalog() {
  state.metadata = await request('demo-data.json');
  if (state.demo) {
    useDemoCatalog();
    return;
  }
  try {
    const [restaurants, dishes] = await Promise.all([request(apiUrl('/restaurante')), request(apiUrl('/prato'))]);
    state.restaurants = restaurants.filter((restaurant) => restaurant.ativo);
    state.dishes = dishes.filter((dish) => dish.ativo && dish.disponivel);
  } catch (error) {
    state.demo = true;
    useDemoCatalog();
    toast('API local indisponível. O catálogo de demonstração foi carregado.');
  }
}

function useDemoCatalog() {
  state.restaurants = state.metadata.restaurantes.map((restaurant, index) => ({ ...restaurant, id: index + 1, ativo: true }));
  let nextId = 1;
  state.dishes = state.restaurants.flatMap((restaurant) => restaurant.pratos.map((dish) => ({ ...dish, id: nextId++, restaurante: { id: restaurant.id }, ativo: true, disponivel: true })));
  try { state.orders = JSON.parse(localStorage.getItem('delivery-demo-orders') || '[]'); }
  catch { state.orders = []; localStorage.removeItem('delivery-demo-orders'); }
}

function restaurantMeta(restaurant) {
  if (!restaurant) return {};
  return state.metadata.restaurantes.find((item) => item.nome === restaurant.nome) || {};
}

function dishMeta(dish) {
  return state.metadata.restaurantes.flatMap((restaurant) => restaurant.pratos).find((item) => item.nome === dish.nome) || {};
}

function currentRestaurant() { return state.restaurants.find((restaurant) => restaurant.id === state.restaurantId); }
function currentDishes() { return state.dishes.filter((dish) => dish.restaurante?.id === state.restaurantId); }
function cartSubtotal() { return [...state.cart.values()].reduce((sum, item) => sum + Number(item.dish.preco) * item.quantity, 0); }
function cartCount() { return [...state.cart.values()].reduce((sum, item) => sum + item.quantity, 0); }

function renderRestaurants() {
  $('#restaurant-list').innerHTML = state.restaurants.map((restaurant) => {
    const meta = restaurantMeta(restaurant);
    return `<button class="restaurant-card ${restaurant.id === state.restaurantId ? 'active' : ''}" data-restaurant="${restaurant.id}" type="button" aria-pressed="${restaurant.id === state.restaurantId}"><div class="restaurant-art" style="--card-color:${meta.cor || '#d2ded5'}"><small>${escapeHtml(meta.selo || 'Aberto agora')}</small><span class="restaurant-icon" aria-hidden="true">${svgIcon(meta.icone)}</span></div><div class="restaurant-body"><h3>${escapeHtml(restaurant.nome)}</h3><p>${escapeHtml(restaurant.especialidade || 'Sabores para descobrir')}</p><div class="restaurant-foot"><span class="restaurant-time"><svg aria-hidden="true"><use href="icons.svg#clock"></use></svg>${currentRestaurantTime(restaurant.id)} min em média</span><strong>Ver cardápio →</strong></div></div></button>`;
  }).join('') || '<p>Nenhum restaurante disponível no momento.</p>';
  $('#mode-badge').textContent = state.demo ? '● Modo demonstração' : '● API Spring Boot conectada';
}

function currentRestaurantTime(id) {
  const times = state.dishes.filter((dish) => dish.restaurante?.id === id).map((dish) => Number(dish.tempoPreparoMinutos || 25));
  return times.length ? Math.max(...times) : 25;
}

function renderMenu() {
  const restaurant = currentRestaurant();
  $('#menu-subtitle').textContent = restaurant ? `Escolhas do ${restaurant.nome}. Adicione seus favoritos à sacola.` : 'Nenhum restaurante disponível.';
  const categories = ['Todos', ...new Set(currentDishes().map((dish) => dish.categoria || 'Outros'))];
  $('#category-filters').innerHTML = categories.map((category) => `<button class="filter-chip ${category === state.category ? 'active' : ''}" type="button" data-category="${escapeHtml(category)}">${escapeHtml(category)}</button>`).join('');
  const filtered = currentDishes().filter((dish) => (state.category === 'Todos' || (dish.categoria || 'Outros') === state.category) && dish.nome.toLocaleLowerCase('pt-BR').includes(state.query));
  $('#dish-list').innerHTML = filtered.map((dish) => {
    const meta = dishMeta(dish);
    return `<article class="dish-card"><div class="dish-art" style="--dish-color:${restaurantMeta(restaurant).cor || '#dfe7db'}"><small>${escapeHtml(dish.categoria || 'Especial')}</small><span class="dish-icon" role="img" aria-label="${escapeHtml(dish.nome)}">${svgIcon(meta.icone)}</span></div><div class="dish-body"><h3>${escapeHtml(dish.nome)}</h3><p>${escapeHtml(dish.descricao || 'Preparado especialmente para você.')}</p><div class="dish-bottom"><strong>${money(dish.preco)}</strong><button class="add-button" data-add="${dish.id}" type="button" aria-label="Adicionar ${escapeHtml(dish.nome)} à sacola">+</button></div></div></article>`;
  }).join('') || '<div class="empty-orders">Nenhum prato encontrado. Experimente outra busca.</div>';
}

function renderCart() {
  const entries = [...state.cart.values()];
  $('#cart-items').innerHTML = entries.length ? entries.map(({ dish, quantity }) => `<div class="cart-item"><span class="cart-item-art" aria-hidden="true">${svgIcon(dishMeta(dish).icone)}</span><div class="cart-item-main"><strong>${escapeHtml(dish.nome)}</strong><small>${money(dish.preco)} cada</small></div><div class="quantity-control"><button type="button" data-quantity="${dish.id}" data-delta="-1" aria-label="Remover uma unidade de ${escapeHtml(dish.nome)}">−</button><b>${quantity}</b><button type="button" data-quantity="${dish.id}" data-delta="1" aria-label="Adicionar uma unidade de ${escapeHtml(dish.nome)}">+</button></div></div>`).join('') : `<div class="empty-cart">${svgIcon('bag')}Sua sacola está esperando algo gostoso.<br>Escolha um prato para começar.</div>`;
  $('#cart-total').textContent = money(cartSubtotal());
  $('#checkout-total').textContent = money(cartSubtotal());
  $('#cart-count').textContent = cartCount();
  $('#cart-items-count').textContent = `${cartCount()} ${cartCount() === 1 ? 'item' : 'itens'}`;
  $('#checkout-button').disabled = entries.length === 0;
}

function addDish(id) {
  const dish = currentDishes().find((item) => item.id === id);
  if (!dish) return;
  const existing = state.cart.get(id);
  state.cart.set(id, { dish, quantity: (existing?.quantity || 0) + 1 });
  renderCart();
  toast(`${dish.nome} adicionado à sacola.`);
}

function changeQuantity(id, delta) {
  const entry = state.cart.get(id);
  if (!entry) return;
  const next = entry.quantity + delta;
  if (next <= 0) state.cart.delete(id);
  else state.cart.set(id, { ...entry, quantity: next });
  renderCart();
}

function selectRestaurant(id) {
  if (id === state.restaurantId) { $('#cardapio').scrollIntoView({ behavior: 'smooth' }); return; }
  if (state.cart.size && !confirm('Trocar de restaurante vai limpar a sacola atual. Continuar?')) return;
  state.cart.clear();
  state.restaurantId = id;
  state.category = 'Todos';
  state.query = '';
  $('#dish-search').value = '';
  renderRestaurants(); renderMenu(); renderCart();
  $('#cardapio').scrollIntoView({ behavior: 'smooth' });
}

async function lookupCep() {
  const cep = $('#checkout-cep').value.replace(/\D/g, '');
  if (cep.length !== 8) { $('#cep-message').textContent = 'Digite um CEP com 8 números.'; return; }
  $('#cep-message').textContent = 'Consultando CEP…';
  $('#estimate-box').innerHTML = '<span>Estimativa de entrega</span><strong>Calculando rota…</strong><small>O valor exibido é apenas informativo.</small>';
  try {
    const address = state.demo ? await request(`https://viacep.com.br/ws/${cep}/json/`) : await request(apiUrl(`/api/cep/${cep}`));
    if (address.erro) throw new Error('CEP não encontrado.');
    const street = [address.logradouro, address.bairro].filter(Boolean).join(', ');
    $('#checkout-street').value = street;
    $('#checkout-city').value = `${address.localidade} - ${address.uf}`;
    $('#cep-message').textContent = street ? 'Endereço encontrado. Confira o número e os dados.' : 'CEP encontrado. Complete o endereço manualmente.';
    await estimateDelivery(cep);
  } catch (error) {
    $('#cep-message').textContent = `${error.message} Você pode preencher o endereço manualmente.`;
    $('#estimate-box').innerHTML = '<span>Estimativa de entrega</span><strong>Indisponível para este CEP</strong><small>O pedido pode continuar sem o cálculo informativo.</small>';
  }
}

async function estimateDelivery(cep) {
  try {
    let estimate;
    if (state.demo) {
      const geo = await request(`https://brasilapi.com.br/api/cep/v2/${cep}`);
      const coordinates = geo.location?.coordinates;
      if (!coordinates?.latitude || !coordinates?.longitude) throw new Error('Coordenadas indisponíveis.');
      const restaurant = currentRestaurant();
      const url = `https://router.project-osrm.org/route/v1/driving/${restaurant.longitude},${restaurant.latitude};${coordinates.longitude},${coordinates.latitude}?overview=false`;
      const route = (await request(url)).routes?.[0];
      if (!route) throw new Error('Rota indisponível.');
      estimate = { distanciaKm: route.distance / 1000, duracaoMinutos: Math.ceil(route.duration / 60), frete: 4.9 + 1.8 * route.distance / 1000, fonte: 'OSRM/OpenStreetMap' };
    } else {
      estimate = await request(apiUrl(`/api/entrega/estimativa?restauranteId=${state.restaurantId}&cep=${cep}`));
    }
    $('#estimate-box').innerHTML = `<span>Estimativa de entrega</span><strong>${Number(estimate.distanciaKm).toFixed(1).replace('.', ',')} km · ${estimate.duracaoMinutos} min · ${money(estimate.frete)}</strong><small>Rota: ${escapeHtml(estimate.fonte)}. Frete informativo, não cobrado no pedido.</small>`;
  } catch (error) {
    $('#estimate-box').innerHTML = '<span>Estimativa de entrega</span><strong>Rota indisponível agora</strong><small>O pedido pode continuar. Frete não é cobrado.</small>';
  }
}

async function submitOrder(event) {
  event.preventDefault();
  if (!state.cart.size) return;
  const submit = $('#submit-order');
  const form = new FormData(event.currentTarget);
  const address = `${form.get('rua')}, ${form.get('numero')}${form.get('complemento') ? ', ' + form.get('complemento') : ''}, ${form.get('cidade')} — CEP ${form.get('cep')}`;
  const payload = { nomeCliente: String(form.get('nomeCliente')).trim(), telefoneCliente: String(form.get('telefoneCliente')).trim(), enderecoEntrega: address, itens: [...state.cart.values()].map(({ dish, quantity }) => ({ pratoId: dish.id, quantidade: quantity })) };
  submit.disabled = true;
  submit.textContent = 'Confirmando…';
  try {
    if (state.demo) {
      const order = { ...payload, id: Date.now(), restauranteId: state.restaurantId, itens: [...state.cart.values()].map(({ dish, quantity }) => ({ pratoId: dish.id, nomePrato: dish.nome, quantidade: quantity, precoUnitario: Number(dish.preco), subtotal: Number(dish.preco) * quantity })), valorTotal: cartSubtotal(), status: 'PENDENTE', dataHoraPedido: new Date().toISOString() };
      state.orders.unshift(order);
      localStorage.setItem('delivery-demo-orders', JSON.stringify(state.orders));
    } else {
      const order = await request(apiUrl('/pedido'), { method: 'POST', body: JSON.stringify(payload) });
      state.orders.unshift(order);
    }
    state.cart.clear(); renderCart(); renderOrders();
    $('#checkout-dialog').close();
    $('#checkout-form').reset();
    toast('Pedido confirmado! Acompanhe o andamento abaixo.');
    $('#pedidos').scrollIntoView({ behavior: 'smooth' });
  } catch (error) { toast(`Não foi possível criar o pedido: ${error.message}`); }
  finally { submit.disabled = false; submit.innerHTML = 'Confirmar pedido <span aria-hidden="true">→</span>'; }
}

async function loadOrders() {
  if (!state.demo) {
    try { state.orders = await request(apiUrl('/pedido')); }
    catch (error) { toast(`Não foi possível carregar os pedidos: ${error.message}`); }
  }
  renderOrders();
}

function renderOrders() {
  $('#orders-list').innerHTML = state.orders.length ? [...state.orders].sort((a, b) => new Date(b.dataHoraPedido) - new Date(a.dataHoraPedido)).map((order) => {
    const restaurant = state.restaurants.find((item) => item.id === order.restauranteId);
    const next = statuses[statuses.indexOf(order.status) + 1];
    const date = new Date(order.dataHoraPedido).toLocaleString('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
    return `<article class="order-card"><div class="order-card-head"><div><h3>Pedido #${escapeHtml(order.id)}</h3><small>${escapeHtml(restaurant?.nome || 'Restaurante')} · ${date}</small></div><span class="status-pill">${statusLabels[order.status] || escapeHtml(order.status)}</span></div><p>${order.itens.map((item) => `${item.quantidade}× ${escapeHtml(item.nomePrato)}`).join(' · ')}</p><div class="order-card-foot"><strong>${money(order.valorTotal)}</strong><div>${next ? `<button type="button" data-advance="${order.id}" data-status="${next}">${state.demo ? 'Simular' : 'Avançar'} etapa →</button>` : ''}${['PENDENTE', 'CONFIRMADO', 'EM_PREPARO'].includes(order.status) ? ` <button type="button" data-cancel="${order.id}">Cancelar</button>` : ''}</div></div></article>`;
  }).join('') : '<div class="empty-orders">Ainda não há pedidos. Seu próximo favorito pode estar no cardápio acima.</div>';
}

async function changeStatus(id, status) {
  try {
    if (state.demo) {
      const order = state.orders.find((item) => String(item.id) === String(id));
      if (!order) return;
      order.status = status;
      if (status === 'ENTREGUE') order.dataHoraEntrega = new Date().toISOString();
      localStorage.setItem('delivery-demo-orders', JSON.stringify(state.orders));
    } else if (status === 'CANCELADO') {
      await request(apiUrl(`/pedido/${id}`), { method: 'DELETE' });
      await loadOrders();
    } else {
      await request(apiUrl(`/pedido/${id}/status?status=${status}`), { method: 'PATCH' });
      await loadOrders();
    }
    renderOrders();
    toast(`Pedido atualizado: ${statusLabels[status]}.`);
  } catch (error) { toast(error.message); }
}

function bindEvents() {
  $('#restaurant-list').addEventListener('click', (event) => { const card = event.target.closest('[data-restaurant]'); if (card) selectRestaurant(Number(card.dataset.restaurant)); });
  $('#category-filters').addEventListener('click', (event) => { const chip = event.target.closest('[data-category]'); if (chip) { state.category = chip.dataset.category; renderMenu(); } });
  $('#dish-search').addEventListener('input', (event) => { state.query = event.target.value.trim().toLocaleLowerCase('pt-BR'); renderMenu(); });
  $('#dish-list').addEventListener('click', (event) => { const button = event.target.closest('[data-add]'); if (button) addDish(Number(button.dataset.add)); });
  $('#cart-items').addEventListener('click', (event) => { const button = event.target.closest('[data-quantity]'); if (button) changeQuantity(Number(button.dataset.quantity), Number(button.dataset.delta)); });
  $('#checkout-button').addEventListener('click', () => $('#checkout-dialog').showModal());
  $('#close-checkout').addEventListener('click', () => $('#checkout-dialog').close());
  $('#lookup-cep').addEventListener('click', lookupCep);
  $('#checkout-form').addEventListener('submit', submitOrder);
  $('#refresh-orders').addEventListener('click', loadOrders);
  $('#orders-list').addEventListener('click', (event) => { const advance = event.target.closest('[data-advance]'); const cancel = event.target.closest('[data-cancel]'); if (advance) changeStatus(advance.dataset.advance, advance.dataset.status); if (cancel) changeStatus(cancel.dataset.cancel, 'CANCELADO'); });
}

async function start() {
  bindEvents();
  $('#restaurant-list').innerHTML = '<div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div>';
  try {
    await loadCatalog();
    state.restaurantId = state.restaurants[0]?.id || null;
    renderRestaurants(); renderMenu(); renderCart();
    await loadOrders();
  } catch (error) {
    $('#restaurant-list').innerHTML = '<div class="empty-orders">Não foi possível carregar o catálogo. Atualize a página para tentar novamente.</div>';
    toast(error.message);
  }
}
start();
