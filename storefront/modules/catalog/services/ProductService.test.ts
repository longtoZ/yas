import apiClientService from '@/common/services/ApiClientService';
import {
  getFeaturedProducts,
  getProductByMultiParams,
  getProductOptionValueByProductId,
  getProductOptionValues,
  getProductsByIds,
  getProductSlug,
  getProductVariationsByParentId,
  getRelatedProductsByProductId,
  getSimilarProductsByProductId,
} from './ProductService';

jest.mock('@/common/services/ApiClientService', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
  },
}));

describe('ProductService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(console, 'log').mockImplementation(() => undefined);
  });

  it('getFeaturedProducts calls correct endpoint', async () => {
    const json = jest.fn().mockResolvedValue({ content: [] });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    await getFeaturedProducts(0);

    expect(apiClientService.get).toHaveBeenCalledWith(
      '/api/product/storefront/products/featured?pageNo=0'
    );
  });

  it('getProductByMultiParams calls correct endpoint', async () => {
    const json = jest.fn().mockResolvedValue({ content: [] });
    (apiClientService.get as jest.Mock).mockResolvedValue({ json });

    await getProductByMultiParams('category=1&brand=2');

    expect(apiClientService.get).toHaveBeenCalledWith(
      '/api/product/storefront/products?category=1&brand=2'
    );
  });

  it('getProductsByIds returns data when response is ok', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ ok: true, json });

    const result = await getProductsByIds([1]);

    expect(result).toEqual([{ id: 1 }]);
  });

  it('getProductsByIds throws YasError when response is not ok', async () => {
    const json = jest.fn().mockResolvedValue({ message: 'Not found' });
    (apiClientService.get as jest.Mock).mockResolvedValue({ ok: false, json });

    await expect(getProductsByIds([999])).rejects.toBeDefined();
  });

  it('getProductOptionValues returns data when 2xx', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getProductOptionValues(5);

    expect(result).toEqual([{ id: 1 }]);
  });

  it('getProductOptionValues throws when non-2xx', async () => {
    const json = jest.fn().mockResolvedValue('error');
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 404, json });

    await expect(getProductOptionValues(5)).rejects.toThrow();
  });

  it('getProductVariationsByParentId returns data when 2xx', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 2 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getProductVariationsByParentId(10);

    expect(result).toEqual([{ id: 2 }]);
  });

  it('getProductVariationsByParentId throws when non-2xx', async () => {
    const json = jest.fn().mockResolvedValue('error');
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 400, json });

    await expect(getProductVariationsByParentId(10)).rejects.toThrow();
  });

  it('getProductSlug returns slug when 2xx', async () => {
    const json = jest.fn().mockResolvedValue({ slug: 'my-product' });
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getProductSlug(7);

    expect(result).toEqual({ slug: 'my-product' });
  });

  it('getProductSlug throws when non-2xx', async () => {
    const json = jest.fn().mockResolvedValue('error');
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 404, json });

    await expect(getProductSlug(7)).rejects.toThrow();
  });

  it('getRelatedProductsByProductId returns data when 2xx', async () => {
    const json = jest.fn().mockResolvedValue({ content: [] });
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getRelatedProductsByProductId(3);

    expect(result).toEqual({ content: [] });
  });

  it('getRelatedProductsByProductId throws when non-2xx', async () => {
    const json = jest.fn().mockResolvedValue('error');
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 500, json });

    await expect(getRelatedProductsByProductId(3)).rejects.toThrow();
  });

  it('getSimilarProductsByProductId returns data when 2xx', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 8 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getSimilarProductsByProductId(8);

    expect(result).toEqual([{ id: 8 }]);
  });

  it('getSimilarProductsByProductId throws when non-2xx', async () => {
    const json = jest.fn().mockResolvedValue('error');
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 404, json });

    await expect(getSimilarProductsByProductId(8)).rejects.toThrow();
  });

  it('getProductOptionValueByProductId returns data when 2xx', async () => {
    const json = jest.fn().mockResolvedValue([{ id: 1 }]);
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 200, json });

    const result = await getProductOptionValueByProductId(4);

    expect(result).toEqual([{ id: 1 }]);
  });

  it('getProductOptionValueByProductId rejects when non-2xx', async () => {
    (apiClientService.get as jest.Mock).mockResolvedValue({ status: 404, statusText: 'Not Found' });

    await expect(getProductOptionValueByProductId(4)).rejects.toThrow('Not Found');
  });
});
