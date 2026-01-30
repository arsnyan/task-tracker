import { Link } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { Button } from '../ui/Button';

export function Navbar() {
  const { user, logout } = useAuthStore();

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center gap-2">
              <svg
                className="h-8 w-8 text-indigo-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
                />
              </svg>
              <span className="text-xl font-bold text-gray-900">Task Tracker</span>
            </Link>
          </div>

          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center">
                <span className="text-sm font-medium text-indigo-600">
                  {user?.username?.charAt(0).toUpperCase()}
                </span>
              </div>
              <span className="text-sm font-medium text-gray-700 hidden sm:block">
                {user?.username}
              </span>
            </div>
            <Button variant="ghost" size="sm" onClick={logout}>
              Logout
            </Button>
          </div>
        </div>
      </div>
    </nav>
  );
}
