
const URL = "http://localhost:28852/";
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

let privateChatWith;
var loggedOutByButton = false;
let isScrolledToBottom = true;

var actDate = () => new Date().toLocaleString();

let stompClient = null;
let sock = new SockJS(`${URL}chat`);

register();

var userNameElement = document.getElementById("user-data");
var userName = userNameElement.getAttribute("data-user");


//event listener for logout user from list after closing chat page
/*window.addEventListener('unload', function (event) {
    if (!loggedOutByButton) {
        logOutUser();
    }

});*/

//event listener for UNDO step from chat page

window.addEventListener('popstate', function (event) {

    if (!event.state) {
        logOutUser();

    }
});


if (!sessionStorage.getItem('chatVisited')) {
    sessionStorage.setItem('chatVisited', 'true');
    history.pushState({ chat: true }, '', '');
}


//event listener for sending msg by pressing Enter
msgInputWindow.addEventListener("keypress", (event) => {
    if (event.code === "Enter") {
        send();
    }
});

messageContainer.addEventListener('scroll', () => {
    isScrolledToBottom = messageContainer.scrollHeight - messageContainer.clientHeight <= messageContainer.scrollTop + 1;

})

function register() {

    // establishing connection

    stompClient = Stomp.over(sock);
    stompClient.connect({}, onConnectedSuccessfully, (error) => {
        console.log('unable to connect' + error);
    });

    // loading chat history for new user
    getLatestHistory();
}

//function for sending msg to server
function send() {

    let finalMsg;
    if (msgInputWindow.value) {
        if (chatWithElement.innerHTML === "Public chat") {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": userName,
                "date": actDate(),
                "type": 'message',
                "sendTo": "public"
            }

        } else {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": userName,
                "date": actDate(),
                "type": 'message',
                "sendTo": privateChatWith
            }

        }
        stompClient.send("/app/chat", {}, JSON.stringify(finalMsg));
    }
}

//function for getting messages and online users from server
function onMessageReceived(payload) {

    var message = JSON.parse(payload.body);


    if (message.type) {
        if (message.type === "Leave") {

            messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
            if (usersContainer.querySelector(`.${message.sender}`)) {
                usersContainer.querySelector(`.${message.sender}`).remove();
            }
        }

        if (message.type === "kick") {
            if (userName === message.sendTo) {
                logOutUser();
                alert("You have been kicked out by admin!");
            }

            messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        }

        if (message.type === "BAN") {
            if (userName === message.sendTo) {
                fetch(`${URL}admin/banned/${message.sendTo}`, {
                    method: 'PUT'
                }).then(response => {
                    if (response.ok) {
                        fetch(`${URL}logout`, {
                            method: 'POST'
                        });
                        stompClient.disconnect();
                        window.location.href = URL;
                        alert("Admin banned you")
                    }
                });
            }

            messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        }

        if (message.type === "update-name") {

            if (userName === message.sender) {

                if (message.sender !== message.content) {
                    userName = message.content;

                }
            } else {
                let onUserbtn = document.querySelector(`.${message.sender}`);
                let name = onUserbtn.querySelector(`.user`);

                Array.from(document.getElementById("user-to-find").options).forEach(option => {

                    if (option.value === message.sender) {
                        option.value = message.content;
                        option.text = message.content;

                    }
                });

                onUserbtn.classList.replace(`${message.sender}`, `${message.content}`);

                name.innerHTML = `${message.content}`;
                name.classList.replace(`${message.sender}`, `${message.content}`);
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
            if (userName !== message.sender) {
                let onUserbtn = document.querySelector(`.${message.sender}`);

                setProfilePicture(onUserbtn, message.sender);

            }
            messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        }

    }


    // displaying newest message
    if (message.type === 'message') {
        let html;
        if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {

            messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
            msgInputWindow.value = "";
        }

        if (chatWithElement.innerHTML === "Public chat" && message.sendTo === userName) {
            let incomingMsgUser = document.querySelector(`.${message.sender}`);
            let msgCounter = incomingMsgUser.querySelector(".message-counter");


            usersContainer.insertBefore(incomingMsgUser, usersContainer.firstChild);


            let count = parseInt(msgCounter.innerHTML) + 1;
            msgCounter.innerText = count;
            msgCounter.style.setProperty("visibility", "visible");

        }

        if ((userName == message.sendTo && message.sender == privateChatWith) ||
            (message.sendTo == privateChatWith && message.sender == userName)) {

            messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
            msgInputWindow.value = "";

        }
        clearOldMsg();
        if (isScrolledToBottom) {
            messageContainer.scrollTop = messageContainer.scrollHeight - messageContainer.clientHeight;
        }
    }

    // displaying new user in chat and online user panel
    if (message.type === 'newUser') {
        let welcomeMsg = `<div class='event-message-container'> <div class='event-message login-event'>${message.content}</div></div>`;
        messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);

        if (usersContainer.querySelectorAll("*").length === 0) {
            fetch(`${URL}users`)
                .then(response => response.json())
                .then(data => {

                    data.forEach(onlineUser => {
                        updateOnlineUserList(onlineUser);

                    });

                });


        } else {

            fetch(`${URL}users`)
                .then(response => response.json())
                .then(data => {
                    let lastUser = data[data.length - 1];
                    updateOnlineUserList(lastUser);

                });


        }
    }

}


//connecting new user
function onConnectedSuccessfully() {

    stompClient.subscribe("/topic/chat", onMessageReceived);

    fetch(`${URL}users`)
        .then(response => response.json())
        .then(data => {
            if (!data.includes(userName)) {

                stompClient.send("/app/user", {}, JSON.stringify(
                    {
                        sender: userName,
                        type: 'newUser',
                        content: `${userName} just joined chatroom. Welcome!`,
                        sendTo: "public",
                        date: actDate()
                    }));
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
        privateChatWith = "";
        publicBtn.disabled = true;
        publicBtn.style.setProperty("color", "darkgrey");
    }
}
// getting message history form server
function getLatestHistory() {
    if (chatWithElement.innerHTML === "Public chat") {
        fetch(`${URL}history/public-latest`)
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {

                    messageContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));
                });
            });

    } else {
        fetch(`${URL}history/${privateChatWith}-${userName}/latest`)
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {

                    messageContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));

                });
            });

    }

}

function getFullPublicHistory() {


    historyContainer.innerHTML = "";
    fetch(`${URL}history/public`)
        .then(response => response.json())
        .then(message => {
            message.forEach((msg) => {

                historyContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));
            });
        });
}

function getFullPersonalHistory() {
    historyContainer.innerHTML = "";
    let selectedUser = document.getElementById("user-to-find");

    fetch(`${URL}history/${selectedUser.value}-${userName}`)
        .then(response => response.json())
        .then(message => {
            message.forEach((msg) => {

                historyContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));

            });
        });

}

// function for creating message to display
function prepareMessage(messageData) {
    let completedMessage;

    if (messageData.type === "newUser") {
        completedMessage = `<div class='event-message-container'><div class='event-message  login-event'>${messageData.content}</div></div>`;
    }
    if (messageData.type === "Leave" || messageData.type === "kick" || messageData.type === "BAN") {
        console.log(`DEBUG MESSAGES: TYPE - ${messageData.type} \n CONTENT - ${messageData.content}`);
        completedMessage = `<div class='event-message-container'> <div class='event-message  logout-event'>${messageData.content}</div></div>`;
    }

    if (messageData.type === "update-name") {
        completedMessage = `<div class='event-message-container'> <div class='event-message  login-event'>${messageData.sender} changed his name to: ${messageData.content}</div></div>`;
    }

    if (messageData.type === "update-profilePic") {
        completedMessage = `<div class='event-message-container'> <div class='event-message  login-event'>${messageData.sender} changed his profile picture</div></div>`;
    }

    if (messageData.type === "message") {

        if (messageData.sender === userName) {
            completedMessage = `<div class='message-container revert'><div class='sender'> ${messageData.sender}<div class='date'>${messageData.date}</div></div>
                        <div class='message right-msg'> ${messageData.content}</div></div>`;

        } else {
            completedMessage = `<div class='message-container'><div class='sender'> ${messageData.sender}<div class='date'> ${messageData.date}</div></div>
                        <div class='message left-msg'> ${messageData.content}</div></div>`;
        }

    }
    return completedMessage;
}

function updateOnlineUserList(userData) {
    if (userData.nickname !== userName) {

        let user = `<button class='user-container ${userData.nickname}' type='button'>
                                    <img class='profile-img-online' src= '' alt='Profile Picture'>
                                    <span class='user'>${userData.nickname}</span>
                                    <span class='message-counter'>0</span>
                                    </button >`;

        usersContainer.insertAdjacentHTML("beforeend", user);
        let userBtn = document.querySelector(`.${userData.nickname}`);

        setUpOnlineUserBtn(userBtn);
        setProfilePicture(userBtn, userData.nickname);
    }
}


// online user button set up
function setUpOnlineUserBtn(btn) {

    btn.addEventListener("click", () => {
        if (chatWithElement.innerHTML !== "Public chat") {

            document.querySelector(".user-container.active").classList.remove("active");
        }
        btn.classList.add("active");

        publicBtn.disabled = false;
        publicBtn.style.setProperty("color", "#C6AC8E");
        messageContainer.innerHTML = "";
        if (btn.querySelector(".message-counter").innerHTML !== "0") {
            btn.querySelector(".message-counter").style.setProperty("visibility", "hidden");
            btn.querySelector(".message-counter").innerHTML = "0";
        }

        var btnUserName = btn.querySelector(".user").innerHTML;
        chatWithElement.innerHTML = `Private chat with: ${btnUserName}`;

        privateChatWith = btnUserName;
        getLatestHistory();
    });
}


function logOutUser() {
    loggedOutByButton = true;
    try {
        fetch(`${URL}logout`, {
            method: 'POST'
        }).then(response => {
            if (response.ok) {
                window.location.href = URL;
            }
        });
    } catch (error) {
        console.error('Error while logout:', error);
    }
    stompClient.disconnect();
}

/* When the user clicks on the button,
toggle between hiding and showing the dropdown content */
function show() {
    document.getElementById("dropupMenu").classList.toggle("show");
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
if (userName !== "Admin") {
    document.getElementById("delete-profile-form").addEventListener("submit", function (event) {
        event.preventDefault();
        const formData = new FormData(this);
        let deleteResponse = document.querySelector('.delete-response');
        deleteResponse.innerHTML = "";

        fetch(`${URL}profile/delete`, { method: 'POST', body: formData })
            .then(response => {

                if (response.ok) {
                    alert("Your profile was succesfully deleted!");
                    logOutUser();
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
    fetch(`${URL}profile/update`, {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            let updateMsg;
            if (Object.keys(data).length > 1) {
                if (Object.keys(data).length == 2 && data.hasOwnProperty("pass")) {

                } else {
                    if (data.hasOwnProperty("newUserName")) {

                        updateMsg =
                        {
                            "sender": userName,
                            "content": data["newUserName"],
                            "date": actDate(),
                            "type": 'update-name',
                            "sendTo": "public"
                        }

                        fetch(`${URL}history/update`, {
                            method: 'PUT',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                'prevName': userName,
                                'actName': data["newUserName"]
                            })
                        })
                            .then(() => {

                                stompClient.send("/app/chat", {}, JSON.stringify(updateMsg));

                            });

                    } else {

                        updateMsg =
                        {
                            "sender": userName,
                            "content": userName,
                            "date": actDate(),
                            "type": 'update-profilePic',
                            "sendTo": "public"

                        }

                        stompClient.send("/app/chat", {}, JSON.stringify(updateMsg));
                    }
                }

                if (data["message"].includes("successfully")) {
                    response.style.color = "#345635";

                } else {
                    response.style.color = "#7E102C";
                }
                response.innerHTML = `${data["message"]}`;
            });
    clearUpdateForm();

});
//#endregion

function clearUpdateForm() {
    document.getElementById("act-pass-input").value = "";
    document.getElementById("new-pass-input").value = "";
    document.getElementById("name-input").value = "";
    document.getElementById("fileData").value = null;
    document.getElementById("drop").innerHTML = "Drag your profile picture HERE <br> Max size of picture: 2MB";
}

function clearOldMsg() {
    if (messageContainer.querySelectorAll(".message-container").length + messageContainer.querySelectorAll(".event-message-container").length > 15) {
        messageContainer.removeChild(messageContainer.firstElementChild);
    }

}

function setProfilePicture(button, nickname) {

    fetch(`${URL}profile/get/${nickname}`)
        .then(response => {
            if (response.ok) {
                return response.blob();
            } else {
                throw new Error('Image not found');
            }
        })
        .then(blob => {
            const imageUrl = URL.createObjectURL(blob);
            const imgElement = button.querySelector('.profile-img-online');
            imgElement.src = imageUrl;
        })
        .catch(error => {
            console.error('Error fetching image:', error);
        });
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