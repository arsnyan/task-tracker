import { Link } from 'react-router-dom';
import { StatusSelect } from '../ui/StatusSelect';
import type { TaskOverview, TaskStatus } from '../../types/task';

interface TaskCardProps {
  task: TaskOverview;
  onStatusChange?: (taskId: number, status: TaskStatus) => void;
  isUpdating?: boolean;
}

export function TaskCard({ task, onStatusChange, isUpdating = false }: TaskCardProps) {
  const handleStatusChange = (status: TaskStatus) => {
    if (onStatusChange) {
      onStatusChange(task.taskId, status);
    }
  };

  return (
    <Link
      to={`/tasks/${task.taskId}`}
      className="block bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md hover:border-indigo-200 transition-all group"
    >
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3 min-w-0">
          <div className="flex-shrink-0">
            {task.status === 'DONE' ? (
              <div className="h-5 w-5 rounded-full bg-green-100 flex items-center justify-center">
                <svg
                  className="h-3 w-3 text-green-600"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
            ) : (
              <div className="h-5 w-5 rounded-full border-2 border-gray-300 group-hover:border-indigo-400 transition-colors" />
            )}
          </div>
          <h3 className="text-base font-medium text-gray-900 truncate group-hover:text-indigo-600 transition-colors">
            {task.title}
          </h3>
        </div>
        <StatusSelect
          value={task.status}
          onChange={handleStatusChange}
          disabled={isUpdating}
        />
      </div>
    </Link>
  );
}
