import { useState } from 'react';
import { useTasks, useCreateTask, useUpdateTask } from '../hooks/useTasks';
import { TaskList } from '../components/tasks/TaskList';
import { TaskForm } from '../components/tasks/TaskForm';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';
import type { TaskStatus } from '../types/task';

export function TaskListPage() {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [updatingTaskId, setUpdatingTaskId] = useState<number | null>(null);
  const { data: tasks = [], isLoading, error } = useTasks();
  const createTask = useCreateTask();
  const updateTask = useUpdateTask();

  const handleCreateTask = async (data: { title: string; content?: string }) => {
    await createTask.mutateAsync(data);
    setIsCreateModalOpen(false);
  };

  const handleStatusChange = async (taskId: number, status: TaskStatus) => {
    setUpdatingTaskId(taskId);
    try {
      await updateTask.mutateAsync({ id: taskId, data: { status } });
    } finally {
      setUpdatingTaskId(null);
    }
  };

  if (error) {
    return (
      <div className="text-center py-12">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-red-100 mb-4">
          <svg
            className="w-6 h-6 text-red-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900">Failed to load tasks</h3>
        <p className="mt-2 text-sm text-gray-500">Please try refreshing the page.</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Tasks</h1>
          <p className="mt-1 text-sm text-gray-500">
            {tasks.length} {tasks.length === 1 ? 'task' : 'tasks'} total
          </p>
        </div>
        <Button onClick={() => setIsCreateModalOpen(true)}>
          <svg
            className="w-5 h-5 mr-1.5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 4v16m8-8H4"
            />
          </svg>
          New Task
        </Button>
      </div>

      <TaskList
        tasks={tasks}
        isLoading={isLoading}
        onStatusChange={handleStatusChange}
        updatingTaskId={updatingTaskId}
      />

      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create New Task"
      >
        <TaskForm
          onSubmit={handleCreateTask}
          onCancel={() => setIsCreateModalOpen(false)}
          isLoading={createTask.isPending}
          submitLabel="Create Task"
        />
      </Modal>
    </div>
  );
}
