import {
  handleCreatingResponse,
  handleDeletingResponse,
  handleResponse,
  handleUpdatingResponse,
} from './ResponseStatusHandlingService';
import {
  CREATE_FAILED,
  CREATE_SUCCESSFULLY,
  DELETE_FAILED,
  HAVE_BEEN_DELETED,
  ResponseStatus,
  ResponseTitle,
  UPDATE_FAILED,
  UPDATE_SUCCESSFULLY,
} from '../../constants/Common';
import { toastError, toastSuccess } from './ToastService';

jest.mock('./ToastService', () => ({
  toastSuccess: jest.fn(),
  toastError: jest.fn(),
}));

describe('ResponseStatusHandlingService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('shows delete success toast when response status is SUCCESS', () => {
    handleDeletingResponse({ status: ResponseStatus.SUCCESS }, 'Brand');
    expect(toastSuccess).toHaveBeenCalledWith(`Brand${HAVE_BEEN_DELETED}`);
  });

  it('shows delete fallback error for unknown response', () => {
    handleDeletingResponse({ title: 'OTHER' }, 'Brand');
    expect(toastError).toHaveBeenCalledWith(DELETE_FAILED);
  });

  it('shows update success toast', () => {
    handleUpdatingResponse({ status: ResponseStatus.SUCCESS });
    expect(toastSuccess).toHaveBeenCalledWith(UPDATE_SUCCESSFULLY);
  });

  it('shows update validation error detail', () => {
    handleUpdatingResponse({ title: ResponseTitle.BAD_REQUEST, detail: 'Invalid input' });
    expect(toastError).toHaveBeenCalledWith('Invalid input');
  });

  it('shows create success toast', async () => {
    await handleCreatingResponse({ status: ResponseStatus.CREATED });
    expect(toastSuccess).toHaveBeenCalledWith(CREATE_SUCCESSFULLY);
  });

  it('shows create bad-request detail toast', async () => {
    await handleCreatingResponse({
      status: ResponseStatus.BAD_REQUEST,
      json: async () => ({ detail: 'Bad data' }),
    });
    expect(toastError).toHaveBeenCalledWith('Bad data');
  });

  it('shows create fallback failure toast', async () => {
    await handleCreatingResponse({ status: 500 });
    expect(toastError).toHaveBeenCalledWith(CREATE_FAILED);
  });

  it('shows generic success and error based on response.ok', () => {
    handleResponse({ ok: true }, 'ok', 'fail');
    handleResponse({ ok: false }, 'ok', 'fail');

    expect(toastSuccess).toHaveBeenCalledWith('ok');
    expect(toastError).toHaveBeenCalledWith('fail');
  });
});
