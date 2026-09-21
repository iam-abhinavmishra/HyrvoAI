import { useAuth } from '../context/AuthContext';

function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div>
      <h1>Welcome to HyrvoAI</h1>

      <p>
        Hello, {user?.name || user?.email}
      </p>

      <p>
        Role: {user?.role}
      </p>

      <button onClick={logout}>
        Logout
      </button>
    </div>
  );
}

export default Dashboard;