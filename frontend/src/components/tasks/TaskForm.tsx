import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import type { TaskStatus } from '../../types/task';

const taskSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  content: z.string().optional(),
  status: z.enum(['CREATED', 'CANCELLED', 'IN_BACKLOG', 'BLOCKED', 'DONE']).optional(),
});

type TaskFormData = z.infer<typeof taskSchema>;

interface TaskFormProps {
  defaultValues?: {
    title?: string;
    content?: string | null;
    status?: TaskStatus;
  };
  onSubmit: (data: TaskFormData) => void;
  onCancel?: () => void;
  isLoading?: boolean;
  showStatus?: boolean;
  submitLabel?: string;
}

const statusOptions: { value: TaskStatus; label: string }[] = [
  { value: 'CREATED', label: 'Created' },
  { value: 'IN_BACKLOG', label: 'Backlog' },
  { value: 'BLOCKED', label: 'Blocked' },
  { value: 'DONE', label: 'Done' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

export function TaskForm({
  defaultValues,
  onSubmit,
  onCancel,
  isLoading = false,
  showStatus = false,
  submitLabel = 'Save',
}: TaskFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<TaskFormData>({
    resolver: zodResolver(taskSchema),
    defaultValues: {
      title: defaultValues?.title || '',
      content: defaultValues?.content || '',
      status: defaultValues?.status,
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="Title"
        placeholder="Enter task title"
        error={errors.title?.message}
        {...register('title')}
      />

      <div>
        <label
          htmlFor="content"
          className="block text-sm font-medium text-gray-700 mb-1"
        >
          Description
        </label>
        <textarea
          id="content"
          rows={4}
          placeholder="Enter task description (optional)"
          className="block w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-400 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent hover:border-gray-400 resize-none"
          {...register('content')}
        />
      </div>

      {showStatus && (
        <div>
          <label
            htmlFor="status"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            Status
          </label>
          <select
            id="status"
            className="block w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-900 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent hover:border-gray-400"
            {...register('status')}
          >
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      )}

      <div className="flex justify-end gap-3 pt-2">
        {onCancel && (
          <Button type="button" variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
        )}
        <Button type="submit" isLoading={isLoading}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
