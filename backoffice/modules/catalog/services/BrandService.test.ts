import apiClientService from '@commonServices/ApiClientService';
import {
  createBrand,
  deleteBrand,
  editBrand,
  getBrand,
  getBrands,
  getPageableBrands,
} from './BrandService';

jest.mock('@commonServices/ApiClientService', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('BrandService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('gets all brands', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    const data = await getBrands();

    expect(apiClientService.get).toHaveBeenCalledWith('/api/product/backoffice/brands');
    expect(data).toEqual([{ id: 1 }]);
  });

  it('gets pageable brands', async () => {
    const json = jest.fn().mockResolvedValue({ content: [] });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    await getPageableBrands(2, 10);

    expect(apiClientService.get).toHaveBeenCalledWith(
      '/api/product/backoffice/brands/paging?pageNo=2&pageSize=10'
    );
  });

  it('creates a brand', async () => {
    (apiClientService.post as jest.Mock).mockResolvedValue({ status: 201 });

    await createBrand({ id: 1, name: 'A' } as never);

    expect(apiClientService.post).toHaveBeenCalledWith(
      '/api/product/backoffice/brands',
      JSON.stringify({ id: 1, name: 'A' })
    );
  });

  it('gets one brand by id', async () => {
    const json = jest.fn().mockResolvedValue({ id: 7 });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    const data = await getBrand(7);

    expect(apiClientService.get).toHaveBeenCalledWith('/api/product/backoffice/brands/7');
    expect(data).toEqual({ id: 7 });
  });

  it('deleteBrand returns response when 204', async () => {
    const response = { status: 204 };
    (apiClientService.delete as jest.Mock).mockResolvedValue(response);

    const data = await deleteBrand(9);

    expect(data).toBe(response);
  });

  it('deleteBrand returns error json when non-204', async () => {
    const json = jest.fn().mockResolvedValue({ title: 'BAD_REQUEST' });
    (apiClientService.delete as jest.Mock).mockResolvedValue({ status: 400, json });

    const data = await deleteBrand(9);

    expect(data).toEqual({ title: 'BAD_REQUEST' });
  });

  it('editBrand returns response when 204', async () => {
    const response = { status: 204 };
    (apiClientService.put as jest.Mock).mockResolvedValue(response);

    const data = await editBrand(2, { name: 'Updated' } as never);

    expect(apiClientService.put).toHaveBeenCalledWith(
      '/api/product/backoffice/brands/2',
      JSON.stringify({ name: 'Updated' })
    );
    expect(data).toBe(response);
  });

  it('editBrand returns json when non-204', async () => {
    const json = jest.fn().mockResolvedValue({ detail: 'failed' });
    (apiClientService.put as jest.Mock).mockResolvedValue({ status: 400, json });

    const data = await editBrand(2, { name: 'Updated' } as never);

    expect(data).toEqual({ detail: 'failed' });
  });
});
