import { formatPriceUSD, formatPriceVND } from './formatPrice';

describe('formatPrice utilities', () => {
  it('formats VND with Vietnamese locale currency', () => {
    const formatted = formatPriceVND(1000000);
    expect(formatted).toContain('₫');
    expect(formatted.replace(/\s/g, '')).toContain('1.000.000');
  });

  it('formats USD with US locale currency', () => {
    const formatted = formatPriceUSD(1234.56);
    expect(formatted).toContain('$');
    expect(formatted).toContain('1,234.56');
  });
});
