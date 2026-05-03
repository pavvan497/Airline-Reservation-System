const urlParams = new URLSearchParams(window.location.search);
const selectedFlight = JSON.parse(sessionStorage.getItem("selectedFlight") || "null");
const ticketMessage = document.getElementById("ticket-message");
const flightDetails = {
    flightName: urlParams.get("name") || selectedFlight?.flightName || "Selected Flight",
    flightRoute: urlParams.get("route") || selectedFlight?.flightRoute || "Route unavailable",
    flightTime: urlParams.get("time") || selectedFlight?.flightTime || "Schedule shared at confirmation",
    start: urlParams.get("start") || selectedFlight?.start || "",
    end: urlParams.get("end") || selectedFlight?.end || "",
    availableSeats: Number(urlParams.get("seats") || selectedFlight?.availableSeats || 0),
    km: Number(urlParams.get("km") || selectedFlight?.km || 0),
};

function showTicketMessage(message, color) {
    ticketMessage.textContent = message;
    ticketMessage.style.color = color;
}

function renderFlightDetails() {
    if (!flightDetails.start || !flightDetails.end) {
        showTicketMessage("No flight information is available. Please go back and choose a route again.", "#c0392b");
        return false;
    }

    document.getElementById("displayFlightName").textContent = flightDetails.flightName;
    document.getElementById("displayFlightRoute").textContent = flightDetails.flightRoute;
    document.getElementById("displayFlightTime").textContent = flightDetails.flightTime;
    document.getElementById("displayFlightSeats").textContent = `${flightDetails.availableSeats} seats available`;
    document.getElementById("displayFlightKm").textContent = `${flightDetails.km} km`;
    return true;
}

async function handleTicketSelection(event) {
    event.preventDefault();

    if (!renderFlightDetails()) {
        return;
    }

    const ticketCount = Number(document.getElementById("ticket-count").value);
    const ticketType = document.getElementById("ticket-type").value;

    if (!ticketCount || ticketCount < 1) {
        showTicketMessage("Please choose at least one ticket.", "#c0392b");
        return;
    }

    if (flightDetails.availableSeats && ticketCount > flightDetails.availableSeats) {
        showTicketMessage("The selected number of tickets is higher than the available seats.", "#c0392b");
        return;
    }

    const jwtToken = localStorage.getItem("jwtToken");
    if (!jwtToken) {
        window.location.href = "/login";
        return;
    }

    const bookingData = {
        bstart: flightDetails.start,
        bend: flightDetails.end,
        bnumofseat: ticketCount,
        flightName: flightDetails.flightName,
        flightRoute: flightDetails.flightRoute,
        flightTime: `${flightDetails.flightTime} | ${ticketType} class`,
        ticketType,
        availableSeats: flightDetails.availableSeats,
        km: flightDetails.km,
    };

    showTicketMessage("Calculating ticket price for the selected flight...", "#0d8b7a");

    try {
        const response = await fetch("/api/v1/demo-controller/checkPrice", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${jwtToken}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(bookingData),
        });

        if (!response.ok) {
            throw new Error("Unable to calculate the price for this flight.");
        }

        const price = await response.json();
        sessionStorage.setItem("bookingData", JSON.stringify({
            ...bookingData,
            price,
        }));
        window.location.href = "/payment";
    } catch (error) {
        showTicketMessage(error.message, "#c0392b");
    }
}

renderFlightDetails();
document.getElementById("ticket-selection-form").addEventListener("submit", handleTicketSelection);
