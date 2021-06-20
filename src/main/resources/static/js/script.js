// var welcomeForm = document.querySelector('#welcomeForm');
// var dialogueForm = document.querySelector('#dialogueForm');
// welcomeForm.addEventListener('submit', connect, true)

const connection = new signalR.HubConnectionBuilder()
    .withUrl("/hub")
    .configureLogging(signalR.LogLevel.Information)
    .build();

async function start() {
    try {
        await connection.start();
        console.log("SignalR Connected.");
    } catch (err) {
        console.log(err);
        setTimeout(start, 5000);
    }
};
connection.onclose(start);
start();

connection.on("sent", (user, message) => {
 console.log(message)
});

