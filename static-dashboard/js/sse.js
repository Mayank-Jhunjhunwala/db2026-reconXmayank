// File: static-dashboard/js/sse.js
// TICKET-ADV104 — EventSource live feed with prepend + slide-in animation.
// TICKET-ADV105 — SSE handler with prepend-and-animate, escaping, and DOM cap.

(function () {
  const FEED_EL = document.getElementById('trade-feed');
  if (!FEED_EL) return;

  const STREAM_URL = '/api/v1/trades/stream';
  let sse = null;
  let connectionStatus = 'connecting';

  function updateConnectionBadge(text, variant) {
    const badge = document.getElementById('sse-status');
    if (!badge) return;
    badge.textContent = text;
    badge.className = variant;
  }

  // ---- TICKET-ADV105: safe rendering helpers ----

  function escapeHtml(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  const formatQty = new Intl.NumberFormat('en-US');
  const formatPrice = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  });

  function prependTradeRow(trade) {
    let statusModifier = '';
    // NOTE: confirm these match your backend's actual Trade.status values
    // (adjust if your enum uses e.g. BREAK/PENDING instead of UNMATCHED)
    if (trade.status === 'MATCHED') statusModifier = 'trade-card--matched';
    else if (trade.status === 'UNMATCHED' || trade.status === 'BREAK') statusModifier = 'trade-card--break';
    else if (trade.status === 'PENDING') statusModifier = 'trade-card--pending';

    const row = document.createElement('article');
    row.className = 'trade-card ' + statusModifier + ' trade-card--new';
    row.innerHTML = `
      <header class="trade-card__header">
        <strong>${escapeHtml(trade.tradeRef)}</strong>
        <span>${escapeHtml(trade.status)}</span>
      </header>
      <div class="trade-card__body">
        <span>${escapeHtml(trade.symbol)}</span>
        <span>qty=${formatQty.format(trade.qty)}</span>
        <span>price=${formatPrice.format(trade.price)}</span>
      </div>`;

    FEED_EL.prepend(row);
    setTimeout(() => row.classList.remove('trade-card--new'), 500);

    while (FEED_EL.children.length > 50) {
      FEED_EL.lastElementChild.remove();
    }
  }

  // ---- TICKET-ADV104: EventSource connection ----

  function connect() {
    sse = new EventSource(STREAM_URL);

    sse.onopen = () => {
      connectionStatus = 'open';
      updateConnectionBadge('Live', 'sse-status sse-status--live');
    };

    sse.onmessage = (event) => {
      try {
        const trade = JSON.parse(event.data);
        prependTradeRow(trade);
      } catch (err) {
        console.error('Failed to parse SSE trade event', err);
      }
    };

    sse.onerror = () => {
      connectionStatus = 'reconnecting';
      updateConnectionBadge('Reconnecting...', 'sse-status sse-status--reconnecting');
      // Do NOT call connect() here — EventSource auto-reconnects natively.
      // Calling connect() again would open duplicate connections.
    };
  }

  window.addEventListener('beforeunload', () => sse?.close());

  connect();
})();
