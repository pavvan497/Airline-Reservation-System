const form = document.getElementById("flight-chooser-form");
const flightSelect = document.getElementById("flight-select");
const sourceSelect = document.getElementById("source-select");
const destinationSelect = document.getElementById("destination-select");
const validationMessage = document.getElementById("route-validation-message");
const selectedFlightCard = document.getElementById("selected-flight-card");
const selectedFlightName = document.getElementById("selected-flight-name");
const selectedFlightRoute = document.getElementById("selected-flight-route");
const selectedFlightTime = document.getElementById("selected-flight-time");

function getSelectedFlightOption() {
  if (!flightSelect || !flightSelect.value) {
    return null;
  }
  return flightSelect.options[flightSelect.selectedIndex];
}

function showValidationMessage(message) {
  if (validationMessage) {
    validationMessage.textContent = message;
  }
}

function renderSelectedFlight(option) {
  if (!selectedFlightCard || !selectedFlightName || !selectedFlightRoute || !selectedFlightTime) {
    return;
  }

  if (!option) {
    selectedFlightCard.classList.add("is-hidden");
    selectedFlightName.textContent = "Flight";
    selectedFlightRoute.textContent = "Route";
    selectedFlightTime.textContent = "Details";
    return;
  }

  const { start, end, seats, km } = option.dataset;
  selectedFlightName.textContent = option.textContent.trim().split(" - ")[0];
  selectedFlightRoute.textContent = `${start} to ${end}`;
  selectedFlightTime.textContent = `${seats} seats available | ${km} km`;
  selectedFlightCard.classList.remove("is-hidden");
}

function syncRouteFromFlight() {
  const option = getSelectedFlightOption();
  if (!option) {
    renderSelectedFlight(null);
    return;
  }

  sourceSelect.value = option.dataset.start || "";
  destinationSelect.value = option.dataset.end || "";
  showValidationMessage("");
  renderSelectedFlight(option);
}

function syncFlightFromRoute() {
  if (!flightSelect || !sourceSelect || !destinationSelect) {
    return;
  }

  const source = sourceSelect.value.trim().toLowerCase();
  const destination = destinationSelect.value.trim().toLowerCase();

  if (source && destination && source === destination) {
    flightSelect.value = "";
    renderSelectedFlight(null);
    showValidationMessage("Source and destination cannot be the same.");
    return;
  }

  showValidationMessage("");

  if (!source || !destination) {
    return;
  }

  const matchingOption = Array.from(flightSelect.options).find((option) => {
    return option.value
      && option.dataset.start?.trim().toLowerCase() === source
      && option.dataset.end?.trim().toLowerCase() === destination;
  });

  if (matchingOption) {
    flightSelect.value = matchingOption.value;
    renderSelectedFlight(matchingOption);
  } else {
    flightSelect.value = "";
    renderSelectedFlight(null);
    showValidationMessage("No registered flight is available for the selected route.");
  }
}

if (flightSelect) {
  flightSelect.addEventListener("change", syncRouteFromFlight);
}

if (sourceSelect) {
  sourceSelect.addEventListener("change", syncFlightFromRoute);
}

if (destinationSelect) {
  destinationSelect.addEventListener("change", syncFlightFromRoute);
}

form.addEventListener("submit", function(event) {
  event.preventDefault();

  const option = getSelectedFlightOption();
  const source = sourceSelect?.value?.trim();
  const destination = destinationSelect?.value?.trim();

  if (!option) {
    showValidationMessage("Please select a registered flight.");
    return;
  }

  if (source && destination && source.toLowerCase() === destination.toLowerCase()) {
    showValidationMessage("Source and destination cannot be the same.");
    return;
  }

  const flightName = option.textContent.trim().split(" - ")[0];
  const flightRoute = `${option.dataset.start} to ${option.dataset.end}`;
  const flightTime = `${option.dataset.seats} seats available | ${option.dataset.km} km`;
  const flightStart = option.dataset.start;
  const flightEnd = option.dataset.end;
  const flightSeats = option.dataset.seats;
  const flightKm = option.dataset.km;

  sessionStorage.setItem("selectedFlight", JSON.stringify({
    flightName,
    flightRoute,
    flightTime,
    start: flightStart,
    end: flightEnd,
    availableSeats: Number(flightSeats),
    km: Number(flightKm),
  }));

  const redirectUrl =
    `/ticket?name=${encodeURIComponent(flightName)}&route=${encodeURIComponent(flightRoute)}&time=${encodeURIComponent(flightTime)}&start=${encodeURIComponent(flightStart)}&end=${encodeURIComponent(flightEnd)}&seats=${encodeURIComponent(flightSeats)}&km=${encodeURIComponent(flightKm)}`;
  window.location.href = redirectUrl;
});
