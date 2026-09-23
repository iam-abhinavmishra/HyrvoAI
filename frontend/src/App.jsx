import Login from './pages/Login';
import ChatDashboard from './pages/ChatDashboard';
import AdminDocuments from './pages/AdminDocuments';
import OAuthCallback from './pages/OAuthCallback';
import PublicWidget from './pages/PublicWidget';
import { useAuth } from './context/AuthContext';
import HyrvoAIWidget from './widget/HyrvoAIWidget';

function App() {
  const { isAuthenticated, loading, user } = useAuth();

  if (loading) {
    return (
      <div className="app-loading">
        Loading HyrvoAI...
      </div>
    );
  }

  const path = window.location.pathname;

  /*
   * Standalone embeddable/public widget.
   *
   * Example:
   * /widget?key=hyrvo_pub_xxxxx
   */
  if (path === '/widget') {
    return <PublicWidget />;
  }

  /*
   * OAuth callback must be handled before
   * normal authentication routing.
   */
  if (path === '/oauth/callback') {
    return <OAuthCallback />;
  }

  /*
   * Public login page.
   *
   * The widget is available even when the visitor
   * is not authenticated.
   */
  if (!isAuthenticated) {
    return (
      <>
        <Login />
        <HyrvoAIWidget />
      </>
    );
  }

  /*
   * Admin document management.
   */
  if (path === '/admin/documents') {
    if (user?.role !== 'ADMIN') {
      window.history.replaceState({}, '', '/');

      return (
        <>
          <ChatDashboard />
          <HyrvoAIWidget />
        </>
      );
    }

    return (
      <>
        <AdminDocuments />
        <HyrvoAIWidget />
      </>
    );
  }

  /*
   * Normal authenticated dashboard.
   */
  return (
    <>
      <ChatDashboard />
      <HyrvoAIWidget />
    </>
  );
}

export default App;