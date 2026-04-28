import apiClientService from '@/common/services/ApiClientService';
import { getCountries } from './CountryService';

jest.mock('@/common/services/ApiClientService', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
  },
}));

describe('CountryService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('fetches countries from correct endpoint', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1, name: 'Vietnam' }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    const data = await getCountries();

    expect(apiClientService.get).toHaveBeenCalledWith('/api/location/storefront/countries');
    expect(data).toEqual([{ id: 1, name: 'Vietnam' }]);
  });
});
