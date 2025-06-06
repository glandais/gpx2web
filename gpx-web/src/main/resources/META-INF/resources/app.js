// Main Application Logic

var dataChart;
var virtualizationTimeout;

function initializeApp() {
    // Initialize step event handlers
    initUploadHandlers();
    initParameterHandlers();
    initPowerCurve();
    loadSavedParameters();
}

// ============================================================================
// STEP 0: FILE UPLOAD HANDLERS
// ============================================================================

function initUploadHandlers() {
    const fileInput = document.getElementById('gpxFileInput');
    const uploadArea = document.getElementById('fileUploadArea');
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

function selectGpxFile(gpxFileData) {
    StateManager.setState({ gpxFileData });
}

StateManager.addListener(function (keys) {
    if (keys.indexOf("gpxFileData") >= 0) {
        if (AppState.gpxFileData) {
            analyzeGpxFile();
        }
    }
});

async function analyzeGpxFile() {
    if (!AppState.gpxFileData) {
        alert('Please select a GPX file first.');
        return;
    }
    
    StateManager.setLoading(true, 'Analyzing GPX File', 'Reading route data...');
    
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
            gpxAnalysis: analysisData,
            powerCurveData: [
                { distanceKm: 0, powerW: 280 },
                { distanceKm: analysisData.totalDistanceMeters / 1000, powerW: 280 }
            ],
            virtualizationResult: null,
        });
        
        StateManager.setLoading(false);
        StateManager.nextStep();
        
    } catch (error) {
        StateManager.setLoading(false);
        StateManager.setError('Error analyzing GPX file: ' + error.message);
    }
}

// ============================================================================
// STEP 2: PARAMETERS HANDLERS
// ============================================================================

StateManager.addListener(function (keys) {
    if (keys.indexOf("inputDateTime") >= 0) {
        if (AppState.inputDateTime) {
            // Update UI
            const startTimeInput = document.getElementById('startTime');
            const dateTime = AppState.inputDateTime;
            const offset = dateTime.getTimezoneOffset() * 60000;
            const localISOTime = (new Date(dateTime - offset)).toISOString().slice(0, 16);
            startTimeInput.value = localISOTime;
        }
    }
});

function initParameterHandlers() {
    var formControls = [].slice.call(document.querySelectorAll('.form-control'))
    formControls.forEach(function (formControl) {
        formControl.addEventListener('change', saveParameters);
    });
    const resetParamsBtn = document.getElementById('resetParamsBtn');
    resetParamsBtn.addEventListener('click', resetParameters);
}

function resetParameters() {
    StateManager.setState({ parametersData: AppState.resetUpdates().parametersData });
    loadSavedParameters();
}

function loadSavedParameters() {
    const params = AppState.parametersData;
    if (params) {
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
    }
}

function saveParameters() {
    const form = document.getElementById('parametersForm');
    const formData = new FormData(form);
    
    const parametersData = {
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
    
    StateManager.setState({ parametersData });
}

StateManager.addListener(function (keys) {
    if (keys.indexOf("parametersData") >= 0) {
        scheduleVirtualization();
    }
});

// ============================================================================
// STEP 3: POWER CURVE HANDLERS (adapted from powercurve-editor.js)
// ============================================================================

function initPowerCurve() {
    initializeDataChart();
    const generateBtn = document.getElementById('generateVirtualActivityBtn');
    generateBtn.addEventListener('click', generateVirtualActivity);
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
        if (dataChart) {
            dataChart.resetZoom();
        }
    });
}

async function generateVirtualActivity() {
    if (AppState.powerCurveData.length < 2) {
        alert('Power curve must have at least 2 points');
        return;
    }
    
    StateManager.setLoading(true, 'Generating Virtual Activity', 'Processing your GPX file with custom power curve...');

    await processVirtualization(AppState.powerCurveData,
        (result) => {
            StateManager.setLoading(false);
            downloadGpxFile(result.gpxContent);
        },
        (error) => {
            StateManager.setLoading(false);
            StateManager.setError('Error generating virtual activity: ' + error.message);
        });
}

function downloadGpxFile(gpxContent) {
    if (!gpxContent) {
        alert('No GPX content available');
        return;
    }
    
    const blob = new Blob([gpxContent], { type: 'application/gpx+xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = AppState.gpxAnalysis.name + '.gpx';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function downloadJsonFile(virtualizationData) {
    if (!virtualizationData) {
        alert('No JSON data available');
        return;
    }
    
    const blob = new Blob([virtualizationData], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = AppState.gpxAnalysis.name + '.json';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

StateManager.addListener(function (keys) {
    if (keys.indexOf("isLoading") >= 0) {
        // Update UI
        if (AppState.isLoading) {
            document.getElementById('loadingOverlay').classList.remove('hidden');
            document.getElementById('loadingText').textContent = AppState.loadingTitle;
            document.getElementById('loadingSubtext').textContent = AppState.loadingSubtitle;
        } else {
            document.getElementById('loadingOverlay').classList.add('hidden');
        }
    }
});

StateManager.addListener(function (keys) {
    if (keys.indexOf("error") >= 0) {
        // Update UI
        var errorToastElement = document.getElementById('errorToast');
        var errorToast = bootstrap.Toast.getOrCreateInstance(errorToastElement);
        if (AppState.error) {
            document.getElementById('errorText').textContent = AppState.error;
            errorToast.show();
        } else {
            errorToast.hide();
        }
    }
});

// ============================================================================
// POWER CURVE FUNCTIONS
// ============================================================================

StateManager.addListener(function (keys) {
    if (keys.indexOf("powerCurveData") >= 0) {
        if (AppState.powerCurveData.length > 0) {
            updateDataCharts();
            updatePowerCurveDataTable();
            scheduleVirtualization();
        }
    }
});

function clampPower(power) {
    return Math.max(100, Math.min(power, 3000));
}

function addPowerPoint(distanceKm, powerW) {
    distanceKm = Math.max(0, Math.min(distanceKm, AppState.totalDistanceKm()));
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
}

function removePowerPoint(index) {
    if (index === 0 || index === AppState.powerCurveData.length - 1) {
        alert('Cannot remove start or end points');
        return;
    }
    
    const powerCurveData = [...AppState.powerCurveData];
    powerCurveData.splice(index, 1);
    
    StateManager.setState({ powerCurveData });
}

function updatePowerCurveDataTable() {
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
                       max="${AppState.totalDistanceKm().toFixed(2)}"
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
        const clampedDistance = Math.max(0, Math.min(numValue, AppState.totalDistanceKm()));
        powerCurveData[index].distanceKm = clampedDistance;
        powerCurveData.sort((a, b) => a.distanceKm - b.distanceKm);
    } else if (field === 'power') {
        const clampedPower = clampPower(numValue);
        powerCurveData[index].powerW = clampedPower;
    }
    
    StateManager.setState({ powerCurveData });
}

function scheduleVirtualization() {

    if (virtualizationTimeout) {
        clearTimeout(virtualizationTimeout);
    }

    virtualizationTimeout = setTimeout(() => {
        if (!AppState.isVirtualizing) {
            performBackgroundVirtualization();
        }
    }, 2000);
}

async function performBackgroundVirtualization() {
    if (AppState.isVirtualizing) return;

    if (!AppState.parametersData || !AppState.powerCurveData || !AppState.gpxFileData) {
        return;
    }

    StateManager.setState({ isVirtualizing: true });
    
    await processVirtualization(
        AppState.powerCurveData,
        (result) => {
            const gpxData = JSON.parse(result.jsonData);
            StateManager.setState({ virtualizationResult: {
                points: gpxData.points,
                summary: result.summary
            } });
        },
        (error) => {
            console.warn('Background virtualization failed:', error.message);
        }
    );
    
    StateManager.setState({ isVirtualizing: false });
}

StateManager.addListener(function (keys) {
    if (keys.indexOf("virtualizationResult") >= 0) {
        const summaryElement = document.getElementById('virtualizationSummary');
        if (AppState.virtualizationResult && AppState.virtualizationResult.summary) {
            const summary = AppState.virtualizationResult.summary
            const durationMinutes = Math.floor(summary.totalTimeSeconds / 60);
            const durationSeconds = Math.floor(summary.totalTimeSeconds % 60);
            summaryElement.innerHTML = `
                <strong>Preview:</strong>
                ${summary.totalDistanceKm.toFixed(2)} km,
                ${durationMinutes}:${durationSeconds.toString().padStart(2, '0')},
                ${summary.averageSpeedKmH.toFixed(1)} km/h avg
            `;
        } else {
            summaryElement.innerHTML = "";
        }
    }
});


StateManager.addListener(function (keys) {
    if (keys.indexOf("virtualizationResult") >= 0) {
        updateDataCharts();
    }
});

function updateDataCharts() {
    if (!dataChart) {
        return;
    }

    if (AppState.powerCurveData) {
        dataChart.data.datasets[0].data = AppState.powerCurveData.map(point =>
            ({ x: point.distanceKm, y: point.powerW })
        );
    } else {
        dataChart.data.datasets[0].data = [];
    }
    if (AppState.virtualizationResult && AppState.virtualizationResult.points) {
        const points = AppState.virtualizationResult.points;
        const distances = points.map(point => point.dist / 1000);
        const elevations = points.map(point => point.ele);
        const speeds = points.map(point => point.speed || 0);
        const powers = points.map(point => point.power || 0);

        dataChart.data.datasets[1].data = distances.map((dist, i) => ({ x: dist, y: elevations[i] }));
        dataChart.data.datasets[2].data = distances.map((dist, i) => ({ x: dist, y: speeds[i] }));
        dataChart.data.datasets[3].data = distances.map((dist, i) => ({ x: dist, y: powers[i] }));
    } else {
        dataChart.data.datasets[1].data = [];
        dataChart.data.datasets[2].data = [];
        dataChart.data.datasets[3].data = [];
    }

    updateChartScales();
    dataChart.update('none');
}

function updateChartScales() {
    dataChart.options.scales.x.min = 0
    dataChart.options.scales.x.max = AppState.totalDistanceKm()

    let powerDataRange = { min: 0, max: 500 };
    powerDataRange = getYRange(dataChart.data.datasets[0].data, powerDataRange);
    powerDataRange = getYRange(dataChart.data.datasets[3].data, powerDataRange);
    dataChart.options.scales.y2.min = addPadding(powerDataRange.min, powerDataRange.max, 0.1).min;
    dataChart.options.scales.y2.max = addPadding(powerDataRange.min, powerDataRange.max, 0.1).max;

    const elevationRange = getYRange(dataChart.data.datasets[1].data);
    if (elevationRange) {
        dataChart.options.scales.y.min = addPadding(elevationRange.min, elevationRange.max, 0.1).min;
        dataChart.options.scales.y.max = addPadding(elevationRange.min, elevationRange.max, 0.1).max;
    } else {
        dataChart.options.scales.y.min = 0;
        dataChart.options.scales.y.max = 100;
    }

    const speedRange = getYRange(dataChart.data.datasets[2].data);
    if (speedRange) {
        dataChart.options.scales.y1.min = addPadding(speedRange.min, speedRange.max, 0.1).min;
        dataChart.options.scales.y1.max = addPadding(speedRange.min, speedRange.max, 0.1).max;
    } else {
        dataChart.options.scales.y1.min = 0;
        dataChart.options.scales.y1.max = 100;
    }
    dataChart.options.plugins.zoom.limits.x.min = dataChart.options.scales.x.min;
    dataChart.options.plugins.zoom.limits.x.max = dataChart.options.scales.x.max;
    dataChart.options.plugins.zoom.limits.y.min = dataChart.options.scales.y.min;
    dataChart.options.plugins.zoom.limits.y.max = dataChart.options.scales.y.max;
    dataChart.options.plugins.zoom.limits.y1.min = dataChart.options.scales.y1.min;
    dataChart.options.plugins.zoom.limits.y1.max = dataChart.options.scales.y1.max;
    dataChart.options.plugins.zoom.limits.y2.min = dataChart.options.scales.y2.min;
    dataChart.options.plugins.zoom.limits.y2.max = dataChart.options.scales.y2.max;
}

function getYRange(points, existingRange) {
    if (!points || points.length === 0) {
        return existingRange;
    }
    const values = points.map(d => d.y);
    const validData = values.filter(v => v != null && !isNaN(v));
    if (validData.length === 0) {
        return existingRange;
    }
    let range = {
        min: Math.min(...validData),
        max: Math.max(...validData)
    };
    if (existingRange) {
        return {
            min: Math.min(existingRange.min, range.min),
            max: Math.max(existingRange.max, range.max)
        }
    } else {
        return range;
    }
}

function addPadding(min, max, paddingPercent) {
    const range = max - min;
    const padding = range * paddingPercent;
    
    return {
        min: min - padding,
        max: max + padding
    };
}

// Chart initialization and interaction functions
function initializeDataChart() {
    const dataCtx = document.getElementById('dataChart');
    if (!dataCtx) return;

    dataChart = new Chart(dataCtx, {
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
                    max: 10,
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
                        mode: 'xy'
                    },
                    pan: {
                        enabled: true,
                        mode: 'xy',
                    },
                    limits: {
                        x: {min: 0, max: 10},
                        y: {min: 0, max: 100},
                        y1: {min: 0, max: 100},
                        y2: {min: 0, max: 1000}
                    },
                }
            },
            onClick: function(event, elements) {
                if (AppState.isDragging) return;
                
                const hasPowerCurvePoint = elements.some(element => element.datasetIndex === 0);
                
                if (!hasPowerCurvePoint) {
                    const canvasPosition = Chart.helpers.getRelativePosition(event, dataChart);
                    const dataX = dataChart.scales.x.getValueForPixel(canvasPosition.x);
                    const dataY = dataChart.scales.y2.getValueForPixel(canvasPosition.y);
                    
                    if (dataX >= 0 && dataX <= AppState.totalDistanceKm() && dataY >= 50 && dataY <= 3000) {
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
    setupDragAndDrop();
}

function setupDragAndDrop() {
    const canvas = dataChart.canvas;
    
    canvas.addEventListener('mousedown', function(event) {
        const elements = dataChart.getElementsAtEventForMode(event, 'nearest', { intersect: true }, true);
        const powerCurveElement = elements.find(element => element.datasetIndex === 0);
        
        if (powerCurveElement) {
            StateManager.setState({ 
                isDragging: true,
                dragPointIndex: powerCurveElement.index 
            });
            canvas.style.cursor = 'grabbing';
            
            dataChart.options.plugins.zoom.zoom.wheel.enabled = false;
            dataChart.options.plugins.zoom.pan.enabled = false;
        }
    });
    
    canvas.addEventListener('mousemove', function(event) {
        if (AppState.isDragging && AppState.dragPointIndex >= 0) {
            const canvasPosition = Chart.helpers.getRelativePosition(event, dataChart);
            const dataX = dataChart.scales.x.getValueForPixel(canvasPosition.x);
            const dataY = dataChart.scales.y2.getValueForPixel(canvasPosition.y);
            
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
        }
    });
    
    canvas.addEventListener('mouseup', function(event) {
        if (AppState.isDragging) {
            StateManager.setState({ 
                isDragging: false,
                dragPointIndex: -1 
            });
            canvas.style.cursor = '';
            
            dataChart.options.plugins.zoom.zoom.wheel.enabled = true;
            dataChart.options.plugins.zoom.pan.enabled = true;
        }
    });
    
    canvas.addEventListener('mouseleave', function(event) {
        if (AppState.isDragging) {
            StateManager.setState({ 
                isDragging: false,
                dragPointIndex: -1 
            });
            canvas.style.cursor = '';
            
            dataChart.options.plugins.zoom.zoom.wheel.enabled = true;
            dataChart.options.plugins.zoom.pan.enabled = true;
        }
    });
}

// Export functions for global access
window.updatePowerPoint = updatePowerPoint;
window.removePowerPoint = removePowerPoint;