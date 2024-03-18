//used elements stored in variables
//let userNameInput = document.getElementById("input-username");
//let loginScreen = document.querySelector(".loginScreen");
let inputContainer = document.querySelector(".inputContainer");
let chatScreen = document.querySelector(".chat");
let msgInputWindow = document.getElementById("input-msg");
let messageContainer = document.getElementById("messages");
let chatWithElement = document.getElementById("chat-with");
let usersContainer = document.getElementById("users");
let publicBtn = document.getElementById("public-chat-btn");


let stompClient = null;
register();
var userNameElement = document.getElementById("user-data");
var userName = userNameElement.getAttribute("data-user");
console.log(userName);

/*userNameInput.focus();*/

// event listener for register user name by pressing Enter
/*userNameInput.addEventListener("keypress", (event) => {
    if (event.code == "Enter" && userNameInput.value !== "") {
        register();
    }
});*/


//event listener for sending msg by pressing Enter
msgInputWindow.addEventListener("keypress", (event) => {
    if (event.code === "Enter") {
        send();
    }
});



//defined functions

function register() {
    /*if (userNameInput.value !== "") {*/
        //userName = "pokus";/*userNameInput.value;*/
        // establishing connection
        let sock = new SockJS("http://localhost:28852/chat");
        stompClient = Stomp.over(sock);
        stompClient.connect({}, onConnectedSuccessfully, (error) => {
            console.log('unable to connect' + error);
        });

        // loading chat history for new user
        getHistory();


        // switching login and chat screen
        /*chatWithElement.style.setProperty("visibility", "visible");
        loginScreen.style.setProperty("visibility", "hidden");
        chatScreen.style.setProperty("visibility", "visible");
        inputContainer.style.setProperty("visibility", "visible");
        msgInputWindow.focus();*/
    /*}*/
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
                "sendTo": chatWithElement.innerHTML
            }

        }
        stompClient.send("/app/chat", {}, JSON.stringify(finalMsg));
        let activeUser = usersContainer.querySelector("." + chatWithElement.innerHTML);
        usersContainer.insertBefore(activeUser, usersContainer.firstChild);
    }
}

//function for getting messages and online users from server
function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    console.log("DEBUG" + message);
    if (message.type === "Leave") {
        let disconnectedUser = message.user;
        if (usersContainer.querySelector("." + disconnectedUser)) {

            usersContainer.querySelector("." + disconnectedUser).remove();
        }

    } else {
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

            if ((userName == message.sendTo && message.sender == chatWithElement.innerHTML) ||
                (message.sendTo == chatWithElement.innerHTML && message.sender == userName)) {

                let html = "<div class='message-container'><div class='sender'>" + message.sender + "</div>"
                    + "<div class='message'>" + message.content + "</div><div class='date'>" + message.date + "</div></div>";
                messageContainer.insertAdjacentHTML("beforeend", html);
                msgInputWindow.value = "";
            }
        } else {
            // adding incoming user to online users list
            if (usersContainer.querySelectorAll("*").length === 0) {
                console.log(" var 1 triggered")

                message.forEach(onlineUser => {
                    console.log(onlineUser.nickname);
                    if (onlineUser.nickname !== userName) { // username
                        let user = "<button class='user-container " + onlineUser.nickname + "' type='button'><div class='user'>" + onlineUser.nickname + "</div></button>"

                        usersContainer.insertAdjacentHTML("beforeend", user);
                        let userBtn = document.querySelector("." + onlineUser.nickname);

                        userBtn.addEventListener("click", () => {
                            if (chatWithElement.innerHTML !== "Public chat") {
                                document.querySelector("." + chatWithElement.innerHTML).style.setProperty("Background-color", "Azure");
                            }
                            userBtn.style.setProperty("Background-color", "blue");
                            publicBtn.style.setProperty("Background-color", "grey")
                            messageContainer.innerHTML = "";
                            if (userBtn.querySelector(".new-message-counter") !== null) {
                                userBtn.querySelector(".new-message-counter").remove();
                            }
                            chatWithElement.innerHTML = onlineUser.nickname;
                            getHistory();
                        });

                    }
                });
            } else {
                console.log(" var 2 triggered")
                let newestUser = message[message.length - 1];
                console.log(newestUser);
                let user = "<button class='user-container " + newestUser + "' type='button'><div class='user'>" + newestUser + "</div></button>"
                usersContainer.insertAdjacentHTML("beforeend", user);

                let userBtn = document.querySelector("." + newestUser);
                userBtn.addEventListener("click", () => {
                    if (chatWithElement.innerHTML !== "Public chat") {
                        document.querySelector("." + chatWithElement.innerHTML).style.setProperty("Background-color", "Azure");
                    }
                    userBtn.style.setProperty("Background-color", "blue");
                    publicBtn.style.setProperty("Background-color", "grey")
                    messageContainer.innerHTML = "";
                    if (userBtn.querySelector(".new-message-counter") !== null) {
                        userBtn.querySelector(".new-message-counter").remove();
                    }
                    chatWithElement.innerHTML = newestUser;
                    getHistory();
                });
            }

        }
    }
}



//function for connecting new user
function onConnectedSuccessfully() {
console.log("connected");
    stompClient.subscribe("/topic/chat", onMessageReceived);

    stompClient.send("/app/user", {}, JSON.stringify(
        {
            sender: userName,
            type: 'newUser'
        }));
}

function switchToPublic() {

    if (chatWithElement.innerHTML !== "Public chat") {
        let activeBtn = document.querySelector("." + chatWithElement.innerHTML);
        chatWithElement.innerHTML = "Public chat";
        messages.innerHTML = "";
        publicBtn.style.setProperty("background-color", "blue");
        if (activeBtn) {
            activeBtn.style.setProperty("Background-color", "Azure");
        }
        getHistory();
    }
}

function getHistory() {

    if (chatWithElement.innerHTML === "Public chat") {
        fetch("http://localhost:28852/history")
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {
                    if (msg.sendTo === "public") {
                        let history = "<div class='message-container'><div class='sender'>"
                            + msg.sender + "</div>"
                            + "<div class='message'>" + msg.content + "</div><div class='date'>"
                            + msg.date + "</div></div>";

                        messageContainer.insertAdjacentHTML("beforeend", history);
                    }
                });
            });
    } else {
        fetch("http://localhost:28852/history")
            .then(response => response.json())
            .then(message => {
                message.forEach((msg) => {
                    if ((msg.sendTo == userName && msg.sender == chatWithElement.innerHTML) || (msg.sendTo == chatWithElement.innerHTML && msg.sender == userName)) {
                        let history = "<div class='message-container'><div class='sender'>"
                            + msg.sender + "</div>"
                            + "<div class='message'>" + msg.content + "</div><div class='date'>"
                            + msg.date + "</div></div>";

                        messageContainer.insertAdjacentHTML("beforeend", history);
                    }
                });
            });
    }
}



