// Main Application Logic

// All state is now managed in AppState - no global variables needed

// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    console.log('Initializing GPX Virtual Cyclist App');
    
    // Initialize step event handlers
    initUploadHandlers();
    initParametersHandlers();
    initPowerCurveHandlers();
    initResultsHandlers();
    
    // Set default start time
    setDefaultStartTime();
    
    // Initial render
    StateManager.renderCurrentStep();
}

// ============================================================================
// STEP 0: FILE UPLOAD HANDLERS
// ============================================================================

function initUploadHandlers() {
    const fileInput = document.getElementById('gpxFileInput');
    const uploadArea = document.getElementById('fileUploadArea');
    const analyzeBtn = document.getElementById('analyzeBtn');
    const fileSelectBtn = document.getElementById('fileSelectBtn');
    
    // File input change
    fileInput.addEventListener('change', handleFileSelect);
    
    // Drag and drop
    uploadArea.addEventListener('dragover', handleDragOver);
    uploadArea.addEventListener('dragleave', handleDragLeave);
    uploadArea.addEventListener('drop', handleFileDrop);
    uploadArea.addEventListener('click', (e) => {
        e.stopPropagation();
        fileInput.click();
    });

    fileSelectBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        fileInput.click();
    });
    
    // Analyze button
    analyzeBtn.addEventListener('click', analyzeGpxFile);
}

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file && file.name.endsWith('.gpx')) {
        selectGpxFile(file);
    } else {
        alert('Please select a valid GPX file.');
    }
}

function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.classList.add('dragover');
}

function handleDragLeave(event) {
    event.currentTarget.classList.remove('dragover');
}

function handleFileDrop(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('dragover');
    
    const files = event.dataTransfer.files;
    if (files.length > 0 && files[0].name.endsWith('.gpx')) {
        selectGpxFile(files[0]);
    } else {
        alert('Please drop a valid GPX file.');
    }
}

function selectGpxFile(file) {
    StateManager.setState({
        selectedGpxFile: { name: file.name, size: file.size },
        gpxFileData: file
    });
    
    // Update UI
    document.getElementById('selectedFileName').textContent = file.name;
    document.getElementById('selectedFileInfo').classList.remove('hidden');
}

async function analyzeGpxFile() {
    if (!AppState.gpxFileData) {
        alert('Please select a GPX file first.');
        return;
    }
    
    StateManager.setLoading(true);
    updateLoadingText('Analyzing GPX File', 'Reading route data and calculating statistics...');
    
    try {
        const formData = new FormData();
        formData.append('gpxFile', AppState.gpxFileData);
        
        const response = await fetch('/api/analyze', {
            method: 'POST',
            body: formData
        });
        
        if (!response.ok) {
            throw new Error('Failed to analyze GPX file');
        }
        
        const analysisData = await response.json();
        
        StateManager.setState({
            gpxAnalysis: analysisData
        });
        
        StateManager.setLoading(false);
        StateManager.nextStep();
        
    } catch (error) {
        StateManager.setLoading(false);
        StateManager.setError(error.message);
        alert('Error analyzing GPX file: ' + error.message);
    }
}

// ============================================================================
// STEP 2: PARAMETERS HANDLERS
// ============================================================================

function initParametersHandlers() {
    const saveBtn = document.getElementById('saveParametersBtn');
    saveBtn.addEventListener('click', saveParameters);
}

function setDefaultStartTime() {
    const startTimeInput = document.getElementById('startTime');
    const now = new Date();
    const offset = now.getTimezoneOffset() * 60000;
    const localISOTime = (new Date(now - offset)).toISOString().slice(0, 16);
    startTimeInput.value = localISOTime;
}

function saveParameters() {
    const form = document.getElementById('parametersForm');
    const formData = new FormData(form);
    
    const parameters = {
        startTime: new Date(document.getElementById('startTime').value).toISOString(),
        weightKg: parseFloat(document.getElementById('weightKg').value),
        harmonics: document.getElementById('harmonics').checked,
        maxBrakeG: parseFloat(document.getElementById('maxBrakeG').value),
        dragCoefficient: parseFloat(document.getElementById('dragCoefficient').value),
        frontalAreaM2: parseFloat(document.getElementById('frontalAreaM2').value),
        maxAngleDeg: parseFloat(document.getElementById('maxAngleDeg').value),
        maxSpeedKmH: parseFloat(document.getElementById('maxSpeedKmH').value),
        rollingResistance: parseFloat(document.getElementById('rollingResistance').value),
        efficiency: parseFloat(document.getElementById('efficiency').value),
        frontWheelInertia: parseFloat(document.getElementById('frontWheelInertia').value),
        rearWheelInertia: parseFloat(document.getElementById('rearWheelInertia').value),
        wheelRadiusM: parseFloat(document.getElementById('wheelRadiusM').value),
        windSpeedMs: parseFloat(document.getElementById('windSpeedMs').value),
        windDirectionDeg: parseFloat(document.getElementById('windDirectionDeg').value)
    };
    
    StateManager.setState({ parametersData: parameters });
    StateManager.nextStep();
}

// ============================================================================
// STEP 3: POWER CURVE HANDLERS (adapted from powercurve-editor.js)
// ============================================================================

function initPowerCurveHandlers() {
    const generateBtn = document.getElementById('generateVirtualActivityBtn');
    generateBtn.addEventListener('click', generateVirtualActivity);
}

// Override StateManager.initPowerCurveStep to use our data
StateManager.initPowerCurveStep = function() {
    if (AppState.gpxAnalysis) {
        // Display analysis info
        document.getElementById('totalDistance').textContent = (AppState.gpxAnalysis.totalDistanceMeters / 1000).toFixed(2);
        document.getElementById('totalPoints').textContent = AppState.gpxAnalysis.totalPoints.toLocaleString();
        
        // Update totalDistanceKm in state
        StateManager.setState({ 
            totalDistanceKm: AppState.gpxAnalysis.totalDistanceMeters / 1000 
        });

        // Initialize power curve data if empty
        if (AppState.powerCurveData.length === 0) {
            const initialPowerCurve = [
                { distanceKm: 0, powerW: 280 },
                { distanceKm: AppState.totalDistanceKm, powerW: 280 }
            ];
            StateManager.setState({ powerCurveData: initialPowerCurve });
        }
        
        // Initialize chart and table
        setTimeout(() => {
            initializeDataChart();
            updatePowerTable();
            setupEventHandlers();
            performBackgroundVirtualization();
        }, 100);
    }
};

async function generateVirtualActivity() {
    if (AppState.powerCurveData.length < 2) {
        alert('Power curve must have at least 2 points');
        return;
    }
    
    StateManager.setLoading(true);
    updateLoadingText('Generating Virtual Activity', 'Processing your GPX file with custom power curve...');
    
    try {
        const result = await callVirtualizationAPI(AppState.powerCurveData);
        
        StateManager.setState({
            virtualizationData: result.jsonData,
            gpxContent: result.gpxContent
        });
        
        StateManager.setLoading(false);
        StateManager.nextStep();
        
    } catch (error) {
        StateManager.setLoading(false);
        StateManager.setError(error.message);
        alert('Error generating virtual activity: ' + error.message);
    }
}

// ============================================================================
// STEP 4: RESULTS HANDLERS
// ============================================================================

function initResultsHandlers() {
    const downloadGpxBtn = document.getElementById('downloadGpxBtn');
    const downloadJsonBtn = document.getElementById('downloadJsonBtn');
    
    downloadGpxBtn.addEventListener('click', downloadGpxFile);
    downloadJsonBtn.addEventListener('click', downloadJsonFile);
}

function downloadGpxFile() {
    if (!AppState.gpxContent) {
        alert('No GPX content available');
        return;
    }
    
    const blob = new Blob([AppState.gpxContent], { type: 'application/gpx+xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'virtual-activity.gpx';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function downloadJsonFile() {
    if (!AppState.virtualizationData) {
        alert('No JSON data available');
        return;
    }
    
    const blob = new Blob([AppState.virtualizationData], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'virtual-activity-data.json';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

function updateLoadingText(title, subtitle) {
    document.getElementById('loadingText').textContent = title;
    document.getElementById('loadingSubtext').textContent = subtitle;
}

function showLoading() {
    document.getElementById('loadingOverlay').classList.remove('hidden');
}

function hideLoading() {
    document.getElementById('loadingOverlay').classList.add('hidden');
}

// Override StateManager loading functions to use our UI
StateManager.setLoading = function(isLoading) {
    this.setState({ isLoading });
    if (isLoading) {
        showLoading();
    } else {
        hideLoading();
    }
};

// ============================================================================
// POWER CURVE FUNCTIONS (from powercurve-editor.js)
// ============================================================================

function clampPower(power) {
    return Math.max(100, Math.min(power, 3000));
}

function addPowerPoint(distanceKm, powerW) {
    distanceKm = Math.max(0, Math.min(distanceKm, AppState.totalDistanceKm));
    powerW = clampPower(powerW);
    
    const powerCurveData = [...AppState.powerCurveData];
    const existingIndex = powerCurveData.findIndex(point => 
        Math.abs(point.distanceKm - distanceKm) < 0.1
    );
    
    if (existingIndex >= 0) {
        powerCurveData[existingIndex].powerW = powerW;
    } else {
        powerCurveData.push({ distanceKm, powerW });
    }
    
    powerCurveData.sort((a, b) => a.distanceKm - b.distanceKm);
    
    StateManager.setState({ powerCurveData });
    updateChart();
    updatePowerTable();
    scheduleVirtualization();
}

function removePowerPoint(index) {
    if (index === 0 || index === AppState.powerCurveData.length - 1) {
        alert('Cannot remove start or end points');
        return;
    }
    
    const powerCurveData = [...AppState.powerCurveData];
    powerCurveData.splice(index, 1);
    
    StateManager.setState({ powerCurveData });
    updateChart();
    updatePowerTable();
    scheduleVirtualization();
}

function updateChart() {
    if (AppState.dataChart) {
        AppState.dataChart.data.datasets[0].data = AppState.powerCurveData.map(point => 
            ({ x: point.distanceKm, y: point.powerW })
        );
        
        updatePowerAxisScale();
        AppState.dataChart.update();
    }
}

function updatePowerAxisScale() {
    const powerCurveRange = getDataRange(AppState.powerCurveData.map(p => p.powerW));
    
    let powerDataRange = { min: powerCurveRange.min, max: powerCurveRange.max };
    if (AppState.dataChart.data.datasets[3].data.length > 0) {
        const powerData = AppState.dataChart.data.datasets[3].data.map(d => d.y);
        powerDataRange = getDataRange(powerData);
    }
    
    const combinedPowerRange = {
        min: Math.min(powerCurveRange.min, powerDataRange.min),
        max: Math.max(powerCurveRange.max, powerDataRange.max)
    };
    
    AppState.dataChart.options.scales.y2.min = addPadding(combinedPowerRange.min, combinedPowerRange.max, 0.1).min;
    AppState.dataChart.options.scales.y2.max = addPadding(combinedPowerRange.min, combinedPowerRange.max, 0.1).max;
}

function updatePowerTable() {
    const tableBody = document.getElementById('powerPointsTable');
    tableBody.innerHTML = '';
    
    AppState.powerCurveData.forEach((point, index) => {
        const row = document.createElement('tr');
        const isEndpoint = index === 0 || index === AppState.powerCurveData.length - 1;
        
        row.innerHTML = `
            <td>
                <input type="number" 
                       class="editable-cell" 
                       value="${point.distanceKm.toFixed(2)}" 
                       min="0" 
                       max="${AppState.totalDistanceKm.toFixed(2)}" 
                       step="0.1"
                       ${isEndpoint ? 'readonly' : ''}
                       onchange="updatePowerPoint(${index}, 'distance', this.value)">
            </td>
            <td>
                <input type="number" 
                       class="editable-cell" 
                       value="${point.powerW}" 
                       min="100" 
                       max="3000" 
                       step="1"
                       onchange="updatePowerPoint(${index}, 'power', this.value)">
            </td>
            <td class="text-center">
                ${!isEndpoint ? `
                    <button type="button" 
                            class="btn btn-sm btn-remove-point" 
                            onclick="removePowerPoint(${index})"
                            title="Remove point">
                        ×
                    </button>
                ` : ''}
            </td>
        `;
        
        tableBody.appendChild(row);
    });
}

function updatePowerPoint(index, field, value) {
    const numValue = parseFloat(value);
    const powerCurveData = [...AppState.powerCurveData];
    
    if (field === 'distance') {
        const clampedDistance = Math.max(0, Math.min(numValue, AppState.totalDistanceKm));
        powerCurveData[index].distanceKm = clampedDistance;
        powerCurveData.sort((a, b) => a.distanceKm - b.distanceKm);
    } else if (field === 'power') {
        const clampedPower = clampPower(numValue);
        powerCurveData[index].powerW = clampedPower;
    }
    
    StateManager.setState({ powerCurveData });
    updateChart();
    updatePowerTable();
    scheduleVirtualization();
}

function scheduleVirtualization() {
    if (AppState.virtualizationTimeout) {
        clearTimeout(AppState.virtualizationTimeout);
    }
    
    const timeoutId = setTimeout(() => {
        if (!AppState.isVirtualizing) {
            performBackgroundVirtualization();
        }
    }, 2000);
    
    StateManager.setState({ virtualizationTimeout: timeoutId });
}

async function performBackgroundVirtualization() {
    if (AppState.isVirtualizing) return;
    
    StateManager.setState({ isVirtualizing: true });
    
    await processBackgroundVirtualization(
        AppState.powerCurveData,
        (result) => {
            const gpxData = JSON.parse(result.jsonData);
            updateDataCharts(gpxData.points);
            updateSummaryDisplay(result.summary);
        },
        (error) => {
            console.warn('Background virtualization failed:', error.message);
        }
    );
    
    StateManager.setState({ isVirtualizing: false });
}

function updateDataCharts(gpxData) {
    if (!AppState.dataChart) {
        return;
    }
    
    const distances = gpxData.map(point => point.dist / 1000);
    const elevations = gpxData.map(point => point.ele);
    const speeds = gpxData.map(point => (point.speed || 0) * 3.6);
    const powers = gpxData.map(point => point.power || 0);
    
    AppState.dataChart.data.datasets[1].data = distances.map((dist, i) => ({ x: dist, y: elevations[i] }));
    AppState.dataChart.data.datasets[2].data = distances.map((dist, i) => ({ x: dist, y: speeds[i] }));
    AppState.dataChart.data.datasets[3].data = distances.map((dist, i) => ({ x: dist, y: powers[i] }));
    
    updateAxisScales(elevations, speeds, powers);
    AppState.dataChart.update('none');
}

function updateAxisScales(elevations, speeds, powers) {
    const elevationRange = getDataRange(elevations);
    const speedRange = getDataRange(speeds);

    AppState.dataChart.options.scales.y.min = addPadding(elevationRange.min, elevationRange.max, 0.1).min;
    AppState.dataChart.options.scales.y.max = addPadding(elevationRange.min, elevationRange.max, 0.1).max;

    AppState.dataChart.options.scales.y1.min = addPadding(speedRange.min, speedRange.max, 0.1).min;
    AppState.dataChart.options.scales.y1.max = addPadding(speedRange.min, speedRange.max, 0.1).max;

    updatePowerAxisScale();
}

function getDataRange(data) {
    if (!data || data.length === 0) {
        return { min: 0, max: 100 };
    }
    
    const validData = data.filter(v => v != null && !isNaN(v));
    if (validData.length === 0) {
        return { min: 0, max: 100 };
    }
    
    return {
        min: Math.min(...validData),
        max: Math.max(...validData)
    };
}

function addPadding(min, max, paddingPercent) {
    const range = max - min;
    const padding = range * paddingPercent;
    
    return {
        min: min - padding,
        max: max + padding
    };
}

function updateSummaryDisplay(summary) {
    if (summary) {
        const summaryElement = document.getElementById('virtualizationSummary');
        if (summaryElement) {
            const durationMinutes = Math.floor(summary.totalTimeSeconds / 60);
            const durationSeconds = Math.floor(summary.totalTimeSeconds % 60);
            summaryElement.innerHTML = `
                <strong>Preview:</strong> 
                ${summary.totalDistanceKm.toFixed(2)} km, 
                ${durationMinutes}:${durationSeconds.toString().padStart(2, '0')}, 
                ${summary.averageSpeedKmH.toFixed(1)} km/h avg
            `;
        }
    }
}

// Chart initialization and interaction functions
function initializeDataChart() {
    const dataCtx = document.getElementById('dataChart');
    if (!dataCtx) return;
    
    const chart = new Chart(dataCtx, {
        type: 'line',
        data: {
            datasets: [
                {
                    label: 'Power Curve',
                    data: AppState.powerCurveData.map(point => ({ x: point.distanceKm, y: point.powerW })),
                    borderColor: '#007bff',
                    backgroundColor: 'rgba(0, 123, 255, 0.1)',
                    borderWidth: 3,
                    fill: false,
                    tension: 0.1,
                    pointRadius: 6,
                    pointHoverRadius: 8,
                    pointBackgroundColor: '#007bff',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    yAxisID: 'y2'
                },
                {
                    label: 'Elevation (m)',
                    data: [],
                    borderColor: '#28a745',
                    backgroundColor: '#28a74520',
                    borderWidth: 2,
                    fill: false,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    yAxisID: 'y'
                },
                {
                    label: 'Speed (km/h)',
                    data: [],
                    borderColor: '#fd7e14',
                    backgroundColor: '#fd7e1420',
                    borderWidth: 2,
                    fill: false,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    yAxisID: 'y1'
                },
                {
                    label: 'Power (W)',
                    data: [],
                    borderColor: '#dc3545',
                    backgroundColor: '#dc354520',
                    borderWidth: 2,
                    fill: false,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    yAxisID: 'y2'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            interaction: {
                intersect: false,
                mode: 'nearest'
            },
            scales: {
                x: {
                    type: 'linear',
                    title: {
                        display: true,
                        text: 'Distance (km)',
                        font: { weight: 'bold' }
                    },
                    min: 0,
                    max: AppState.totalDistanceKm,
                    grid: {
                        color: 'rgba(0,0,0,0.1)'
                    }
                },
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Elevation (m)',
                        color: '#28a745',
                        font: { weight: 'bold' }
                    },
                    ticks: {
                        color: '#28a745'
                    }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        text: 'Speed (km/h)',
                        color: '#fd7e14',
                        font: { weight: 'bold' }
                    },
                    ticks: {
                        color: '#fd7e14'
                    },
                    grid: {
                        drawOnChartArea: false,
                    }
                },
                y2: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    offset: true,
                    title: {
                        display: true,
                        text: 'Power (W)',
                        color: '#dc3545',
                        font: { weight: 'bold' }
                    },
                    ticks: {
                        color: '#dc3545'
                    },
                    grid: {
                        drawOnChartArea: false,
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        title: function(context) {
                            return `Distance: ${context[0].parsed.x.toFixed(2)} km`;
                        },
                        label: function(context) {
                            const datasetLabel = context.dataset.label;
                            const value = context.parsed.y.toFixed(datasetLabel.includes('Power') ? 0 : 1);
                            return `${datasetLabel}: ${value}`;
                        }
                    }
                },
                zoom: {
                    zoom: {
                        wheel: {
                            enabled: true,
                        },
                        pinch: {
                            enabled: true
                        },
                        mode: 'xy',
                    },
                    pan: {
                        enabled: true,
                        mode: 'xy',
                    }
                }
            },
            onClick: function(event, elements) {
                if (AppState.isDragging) return;
                
                const hasPowerCurvePoint = elements.some(element => element.datasetIndex === 0);
                
                if (!hasPowerCurvePoint) {
                    const canvasPosition = Chart.helpers.getRelativePosition(event, AppState.dataChart);
                    const dataX = AppState.dataChart.scales.x.getValueForPixel(canvasPosition.x);
                    const dataY = AppState.dataChart.scales.y2.getValueForPixel(canvasPosition.y);
                    
                    if (dataX >= 0 && dataX <= AppState.totalDistanceKm && dataY >= 50 && dataY <= 3000) {
                        addPowerPoint(dataX, Math.round(dataY));
                    }
                }
            },
            onHover: function(event, elements) {
                const powerCurveElement = elements.find(element => element.datasetIndex === 0);
                if (powerCurveElement) {
                    event.native.target.style.cursor = 'grab';
                } else {
                    event.native.target.style.cursor = elements.length > 0 ? 'pointer' : 'crosshair';
                }
            }
        }
    });
    
    StateManager.setState({ dataChart: chart });
    setupDragAndDrop();
}

function setupEventHandlers() {
    document.getElementById('addPointBtn').addEventListener('click', function() {
        const powerCurveData = AppState.powerCurveData;
        const minDistance = powerCurveData[powerCurveData.length - 2].distanceKm;
        const maxDistance = powerCurveData[powerCurveData.length - 1].distanceKm;
        const minPower = powerCurveData[powerCurveData.length - 2].powerW;
        const maxPower = powerCurveData[powerCurveData.length - 1].powerW;

        const midDistance = (minDistance + maxDistance) / 2;
        const midPower = (minPower + maxPower) / 2;
        addPowerPoint(midDistance, midPower);
    });
    
    document.getElementById('resetZoomBtn').addEventListener('click', function() {
        if (AppState.dataChart) {
            AppState.dataChart.resetZoom();
        }
    });
}

function setupDragAndDrop() {
    const canvas = AppState.dataChart.canvas;
    
    canvas.addEventListener('mousedown', function(event) {
        const elements = AppState.dataChart.getElementsAtEventForMode(event, 'nearest', { intersect: true }, true);
        const powerCurveElement = elements.find(element => element.datasetIndex === 0);
        
        if (powerCurveElement) {
            StateManager.setState({ 
                isDragging: true,
                dragPointIndex: powerCurveElement.index 
            });
            canvas.style.cursor = 'grabbing';
            
            AppState.dataChart.options.plugins.zoom.zoom.wheel.enabled = false;
            AppState.dataChart.options.plugins.zoom.pan.enabled = false;
        }
    });
    
    canvas.addEventListener('mousemove', function(event) {
        if (AppState.isDragging && AppState.dragPointIndex >= 0) {
            const canvasPosition = Chart.helpers.getRelativePosition(event, AppState.dataChart);
            const dataX = AppState.dataChart.scales.x.getValueForPixel(canvasPosition.x);
            const dataY = AppState.dataChart.scales.y2.getValueForPixel(canvasPosition.y);
            
            const isFirstPoint = AppState.dragPointIndex === 0;
            const isLastPoint = AppState.dragPointIndex === AppState.powerCurveData.length - 1;
            const isEndpoint = isFirstPoint || isLastPoint;
            
            let newDistance, newPower;
            const powerCurveData = [...AppState.powerCurveData];
            
            if (isEndpoint) {
                newDistance = powerCurveData[AppState.dragPointIndex].distanceKm;
            } else {
                const minDistance = powerCurveData[AppState.dragPointIndex - 1].distanceKm + 0.01;
                const maxDistance = powerCurveData[AppState.dragPointIndex + 1].distanceKm - 0.01;
                
                newDistance = Math.max(minDistance, Math.min(maxDistance, dataX));
            }
            newPower = clampPower(dataY);
            
            powerCurveData[AppState.dragPointIndex].distanceKm = newDistance;
            powerCurveData[AppState.dragPointIndex].powerW = Math.round(newPower);
            
            StateManager.setState({ powerCurveData });
            updateChart();
            updatePowerTable();
            scheduleVirtualization();
        }
    });
    
    canvas.addEventListener('mouseup', function(event) {
        if (AppState.isDragging) {
            StateManager.setState({ 
                isDragging: false,
                dragPointIndex: -1 
            });
            canvas.style.cursor = '';
            
            AppState.dataChart.options.plugins.zoom.zoom.wheel.enabled = true;
            AppState.dataChart.options.plugins.zoom.pan.enabled = true;
        }
    });
    
    canvas.addEventListener('mouseleave', function(event) {
        if (AppState.isDragging) {
            StateManager.setState({ 
                isDragging: false,
                dragPointIndex: -1 
            });
            canvas.style.cursor = '';
            
            AppState.dataChart.options.plugins.zoom.zoom.wheel.enabled = true;
            AppState.dataChart.options.plugins.zoom.pan.enabled = true;
        }
    });
}

// Export functions for global access
window.updatePowerPoint = updatePowerPoint;
window.removePowerPoint = removePowerPoint;