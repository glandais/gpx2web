document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('virtualizationForm');
    const submitBtn = document.getElementById('submitBtn');
    const spinner = document.getElementById('spinner');
    const progressSection = document.getElementById('progressSection');
    const errorSection = document.getElementById('errorSection');
    const errorMessage = document.getElementById('errorMessage');

    // Set default start time to current time
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    document.getElementById('startTime').value = now.toISOString().slice(0, 16);

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const gpxFile = document.getElementById('gpxFile').files[0];
        if (!gpxFile) {
            showError('Please select a GPX file');
            return;
        }

        // Show progress and disable submit
        showProgress();
        
        // Prepare form data
        const formData = new FormData();
        formData.append('gpxFile', gpxFile);
        
        // Prepare parameters object
        const parameters = {
            startTime: new Date(document.getElementById('startTime').value).toISOString(),
            cyclist: {
                weightKg: parseFloat(document.getElementById('weightKg').value),
                powerWatts: parseFloat(document.getElementById('powerWatts').value),
                harmonics: document.getElementById('harmonics').checked,
                maxBrakeG: parseFloat(document.getElementById('maxBrakeG').value),
                dragCoefficient: parseFloat(document.getElementById('dragCoefficient').value),
                frontalAreaM2: parseFloat(document.getElementById('frontalAreaM2').value),
                maxAngleDeg: parseFloat(document.getElementById('maxAngleDeg').value),
                maxSpeedKmH: parseFloat(document.getElementById('maxSpeedKmH').value)
            },
            bike: {
                rollingResistance: parseFloat(document.getElementById('rollingResistance').value),
                frontWheelInertia: parseFloat(document.getElementById('frontWheelInertia').value),
                rearWheelInertia: parseFloat(document.getElementById('rearWheelInertia').value),
                wheelRadiusM: parseFloat(document.getElementById('wheelRadiusM').value),
                efficiency: parseFloat(document.getElementById('efficiency').value)
            },
            wind: {
                speedMs: parseFloat(document.getElementById('windSpeedMs').value),
                directionDeg: parseFloat(document.getElementById('windDirectionDeg').value)
            }
        };
        
        formData.append('parameters', new Blob([JSON.stringify(parameters)], {
            type: 'application/json'
        }));

        // Submit to API
        fetch('/api/virtualize', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Server error occurred');
                });
            }
            return response.json();
        })
        .then(result => {
            hideProgress();
            
            // Store session data for visualization
            sessionStorage.setItem('virtualizationData', result.jsonData);
            sessionStorage.setItem('gpxContent', result.gpxContent);
            
            // Redirect to visualization page
            window.location.href = '/visualization';
        })
        .catch(error => {
            console.error('Error:', error);
            showError(error.message);
        });
    });

    function showProgress() {
        submitBtn.disabled = true;
        spinner.classList.remove('d-none');
        progressSection.classList.remove('d-none');
        errorSection.classList.add('d-none');
    }

    function hideProgress() {
        submitBtn.disabled = false;
        spinner.classList.add('d-none');
        progressSection.classList.add('d-none');
    }

    function showError(message) {
        hideProgress();
        errorMessage.textContent = message;
        errorSection.classList.remove('d-none');
    }
});