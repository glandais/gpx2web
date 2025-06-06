// Application State Management System

const Step = {
    UPLOAD: 0,
    PARAMETERS: 1,
    max: 1
}

const StepIds = ['upload', 'parameters'];

// Global application state
const AppState = {
    totalDistanceKm() {
        if (this.gpxAnalysis) {
            return this.gpxAnalysis.totalDistanceMeters / 1000;
        }
    },

    // Update state and trigger re-render
    setState(updates) {
        Object.assign(AppState, updates);
    },

    // Reset
    resetUpdates() {
        return {
            // Current step (Step)
            currentStep : Step.UPLOAD,
            // Step 0 - File Upload
            gpxFileData : null,
            // Step 1 - Analysis / Parameters
            gpxAnalysis : null,
            parametersData : {
                startTime : new Date(),
                // cyclist
                weightKg: 80,
                powerWatts: 250, //not used
                harmonics: true,
                maxBrakeG: 0.6,
                dragCoefficient: 0.7,
                frontalAreaM2: 0.5,
                maxAngleDeg: 35,
                maxSpeedKmH: 100,
                // bike
                rollingResistance: 0.004,
                frontWheelInertia: 0.05,
                rearWheelInertia: 0.07,
                wheelRadiusM: 0.7,
                efficiency: 0.976,
                // wind
                directionDeg: 0,
                speedMs: 0
            },
            // Step 2 - Power Curve
            powerCurveData : [],
            chartDatasets: ['user_power', 'power', 'ele', 'speed'],
            virtualizationResult: null,
            // UI State
            isLoading : false,
            loadingTitle: null,
            loadingSubtitle: null,
            error : null,
            // Chart state
            isVirtualizing : false,
            isDragging : false,
            dragPointIndex : -1,
        };
    },
};

AppState.setState(AppState.resetUpdates());

// State management functions
const StateManager = {
    // Get current state
    getState() {
        return AppState;
    },
    
    // Update state and trigger re-render
    setState(updates) {
        AppState.setState(updates);
        this.notifyStateChange(Object.keys(updates));
    },

    // Reset state to initial
    reset() {
        this.setState(AppState.resetUpdates());
    },

    // Reset state to initial
    restart() {
        this.setState({
            // Current step (Step)
            currentStep : Step.UPLOAD,
            // Step 0 - File Upload
            gpxFileData : null
        });
    },

    // Navigate to specific step
    navigateToStep(stepIndex) {
        if (stepIndex >= 0 && stepIndex <= Step.max) {
            this.setState({ currentStep: stepIndex });
        }
    },
    
    // Go to next step
    nextStep() {
        if (AppState.currentStep < Step.max) {
            this.navigateToStep(AppState.currentStep + 1);
        }
    },
    
    // Go to previous step
    prevStep() {
        if (AppState.currentStep > 0) {
            this.navigateToStep(AppState.currentStep - 1);
        }
    },

    // Set loading state
    setLoading(isLoading, loadingTitle, loadingSubtitle) {
        this.setState({ isLoading, loadingTitle, loadingSubtitle });
    },
    
    // Set error state
    setError(error) {
        console.warn(error);
        this.setState({ error });
    },
    
    // Clear error
    clearError() {
        this.setState({ error: null });
    },
    
    // Render current step
    renderCurrentStep() {
        const currentStepName = StepIds[AppState.currentStep];
        
        // Hide all step containers
        StepIds.forEach(step => {
            const container = document.getElementById(`step-${step}`);
            if (container) {
                container.style.display = 'none';
            }
        });
        
        // Show current step
        const currentContainer = document.getElementById(`step-${currentStepName}`);
        if (currentContainer) {
            currentContainer.style.display = 'block';
        }
    },

    // State change listeners
    listeners: [ ],
    
    addListener(callback) {
        this.listeners.push(callback);
    },
    
    removeListener(callback) {
        this.listeners = this.listeners.filter(listener => listener !== callback);
    },
    
    notifyStateChange(keys) {
        this.listeners.forEach(listener => listener(keys));
    }
};

StateManager.addListener(function (keys) {
    if (keys.indexOf("currentStep") >= 0) {
        StateManager.renderCurrentStep();
    }
});

// Export for global access
window.AppState = AppState;
window.StateManager = StateManager;
