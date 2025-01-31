
const serverURL = document.getElementById("serverURL").getAttribute("data-URL");
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


  function back() {
        window.location.href = '/login';
  }


document.getElementById("pass-reset-form").addEventListener("submit", function () {
    event.preventDefault();
    let formData = new FormData(this);
    let resetResponse = document.querySelector(".reset_response");

    fetch(`${serverURL}passReset`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
                body: FormData
    })
        .then(response => {
            if (response.ok) {
                resetResponse.style.color = "#345635";
                resetResponse.textContent = `Link for reset your password was send to your email.`
            } else {
                resetResponse.textContent = `There is no registred user with this email!`
                resetResponse.style.color = "#7E102C";
            }
        });
    
    
})