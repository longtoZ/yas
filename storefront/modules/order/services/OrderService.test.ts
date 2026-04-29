import apiClientService from '@/common/services/ApiClientService';
import { EOrderStatus } from '../models/EOrderStatus';
import { createCheckout, createOrder, getCheckoutById, getMyOrders } from './OrderService';

jest.mock('@/common/services/ApiClientService', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
  },
}));

describe('OrderService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('createOrder returns order when response is 2xx', async () => {
    const json = jest.fn().mockResolvedValue({ id: 'order-1' });
    (apiClientService.post as jest.Mock).mockResolvedValue({ status: 201, json });

    const result = await createOrder({ id: 'order-1' } as never);

    expect(apiClientService.post).toHaveBeenCalledWith(
      '/api/order/storefront/orders',
      JSON.stringify({ id: 'order-1' })
    );
    expect(result).toEqual({ id: 'order-1' });
  });

  it('createOrder throws when response is non-2xx', async () => {
    (apiClientService.post as jest.Mock).mockResolvedValue({ status: 400, statusText: 'Bad Request' });

    await expect(createOrder({} as never)).rejects.toThrow('Bad Request');
  });

  it('getMyOrders returns list when 2xx', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getMyOrders('shirt', EOrderStatus.PENDING);

    expect(apiClientService.get).toHaveBeenCalledWith(
      '/api/order/storefront/orders/my-orders?productName=shirt&orderStatus=PENDING'
    );
    expect(result).toEqual([{ id: 1 }]);
  });

  it('getMyOrders uses empty string when orderStatus is null', async () => {
    const json = jest.fn().mockResolvedValue([]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    await getMyOrders('', null);

    expect(apiClientService.get).toHaveBeenCalledWith(
      '/api/order/storefront/orders/my-orders?productName=&orderStatus='
    );
  });

  it('createCheckout returns checkout when 2xx', async () => {
    const json = jest.fn().mockResolvedValue({ id: 'chk-1' });
    (apiClientService.post as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await createCheckout({ id: 'chk-1' } as never);

    expect(result).toEqual({ id: 'chk-1' });
  });

  it('createCheckout throws when non-2xx', async () => {
    (apiClientService.post as jest.Mock).mockResolvedValue({ status: 500, statusText: 'Server Error' });

    await expect(createCheckout({} as never)).rejects.toThrow('Server Error');
  });

  it('getCheckoutById returns checkout when 2xx', async () => {
    const json = jest.fn().mockResolvedValue({ id: 'chk-99' });
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getCheckoutById('chk-99');

    expect(result).toEqual({ id: 'chk-99' });
  });

  it('getCheckoutById throws when non-2xx', async () => {
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 404, statusText: 'Not Found' });

    await expect(getCheckoutById('bad-id')).rejects.toThrow('Not Found');
  });
});
