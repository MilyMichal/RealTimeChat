const serverURL = document.getElementById("serverURL").getAttribute("data-URL");
const token = document.getElementById("token").value;
const nick = document.getElementById("nick").value;
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


document.getElementById("pass-reset-form").addEventListener("submit", function () {
    event.preventDefault();
    let formData = new FormData(this);
    let resetResponse = document.querySelector(".reset_response");

    fetch(`${serverURL}passReset/${token}/${nick}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        body: formData
    })
        .then(response => {
            if (response.status == 202) {
                resetResponse.style.color = "#345635";

                  setTimeout(function() {
                        window.location.href = '/chat';
                    }, 3500);

            } else {

                resetResponse.style.color = "#7E102C";
            }
            return response.text();


        })
        .then(data => {
            resetResponse.textContent = data;
        });

})

