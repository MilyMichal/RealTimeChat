
const serverURL = document.getElementById("serverURL").getAttribute("data-URL");
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


function back() {
    window.location.href = '/login';
}


document.getElementById("reset-request-form").addEventListener("submit", function () {
    event.preventDefault();
    let formData = new FormData(this);
    let resetResponse = document.querySelector(".reset_response");
    let resetBtn = document.querySelector(".resetBtn");

    fetch(`${serverURL}passReset`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        body: formData
    })
        .then(response => {
            if (response.status == 200) {
                resetResponse.style.color = "#345635";
                resetBtn.disabled = true;
            } else {

                resetResponse.style.color = "#7E102C";
            }
            return response.text();


        })
        .then(data => {
            resetResponse.textContent = data;
        });


})