import { clsx } from 'clsx';
import type { TaskStatus } from '../../types/task';

const statusOptions: { value: TaskStatus; label: string; bgColor: string; textColor: string }[] = [
  { value: 'CREATED', label: 'Created', bgColor: 'bg-blue-100', textColor: 'text-blue-800' },
  { value: 'IN_BACKLOG', label: 'Backlog', bgColor: 'bg-yellow-100', textColor: 'text-yellow-800' },
  { value: 'BLOCKED', label: 'Blocked', bgColor: 'bg-red-100', textColor: 'text-red-800' },
  { value: 'DONE', label: 'Done', bgColor: 'bg-green-100', textColor: 'text-green-800' },
  { value: 'CANCELLED', label: 'Cancelled', bgColor: 'bg-gray-100', textColor: 'text-gray-600' },
];

interface StatusSelectProps {
  value: TaskStatus;
  onChange: (status: TaskStatus) => void;
  disabled?: boolean;
  className?: string;
}

export function StatusSelect({ value, onChange, disabled = false, className }: StatusSelectProps) {
  const currentStatus = statusOptions.find((s) => s.value === value) || statusOptions[0];

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    e.stopPropagation();
    onChange(e.target.value as TaskStatus);
  };

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  return (
    <select
      value={value}
      onChange={handleChange}
      onClick={handleClick}
      disabled={disabled}
      className={clsx(
        'appearance-none cursor-pointer rounded-full px-2.5 py-0.5 text-xs font-medium border-0',
        'focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-1',
        'disabled:opacity-50 disabled:cursor-not-allowed',
        currentStatus.bgColor,
        currentStatus.textColor,
        className
      )}
      style={{
        backgroundImage: `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e")`,
        backgroundPosition: 'right 0.25rem center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: '1rem 1rem',
        paddingRight: '1.5rem',
      }}
    >
      {statusOptions.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
}
