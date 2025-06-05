let map;
let chart;
let currentData = null;
let currentMarker = null;
let polyline = null;

// Initialize the visualization with session data
document.addEventListener('DOMContentLoaded', function() {
    try {
        showLoading();
        
        // Get the JSON data from sessionStorage
        const jsonData = sessionStorage.getItem('virtualizationData');
        if (!jsonData) {
            // No results data - redirect to start
            window.location.href = '/';
        }
        
        currentData = JSON.parse(jsonData);
        
        // Initialize map and chart
        initializeMap();
        initializeChart();
        populateFieldSelectors();
        setupEventHandlers();
        
        hideLoading();
        
    } catch (error) {
        console.error('Error initializing visualization:', error);
        showError('Failed to load visualization data: ' + error.message);
    }
});


// Initialize the Leaflet map
function initializeMap() {
    if (!currentData || !currentData.points) {
        throw new Error('No GPS data available');
    }
    
    // Initialize map
    map = L.map('map').setView([48.8566, 2.3522], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Add route polyline
    const latLngs = currentData.points.map(point => [point.lat, point.lon]);
    polyline = L.polyline(latLngs, { 
        color: '#007bff', 
        weight: 3,
        opacity: 0.8
    }).addTo(map);

    // Fit map to route bounds
    map.fitBounds(polyline.getBounds());
}

// Initialize the Chart.js chart
function initializeChart() {
    const ctx = document.getElementById('chart').getContext('2d');
    
    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: currentData.points.map(point => point.dist),
            datasets: []
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false,
            },
            scales: {
                x: {
                    type: 'linear',
                    position: 'bottom',
                    title: {
                        display: true,
                        text: 'Distance (m)'
                    }
                },
                y: {
                    type: 'linear',
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Value'
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        afterLabel: function(context) {
                            const index = context.dataIndex;
                            const point = currentData.points[index];
                            highlightPointOnMap(index);
                            return [
                                `Elevation: ${point.ele?.toFixed(1) || 'N/A'} m`,
                                `Speed: ${point.speed?.toFixed(1) || 'N/A'} m/s`,
                                `Power: ${point.power?.toFixed(0) || 'N/A'} W`
                            ];
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
                        mode: 'x',
                    },
                    pan: {
                        enabled: true,
                        mode: 'x',
                    }
                },
                legend: {
                    position: 'top',
                }
            }
        }
    });
}

// Populate field selector dropdowns
function populateFieldSelectors() {
    const selects = ['dataSelect1', 'dataSelect2', 'dataSelect3'].map(id => document.getElementById(id));
    const excludeFields = ['lon', 'lat', 'dist'];
    
    // Clear existing options
    selects.forEach(select => {
        select.innerHTML = '<option value="">Select field...</option>';
    });
    
    // Add available fields
    currentData.keys.forEach(key => {
        if (!excludeFields.includes(key)) {
            selects.forEach(select => {
                const option = document.createElement('option');
                option.value = key;
                option.textContent = formatFieldName(key);
                select.appendChild(option);
            });
        }
    });
    
    // Set default selections
    if (selects[0].options.length > 1) selects[0].selectedIndex = 1; // First available field
    if (selects[1].options.length > 2) selects[1].selectedIndex = 2; // Second available field
    if (selects[2].options.length > 3) selects[2].selectedIndex = 3; // Third available field
    
    // Update chart with initial selections
    updateChart();
}

// Format field names for display
function formatFieldName(fieldName) {
    const fieldMappings = {
        'ele': 'Elevation (m)',
        'speed': 'Speed (km/h)',
        'power': 'Power (W)',
        'grade': 'Grade (%)',
        'elapsed': 'Elapsed Time (s)',
        'p_cyclist_current_speed': 'Cyclist Speed (km/h)',
        'p_cyclist_optimal_power': 'Optimal Power (W)',
        'speed_max': 'Max Speed (km/h)',
        'virt_speed_current': 'Virtual Speed (km/h)'
    };
    
    return fieldMappings[fieldName] || fieldName;
}

// Update chart with selected data fields
function updateChart() {
    const selects = ['dataSelect1', 'dataSelect2', 'dataSelect3'].map(id => document.getElementById(id));
    const selectedKeys = selects.map(select => select.value).filter(key => key);
    
    const colors = [
        'rgba(75, 192, 192, 1)',
        'rgba(255, 99, 132, 1)', 
        'rgba(54, 162, 235, 1)'
    ];
    
    chart.data.datasets = selectedKeys.map((key, index) => ({
        label: formatFieldName(key),
        data: currentData.points.map(point => point[key]),
        borderColor: colors[index],
        backgroundColor: colors[index].replace('1)', '0.1)'),
        borderWidth: 2,
        fill: false,
        tension: 0.1
    }));
    
    chart.update();
}

// Highlight point on map
function highlightPointOnMap(index) {
    if (!currentData.points[index]) return;
    
    const point = currentData.points[index];
    
    // Remove previous marker
    if (currentMarker) {
        map.removeLayer(currentMarker);
    }
    
    // Add new marker
    currentMarker = L.circleMarker([point.lat, point.lon], {
        color: '#ffd700',
        fillColor: '#ffd700',
        fillOpacity: 0.8,
        radius: 8,
        weight: 2
    }).addTo(map);
    
    // Show popup with point info
    currentMarker.bindPopup(`
        <strong>Distance:</strong> ${point.dist?.toFixed(0) || 'N/A'} m<br>
        <strong>Elevation:</strong> ${point.ele?.toFixed(1) || 'N/A'} m<br>
        <strong>Speed:</strong> ${point.speed?.toFixed(1) || 'N/A'} m/s<br>
        <strong>Power:</strong> ${point.power?.toFixed(0) || 'N/A'} W
    `).openPopup();
}

// Setup event handlers
function setupEventHandlers() {

    // Setup additional buttons
    document.getElementById('newActivityBtn').addEventListener('click', function() {
        // Clear all data and start over
        sessionStorage.clear();
        window.location.href = '/';
    });

    document.getElementById('backToPowerCurveBtn').addEventListener('click', function() {
        window.location.href = '/powercurve';
    });

    // Field selector change events
    ['dataSelect1', 'dataSelect2', 'dataSelect3'].forEach(id => {
        document.getElementById(id).addEventListener('change', updateChart);
    });
    
    // Reset zoom button
    document.getElementById('resetZoomBtn').addEventListener('click', () => {
        chart.resetZoom();
    });
    
    // Download GPX button
    document.getElementById('downloadGpxBtn').addEventListener('click', () => {
        downloadGpx();
    });
}

// Download GPX file
function downloadGpx() {
    try {
        const gpxContent = sessionStorage.getItem('gpxContent');
        if (!gpxContent) {
            showError('No GPX content available for download');
            return;
        }
        
        const blob = new Blob([gpxContent], { type: 'application/gpx+xml' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = 'virtualized.gpx';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
    } catch (error) {
        console.error('Error downloading GPX:', error);
        showError('Failed to download GPX file: ' + error.message);
    }
}

// Show loading overlay
function showLoading() {
    document.getElementById('mapLoading').style.display = 'flex';
    document.getElementById('chartLoading').style.display = 'flex';
}

// Hide loading overlay
function hideLoading() {
    document.getElementById('mapLoading').style.display = 'none';
    document.getElementById('chartLoading').style.display = 'none';
}

// Show error message
function showError(message) {
    hideLoading();
    alert('Error: ' + message);
    // In a real implementation, you'd show a proper error UI
}