import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';

function OAuthCallback() {
  const { login } = useAuth();
  const [error, setError] = useState('');

  useEffect(() => {
    const hash = window.location.hash;

    if (!hash) {
      setError('No authentication token was received.');
      return;
    }

    const params = new URLSearchParams(hash.substring(1));
    const token = params.get('token');

    if (!token) {
      setError('Authentication failed.');
      return;
    }

    try {
      const payload = JSON.parse(
        atob(
          token
            .split('.')[1]
            .replace(/-/g, '+')
            .replace(/_/g, '/')
        )
      );

      const user = {
        email: payload.sub,
        name: payload.name || payload.sub,
        role: payload.role,
      };

      login({
        token,
        email: user.email,
        name: user.name,
        role: user.role,
      });

      window.history.replaceState(
        {},
        document.title,
        '/oauth/callback'
      );

      window.location.href = '/';
    } catch (err) {
      console.error(err);
      setError('Invalid authentication response.');
    }
  }, [login]);

  if (error) {
    return (
      <div className="oauth-callback-page">
        <div className="oauth-callback-card">
          <h2>Authentication failed</h2>

          <p>{error}</p>

          <button
            onClick={() => {
              window.location.href = '/';
            }}
          >
            Back to login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="oauth-callback-page">
      <div className="oauth-callback-card">
        <div className="oauth-spinner" />

        <h2>Signing you in...</h2>

        <p>
          Please wait while we finish authentication.
        </p>
      </div>
    </div>
  );
}

export default OAuthCallback;