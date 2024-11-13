
const serverURL = document.getElementById("serverURL").getAttribute("data-URL");

const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const activeUserName = document.querySelector(".active-user-name");
let activeUserImage = document.querySelector(".active");

let inputContainer = document.querySelector(".inputContainer");
let chatScreen = document.querySelector(".chat");
let msgInputWindow = document.getElementById("input-msg");
let messageContainer = document.getElementById("messages");
let chatWithElement = document.getElementById("chat-with");
let usersContainer = document.getElementById("users");
let publicBtn = document.getElementById("public-chat-btn");
let historyContainer = document.querySelector(".history-window");

let deleteProfileModal = document.querySelector('.modal.delete-mod');

let emojiPicker = document.getElementById('emoji-win');

const { DateTime } = luxon;

let privateChatWith;
var loggedOutByButton = false;
let isScrolledToBottom = true;

var actDate = () => DateTime.now().setZone('Europe/Prague');


let stompClient = null;
let sock = new SockJS(`${serverURL}chat`, null, {
    debug: false
});



register();

var nicknameElement = document.getElementById("user-data");
var nickname = nicknameElement.getAttribute("data-user");




//event listener for UNDO step from chat page

window.addEventListener('popstate', function (event) {

    if (!event.state) {
        logOutUser();
        window.location.href = serverURL;

    }
});


if (!sessionStorage.getItem('chatVisited')) {
    sessionStorage.setItem('chatVisited', 'true');
    history.pushState({ chat: true }, '', '');
}


//event listener for sending msg by pressing Enter
msgInputWindow.addEventListener("keypress", (event) => {
    if (event.code === "Enter") {
        event.preventDefault();
        send();
    }
});
//event listener for adding new line to message window
msgInputWindow.addEventListener("keydown", (event) => {
    if (event.shiftKey && event.code === "Enter") {
        event.preventDefault();
        const start = msgInputWindow.selectionStart;
        const end = msgInputWindow.selectionEnd;


        msgInputWindow.value = msgInputWindow.value.substring(0, start) + '\n' + msgInputWindow.value.substring(end);


        msgInputWindow.selectionStart = msgInputWindow.selectionEnd = start + 1;
    }
});


messageContainer.addEventListener('scroll', () => {
    isScrolledToBottom = messageContainer.scrollHeight - messageContainer.clientHeight <= messageContainer.scrollTop + 1;

})

function register() {

    // establishing connection
    stompClient = Stomp.over(sock);
    stompClient.debug = null;
    stompClient.connect({}, onConnectedSuccessfully, (error) => {
        console.log('unable to connect' + error);
    });

}

//function for sending msg to server
function send() {

    let finalMsg;
    if (msgInputWindow.value) {
        if (chatWithElement.innerHTML === "Public chat") {
            finalMsg = {
                "content": msgInputWindow.value,
                "type": 'message',
                "recipient": 'public'
            }
            stompClient.send("/app/chat/public", {}, JSON.stringify(finalMsg));

        } else {
            finalMsg = {
                "content": msgInputWindow.value,
                "type": 'message',
                "recipient": privateChatWith
            }
            stompClient.send(`/app/chat/private`, {}, JSON.stringify(finalMsg));

        }
        msgInputWindow.value = "";
    }

}

//function for getting messages and online users from server
function onMessageReceived(payload) {

    var message = JSON.parse(payload.body);


    if (message.type) {
        if (message.type === "Leave") {
            prepareMessage(messageContainer, message);

            if (usersContainer.querySelector(`.${rawNick}`)) {
                usersContainer.querySelector(`.${rawNick}`).remove();
            }
        }

        if (message.type === "kick") {
            if (nickname === message.sendTo) {
                logOutUser();
                alert("You have been kicked out by admin!");
                window.location.href = serverURL;
            }
            prepareMessage(messageContainer, message);

        }

        if (message.type === "BAN") {
            if (nickname === message.sendTo) {
                fetch(`${serverURL}logout`, {
                    method: 'POST',
                    headers: {
                        [csrfHeader]: csrfToken
                    }
                });

                stompClient.disconnect();
                window.location.href = serverURL;
                alert("Admin banned you")
            }

            prepareMessage(messageContainer, message);

        }

        if (message.type === "UNBAN") {
            prepareMessage(messageContainer, message);
        }



        if (message.type === "update-nick") {

            if (nickname === message.sender) {



                nickname = message.content;
                //
                updateActiveUserInfo(activeUserName, activeUserImage);
                ///
            } else {
                let onUserbtn = document.querySelector(`.${rawNick}`);
                let name = onUserbtn.querySelector(`.user`);

                Array.from(document.getElementById("user-to-find").options).forEach(option => {

                    if (option.value === message.sender) {
                        option.value = message.content;
                        option.text = message.content;

                    }
                });

                onUserbtn.classList.replace(`${rawNick}`, `${message.content}`);

                name.innerHTML = `${message.content}`;
                name.classList.replace(`${rawNick}`, `${message.content}`);
                setProfilePicture(onUserbtn, message.content);

                if (chatWithElement.innerHTML !== "Public chat") {
                    privateChatWith = message.content;
                    chatWithElement.innerHTML = `Private chat with: ${message.content}`;
                }
            }


            messageContainer.innerHTML = "";
            getLatestHistory();
        }

        if (message.type === "update-profilePic") {
            if (nickname !== message.sender) {
                let onUserbtn = document.querySelector(`.${rawNick}`);

                setProfilePicture(onUserbtn, message.sender);

            } else {
                updateActiveUserInfo(activeUserName, activeUserImage);
            }
            prepareMessage(messageContainer, message);

        }

    }


    // displaying newest message
    if (message.type === 'message') {


        if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {

            prepareMessage(messageContainer, message);
        }

        if (chatWithElement.innerHTML === "Public chat" && message.sendTo === nickname) {
            let sender = CSS.escape(message.sender);
            let incomingMsgUser = document.querySelector(`.${sender}`);
            let msgCounterContainer = incomingMsgUser.querySelector(".message-counter-container");
            let msgCounter = incomingMsgUser.querySelector(".message-counter");


            usersContainer.insertBefore(incomingMsgUser, usersContainer.firstChild);


            let count = parseInt(msgCounter.innerHTML) + 1;
            msgCounter.innerText = count;
            msgCounterContainer.style.setProperty("visibility", "visible");

        }

        if ((nickname == message.sendTo && message.sender == privateChatWith) ||
            (message.sendTo == privateChatWith && message.sender == nickname)) {
            prepareMessage(messageContainer, message);


        }
        clearOldMsg();
        if (isScrolledToBottom) {
            messageContainer.scrollTop = messageContainer.scrollHeight - messageContainer.clientHeight;
        }
    }

    // displaying new user in chat and online user panel
    if (message.type === 'newUser') {

        prepareMessage(messageContainer, message);

        fetch(`${serverURL}users`)
            .then(response => response.json())
            .then(data => {
                showOnlineUsers(data);

            });

    }
}

function showOnlineUsers(usersList) {

    if (usersContainer.querySelectorAll("*").length === 0) {
        usersList.forEach(onlineUser => {
            updateOnlineUserList(onlineUser);

        });
    } else {
        let lastUser = usersList[usersList.length - 1];
        updateOnlineUserList(lastUser);
    }

}


//connecting new user
function onConnectedSuccessfully() {

    messageContainer.innerHTML = "";

    getLatestHistory();

    updateActiveUserInfo(activeUserName, activeUserImage);

    stompClient.subscribe("/queue/public", onMessageReceived);
    stompClient.subscribe(`/user/queue/private`, onMessageReceived);

    fetch(`${serverURL}users`)
        .then(response => response.json())
        .then(data => {
            let info = JSON.stringify(data);

            if (!info.includes(nickname)) {

                stompClient.send("/app/user", {}, JSON.stringify(
                    {
                        type: 'newUser',
                        content: `${nickname} just joined chatroom. Welcome!`,
                        recipient: "public"

                    }));
            } else {
                showOnlineUsers(data);
            }
        });
}
//"Public chat" switch button
function switchToPublic() {

    if (chatWithElement.innerHTML !== "Public chat") {
        let activeBtn = document.querySelector(".user-container.active");
        chatWithElement.innerHTML = "Public chat";
        messages.innerHTML = "";


        if (activeBtn) {
            activeBtn.classList.remove("active");
        }
        getLatestHistory();

        messageContainer.scrollTop = messageContainer.scrollHeight;
        privateChatWith = "";
        publicBtn.disabled = true;
        publicBtn.style.setProperty("color", "darkgrey");
    }
}
// getting message history form server
function getLatestHistory() {
    if (chatWithElement.innerHTML === "Public chat") {
        fetch(`${serverURL}history/public-latest`)
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {
                    prepareMessage(messageContainer, msg);

                });
            });

    } else {
        fetch(`${serverURL}history/private-latest`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }, body: JSON.stringify({
                'sendTo': privateChatWith,
                'sender': nickname
            })
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error("Acces DENIED!");
                }

            }).then(data => {
                data.forEach((msg) => {
                    prepareMessage(messageContainer, msg);
                });
            })
            .catch(error => {
                console.log(`Nastala chyba: ${error}`);

            });
    }
}




function getFullPublicHistory() {

    historyContainer.innerHTML = "";
    fetch(`${serverURL}history/public`)
        .then(response => response.json())
        .then(message => {
            message.forEach((msg) => {
                prepareMessage(historyContainer, msg);

            });
        });
}

function getFullPersonalHistory() {
    historyContainer.innerHTML = "";
    let selectedUser = document.getElementById("user-to-find");

    fetch(`${serverURL}history/private`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }, body: JSON.stringify({
            'sendTo': selectedUser.value,
            'sender': nickname
        })
    })
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {
                    prepareMessage(historyContainer, msg);

                });
            });
}

// function for creating message to display
function prepareMessage(container, messageData) {

    //#region messageVariables
    const rawText = document.createTextNode(messageData.content);

    const rawNick = document.createTextNode(messageData.sender);
    const newMessageContainer = document.createElement("div");
    newMessageContainer.className = "message-container";

    const eventMessageContainer = document.createElement("div");
    eventMessageContainer.className = "event-message-container";

    const eventMessage = document.createElement("div");
    eventMessage.className = "event-message";

    const senderContainer = document.createElement("div");
    senderContainer.className = "sender";

    const dateContainer = document.createElement("div");
    dateContainer.className = "date";

    const messageContent = document.createElement("div");
    messageContent.className = "message";

    var rawDate = messageData.date.replace(" ", "T");

    var formatedDate = DateTime.fromISO(rawDate, { setZone: true });

    var cleanDate = formatedDate.toFormat('d.M.yyyy H:mm:ss');

    //#endregion

    if (messageData.type === "message") {

        messageContent.appendChild(rawText);

        senderContainer.appendChild(rawNick);

        dateContainer.innerHTML = cleanDate;
        senderContainer.appendChild(dateContainer);
        newMessageContainer.appendChild(senderContainer);
        newMessageContainer.appendChild(messageContent);

        if (messageData.sender === nickname) {

            newMessageContainer.classList.add("revert");
            messageContent.classList.add("right-msg");


        } else {

            messageContent.classList.add("left-msg");

        }

        container.appendChild(newMessageContainer);

    } else {

        if (messageData.type === "newUser" || messageData.type === "UNBAN") {

            eventMessage.classList.add("login-event");
            eventMessage.appendChild(rawText);

        }
        if (messageData.type === "Leave" || messageData.type === "kick") {

            eventMessage.classList.add("logout-event");
            eventMessage.appendChild(rawText);

        }

        if (messageData.type === "BAN") {

            eventMessage.classList.add("logout-event");
            let banMessage = document.createTextNode(`${messageData.sendTo} was banned by admin for ${messageData.content} minutes!`)
            eventMessage.appendChild(banMessage);
        }

        if (messageData.type === "update-nick") {

            eventMessage.classList.add("login-event");
            let updateMessage = document.createTextNode(`${messageData.sender} changed his name to: ${messageData.content}`)
            eventMessage.appendChild(updateMessage);

        }

        if (messageData.type === "update-profilePic") {
            eventMessage.classList.add("login-event");
            let updateMessage = document.createTextNode(`${messageData.sender} changed his profile picture`)
            eventMessage.appendChild(updateMessage);

        }

        eventMessageContainer.appendChild(eventMessage);

        container.appendChild(eventMessageContainer);


    }

}

function updateOnlineUserList(userData) {
    if (userData.nickname !== nickname) {


        let userButton = document.createElement("button");
        userButton.className = "user-container";
        userButton.classList.add(userData.nickname);
        userButton.type = "button";

        let img = document.createElement("img");
        img.className = "profile-img-online";
        img.src = "";
        img.alt = "Profile picture";

        let user = document.createElement("span");
        user.className = "user";
        var plainNick = document.createTextNode(userData.nickname);
        user.appendChild(plainNick);

        let counterContainer = document.createElement("span");
        counterContainer.className = "message-counter-container";

        let counterBg = document.createElement("span");
        counterBg.className = "msg-counter-bg";

        let svgElem = `<svg id="Layer_1" xmlns="http://www.w3.org/2000/svg" width="17px"
                                    height="17px" viewBox="0 0 64 64"
                                    xml:space="preserve">
                                    <g>
	                                    <rect x="1" y="13" fill="none" stroke="#C6AC8E" stroke-width="2" stroke-miterlimit="10" width="62" height="37"/>
	                                    <polyline fill="none" stroke="#C6AC8E" stroke-width="2" stroke-miterlimit="10" points="1,13 32,33 63,13"/>
                                    </g>
                                    </svg>`;
        counterBg.innerHTML = svgElem;

        let counter = document.createElement("span");
        counter.className = "message-counter";
        counter.innerHTML = 0;


        counterContainer.appendChild(counterBg);
        counterContainer.appendChild(counter);

        userButton.appendChild(img);
        userButton.appendChild(user);
        userButton.appendChild(counterContainer);

        setProfilePicture(userButton, userData.nickname);
        setUpOnlineUserBtn(userButton);


        usersContainer.insertAdjacentElement("beforeend", userButton);
    }
}


// online user button set up
function setUpOnlineUserBtn(btn) {

    btn.addEventListener("click", () => {
        if (chatWithElement.textContent !== "Public chat") {

            document.querySelector(".user-container.active").classList.remove("active");
        }
        btn.classList.add("active");

        publicBtn.disabled = false;
        publicBtn.style.setProperty("color", "#C6AC8E");
        messageContainer.textContent = "";
        if (btn.querySelector(".message-counter").textContent !== "0") {
            btn.querySelector(".message-counter-container").style.setProperty("visibility", "hidden");
            btn.querySelector(".message-counter").textContent = "0";
        }

        var btnUserName = btn.querySelector(".user").textContent;
        chatWithElement.textContent = `Private chat with: ${btnUserName}`;
        privateChatWith = btnUserName;
        getLatestHistory();
    });
}


function logOutUser() {


    loggedOutByButton = true;
    try {
        fetch(`${serverURL}logout`, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            }

        });
    } catch (error) {
        console.error('Error while logout:', error);
    }
    stompClient.disconnect();
}

function logOutButton() {
    logOutUser();
    window.location.href = serverURL;
}

/* When the user clicks on the button,
toggle between hiding and showing the dropdown content */
function show() {
    let dropMenu = document.getElementById("dropupMenu");
    let userSett = document.querySelector('.user-settings');
    let dropBtn = document.querySelector('.dropbtn');
    dropMenu.classList.toggle("show");
    if (window.getComputedStyle(dropBtn).borderRadius !== "0px") {
        dropBtn.style.backgroundColor = '#00000045';
        userSett.style.borderBottomRightRadius = "0px";
        userSett.style.borderTopRightRadius = "0px";
    } else {
        userSett.style.borderBottomRightRadius = "";
        userSett.style.borderTopRightRadius = "";
        dropBtn.style.backgroundColor = '';
    }

}
/* Modals open/close settings*/
//#region ModalsDisplay

// Close the dropdown menu if the user clicks outside of it or close modal if clics outside
window.onclick = function (event) {

    /* close emoji window when click outside */
    if ((!event.target.matches('.emoji-picker') && !event.target.matches('.emoji-btn')) && emojiPicker.classList.contains('emojiShow')) {

        emojiPicker.classList.remove('emojiShow');
        emojiPicker.classList.add('emojiHidden');
    }

    /* close only profile delete window when click outside  */
    if (event.target.matches('.modal.delete-mod')) {
        document.getElementById("pass-confirm-input").value = "";
        document.querySelector('.delete-response').innerHTML = "";
        deleteProfileModal.style.display = "none";
    }


    /* close dropUp menu when click outside */
    if (!event.target.matches('.dropbtn')) {
        var dropupMenu = document.getElementById("dropupMenu");
        var userSett = document.querySelector('.user-settings');
        var dropBtn = document.querySelector('.dropbtn');
        userSett.style.borderRadius = "";
        dropBtn.style.backgroundColor = "";

        if (dropupMenu.classList.contains('show')) {
            dropupMenu.classList.remove('show');

        }
    }



    /* close any other current open modal */
    if (event.target.matches('.modal') && !event.target.matches('.modal.delete-mod')) {
        var modals = document.getElementsByClassName("modal");
        Array.prototype.forEach.call(modals, function (modal) {
            if (modal.style.display == "block") {
                modal.style.display = "none";
            }
        });
        historyContainer.innerHTML = "";
        clearUpdateForm();

    }

}


/* open selected modal */
function openSelectedModal(modal) {
    document.querySelector(`.${modal}`).style.display = "block";

}
//#endregion

/*drag and drop area setup*/
//#region DragAndDrop

let dragAndDrop = document.getElementById("drop");

dragAndDrop.addEventListener("dragenter", setPreventDefaults, false);
dragAndDrop.addEventListener("dragleave", setPreventDefaults, false);
dragAndDrop.addEventListener("dragover", setPreventDefaults, false);
dragAndDrop.addEventListener("drop", setPreventDefaults, false);
dragAndDrop.addEventListener("drop", handleDrop, false);



function setPreventDefaults(ev) {
    ev.preventDefault();
    ev.stopPropagation();

}

function handleDrop(ev) {
    let dataTrans = ev.dataTransfer;

    let files = dataTrans.files;


    handleFiles(files);
}

function handleFiles(files) {
    let file = files[0];
    if (file.size > 2 * 1024 * 1024) {
        alert("Picture is too big");
    } else {
        let reader = new FileReader();
        document.getElementById("fileData").files = files;

        reader.onload = function (e) {
            let img = document.createElement("img");
            img.src = e.target.result;
            img.style.maxWidth = "100%";
            img.style.maxHeight = "100%";
            dragAndDrop.innerHTML = "";
            dragAndDrop.appendChild(img);
        };
        reader.readAsDataURL(file);
    }
}
//#endregion

//#region DeleteProfileUpdate
if (nickname !== "Admin") {
    document.getElementById("delete-profile-form").addEventListener("submit", function (event) {
        event.preventDefault();
        const formData = new FormData(this);
        let deleteResponse = document.querySelector('.delete-response');
        deleteResponse.innerHTML = "";
        logOutUser();

        fetch(`${serverURL}profile/delete`, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            },
            body: formData
        })
            .then(response => {

                if (response.ok) {
                    alert("Your profile was succesfully deleted!");
                    window.location.href = serverURL;
                } else {
                    response.text()
                        .then(errorText => {
                            deleteResponse.innerHTML = errorText;
                            deleteResponse.style.color = "#7E102C";
                        });

                }

            });

    });
}

//#endregion


//#region SubmitProfileUpdate
document.getElementById("profile-update-form").addEventListener("submit", function (event) {
    event.preventDefault();

    const formData = new FormData(this);
    let response = document.querySelector(".response");
    fetch(`${serverURL}profile/update`, {
        method: 'POST',
        [csrfHeader]: csrfToken,

        body: formData
    })
        .then(response => response.json())
        .then(data => {

            let updateMsg;

            if (Object.keys(data).length == 2 && data.hasOwnProperty("pass")) {

            } else {
                if (data.hasOwnProperty("newNickname")) {
                    updateMsg =
                    {
                        "content": data["newNickname"],
                        "oldNick": nickname,
                        "type": 'update-nick',
                        "recipient": 'public'
                    }

                    fetch(`${serverURL}history/update`, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json',
                            [csrfHeader]: csrfToken

                        },
                        body: JSON.stringify({
                            'prevNick': nickname,
                            'actNick': data["newNickname"]
                        })
                    })
                        .then(() => {

                            stompClient.send("/app/chat/public", {}, JSON.stringify(updateMsg));

                        });

                } else if (data.hasOwnProperty("profPic")) {

                    updateMsg =
                    {
                        "content": nickname,
                        "type": 'update-profilePic',
                        "recipient": 'public'
                    }
                    stompClient.send("/app/chat/public", {}, JSON.stringify(updateMsg));
                }

            }


            if (data["message"].includes("successfully")) {
                response.style.color = "#345635";
                clearUpdateForm();
            } else {
                response.style.color = "#7E102C";
                document.getElementById("act-pass-input").value = "";
                document.getElementById("new-pass-input").value = "";
                document.getElementById("re-type-new-pass-input").value = "";
            }
            response.innerHTML = `${data["message"]}`;

        });




});
//#endregion

function clearUpdateForm() {
    document.getElementById("act-pass-input").value = "";
    document.getElementById("new-pass-input").value = "";
    document.getElementById("re-type-new-pass-input").value = "";
    document.getElementById("name-input").value = "";
    document.getElementById("fileData").value = null;
    document.getElementById("drop").innerHTML = "Drag your profile picture HERE <br> Max size of picture: 2MB";
    if (document.querySelector(".response")) {
        document.querySelector(".response").innerHTML = "";
    }
}

function clearOldMsg() {
    if (messageContainer.querySelectorAll(".message-container").length + messageContainer.querySelectorAll(".event-message-container").length > 15) {
        messageContainer.removeChild(messageContainer.firstElementChild);
    }

}

function setProfilePicture(button, btnNickname) {

    fetch(`${serverURL}profile/get?nickname=${encode(btnNickname)}`)
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Image not found');
            }
        })
        .then(imageUrl => {
            const imgElement = button.querySelector('.profile-img-online');
            imgElement.src = `${serverURL}${imageUrl}`;
        })
        .catch(error => {
            console.error('Error fetching image:', error);
        });
}

function updateActiveUserInfo(nameElement, imgElement) {
    fetch(`${serverURL}profile/get?nickname=${encode(nickname)}`)
        .then(response => {
            if (response.ok) {
                return response.text();
            } else {
                throw new Error('Image not found');
            }
        })
        .then(imageUrl => {
            imgElement.src = `${serverURL}${imageUrl}`;
        }).catch(error => {
            console.error(`Error fetching image: `, error);
        });

    if (nameElement.innerHTML !== "") {
        nameElement.innerHTML = "";
    }
    var plainUsername = document.createTextNode(nickname);
    nameElement.appendChild(plainUsername);


}

function showEmojiPicker() {

    emojiPicker.classList.remove("emojiHidden");
    emojiPicker.classList.add("emojiShow");

}


emojiPicker.addEventListener('emoji-click', event => {
    var start = msgInputWindow.selectionStart;
    var end = msgInputWindow.selectionEnd;
    var textBefore;
    var textAfter;
    var text = msgInputWindow.value;
    var emoji = event.detail.unicode;

    if (start < end) {
        textBefore = text.substring(0, start);
        textAfter = text.substring(end, text.length);
    } else {
        textBefore = text.substring(0, start);
        textAfter = text.substring(start, text.length);
    }

    msgInputWindow.value = textBefore + emoji + textAfter;
    msgInputWindow.focus();
    msgInputWindow.selectionStart = start + emoji.length;
    msgInputWindow.selectionEnd = start + emoji.length;

});

function encode(nick) {
    return encodeURIComponent(nick);
}
