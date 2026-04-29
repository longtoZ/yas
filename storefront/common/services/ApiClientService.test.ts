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

  it('sends DELETE request', async () => {
    const fetchMock = jest.fn().mockResolvedValue({ ok: true });
    global.fetch = fetchMock as unknown as typeof fetch;

    await apiClientService.delete(endpoint);

    expect(fetchMock).toHaveBeenCalledWith(
      endpoint,
      expect.objectContaining({ method: 'DELETE' })
    );
  });

  it('sends PUT request', async () => {
    const fetchMock = jest.fn().mockResolvedValue({ ok: true });
    global.fetch = fetchMock as unknown as typeof fetch;

    await apiClientService.put(endpoint, JSON.stringify({ id: 1 }));

    expect(fetchMock).toHaveBeenCalledWith(
      endpoint,
      expect.objectContaining({ method: 'PUT' })
    );
  });

  it('rethrows errors from fetch', async () => {
    const fetchMock = jest.fn().mockRejectedValue(new Error('network'));
    global.fetch = fetchMock as unknown as typeof fetch;
    jest.spyOn(console, 'error').mockImplementation(() => undefined);

    await expect(apiClientService.get(endpoint)).rejects.toThrow('network');
  });
});
