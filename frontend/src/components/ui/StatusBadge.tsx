import { clsx } from 'clsx';
import type { TaskStatus } from '../../types/task';

const statusConfig: Record<TaskStatus, { label: string; className: string }> = {
  CREATED: { label: 'Created', className: 'bg-blue-100 text-blue-800' },
  IN_BACKLOG: { label: 'Backlog', className: 'bg-yellow-100 text-yellow-800' },
  BLOCKED: { label: 'Blocked', className: 'bg-red-100 text-red-800' },
  DONE: { label: 'Done', className: 'bg-green-100 text-green-800' },
  CANCELLED: { label: 'Cancelled', className: 'bg-gray-100 text-gray-600' },
};

interface StatusBadgeProps {
  status: TaskStatus;
  className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const { label, className: statusClassName } = statusConfig[status];

  return (
    <span
      className={clsx(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        statusClassName,
        className
      )}
    >
      {label}
    </span>
  );
}
