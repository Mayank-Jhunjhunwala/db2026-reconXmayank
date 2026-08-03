// File: static-dashboard/js/sse.js
// TICKET-ADV104 — EventSource live feed with prepend + slide-in animation.

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

  function prepend(trade) {
    const el = document.createElement('article');
    el.className = 'trade-card trade-card--new trade-card--' + trade.status.toLowerCase();
    el.innerHTML = `
      <strong>${trade.tradeRef}</strong>
      <span> ${trade.symbol} </span>
      <span> qty=${trade.qty} </span>
      <span> price=${trade.price} </span>
      <span> [${trade.status}]</span>`;
    FEED_EL.prepend(el);
  }

  function connect() {
    sse = new EventSource(STREAM_URL);

    sse.onopen = () => {
      connectionStatus = 'open';
      updateConnectionBadge('Live', 'sse-status sse-status--live');
    };

    sse.onmessage = (event) => {
      try {
        const trade = JSON.parse(event.data);
        prepend(trade);
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
