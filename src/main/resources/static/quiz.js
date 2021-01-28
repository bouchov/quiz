class MessageWindow extends WebForm {
    constructor() {
        super('messageWindow');
        this.text = document.getElementById('messageWindow-text');
        this.submit = document.getElementById('messageWindow-submit');
    }

    showMessage(text, callback) {
        this.text.innerHTML = text;
        if (callback === undefined) {
            callback = function () {mainMenu.show()}
        }
        this.show(callback);
    }

    show(callback) {
        super.show(callback);
        this.submit.focus();
    }

    dismiss() {
        if (this.hide()) {
            this.runCallback();
        }
    }
}

class MainMenu extends WebForm {
    constructor() {
        super('mainMenu');
        this.quiz = document.getElementById('mainMenu-quiz');
        this.createQuiz = document.getElementById('mainMenu-createQuiz');
        this.createQuestion = document.getElementById('mainMenu-createQuestion');
        this.login = document.getElementById('mainMenu-login');
    }

    beforeShow() {
        super.beforeShow();
        this.quiz.disabled = false;
        if (personalInfo.user && personalInfo.user.role === 'ADMIN') {
            this.createQuiz.disabled = false;
            this.createQuestion.disabled = false;
            this.createQuiz.style.display = 'inline';
            this.createQuestion.style.display = 'inline';
        } else {
            //admin required
            this.createQuiz.disabled = true;
            this.createQuestion.disabled = true;
            this.createQuiz.style.display = 'none';
            this.createQuestion.style.display = 'none';
        }
        this.login.disabled = false;
    }

    show() {
        super.show();
        this.quiz.focus();
    }
}

class PersonalInfo extends WebForm {
    KEY_USER = 'KEY_USER';

    constructor() {
        super('personalInfo');

        let jsonUser = localStorage.getItem(this.KEY_USER)
        if (!jsonUser) {
            this.user = {nickname: 'Гость'};
        } else {
            this.user = JSON.parse(jsonUser);
        }
        this.element.innerHTML='<p>' + this.user.nickname + '</p>';
    }

    saveUser(user) {
        this.user = user;
        localStorage.setItem(this.KEY_USER, JSON.stringify(user));
        this.element.innerHTML='<p>' + user.nickname + '</p>';
    }

    isAdmin() {
        return this.user && this.user.role === 'ADMIN';
    }


    show() {
        //always shown
    }


    hide() {
        return false;
    }
}

class LoginWindow extends WebForm {
    KEY_LOGIN = 'KEY_LOGIN'

    constructor() {
        super('loginWindow');
        this.element = document.getElementById('loginWindow');
        this.userName = document.getElementById('loginWindow-name');
        this.userPassword = document.getElementById('loginWindow-password');
        this.submitButton = document.getElementById('loginWindow-submit');

        this.userName.value = localStorage.getItem(this.KEY_LOGIN);
    }

    saveLogin(login) {
        localStorage.setItem(this.KEY_LOGIN, login);
        this.userName.value = login;
    }

    beforeShow() {
        super.beforeShow();
        this.submitButton.disabled = false;
    }


    show(callback) {
        if (callback === undefined) {
            callback = function () {mainMenu.show()};
        }
        super.show(callback);
        if (!this.userName.value) {
            this.userName.focus();
        } else {
            this.userPassword.focus();
        }
    }

    doSubmit() {
        if (this.userName.value == null || this.userName.value === ''
            || this.userPassword.value == null || this.userPassword.value === '') {
            return false;
        }
        this.submitButton.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                form.hide();
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let user = JSON.parse(this.responseText);
                    form.log.log('Logged-In Successfully');
                    personalInfo.saveUser(user);
                    form.saveLogin(user.login);
                    form.runCallback();
                } else {
                    form.log.warn('Login Failed: ' + this.status);
                    messageWindow.showMessage(this.responseText, function() {form.show()});
                }
            }
        };
        this.sendPost(xhttp, '/', 'login=' + this.userName.value + '&password=' + this.userPassword.value);
        this.userPassword.value = '';
    }
}

class RegisterWindow extends WebForm {
    constructor() {
        super('registerWindow');
        this.userName = document.getElementById('registerWindow-name');
        this.nickname = document.getElementById('registerWindow-nickname');
        this.userPassword = document.getElementById('registerWindow-password');
        this.submit = document.getElementById('registerWindow-submit');
    }

    beforeShow() {
        super.beforeShow();
        this.submit.disabled = false;
        this.callback = loginWindow.callback;
        if (this.userName.value === '') {
            this.userName.value = loginWindow.userName.value;
        }
    }


    show(callback) {
        super.show(callback);
        if (this.userName.value) {
            this.userName.focus();
        } else {
            this.userPassword.focus();
        }
    }

    doSubmit() {
        if (this.userName.value == null || this.userName.value === ''
            || this.nickname.value == null || this.nickname.value === ''
            || this.userPassword.value == null || this.userPassword.value === '') {
            return;
        }
        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                form.hide();
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let user = JSON.parse(this.responseText);
                    form.log.log('Register Successfully');
                    personalInfo.saveUser(user);
                    loginWindow.saveLogin(user.login);
                    form.runCallback();
                } else {
                    form.log.warn('Registration Failed: ' + this.status);
                    messageWindow.showMessage(this.responseText, function () {form.show()});
                }
            }
        };
        this.sendPost(xhttp, '/register',
            'login=' + this.userName.value +
            '&password=' + this.userPassword.value +
            '&nickname=' + this.nickname.value);
        this.userPassword.value = '';
    }
}

class QuizListWindow extends WebForm {
    constructor() {
        super('quizListWindow');
        this.quizName = document.getElementById('quizListWindow-quizName');
        this.quizList = document.getElementById('quizListWindow-quizList');
        this.quizArray = [];
        this.reload = true;
    }

    beforeShow() {
        super.beforeShow();
        mainMenu.quiz.disabled = true;
        if (this.reload) {
            this.loadQuizList(this.quizName.value);
        }
    }

    show(callback) {
        super.show(callback);
    }

    loadQuizList(name) {
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    form.reload = false;
                    form.quizArray = JSON.parse(xhttp.responseText);
                    form.quizList.innerHTML = '';
                    form.quizArray.forEach(function(quiz) {
                        let quizName = quiz.name;
                        if (quiz.status === 'DRAFT') {
                            quizName += ' *';
                        }
                        form.quizList.insertAdjacentHTML('beforeend',
                            '<button type="button" onclick="onSelectQuiz()" value="' + quiz.id + '" class="dialog-button">' + quizName + '</button>');
                    })
                    form.show();
                } else if (this.status === 401) {
                    loginWindow.show(function () {form.show()});
                } else {
                    form.log.warn('problem to load quiz list: ', xhttp.responseText);
                    messageWindow.showMessage(xhttp.responseText);
                }
            }
        };
        if (!name) {
            this.sendPost(xhttp, '/quiz/list');
        } else {
            this.sendPost(xhttp, '/quiz/list', 'name=' + name);
        }
    }

    getQuiz(quizId) {
        let quiz = undefined;
        this.quizArray.forEach(function (q) {
            if (q.id === quizId) {
                quiz = q;
            }
        })
        return quiz;
    }

}

class QuizWindow extends WebForm {
    constructor() {
        super('quizWindow');
        this.view = document.getElementById('quizWindow-view');
        this.startButton = document.getElementById('quizWindow-start');
        this.editButton = document.getElementById('quizWindow-edit');
        this.quizId = undefined;
        this.quiz = {};
    }

    setQuiz(quiz) {
        if (quiz !== undefined) {
            this.quizId = quiz.id;
            this.quiz = quiz;
            this.view.innerHTML = '';
            this.view.insertAdjacentHTML('beforeend', '<p>' + quiz.name + '</p>');
            this.view.insertAdjacentHTML('beforeend', '<p>Игроков: ' + quiz.minPlayers + ' - ' + quiz.maxPlayers + '</p>');
            if (quiz.selectionStrategy === 'ALL') {
                this.view.insertAdjacentHTML('beforeend', '<p>Все вопросы</p>');
            } else {
                this.view.insertAdjacentHTML('beforeend', '<p>Вопросов: ' + quiz.questionsNumber + '</p>');
            }
            if (quiz.status === 'FINISHED') {
                this.view.insertAdjacentHTML('beforeend', '<p>Викторина окончена</p>');
            }
        }
    }

    registerQuiz() {
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let quiz = JSON.parse(xhttp.responseText);
                    if (quiz.result) {
                        questionWindow.setParticipant(quiz.result);
                        questionWindow.show();
                    } else {
                        //todo not active
                    }
                } else if (this.status === 401) {
                    loginWindow.show(function () {form.show()});
                }
            }
        };
        this.sendGet( xhttp, '/quiz/' + this.quizId + '/register');
    }

    beforeShow() {
        super.beforeShow();
        if (personalInfo.isAdmin() && this.quiz.status === 'DRAFT') {
            this.editButton.style.display = 'inline';
        } else {
            this.editButton.style.display = 'none';
        }
        if (this.quiz.status === 'ACTIVE') {
            this.startButton.style.display = 'inline';
        } else {
            this.startButton.style.display = 'none';
        }
    }
}

class EditQuizWindow extends WebForm {
    constructor() {
        super('editQuizWindow');
        this.name = document.getElementById('editQuizWindow-name')
        this.minPlayers = document.getElementById('editQuizWindow-minPlayers')
        this.maxPlayers = document.getElementById('editQuizWindow-maxPlayers')
        this.questionType = document.getElementById('editQuizWindow-questionType')
        this.questionsNumber = document.getElementById('editQuizWindow-questionsNumber')
        this.status = document.getElementById('editQuizWindow-status')
        this.selectQuestions = document.getElementById('editQuizWindow-selectQuestions')
        this.submit = document.getElementById('editQuizWindow-submit')
        this.quiz = {}
        this.quizId = undefined;
    }

    setNewQuiz() {
        let quiz = {id: null,
            name: '',
            minPlayers: 1,
            maxPlayers: 2,
            selectionStrategy: 'EMPTY',
            questionsNumber: 0,
            status: 'DRAFT'};
        this.setQuiz(quiz);
    }

    setQuiz(quiz) {
        this.quizId = quiz.id;
        this.quiz = quiz;

        this.name.value = quiz.name;
        this.minPlayers.value = quiz.minPlayers;
        this.maxPlayers.value = quiz.maxPlayers;
        this.questionType.value = quiz.selectionStrategy;
        this.questionsNumber.value = quiz.questionsNumber;
        this.status.value = quiz.status;
    }

    saveQuiz() {
        //validate !!!
        let minPlayers = Number.parseInt(this.minPlayers.value);
        let maxPlayers = Number.parseInt(this.maxPlayers.value);
        let questionsNumber = Number.parseInt(this.questionsNumber.value);

        this.submit.disabled = true;
        let quiz = {
            id               :this.quizId,
            name             :this.name.value,
            type             :'SIMPLE',
            minPlayers       :minPlayers,
            maxPlayers       :maxPlayers,
            questionsNumber  :questionsNumber,
            selectionStrategy:this.questionType.value,
            status           :this.status.value
        };

        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                form.hide();
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let quiz = JSON.parse(xhttp.responseText);
                    form.setQuiz(quiz);
                    form.log.log('quiz saved: ', xhttp.responseText);
                    quizListWindow.reload = true;
                    messageWindow.showMessage('Викторина успешно сохранена', function () {form.show()});
                } else if (this.status === 401) {
                    loginWindow.show(function () {form.show()});
                } else {
                    form.log.warn('error save quiz: ', xhttp.responseText);
                    messageWindow.showMessage(xhttp.responseText, function () {form.show()});
                }
            }
        };
        let url;
        if (quiz.id) {
            url =  '/quiz/' + quiz.id + '/edit';
        } else {
            url =  '/quiz/create';
        }
        this.sendJson(xhttp, url, quiz);
    }

    beforeShow() {
        super.beforeShow();
        this.submit.disabled = false;
        if (this.quiz.id && this.quiz.selectionStrategy === 'QUIZ' && this.quiz.status === 'DRAFT') {
            this.selectQuestions.disabled = false;
            this.selectQuestions.style.display = 'inline';
        } else {
            this.selectQuestions.disabled = true;
            this.selectQuestions.style.display = 'none';
        }
    }
}

class CategoryControl extends WebControl {
    constructor(name) {
        super(name);
    }

    setCategory(categoryId) {
        this.control.value = categoryId;
    }

    getCategory() {
        return Number.parseInt(this.control.value);
    }

    loadData() {
        let form = this;
        let xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let categories = JSON.parse(xhttp.responseText);
                    form.loadCategoryListData(categories);
                } else {
                    form.reset();
                    form.log.warn('error load categories: ', xhttp.responseText);
                }
            }
        };
        this.sendJson(xhttp, '/categories/list');
    }

    loadCategoryListData(categories) {
        this.control.innerHTML = '<option selected value="0">Все категории</option>';
        let form = this;
        categories.forEach(function(category){
            form.control.insertAdjacentHTML('beforeend',
                '<option value="' + category.id + '">' + category.name + '</option>');
        });
    }
}

class QuestionListWindow extends WebForm {
    constructor() {
        super('questionListWindow');
        this.category = new CategoryControl('questionListWindow-category')
        this.view = document.getElementById('questionListWindow-view');
        this.nextPage = document.getElementById('questionListWindow-nextPage');
        this.prevPage = document.getElementById('questionListWindow-prevPage');
        this.submit = document.getElementById('questionListWindow-submit');

        this.pageNumber = 0;
        this.total = undefined;
        this.pageSize = 10;
        this.quizId = undefined;

        this.view.innerHTML = 'Список пуст';
    }

    resetPage() {
        this.pageNumber = 0;
        this.total = undefined;
    }

    setQuizId(quizId) {
        this.quizId = quizId;
        this.view.innerHTML = 'Список пуст';
        this.resetPage();
        this.category.load();
    }

    beforeShow() {
        super.beforeShow();
        if (this.pageNumber <= 0) {
            this.prevPage.disabled = true;
        } else {
            this.prevPage.disabled = false;
        }
        if (this.total !== undefined) {
            this.submit.disabled = false;
            if (this.pageNumber + 1 >= this.total) {
                this.nextPage.disabled = true;
            } else {
                this.nextPage.disabled = false;
            }
        } else {
            this.submit.disabled = true;
            this.nextPage.disabled = false;
            this.loadQuestionList();
        }
    }

    saveQuestionList() {
        let addedQuestions = [];
        let removedQuestions = [];
        forInputs(this.element.id, 'checkbox', function(checkbox){
            if (checkbox.checked) {
                addedQuestions.push(checkbox.value);
            } else {
                removedQuestions.push(checkbox.value);
            }
        });
        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                form.hide();
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let questionIds = JSON.parse(xhttp.responseText);
                    form.updateQuestionListData(questionIds);
                    form.show();
                } else if (this.status === 401) {
                    loginWindow.show(function () {form.show()});
                } else {
                    form.log.warn('error save questions: ', xhttp.responseText);
                    messageWindow.showMessage(xhttp.responseText, function () {form.show()});
                }
            }
        };
        this.sendJson(xhttp, '/quiz/' + this.quizId + '/questions',
            {added: addedQuestions, removed: removedQuestions});
    }

    loadNextPage(inc) {
        let nextPage = this.pageNumber + inc;
        if (nextPage >= 0) {
            if (this.total !== undefined) {
                if (nextPage < this.total) {
                    this.pageNumber = nextPage;
                    this.loadQuestionList();
                }
            }
        }
    }

    loadQuestionList() {
        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                form.hide();
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let questionsPage = JSON.parse(xhttp.responseText);
                    form.loadQuestionListData(questionsPage);
                    form.show();
                } else if (this.status === 401) {
                    loginWindow.show(function () {form.show()});
                } else {
                    form.log.warn('error load questions: ', xhttp.responseText);
                    messageWindow.showMessage(xhttp.responseText, function () {form.show()});
                }
            }
        };
        let categoryId = null;
        if (this.category.getCategory() > 0) {
            categoryId = this.category.getCategory();
        }
        this.sendJson(xhttp, '/questions/list', {
            categoryId: categoryId,
            quizId: this.quizId,
            page: this.pageNumber,
            size: this.pageSize
        });
    }

    loadQuestionListData(page) {
        this.view.innerHTML = '';
        this.total = page.total;
        this.pageNumber = page.page;
        if (!page.elements || page.elements.length === 0) {
            this.view.innerHTML = 'Список пуст';
        } else {
            let form = this;
            page.elements.forEach(function (question) {
                let checked = '';
                if (question.selected) {
                    checked = 'checked';
                }
                form.view.insertAdjacentHTML('beforeend',
                    '<div class="table-row">' +
                    '  <div class="table-cell" style="width: 2em"><div class="table-cell-content">' +
                    '    <input type="checkbox" name="questions" ' + checked + ' value="' + question.id + '" id="questionListWindow-question' + question.id + '">' +
                    '  </div></div>' +
                    '  <div class="table-cell"><div class="table-cell-content">' +
                    '    <label for="questionListWindow-question' + question.id + '">' + question.category + ':' + question.text + '</label>' +
                    '  </div></div>' +
                    '</div>');
            })
        }
    }

    updateQuestionListData(questionIds) {
        forInputs(this.element.id, 'checkbox', function(checkbox) {
            let id = Number.parseInt(checkbox.value);
            checkbox.checked = questionIds.indexOf(id) >= 0;
        });
    }
}

class EditQuestionWindow extends WebForm {
    constructor() {
        super('editQuestionWindow');
        this.category = new CategoryControl('editQuestionWindow-category');
        this.text = document.getElementById('editQuestionWindow-text');
        this.value = document.getElementById('editQuestionWindow-value');
        this.options = document.getElementById('editQuestionWindow-options');
        this.submit = document.getElementById('editQuestionWindow-submit');

        this.questionId = null;
        this.question = {id:null, categoryId:0, text:'', value:1, answer:undefined, options:[{id:0, name:'текст ответа 0'}]};
    }

    setNewQuestion() {
        this.setQuestion({id:null, categoryId:0, text:'', value:1, answer:undefined, options:[{id:0, name:'текст ответа 0'}]});
    }

    setQuestion(question) {
        this.questionId = question.id;
        this.question = question;
        this.category.load();
        this.category.setCategory(question.categoryId);
        this.text.value = question.text;
        this.value.value = question.value;
        this.writeOptions();
    }

    beforeShow() {
        super.beforeShow();
        this.submit.disabled = false;
    }

    writeOptions() {
        this.options.innerHTML = '';
        let form = this;
        this.question.options.forEach(function (option) {form.addOptionTag(option);});
    }

    addOption() {
        let maxId = -1;
        forInputs(this.element.id, 'radio', function (radio) {
            if (radio.name === 'options') {
                maxId = Math.max(radio.value, maxId);
            }
        })
        let id = maxId + 1;
        this.addOptionTag({id:id, name:'текст ответа ' + id});
    }

    addOptionTag(option) {
        let id = option.id;
        let idPrefix = this.element.id;
        let checked = '';
        if (id === this.question.answer) {
            checked = ' checked';
        }
        this.options.insertAdjacentHTML('beforeend',
            '<div class="table-row" id="' + idPrefix + '-option' + id + '-row">' +
            '  <div class="table-cell" style="width: 20%"><div class="table-cell-content, right_label">' +
            '    <input type="radio" name="options" value="' + id + '"' + checked + '>' +
            '  </div></div>' +
            '  <div class="table-cell" style="width: 70%"><div class="table-cell-content">' +
            '    <input type="text" name="option-text' + id + '" required>' +
            '  </div></div>' +
            '  <div class="table-cell" style="width: 10%"><div class="table-cell-content">' +
            '    <button onclick="onRemoveQuestionOption(' + id + ')" type="button" class="remove-button">&times;</button>' +
            '  </div></div>' +
            '</div>');
        let textName = 'option-text' + id;
        forInputs(this.element.id, 'text', function (text) {
            if (text.name === textName) {
                text.value = option.name;
            }
        })
    }

    removeOption(id) {
        let element = document.getElementById(this.element.id + '-option' + id + '-row');
        if (element) {
            element.remove();
        }
    }

    doSubmit() {
        if (this.category.getCategory() <= 0
            || this.text.value.length <= 0
            || this.value.value <= 0) {
            return false;
        }
        let answer = undefined;
        forInputs(this.element.id, 'radio', function (radio) {
            if (radio.name === 'options' && radio.checked) {
                answer = radio.value;
            }
        });
        if (answer === undefined) {
            return false;
        }
        let categoryId = this.category.getCategory();
        let options = [];
        forInputs(this.element.id, 'text', function (text) {
            if (text.name.startsWith('option-text')) {
                let id = Number.parseInt(text.name.substr('option-text'.length));
                options.push({id:id, name:text.value});
            }
        });
        let value = Number.parseInt(this.value.value);

        let query = {
            id: this.questionId,
            categoryId: categoryId,
            text: this.text.value,
            value: value,
            answer: answer,
            options: options};

        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                form.hide();
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let question = JSON.parse(xhttp.responseText);
                    form.setQuestion(question);
                    messageWindow.showMessage('Вопрос успешно сохранён', function () {form.show()});
                } else if (this.status === 401) {
                    loginWindow.show(function () {form.show()});
                } else {
                    form.log.warn('error save question: ', xhttp.responseText);
                    messageWindow.showMessage(xhttp.responseText, function () {form.show()});
                }
            }
        };
        let url;
        if (query.id) {
            url = '/questions/' + query.id + '/edit';
        } else {
            url = '/questions/create';
        }
        this.sendJson(xhttp, url, query);
    }
}

class QuestionWindow extends WebForm {
    constructor() {
        super('questionWindow');
        this.view = document.getElementById('questionWindow-view');
        this.sounds = {};
        this.sounds.right = document.getElementById('questionWindow-sound-right');
        this.sounds.wrong = document.getElementById('questionWindow-sound-wrong');
        this.sounds.tickTack = document.getElementById('questionWindow-sound-tickTack');

        this.participant = undefined;
        this.webSocket = undefined;
        this.question = undefined;
        this.finished = false;
    }

    setParticipant(participant) {
        this.participant = participant;
    }


    beforeShow() {
        super.beforeShow();
        this.view.innerHTML = quizWindow.view.innerHTML;
        this.finished = false;

        let url = getWebsocketProtocol() + '://' + getUrl() + '/websocket';
        this.webSocket = new WebSocket(url);
        let form = this;
        this.webSocket.onopen = function () {
            form.webSocket.send(JSON.stringify({enter: {participantId: form.participant.id}}));
        }
        this.webSocket.onmessage = playQuizWebsocketMessageHandler;
        this.webSocket.onclose = function () {
            form.log.log("connection closed by server");
            quizWindow.show();
        }
    }

    sendAnswer(answerId) {
        if (this.webSocket) {
            this.webSocket.send(JSON.stringify({answer:answerId}))
        }
        this.stopPlaySound();
        disableAllButtons(this.view);
    }

    close() {
        this.stopPlaySound();
        if (this.webSocket && this.webSocket.readyState === WebSocket.OPEN) {
            this.webSocket.onclose = null;
            this.webSocket.close();
            this.webSocket = null;
            this.log.log("close connection");
        }
        if (this.finished) {
            quizListWindow.reload = true;
            quizListWindow.show();
        } else {
            quizWindow.show();
        }
    }

    stopPlaySound() {
        this.sounds.tickTack.pause();
    }


    sendNext() {
        if (this.webSocket) {
            this.webSocket.send(JSON.stringify({next:true}))
        }
    }

    setQuiz(quiz) {
        this.view.innerHTML = '';
        this.view.insertAdjacentHTML("beforeend", '<p> Викторина ' + quiz.name + '</p>');
        this.view.insertAdjacentHTML("beforeend", '<p> начнется ' + new Date(quiz.startDate) + '</p>');
    }

    setQuestion(question) {
        this.question = question;
        this.writeQuestion();
        playSound(this.sounds.tickTack);
    }

    setAnswer(answer) {
        //QuizAnswerStatus
        this.stopPlaySound();
        if (answer.status === 'SUCCESS') {
            playSound(this.sounds.right);
        } else {
            playSound(this.sounds.wrong);
        }
        if (!this.question) {
            this.question = answer.question;
        }
        this.writeQuestion(answer.rightAnswer, answer.answer);
        disableAllButtons(this.view);
    }

    writeQuestion(rightAnswer, answer) {
        this.view.innerHTML = '';
        this.view.insertAdjacentHTML("beforeend", '<h1>' + this.question.category + '</h1>');
        if (this.question.total !== undefined) {
            this.view.insertAdjacentHTML("beforeend", '<p>Вопрос №' + this.question.number + ' из ' + this.question.total + '</p>');
        } else {
            this.view.insertAdjacentHTML("beforeend", '<p>Вопрос №' + this.question.number + '</p>');
        }
        this.view.insertAdjacentHTML("beforeend", '<p>' + this.question.text + '</p>');
        let form = this;
        this.question.options.forEach(function (option) {
            let buttonClass = 'answer-button';
            if (rightAnswer !== undefined) {
                if (option.id === rightAnswer) {
                    buttonClass = buttonClass + ' color-success';
                } else if (option.id === answer) {
                    buttonClass = buttonClass + ' color-fail';
                }
            }
            form.view.insertAdjacentHTML("beforeend",
                '<button onclick="doRequestAnswer()" type="button" value="' + option.id + '" class="' + buttonClass + '">' + option.name + '</button>');
        })
    }

    setResult(participant) {
        this.participant = participant;
        this.finished = true;
        this.view.innerHTML = '';
        this.view.insertAdjacentHTML("beforeend", '<h1>Результат</h1>');
        this.view.insertAdjacentHTML("beforeend", '<p>место: ' + participant.place + '</p>');
        this.view.insertAdjacentHTML("beforeend", '<p>верных ответов: ' + participant.right + '</p>');
        this.view.insertAdjacentHTML("beforeend", '<p>неверных ответов: ' + participant.wrong + '</p>');
        this.view.insertAdjacentHTML("beforeend", '<p>очков: ' + participant.value + '</p>');
    }
}

function playQuizWebsocketMessageHandler(event) {
    log.log('WEBSOCKET: <<< ' + event.data);
    let msg = JSON.parse(event.data);
    if (msg.quiz) {
        questionWindow.setQuiz(msg.quiz);
    } else if (msg.question) {
        questionWindow.setQuestion(msg.question);
    } else if (msg.answer) {
        questionWindow.setAnswer(msg.answer);
    } else if (msg.result) {
        questionWindow.setResult(msg.result);
    }
}

var mainMenu = new MainMenu();
var personalInfo = new PersonalInfo();
var messageWindow = new MessageWindow();
var loginWindow = new LoginWindow();
var registerWindow = new RegisterWindow();
var quizListWindow = new QuizListWindow();
var quizWindow = new QuizWindow();
var editQuizWindow = new EditQuizWindow();
var questionWindow = new QuestionWindow();
var questionListWindow = new QuestionListWindow();
var editQuestionWindow = new EditQuestionWindow();
