// TICKET-ADV123 — React Hook Form + Yup validation.
import React from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

const schema = yup.object({
  tradeRef:         yup.string().matches(/^[A-Z]{3}-\d{8}-\d{4}$/, 'AAA-YYYYMMDD-NNNN').required(),
  instrumentId:     yup.number().integer().positive().typeError('Must be a number').required(),
  counterpartyId:   yup.number().integer().positive().typeError('Must be a number').required(),
  assetClass:       yup.string().oneOf(['EQUITY','FX','BOND','DERIVATIVE']).required(),
  side:             yup.string().oneOf(['BUY','SELL']).required(),
  quantity:         yup.number().positive().typeError('Must be a number').required(),
  price:            yup.number().positive().typeError('Must be a number').required(),
  tradeDate:        yup.date().typeError('Must be a valid date').required(),
});

function AddTrade() {
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } =
        useForm({ 
          resolver: yupResolver(schema), 
          mode: 'onBlur',
          defaultValues: { tradeRef: '', instrumentId: '', counterpartyId: '', assetClass: 'EQUITY', side: 'BUY', quantity: '', price: '', tradeDate: '' }
        });

  async function onSubmit(values) {
    try {
      await api.createTrade(values);
      reset();
    } catch (e) {
      alert('Failed to submit: ' + e.message);
    }
  }

  return (
    <section>
      <h2>Add trade</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="trade-form" noValidate>
        <label>Trade ref   <input {...register('tradeRef')} placeholder="EQU-20260603-0001" /></label>
        {errors.tradeRef && <p className="form-error" role="alert">{errors.tradeRef.message}</p>}

        <label>Instrument id   <input type="number" {...register('instrumentId')} /></label>
        {errors.instrumentId && <p className="form-error" role="alert">{errors.instrumentId.message}</p>}

        <label>Counterparty id <input type="number" {...register('counterpartyId')} /></label>
        {errors.counterpartyId && <p className="form-error" role="alert">{errors.counterpartyId.message}</p>}

        <label>Asset class    <select {...register('assetClass')}>
          <option value="EQUITY">EQUITY</option><option value="FX">FX</option>
          <option value="BOND">BOND</option><option value="DERIVATIVE">DERIVATIVE</option>
        </select></label>
        {errors.assetClass && <p className="form-error" role="alert">{errors.assetClass.message}</p>}

        <label>Side <select {...register('side')}>
          <option value="BUY">BUY</option><option value="SELL">SELL</option>
        </select></label>
        {errors.side && <p className="form-error" role="alert">{errors.side.message}</p>}

        <label>Quantity  <input type="number" step="0.0001" {...register('quantity')} /></label>
        {errors.quantity && <p className="form-error" role="alert">{errors.quantity.message}</p>}

        <label>Price     <input type="number" step="0.0001" {...register('price')} /></label>
        {errors.price && <p className="form-error" role="alert">{errors.price.message}</p>}

        <label>Trade date<input type="date" {...register('tradeDate')} /></label>
        {errors.tradeDate && <p className="form-error" role="alert">{errors.tradeDate.message}</p>}

        <button disabled={isSubmitting} type="submit">Submit</button>
      </form>
    </section>
  );
}

export default withAuth(AddTrade);
