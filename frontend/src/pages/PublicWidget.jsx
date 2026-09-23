import HyrvoAIWidget from "../widget/HyrvoAIWidget";

export default function PublicWidget() {
    const params = new URLSearchParams(
        window.location.search
    );

    const widgetKey = params.get("key");

    if (!widgetKey) {
        return (
            <div
                style={{
                    minHeight: "100vh",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontFamily: "Arial, sans-serif",
                    color: "#111827"
                }}
            >
                Invalid HyrvoAI widget configuration.
            </div>
        );
    }

    return (
        <div
            style={{
                minHeight: "100vh",
                background: "transparent"
            }}
        >
            <HyrvoAIWidget
                publicWidgetKey={widgetKey}
            />
        </div>
    );
}