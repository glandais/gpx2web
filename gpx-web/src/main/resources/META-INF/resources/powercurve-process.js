// Process virtualization and redirect to results - now uses shared utilities
async function processVirtualizationFromContinueButton() {
    const powerCurveData = JSON.parse(sessionStorage.getItem('powerCurveData'));
    
    if (!powerCurveData) {
        showVirtualizationError('Missing power curve data. Please start over.');
        return;
    }
    
    // Use the shared virtualization function
    await processVirtualization(powerCurveData);
}

// Make function available globally
window.processVirtualizationFromContinueButton = processVirtualizationFromContinueButton;