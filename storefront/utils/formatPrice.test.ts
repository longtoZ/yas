import { formatPrice } from './formatPrice';

describe('formatPrice', () => {
  it('formats a number as USD currency', () => {
    const result = formatPrice(1234.56);
    expect(result).toContain('$');
    expect(result).toContain('1,234.56');
  });

  it('formats zero correctly', () => {
    const result = formatPrice(0);
    expect(result).toContain('$');
    expect(result).toContain('0.00');
  });

  it('formats large numbers with commas', () => {
    const result = formatPrice(1000000);
    expect(result).toContain('1,000,000');
  });
});
