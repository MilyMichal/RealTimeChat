
let onlineUsers = document.getElementById("online-users");
let bannedUsers = document.getElementById("banned-users")


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
            removeUserFromSelect(message.sendTo, onlineUsers);
        }


        if (message.type === "UNBAN") {
            fetch("http://localhost:28852/admin/unban/" + message.sendTo, {
                method: 'PUT'
            });
            removeUserFromSelect(message.sendTo, bannedUsers);

        }

        if (message.type === "BanExpired") {
            removeUserFromSelect(message.sendTo, bannedUsers);
        }

        if (message.type === "BAN") {
            
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
                let name = onUserbtn.querySelector(`.new-user`);
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

            }

            messageContainer.innerHTML = "";
            getLastestHistory();
            removeUserFromSelect(message.sender, onlineUsers);
            addUserToSelect(message.content, onlineUsers);

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
            clearOldMsg();
            if (isScrolledToBottom) {
                messageContainer.scrollTop = messageContainer.scrollHeight - messageContainer.clientHeight;
            }
        }

        // displaying new user in chat and online user panel
        if (message.type === 'newUser') {
            let welcomeMsg = "<div class='event-message-container'> <div class='event-message login-event'>" + message.content + "</div></div>";
            messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);

            if (message.sender != userName) {
                addUserToSelect(message.sender, onlineUsers);
            }

            if (usersContainer.querySelectorAll("*").length === 0) {
                console.log(" var 1 triggered num of users " + usersContainer.querySelectorAll("*").length);
                fetch("http://localhost:28852/users")
                    .then(response => response.json())
                    .then(data => {

                        data.forEach(onlineUser => {

                            if (onlineUser.nickname !== userName) {


                                let user = `<button class='user-container ${onlineUser.nickname}' type='button'>
                                    <img class='profile-img-online' src= '' alt='Profile Picture'>
                                    <span class='new-user'>${onlineUser.nickname}</span>
                                    <span class='new-message-counter'>0</span>
                                    </button >`;

                                usersContainer.insertAdjacentHTML("beforeend", user);
                                let userBtn = document.querySelector("." + onlineUser.nickname);

                                setUpOnlineUserBtn(userBtn);
                                setProfilePicture(userBtn, onlineUser.nickname);
                                /*fetch("http://localhost:28852/profile/get/" + onlineUser.nickname)
                                    .then(response => {
                                        if (response.ok) {
                                            return response.blob();
                                        } else {
                                            throw new Error('Image not found');
                                        }
                                    })
                                    .then(blob => {
                                        const imageUrl = URL.createObjectURL(blob);
                                        const imgElement = userBtn.querySelector('.profile-img-online');
                                        imgElement.src = imageUrl;
                                    })
                                    .catch(error => {
                                        console.error('Error fetching image:', error);
                                    });
                                    */
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
                                    <span class='new-user'>${lastUser.nickname}</span>
                                    <span class='new-message-counter'>0</span>
                                    </button >`;

                            usersContainer.insertAdjacentHTML("beforeend", user);
                            let userBtn = document.querySelector("." + lastUser.nickname);

                            setUpOnlineUserBtn(userBtn);
                            setProfilePicture(userBtn, lastUser.nickname);

                            /* fetch("http://localhost:28852/profile/get/" + lastUser.nickname)
                                 .then(response => {
                                     if (response.ok) {
                                         return response.blob();
                                     } else {
                                         throw new Error('Image not found');
                                     }
                                 })
                                 .then(blob => {
                                     const imageUrl = URL.createObjectURL(blob);
                                     const imgElement = userBtn.querySelector('.profile-img-online');
                                     imgElement.src = imageUrl;
                                 })
                                 .catch(error => {
                                     console.error('Error fetching image:', error);
                                 });*/

                        }

                    });


            }
        }

    }

}


// admin features:

//Kick selected user from chat
function kickUser() {

    if (select != "0") {
        var select = document.getElementById("online-users").value;
        let date = new Date().toLocaleString();
        stompClient.send("/app/chat", {}, JSON.stringify(
            {
                sender: 'admin',
                type: 'kick',
                content: select + ' was kicked out by admin!',
                sendTo: select,
                date: date
            }));
    };
}

// remove kicked user from option list
function removeUserFromSelect(user, options) {
    /*var options = select.options;*/
    for (var i = 0; i < options.length; i++) {
        if (options[i].value === user) {
            options.remove(i);
            break;
        }
    }
}

// add incoming user to option list
function addUserToSelect(user, options) {
    var newOption = document.createElement("option");
    newOption.text = user;
    newOption.value = user;
    options.add(newOption);
}

// BAN selected user
function banUser() {
    var select = document.getElementById("online-users").value;
    if (select != "0") {
        let date = new Date().toLocaleString();
        /*date.setMinutes(date.getMinutes() + 1);
        let banExp = date.toLocaleString();
*/
        stompClient.send("/app/chat", {}, JSON.stringify(
            {
                sender: 'admin',
                type: 'BAN',
                content: select + ' was BANNED by admin!',
                sendTo: select,
                date: date
            }));
        addUserToSelect(select, bannedUsers);
        removeUserFromSelect(select, onlineUsers);
    };
}

function unBanUser() {
    var select = document.getElementById("banned-users").value;
    if (select != "0") {
        let date = new Date().toLocaleString();
        stompClient.send("/app/chat", {}, JSON.stringify(
            {
                sender: 'admin',
                type: 'UNBAN',
                content: select + ' was set free by admin!',
                sendTo: select,
                date: date
            }));
    };
}





