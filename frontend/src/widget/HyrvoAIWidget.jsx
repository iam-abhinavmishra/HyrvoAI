import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import {
    sendChatMessage,
    sendPublicChatMessage
} from "../services/api";
import "./HyrvoAIWidget.css";

export default function HyrvoAIWidget({
    publicWidgetKey = null
}) {
    const { token } = useAuth();

    /*
     * Priority:
     *
     * 1. Explicit key passed to the widget
     *    (/widget?key=...)
     *
     * 2. Vite environment variable
     *    (VITE_HYRVOAI_WIDGET_PUBLIC_KEY)
     */
    const configuredPublicWidgetKey =
        publicWidgetKey ||
        import.meta.env.VITE_HYRVOAI_WIDGET_PUBLIC_KEY;

    const isAuthenticated = Boolean(token);

    const [open, setOpen] = useState(false);
    const [message, setMessage] = useState("");
    const [sessionId, setSessionId] = useState(null);
    const [loading, setLoading] = useState(false);

    const [messages, setMessages] = useState([
        {
            role: "assistant",
            content:
                "Hello! I'm HyrvoAI. How can I help you?"
        }
    ]);

    const sendMessage = async () => {
        const trimmedMessage = message.trim();

        if (!trimmedMessage || loading) {
            return;
        }

        /*
         * Public users need a valid public widget key.
         * Authenticated users use JWT and don't need
         * the public key.
         */
        if (
            !isAuthenticated &&
            !configuredPublicWidgetKey
        ) {
            setMessages((previousMessages) => [
                ...previousMessages,
                {
                    role: "assistant",
                    content:
                        "HyrvoAI is not configured for this website."
                }
            ]);

            setMessage("");
            return;
        }

        /*
         * Add user's message immediately.
         */
        setMessages((previousMessages) => [
            ...previousMessages,
            {
                role: "user",
                content: trimmedMessage
            }
        ]);

        setMessage("");
        setLoading(true);

        try {
            let data;

            /*
             * Authenticated flow
             *
             * Uses:
             * POST /api/chat
             *
             * with JWT.
             */
            if (isAuthenticated) {
                data = await sendChatMessage(
                    token,
                    trimmedMessage,
                    sessionId
                );
            }

            /*
             * Public flow
             *
             * Uses:
             * POST /api/public/chat
             *
             * with the company's public widget key.
             */
            else {
                data = await sendPublicChatMessage(
                    trimmedMessage,
                    configuredPublicWidgetKey
                );
            }

            /*
             * Authenticated conversations have
             * server-side sessions.
             *
             * Public conversations are currently
             * stateless.
             */
            if (
                data.sessionId &&
                isAuthenticated
            ) {
                setSessionId(data.sessionId);
            }

            setMessages((previousMessages) => [
                ...previousMessages,
                {
                    role: "assistant",
                    content:
                        data.answer ||
                        "I couldn't generate an answer."
                }
            ]);
        } catch (error) {
            console.error(
                "HyrvoAI widget chat error:",
                error
            );

            setMessages((previousMessages) => [
                ...previousMessages,
                {
                    role: "assistant",
                    content:
                        error.message ||
                        "Sorry, I was unable to process your request."
                }
            ]);
        } finally {
            setLoading(false);
        }
    };

    const handleKeyDown = (event) => {
        if (
            event.key === "Enter" &&
            !event.shiftKey
        ) {
            event.preventDefault();
            sendMessage();
        }
    };

    const handleNewChat = () => {
        setMessages([
            {
                role: "assistant",
                content:
                    "Hello! I'm HyrvoAI. How can I help you?"
            }
        ]);

        setSessionId(null);
        setMessage("");
    };

    return (
        <>
            {open && (
                <div className="hyrvoai-widget">

                    <div className="hyrvoai-widget-header">

                        <div>
                            <div className="hyrvoai-widget-title">
                                HyrvoAI
                            </div>

                            <div className="hyrvoai-widget-status">
                                AI Helpdesk
                            </div>
                        </div>

                        <button
                            className="hyrvoai-widget-close"
                            onClick={() =>
                                setOpen(false)
                            }
                            aria-label="Close HyrvoAI"
                        >
                            ×
                        </button>

                    </div>

                    <div className="hyrvoai-widget-messages">

                        {messages.map(
                            (item, index) => (
                                <div
                                    key={index}
                                    className={`hyrvoai-message ${
                                        item.role === "user"
                                            ? "hyrvoai-message-user"
                                            : "hyrvoai-message-assistant"
                                    }`}
                                >
                                    {item.content}
                                </div>
                            )
                        )}

                        {loading && (
                            <div className="hyrvoai-message hyrvoai-message-assistant">
                                Thinking...
                            </div>
                        )}

                    </div>

                    <div className="hyrvoai-widget-input-area">

                        <textarea
                            value={message}
                            onChange={(event) =>
                                setMessage(
                                    event.target.value
                                )
                            }
                            onKeyDown={handleKeyDown}
                            placeholder="Ask something..."
                            rows={1}
                            disabled={loading}
                        />

                        <button
                            onClick={sendMessage}
                            disabled={loading}
                            aria-label="Send message"
                        >
                            {loading ? "..." : "➤"}
                        </button>

                    </div>

                    <button
                        className="hyrvoai-widget-new-chat"
                        onClick={handleNewChat}
                    >
                        New chat
                    </button>

                </div>
            )}

            <button
                className={`hyrvoai-widget-launcher ${
                    open
                        ? "hyrvoai-widget-launcher-open"
                        : ""
                }`}
                onClick={() =>
                    setOpen(
                        (previous) => !previous
                    )
                }
                aria-label="Open HyrvoAI"
            >
                {open ? "×" : "💬"}
            </button>
        </>
    );
}