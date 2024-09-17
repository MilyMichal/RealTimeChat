
let onlineUsers = document.getElementById("online-users");
let bannedUsers = document.getElementById("banned-users")

var banDuration = () => document.getElementById("ban-duration").value;


function onMessageReceived(payload) {

    var message = JSON.parse(payload.body);

    /*console.log("\n\n ! DEBUG ! MESSAGE TYPE:\n" + message.type);*/
    if (message.type) {
        if (message.type === "Leave") {
            prepareMessage(messageContainer, message);
            //messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
            if (usersContainer.querySelector(`.${message.sender}`)) {
                usersContainer.querySelector(`.${message.sender}`).remove();
            }
        }

        if (message.type === "kick") {
            removeUserFromSelect(message.sendTo, onlineUsers);
        }


        if (message.type === "UNBAN") {
            fetch(`${serverURL}admin/unban/${message.sendTo}`, {
                method: 'PUT'
            });
            removeUserFromSelect(message.sendTo, bannedUsers);

        }

        if (message.type === "BanExpired") {
            removeUserFromSelect(message.sendTo, bannedUsers);
        }

        if (message.type === "update-name") {

            if (userName === message.sender) {

                //  if (message.sender !== message.content) {
                userName = message.content;

                // }
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
            prepareMessage(messageContainer, message);
            // messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
        }



        // displaying newest message
        if (message.type === 'message') {
            let html;
            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {

                prepareMessage(messageContainer, message);
                /* messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
                 msgInputWindow.value = "";*/
            }

            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === userName) {
                let incomingMsgUser = document.querySelector(`.${message.sender}`);
                let msgCounterContainer = incomingMsgUser.querySelector(".message-counter-container");
                let msgCounter = incomingMsgUser.querySelector(".message-counter");


                usersContainer.insertBefore(incomingMsgUser, usersContainer.firstChild);


                let count = parseInt(msgCounter.innerHTML) + 1;
                msgCounter.innerText = count;
                msgCounterContainer.style.setProperty("visibility", "visible");

            }

            if ((userName == message.sendTo && message.sender == privateChatWith) ||
                (message.sendTo == privateChatWith && message.sender == userName)) {
                prepareMessage(messageContainer, message);
                /* messageContainer.insertAdjacentHTML("beforeend", prepareMessage(message));
                 msgInputWindow.value = "";*/

            }
            clearOldMsg();
            if (isScrolledToBottom) {
                messageContainer.scrollTop = messageContainer.scrollHeight - messageContainer.clientHeight;
            }
        }

        // displaying new user in chat and online user panel
        if (message.type === 'newUser') {
            /* let welcomeMsg = `<div class='event-message-container'><div class='event-message login-event'> ${message.content}</div></div>`;
             messageContainer.insertAdjacentHTML("beforeend", welcomeMsg);*/

            if (message.sender != userName) {
                addUserToSelect(message.sender, onlineUsers);
            }
            prepareMessage(messageContainer, message);
            /* if (usersContainer.querySelectorAll("*").length === 0) {*/
            /*console.log(" var 1 triggered num of users " + usersContainer.querySelectorAll("*").length);*/
            fetch(`${serverURL}users`)
                .then(response => response.json())
                .then(data => {
                    showOnlineUsers(data);
                    /* data.forEach(onlineUser => {
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
}





    // admin features:

    //Kick selected user from chat
    function kickUser() {
        var select = document.getElementById("online-users").value;
        if (select != 0) {

            stompClient.send("/app/chat", {}, JSON.stringify(prepareAdminMessage(select, 'kick')));

            removeUserFromSelect(select, onlineUsers);
        }

    }

    // remove kicked user from option list
    function removeUserFromSelect(user, options) {

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

            stompClient.send("/app/chat", {}, JSON.stringify(prepareAdminMessage(select, 'BAN')));

            addUserToSelect(select, bannedUsers);
            removeUserFromSelect(select, onlineUsers);
        };
    }

    function unBanUser() {
        var select = document.getElementById("banned-users").value;
        if (select != "0") {

            stompClient.send("/app/chat", {}, JSON.stringify(prepareAdminMessage(select, 'UNBAN')));

        }
    }

    function prepareAdminMessage(targetName, msgType) {
        let adminmsg;

        if (msgType == 'UNBAN') {

            adminmsg =

            {
                sender: 'admin',
                type: msgType,
                content: `${targetName} was set free by admin!`,
                sendTo: targetName,
                date: actDate()
            }
        }
        if (msgType == 'BAN') {
            adminmsg =

            {
                sender: 'admin',
                type: msgType,
                content: banDuration(),
                sendTo: targetName,
                date: actDate()
            }
        }

        if (msgType == 'kick') {
            adminmsg =

            {
                sender: 'admin',
                type: msgType,
                content: `${targetName} was kicked out by admin!`,
                sendTo: targetName,
                date: actDate()
            }
        }
        return adminmsg;
    }




