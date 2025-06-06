// Shared virtualization utilities

/**
 * Calls the virtualization API with the provided power curve data
 * @param {Array} powerCurveData - Array of power curve points
 * @param {boolean} isBackground - Whether this is a background call (no UI feedback)
 * @returns {Promise} Promise that resolves with the virtualization result
 */
async function callVirtualizationAPI(powerCurveData, isBackground = false) {
    const { gpxFileData, parametersData } = AppState;
    
    if (!gpxFileData || !parametersData) {
        throw new Error('Missing required data. Please start over.');
    }
    
    // Prepare form data
    const formData = new FormData();
    formData.append('gpxFile', gpxFileData);
    
    // Use parameters directly from AppState
    const parameters = parametersData;
    
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
        
        StateManager.setState({
            virtualizationData: result.jsonData,
            gpxContent: result.gpxContent
        });

        hideVirtualizationProgress();
        
        // Navigate to results step instead of redirecting
        StateManager.nextStep();
        
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