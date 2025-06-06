// Shared virtualization utilities

/**
 * Calls the virtualization API with the provided power curve data
 * @param {Array} powerCurveData - Array of power curve points
 * @returns {Promise} Promise that resolves with the virtualization result
 */
async function callVirtualizationAPI(powerCurveData) {
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
 * Process background virtualization for preview (no redirect)
 * @param {Array} powerCurveData - Array of power curve points
 * @param {Function} onSuccess - Callback function called with result on success
 * @param {Function} onError - Callback function called with error on failure
 */
async function processVirtualization(powerCurveData, onSuccess, onError) {
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

// Export functions for global access
window.processVirtualization = processVirtualization;
