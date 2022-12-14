class MessageWindow extends WebForm {
    constructor() {
        super('messageWindow');
        this.text = document.getElementById('messageWindow-text')
        this.submit = document.getElementById('messageWindow-submit')
        this.spanClose = document.getElementById('messageWindow-span-close')

        let form = this
        this.addSubmitListener(() => {form.dismiss()})
        this.spanClose.addEventListener('click', () => {form.dismiss()})
    }

    showMessage(text, callback) {
        this.text.innerHTML = text;
        if (callback === undefined) {
            callback = function () {clubListWindow.show()}
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

class LoadingWindow extends WebForm {
    constructor() {
        super('loadingWindow');
    }

    show(callback) {
        this.element.style.display='block';
    }

    hide() {
        this.element.style.display='none';
        return true;
    }
}

class ClubListWindow extends PagedWebForm {
    constructor() {
        super('clubListWindow');
        this.pageSize = 5;
        this.clubName = document.getElementById('clubListWindow-clubName');
        this.view = document.getElementById('clubListWindow-view');

        let form = this
        this.addSubmitListener(() => {
            form.reset()
            form.show()
        })

        this.clubArray = [];

        this.view.innerHTML = 'Список пуст';
    }

    beforeShow() {
        enableAllButtons(this.view);
        return super.beforeShow();
    }

    loadPage() {
        let name = this.clubName.value;
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                form.reload = false;
                let data = JSON.parse(xhttp.responseText);
                form.loadClubListData(data);
                form.show();
            } else if (this.status === 401) {
                loginWindow.show(function () {form.show()});
            } else {
                form.log.warn('problem to load club list: ', xhttp.responseText);
                messageWindow.showMessage(xhttp.responseText);
            }
        };

        this.sendJson(xhttp, '/club/list',
            {
                name: name,
                page: this.pageNumber,
                size: this.pageSize
            });
    }

    loadClubListData(page) {
        this.view.innerHTML = '';
        this.total = page.total;
        this.pageNumber = page.page;
        if (!page.elements || page.elements.length === 0) {
            this.view.innerHTML = 'Список пуст';
        } else {
            let form = this;
            this.clubArray = page.elements;
            this.view.innerHTML = '';
            this.clubArray.forEach(function(club) {
                let clubName = club.name;
                if (club.owner) {
                    clubName += ' *';
                }
                form.view.insertAdjacentHTML('beforeend',
                    '<button type="button" onclick="onSelectClub()" value="' + club.id + '" class="dialog-button">' + clubName + '</button>');
            })
        }
    }

    selectClub(clubId) {
        let club = undefined;
        this.clubArray.forEach(function (q) {
            if (q.id === clubId) {
                club = q;
            }
        })
        if (club) {
            disableAllButtons(this.view)
            let form = this;
            let xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = function () {
                form.hide();
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let club = JSON.parse(xhttp.responseText);
                    form.log.log('Club selected: ', club);
                    mainMenu.saveClub(club);
                    mainMenu.show();
                } else {
                    form.log.warn('Club selection failed: ' + this.status);
                    messageWindow.showMessage(this.responseText, function () {form.show()});
                }
            };
            this.sendGet(xhttp, '/club/' + club.id);
        }
    }
}

class CreateClubWindow extends WebForm {
    constructor() {
        super('createClubWindow');
        this.clubName = document.getElementById('createClubWindow-clubName')
        this.submit = document.getElementById('createClubWindow-submit')

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})
    }

    beforeShow() {
        this.submit.disabled = false;

        return super.beforeShow();
    }


    show() {
        super.show();
        this.clubName.focus();
    }

    doSubmit() {
        let name = this.clubName.value;
        if (name == null || name === '') {
            return;
        }
        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                let club = JSON.parse(xhttp.responseText);
                form.log.log('Club created: ', club);
                clubListWindow.reset();
                messageWindow.showMessage('Клуб успешно сохранён', function () {form.show()});
            } else {
                form.log.warn('Club creation failed: ' + this.status);
                messageWindow.showMessage(this.responseText, function () {form.show()});
            }
        };
        this.sendJson(xhttp, '/club/create',
            {
                name: name
            });
    }
}

class EnterClubWindow extends WebForm {
    constructor() {
        super('enterClubWindow');
        this.clubUid = document.getElementById('enterClubWindow-clubUid')
        this.submit = document.getElementById('enterClubWindow-submit')

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})
    }

    beforeShow() {
        this.submit.disabled = false;

        return super.beforeShow();
    }


    show() {
        super.show();
        this.clubUid.focus();
    }

    doSubmit() {
        let uid = this.clubUid.value;
        if (uid == null || uid === '') {
            return;
        }
        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                let request = JSON.parse(xhttp.responseText);
                //request can be pending, success, resigned
                form.log.log('Club request: ', request);
                if (request.status === 'PENDING') {
                    messageWindow.showMessage('Запрос успешно отправлен в клуб ' + request.club.name, function () {form.show()});
                } else if (request.status === 'ACCEPTED') {
                    messageWindow.showMessage('Вы успешно вступили в клуб ' + request.club.name, function () {form.show()});
                } else {
                    messageWindow.showMessage('Вам отказано во вступлении в клуб ' + request.club.name, function () {form.show()});
                }
            } else {
                form.log.warn('Club entering failed: ' + this.status);
                messageWindow.showMessage(this.responseText, function () {form.show()});
            }
        };
        this.sendJson(xhttp, '/club/enter',
            {
                uid: uid
            });
    }
}

class MainMenu extends WebForm {
    KEY_CLUB = 'KEY_CLUB';

    constructor() {
        super('mainMenu');
        this.title = document.getElementById('mainMenu-title');
        this.quiz = document.getElementById('mainMenu-quiz');
        this.createQuiz = document.getElementById('mainMenu-createQuiz');
        this.questionList = document.getElementById('mainMenu-questionList');
        this.createQuestion = document.getElementById('mainMenu-createQuestion');
        this.acceptClub = document.getElementById('mainMenu-acceptClub');

        this.quiz.addEventListener('click', () => {
            quizListWindow.reset()
            quizListWindow.show()
        })
        this.acceptClub.addEventListener('click', () => {acceptClubWindow.show()})
        this.createQuiz.addEventListener('click', () => {
            editQuizWindow.setNewQuiz()
            editQuizWindow.show()
        })
        this.questionList.addEventListener('click', () => {
            questionListWindow.reset()
            questionListWindow.show()
        })
        this.createQuestion.addEventListener('click', () => {
            editQuestionWindow.setNewQuestion()
            editQuestionWindow.show()
        })

        let jsonClub = localStorage.getItem(this.KEY_CLUB)
        if (!jsonClub) {
            this.club = {id:undefined, uid:undefined, name:'Неизвестно', owner:undefined};
        } else {
            this.club = JSON.parse(jsonClub)
        }
    }

    beforeShow() {
        this.quiz.disabled = false;
        if (personalInfo.user && this.club.owner) {
            this.createQuiz.disabled = false;
            this.questionList.disabled = false;
            this.createQuestion.disabled = false;
            this.createQuiz.style.display = 'inline';
            this.questionList.style.display = 'inline';
            this.createQuestion.style.display = 'inline';
            this.acceptClub.style.display = 'inline';
        } else {
            this.createQuiz.disabled = true;
            this.questionList.disabled = true;
            this.createQuestion.disabled = true;
            this.createQuiz.style.display = 'none';
            this.questionList.style.display = 'none';
            this.createQuestion.style.display = 'none';
            this.acceptClub.style.display = 'none';
        }
        this.title.innerHtml = this.generateHtml();
        return super.beforeShow();
    }

    show() {
        super.show();
        this.quiz.focus();
    }

    saveClub(club) {
        this.club = club;
        localStorage.setItem(this.KEY_CLUB, JSON.stringify(club));
        this.title.innerHTML = this.generateHtml();
        acceptClubWindow.changeClub(this.club);
    }

    generateHtml() {
        return '<h1>' + this.club.name + '</h1>'
    }
}

class PersonalInfo extends WebForm {
    KEY_USER = 'KEY_USER';

    constructor() {
        super('personalInfo');

        this.name = document.getElementById('personalInfo-name')

        let jsonUser = localStorage.getItem(this.KEY_USER)
        if (!jsonUser) {
            this.user = {nickname: 'Гость'}
        } else {
            this.user = JSON.parse(jsonUser)
        }
        this.name.innerHTML = this.generateHtml();
    }

    generateHtml() {
        return '<p>' + this.user.nickname + '</p>'
    }

    saveUser(user) {
        this.user = user;
        localStorage.setItem(this.KEY_USER, JSON.stringify(user));
        this.name.innerHTML = this.generateHtml();
    }

    initApplication() {
        if (this.user.id) {
            let form = this;
            let xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = function () {
                if (this.status === 200) {
                    form.log.log('WEB: <<< ' + xhttp.responseText);
                    let session = JSON.parse(this.responseText);
                    form.log.log('user is logged in', session);
                    form.showApplication(session);
                } else {
                    form.log.warn('Session expired');
                    loginWindow.show();
                }
            };
            this.sendGet(xhttp, '/ping');
        } else {
            this.log.log('user is not logged in')
            loginWindow.show();
        }
    }

    showApplication(session) {
        if (session.games && session.games.length === 1) {
            let quiz = session.games[0];
            quizWindow.setQuiz(quiz)
            questionWindow.setParticipant(quiz.result)
            questionWindow.show()
        } else {
            clubListWindow.show();
        }
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

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})

        this.userName.value = localStorage.getItem(this.KEY_LOGIN);
    }

    saveLogin(login) {
        localStorage.setItem(this.KEY_LOGIN, login);
        this.userName.value = login;
    }

    beforeShow() {
        this.submitButton.disabled = false;
        return super.beforeShow();
    }


    show(callback) {
        if (callback === undefined) {
            callback = function () {clubListWindow.show()};
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
        let login = this.userName.value;
        let password = this.userPassword.value;
        this.userPassword.value = '';
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                let user = JSON.parse(this.responseText);
                form.log.log('Logged-In Successfully');
                personalInfo.saveUser(user);
                form.saveLogin(user.login);
                form.runCallback();
            } else if (this.status === 409) {
                //need re-login
                form.log.warn('Need ReLogin: ' + this.status);
                form.userPassword.value = password;
                messageWindow.showMessage('Произошла ошибка. Попробуйте ещё раз.', function() {form.show()});
            } else {
                form.log.warn('Login Failed: ' + this.status);
                messageWindow.showMessage(this.responseText, function() {form.show()});
            }
        };
        this.sendPost(xhttp, '/', 'login=' + login + '&password=' + password);
    }
}

class RegisterWindow extends WebForm {
    constructor() {
        super('registerWindow');
        this.userName = document.getElementById('registerWindow-name');
        this.nickname = document.getElementById('registerWindow-nickname');
        this.userPassword = document.getElementById('registerWindow-password');
        this.submit = document.getElementById('registerWindow-submit');

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})
    }

    beforeShow() {
        this.submit.disabled = false;
        this.callback = loginWindow.callback;
        if (this.userName.value === '') {
            this.userName.value = loginWindow.userName.value;
        }
        return super.beforeShow();
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
        };
        this.sendPost(xhttp, '/register',
            'login=' + this.userName.value +
            '&password=' + this.userPassword.value +
            '&nickname=' + this.nickname.value);
        this.userPassword.value = '';
    }
}

class QuizListWindow extends PagedWebForm {
    constructor() {
        super('quizListWindow');
        this.pageSize = 5;
        this.quizName = document.getElementById('quizListWindow-quizName');
        this.view = document.getElementById('quizListWindow-view');

        let form = this
        this.addSubmitListener(() => {
            form.reset()
            form.show()
        })

        this.quizArray = [];

        this.view.innerHTML = 'Список пуст';
    }

    beforeShow() {
        mainMenu.quiz.disabled = true;
        return super.beforeShow();
    }


    loadPage() {
        let name = this.quizName.value;
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                form.reload = false;
                let data = JSON.parse(xhttp.responseText);
                form.loadQuizListData(data);
                form.show();
            } else if (this.status === 401) {
                loginWindow.show(function () {form.show()});
            } else {
                form.log.warn('problem to load quiz list: ', xhttp.responseText);
                messageWindow.showMessage(xhttp.responseText);
            }
        };
        this.sendJson(xhttp, '/quiz/list',
            {
                name: name,
                page: this.pageNumber,
                size: this.pageSize
            });
    }

    loadQuizListData(page) {
        this.view.innerHTML = '';
        this.total = page.total;
        this.pageNumber = page.page;
        if (!page.elements || page.elements.length === 0) {
            this.view.innerHTML = 'Список пуст';
        } else {
            let form = this;
            this.quizArray = page.elements;
            this.view.innerHTML = '';
            this.quizArray.forEach(function(quiz) {
                let quizName = quiz.name;
                if (quiz.status === 'DRAFT') {
                    quizName += ' *';
                }
                form.view.insertAdjacentHTML('beforeend',
                    '<button type="button" onclick="onSelectQuiz()" value="' + quiz.id + '" class="dialog-button">' + quizName + '</button>');
            })
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

class AcceptClubWindow extends PagedWebForm {
    constructor() {
        super('acceptClubWindow');
        this.pageSize = 5;
        this.title = document.getElementById('acceptClubWindow-title');
        this.clubUid = document.getElementById('acceptClubWindow-clubUid');
        this.status = document.getElementById('acceptClubWindow-status');
        this.view = document.getElementById('acceptClubWindow-view');
        this.accept = document.getElementById('acceptClubWindow-accept');
        this.resign = document.getElementById('acceptClubWindow-resign');
        this.selectAll = document.getElementById('acceptClubWindow-selectAll')
        this.invertAll = document.getElementById('acceptClubWindow-invertAll')

        let form = this
        this.addSubmitListener(() => {
            form.reset()
            form.show()
        })
        this.selectAll.addEventListener('click', () => {onSelectAll(form.name)})
        this.invertAll.addEventListener('click', () => {onInvertAll(form.name)})
        this.accept.addEventListener('click', () => {form.doAccept()})
        this.resign.addEventListener('click', () => {form.doResign()})

        this.requestArray = [];

        this.view.innerHTML = 'Список пуст';
    }

    beforeShow() {
        this.accept.disabled = false;
        this.resign.disabled = false;
        return super.beforeShow();
    }

    changeClub(club) {
        this.title.innerHTML = '<h1>Список запросов: ' + club.name + '<h1>'
        this.clubUid.value = club.uid;
    }

    loadPage() {
        let status = [];
        if (this.status.value !== 'NONE') {
            status.push(this.status.value);
        }
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                form.reload = false;
                let data = JSON.parse(xhttp.responseText);
                form.loadRequestListData(data);
                form.show();
            } else if (this.status === 401) {
                loginWindow.show(function () {form.show()});
            } else {
                form.log.warn('problem to load request list: ', xhttp.responseText);
                messageWindow.showMessage(xhttp.responseText);
            }
        };

        this.sendJson(xhttp, '/club/requests',
            {
                status: status,
                page: this.pageNumber,
                size: this.pageSize
            });
    }

    loadRequestListData(page) {
        this.view.innerHTML = '';
        this.total = page.total;
        this.pageNumber = page.page;
        if (!page.elements || page.elements.length === 0) {
            this.view.innerHTML = 'Список пуст';
        } else {
            let form = this;
            this.requestArray = page.elements;
            this.view.innerHTML = '';
            this.requestArray.forEach(function(request) {
                form.view.insertAdjacentHTML('beforeend',
                    '<div class="table-row">' +
                    '  <div class="table-cell" style="width: 2em"><div class="table-cell-content">' +
                    '    <input type="checkbox" name="requests" value="' + request.id + '" id="acceptClubWindow-request' + request.id + '">' +
                    '  </div></div>' +
                    '  <div class="table-cell"><div class="table-cell-content">' +
                    '    <label for="acceptClubWindow-request' + request.id + '">' + request.user.nickname + ' (' + request.status + ')</label>' +
                    '  </div></div>' +
                    '</div>');
            })
        }
    }

    doAccept() {
        this.doChangeStatus(true);
    }

    doResign() {
        this.doChangeStatus(false);
    }

    doChangeStatus(accept) {
        let ids = [];
        forInputs(this.element.id, 'checkbox', function(checkbox){
            if (checkbox.checked) {
                ids.push(checkbox.value);
            }
        });
        if (ids.length <= 0) {
            return;
        }
        this.accept.disabled = true;
        this.resign.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function() {
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                let pgNum = form.pageNumber;
                form.reset();
                form.pageNumber = pgNum;
                form.show();
            } else if (this.status === 401) {
                loginWindow.show(function () {form.show()});
            } else {
                form.log.warn('problem to load request list: ', xhttp.responseText);
                messageWindow.showMessage(xhttp.responseText);
            }
        }
        let added = accept ? ids : null;
        let removed = accept ? null : ids
        this.sendJson(xhttp, '/club/requestStatus',
            {
                added   : added,
                removed : removed
            });
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
        }
    }

    registerQuiz() {
        let form = this;
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
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
        };
        this.sendGet( xhttp, '/quiz/' + this.quizId + '/register');
    }

    beforeShow() {
        if (mainMenu.club.owner && this.quiz.status === 'DRAFT') {
            this.editButton.style.display = 'inline';
        } else {
            this.editButton.style.display = 'none';
        }
        if (this.quiz.status === 'ACTIVE') {
            this.startButton.style.display = 'inline';
        } else {
            this.startButton.style.display = 'none';
        }
        return super.beforeShow();
    }
}

class ClubControl extends WebControl {
    constructor(name) {
        super(name);
    }

    setClub(clubId) {
        this.control.value = clubId;
    }

    getClub() {
        return Number.parseInt(this.control.value);
    }

    loadData() {
        let form = this;
        let xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                let page = JSON.parse(xhttp.responseText);
                form.loadClubListData(page);
            } else {
                form.reset();
                form.log.warn('error load clubs: ', xhttp.responseText);
            }
        };
        this.sendJson(xhttp, '/club/list');
    }

    loadClubListData(page) {
        this.control.innerHTML = '<option selected value="0">Выберите клуб</option>';
        if (page.elements) {
            let form = this;
            page.elements.forEach(function(club){
                form.control.insertAdjacentHTML('beforeend',
                    '<option value="' + club.id + '">' + club.name + '</option>');
            });
        }
    }
}

class EditQuizWindow extends WebForm {
    constructor() {
        super('editQuizWindow');
        this.club = document.getElementById('editQuizWindow-club')
        this.name = document.getElementById('editQuizWindow-name')
        this.minPlayers = document.getElementById('editQuizWindow-minPlayers')
        this.maxPlayers = document.getElementById('editQuizWindow-maxPlayers')
        this.questionType = document.getElementById('editQuizWindow-questionType')
        this.questionsNumber = document.getElementById('editQuizWindow-questionsNumber')
        this.status = document.getElementById('editQuizWindow-status')
        this.selectQuestions = document.getElementById('editQuizWindow-selectQuestions')
        this.submit = document.getElementById('editQuizWindow-submit')

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})
        this.selectQuestions.addEventListener('click', () => {
            selectQuizQuestionsWindow.setQuizId(form.quizId);
            selectQuizQuestionsWindow.show();
        })

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

    doSubmit() {
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
            form.hide();
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                let quiz = JSON.parse(xhttp.responseText);
                form.setQuiz(quiz);
                form.log.log('quiz saved: ', quiz);
                quizListWindow.reset();
                messageWindow.showMessage('Викторина успешно сохранена', function () {form.show()});
            } else if (this.status === 401) {
                loginWindow.show(function () {form.show()});
            } else {
                form.log.warn('error save quiz: ', xhttp.responseText);
                messageWindow.showMessage(xhttp.responseText, function () {form.show()});
            }
        };
        if (quiz.id) {
            this.sendJson(xhttp, '/quiz/' + quiz.id + '/edit', quiz);
        } else {
            this.sendJson(xhttp, '/quiz/create', quiz);
        }
    }

    beforeShow() {
        this.submit.disabled = false;
        this.club.value = mainMenu.club.name;
        if (this.quiz.id && this.quiz.selectionStrategy === 'QUIZ' && this.quiz.status === 'DRAFT') {
            this.selectQuestions.disabled = false;
            this.selectQuestions.style.display = 'inline';
        } else {
            this.selectQuestions.disabled = true;
            this.selectQuestions.style.display = 'none';
        }
        return super.beforeShow();
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
            if (this.status === 200) {
                form.log.log('WEB: <<< ' + xhttp.responseText);
                let categories = JSON.parse(xhttp.responseText);
                form.loadCategoryListData(categories);
            } else {
                form.reset();
                form.log.warn('error load categories: ', xhttp.responseText);
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

class QuestionListWindow extends PagedWebForm {
    constructor() {
        super('questionListWindow');
        this.category = new CategoryControl('questionListWindow-category')
        this.view = document.getElementById('questionListWindow-view')
        this.file = document.getElementById('questionListWindow-file')
        this.search = document.getElementById('questionListWindow-search')
        this.download = document.getElementById('questionListWindow-download')

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})
        this.file.addEventListener('change', () => {form.doSubmitFile()})
        this.search.addEventListener('click', () => {form.doSearch()})
        this.download.addEventListener('click', () => {form.doDownload()})

        this.view.innerHTML = 'Список пуст';
    }

    clear() {
        this.view.innerHTML = 'Список пуст';
        this.reset();
        this.category.load();
    }

    doSubmit() {
        this.file.click()
    }

    doSearch() {
        this.reset()
        this.loadPage()
    }

    doSubmitFile() {
        if (this.file.files.length === 1) {
            let data = this.file.files[0]
            if (!data.type.match('.*json')) {
                this.log.warn('not a json file')
                return
            }
            this.submit.disabled = true

            let form = this

            let reader = new FileReader()
            reader.onload = function (ff) {
                form.log.log('loaded ', ff.loaded)

                let xhttp = new XMLHttpRequest()

                xhttp.onreadystatechange = function () {
                    form.hide()
                    if (this.status === 200) {
                        form.log.log('WEB: <<< ' + xhttp.responseText)
                        let resp = JSON.parse(xhttp.responseText)
                        messageWindow.showMessage(
                            '<p>Создано ' + resp.created + ' вопросов</p>' +
                            '<p>Обновлено ' + resp.modified + ' вопросов<p>' +
                            '<p>Всего обработано ' + resp.total + ' вопросов<p>',
                            () => {form.clear(); form.show()})
                    } else if (this.status === 401) {
                        loginWindow.show(() => {form.show()})
                    } else {
                        form.log.warn('error save questions: ', xhttp.responseText)
                        messageWindow.showMessage(xhttp.responseText, () => {form.show()})
                    }
                };
                form.sendJson(xhttp, '/questions', reader.result)
            }
            reader.readAsText(data)
        }
    }
    
    loadPage() {
        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
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
        };
        let categoryId = null;
        if (this.category.getCategory() > 0) {
            categoryId = this.category.getCategory();
        }
        this.sendJson(xhttp, '/questions/list', {
            categoryId: categoryId,
            page: this.pageNumber,
            size: this.pageSize
        });
    }

    doDownload() {
        let href = document.createElement('A')
        href.style.display = 'none'
        href.href = '/questions'
        href.download = 'questions.json'
        href.innerText = 'Скачать'
        this.element.appendChild(href)
        href.click()
        this.element.removeChild(href)
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
                form.view.insertAdjacentHTML('beforeend',
                    '<div class="table-row">' +
                    '  <div class="table-cell"><div class="table-cell-content">' +
                    '    <p>' + question.category + ': ' + question.text + '</p>' +
                    '  </div></div>' +
                    '</div>');
            })
        }
    }

    doClose() {
        mainMenu.show()
    }
}

class SelectQuizQuestionsWindow extends PagedWebForm {
    constructor() {
        super('selectQuizQuestionsWindow');
        this.category = new CategoryControl('selectQuizQuestionsWindow-category')
        this.view = document.getElementById('selectQuizQuestionsWindow-view')
        this.selectAll = document.getElementById('selectQuizQuestionsWindow-selectAll')
        this.invertAll = document.getElementById('selectQuizQuestionsWindow-invertAll')
        this.search = document.getElementById('selectQuizQuestionsWindow-search')

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})
        this.selectAll.addEventListener('click', () => {onSelectAll(form.name)})
        this.invertAll.addEventListener('click', () => {onInvertAll(form.name)})
        this.search.addEventListener('click', () => {form.doSearch()})

        this.quizId = undefined;

        this.view.innerHTML = 'Список пуст';
    }

    setQuizId(quizId) {
        this.quizId = quizId;
        this.view.innerHTML = 'Список пуст';
        this.reset();
        this.category.load();
    }

    doSubmit() {
        this.saveQuestionList()
    }

    doSearch() {
        this.reset()
        this.loadPage()
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
        };
        this.sendJson(xhttp, '/quiz/' + this.quizId + '/questions',
            {added: addedQuestions, removed: removedQuestions});
    }

    loadPage() {
        this.submit.disabled = true;
        let form = this;
        let xhttp = new XMLHttpRequest();

        xhttp.onreadystatechange = function () {
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
                let checkbox = ''
                if (form.quizId) {
                    checkbox = '  <div class="table-cell" style="width: 2em"><div class="table-cell-content">' +
                        '    <input type="checkbox" name="questions" ' + checked + ' value="' + question.id + '" id="questionListWindow-question' + question.id + '">' +
                        '  </div></div>'
                }
                form.view.insertAdjacentHTML('beforeend',
                    '<div class="table-row">' + checkbox +
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

    doClose() {
        editQuizWindow.show()
    }
}

class EditQuestionWindow extends WebForm {
    constructor() {
        super('editQuestionWindow');
        this.category = new CategoryControl('editQuestionWindow-category');
        this.club = document.getElementById('editQuestionWindow-club');
        this.text = document.getElementById('editQuestionWindow-text');
        this.value = document.getElementById('editQuestionWindow-value');
        this.options = document.getElementById('editQuestionWindow-options');
        this.submit = document.getElementById('editQuestionWindow-submit');

        let form = this
        this.addSubmitListener(() => {form.doSubmit()})

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
        this.submit.disabled = false;
        this.club.value = mainMenu.club.name;
        return super.beforeShow();
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
        };
        if (query.id) {
            this.sendJson(xhttp, '/questions/' + query.id + '/edit', query);
        } else {
            this.sendJson(xhttp, '/questions/create', query);
        }
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
        this.view.innerHTML = quizWindow.view.innerHTML;
        this.finished = false;
        this.connect()

        return super.beforeShow();
    }

    connect() {
        if (!this.webSocket || this.webSocket.readyState !== WebSocket.OPEN) {
            let url = getWebsocketProtocol() + '://' + getUrl() + '/websocket';
            this.log.log('WEBSOCKET: connecting to ', url)
            this.webSocket = new WebSocket(url);
            let form = this;
            this.webSocket.onopen = function () {
                form.log.log('WEBSOCKET: connected to ', url, 'sending handshake')
                form.sendWebSocket(JSON.stringify({enter: {participantId: form.participant.id}}));
            }
            this.webSocket.onmessage = playQuizWebsocketMessageHandler;
            this.webSocket.onclose = function () {
                form.log.log("connection closed by server");
                quizWindow.show();
            }
            this.webSocket.onerror = function(err) {
                form.log.warn('WEBSOCKET: error occur', err, 'Close socket')
                form.webSocket.close()
            }
        }
    }

    sendWebSocket(data) {
        if (this.webSocket) {
            if (typeof data == 'string') {
                this.log.log('WEBSOCKET: >>> ', data)
            } else if (data instanceof ArrayBuffer) {
                this.log.log('WEBSOCKET: >>> ', data.byteLength)
            }
            this.webSocket.send(data)
        } else {
            this.log.warn('WEBSOCKET: cannot send to closed connection')
        }
    }

    sendAnswer(answerId) {
        this.sendWebSocket(JSON.stringify({answer:answerId}))
        this.stopPlaySound();
        disableAllButtons(this.view);
    }

    closeConnection() {
        if (this.webSocket && this.webSocket.readyState === WebSocket.OPEN) {
            this.webSocket.onclose = null;
            this.webSocket.close();
            this.log.log("WEBSOCKET: close connection");
        }
        this.webSocket = undefined;
    }

    close() {
        this.stopPlaySound()
        this.closeConnection()

        if (this.finished) {
            quizListWindow.reset()
            quizListWindow.show()
        } else {
            quizWindow.show()
        }
    }

    stopPlaySound() {
        this.sounds.tickTack.pause();
    }


    sendNext() {
        this.sendWebSocket(JSON.stringify({next:true}))
    }

    showProgress(start, end) {
        let progress = document.getElementById('questionWindow-progress')
        let seconds = document.getElementById('questionWindow-seconds')
        if (progress) {
            let millis = Date.now()
            progress.value = end - millis
            let secs = ((end - millis) / 1000).toFixed()
            if (seconds) {
                seconds.innerText = secs
            }
            this.log.log('Quiz will start in ' + secs + ' secs')
        } else {
            this.stopTimer()
        }
    }

    stopTimer() {
        if (this.timer) {
            this.log.log('stop timer')
            clearInterval(this.timer)
            this.timer = undefined
        }
    }

    setQuiz(quiz) {
        this.view.innerHTML = '';
        this.view.insertAdjacentHTML('beforeend', '<p> Викторина ' + quiz.name + '</p>')
        if (quiz.result !== undefined) {
            this.view.insertAdjacentHTML('beforeend', '<p> ' + quiz.result.players + ' участников</p>')
            if (quiz.result.status === 'ACTIVE') {
                let startTime = quiz.result.started
                let registerTime = quiz.result.registered
                let maxProgress = startTime - registerTime
                if (Date.now() < startTime) {
                    this.view.insertAdjacentHTML('beforeend',
                        '<p> начнется в ' + new Date(startTime) + '</p>')
                    this.view.insertAdjacentHTML('beforeend',
                        '<progress max="' + maxProgress + '" value="' + maxProgress + '" id="questionWindow-progress" style="width: 100%">' +
                        'осталось <span id="questionWindow-seconds">0</span> секунд</progress>'
                    )
                    this.stopTimer()
                    let form = this
                    this.timer = setInterval((start, end) => {form.showProgress(start, end)},
                        1000,
                        registerTime, startTime)
                } else {
                    this.stopTimer()
                    this.view.insertAdjacentHTML('beforeend',
                        '<p> начинается </p>')
                }
            } else if (quiz.result.status === 'FINISHED') {
                this.view.insertAdjacentHTML('beforeend', '<p> окончился ' + new Date(quiz.result.finished) + '</p>')
            } else {
                this.view.insertAdjacentHTML('beforeend', '<p> отменен</p>')
            }
        }
    }

    setQuestion(question) {
        this.stopTimer()
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
        this.view.insertAdjacentHTML("beforeend", '<p>место: ' + participant.place + ' из ' + participant.players + '</p>');
        this.view.insertAdjacentHTML("beforeend", '<p>верных ответов: ' + participant.right + '</p>');
        this.view.insertAdjacentHTML("beforeend", '<p>неверных ответов: ' + participant.wrong + '</p>');
        this.view.insertAdjacentHTML("beforeend", '<p>очков: ' + participant.value + '</p>');
        this.stopPlaySound()
        this.closeConnection()
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

const clubListWindow = new ClubListWindow()
const createClubWindow = new CreateClubWindow()
const enterClubWindow = new EnterClubWindow()
const acceptClubWindow = new AcceptClubWindow()
const mainMenu = new MainMenu()
const loadingWindow = new LoadingWindow()
const personalInfo = new PersonalInfo()
const messageWindow = new MessageWindow()
const loginWindow = new LoginWindow()
const registerWindow = new RegisterWindow()
const quizListWindow = new QuizListWindow()
const quizWindow = new QuizWindow()
const editQuizWindow = new EditQuizWindow()
const questionWindow = new QuestionWindow()
const questionListWindow = new QuestionListWindow()
const selectQuizQuestionsWindow = new SelectQuizQuestionsWindow()
const editQuestionWindow = new EditQuestionWindow()
