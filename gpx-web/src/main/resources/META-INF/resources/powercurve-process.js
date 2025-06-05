// Process virtualization and redirect to results
async function processVirtualization() {
    try {
        showVirtualizationProgress();
        
        // Get stored data
        const gpxFileData = sessionStorage.getItem('gpxFileData');
        const parametersData = sessionStorage.getItem('parametersData');
        const powerCurveData = sessionStorage.getItem('powerCurveData');
        
        if (!gpxFileData || !parametersData || !powerCurveData) {
            throw new Error('Missing required data. Please start over.');
        }
        
        // Convert data URL back to file
        const response = await fetch(gpxFileData);
        const blob = await response.blob();
        const fileInfo = JSON.parse(sessionStorage.getItem('selectedGpxFile'));
        const file = new File([blob], fileInfo.name, { type: 'application/gpx+xml' });
        
        // Prepare form data
        const formData = new FormData();
        formData.append('gpxFile', file);
        
        // Prepare parameters object
        const parameters = JSON.parse(parametersData);
        const powerCurve = JSON.parse(powerCurveData);
        
        const requestData = {
            startTime: parameters.startTime,
            cyclist: {
                weightKg: parameters.weightKg,
                powerWatts: 250, // Dummy value, not used with power curve
                harmonics: parameters.harmonics,
                maxBrakeG: parameters.maxBrakeG,
                dragCoefficient: parameters.dragCoefficient,
                frontalAreaM2: parameters.frontalAreaM2,
                maxAngleDeg: parameters.maxAngleDeg,
                maxSpeedKmH: parameters.maxSpeedKmH
            },
            bike: {
                rollingResistance: parameters.rollingResistance,
                frontWheelInertia: parameters.frontWheelInertia,
                rearWheelInertia: parameters.rearWheelInertia,
                wheelRadiusM: parameters.wheelRadiusM,
                efficiency: parameters.efficiency
            },
            wind: {
                speedMs: parameters.windSpeedMs,
                directionDeg: parameters.windDirectionDeg
            },
            powerCurve: powerCurve
        };
        
        formData.append('parameters', new Blob([JSON.stringify(requestData)], {
            type: 'application/json'
        }));

        // Submit to API
        const apiResponse = await fetch('/api/virtualize', {
            method: 'POST',
            body: formData
        });
        
        if (!apiResponse.ok) {
            const errorText = await apiResponse.text();
            throw new Error(errorText || 'Virtualization failed');
        }
        
        const result = await apiResponse.json();
        
        // Store results and redirect
        sessionStorage.setItem('virtualizationData', result.jsonData);
        sessionStorage.setItem('gpxContent', result.gpxContent);

        hideVirtualizationProgress();
        
        // Redirect to results page
        window.location.href = '/results';
        
    } catch (error) {
        console.error('Error during virtualization:', error);
        hideVirtualizationProgress();
        showVirtualizationError(error.message);
    }
}

function showVirtualizationProgress() {
    // Create progress overlay if it doesn't exist
    if (!document.getElementById('virtualizationProgress')) {
        const progressOverlay = document.createElement('div');
        progressOverlay.id = 'virtualizationProgress';
        progressOverlay.className = 'position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center';
        progressOverlay.style.backgroundColor = 'rgba(0,0,0,0.8)';
        progressOverlay.style.zIndex = '9999';
        progressOverlay.innerHTML = `
            <div class="text-center text-white">
                <div class="spinner-border mb-3" style="width: 3rem; height: 3rem;"></div>
                <h4>Generating Virtual Activity</h4>
                <p>Processing your GPX file with custom power curve...</p>
            </div>
        `;
        document.body.appendChild(progressOverlay);
    } else {
        document.getElementById('virtualizationProgress').classList.remove('d-none');
    }
}

function hideVirtualizationProgress() {
    const progress = document.getElementById('virtualizationProgress');
    if (progress) {
        progress.classList.add('d-none');
    }
}

function showVirtualizationError(message) {
    alert('Virtualization Error: ' + message);
}

// Make function available globally
window.processVirtualization = processVirtualization;