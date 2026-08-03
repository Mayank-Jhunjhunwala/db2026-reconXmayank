// TICKET-ADV114 — Compound DataTable.
// TICKET-ADV117 — useDebouncedSearch.
import React, { useState, useEffect, useCallback } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';
import { TradeRow } from '@components/TradeRow.jsx';

function Trades() {
  const [search, setSearch] = useState('');
  const debounced = useDebouncedSearch(search, 300);
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ items: [], totalPages: 0 });
  const [selectedId, setSelectedId] = useState(null);

  useEffect(() => {
    let active = true;
    const query = new URLSearchParams({ page, size: 20 });
    if (debounced) query.set('status', debounced);
    
    api.listTrades('?' + query.toString()).then((res) => {
      if (active) setData(res);
    }).catch(() => {
      if (active) setData({ items: [], totalPages: 0 });
    });
    return () => { active = false; };
  }, [page, debounced]);

  const handleSelect = useCallback((id) => setSelectedId(id), []);

  return (
    <section>
      <h2>Trades</h2>
      <input
        aria-label="Filter by status"
        placeholder="status filter (PENDING/MATCHED/…)"
        value={search}
        onChange={(e) => setSearch(e.target.value.toUpperCase())}
      />
      <DataTable>
        <DataTable.Header columns={[
          { key: 'tradeRef', label: 'Ref' },
          { key: 'symbol',   label: 'Symbol' },
          { key: 'qty',      label: 'Qty' },
          { key: 'price',    label: 'Price' },
          { key: 'status',   label: 'Status' },
        ]} />
        <DataTable.Body 
          rows={data.items} 
          render={(t) => <TradeRow key={t.id} trade={t} onClick={handleSelect} />} 
        />
        <DataTable.Pagination
          page={page}
          totalPages={Math.max(1, data.totalPages)}
          onChange={setPage}
        />
      </DataTable>
    </section>
  );
}

export default withAuth(Trades);
