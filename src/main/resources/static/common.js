class Log {
    constructor(name) {
        this.name = name;
    }

    log(...message) {
        if (!this.name) {
            console.log(message);
        } else {
            console.log(this.name, message);
        }
    }

    warn(...message) {
        if (!this.name) {
            console.warn(message);
        } else {
            console.warn(this.name, message);
        }
    }
}

var log = new Log();
var modalWindow;

class HttpCommunicator {
    constructor(name) {
        this.log = new Log(name);
    }

    overrideRequest(xhttp) {
        let callback = xhttp.onreadystatechange;
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                loadingWindow.hide();
                callback.call(xhttp);
            }
        }
        loadingWindow.show();
    }

    sendJson(xhttp, url, value) {
        this.overrideRequest(xhttp);
        xhttp.open('POST', url, true);
        xhttp.setRequestHeader('Content-type', 'application/json');
        if (value !== undefined) {
            let body = JSON.stringify(value);
            this.log.log('POST', url, body)
            xhttp.send(body);
        } else {
            this.log.log('POST', url)
            xhttp.send();
        }
    }

    sendPost(xhttp, url, content) {
        this.overrideRequest(xhttp);
        xhttp.open('POST', url, true);
        xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        if (content !== undefined) {
            this.log.log('POST', url, content)
            xhttp.send(content);
        } else {
            this.log.log('POST', url)
            xhttp.send();
        }
    }

    sendGet(xhttp, url) {
        this.overrideRequest(xhttp);
        xhttp.open('GET', url, true);
        this.log.log('GET', url)
        xhttp.send();
    }
}

class WebForm extends HttpCommunicator {
    constructor(name) {
        super(name);
        this.name = name;
        this.log = new Log(name);
        this.element = document.getElementById(name);

        this.callback = undefined;
    }

    beforeShow() {
        this.log.log('beforeShow');
        return true;
    }

    show(callback) {
        if (callback !== undefined) {
            this.callback = callback;
        }
        if (this.beforeShow()) {
            if (modalWindow !== undefined) {
                modalWindow.hide();
            }
            modalWindow = this;
            this.element.style.display='block';
        }
    }

    hide() {
        if (modalWindow === this) {
            this.log.log('hide');
            modalWindow = undefined;
            this.element.style.display='none';
            return true;
        }
        return false;
    }

    runCallback() {
        if (this.callback !== undefined) {
            this.callback();
            this.callback = undefined;
        }
    }

    isShown() {
        return this.element.style.display === 'block';
    }

}

class WebControl extends HttpCommunicator {
    constructor(name) {
        super(name)
        this.control = document.getElementById(name);
        this.log = new Log(name);
        this.loaded = false;
    }

    load() {
        if (!this.loaded) {
            this.log.log('loadData');
            this.loadData();
        }
        this.loaded = true;
    }

    reset() {
        this.loaded = false;
    }

    loadData() {
    }
}

function playSound(audio) {
    audio.volume = 0.2;
    audio.play();
}

function disableAllButtons(element) {
    let node = element.firstChild;
    do {
        if (node.tagName === 'BUTTON') {
            node.disabled = true;
        }
        node = node.nextSibling;
    } while (node);
}

function onSelectAll(id) {
    forInputs(id, 'checkbox', function(checkbox) {checkbox.checked = true})
}

function onInvertAll(id) {
    forInputs(id, 'checkbox', function(checkbox) {checkbox.checked = !checkbox.checked})
}

function forInputs(id, inputType, callback) {
    let element = document.getElementById(id);
    let node = element.firstChild;
    do {
        if (node.tagName === 'FORM') {
            let form = node;
            for (let i = 0; i < form.length; i++) {
                if (form[i].type === inputType) {
                    let input = form[i];
                    callback(input);
                }
            }
        }
        node = node.nextSibling;
    } while (node);
}

function setLabelClass(input, labelClass) {
    if (input.labels) {
        input.labels.forEach(function(label) {label.className=labelClass})
    }
}

function goTo(url) {
    document.location.href = url;
}

function getWebsocketProtocol() {
    let protocol = window.location.protocol;
    if (protocol.startsWith('https')) {
        return 'wss';
    } else {
        return 'ws';
    }
}

function getUrl() {
    let url = window.location.hostname;
    if (window.location.port) {
        url += ':' + window.location.port;
    }
    return url;
}

