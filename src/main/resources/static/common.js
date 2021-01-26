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

class WebForm {
    constructor(name) {
        this.name = name;
        this.log = new Log(name);
        this.element = document.getElementById(name);

        this.callback = undefined;
    }

    beforeShow() {
        this.log.log("beforeShow");
    }

    show(callback) {
        if (callback !== undefined) {
            this.callback = callback;
        }
        this.beforeShow();
        if (modalWindow !== undefined) {
            modalWindow.hide();
        }
        modalWindow = this;
        this.element.style.display='block';
    }

    hide() {
        if (modalWindow === this) {
            this.log.log("hide");
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
