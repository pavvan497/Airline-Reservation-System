(function () {
  const userRole = localStorage.getItem("userRole");
  if (userRole !== "ROLE_ADMIN") {
    window.location.href = "/login";
    return;
  }

  const currentUserEmail = localStorage.getItem("currentUserEmail") || "admin@arms.com";
  const displayName = currentUserEmail.split("@")[0];

  document.querySelectorAll("[data-admin-email]").forEach((node) => {
    node.textContent = currentUserEmail;
  });

  document.querySelectorAll("[data-admin-name]").forEach((node) => {
    node.textContent = displayName;
  });

  const activeSection = document.body.dataset.adminSection;
  if (activeSection) {
    document.querySelectorAll("[data-nav]").forEach((link) => {
      if (link.dataset.nav === activeSection) {
        link.classList.add("is-active");
      }
    });
  }

  window.logoutAdmin = function logoutAdmin() {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("userRole");
    localStorage.removeItem("currentUserEmail");
    sessionStorage.removeItem("bookingData");
    sessionStorage.removeItem("selectedFlight");
    sessionStorage.removeItem("bookingConfirmation");
    window.location.href = "/login";
  };
})();
