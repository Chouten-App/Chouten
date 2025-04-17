// This is the common js file for Chouten, containing needed js functions
// to allow modules to run smoothly.

// sendRequest(url: String, method: String, headers: {String: String}?, body: String?)
async function request(url, method, headers = {}, body = null) {
    return JSON.parse(RelayBridge.request(url, method, headers))
}

// Override console.log
console.log = function (...args) {
    RelayBridge.consoleLog(`Console Log: `.concat(args.join(" ")));
};

console.error = function (...args) {
    RelayBridge.consoleLog(`Console Error: `.concat(args.join(" ")));
};