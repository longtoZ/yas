import { EDeliveryMethod } from '@/modules/order/models/EDeliveryMethod';
import { EDeliveryStatus } from '@/modules/order/models/EDeliveryStatus';
import { EOrderStatus } from '@/modules/order/models/EOrderStatus';
import {
  getDeliveryMethodTitle,
  getDeliveryStatusTitle,
  getOrderStatusTitle,
} from './orderUtil';

describe('getOrderStatusTitle', () => {
  it.each([
    [EOrderStatus.PENDING, 'Pending'],
    [EOrderStatus.ACCEPTED, 'Accepted'],
    [EOrderStatus.COMPLETED, 'Completed'],
    [EOrderStatus.CANCELLED, 'Cancelled'],
    [EOrderStatus.PENDING_PAYMENT, 'Pending Payment'],
    [EOrderStatus.PAID, 'Paid'],
    [EOrderStatus.REFUND, 'Refund'],
    [EOrderStatus.SHIPPING, 'Shipping'],
    [EOrderStatus.REJECT, 'Reject'],
  ])('returns "%s" for status %s', (status, expected) => {
    expect(getOrderStatusTitle(status)).toBe(expected);
  });

  it('returns "All" for null status', () => {
    expect(getOrderStatusTitle(null)).toBe('All');
  });
});

describe('getDeliveryMethodTitle', () => {
  it.each([
    [EDeliveryMethod.GRAB_EXPRESS, 'Grab Express'],
    [EDeliveryMethod.VIETTEL_POST, 'Viettel Post'],
    [EDeliveryMethod.SHOPEE_EXPRESS, 'Shopee Express'],
    [EDeliveryMethod.YAS_EXPRESS, 'Yas Express'],
  ])('returns "%s" for method %s', (method, expected) => {
    expect(getDeliveryMethodTitle(method)).toBe(expected);
  });

  it('returns "Preparing" for unknown method', () => {
    expect(getDeliveryMethodTitle('UNKNOWN' as EDeliveryMethod)).toBe('Preparing');
  });
});

describe('getDeliveryStatusTitle', () => {
  it.each([
    [EDeliveryStatus.CANCELLED, 'Cancelled'],
    [EDeliveryStatus.DELIVERED, 'Delivered'],
    [EDeliveryStatus.DELIVERING, 'Delivering'],
    [EDeliveryStatus.PENDING, 'Pending'],
  ])('returns "%s" for status %s', (status, expected) => {
    expect(getDeliveryStatusTitle(status)).toBe(expected);
  });

  it('returns "Preparing" for unknown status', () => {
    expect(getDeliveryStatusTitle('UNKNOWN' as EDeliveryStatus)).toBe('Preparing');
  });
});
