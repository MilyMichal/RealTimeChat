
let inputContainer = document.querySelector(".inputContainer");
let chatScreen = document.querySelector(".chat");
let msgInputWindow = document.getElementById("input-msg");
let messageContainer = document.getElementById("messages");
let chatWithElement = document.getElementById("chat-with");
let usersContainer = document.getElementById("users");
let publicBtn = document.getElementById("public-chat-btn");
let historyContainer = document.querySelector(".history-window");

let emojiPicker = document.getElementById('emoji-win');

let privateChatWith;
var loggedOutByButton = false;
let isScrolledToBottom = true;

var actDate = new Date().toLocaleString();

let stompClient = null;
let sock = new SockJS("http://localhost:28852/chat");

register();

var userNameElement = document.getElementById("user-data");
var userName = userNameElement.getAttribute("data-user");


//event listener for logout user from list after closing chat page
window.addEventListener('unload', function (event) {
    if (!loggedOutByButton) {
        logOutUser();
    }

});

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
    getLastestHistory();
}

//function for sending msg to server
function send() {

    let finalMsg;
    if (msgInputWindow.value) {
        if (chatWithElement.innerHTML === "Public chat") {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": userName,
                "date": actDate,
                "type": 'message',
                "sendTo": "public"
            }

        } else {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": userName,
                "date": actDate,
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

    console.log("\n\n ! DEBUG ! MESSAGE TYPE:\n" + message.type);
    if (message.type) {
        if (message.type === "Leave") {

            let disconnectedUser = message.sender;
            let disconnectedMsg = "<div class='event-message-container'> <div class='event-message logout-event'>" + message.content + "</div></div>";
            messageContainer.insertAdjacentHTML("beforeend", disconnectedMsg);
            if (usersContainer.querySelector("." + disconnectedUser)) {
                usersContainer.querySelector("." + disconnectedUser).remove();
            }
        }

        if (message.type === "kick") {
            if (userName === message.sendTo) {
                logOutUser();
                alert("You have been kicked out by admin!");
            }
            let kickedMsg = "<div class='event-message-container'> <div class='event-message  logout-event'>" + message.content + "</div></div>";
            messageContainer.insertAdjacentHTML("beforeend", kickedMsg);
        }

        if (message.type === "BAN") {
            if (userName === message.sendTo) {
                fetch("http://localhost:28852/admin/banned/" + message.sendTo, {
                    method: 'PUT'
                }).then(response => {
                    if (response.ok) {
                        fetch('http://localhost:28852/logout', {
                            method: 'POST'
                        });
                        stompClient.disconnect();
                        window.location.href = 'http://localhost:28852/';
                        alert("Admin banned you")
                    }
                });
            }
            let bannedMsg = "<div class='event-message-container'> <div class='event-message  logout-event'>" + message.content + "</div></div>";
            messageContainer.insertAdjacentHTML("beforeend", bannedMsg);
        }



        if (message.type === "update") {
            console.log("userName before updatemsg: " + userName);
            if (userName === message.sender) {

                if (message.sender !== message.content) {
                    userName = message.content;
                    console.log("userName after updatemsg: " + userName);
                }
            } else {
                let onUserbtn = document.querySelector(`.${message.sender}`);
                let name = onUserbtn.querySelector(`.user`);
                if (message.sender !== message.content) {

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
                        chatWithElement.innerHTML = "Private chat with: " + message.content;
                    }
                } else {
                    setProfilePicture(onUserbtn, message.sender);
                }


                console.log("CLEARING MESSAGE WINDOW");
            }

            messageContainer.innerHTML = "";
            getLastestHistory();

        }



        // displaying newest message
        if (message.type === 'message') {
            let html;
            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {
                if (message.sender === userName) {
                    html = "<div class='message-container revert'><div class='sender'>" + message.sender + "</div>"
                        + "<div class='message right-msg'><div class='date'>" + message.date + "</div>" + message.content + "</div></div></div>";


                } else {
                    html = "<div class='message-container'><div class='sender'>" + message.sender + "</div>"
                        + "<div class='message left-msg'><div class='date'>" + message.date + "</div>" + message.content + "</div></div></div>";


                }
                messageContainer.insertAdjacentHTML("beforeend", html);
                msgInputWindow.value = "";
            }

            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === userName) {
                let incomingMsgUser = document.querySelector("." + message.sender);
                let msgCounter = incomingMsgUser.querySelector(".message-counter");


                usersContainer.insertBefore(incomingMsgUser, usersContainer.firstChild);


                let count = parseInt(msgCounter.innerHTML) + 1;
                msgCounter.innerText = count;
                msgCounter.style.setProperty("visibility", "visible");

            }


            if ((userName == message.sendTo && message.sender == privateChatWith) ||
                (message.sendTo == privateChatWith && message.sender == userName)) {
                if (message.sender === userName) {
                    html = "<div class='message-container revert'><div class='sender'>" + message.sender + "</div>"
                        + "<div class='message right-msg'><div class='date'>" + message.date + "</div>" + message.content + "</div></div>";


                } else {
                    html = "<div class='message-container'><div class='sender'>" + message.sender + "</div>"
                        + "<div class='message left-msg'><div class='date'>" + message.date + "</div>" + message.content + "</div></div>";

                }


                messageContainer.insertAdjacentHTML("beforeend", html);
                msgInputWindow.value = "";

            }
            clearOldMsg();
            if (isScrolledToBottom) {
                messageContainer.scrollTop = messageContainer.scrollHeight - messageContainer.clientHeight;
            }
        }

        // displaying new user in chat and online user panel
        if (message.type === 'newUser') {
            let welcomeMsg = "<div class='event-message-container'> <div class='event-message login-event'>" + message.content + "</div></div>";
            messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);

            if (usersContainer.querySelectorAll("*").length === 0) {
                console.log(" var 1 triggered num of users " + usersContainer.querySelectorAll("*").length);
                fetch("http://localhost:28852/users")
                    .then(response => response.json())
                    .then(data => {

                        data.forEach(onlineUser => {

                            if (onlineUser.nickname !== userName) {


                                let user = `<button class='user-container ${onlineUser.nickname}' type='button'>
                                    <img class='profile-img-online' src= '' alt='Profile Picture'>
                                    <span class='user'>${onlineUser.nickname}</span>
                                    <span class='message-counter'>0</span>
                                    </button >`;

                                usersContainer.insertAdjacentHTML("beforeend", user);
                                let userBtn = document.querySelector("." + onlineUser.nickname);

                                setUpOnlineUserBtn(userBtn);
                                setProfilePicture(userBtn, onlineUser.nickname);

                            }
                        });

                    });


            } else {
                console.log(" var 2 triggered num of users " + usersContainer.querySelectorAll("*").length)
                fetch("http://localhost:28852/users")
                    .then(response => response.json())
                    .then(data => {
                        let lastUser = data[data.length - 1];

                        if (lastUser.nickname !== userName) {

                            let user = `<button class='user-container ${lastUser.nickname}' type='button'>
                                    <img class='profile-img-online' src= '' alt='Profile Picture'>
                                    <span class='user'>${lastUser.nickname}</span>
                                    <span class='message-counter'>0</span>
                                    </button >`;

                            usersContainer.insertAdjacentHTML("beforeend", user);
                            let userBtn = document.querySelector("." + lastUser.nickname);

                            setUpOnlineUserBtn(userBtn);
                            setProfilePicture(userBtn, lastUser.nickname);

                        }

                    });


            }
        }

    }

}
//connecting new user
function onConnectedSuccessfully() {

    stompClient.subscribe("/topic/chat", onMessageReceived);

    stompClient.send("/app/user", {}, JSON.stringify(
        {
            sender: userName,
            type: 'newUser',
            content: userName + ' just joined chatroom. Welcome!',
            sendTo: "public",
            date: actDate
        }));
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
        getLastestHistory();
        privateChatWith = "";
        publicBtn.disabled = true;
        publicBtn.style.setProperty("color", "darkgrey");
    }
}
// getting message history form server
function getLastestHistory() {
    if (chatWithElement.innerHTML === "Public chat") {
        fetch("http://localhost:28852/history/public-latest")
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {
                    if (msg.type === "newUser") {
                        let welcomeMsg = "<div class='event-message-container'><div class='event-message  login-event'> " + msg.content + "</div></div>";
                        messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);
                    }
                    if (msg.type === "Leave" || msg.type === "kick") {
                        let disconnectedMsg = "<div class='event-message-container'> <div class='event-message  logout-event'>" + msg.content + "</div></div>";
                        messageContainer.insertAdjacentHTML("beforeend", disconnectedMsg);
                    }


                    if (msg.type === "update" && msg.sender != msg.content) {
                        let updateMsg = "<div class='event-message-container'> <div class='event-message  login-event'>" + msg.sender + " changed his name to: " + msg.content + "</div></div>";
                        messageContainer.insertAdjacentHTML("beforeend", updateMsg);
                    }


                    if (msg.type === "message") {
                        let history;
                        if (msg.sender === userName) {
                            history =
                                "<div class='message-container revert'><div class='sender'>"
                                + msg.sender + "</div>"
                                + "<div class='message right-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";

                        } else {
                            history =
                                "<div class='message-container'><div class='sender'>"
                                + msg.sender + "</div>"
                                + "<div class='message left-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";
                        }
                        messageContainer.insertAdjacentHTML("beforeend", history);
                    }
                });
            });

    } else {
        fetch("http://localhost:28852/history/" + privateChatWith + "-" + userName + "/latest")
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {

                    let history;
                    if (msg.sender === userName) {
                        history =
                            "<div class='message-container revert'><div class='sender'>"
                            + msg.sender + "</div>"
                            + "<div class='message right-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";

                    } else {
                        history =
                            "<div class='message-container'><div class='sender'>"
                            + msg.sender + "</div>"
                            + "<div class='message left-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";
                    }
                    messageContainer.insertAdjacentHTML("beforeend", history);

                });
            });

    }

}

function getFullPublicHistory() {


    historyContainer.innerHTML = "";
    fetch("http://localhost:28852/history/public")
        .then(response => response.json())
        .then(message => {
            message.forEach((msg) => {
                if (msg.type === "newUser") {
                    let welcomeMsg = "<div class='event-message-container'><div class='event-message  login-event'> " + msg.content + "</div></div>";
                    historyContainer.insertAdjacentHTML("beforeend", welcomeMsg);
                }
                if (msg.type === "Leave" || msg.type === "kick") {
                    let disconnectedMsg = "<div class='event-message-container'> <div class='event-message  logout-event'>" + msg.content + "</div></div>";
                    historyContainer.insertAdjacentHTML("beforeend", disconnectedMsg);
                }

                if (msg.type === "update") {
                    let updateMsg = "<div class='event-message-container'> <div class='event-message  login-event'>" + msg.sender + " changed his name to: " + msg.content + "</div></div>";
                    messageContainer.insertAdjacentHTML("beforeend", updateMsg);
                }

                if (msg.type === "message") {
                    let history;
                    if (msg.sender === userName) {
                        history =
                            "<div class='message-container revert'><div class='sender'>"
                            + msg.sender + "</div>"
                            + "<div class='message right-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";

                    } else {
                        history =
                            "<div class='message-container'><div class='sender'>"
                            + msg.sender + "</div>"
                            + "<div class='message left-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";
                    }
                    historyContainer.insertAdjacentHTML("beforeend", history);
                }
            });
        });
}

function getFullPersonalHistory() {
    historyContainer.innerHTML = "";
    let selectedUser = document.getElementById("user-to-find");

    fetch("http://localhost:28852/history/" + selectedUser.value + "-" + userName)
        .then(response => response.json())
        .then(message => {
            message.forEach((msg) => {

                let history;
                if (msg.sender === userName) {
                    history =
                        "<div class='message-container revert'><div class='sender'>"
                        + msg.sender + "</div>"
                        + "<div class='message right-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";

                } else {
                    history =
                        "<div class='message-container'><div class='sender'>"
                        + msg.sender + "</div>"
                        + "<div class='message left-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";
                }
                historyContainer.insertAdjacentHTML("beforeend", history);

            });
        });

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
        chatWithElement.innerHTML = "Private chat with: " + btnUserName;

        privateChatWith = btnUserName;
        getLastestHistory();
    });
}


function logOutUser() {
    loggedOutByButton = true;
    try {
        fetch('http://localhost:28852/logout', {
            method: 'POST'
        }).then(response => {
            if (response.ok) {
                window.location.href = 'http://localhost:28852/';
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

// Close the dropdown menu if the user clicks outside of it or close modal if clics outside
window.onclick = function (event) {
    /*console.log(event.target);*/
    if ((!event.target.matches('.emoji-picker') && !event.target.matches('.emoji-btn')) && emojiPicker.classList.contains('emojiShow')) {
        /* console.log("off targer");*/
        emojiPicker.classList.remove('emojiShow');
        emojiPicker.classList.add('emojiHidden');
    }

    if (!event.target.matches('.dropbtn')) {
        var dropupMenu = document.getElementById("dropupMenu");


        if (dropupMenu.classList.contains('show')) {
            dropupMenu.classList.remove('show');
        }
    }
    if (event.target.matches('.modal')) {
        var modals = document.getElementsByClassName("modal");
        Array.prototype.forEach.call(modals, function (modal) {
            if (modal.style.display == "block") {
                modal.style.display = "none";
            }
        });
        document.querySelector(".history-window").innerHTML = "";
        document.querySelector(".response").innerHTML = "";
    }

}


/*Modal display settings */
function openSelectedModal(modal) {
    document.querySelector(`.${modal}`).style.display = "block";

}


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
//#endregion

//#region SubmitProfileUpdate
document.getElementById("profile-update-form").addEventListener("submit", function (event) {
    event.preventDefault();

    const formData = new FormData(this);
    let response = document.querySelector(".response");
    fetch("http://localhost:28852/profile/update", {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            let updateMsg;
            if (Object.keys(data).length > 1) {
                if (Object.keys(data).length == 2 && data.hasOwnProperty("pass")) {
                    console.log("JUST PASS UDPATED");
                } else {
                    if (data.hasOwnProperty("newUserName")) {
                        console.log("SEND UPDATE MSG");
                        updateMsg =
                        {
                            "sender": userName,
                            "content": data["newUserName"],
                            "date": actDate,
                            "type": 'update',
                            "sendTo": "public"
                        }

                        fetch('http://localhost:28852/history/update', {
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

                                console.log("HISTORY NAME Update is succesfull");

                                stompClient.send("/app/chat", {}, JSON.stringify(updateMsg));

                            });

                    } else {

                        updateMsg =
                        {
                            "sender": userName,
                            "content": userName,
                            "date": actDate,
                            "type": 'update',
                            "sendTo": "public"

                        }

                        console.log("PROFILE PIC UPDATE SUCCESFUL")
                        stompClient.send("/app/chat", {}, JSON.stringify(updateMsg));
                    }
                }
            }
            console.log("PROFILE UPDATE DEBGU: " + data["message"]);
            if (data["message"].includes("successfully")) {
                response.style.color = "#345635";

            } else {
                response.style.color = "#7E102C";
            }
            response.innerHTML = `${data["message"]}`;
        });
    document.getElementById("act-pass-input").value = "";
    document.getElementById("new-pass-input").value = "";
    document.getElementById("name-input").value = "";
    document.getElementById("fileData").value = null;
    document.getElementById("drop").innerHTML = "Drag your profile picture HERE";

});
//#endregion



function clearOldMsg() {
    console.log("nume of messages in window: " + messageContainer.querySelectorAll(".message-container").length);
    if (messageContainer.querySelectorAll(".message-container").length + messageContainer.querySelectorAll(".event-message-container").length > 15) {
        messageContainer.removeChild(messageContainer.firstElementChild);
    }

}

function setProfilePicture(button, nickname) {

    fetch("http://localhost:28852/profile/get/" + nickname)
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