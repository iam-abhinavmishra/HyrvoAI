import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import ThemeToggle from '../components/ThemeToggle';

import {
  sendChatMessage,
  getChatSessions,
  getChatMessages,
} from '../services/api';

function ChatDashboard() {
  const { user, token, logout } = useAuth();

  const [sessions, setSessions] = useState([]);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [sessionId, setSessionId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  /*
   * Safely extract an array from API responses.
   *
   * Depending on the backend response format, the API may return:
   *   [...]
   * or
   *   { sessions: [...] }
   * or
   *   { data: [...] }
   */
  function normalizeArray(data, possibleKeys = []) {
    if (Array.isArray(data)) {
      return data;
    }

    if (data && typeof data === 'object') {
      for (const key of possibleKeys) {
        if (Array.isArray(data[key])) {
          return data[key];
        }
      }
    }

    return [];
  }

  /*
   * Send a chat message
   */
  async function handleSendMessage(event) {
    event.preventDefault();

    const trimmedMessage = input.trim();

    if (!trimmedMessage || loading) {
      return;
    }

    const userMessage = {
      role: 'user',
      content: trimmedMessage,
    };

    setMessages((previousMessages) => [
      ...previousMessages,
      userMessage,
    ]);

    setInput('');
    setError('');
    setLoading(true);

    try {
      const data = await sendChatMessage(
        token,
        trimmedMessage,
        sessionId
      );

      /*
       * Save the session ID returned by the backend.
       */
      if (data?.sessionId) {
        setSessionId(data.sessionId);
      }

      /*
       * Add the assistant response.
       */
      const assistantMessage = {
        role: 'assistant',
        content:
          data?.answer ||
          'I could not generate an answer.',
        sources: Array.isArray(data?.sources)
          ? data.sources
          : [],
      };

      setMessages((previousMessages) => [
        ...previousMessages,
        assistantMessage,
      ]);

      /*
       * Refresh the sidebar so the newly created
       * conversation appears immediately.
       */
      try {
        const updatedSessions = await getChatSessions(token);

        const normalizedSessions = normalizeArray(
          updatedSessions,
          ['sessions', 'data', 'content']
        );

        setSessions(normalizedSessions);
      } catch (err) {
        console.error(
          'Failed to refresh chat history:',
          err
        );
      }
    } catch (err) {
      console.error('Failed to send message:', err);

      setError(
        err?.message ||
          'Something went wrong while processing your request.'
      );

      setMessages((previousMessages) => [
        ...previousMessages,
        {
          role: 'assistant',
          content:
            'Sorry, I was unable to process your request.',
          sources: [],
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  /*
   * Load all previous chat sessions
   * when the dashboard opens.
   */
  useEffect(() => {
    async function loadSessions() {
      try {
        const data = await getChatSessions(token);

        const normalizedSessions = normalizeArray(
          data,
          ['sessions', 'data', 'content']
        );

        setSessions(normalizedSessions);
      } catch (err) {
        console.error(
          'Failed to load chat sessions:',
          err
        );

        setSessions([]);
      }
    }

    if (token) {
      loadSessions();
    }
  }, [token]);

  /*
   * Open an existing conversation.
   */
  async function handleSelectSession(selectedSessionId) {
    if (loading) {
      return;
    }

    setError('');
    setLoading(true);

    try {
      const data = await getChatMessages(
        token,
        selectedSessionId
      );

      const messageList = normalizeArray(
        data,
        ['messages', 'data', 'content']
      );

      const formattedMessages = messageList.map(
        (message) => ({
          role:
            message?.role === 'USER'
              ? 'user'
              : 'assistant',

          content: message?.content || '',

          /*
           * Historical messages currently don't contain
           * source information because sources are not
           * persisted in ChatMessage.
           */
          sources: [],
        })
      );

      setMessages(formattedMessages);
      setSessionId(selectedSessionId);
    } catch (err) {
      console.error(
        'Failed to load conversation:',
        err
      );

      setError(
        err?.message ||
          'Unable to load this conversation.'
      );
    } finally {
      setLoading(false);
    }
  }

  /*
   * Start a completely new conversation.
   *
   * A database session will be created only when
   * the user sends the first message.
   */
  function handleNewChat() {
    setMessages([]);
    setSessionId(null);
    setInput('');
    setError('');
  }

  return (
    <div className="chat-app">

      {/* =========================
          SIDEBAR
      ========================== */}

      <aside className="chat-sidebar">

        <div className="sidebar-header">
          <h1>HyrvoAI</h1>

          <p>
            Company Helpdesk
          </p>
        </div>

        {/* New Chat Button */}

        <button
          className="new-chat-button"
          onClick={handleNewChat}
          disabled={loading}
        >
          + New chat
        </button>

        {/* Recent Chats */}

        <div className="sidebar-section">

          <p className="sidebar-title">
            Recent chats
          </p>

          <div className="chat-history">

            {sessions.length === 0 ? (
              <p className="empty-history">
                No conversations yet
              </p>
            ) : (
              sessions.map((session) => (
                <button
                  key={session.id}
                  className={`chat-history-item ${
                    session.id === sessionId
                      ? 'active'
                      : ''
                  }`}
                  onClick={() =>
                    handleSelectSession(session.id)
                  }
                  disabled={loading}
                >
                  {session.title ||
                    'New conversation'}
                </button>
              ))
            )}

          </div>
        </div>

        {/* Sidebar Bottom */}

        <div className="sidebar-bottom">

          <ThemeToggle />

          {user?.role === 'ADMIN' && (
            <a
              href="/admin/documents"
              className="admin-panel-button"
            >
              Admin Panel
            </a>
          )}

          <div className="user-info">

            <div className="user-avatar">
              {(user?.name ||
                user?.email ||
                'U')
                .charAt(0)
                .toUpperCase()}
            </div>

            <div>
              <strong>
                {user?.name ||
                  user?.email}
              </strong>

              <span>
                {user?.role}
              </span>
            </div>

          </div>

          <button
            className="logout-button"
            onClick={logout}
          >
            Logout
          </button>

        </div>

      </aside>

      {/* =========================
          MAIN CHAT
      ========================== */}

      <main className="chat-main">

        {/* Header */}

        <header className="chat-header">

          <div>
            <h2>
              Company Assistant
            </h2>

            <p>
              Ask questions about company
              policies and documents.
            </p>
          </div>

        </header>

        {/* =========================
            MESSAGES
        ========================== */}

        <div className="messages-container">

          {/* Welcome Screen */}

          {messages.length === 0 && (
            <div className="welcome-section">

              <div className="welcome-icon">
                ✦
              </div>

              <h2>
                How can I help you?
              </h2>

              <p>
                Ask me about company policies,
                procedures, benefits, manuals,
                and other internal documents.
              </p>

              {/* Suggested Questions */}

              <div className="suggestion-grid">

                <button
                  onClick={() =>
                    setInput(
                      'What is covered in the company documents?'
                    )
                  }
                >
                  What is covered in the company
                  documents?
                </button>

                <button
                  onClick={() =>
                    setInput(
                      'What is the leave policy?'
                    )
                  }
                >
                  What is the leave policy?
                </button>

                <button
                  onClick={() =>
                    setInput(
                      'What benefits are mentioned in the documents?'
                    )
                  }
                >
                  What benefits are mentioned?
                </button>

                <button
                  onClick={() =>
                    setInput(
                      'Summarize the relevant company policies.'
                    )
                  }
                >
                  Summarize the company policies
                </button>

              </div>

            </div>
          )}

          {/* Chat Messages */}

          {Array.isArray(messages) &&
            messages.map((message, index) => (

              <div
                key={index}
                className={`message-row ${message.role}`}
              >

                {/* Avatar */}

                <div className="message-avatar">
                  {message.role === 'user'
                    ? 'U'
                    : '✦'}
                </div>

                {/* Message Content */}

                <div className="message-content">

                  <div className="message-role">
                    {message.role === 'user'
                      ? 'You'
                      : 'HyrvoAI'}
                  </div>

                  <div className="message-text">
                    {message.content}
                  </div>

                  {/* Sources */}

                  {message.role === 'assistant' &&
                    Array.isArray(message.sources) &&
                    message.sources.length > 0 && (

                      <div className="sources-section">

                        <p className="sources-title">
                          Sources
                        </p>

                        <div className="sources-list">

                          {message.sources.map(
                            (
                              source,
                              sourceIndex
                            ) => (

                              <div
                                className="source-card"
                                key={sourceIndex}
                              >

                                <span className="source-icon">
                                  📄
                                </span>

                                <div>

                                  <strong>
                                    {source?.title ||
                                      source?.document ||
                                      source?.fileName ||
                                      'Company document'}
                                  </strong>

                                  <span>

                                    {source?.version
                                      ? `Version ${source.version}`
                                      : ''}

                                    {source?.chunkIndex !==
                                      undefined
                                      ? ` · Section ${source.chunkIndex}`
                                      : ''}

                                  </span>

                                </div>

                              </div>

                            )
                          )}

                        </div>

                      </div>

                    )}

                </div>

              </div>

            ))}

          {/* Typing Indicator */}

          {loading && (

            <div className="message-row assistant">

              <div className="message-avatar">
                ✦
              </div>

              <div className="message-content">

                <div className="message-role">
                  HyrvoAI
                </div>

                <div className="typing-indicator">
                  <span />
                  <span />
                  <span />
                </div>

              </div>

            </div>

          )}

          {/* Error */}

          {error && (
            <div className="chat-error">
              {error}
            </div>
          )}

        </div>

        {/* =========================
            CHAT INPUT
        ========================== */}

        <div className="chat-input-area">

          <form
            className="chat-input-form"
            onSubmit={handleSendMessage}
          >

            <textarea
              value={input}
              onChange={(event) =>
                setInput(event.target.value)
              }
              placeholder="Ask about company policies..."
              rows="1"
              disabled={loading}
              onKeyDown={(event) => {

                if (
                  event.key === 'Enter' &&
                  !event.shiftKey
                ) {
                  event.preventDefault();
                  handleSendMessage(event);
                }

              }}
            />

            <button
              type="submit"
              disabled={
                !input.trim() ||
                loading
              }
              className="send-button"
            >
              ➤
            </button>

          </form>

          <p className="input-disclaimer">
            HyrvoAI answers using available
            company documents.
          </p>

        </div>

      </main>

    </div>
  );
}

export default ChatDashboard;