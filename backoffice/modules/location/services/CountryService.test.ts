import apiClientService from '@commonServices/ApiClientService';
import {
  createCountry,
  deleteCountry,
  editCountry,
  getCountries,
  getCountry,
  getPageableCountries,
} from './CountryService';

jest.mock('@commonServices/ApiClientService', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('CountryService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('gets countries', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    const data = await getCountries();

    expect(apiClientService.get).toHaveBeenCalledWith('/api/location/backoffice/countries');
    expect(data).toEqual([{ id: 1 }]);
  });

  it('gets pageable countries', async () => {
    const json = jest.fn().mockResolvedValue({ content: [] });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    await getPageableCountries(1, 20);

    expect(apiClientService.get).toHaveBeenCalledWith(
      '/api/location/backoffice/countries/paging?pageNo=1&pageSize=20'
    );
  });

  it('creates country', async () => {
    (apiClientService.post as jest.Mock).mockResolvedValue({ status: 201 });

    await createCountry({ id: 1, name: 'VN' } as never);

    expect(apiClientService.post).toHaveBeenCalledWith(
      '/api/location/backoffice/countries',
      JSON.stringify({ id: 1, name: 'VN' })
    );
  });

  it('gets one country', async () => {
    const json = jest.fn().mockResolvedValue({ id: 8 });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    const data = await getCountry(8);

    expect(apiClientService.get).toHaveBeenCalledWith('/api/location/backoffice/countries/8');
    expect(data).toEqual({ id: 8 });
  });

  it('deleteCountry handles 204 and non-204', async () => {
    const response204 = { status: 204 };
    (apiClientService.delete as jest.Mock).mockResolvedValueOnce(response204);
    const json = jest.fn().mockResolvedValue({ title: 'BAD_REQUEST' });
    (apiClientService.delete as jest.Mock).mockResolvedValueOnce({ status: 400, json });

    const okResult = await deleteCountry(1);
    const badResult = await deleteCountry(1);

    expect(okResult).toBe(response204);
    expect(badResult).toEqual({ title: 'BAD_REQUEST' });
  });

  it('editCountry handles 204 and non-204', async () => {
    const response204 = { status: 204 };
    (apiClientService.put as jest.Mock).mockResolvedValueOnce(response204);
    const json = jest.fn().mockResolvedValue({ detail: 'failed' });
    (apiClientService.put as jest.Mock).mockResolvedValueOnce({ status: 400, json });

    const okResult = await editCountry(1, { name: 'VN' } as never);
    const badResult = await editCountry(1, { name: 'VN' } as never);

    expect(okResult).toBe(response204);
    expect(badResult).toEqual({ detail: 'failed' });
  });
});
