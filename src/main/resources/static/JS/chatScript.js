let inputContainer = document.querySelector(".inputContainer");
let chatScreen = document.querySelector(".chat");
let msgInputWindow = document.getElementById("input-msg");
let messageContainer = document.getElementById("messages");
let chatWithElement = document.getElementById("chat-with");
let usersContainer = document.getElementById("users");
let publicBtn = document.getElementById("public-chat-btn");
let privateChatWith;

let stompClient = null;

register();

var userNameElement = document.getElementById("user-data");
var userName = userNameElement.getAttribute("data-user");
console.log(userName);


//event listener for deleting online user from list after closing chat page
window.addEventListener('beforeunload', function (event) {
    fetch('http://localhost:28852/logout', {
        method: 'POST'
    })
        .then(response => {
            console.log("user " + userName + " disconnected")
        })
        .catch(error => {
            console.error('Error while logout:', error);
        });
});


//event listener for sending msg by pressing Enter
msgInputWindow.addEventListener("keypress", (event) => {
    if (event.code === "Enter") {
        send();
    }
});


//defined functions
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
            let disconnectedUser = message.user;
            let disconnectedMsg = "<div class='message-container'> <div class='message'>" + message.content + "</div><div class='date'>" + date + "</div></div></div>";
            messageContainer.insertAdjacentHTML("beforeend", disconnectedMsg);
            if (usersContainer.querySelector("." + disconnectedUser)) {
                usersContainer.querySelector("." + disconnectedUser).remove();
            }
        }


        // displaying newest message
        if (message.type === 'message') {
            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {
                let html = "<div class='message-container'><div class='sender'>" + message.sender + "</div>"
                    + "<div class='message'>" + message.content + "</div><div class='date'>" + message.date + "</div></div>";
                messageContainer.insertAdjacentHTML("beforeend", html);
                msgInputWindow.value = "";
            }
            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === userName) {

                if (usersContainer.querySelector("." + message.sender).querySelector(".new-message-counter") == null) {
                    usersContainer.querySelector("." + message.sender).insertAdjacentHTML("beforeend", "<div class='new-message-counter'><b>0</b></div>");
                }
                let incomingMsgUser = document.querySelector("." + message.sender);
                let msgCounter = incomingMsgUser.querySelector(".new-message-counter");
                usersContainer.insertBefore(incomingMsgUser, usersContainer.firstChild);
                msgCounter.style.setProperty("visibility", "visible");
                let count = parseInt(msgCounter.innerText) + 1;
                msgCounter.innerText = count;
            }

            if ((userName == message.sendTo && message.sender == privateChatWith) ||
                (message.sendTo == privateChatWith && message.sender == userName)) {

                let html = "<div class='message-container'><div class='sender'>" + message.sender + "</div>"
                    + "<div class='message'>" + message.content + "</div><div class='date'>" + message.date + "</div></div>";
                messageContainer.insertAdjacentHTML("beforeend", html);
                msgInputWindow.value = "";
            }
        }

        if (message.type === 'newUser') {
            let welcomeMsg = "<div class='message-container'> <div class='message'>" + message.content + "</div><div class='date'>" + message.date + "</div></div></div>";
            messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);

            if (usersContainer.querySelectorAll("*").length === 0) {
                console.log(" var 1 triggered")
                fetch("http://localhost:28852/users")
                    .then(response => response.json())
                    .then(message => {

                        message.forEach(onlineUser => {
                        console.log("DEBUG TRIGGER 1 EACH USER: " + onlineUser.nickname);
                            if (onlineUser.nickname !== userName) {
                                let user = "<button class='user-container " + onlineUser.nickname + "' type='button'><div class='user'>" + onlineUser.nickname + "</div></button>"

                                usersContainer.insertAdjacentHTML("beforeend", user);
                                let userBtn = document.querySelector("." + onlineUser.nickname);

                                setUpOnlineUserBtn(userBtn,onlineUser.nickname);

                            }
                        });
                    });


            } else {
                console.log(" var 2 triggered")
                fetch("http://localhost:28852/users")
                    .then(response => response.json())
                    .then(message => {
                        let newestUser = message[message.length - 1].nickname;
                        console.log("DEBUG NER USER: " + newestUser);
                        let user = "<button class='user-container " + newestUser + "' type='button'><div class='user'>" + newestUser + "</div></button>"
                        usersContainer.insertAdjacentHTML("beforeend", user);

                        let userBtn = document.querySelector("." + newestUser);

                        setUpOnlineUserBtn(userBtn,newestUser);

                    });
            }
        }

    }
}


        //connecting new user
        function onConnectedSuccessfully() {
            //console.log("connected");
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
                publicBtn.style.setProperty("background-color", "blue");
                if (activeBtn) {
                    activeBtn.style.setProperty("Background-color", "Azure");
                }
                getHistory();
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
                                let welcomeMsg = "<div class='message-container'><div class='message'> " + msg.content + "</div><div class='date'>" + msg.date + "</div></div></div>";
                                messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);

                            } else {
                                let history =
                                    "<div class='message-container'><div class='sender'>"
                                    + msg.sender + "</div>"
                                    + "<div class='message'>" + msg.content + "</div><div class='date'>"
                                    + msg.date + "</div></div>";

                                messageContainer.insertAdjacentHTML("beforeend", history);
                            }
                        });
                    });
            } else {
                fetch("http://localhost:28852/history/" + privateChatWith + "-" + userName)
                    .then(response => response.json())
                    .then(message => {
                        message.forEach((msg) => {
                            let history = "<div class='message-container'><div class='sender'>"
                                + msg.sender + "</div>"
                                + "<div class='message'>" + msg.content + "</div><div class='date'>"
                                + msg.date + "</div></div>";
                            messageContainer.insertAdjacentHTML("beforeend", history);

                        });
                    });
            }


        }
// online user button set up
        function setUpOnlineUserBtn(btn,newUser) {
         btn.addEventListener("click", () => {
                                    if (chatWithElement.innerHTML !== "Public chat") {
                                        document.querySelector("." + privateChatWith).style.setProperty("Background-color", "Azure");
                                    }
                                    btn.style.setProperty("Background-color", "blue");
                                    publicBtn.style.setProperty("Background-color", "grey")
                                    messageContainer.innerHTML = "";
                                    if (btn.querySelector(".new-message-counter") !== null) {
                                        btn.querySelector(".new-message-counter").remove();
                                    }
                                    chatWithElement.innerHTML = "Private chat with: " + newUser;
                                    privateChatWith = newUser;
                                    getHistory();
                                });
        }
    


