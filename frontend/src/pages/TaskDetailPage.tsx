import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useTask, useUpdateTask, useDeleteTask } from '../hooks/useTasks';
import { StatusSelect } from '../components/ui/StatusSelect';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { Modal } from '../components/ui/Modal';
import { Card } from '../components/ui/Card';
import { format } from 'date-fns';
import type { TaskStatus } from '../types/task';

type EditingField = 'title' | 'content' | null;

export function TaskDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const taskId = Number(id);

  const { data: task, isLoading, error } = useTask(taskId);
  const updateTask = useUpdateTask();
  const deleteTask = useDeleteTask();

  const [editingField, setEditingField] = useState<EditingField>(null);
  const [editValue, setEditValue] = useState('');
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  const startEditing = (field: EditingField) => {
    if (!task || !field) return;
    setEditingField(field);
    setEditValue(field === 'title' ? task.title : task.content || '');
  };

  const cancelEditing = () => {
    setEditingField(null);
    setEditValue('');
  };

  const saveField = async () => {
    if (!editingField || !task) return;

    const data = editingField === 'title'
      ? { title: editValue }
      : { content: editValue };

    await updateTask.mutateAsync({ id: taskId, data });
    setEditingField(null);
    setEditValue('');
  };

  const handleStatusChange = async (status: TaskStatus) => {
    await updateTask.mutateAsync({ id: taskId, data: { status } });
  };

  const handleDelete = async () => {
    await deleteTask.mutateAsync(taskId);
    navigate('/');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey && editingField === 'title') {
      e.preventDefault();
      saveField();
    } else if (e.key === 'Escape') {
      cancelEditing();
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error || !task) {
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
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900">Task not found</h3>
        <p className="mt-2 text-sm text-gray-500">
          The task you're looking for doesn't exist or has been deleted.
        </p>
        <Link
          to="/"
          className="mt-4 inline-flex items-center text-sm font-medium text-indigo-600 hover:text-indigo-500"
        >
          <svg
            className="w-4 h-4 mr-1"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
          Back to tasks
        </Link>
      </div>
    );
  }

  return (
    <div>
      {/* Breadcrumb */}
      <nav className="mb-6">
        <Link
          to="/"
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-700"
        >
          <svg
            className="w-4 h-4 mr-1"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
          Back to tasks
        </Link>
      </nav>

      <Card padding="lg">
        <div className="flex items-start justify-between gap-4 mb-6">
          <div className="flex-1 min-w-0">
            {/* Editable Title */}
            {editingField === 'title' ? (
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  value={editValue}
                  onChange={(e) => setEditValue(e.target.value)}
                  onKeyDown={handleKeyDown}
                  autoFocus
                  className="flex-1 text-2xl font-bold text-gray-900 border border-indigo-300 rounded-lg px-3 py-1 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
                <Button
                  size="sm"
                  onClick={saveField}
                  isLoading={updateTask.isPending}
                >
                  Save
                </Button>
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={cancelEditing}
                  disabled={updateTask.isPending}
                >
                  Cancel
                </Button>
              </div>
            ) : (
              <div className="group flex items-center gap-2">
                <h1
                  className="text-2xl font-bold text-gray-900 cursor-pointer hover:text-indigo-600 transition-colors"
                  onClick={() => startEditing('title')}
                  title="Click to edit"
                >
                  {task.title}
                </h1>
                <button
                  onClick={() => startEditing('title')}
                  className="opacity-0 group-hover:opacity-100 transition-opacity p-1 text-gray-400 hover:text-indigo-600"
                  title="Edit title"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                  </svg>
                </button>
              </div>
            )}

            {/* Status and completion date */}
            <div className="flex items-center gap-3 mt-3">
              <StatusSelect
                value={task.status}
                onChange={handleStatusChange}
                disabled={updateTask.isPending}
              />
              {task.finishedAt && (
                <span className="text-sm text-gray-500">
                  Completed on {format(new Date(task.finishedAt), 'MMM d, yyyy')}
                </span>
              )}
            </div>
          </div>

          <Button variant="danger" onClick={() => setIsDeleteModalOpen(true)}>
            <svg className="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
            Delete
          </Button>
        </div>

        {/* Editable Description */}
        <div className="border-t border-gray-200 pt-6">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-sm font-medium text-gray-500">Description</h2>
            {editingField !== 'content' && (
              <button
                onClick={() => startEditing('content')}
                className="text-sm text-indigo-600 hover:text-indigo-800 font-medium"
              >
                {task.content ? 'Edit' : 'Add description'}
              </button>
            )}
          </div>

          {editingField === 'content' ? (
            <div className="space-y-3">
              <textarea
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                onKeyDown={handleKeyDown}
                autoFocus
                rows={6}
                placeholder="Enter task description..."
                className="w-full border border-indigo-300 rounded-lg px-3 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
              />
              <div className="flex justify-end gap-2">
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={cancelEditing}
                  disabled={updateTask.isPending}
                >
                  Cancel
                </Button>
                <Button
                  size="sm"
                  onClick={saveField}
                  isLoading={updateTask.isPending}
                >
                  Save
                </Button>
              </div>
            </div>
          ) : task.content ? (
            <p
              className="text-gray-700 whitespace-pre-wrap cursor-pointer hover:bg-gray-50 rounded-lg p-2 -mx-2 transition-colors"
              onClick={() => startEditing('content')}
              title="Click to edit"
            >
              {task.content}
            </p>
          ) : (
            <p
              className="text-sm text-gray-400 italic cursor-pointer hover:text-indigo-600 transition-colors"
              onClick={() => startEditing('content')}
            >
              Click to add a description...
            </p>
          )}
        </div>
      </Card>

      {/* Delete confirmation modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Delete Task"
      >
        <p className="text-sm text-gray-600 mb-4">
          Are you sure you want to delete this task? This action cannot be undone.
        </p>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={() => setIsDeleteModalOpen(false)}>
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={handleDelete}
            isLoading={deleteTask.isPending}
          >
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
