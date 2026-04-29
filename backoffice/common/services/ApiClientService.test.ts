import apiClientService from './ApiClientService';

describe('ApiClientService', () => {
  const endpoint = '/api/test';

  beforeEach(() => {
    jest.resetAllMocks();
    Object.defineProperty(window, 'location', {
      value: { href: '' },
      writable: true,
    });
  });

  it('sends GET request without options payload', async () => {
    const fetchMock = jest.fn().mockResolvedValue({ ok: true });
    global.fetch = fetchMock as unknown as typeof fetch;

    await apiClientService.get(endpoint);

    expect(fetchMock).toHaveBeenCalledWith(endpoint, undefined);
  });

  it('sends POST request with JSON content type by default', async () => {
    const fetchMock = jest.fn().mockResolvedValue({ ok: true });
    global.fetch = fetchMock as unknown as typeof fetch;

    await apiClientService.post(endpoint, JSON.stringify({ name: 'demo' }));

    expect(fetchMock).toHaveBeenCalledWith(
      endpoint,
      expect.objectContaining({
        method: 'POST',
        body: '{"name":"demo"}',
        headers: expect.objectContaining({
          'Content-type': 'application/json; charset=UTF-8',
        }),
      })
    );
  });

  it('removes content type for FormData payload', async () => {
    const fetchMock = jest.fn().mockResolvedValue({ ok: true });
    global.fetch = fetchMock as unknown as typeof fetch;
    const formData = new FormData();
    formData.append('file', new Blob(['x']), 'a.txt');

    await apiClientService.post(endpoint, formData);

    const options = fetchMock.mock.calls[0][1];
    expect(options.headers['Content-type']).toBeUndefined();
  });

  it('redirects browser when cors response is redirected', async () => {
    const fetchMock = jest.fn().mockResolvedValue({
      type: 'cors',
      redirected: true,
      url: 'https://redirected.example',
    });
    global.fetch = fetchMock as unknown as typeof fetch;

    await apiClientService.get(endpoint);

    expect(window.location.href).toBe('https://redirected.example');
  });

  it('rethrows errors from fetch', async () => {
    const error = new Error('network');
    const fetchMock = jest.fn().mockRejectedValue(error);
    global.fetch = fetchMock as unknown as typeof fetch;
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => undefined);

    await expect(apiClientService.get(endpoint)).rejects.toThrow('network');
    expect(consoleSpy).toHaveBeenCalled();
  });
});
