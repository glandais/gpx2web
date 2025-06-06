
var dataChart;

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

    document.getElementById('resetZoomBtn').addEventListener('click', function() {
        if (dataChart) {
            dataChart.resetZoom();
        }
    });

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
