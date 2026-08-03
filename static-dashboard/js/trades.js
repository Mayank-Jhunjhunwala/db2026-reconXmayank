// File: static-dashboard/js/trades.js
// TICKET-ADV106 — sortable, resizable, frozen-header data table.

(function () {
  const TABLE_EL = document.getElementById('trades-table');
  if (!TABLE_EL) return;

  const TBODY_EL = document.getElementById('trades-tbody');
  const HEADERS = Array.from(TABLE_EL.querySelectorAll('thead th'));

  let rows = [];

  function escapeHtml(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function renderRows() {
    TBODY_EL.innerHTML = rows.map(r => `
      <tr>
        <td>${escapeHtml(r.tradeRef)}</td>
        <td>${escapeHtml(r.symbol)}</td>
        <td>${escapeHtml(r.quantity)}</td>
        <td>${escapeHtml(r.price)}</td>
        <td>${escapeHtml(r.status)}</td>
      </tr>`).join('');
  }

  // ---- Sorting ----

  HEADERS.forEach(th => {
    th.addEventListener('click', (e) => {
      if (e.target.classList.contains('resize-handle')) return; // ignore resize clicks

      const col = th.dataset.col;
      const type = th.dataset.type || 'string';
      const dir = th.getAttribute('aria-sort') === 'ascending' ? 'descending' : 'ascending';

      // clear all, set this one
      HEADERS.forEach(o => o.removeAttribute('aria-sort'));
      th.setAttribute('aria-sort', dir);

      const mult = dir === 'ascending' ? 1 : -1;
      rows.sort((a, b) => {
        const av = a[col], bv = b[col];
        if (type === 'number') return (Number(av) - Number(bv)) * mult;
        return String(av).localeCompare(String(bv)) * mult;
      });

      renderRows();
    });
  });

  // ---- Column resize ----

  TABLE_EL.querySelectorAll('.resize-handle').forEach(handle => {
    handle.addEventListener('mousedown', (e) => {
      e.preventDefault();
      const th = handle.closest('th');
      const startX = e.clientX;
      const startWidth = th.offsetWidth;

      // Listen on DOCUMENT so the drag survives leaving the handle.
      function onMove(ev) { th.style.width = (startWidth + ev.clientX - startX) + 'px'; }
      function onUp() {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
      }

      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    });
  });

  // ---- Initial data load ----

  fetch('/api/v1/trades?size=200')
    .then(res => res.json())
    .then(data => {
      rows = data.content || data;
      renderRows();
    })
    .catch(err => console.error('Failed to load trades', err));
})();
