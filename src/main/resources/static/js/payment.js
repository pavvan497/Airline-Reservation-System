const bookingData = JSON.parse(sessionStorage.getItem("bookingData") || "null");
const paymentMessage = document.getElementById("payment-message");

function formatPrice(price) {
  return `INR ${Number(price).toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatTravelDate(dateValue) {
  if (!dateValue) {
    return "Not selected";
  }

  return new Date(`${dateValue}T00:00:00`).toLocaleDateString("en-IN", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function updateBookingDetails() {
  if (!bookingData) {
    paymentMessage.textContent = "No booking details were found. Please start again from the home page.";
    paymentMessage.style.color = "#c0392b";
    document.querySelector("#payment-form button[type='submit']").disabled = true;
    return;
  }

  document.getElementById("airplane-type").textContent = bookingData.flightName || "Available flight";
  document.getElementById("departure-time").textContent = bookingData.flightTime || "Assigned at confirmation";
  document.getElementById("travel-date").textContent = formatTravelDate(bookingData.travelDate);
  document.getElementById("start-place").textContent = bookingData.bstart;
  document.getElementById("destination").textContent = bookingData.bend;
  document.getElementById("ticket-count").textContent = bookingData.bnumofseat;
  document.getElementById("ticket-price").textContent = formatPrice(bookingData.price);
}

document.getElementById("payment-form").addEventListener("submit", async function (event) {
  event.preventDefault();

  const jwtToken = localStorage.getItem("jwtToken");
  if (!jwtToken) {
    window.location.href = "/login";
    return;
  }

  if (!bookingData) {
    paymentMessage.textContent = "Booking details are missing. Please try again.";
    paymentMessage.style.color = "#c0392b";
    return;
  }

  try {
    const response = await fetch("/api/v1/demo-controller/checkPrice/addBooking", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${jwtToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(bookingData),
    });

    const responseText = await response.text();
    let responseBody = {};
    try {
      responseBody = responseText ? JSON.parse(responseText) : {};
    } catch (parseError) {
      responseBody = { message: responseText || "Booking failed" };
    }
    if (!response.ok) {
      throw new Error(responseBody.message || "Booking failed");
    }

    sessionStorage.setItem("bookingConfirmation", JSON.stringify(responseBody));
    sessionStorage.removeItem("bookingData");
    sessionStorage.removeItem("selectedFlight");
    window.location.href = "/confirmation";
  } catch (error) {
    paymentMessage.textContent = error.message;
    paymentMessage.style.color = "#c0392b";
  }
});

updateBookingDetails();
