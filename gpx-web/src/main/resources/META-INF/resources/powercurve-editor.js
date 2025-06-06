let powerChart;
let dataChart;
let powerCurveData = [];
let totalDistanceKm = 0;
let virtualizationTimeout = null;
let isVirtualizing = false;
let isDragging = false;
let dragPointIndex = -1;

// Initialize the power curve editor
document.addEventListener('DOMContentLoaded', function() {

    // Check if we have analysis data
    const savedAnalysisData = sessionStorage.getItem('gpxAnalysis');

    if (!savedAnalysisData) {
        // No analysis data - redirect to start
        window.location.href = '/';
    }
    const analysisData = JSON.parse(savedAnalysisData);

    // Display analysis info
    document.getElementById('totalDistance').textContent = (analysisData.totalDistanceMeters / 1000).toFixed(2);
    document.getElementById('totalPoints').textContent = analysisData.totalPoints.toLocaleString();
    
    totalDistanceKm = analysisData.totalDistanceMeters / 1000;

    const savedParams = sessionStorage.getItem('powerCurveData');
    if (savedParams) {
        powerCurveData = JSON.parse(savedParams);
    } else {
        // Initialize with default power points (start and end)
        powerCurveData = [
            { distanceKm: 0, powerW: 280 },
            { distanceKm: totalDistanceKm, powerW: 280 }
        ];
    }
    
    // Initialize chart and table
    initializeDataChart();
    updatePowerTable();
    setupEventHandlers();
    performBackgroundVirtualization();
});

function clampPower(power) {
    return Math.max(100, Math.min(power, 3000));
}

// Add a new power point
function addPowerPoint(distanceKm, powerW) {
    // Ensure distance is within bounds
    distanceKm = Math.max(0, Math.min(distanceKm, totalDistanceKm));
    powerW = clampPower(powerW);
    
    // Check if point already exists at this distance
    const existingIndex = powerCurveData.findIndex(point => 
        Math.abs(point.distanceKm - distanceKm) < 0.1
    );
    
    if (existingIndex >= 0) {
        // Update existing point
        powerCurveData[existingIndex].powerW = powerW;
    } else {
        // Add new point
        powerCurveData.push({ distanceKm, powerW });
    }
    
    // Sort by distance
    powerCurveData.sort((a, b) => a.distanceKm - b.distanceKm);
    
    updateChart();
    updatePowerTable();
    scheduleVirtualization();
}

// Remove a power point
function removePowerPoint(index) {
    // Don't allow removal of first or last point
    if (index === 0 || index === powerCurveData.length - 1) {
        alert('Cannot remove start or end points');
        return;
    }
    
    powerCurveData.splice(index, 1);
    updateChart();
    updatePowerTable();
    scheduleVirtualization();
}

// Update the chart
function updateChart() {
    if (dataChart) {
        dataChart.data.datasets[0].data = powerCurveData.map(point => 
            ({ x: point.distanceKm, y: point.powerW })
        );
        
        // Update power axis scale when power curve changes
        updatePowerAxisScale();
        
        dataChart.update();
    }
}

// Update only the power axis (y2) scale based on power curve data
function updatePowerAxisScale() {
    const powerCurveRange = getDataRange(powerCurveData.map(p => p.powerW));
    
    // Get current power data range if available
    let powerDataRange = { min: powerCurveRange.min, max: powerCurveRange.max };
    if (dataChart.data.datasets[3].data.length > 0) {
        const powerData = dataChart.data.datasets[3].data.map(d => d.y);
        powerDataRange = getDataRange(powerData);
    }
    
    // Combine power curve and power data ranges
    const combinedPowerRange = {
        min: Math.min(powerCurveRange.min, powerDataRange.min),
        max: Math.max(powerCurveRange.max, powerDataRange.max)
    };
    
    // Update y2 axis
    dataChart.options.scales.y2.min = addPadding(combinedPowerRange.min, combinedPowerRange.max, 0.1).min;
    dataChart.options.scales.y2.max = addPadding(combinedPowerRange.min, combinedPowerRange.max, 0.1).max;
}

// Update the power points table
function updatePowerTable() {
    const tableBody = document.getElementById('powerPointsTable');
    tableBody.innerHTML = '';
    
    powerCurveData.forEach((point, index) => {
        const row = document.createElement('tr');
        const isEndpoint = index === 0 || index === powerCurveData.length - 1;
        
        row.innerHTML = `
            <td>
                <input type="number" 
                       class="editable-cell" 
                       value="${point.distanceKm.toFixed(2)}" 
                       min="0" 
                       max="${totalDistanceKm.toFixed(2)}" 
                       step="0.1"
                       ${isEndpoint ? 'readonly' : ''}
                       onchange="updatePowerPoint(${index}, 'distance', this.value)">
            </td>
            <td>
                <input type="number" 
                       class="editable-cell" 
                       value="${point.powerW}" 
                       min="100" 
                       max="500" 
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

// Update a power point from table input
function updatePowerPoint(index, field, value) {
    const numValue = parseFloat(value);
    
    if (field === 'distance') {
        const clampedDistance = Math.max(0, Math.min(numValue, totalDistanceKm));
        powerCurveData[index].distanceKm = clampedDistance;
        
        // Re-sort by distance
        powerCurveData.sort((a, b) => a.distanceKm - b.distanceKm);
    } else if (field === 'power') {
        const clampedPower = clampPower(numValue);
        powerCurveData[index].powerW = clampedPower;
    }
    
    updateChart();
    updatePowerTable();
    scheduleVirtualization();
}

// Schedule virtualization with timeout to avoid server overload
function scheduleVirtualization() {
    // Clear existing timeout
    if (virtualizationTimeout) {
        clearTimeout(virtualizationTimeout);
    }
    
    // Set new timeout for 2 seconds after last update
    virtualizationTimeout = setTimeout(() => {
        if (!isVirtualizing) {
            performBackgroundVirtualization();
        }
    }, 2000);
}

// Setup event handlers
function setupEventHandlers() {
    // Add point button
    document.getElementById('addPointBtn').addEventListener('click', function() {
        const minDistance = powerCurveData[powerCurveData.length - 2].distanceKm;
        const maxDistance = powerCurveData[powerCurveData.length - 1].distanceKm;
        const minPower = powerCurveData[powerCurveData.length - 2].powerW;
        const maxPower = powerCurveData[powerCurveData.length - 1].powerW;

        const midDistance = (minDistance + maxDistance) / 2;
        const midPower = (minPower + maxPower) / 2;
        addPowerPoint(midDistance, midPower);
    });
    
    // Reset zoom button
    document.getElementById('resetZoomBtn').addEventListener('click', function() {
        if (dataChart) {
            dataChart.resetZoom();
        }
    });
    
    // Back button
    document.getElementById('backBtn').addEventListener('click', function() {
        window.location.href = '/parameters';
    });
    
    // Continue button
    document.getElementById('continueBtn').addEventListener('click', function() {
        // Validate power curve (minimum 2 points)
        if (powerCurveData.length < 2) {
            alert('Power curve must have at least 2 points');
            return;
        }
        
        // Store power curve data and proceed to virtualization
        sessionStorage.setItem('powerCurveData', JSON.stringify(powerCurveData));
        
        // Process virtualization
        processVirtualization();
    });
}

// Perform background virtualization for preview using shared utilities
async function performBackgroundVirtualization() {
    if (isVirtualizing) return;
    
    isVirtualizing = true;
    
    await processBackgroundVirtualization(
        powerCurveData,
        // Success callback
        (result) => {
            // Parse the JSON data and update charts
            const gpxData = JSON.parse(result.jsonData);
            updateDataCharts(gpxData.points);
            updateSummaryDisplay(result.summary);
        },
        // Error callback
        (error) => {
            console.warn('Background virtualization failed:', error.message);
        }
    );
    
    isVirtualizing = false;
}

// Update data chart with new virtualization results
function updateDataCharts(gpxData) {
    if (!dataChart) {
        return;
    }
    
    // Extract data arrays
    const distances = gpxData.map(point => point.dist / 1000); // Convert to km
    const elevations = gpxData.map(point => point.ele);
    const speeds = gpxData.map(point => (point.speed || 0) * 3.6); // Convert m/s to km/h
    const powers = gpxData.map(point => point.power || 0);
    
    // Update all datasets (power curve is index 0, elevation is index 1, speed is index 2, power data is index 3)
    dataChart.data.datasets[1].data = distances.map((dist, i) => ({ x: dist, y: elevations[i] }));
    dataChart.data.datasets[2].data = distances.map((dist, i) => ({ x: dist, y: speeds[i] }));
    dataChart.data.datasets[3].data = distances.map((dist, i) => ({ x: dist, y: powers[i] }));
    
    // Update axis scales based on data
    updateAxisScales(elevations, speeds, powers);
    
    dataChart.update('none');
}

// Update axis scales to fit data with padding
function updateAxisScales(elevations, speeds, powers) {
    // Calculate ranges for each dataset
    const elevationRange = getDataRange(elevations);
    const speedRange = getDataRange(speeds);

    // Update axis options with padding
    dataChart.options.scales.y.min = addPadding(elevationRange.min, elevationRange.max, 0.1).min;
    dataChart.options.scales.y.max = addPadding(elevationRange.min, elevationRange.max, 0.1).max;

    dataChart.options.scales.y1.min = addPadding(speedRange.min, speedRange.max, 0.1).min;
    dataChart.options.scales.y1.max = addPadding(speedRange.min, speedRange.max, 0.1).max;

    updatePowerAxisScale();
}

// Get min/max range from data array
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

// Add padding to min/max values
function addPadding(min, max, paddingPercent) {
    const range = max - min;
    const padding = range * paddingPercent;
    
    return {
        min: min - padding,
        max: max + padding
    };
}

// Update summary display
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

// Initialize combined data chart for elevation, speed, and power
function initializeDataChart() {
    const dataCtx = document.getElementById('dataChart');
    if (!dataCtx) return;
    
    dataChart = new Chart(dataCtx, {
        type: 'line',
        data: {
            datasets: [
                {
                    label: 'Power Curve',
                    data: powerCurveData.map(point => ({ x: point.distanceKm, y: point.powerW })),
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
                    max: totalDistanceKm,
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
                if (isDragging) return; // Ignore clicks during drag
                
                // Check if any clicked element is from the power curve dataset (index 0)
                const hasPowerCurvePoint = elements.some(element => element.datasetIndex === 0);
                
                if (!hasPowerCurvePoint) {
                    // No power curve point clicked - add new point
                    const canvasPosition = Chart.helpers.getRelativePosition(event, dataChart);
                    const dataX = dataChart.scales.x.getValueForPixel(canvasPosition.x);
                    const dataY = dataChart.scales.y2.getValueForPixel(canvasPosition.y);
                    
                    if (dataX >= 0 && dataX <= totalDistanceKm && dataY >= 50 && dataY <= 3000) {
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
    
    // Add mouse event listeners for drag and drop
    setupDragAndDrop();
}

// Setup drag and drop functionality for power curve points
function setupDragAndDrop() {
    const canvas = dataChart.canvas;
    
    canvas.addEventListener('mousedown', function(event) {
        const elements = dataChart.getElementsAtEventForMode(event, 'nearest', { intersect: true }, true);
        const powerCurveElement = elements.find(element => element.datasetIndex === 0);
        
        if (powerCurveElement) {
            isDragging = true;
            dragPointIndex = powerCurveElement.index;
            canvas.style.cursor = 'grabbing';
            
            // Disable chart interactions during drag
            dataChart.options.plugins.zoom.zoom.wheel.enabled = false;
            dataChart.options.plugins.zoom.pan.enabled = false;
        }
    });
    
    canvas.addEventListener('mousemove', function(event) {
        if (isDragging && dragPointIndex >= 0) {
            const canvasPosition = Chart.helpers.getRelativePosition(event, dataChart);
            const dataX = dataChart.scales.x.getValueForPixel(canvasPosition.x);
            const dataY = dataChart.scales.y2.getValueForPixel(canvasPosition.y);
            
            // Calculate constraints
            const isFirstPoint = dragPointIndex === 0;
            const isLastPoint = dragPointIndex === powerCurveData.length - 1;
            const isEndpoint = isFirstPoint || isLastPoint;
            
            let newDistance, newPower;
            
            if (isEndpoint) {
                // First and last points: only allow power changes, keep distance fixed
                newDistance = powerCurveData[dragPointIndex].distanceKm;
            } else {
                // Middle points: allow both distance and power changes with constraints
                const minDistance = powerCurveData[dragPointIndex - 1].distanceKm + 0.01;
                const maxDistance = powerCurveData[dragPointIndex + 1].distanceKm - 0.01;
                
                newDistance = Math.max(minDistance, Math.min(maxDistance, dataX));
            }
            newPower = clampPower(dataY);
            
            // Update the point
            powerCurveData[dragPointIndex].distanceKm = newDistance;
            powerCurveData[dragPointIndex].powerW = Math.round(newPower);
            
            // Update chart and table
            updateChart();
            updatePowerTable();
            scheduleVirtualization();
        }
    });
    
    canvas.addEventListener('mouseup', function(event) {
        if (isDragging) {
            isDragging = false;
            dragPointIndex = -1;
            canvas.style.cursor = '';
            
            // Re-enable chart interactions
            dataChart.options.plugins.zoom.zoom.wheel.enabled = true;
            dataChart.options.plugins.zoom.pan.enabled = true;
        }
    });
    
    // Handle mouse leave to stop dragging
    canvas.addEventListener('mouseleave', function(event) {
        if (isDragging) {
            isDragging = false;
            dragPointIndex = -1;
            canvas.style.cursor = '';
            
            // Re-enable chart interactions
            dataChart.options.plugins.zoom.zoom.wheel.enabled = true;
            dataChart.options.plugins.zoom.pan.enabled = true;
        }
    });
}

// Export functions for global access
window.updatePowerPoint = updatePowerPoint;
window.removePowerPoint = removePowerPoint;