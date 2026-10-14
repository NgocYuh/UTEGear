// Real Chromium/CDP harness; no API interception and no retained JWT frames.
export async function createBrowser({ base, cdp, browsers = [] }) {
    const target = await (await fetch(cdp + '/json/new?about:blank', { method: 'PUT' })).json();
    const socket = new WebSocket(target.webSocketDebuggerUrl), pending = new Map(); let serial = 0;
    await new Promise((resolve,reject) => { socket.onopen=resolve; socket.onerror=reject; });
    const errors = [], stomp = [], requests = [];
    socket.onmessage = event => {
        const value = JSON.parse(event.data); if (value.method === 'Runtime.exceptionThrown') errors.push(value.params.exceptionDetails.text);
        if (value.method === 'Network.requestWillBeSent') {
            const route = new URL(value.params.request.url).pathname;
            if (route.startsWith('/api/')) requests.push({ route, method: value.params.request.method });
        }
        if (value.method === 'Network.webSocketFrameSent' || value.method === 'Network.webSocketFrameReceived') {
            const frame = value.params.response.payloadData;
            // Retain only protocol evidence. Never retain raw frames or JWT header values.
            const command = frame.split('\n')[0].trim();
            if (['CONNECT', 'CONNECTED', 'SUBSCRIBE', 'ERROR'].includes(command)) stomp.push({ command, hasAuthorization: command === 'CONNECT' && /\nAuthorization:Bearer /i.test(frame) });
        }
        const wait = pending.get(value.id); if (wait) { clearTimeout(wait.timer); pending.delete(value.id); value.error ? wait.reject(Error(value.error.message)) : wait.resolve(value.result); }
    };
    const send = (method, params = {}) => new Promise((resolve,reject) => {
        const id=++serial, timer=setTimeout(()=>{pending.delete(id);reject(Error('CDP timeout'));},15000);
        pending.set(id,{resolve,reject,timer}); socket.send(JSON.stringify({id,method,params}));
    });
    const evaluate = async expression => {
        const response=await send('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});
        if(response.exceptionDetails) throw Error('Browser evaluation failed'); return response.result.value;
    };
    const wait = async expression => {
        for(let i=0;i<100;i++){try { if(await evaluate(expression))return; } catch {} await new Promise(resolve=>setTimeout(resolve,100));}
        const status = await evaluate(`document.querySelector('[data-view-status]')?.textContent || ''`).catch(() => '');
        throw Error('UI state timeout: ' + expression.slice(0,100) + (status ? ' :: ' + String(status).slice(0,250) : ''));
    };
    const navigate = async (route, expected = new URL(route,base).pathname) => {
        await send('Page.navigate',{url:base+route});
        await wait(`location.pathname === ${JSON.stringify(expected)} && document.readyState === 'complete' && !!window.UTEGear?.Views`);
    };
    const fill = (selector, values) => evaluate(`(()=>{const form=document.querySelector(${JSON.stringify(selector)});for(const [name,value] of Object.entries(${JSON.stringify(values)}))form.elements.namedItem(name).value=value;form.requestSubmit();})()`);
    const login = async (email,password) => { await navigate('/login'); await fill('[data-auth=login]',{email,password}); await wait(`location.pathname === '/' && !!window.UTEGear?.Session.get() && !document.querySelector('[data-session-controls]').hidden`); };
    await send('Page.enable'); await send('Runtime.enable'); await send('Network.enable');
    // Track real native sockets only, to exercise a dropped connection without mocking frames/API.
    await send('Page.addScriptToEvaluateOnNewDocument', { source: `window.__qaSockets=[];const NativeWebSocket=window.WebSocket;window.WebSocket=class extends NativeWebSocket{constructor(...args){super(...args);window.__qaSockets.push(this);}};` });
    const browser={send,evaluate,wait,navigate,fill,login,errors,stomp,requests,close:async()=>{for(const task of pending.values())clearTimeout(task.timer);socket.close();await fetch(cdp+'/json/close/'+target.id).catch(()=>{});}};
    browsers.push(browser); return browser;
}
