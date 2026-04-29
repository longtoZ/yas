import { toast } from 'react-toastify';
import { toastError, toastSuccess } from './ToastService';

jest.mock('react-toastify', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

describe('ToastService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('calls toast.success with provided message', () => {
    toastSuccess('ok');
    expect(toast.success).toHaveBeenCalledWith(
      'ok',
      expect.objectContaining({
        position: 'top-right',
        autoClose: 3000,
      })
    );
  });

  it('calls toast.error with provided message', () => {
    toastError('fail');
    expect(toast.error).toHaveBeenCalledWith(
      'fail',
      expect.objectContaining({
        theme: 'colored',
      })
    );
  });
});
