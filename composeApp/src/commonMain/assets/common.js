// This is the common js file for Chouten, containing needed js functions
// to allow modules to run smoothly.

// sendRequest(url: String, method: String, headers: {String: String}?, body: String?)
async function request(url, method, headers) {
    return JSON.parse(RelayBridge.request(url, method, headers))
}

// Override console.log
console.log = function (...args) {
    const stack = new Error().stack.split("\n")[2]; // Get caller info
    const match = stack.match(/:(\d+):(\d+)/); // Extract line/column

    if (match) {
        const jsLine = parseInt(match[1], 10);
        const jsColumn = parseInt(match[2], 10);

        // Print with TypeScript file location
        RelayBridge.consoleLog(`[JS:${jsLine}:${jsColumn}] `.concat(args.join(" ")));
    } else {
        RelayBridge.consoleLog(`[JS:${jsLine}:${jsColumn}] `.concat(args.join(" ")));
    }
};