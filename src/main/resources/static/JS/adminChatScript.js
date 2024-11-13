
let onlineUsers = document.getElementById("online-users");
let bannedUsers = document.getElementById("banned-users")

var banDuration = () => document.getElementById("ban-duration").value;


function onMessageReceived(payload) {

    var message = JSON.parse(payload.body);
    var rawNick = CSS.escape(message.sender);

    if (message.type) {
        if (message.type === "Leave" || message.type === "kick") {
            prepareMessage(messageContainer, message);

            if (usersContainer.querySelector(`.${rawNick}`)) {
                usersContainer.querySelector(`.${rawNick}`).remove();
                removeUserFromSelect(message.sender, onlineUsers);
            }
        }

        if (message.type === "kick") {
            removeUserFromSelect(message.sendTo, onlineUsers);
        }


        if (message.type === "UNBAN") {
            fetch(`${serverURL}admin/unban`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body:
                    JSON.stringify({
                        'nickname': message.sendTo
                    })
                
            });

            removeUserFromSelect(message.sendTo, bannedUsers);

        }

        if (message.type === "BanExpired") {
            removeUserFromSelect(message.sendTo, bannedUsers);
        }

        if (message.type === "update-nick") {
            removeUserFromSelect(message.sender, onlineUsers);
            addUserToSelect(message.content, onlineUsers);

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
            name.classList.replace(`${message.sender}`, `${message.content}`);
            setProfilePicture(onUserbtn, message.content);

            if (chatWithElement.innerHTML !== "Public chat") {
                privateChatWith = message.content;
                chatWithElement.innerHTML = `Private chat with: ${message.content}`;
            }



            messageContainer.innerHTML = "";
            getLatestHistory();
        }


        if (message.type === "update-profilePic") {
            if (nickname !== message.sender) {
                let onUserbtn = document.querySelector(`.${rawNick}`);

                setProfilePicture(onUserbtn, message.sender);

            }
            prepareMessage(messageContainer, message);

        }



        // displaying newest message
        if (message.type === 'message') {
            let html;
            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === "public") {

                prepareMessage(messageContainer, message);

            }

            if (chatWithElement.innerHTML === "Public chat" && message.sendTo === nickname) {
                let incomingMsgUser = document.querySelector(`.${rawNick}`);
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


            if (message.sender != nickname) {
                addUserToSelect(message.sender, onlineUsers);
            }
            prepareMessage(messageContainer, message);

            fetch(`${serverURL}users`)
                .then(response => response.json())
                .then(data => {
                    showOnlineUsers(data);

                });

        }





    }
}

// admin features:

//Kick selected user from chat
function kickUser() {
    var select = document.getElementById("online-users").value;
    if (select != 0) {

        stompClient.send("/app/chat/public", {}, JSON.stringify(prepareAdminMessage(select, 'kick')));

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

        fetch(`${serverURL}admin/banned`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }, body: JSON.stringify({
                'nickname': select,
                'banDuration': banDuration()
            })

        }).then(response => {
            if (response.ok) {
                stompClient.send("/app/chat/public", {}, JSON.stringify(prepareAdminMessage(select, 'BAN')));
            }
        });


        addUserToSelect(select, bannedUsers);
        removeUserFromSelect(select, onlineUsers);
    };
}

function unBanUser() {
    var select = document.getElementById("banned-users").value;
    if (select != "0") {

        stompClient.send("/app/chat/public", {}, JSON.stringify(prepareAdminMessage(select, 'UNBAN')));

    }
}

function prepareAdminMessage(targetName, msgType) {
    let adminmsg;

    if (msgType == 'UNBAN') {

        adminmsg =

        {
            //sender: nickname,
            type: msgType,
            content: `${targetName} was set free by admin!`,
            //sendTo: targetName,
            recipient: targetName
            //date: actDate()
        }
    }
    if (msgType == 'BAN') {
        adminmsg =

        {
            // sender: nickname,
            type: msgType,
            content: banDuration(),
            recipient: targetName
            // sendTo: targetName,
            // date: actDate()
        }
    }

    if (msgType == 'kick') {
        adminmsg =

        {
            //sender: nickname,
            type: msgType,
            content: `${targetName} was kicked out by admin!`,
            recipient: targetName
            // sendTo: targetName,
            //date: actDate()
        }
    }
    return adminmsg;
}




