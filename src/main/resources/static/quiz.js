
class Log {
    constructor(name) {
        this.name = name;
    }

    log(...message) {
        if (name === undefined) {
            console.log(message);
        } else {
            console.log(name, message);
        }
    }

    warn(...message) {
        if (name === undefined) {
            console.warn(message);
        } else {
            console.warn(name, message);
        }
    }
}

var log = new Log();

function keyPressed(event) {
    event = !event ? window.event : event;
    if (event.code === 'Enter') {
        if (modalWindow.element === messageWindow.element) {
            messageWindow.button.onclick();
        } else if (modalWindow.element === loginWindow.element) {
            loginWindow.loginButton.onclick();
        }
    }
}

function showMessageWindow(text, afterAction) {
    messageWindow.text.innerHTML = text;
    messageWindow.callback = afterAction;
    showModalWindow(messageWindow.element);
}

function hideMessageWindow() {
    hideModalWindow();
    if (messageWindow.callback !== undefined) {
        messageWindow.callback();
    }
}

function showMainMenu() {
    mainMenu.buttonQuiz.disabled = false;
    mainMenu.buttonCreate.disabled = false;
    hideModalWindow();
    showModalWindow(mainMenu.element);
    mainMenu.buttonQuiz.focus();
}

function showModalWindow(element) {
    modalWindow = {element:null}
    if (element === undefined) {
        modalWindow.element = mainMenu;
    } else {
        modalWindow.element = element;
    }
    modalWindow.element.style.display='block';
}

function hideModalWindow() {
    if (modalWindow !== undefined) {
        modalWindow.element.style.display='none';
        modalWindow = undefined;
    }
}

function goTo(url) {
    document.location.href = url;
}

function showLoginWindow(callback) {
    loginWindow.loginButton.disabled = false;
    if (callback !== undefined) {
        loginWindow.callback = callback;
    }
    loginWindow.userName.value = localStorage.getItem("login");
    hideModalWindow();
    showModalWindow(loginWindow.element);
    if (!loginWindow.userName.value) {
        loginWindow.userName.focus();
    } else {
        loginWindow.userPassword.focus();
    }
}

function showSelectQuiz() {
    hideModalWindow();
    showModalWindow(quizListWindow.element);
}

function showQuizWindow() {
    hideModalWindow();
    showModalWindow(quizWindow.element);
    quizWindow.startButton.focus();
}

function searchQuiz() {
    doRequestLoadQuizList(quizListWindow.quizName.value);
}

function showQuizList(reload) {
    mainMenu.buttonQuiz.disabled = true;
    if (reload !== undefined && !reload) {
        showSelectQuiz();
    } else {
        doRequestLoadQuizList();
    }
}

function showQuestionWindow(participant) {
    questionWindow.view.innerHTML = quizWindow.view.innerHTML;
    questionWindow.finished = false;

    let url = 'ws://' + getUrl() + '/websocket';
    webSocket = new WebSocket(url);
    webSocket.onopen = function () {
        webSocket.send(JSON.stringify({enter:{participantId:participant.id}}));
    }
    webSocket.onmessage = messageHandler;
    webSocket.onclose = function () {
        log.log("connection closed by server");
        showQuizWindow();
    }

    hideModalWindow();
    showModalWindow(questionWindow.element);
}

function closeQuestionWindow() {
    stopPlaySound();
    if (webSocket && webSocket.readyState === WebSocket.OPEN) {
        webSocket.onclose = null;
        webSocket.close();
        webSocket = null;
        log.log("close connection");
    }
    if (questionWindow.finished) {
        showQuizList(true);
    } else {
        showQuizWindow();
    }
}

function messageHandler(event) {
    let msg = JSON.parse(event.data);
    if (msg.quiz) {
        questionWindow.view.innerHTML = '';
        questionWindow.view.insertAdjacentHTML("beforeend", '<p> Викторина ' + msg.quiz.name + '</p>');
        questionWindow.view.insertAdjacentHTML("beforeend", '<p> начнется ' + new Date(msg.quiz.startDate) + '</p>');
    } else if (msg.question) {
        questionWindow.question = msg.question;
        writeQuestion(questionWindow.view, questionWindow.question);
        playSound(questionWindow.sounds.tickTack);
    } else if (msg.answer) {
        //QuizAnswerStatus
        stopPlaySound();
        if (msg.answer.status === 'SUCCESS') {
            playSound(questionWindow.sounds.right);
        } else {
            playSound(questionWindow.sounds.wrong);
        }
        if (!questionWindow.question) {
            questionWindow.question = msg.answer.question;
        }
        writeQuestion(questionWindow.view, questionWindow.question, msg.answer.rightAnswer, msg.answer.answer);
        disableAllButtons(questionWindow.view);
    } else if (msg.result) {
        questionWindow.finished = true;
        questionWindow.view.innerHTML = '';
        questionWindow.view.insertAdjacentHTML("beforeend", '<p>Результат</p>');
        questionWindow.view.insertAdjacentHTML("beforeend", '<p>место: ' + msg.result.place + '</p>');
        questionWindow.view.insertAdjacentHTML("beforeend", '<p>верных ответов: ' + msg.result.right + '</p>');
        questionWindow.view.insertAdjacentHTML("beforeend", '<p>неверных ответов: ' + msg.result.wrong + '</p>');
        questionWindow.view.insertAdjacentHTML("beforeend", '<p>очков: ' + msg.result.value + '</p>');
    }
}

function writeQuestion(element, question, rightAnswer, answer) {
    element.innerHTML = '';
    element.insertAdjacentHTML("beforeend", '<p>' + question.category + '</p>');
    if (question.total !== undefined) {
        element.insertAdjacentHTML("beforeend", '<p>Вопрос №' + question.number + ' из ' + question.total + '</p>');
    } else {
        element.insertAdjacentHTML("beforeend", '<p>Вопрос №' + question.number + '</p>');
    }
    element.insertAdjacentHTML("beforeend", '<p>' + question.text + '</p>');
    question.options.forEach(function (option) {
        let buttonClass = 'answer-button';
        if (rightAnswer !== undefined) {
            if (option.id === rightAnswer) {
                buttonClass = buttonClass + ' color-success';
            } else if (option.id === answer) {
                buttonClass = buttonClass + ' color-fail';
            }
        }
        element.insertAdjacentHTML("beforeend",
            '<button onclick="doRequestAnswer()" type="button" value="' + option.id + '" class="' + buttonClass + '">' + option.name + '</button>');
    })

}

function playSound(audio) {
    audio.volume = 0.2;
    audio.play();
}

function stopPlaySound() {
    questionWindow.sounds.tickTack.pause();
}

function doRequestAnswer(event) {
    event = event || window.event; // IE
    let target = event.target || event.srcElement; // IE
    let answerId = Number.parseInt(target.value);
    if (webSocket) {
        webSocket.send(JSON.stringify({answer:answerId}))
    }
    stopPlaySound();
    disableAllButtons(questionWindow.view);
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

function doRequestNext() {
    if (webSocket) {
        webSocket.send(JSON.stringify({next:true}))
    }
}

function getUrl() {
    let url = window.location.hostname;
    if (window.location.port) {
        url += ':' + window.location.port;
    }
    return url;
}

function doRequestLogin() {
    if (loginWindow.userName.value == null || loginWindow.userName.value === ''
        || loginWindow.userPassword.value == null || loginWindow.userPassword.value === '') {
        return;
    }
    loginWindow.loginButton.disabled = true;
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
            hideModalWindow();
            if (this.status === 200) {
                log.log('RAW RESP: ' + this.responseText);
                let user = JSON.parse(this.responseText);
                localStorage.setItem("login", user.login);
                log.log('Logged-In Successfully');
                personalInfo.element.innerHTML='';
                personalInfo.element.insertAdjacentHTML('beforeend', '<p>' + user.nickname + '</p>')
                if (loginWindow.callback !== undefined) {
                    loginWindow.callback();
                }
            } else {
                log.warn('Login Failed: ' + this.status);
                showMessageWindow('ERROR:\n' + this.responseText, showLoginWindow);
            }
        }
    };
    xhttp.open('POST', '/', true);
    xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhttp.send('login=' + loginWindow.userName.value + '&password=' + loginWindow.userPassword.value);
    loginWindow.userPassword.value = '';
}

function onSelectQuiz(event) {
    event = event || window.event; // IE
    let target = event.target || event.srcElement; // IE
    let quizId = Number.parseInt(target.value);
    quizWindow.quizId = quizId;
    let quiz = undefined;
    quizList.forEach(function (q) {
        if (q.id === quizId) {
            quiz = q;
        }
    })
    if (quiz !== undefined) {
        quizWindow.view.innerHTML = '';
        quizWindow.view.insertAdjacentHTML('beforeend', '<p>' + quiz.name + '</p>');
        quizWindow.view.insertAdjacentHTML('beforeend', '<p>Игроков: ' + quiz.minPlayers + ' - ' + quiz.maxPlayers + '</p>');
        if (quiz.selectionStrategy === 'ALL') {
            quizWindow.view.insertAdjacentHTML('beforeend', '<p>Все вопросы</p>');
        } else {
            quizWindow.view.insertAdjacentHTML('beforeend', '<p>Вопросов: ' + quiz.questionsNumber + '</p>');
        }
        hideModalWindow();
        showModalWindow(quizWindow.element)
    }
}

function doRequestRegisterQuiz() {
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                let quiz = JSON.parse(xhttp.responseText);
                if (quiz.result) {
                    showQuestionWindow(quiz.result);
                } else {
                    //todo not active
                }
            } else if (this.status === 401) {
                showLoginWindow(function () {showModalWindow(quizWindow.element)});
            }
        }
    };
    xhttp.open('GET', '/quiz/' + quizWindow.quizId + '/register', true);
    xhttp.send();
}

function doRequestLoadQuizList(name) {
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                quizList = JSON.parse(xhttp.responseText);
                quizListWindow.quizList.innerHTML = "";
                quizList.forEach(function(quiz) {
                    quizListWindow.quizList.insertAdjacentHTML('beforeend',
                        '<button type="button" onclick="onSelectQuiz()" value="' + quiz.id + '" class="dialog-button">' + quiz.name + '</button>');
                })
                showSelectQuiz();
            } else if (this.status === 401) {
                showLoginWindow(function () {showModalWindow(quizListWindow.element)});
            } else {
                log.warn('problem to load quiz list: ', xhttp.responseText);
                showMessageWindow(xhttp.responseText, showMainMenu);
            }
        }
    };
    if (name === undefined) {
        xhttp.open('POST', '/quiz/list', true);
        xhttp.send();
    } else {
        xhttp.open('POST', '/quiz/list', true);
        xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhttp.send('name=' + name);
    }
}