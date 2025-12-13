// Desktop.js - Vanilla JavaScript for desktop.html

function openUpdateLincePlus() {
    fetch('/component/update', {
        method: 'GET'
    })
    .then(response => {
        console.log("opening updater");
    })
    .catch(error => {
        console.log(error);
    });
}

function updateStats() {
    fetch('/session/getProjectInfo', {
        method: 'GET'
    })
    .then(response => response.json())
    .then(data => {
        console.log("getting project info");
        console.log(data);

        document.getElementById("statScene").textContent = data.scenes;
        document.getElementById("statVideo").textContent = data.videos;
        document.getElementById("projectUri").textContent = data.url;
        document.getElementById("statObserver").textContent = data.observers;
        document.getElementById("projectInfo").classList.remove('hidden');

        if (data.i18n) {
            const ids = ["lang_qr", "lang_desc_1", "lang_desc_2", "option_registers", "option_observers"];
            ids.forEach(id => {
                const msg = data.i18n[id];
                console.log(msg);
                const element = document.getElementById(id);
                if (element) {
                    element.textContent = msg;
                }
            });
        }

        if (data.update) {
            const msg = "There is a newer version of Lince PLUS: " + data.version;
            document.getElementById("updateMessage").textContent = msg;
            document.getElementById("updateMessageWrapper").classList.remove('hidden');
        }

        if (data.information && data.information.userInformation) {
            let msg = "";
            const now = Date.now();
            data.information.userInformation.forEach(item => {
                let itemMsg = "<div>";
                itemMsg += `<b>${item.title}: </b><span>${item.description}</span>`;
                itemMsg += "</div>";
                msg += itemMsg;
            });
            if (msg !== "") {
                document.getElementById("informationMessage").innerHTML = msg;
                document.getElementById("informationMessageWrapper").classList.remove('hidden');
            }
        }

        // Refresh QR code to ensure it shows the latest URL (local or ngrok)
        refreshQRCode();
    })
    .catch(error => {
        console.log(error);
    });
}

function refreshQRCode() {
    // Add timestamp to prevent caching
    const timestamp = new Date().getTime();
    const qrImage = document.getElementById("qrCodeImage");
    if (qrImage) {
        qrImage.src = "/component/getQRCode?t=" + timestamp;
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Set current year
    document.getElementById('currentYear').textContent = new Date().getFullYear();

    // Initial stats update
    updateStats();

    // Update stats every minute
    setInterval(updateStats, 60000);
});