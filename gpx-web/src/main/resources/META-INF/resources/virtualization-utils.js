// Shared virtualization utilities

/**
 * Calls the virtualization API with the provided power curve data
 * @param {Array} powerCurveData - Array of power curve points
 * @param {boolean} isBackground - Whether this is a background call (no UI feedback)
 * @returns {Promise} Promise that resolves with the virtualization result
 */
async function callVirtualizationAPI(powerCurveData, isBackground = false) {
    // Get stored data
    const gpxFileData = sessionStorage.getItem('gpxFileData');
    const parametersData = sessionStorage.getItem('parametersData');
    
    if (!gpxFileData || !parametersData) {
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
        powerCurve: powerCurveData
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
    
    return await apiResponse.json();
}

/**
 * Process virtualization for final results and redirect
 * @param {Array} powerCurveData - Array of power curve points
 */
async function processVirtualization(powerCurveData) {
    try {
        showVirtualizationProgress();
        
        const result = await callVirtualizationAPI(powerCurveData);
        
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

/**
 * Process background virtualization for preview (no redirect)
 * @param {Array} powerCurveData - Array of power curve points
 * @param {Function} onSuccess - Callback function called with result on success
 * @param {Function} onError - Callback function called with error on failure
 */
async function processBackgroundVirtualization(powerCurveData, onSuccess, onError) {
    try {
        const result = await callVirtualizationAPI(powerCurveData, true);
        
        if (onSuccess) {
            onSuccess(result);
        }
        
    } catch (error) {
        console.error('Background virtualization error:', error);
        if (onError) {
            onError(error);
        }
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

// Export functions for global access
window.callVirtualizationAPI = callVirtualizationAPI;
window.processVirtualization = processVirtualization;
window.processBackgroundVirtualization = processBackgroundVirtualization;
window.showVirtualizationProgress = showVirtualizationProgress;
window.hideVirtualizationProgress = hideVirtualizationProgress;
window.showVirtualizationError = showVirtualizationError;