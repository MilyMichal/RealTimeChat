let inputContainer = document.querySelector(".inputContainer");
let chatScreen = document.querySelector(".chat");
let msgInputWindow = document.getElementById("input-msg");
let messageContainer = document.getElementById("messages");
let chatWithElement = document.getElementById("chat-with");
let usersContainer = document.getElementById("users");
let publicBtn = document.getElementById("public-chat-btn");
let privateChatWith;
var loggedOutByButton = false;


let stompClient = null;

register();

var userNameElement = document.getElementById("user-data");
var userName = userNameElement.getAttribute("data-user");



//event listener for logout user from list after closing chat page
window.addEventListener('unload', function (event) {
    if (!loggedOutByButton) {
        logOutUser();
    }

});



//event listener for sending msg by pressing Enter
msgInputWindow.addEventListener("keypress", (event) => {
    if (event.code === "Enter") {
        send();
    }
});


function register() {

    // establishing connection
    let sock = new SockJS("http://localhost:28852/chat");
    stompClient = Stomp.over(sock);
    stompClient.connect({}, onConnectedSuccessfully, (error) => {
        console.log('unable to connect' + error);
    });

    // loading chat history for new user
    getHistory();
}

//function for sending msg to server
function send() {
    let date = new Date().toLocaleString();
    let finalMsg;
    if (msgInputWindow.value) {
        if (chatWithElement.innerHTML === "Public chat") {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": userName,
                "date": date,
                "type": 'message',
                "sendTo": "public"
            }

        } else {
            finalMsg = {
                "content": msgInputWindow.value,
                "sender": userName,
                "date": date,
                "type": 'message',
                "sendTo": privateChatWith
            }

        }
        stompClient.send("/app/chat", {}, JSON.stringify(finalMsg));
    }
}

//function for getting messages and online users from server
function onMessageReceived(payload) {
    // console.log("! DEBUG ! ON MESSAGE RECEIVED PAYLOAD : \n" + payload) ;
    var message = JSON.parse(payload.body);
    //console.log("\n\n ! DEBUG ! PAYLOAD BODY:\n" + message);
    console.log("\n\n ! DEBUG ! MESSAGE TYPE:\n" + message.type);
    if (message.type) {
        if (message.type === "Leave") {
            let date = new Date().toLocaleString();
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
        }


        // displaying newest message
        if (message.type === 'message') {
            let html;
            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {
                if (message.sender === userName) {
                    html = "<div class='new-message-container revert'><div class='new-sender'>" + message.sender + "</div>"
                        + "<div class='new-message right-msg'><div class='new-date'>" + message.date + "</div>" + message.content + "</div></div></div>";


                } else {
                    html = "<div class='new-message-container'><div class='new-sender'>" + message.sender + "</div>"
                        + "<div class='new-message left-msg'><div class='new-date'>" + message.date + "</div>" + message.content + "</div></div></div>";


                }
                messageContainer.insertAdjacentHTML("beforeend", html);
                msgInputWindow.value = "";
            }

            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === userName) {
                let incomingMsgUser = document.querySelector("." + message.sender);
                let msgCounter = incomingMsgUser.querySelector(".new-message-counter");


                usersContainer.insertBefore(incomingMsgUser, usersContainer.firstChild);


                let count = parseInt(msgCounter.innerHTML) + 1;
                msgCounter.innerText = count;
                msgCounter.style.setProperty("visibility", "visible");
                console.log("type: " + count.type);
                console.log(count);
            }


            if ((userName == message.sendTo && message.sender == privateChatWith) ||
                (message.sendTo == privateChatWith && message.sender == userName)) {
                if (message.sender === userName) {
                    html = "<div class='new-message-container revert'><div class='new-sender'>" + message.sender + "</div>"
                        + "<div class='new-message right-msg'><div class='new-date'>" + message.date + "</div>" + message.content + "</div></div>";


                } else {
                    html = "<div class='new-message-container'><div class='new-sender'>" + message.sender + "</div>"
                        + "<div class='new-message left-msg'><div class='new-date'>" + message.date + "</div>" + message.content + "</div></div>";



                }
                messageContainer.insertAdjacentHTML("beforeend", html);
                msgInputWindow.value = "";

            }

        }

        // displaying new user in chat and online user panel
        if (message.type === 'newUser') {
            let welcomeMsg = "<div class='event-message-container'> <div class='event-message login-event'>" + message.content + "</div></div>";
            messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);

            if (usersContainer.querySelectorAll("*").length === 0) {
                console.log(" var 1 triggered num of users " + usersContainer.querySelectorAll("*").length)
                fetch("http://localhost:28852/users")
                    .then(response => response.json())
                    .then(data => {

                        data.forEach(map => {

                            if (map["nickname"] !== userName) {

                                let user = `<button class='user-container ${map["nickname"]}' type='button'>
                                    <img class='profile-img-online' th:src='@{${map["pictureurl"]}}' alt='Profile Picture'>
                                    <span class='new-user'>${map["nickname"]}</span>
                                    <span class='new-message-counter'>0</span>
                                    </button >`;


                                usersContainer.insertAdjacentHTML("beforeend", user);
                                let userBtn = document.querySelector("." + map["nickname"]);

                                setUpOnlineUserBtn(userBtn, map["nickname"]);
                            }
                        });

                    });

                /*.then(message => {

                    message.forEach(onlineUser => {

                        console.log("DEBUG TRIGGER 1 EACH USER: " + onlineUser.nickname);
                        if (onlineUser.nickname !== userName) {

                            let user = `<button class='user-container ${onlineUser.nickname}' type='button'>
                                <img class='profile-img-online' src='/Images/ProfilePictures/defaultPic.jpg' alt='Profile Picture'>
                                <span class='new-user'>${onlineUser.nickname}</span>
                                <span class='new-message-counter'>0</span>
                                </button >`;



                            usersContainer.insertAdjacentHTML("beforeend", user);
                            let userBtn = document.querySelector("." + onlineUser.nickname);

                            setUpOnlineUserBtn(userBtn, onlineUser.nickname);

                        }
                    });
                });*/


            } else {
                console.log(" var 2 triggered num of users " + usersContainer.querySelectorAll("*").length)
                fetch("http://localhost:28852/users")
                    .then(response => response.json())
                    .then(data => {
                        let lastUser = data[data.length - 1];
                        /*for (let key in lastUser) {*/
                        /*if (lastUser["nickname"].hasOwnProperty(key)) {*/
                        if (lastUser["nickname"] !== userName) {
                            let user = `<button class='user-container ${lastUser["nickname"]}' type='button'>
                                    <img class='profile-img-online' th:src='@{${lastUser["pictureurl"]}}' alt='Profile Picture'>
                                    <span class='new-user'>${lastUser["nickname"]}</span>
                                    <span class='new-message-counter'>0</span>
                                    </button >`;

                            usersContainer.insertAdjacentHTML("beforeend", user);

                            let userBtn = document.querySelector("." + lastUser["nickname"]);

                            setUpOnlineUserBtn(userBtn, lastUser["nickname"]);
                        }

                    });

                /*.then(message => {
                    let newestUser = message[message.length - 1].nickname;
                    console.log("DEBUG NER USER: " + newestUser);
                    let user = `<button class='user-container ${newestUser}' type='button'>
                                <img class='profile-img-online' src='/Images/ProfilePictures/defaultPic.jpg' alt='Profile Picture'>
                                <span class='new-user'>${newestUser}</span>
                                <span class='new-message-counter'>0</span>
                                </button >`;

                    usersContainer.insertAdjacentHTML("beforeend", user);

                    let userBtn = document.querySelector("." + newestUser);

                    setUpOnlineUserBtn(userBtn, newestUser);

                });*/
            }
        }

    }
}

//connecting new user
function onConnectedSuccessfully() {

    let date = new Date().toLocaleString();
    stompClient.subscribe("/topic/chat", onMessageReceived);

    stompClient.send("/app/user", {}, JSON.stringify(
        {
            sender: userName,
            type: 'newUser',
            content: userName + ' just joined chatroom. Welcome!',
            sendTo: "public",
            date: date
        }));
}
//"Public chat" switch button
function switchToPublic() {

    if (chatWithElement.innerHTML !== "Public chat") {
        let activeBtn = document.querySelector("." + privateChatWith);
        chatWithElement.innerHTML = "Public chat";
        messages.innerHTML = "";


        if (activeBtn) {
            activeBtn.style.setProperty("Background-color", "#00000000");
        }
        getHistory();
        privateChatWith = "";
        publicBtn.disabled = true;
        publicBtn.style.setProperty("color", "darkgrey");
    }
}
// getting message history form server
function getHistory() {
    if (chatWithElement.innerHTML === "Public chat") {
        fetch("http://localhost:28852/history/public")
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
                    if (msg.type === "message") {
                        let history;
                        if (msg.sender === userName) {
                            history =
                                "<div class='new-message-container revert'><div class='new-sender'>"
                                + msg.sender + "</div>"
                                + "<div class='new-message right-msg'><div class='new-date'>" + msg.date + "</div>" + msg.content + "</div></div>";

                        } else {
                            history =
                                "<div class='new-message-container'><div class='new-sender'>"
                                + msg.sender + "</div>"
                                + "<div class='new-message left-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";
                        }
                        messageContainer.insertAdjacentHTML("beforeend", history);
                    }
                });
            });
    } else {
        fetch("http://localhost:28852/history/" + privateChatWith + "-" + userName)
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {
                    /*let history = "<div class='message-container'><div class='sender'>"
                        + msg.sender + "</div>"
                        + "<div class='message'>" + msg.content + "</div><div class='date'>"
                        + msg.date + "</div></div>";*/
                    let history;
                    if (msg.sender === userName) {
                        history =
                            "<div class='new-message-container revert'><div class='new-sender'>"
                            + msg.sender + "</div>"
                            + "<div class='new-message right-msg'><div class='new-date'>" + msg.date + "</div>" + msg.content + "</div></div>";

                    } else {
                        history =
                            "<div class='new-message-container'><div class='new-sender'>"
                            + msg.sender + "</div>"
                            + "<div class='new-message left-msg'><div class='date'>" + msg.date + "</div>" + msg.content + "</div></div>";
                    }
                    messageContainer.insertAdjacentHTML("beforeend", history);

                });
            });
    }


}
// online user button set up
function setUpOnlineUserBtn(btn, newUser) {
    btn.addEventListener("click", () => {
        if (chatWithElement.innerHTML !== "Public chat") {
            /* btn.style.setProperty("disabled", true);*/
            document.querySelector("." + privateChatWith).style.setProperty("Background-color", "#00000000");
        }
        btn.style.setProperty("Background-color", "#00000045");

        /*publicBtn.style.setProperty("Background-color", "#00000045") */
        publicBtn.disabled = false;
        publicBtn.style.setProperty("color", "#C6AC8E");
        messageContainer.innerHTML = "";
        if (btn.querySelector(".new-message-counter").innerHTML !== "0") {
            btn.querySelector(".new-message-counter").style.setProperty("visibility", "hidden");
            btn.querySelector(".new-message-counter").innerHTML = "0";
        }
        chatWithElement.innerHTML = "Private chat with: " + newUser;
        privateChatWith = newUser;
        getHistory();
    });
}


function kickUser() {
    var select = document.getElementById("select").value;
    let date = new Date().toLocaleString();
    stompClient.send("/app/chat", {}, JSON.stringify(
        {
            sender: 'admin',
            type: 'kick',
            content: select + ' was kicked out by admin!',
            sendTo: select,
            date: date
        }));
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

// Close the dropdown menu if the user clicks outside of it
window.onclick = function (event) {
    if (!event.target.matches('.dropbtn')) {
        var dropupMenu = document.getElementById("dropupMenu");

        if (dropupMenu.classList.contains('show')) {
            dropupMenu.classList.remove('show');
        }
    }
}

function openProfileSettings() {
    document.querySelector(".modal").style.display = "block";

}

/*drag and drop area setup*/

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
    /*dragAndDrop.textContent = `Selected file: ${file.name}`;*/
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

/*function sendUpdate() {
    fetch('http://localhost:28852/profileUpdate', {
        method: 'POST'
    }).then(response => {
        if (response.ok) {
            window.location.href = 'http://localhost:28852/';
            alert("updatet profile");
        }
    });
} */

document.getElementById("profile-update-form").addEventListener("submit", function (event) {
    event.preventDefault();

    const formData = new FormData(this);

    fetch("http://localhost:28852/profileUpdate", {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            //console.log("DEBUG: " + data();
            let updateMessage = `<div class="response">${data["message"]}</div>`;
            document.querySelector(".modal-content").insertAdjacentHTML("beforeend", updateMessage);
        });
});

let closeSpan = document.getElementById("closeBtn");
let modal = document.getElementsByClassName("modal")[0];
closeSpan.onclick = function () {
    modal.style.display = "none";
}