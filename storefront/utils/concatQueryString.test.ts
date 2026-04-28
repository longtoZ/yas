import { concatQueryString } from './concatQueryString';

describe('concatQueryString', () => {
  it('returns original URL when query array is empty', () => {
    expect(concatQueryString([], '/products')).toBe('/products');
  });

  it('adds one query parameter with ?', () => {
    expect(concatQueryString(['page=1'], '/products')).toBe('/products?page=1');
  });

  it('adds multiple query parameters with &', () => {
    expect(concatQueryString(['page=1', 'size=10', 'sort=asc'], '/products')).toBe(
      '/products?page=1&size=10&sort=asc'
    );
  });
});
