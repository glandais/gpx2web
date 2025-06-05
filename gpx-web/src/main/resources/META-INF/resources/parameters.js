document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('parametersForm');
    const backBtn = document.getElementById('backBtn');

    // Check if we have analysis data
    const analysisData = sessionStorage.getItem('gpxAnalysis');
    
    if (!analysisData) {
        // Redirect to upload if no analysis data
        window.location.href = '/';
        return;
    }

    // Display analysis info
    const analysis = JSON.parse(analysisData);
    document.getElementById('totalDistance').textContent = (analysis.totalDistanceMeters / 1000).toFixed(2);
    document.getElementById('totalPoints').textContent = analysis.totalPoints.toLocaleString();

    // Load saved parameters if any
    loadSavedParameters();

    // Form submission
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        saveParametersAndContinue();
    });

    // Back button
    backBtn.addEventListener('click', function() {
        window.location.href = '/';
    });

    function loadSavedParameters() {
        const savedParams = sessionStorage.getItem('parametersData');
        if (savedParams) {
            const params = JSON.parse(savedParams);
            
            // Populate form fields
            Object.keys(params).forEach(key => {
                const element = document.getElementById(key);
                if (element) {
                    if (element.type === 'checkbox') {
                        element.checked = params[key];
                    } else if (element.type === 'datetime-local') {
                        const date = new Date();
                        date.setTime(Date.parse(params[key]));
                        date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
                        const iso = date.toISOString();
                        element.value = iso.slice(0, 16);
                    } else {
                        element.value = params[key];
                    }
                }
            });
        } else {
            // Set default start time to current time
            const now = new Date();
            now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
            document.getElementById('startTime').value = now.toISOString().slice(0, 16);
        }
    }

    function saveParametersAndContinue() {
        // Collect all form data
        const formData = new FormData(form);
        const parameters = {};
        
        // Convert FormData to object
        for (let [key, value] of formData.entries()) {
            const element = document.getElementById(key);
            if (element) {
                if (element.type === 'checkbox') {
                    parameters[key] = element.checked;
                } else if (element.type === 'datetime-local') {
                    parameters[key] = new Date(value).toISOString();
                } else if (element.type === 'number') {
                    parameters[key] = parseFloat(value);
                } else {
                    parameters[key] = value;
                }
            }
        }

        // Handle unchecked checkboxes (they don't appear in FormData)
        const checkboxes = form.querySelectorAll('input[type="checkbox"]');
        checkboxes.forEach(checkbox => {
            if (!formData.has(checkbox.name)) {
                parameters[checkbox.name] = false;
            }
        });

        // Save parameters to sessionStorage
        sessionStorage.setItem('parametersData', JSON.stringify(parameters));

        // Redirect to power curve page
        window.location.href = '/powercurve';
    }
});