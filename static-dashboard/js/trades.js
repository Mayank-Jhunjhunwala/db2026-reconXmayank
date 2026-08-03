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
        <td>${escapeHtml(r.qty)}</td>
        <td>${escapeHtml(r.price)}</td>
        <td>${escapeHtml(r.status)}</td>
      </tr>`).join('');
  }

  // ---- Sorting ----

  function clearOtherSortIndicators(activeTh) {
    HEADERS.forEach(th => {
      if (th !== activeTh) th.removeAttribute('aria-sort');
    });
  }

  function handleHeaderClick(e) {
    if (e.target.classList.contains('resize-handle')) return;

    const th = e.currentTarget;
    const col = th.dataset.col;
    const type = th.dataset.type;

    const currentDir = th.dataset.dir === 'asc' ? 'desc' : 'asc';
    th.dataset.dir = currentDir;
    const multiplier = currentDir === 'asc' ? 1 : -1;

    clearOtherSortIndicators(th);
    th.setAttribute('aria-sort', currentDir === 'asc' ? 'ascending' : 'descending');

    rows.sort((a, b) => {
      if (type === 'number') {
        return (Number(a[col]) - Number(b[col])) * multiplier;
      }
      return String(a[col]).localeCompare(String(b[col])) * multiplier;
    });

    renderRows();
  }

  HEADERS.forEach(th => th.addEventListener('click', handleHeaderClick));

  // ---- Column resize ----

  function handleResizeMousedown(e) {
    const handle = e.target;
    const th = handle.closest('th');
    const startX = e.clientX;
    const startWidth = th.offsetWidth;

    function onMouseMove(moveEvent) {
      th.style.width = (startWidth + moveEvent.clientX - startX) + 'px';
    }

    function onMouseUp() {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    }

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  }

  TABLE_EL.querySelectorAll('.resize-handle').forEach(handle => {
    handle.addEventListener('mousedown', handleResizeMousedown);
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
