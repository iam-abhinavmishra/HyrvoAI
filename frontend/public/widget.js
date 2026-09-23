(function () {
    "use strict";

    const currentScript =
        document.currentScript;

    if (!currentScript) {
        console.error(
            "HyrvoAI: Unable to find widget script."
        );
        return;
    }

    const widgetKey =
        currentScript.getAttribute(
            "data-widget-key"
        );

    if (!widgetKey) {
        console.error(
            "HyrvoAI: data-widget-key is required."
        );
        return;
    }

    const widgetOrigin =
        new URL(
            currentScript.src
        ).origin;

    const iframe =
        document.createElement("iframe");

    iframe.src =
        widgetOrigin +
        "/widget?key=" +
        encodeURIComponent(widgetKey);

    iframe.title =
        "HyrvoAI AI Helpdesk";

    iframe.style.position = "fixed";
    iframe.style.right = "20px";
    iframe.style.bottom = "20px";
    iframe.style.width = "430px";
    iframe.style.height = "700px";
    iframe.style.border = "none";
    iframe.style.background = "transparent";
    iframe.style.zIndex = "2147483647";
    iframe.style.colorScheme = "light";

    iframe.setAttribute(
        "allow",
        "clipboard-write"
    );

    document.body.appendChild(iframe);

    function updateIframeSize() {

        if (window.innerWidth <= 600) {

            iframe.style.right = "0";
            iframe.style.bottom = "0";
            iframe.style.width = "100vw";
            iframe.style.height = "100vh";

        } else {

            iframe.style.right = "20px";
            iframe.style.bottom = "20px";
            iframe.style.width = "430px";
            iframe.style.height = "700px";
        }
    }

    updateIframeSize();

    window.addEventListener(
        "resize",
        updateIframeSize
    );
})();