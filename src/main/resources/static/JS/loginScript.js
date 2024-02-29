let userNameInput = document.getElementById("input-username");
let loginScreen = document.querySelector(".loginScreen");
let chatScreen = document.querySelector(".chat");
let msgInputWindow = document.getElementById("input-msg");

userNameInput.addEventListener("keypress",(event) => {
    if (event.code == "Enter" && userNameInput.value !=="") {
        register();
    }
});
var userName;

function register() {
if (userNameInput.value !== "") {
userName = userNameInput.value;
loginScreen.style.setProperty("visibility","hidden");
chatScreen.style.setProperty("visibility","visible")
msgInputWindow.focus();
    }
}
