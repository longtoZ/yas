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

  it('calls toast.success with message', () => {
    toastSuccess('Saved!');
    expect(toast.success).toHaveBeenCalledWith('Saved!', expect.objectContaining({ autoClose: 1000 }));
  });

  it('calls toast.error with message', () => {
    toastError('Failed!');
    expect(toast.error).toHaveBeenCalledWith('Failed!', expect.objectContaining({ theme: 'colored' }));
  });
});
