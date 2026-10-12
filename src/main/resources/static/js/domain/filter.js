/**
 * UTEGear Catalog Filter & Price Sliders Module
 */
window.UTEGear = window.UTEGear || {};

UTEGear.Filter = {
    init() {
        const minRange = document.getElementById("minPriceRange");
        const maxRange = document.getElementById("maxPriceRange");
        const minInput = document.getElementById("minPriceInput");
        const maxInput = document.getElementById("maxPriceInput");

        if (!minRange || !maxRange || !minInput || !maxInput) return;

        minRange.addEventListener("input", () => {
            let min = Number(minRange.value);
            let max = Number(maxRange.value);
            if (min > max) { min = max; minRange.value = min; }
            minInput.value = min;
        });

        maxRange.addEventListener("input", () => {
            let min = Number(minRange.value);
            let max = Number(maxRange.value);
            if (max < min) { max = min; maxRange.value = max; }
            maxInput.value = max;
        });
    },

    toggleMobileDrawer() {
        const sidebar = document.getElementById("filterSidebar");
        const icon = document.getElementById("filterToggleIcon");
        if (!sidebar) return;
        sidebar.classList.toggle("show");
        if (icon) {
            icon.className = sidebar.classList.contains("show") ? "bi bi-chevron-up ms-auto" : "bi bi-chevron-down ms-auto";
        }
    }
};

window.toggleMobileFilter = function () {
    UTEGear.Filter.toggleMobileDrawer();
};
