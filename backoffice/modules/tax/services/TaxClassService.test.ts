import apiClientService from '@commonServices/ApiClientService';
import {
  createTaxClass,
  deleteTaxClass,
  editTaxClass,
  getPageableTaxClasses,
  getTaxClass,
  getTaxClasses,
} from './TaxClassService';

jest.mock('@commonServices/ApiClientService', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('TaxClassService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('gets tax classes', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    const data = await getTaxClasses();

    expect(apiClientService.get).toHaveBeenCalledWith('/api/tax/backoffice/tax-classes');
    expect(data).toEqual([{ id: 1 }]);
  });

  it('gets pageable tax classes', async () => {
    const json = jest.fn().mockResolvedValue({ content: [] });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    await getPageableTaxClasses(3, 5);

    expect(apiClientService.get).toHaveBeenCalledWith(
      '/api/tax/backoffice/tax-classes/paging?pageNo=3&pageSize=5'
    );
  });

  it('creates tax class', async () => {
    (apiClientService.post as jest.Mock).mockResolvedValue({ status: 201 });

    await createTaxClass({ id: 1, name: 'TAX' } as never);

    expect(apiClientService.post).toHaveBeenCalledWith(
      '/api/tax/backoffice/tax-classes',
      JSON.stringify({ id: 1, name: 'TAX' })
    );
  });

  it('gets one tax class', async () => {
    const json = jest.fn().mockResolvedValue({ id: 3 });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    const data = await getTaxClass(3);

    expect(apiClientService.get).toHaveBeenCalledWith('/api/tax/backoffice/tax-classes/3');
    expect(data).toEqual({ id: 3 });
  });

  it('deleteTaxClass handles 204 and non-204', async () => {
    const response204 = { status: 204 };
    (apiClientService.delete as jest.Mock).mockResolvedValueOnce(response204);
    const json = jest.fn().mockResolvedValue({ title: 'BAD_REQUEST' });
    (apiClientService.delete as jest.Mock).mockResolvedValueOnce({ status: 400, json });

    const okResult = await deleteTaxClass(1);
    const badResult = await deleteTaxClass(1);

    expect(okResult).toBe(response204);
    expect(badResult).toEqual({ title: 'BAD_REQUEST' });
  });

  it('editTaxClass handles 204 and non-204', async () => {
    const response204 = { status: 204 };
    (apiClientService.put as jest.Mock).mockResolvedValueOnce(response204);
    const json = jest.fn().mockResolvedValue({ detail: 'failed' });
    (apiClientService.put as jest.Mock).mockResolvedValueOnce({ status: 400, json });

    const okResult = await editTaxClass(1, { name: 'TAX' } as never);
    const badResult = await editTaxClass(1, { name: 'TAX' } as never);

    expect(okResult).toBe(response204);
    expect(badResult).toEqual({ detail: 'failed' });
  });
});
