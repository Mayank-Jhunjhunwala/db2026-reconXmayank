import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import Dashboard from './Dashboard.jsx';

vi.mock('@components/withAuth.jsx', () => ({
  withAuth: (Component) => Component
}));

vi.mock('@hooks/useTradeStream.js', () => ({
  useTradeStream: () => ({
    trades: [
      { id: 1, quantity: 10, price: 100, status: 'MATCHED' },
      { id: 2, quantity: 5, price: 50, status: 'UNMATCHED' },
    ],
    isConnected: true,
  }),
}));

describe('Dashboard', () => {
  it('renders summary cards correctly', () => {
    render(<Dashboard />);
    expect(screen.getByText('Trades streamed')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();

    expect(screen.getByText('Matched')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();

    expect(screen.getByText('Portfolio value (USD)')).toBeInTheDocument();
    expect(screen.getByText('1,250')).toBeInTheDocument();
  });
});
