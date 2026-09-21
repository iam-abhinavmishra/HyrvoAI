import Login from './pages/Login';
import ChatDashboard from './pages/ChatDashboard';
import AdminDocuments from './pages/AdminDocuments';
import { useAuth } from './context/AuthContext';

function App() {
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return (
      <div className="app-loading">
        Loading HyrvoAI...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Login />;
  }

  const path = window.location.pathname;

  if (path === '/admin/documents') {
    if (user?.role !== 'ADMIN') {
      window.history.replaceState({}, '', '/');
      return <ChatDashboard />;
    }

    return <AdminDocuments />;
  }

  return <ChatDashboard />;
}

export default App;