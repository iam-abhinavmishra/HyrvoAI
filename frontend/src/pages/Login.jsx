import { useState } from 'react';
import { loginUser } from '../services/api';
import { useAuth } from '../context/AuthContext';

function Login() {
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();

    setError('');

    if (!email.trim() || !password) {
      setError('Please enter your email and password.');
      return;
    }

    try {
      setLoading(true);

      const data = await loginUser(email.trim(), password);
      login(data);

      window.location.href = '/';
    } catch (err) {
      console.error(err);

      setError(
        err?.message ||
        'Invalid email or password. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  }

  function handleGoogleLogin() {
    window.location.href =
      'http://localhost:8080/oauth2/authorization/google';
  }

  function handleLinkedInLogin() {
    window.location.href =
      'http://localhost:8080/oauth2/authorization/linkedin';
  }

  return (
    <div className="login-page">

      {/* Left branding section */}
      <section className="login-brand-section">
        <div className="brand-content">

          <div className="brand-logo">
            <div className="brand-logo-mark">
              H
            </div>

            <span>HyrvoAI</span>
          </div>

          <div className="brand-message">
            <p className="brand-eyebrow">
              INTERNAL AI ASSISTANT
            </p>

            <h1>
              Your company's
              <br />
              knowledge,
              <br />
              <span>always within reach.</span>
            </h1>

            <p className="brand-description">
              Ask questions about company policies, procedures,
              manuals and internal documentation — and get
              answers grounded in your organization's knowledge.
            </p>
          </div>

          <div className="brand-features">
            <div className="brand-feature">
              <div className="feature-icon">✓</div>
              <div>
                <strong>Company-aware answers</strong>
                <span>
                  Information is retrieved from your organization's
                  documents.
                </span>
              </div>
            </div>

            <div className="brand-feature">
              <div className="feature-icon">⌁</div>
              <div>
                <strong>Secure access</strong>
                <span>
                  Access is controlled by your company account
                  and role.
                </span>
              </div>
            </div>

            <div className="brand-feature">
              <div className="feature-icon">◈</div>
              <div>
                <strong>Source-grounded AI</strong>
                <span>
                  Answers are generated using relevant company
                  documentation.
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="brand-footer">
          © 2026 HyrvoAI
        </div>
      </section>

      {/* Login section */}
      <section className="login-form-section">

        <div className="login-card">

          <div className="mobile-brand">
            <div className="brand-logo">
              <div className="brand-logo-mark">
                H
              </div>

              <span>HyrvoAI</span>
            </div>
          </div>

          <div className="login-header">
            <h2>Welcome back</h2>

            <p>
              Sign in to access your company knowledge assistant.
            </p>
          </div>

          {error && (
            <div className="login-error">
              <span className="error-icon">!</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit}>

            <div className="form-group">
              <label htmlFor="email">
                Work email
              </label>

              <input
                id="email"
                type="email"
                placeholder="you@company.com"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoComplete="email"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <div className="password-label-row">
                <label htmlFor="password">
                  Password
                </label>
              </div>

              <input
                id="password"
                type="password"
                placeholder="Enter your password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="current-password"
                disabled={loading}
              />
            </div>

            <button
              type="submit"
              className="login-submit-button"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="button-spinner" />
                  Signing in...
                </>
              ) : (
                'Sign in'
              )}
            </button>

          </form>

          <div className="oauth-divider">
            <span>OR CONTINUE WITH</span>
          </div>

          <div className="oauth-buttons">

            <button
              type="button"
              className="oauth-button"
              onClick={handleGoogleLogin}
              disabled={loading}
            >
              <span className="google-icon">
                G
              </span>

              <span>
                Continue with Google
              </span>
            </button>

            <button
              type="button"
              className="oauth-button"
              onClick={handleLinkedInLogin}
              disabled={loading}
            >
              <span className="linkedin-icon">
                in
              </span>

              <span>
                Continue with LinkedIn
              </span>
            </button>

          </div>

          <p className="login-notice">
            Your organization controls access to HyrvoAI.
            <br />
            Contact your administrator if you need an account.
          </p>

        </div>

      </section>

    </div>
  );
}

export default Login;