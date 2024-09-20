
const serverURL = document.getElementById("serverURL").getAttribute("data-URL");

const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

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
let sock = new SockJS(`${serverURL}chat`, null, {
    debug: false
});

register();

var nicknameElement = document.getElementById("user-data");
var nickname = nicknameElement.getAttribute("data-user");



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
        event.preventDefault();
        send();
    }
});

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

    // loading chat history for new user
    // getLatestHistory();
}

//function for sending msg to server
function send() {

    let finalMsg;
    if (msgInputWindow.value) {
        if (chatWithElement.innerHTML === "Public chat") {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": nickname,
                "date": actDate(),
                "type": 'message',
                "sendTo": "public"
            }

        } else {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": nickname,
                "date": actDate(),
                "type": 'message',
                "sendTo": privateChatWith
            }

        }
        stompClient.send("/app/chat", {}, JSON.stringify(finalMsg));
        msgInputWindow.value = "";
    }

}

//function for getting messages and online users from server
function onMessageReceived(payload) {

    var message = JSON.parse(payload.body);


    if (message.type) {
        if (message.type === "Leave") {
            prepareMessage(messageContainer, message);
            //messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
            if (usersContainer.querySelector(`.${message.sender}`)) {
                usersContainer.querySelector(`.${message.sender}`).remove();
            }
        }

        if (message.type === "kick") {
            if (nickname === message.sendTo) {
                logOutUser();
                alert("You have been kicked out by admin!");
            }
            prepareMessage(messageContainer, message);
            //messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        }

        if (message.type === "BAN") {
            if (nickname === message.sendTo) {
                fetch(`${serverURL}admin/banned/${message.sendTo}-${message.content}`, {
                    method: 'PUT'
                }).then(response => {
                    if (response.ok) {
                        fetch(`${serverURL}logout`, {
                            method: 'POST'
                        });
                        stompClient.disconnect();
                        window.location.href = serverURL;
                        alert("Admin banned you")
                    }
                });
            }
            prepareMessage(messageContainer, message);
            //messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        }

        if (message.type === "update-name") {

            if (nickname === message.sender) {

                // if (message.sender !== message.content) {
                nickname = message.content;

                //  }
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
            if (nickname !== message.sender) {
                let onUserbtn = document.querySelector(`.${message.sender}`);

                setProfilePicture(onUserbtn, message.sender);

            }
            prepareMessage(messageContainer, message);
            // messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        }

    }


    // displaying newest message
    if (message.type === 'message') {
        // let html;
        if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {

            prepareMessage(messageContainer, message);
            // messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
            //msgInputWindow.value = "";
        }

        if (chatWithElement.innerHTML === "Public chat" && message.sendTo === nickname ) {
            let incomingMsgUser = document.querySelector(`.${message.sender}`);
            let msgCounterContainer = incomingMsgUser.querySelector(".message-counter-container");
            let msgCounter = incomingMsgUser.querySelector(".message-counter");


            usersContainer.insertBefore(incomingMsgUser, usersContainer.firstChild);


            let count = parseInt(msgCounter.innerHTML) + 1;
            msgCounter.innerText = count;
            msgCounterContainer.style.setProperty("visibility", "visible");

        }

        if ((nickname == message.sendTo && message.sender == privateChatWith) ||
            (message.sendTo == privateChatWith && message.sender == nickname )) {
            prepareMessage(messageContainer, message);
            // messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
            //msgInputWindow.value = "";

        }
        clearOldMsg();
        if (isScrolledToBottom) {
            messageContainer.scrollTop = messageContainer.scrollHeight - messageContainer.clientHeight;
        }
    }

    // displaying new user in chat and online user panel
    if (message.type === 'newUser') {
        //let welcomeMsg = `<div class='event-message-container'> <div class='event-message login-event'>${message.content}</div></div>`;
        // messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);
        prepareMessage(messageContainer, message);
        // messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        fetch(`${serverURL}users`)
            .then(response => response.json())
            .then(data => {
                showOnlineUsers(data);
                /* if (usersContainer.querySelectorAll("*").length === 0) {
                     fetch(`${serverURL}users`)
                         .then(response => response.json())
                         .then(data => {
         
                             data.forEach(onlineUser => {
                                 updateOnlineUserList(onlineUser);
         
                             });
         
                         });
         
         
                 } else {
         
                     fetch(`${serverURL}users`)
                         .then(response => response.json())
                         .then(data => {
                             let lastUser = data[data.length - 1];
                             updateOnlineUserList(lastUser);
         
                         });
         
         
                 }*/
            });

    }
}

function showOnlineUsers(usersList) {
    /* fetch(`${serverURL}users`)
         .then(response => response.json())
         .then(data => {*/
    if (usersContainer.querySelectorAll("*").length === 0) {
        usersList.forEach(onlineUser => {
            updateOnlineUserList(onlineUser);

        });
    } else {
        let lastUser = usersList[usersList.length - 1];
        updateOnlineUserList(lastUser);
    }
    // });
}


//connecting new user
function onConnectedSuccessfully() {

    //
    messageContainer.innerHTML = "";
    getLatestHistory();
    //

    stompClient.subscribe("/topic/chat", onMessageReceived);

    fetch(`${serverURL}users`)
        .then(response => response.json())
        .then(data => {
            let info = JSON.stringify(data);
            console.log(`DEBUG onConnectionSuccesfully method : ${info}`);
            if (!info.includes(nickname)) {
                console.log(`DEBUG onConnectionSuccesfully NOT INCLUDED: ${nickname}`);
                stompClient.send("/app/user", {}, JSON.stringify(
                    {
                        sender: nickname,
                        type: 'newUser',
                        content: `${nickname} just joined chatroom. Welcome!`,
                        sendTo: "public",
                        date: actDate()
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
                    // messageContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));
                });
            });

    } else {
        fetch(`${serverURL}history/${privateChatWith}-${nickname}/latest`)
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


                /* fetch(`${serverURL}history/${privateChatWith}-${userName}/latest`)
                    .then(response => response.json())
                    .then(message => {
                        message.forEach((msg) => {
                            prepareMessage(messageContainer, msg);
                           // messageContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));
        
                        });
                    });*/

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
                //historyContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));
            });
        });
}

function getFullPersonalHistory() {
    historyContainer.innerHTML = "";
    let selectedUser = document.getElementById("user-to-find");

    fetch(`${serverURL}history/${selectedUser.value}-${nickname}`)
        .then(response => response.json())
        .then(message => {
            message.forEach((msg) => {
                prepareMessage(historyContainer, msg);
                //historyContainer.insertAdjacentHTML("beforeend", prepareMessage(msg));

            });
        });

}

// function for creating message to display
function prepareMessage(container, messageData) {
    let completedMessage;
    const rawText = document.createTextNode(messageData.content);
    //
    /*const eventMessageContainer = document.createElement("div");
    eventMessageContainer.className = "event-message-container";
    const eventMessage = document.createElement("div");
    eventMessage.className = "event-message";
    eventMessageContainer.appendChild(eventMessage);*/

    //
    const newMessageContainer = document.createElement("div");
    newMessageContainer.className = "message-container";

    const senderContainer = document.createElement("div");
    senderContainer.className = "sender";

    const dateContainer = document.createElement("div");
    dateContainer.className = "date";

    const messageContent = document.createElement("div");
    messageContent.className = "message";
    //


    if (messageData.type === "message") {

        if (messageData.sender === nickname) {
            //
            newMessageContainer.classList.add("revert");
            messageContent.classList.add("right-msg");
            messageContent.appendChild(rawText);
            senderContainer.innerHTML = messageData.sender;
            dateContainer.innerHTML = messageData.date;
            senderContainer.appendChild(dateContainer);
            newMessageContainer.appendChild(senderContainer);
            //newMessageContainer.appendChild(dateContainer);
            newMessageContainer.appendChild(messageContent);


            //

            /*completedMessage = `<div class='message-container revert'><div class='sender'> ${messageData.sender}<div class='date'>${messageData.date}</div></div>
                        <div class='message right-msg'> ${messageData.content}</div></div>`;*/

        } else {
            //

            messageContent.classList.add("left-msg");
            messageContent.appendChild(rawText);
            senderContainer.innerHTML = messageData.sender;
            dateContainer.innerHTML = messageData.date;
            senderContainer.appendChild(dateContainer);
            newMessageContainer.appendChild(senderContainer);
            //newMessageContainer.appendChild(dateContainer);
            newMessageContainer.appendChild(messageContent);
            //
            /* completedMessage = `<div class='message-container'><div class='sender'> ${messageData.sender}<div class='date'> ${messageData.date}</div></div>
                         <div class='message left-msg'> ${messageData.content}</div></div>`;*/
        }
        container.appendChild(newMessageContainer);

    } else {



        if (messageData.type === "newUser") {


            /* eventMessageContainer.querySelector(".event-message").classList.add("login-event");
             eventMessageContainer.querySelector(".event-message").innerHTML = messageData.content;
             
             messageContainer.appendChild(eventMessageContainer);*/
            completedMessage = `<div class='event-message-container'><div class='event-message  login-event'>${messageData.content}</div></div>`;
        }
        if (messageData.type === "Leave" || messageData.type === "kick" /*|| messageData.type === "BAN"*/) {
            //eventMessageContainer.querySelector(".event-message").classList.add("logout-event");
            // eventMessageContainer.querySelector(".event-message").appendChild(rawText);
            completedMessage = `<div class='event-message-container'> <div class='event-message  logout-event'>${messageData.content}</div></div>`;
        }

        if (messageData.type === "BAN") {
            completedMessage = `<div class='event-message-container'> <div class='event-message  logout-event'>${messageData.sendTo} was banned by admin for ${messageData.content} minutes! </div></div>`;
        }

        if (messageData.type === "update-name") {
            completedMessage = `<div class='event-message-container'> <div class='event-message  login-event'>${messageData.sender} changed his name to: ${messageData.content}</div></div>`;
        }

        if (messageData.type === "update-profilePic") {
            completedMessage = `<div class='event-message-container'> <div class='event-message  login-event'>${messageData.sender} changed his profile picture</div></div>`;
        }
        container.insertAdjacentHTML("beforeend", completedMessage);

    }

}

function updateOnlineUserList(userData) {
    if (userData.nickname !== nickname ) {

        let user = `<button class='user-container ${userData.nickname}' type='button'>
                                    <img class='profile-img-online' src= '' alt='Profile Picture'>
                                    <span class='user'>${userData.nickname}</span>
                                    <span class="message-counter-container">
                                    <span class="msg-counter-bg">
                                    <svg id="Layer_1" xmlns="http://www.w3.org/2000/svg" width="17px"
                                    height="17px" viewBox="0 0 64 64"
                                    xml:space="preserve">
                                    <g>
	                                    <rect x="1" y="13" fill="none" stroke="#C6AC8E" stroke-width="2" stroke-miterlimit="10" width="62" height="37"/>
	                                    <polyline fill="none" stroke="#C6AC8E" stroke-width="2" stroke-miterlimit="10" points="1,13 32,33 63,13"/>
                                    </g>
                                    </svg>
                                    </span>
                                    <span class="message-counter">0</span>
                                    </span>
                                                                       
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
            btn.querySelector(".message-counter-container").style.setProperty("visibility", "hidden");
            btn.querySelector(".message-counter").innerHTML = "0";
        }

        var btnUserName= btn.querySelector(".user").innerHTML;
        chatWithElement.innerHTML = `Private chat with: ${btnUserName}`;

        privateChatWith = btnUserName;
        getLatestHistory();
    });
}


function logOutUser() {
    /*const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');*/

    loggedOutByButton = true;
    try {
        fetch(`${serverURL}logout`, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            }
        }).then(response => {
            if (response.ok) {
                window.location.href = serverURL;
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

        


        fetch(`${serverURL}profile/delete`, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken },
            body: formData
        })
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
    fetch(`${serverURL}profile/update`, {
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
                            "sender": nickname,
                            "content": data["newUserName"],
                            "date": actDate(),
                            "type": 'update-name',
                            "sendTo": "public"
                        }

                        fetch(`${serverURL}history/update`, {
                            method: 'PUT',
                            headers: {
                                'Content-Type': 'application/json',
                                [csrfHeader]: csrfToken
                                
                            },
                            body: JSON.stringify({
                                'prevName': nickname,
                                'actName': data["newUserName"]
                            })
                        })
                            .then(() => {

                                stompClient.send("/app/chat", {}, JSON.stringify(updateMsg));

                            });

                    } else {

                        updateMsg =
                        {
                            "sender": nickname,
                            "content": nickname,
                            "date": actDate(),
                            "type": 'update-profilePic',
                            "sendTo": "public"

                        }

                        stompClient.send("/app/chat", {}, JSON.stringify(updateMsg));
                    }
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

    /*});*/
});
//#endregion

function clearUpdateForm() {
    document.getElementById("act-pass-input").value = "";
    document.getElementById("new-pass-input").value = "";
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

    fetch(`${serverURL}profile/get/${btnNickname}`)
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
