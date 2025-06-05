let powerChart;
let powerCurveData = [];
let totalDistanceKm = 0;

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
    initializePowerChart();
    updatePowerTable();
    setupEventHandlers();
});

// Initialize the Chart.js power curve chart
function initializePowerChart() {
    const ctx = document.getElementById('powerChart').getContext('2d');
    
    powerChart = new Chart(ctx, {
        type: 'line',
        data: {
            datasets: [{
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
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                intersect: false,
                mode: 'nearest'
            },
            scales: {
                x: {
                    type: 'linear',
                    position: 'bottom',
                    title: {
                        display: true,
                        text: 'Distance (km)',
                        font: { weight: 'bold' }
                    },
                    min: 0,
                    max: totalDistanceKm * 1.05,
                    grid: {
                        color: 'rgba(0,0,0,0.1)'
                    }
                },
                y: {
                    type: 'linear',
                    title: {
                        display: true,
                        text: 'Power (W)',
                        font: { weight: 'bold' }
                    },
                    min: 100,
                    max: 500,
                    grid: {
                        color: 'rgba(0,0,0,0.1)'
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `Distance: ${context.parsed.x.toFixed(2)} km, Power: ${context.parsed.y} W`;
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
                if (elements.length === 0) {
                    // Clicked on empty space - add new point
                    const canvasPosition = Chart.helpers.getRelativePosition(event, powerChart);
                    const dataX = powerChart.scales.x.getValueForPixel(canvasPosition.x);
                    const dataY = powerChart.scales.y.getValueForPixel(canvasPosition.y);
                    
                    if (dataX >= 0 && dataX <= totalDistanceKm && dataY >= 100 && dataY <= 500) {
                        addPowerPoint(dataX, Math.round(dataY));
                    }
                }
            },
            onHover: function(event, elements) {
                event.native.target.style.cursor = elements.length > 0 ? 'pointer' : 'crosshair';
            }
        }
    });
}

// Add a new power point
function addPowerPoint(distanceKm, powerW) {
    // Ensure distance is within bounds
    distanceKm = Math.max(0, Math.min(distanceKm, totalDistanceKm));
    powerW = Math.max(100, Math.min(powerW, 500));
    
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
}

// Update the chart
function updateChart() {
    powerChart.data.datasets[0].data = powerCurveData.map(point => 
        ({ x: point.distanceKm, y: point.powerW })
    );
    powerChart.update();
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
        const clampedPower = Math.max(100, Math.min(numValue, 500));
        powerCurveData[index].powerW = clampedPower;
    }
    
    updateChart();
    updatePowerTable();
}

// Setup event handlers
function setupEventHandlers() {
    // Add point button
    document.getElementById('addPointBtn').addEventListener('click', function() {
        const midDistance = totalDistanceKm / 2;
        const midPower = 240; // Default mid power
        addPowerPoint(midDistance, midPower);
    });
    
    // Reset zoom button
    document.getElementById('resetZoomBtn').addEventListener('click', function() {
        powerChart.resetZoom();
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

// Export functions for global access
window.updatePowerPoint = updatePowerPoint;
window.removePowerPoint = removePowerPoint;