// Application State Management System

// Global application state
const AppState = {
    // Current step (0: upload, 1: parameters, 2: powercurve, 3: results)
    currentStep: 0,
    
    // Step 0 - File Upload
    selectedGpxFile: null,
    gpxFileData: null, // File object or data URL
    
    // Step 1 - Analysis
    gpxAnalysis: null,
    
    // Step 2 - Parameters
    parametersData: null,
    
    // Step 3 - Power Curve
    powerCurveData: [],
    totalDistanceKm: 0,
    
    // Step 4 - Results
    virtualizationData: null,
    gpxContent: null,
    
    // UI State
    isLoading: false,
    error: null,
    
    // Chart state
    dataChart: null,
    virtualizationTimeout: null,
    isVirtualizing: false,
    isDragging: false,
    dragPointIndex: -1
};

// State management functions
const StateManager = {
    // Get current state
    getState() {
        return AppState;
    },
    
    // Update state and trigger re-render
    setState(updates) {
        Object.assign(AppState, updates);
        this.notifyStateChange();
    },
    
    // Navigate to specific step
    navigateToStep(stepIndex) {
        if (stepIndex >= 0 && stepIndex <= 4) {
            this.setState({ currentStep: stepIndex });
            this.renderCurrentStep();
        }
    },
    
    // Go to next step
    nextStep() {
        if (AppState.currentStep < 3) {
            this.navigateToStep(AppState.currentStep + 1);
        }
    },
    
    // Go to previous step
    prevStep() {
        if (AppState.currentStep > 0) {
            this.navigateToStep(AppState.currentStep - 1);
        }
    },
    
    // Reset state to initial
    reset() {
        AppState.currentStep = 0;
        AppState.selectedGpxFile = null;
        AppState.gpxFileData = null;
        AppState.gpxAnalysis = null;
        AppState.parametersData = null;
        AppState.powerCurveData = [];
        AppState.totalDistanceKm = 0;
        AppState.virtualizationData = null;
        AppState.gpxContent = null;
        AppState.isLoading = false;
        AppState.error = null;
        AppState.dataChart = null;
        AppState.virtualizationTimeout = null;
        AppState.isVirtualizing = false;
        AppState.isDragging = false;
        AppState.dragPointIndex = -1;
        this.renderCurrentStep();
    },
    
    // Set loading state
    setLoading(isLoading) {
        this.setState({ isLoading });
    },
    
    // Set error state
    setError(error) {
        this.setState({ error });
    },
    
    // Clear error
    clearError() {
        this.setState({ error: null });
    },
    
    // Render current step
    renderCurrentStep() {
        const steps = ['upload', 'parameters', 'powercurve', 'results'];
        const currentStepName = steps[AppState.currentStep];
        
        // Hide all step containers
        steps.forEach(step => {
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
        
        // Update progress indicator
        this.updateProgressIndicator();
        
        // Trigger step-specific initialization
        this.initializeCurrentStep();
    },
    
    // Update progress indicator
    updateProgressIndicator() {
        const progressBar = document.querySelector('.progress-bar');
        const progressText = document.querySelector('.progress-text');
        
        if (progressBar) {
            const percentage = (AppState.currentStep / 3) * 100;
            progressBar.style.width = `${percentage}%`;
        }
        
        if (progressText) {
            const stepNames = ['Upload GPX', 'Set Parameters', 'Define Power Curve', 'View Results'];
            progressText.textContent = stepNames[AppState.currentStep];
        }
    },
    
    // Initialize step-specific functionality
    initializeCurrentStep() {
        switch (AppState.currentStep) {
            case 0:
                this.initUploadStep();
                break;
            case 1:
                this.initAnalysisStep();
                this.initParametersStep();
                break;
            case 2:
                this.initPowerCurveStep();
                break;
            case 3:
                this.initResultsStep();
                break;
        }
    },
    
    // Step initialization functions
    initUploadStep() {
        // File upload step initialization
        console.log('Initializing upload step');
    },
    
    initAnalysisStep() {
        // Analysis step initialization
        if (AppState.gpxAnalysis) {
            this.displayAnalysisResults();
        }
    },
    
    initParametersStep() {
        // Parameters step initialization
        if (AppState.parametersData) {
            this.loadParametersForm();
        }
    },
    
    initPowerCurveStep() {
        // Power curve step initialization
        if (typeof initializeDataChart === 'function') {
            setTimeout(() => {
                initializeDataChart();
                if (AppState.powerCurveData.length > 0) {
                    updatePowerTable();
                    performBackgroundVirtualization();
                }
            }, 100);
        }
    },
    
    initResultsStep() {
        // Results step initialization
        if (AppState.virtualizationData) {
            this.displayResults();
        }
    },
    
    // Display functions
    displayAnalysisResults() {
        if (AppState.gpxAnalysis) {
            const distanceEl = document.getElementById('analysis-distance');
            const pointsEl = document.getElementById('analysis-points');
            
            if (distanceEl) {
                distanceEl.textContent = (AppState.gpxAnalysis.totalDistanceMeters / 1000).toFixed(2);
            }
            if (pointsEl) {
                pointsEl.textContent = AppState.gpxAnalysis.totalPoints.toLocaleString();
            }
        }
    },
    
    loadParametersForm() {
        // Load saved parameters into form
        if (AppState.parametersData) {
            Object.keys(AppState.parametersData).forEach(key => {
                const input = document.getElementById(key);
                if (input) {
                    if (input.type === 'checkbox') {
                        input.checked = AppState.parametersData[key];
                    } else if (input.type === 'datetime-local') {
                        const date = new Date();
                        date.setTime(Date.parse(AppState.parametersData[key]));
                        date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
                        const iso = date.toISOString();
                        input.value = iso.slice(0, 16);
                    } else {
                        input.value = AppState.parametersData[key];
                    }
                }
            });
        }
    },
    
    displayResults() {
        // Display virtualization results
        console.log('Displaying results');
    },
    
    // State change listeners
    listeners: [],
    
    addListener(callback) {
        this.listeners.push(callback);
    },
    
    removeListener(callback) {
        this.listeners = this.listeners.filter(listener => listener !== callback);
    },
    
    notifyStateChange() {
        this.listeners.forEach(listener => listener(AppState));
    }
};

// Export for global access
window.AppState = AppState;
window.StateManager = StateManager;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    StateManager.renderCurrentStep();
});