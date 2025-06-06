
var dataChart;

const availableAxises = {
    power: {
        text: 'Power (W)',
        color: '#dc3545',
        defaultRange: {
            min: 0,
            max: 1000
        }
    },
    elevation: {
        text: 'Elevation (m)',
        color: '#28a745',
        defaultRange: {
            min: 0,
            max: 1000
        }
    },
    speed: {
        text: 'Speed (km/h)',
        color: '#fd7e14',
        defaultRange: {
            min: 0,
            max: 100
        }
    },
    radius: {
        text: 'Radius (m)',
        color: '#fd7e14',
        defaultRange: {
            min: 0,
            max: 1000
        }
    }
}

function pointsDataProvider(key) {
    return function(distances, points) {
        return distances.map((dist, i) => {
            const point = points[i]
            let value = 0
            if (point[key]) {
                value = point[key]
            }
            return {
                x: dist,
                y: value
            }
        });
    }
}

const defaultOptions = {
    borderWidth: 2,
    fill: false,
    pointRadius: 0,
    pointHoverRadius: 4
}

const datasetInterest = {
    mustSee: 1,
    insights: 2,
    computing: 3,
    debug: 4
}

const availableData = {
    user_power: {
        interest: datasetInterest.mustSee,
        axis: 'power',
        label: 'Power Curve',
        detail: 'Provided power',
        options: {
            borderColor: '#007bff',
            backgroundColor: 'rgba(0, 123, 255, 0.1)',
            borderWidth: 3,
            fill: false,
            pointRadius: 6,
            pointHoverRadius: 8,
            pointBackgroundColor: '#007bff',
            pointBorderColor: '#ffffff',
            pointBorderWidth: 2
        }
    },
    ele: {
        interest: datasetInterest.mustSee,
        axis: 'elevation',
        label: 'Elevation',
        detail: 'Skadi data'
    },
    p_cyclist_current_speed: {
        interest: datasetInterest.debug,
        axis: 'speed',
        label: 'Current speed',
        detail: 'Speed during computation'
    },
    p_aero: {
        interest: datasetInterest.insights,
        axis: 'power',
        label: 'Aero power',
        detail: 'Power due to aerodynamics'
    },
    p_cyclist_optimal_power: {
        interest: datasetInterest.computing,
        axis: 'power',
        label: 'Optimal power',
        detail: 'Power Curve with harmonics'
    },
    p_cyclist_optimal_speed: {
        interest: datasetInterest.computing,
        axis: 'speed',
        label: 'Optimal speed',
        detail: 'Stable speed at optimal power given gradient'
    },
    p_cyclist_raw: {
        interest: datasetInterest.computing,
        axis: 'power',
        label: 'Raw power',
        detail: 'Power applied by cyclist during computation'
    },
    p_cyclist_wheel: {
        interest: datasetInterest.computing,
        axis: 'power',
        label: 'Wheel power 1',
        detail: 'Power applied to wheel during computation'
    },
    p_gravity: {
        interest: datasetInterest.insights,
        axis: 'power',
        label: 'Gravity power',
        detail: 'Power due to gravity'
    },
    p_power_from_acc: {
        interest: datasetInterest.computing,
        axis: 'power',
        label: 'Total power from acceleration',
        detail: 'Power computed with kinetic equation'
    },
    p_power_wheel_from_acc: {
        interest: datasetInterest.computing,
        axis: 'power',
        label: 'Wheel power from acceleration',
        detail: 'Power from acceleration minus external power, negative if breaking'
    },
    p_rolling_resistance: {
        interest: datasetInterest.insights,
        axis: 'power',
        label: 'Rolling resistance power',
        detail: 'Power due to rolling resistance'
    },
    p_wheel_bearings: {
        interest: datasetInterest.insights,
        axis: 'power',
        label: 'Wheel bearings power',
        detail: 'Power due to bearings'
    },
    power: {
        interest: datasetInterest.mustSee,
        axis: 'power',
        label: 'Power',
        detail: 'Final power'
    },
    radius: {
        interest: datasetInterest.computing,
        axis: 'radius',
        label: 'Radius',
        detail: 'Road radius'
    },
    speed: {
        interest: datasetInterest.mustSee,
        axis: 'speed',
        label: 'Speed',
        detail: 'Computed speed'
    },
    speed_max: {
        interest: datasetInterest.insights,
        axis: 'speed',
        label: 'Maximum speed',
        detail: 'Maximum speed'
    },
    speed_max_incline: {
        interest: datasetInterest.computing,
        axis: 'speed',
        label: 'Maximum speed incline',
        detail: 'Maximum speed given incline and road radius'
    },
    virt_speed_current: {
        interest: datasetInterest.debug,
        axis: 'speed',
        label: 'Current speed',
        detail: 'Current speed during computation'
    },
}

function getShades(hue, numberOfShades) {
    var lightness = 0;
    var saturation = 0;
    var lightnessSteps = numberOfShades === 1 ? 50 : 90 / numberOfShades;
    var saturationStepsDistance = 100 / Math.ceil(numberOfShades / 2);
    var shades = new Array();

    for (var i = 1; i <= numberOfShades; i++) {
        lightness += lightnessSteps;
        saturation += saturationStepsDistance;
        shades.push("hsl(" + hue + ", " + saturation + "%, " + lightness + "%)");
    }

    return shades;
};

function getColorSet(numberOfHues, numberOfShades) {
    var hueStepDistance = 360 / numberOfHues;
    var hue = 0;
    var colors = new Array();

    for (var i = 0; i < numberOfHues; i++) {
        colors.push(getShades(hue, numberOfShades));
        hue += hueStepDistance;
    }

    return colors;
};

const chartColors = getColorSet(Object.keys(availableData).length, 1);

Object.entries(availableData).forEach(([, value], index) => {
    value.options = value.options || {};
    value.options.borderColor = chartColors[index]
    value.options.backgroundColor = chartColors[index]
});

Object.keys(availableData).forEach(key => {
    availableData[key].provider = pointsDataProvider(key);
});
availableData.user_power.provider =
    (distance, points) => (AppState.powerCurveData.map(point => ({ x: point.distanceKm, y: point.powerW })));


StateManager.addListener(function (keys) {
    if (keys.indexOf("virtualizationResult") >= 0) {
        updateDataCharts();
    }
});


function updateDataCharts() {
    if (!dataChart) {
        return;
    }

    Object.keys(dataChart.options.scales).forEach(key => {
        dataChart.options.scales[key].display = false;
    });
    dataChart.data.datasets = [];
    dataChart.options.scales.x.display = true;
    if (AppState.powerCurveData && AppState.virtualizationResult && AppState.virtualizationResult.points) {
        const points = AppState.virtualizationResult.points;
        const distances = points.map(point => point.dist / 1000);
        dataChart.data.datasets = AppState.chartDatasets.map(dataset => {
            const info = availableData[dataset];
            dataChart.options.scales[info.axis].display = true;
            const data = info.provider(distances, points);
            const options = { ...defaultOptions };
            if (info.options) {
                Object.assign(options, info.options)
            }
            return {
                label: info.label,
                data,
                yAxisID: info.axis,
                ...options
            };
        });
    }
    updateChartScales();
    dataChart.update('none');
}

function updateChartScales() {
    const ranges = {
        x: {
            min: 0,
            max: AppState.totalDistanceKm()
        }
    }
    dataChart.data.datasets.forEach(dataset => {
        let range = ranges[dataset.yAxisID]
        range = getYRange(dataset.data, range);
        ranges[dataset.yAxisID] = range
    });

    Object.keys(dataChart.options.scales).forEach(key => {
        const range = ranges[key]
        if (range) {
            let withPadding;
            if (range.min - range.max == 0) {
                withPadding = availableAxises[key].defaultRange
            } else {
                withPadding = range;
            }
            dataChart.options.scales[key].min = withPadding.min;
            dataChart.options.scales[key].max = withPadding.max;
            dataChart.options.plugins.zoom.limits[key].min = dataChart.options.scales[key].min
            dataChart.options.plugins.zoom.limits[key].max = dataChart.options.scales[key].max
        }
    });
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
    let range = max - min;
    if (range === 0) {
        range = 10;
    }
    const padding = range * paddingPercent;

    return {
        min: min - padding,
        max: max + padding
    };
}

function populateDatasetModal() {
    const powerContainer = document.getElementById('powerDatasets');
    const speedContainer = document.getElementById('speedDatasets');
    const otherContainer = document.getElementById('otherDatasets');
    
    // Clear containers
    powerContainer.innerHTML = '';
    speedContainer.innerHTML = '';
    otherContainer.innerHTML = '';
    
    Object.entries(availableData).forEach(([key, dataset]) => {
        const isSelected = AppState.chartDatasets.includes(key);
        
        const datasetItem = document.createElement('div');
        datasetItem.className = `dataset-item ${isSelected ? 'selected' : ''}`;
        datasetItem.style.cursor = 'pointer';
        datasetItem.dataset.key = key;
        
        datasetItem.innerHTML = `
            <div class="form-check">
                <input class="form-check-input" type="checkbox" id="dataset_${key}" ${isSelected ? 'checked' : ''}>
                <label class="form-check-label w-100" for="dataset_${key}">
                    <div class="dataset-label">${dataset.label}</div>
                    <div class="dataset-detail">${dataset.detail}</div>
                </label>
            </div>
        `;
        
        // Add to appropriate container
        if (dataset.axis === 'power') {
            powerContainer.appendChild(datasetItem);
        } else if (dataset.axis === 'speed') {
            speedContainer.appendChild(datasetItem);
        } else {
            otherContainer.appendChild(datasetItem);
        }
    });
}

function setupDatasetModalHandlers() {
    // Handle individual dataset selection
    document.addEventListener('change', function(e) {
        if (e.target.matches('.dataset-item input[type="checkbox"]')) {
            const key = e.target.closest('.dataset-item').dataset.key;
            const isChecked = e.target.checked;
            
            let newDatasets = [...AppState.chartDatasets];
            
            if (isChecked && !newDatasets.includes(key)) {
                newDatasets.push(key);
            } else if (!isChecked && newDatasets.includes(key)) {
                newDatasets = newDatasets.filter(d => d !== key);
            }
            
            StateManager.setState({ chartDatasets: newDatasets });
        }
    });

    // Select All button
    document.getElementById('selectAllBtn').addEventListener('click', function() {
        const allKeys = Object.keys(availableData);
        StateManager.setState({ chartDatasets: allKeys });
    });
    
    // Clear All button
    document.getElementById('clearAllBtn').addEventListener('click', function() {
        StateManager.setState({ chartDatasets: [] });
    });
}

function updateDatasetModalSelection() {
    document.querySelectorAll('.dataset-item').forEach(item => {
        const key = item.dataset.key;
        const checkbox = item.querySelector('input[type="checkbox"]');
        const isSelected = AppState.chartDatasets.includes(key);
        
        checkbox.checked = isSelected;
        item.classList.toggle('selected', isSelected);
    });
}

function updateDatasetCounts() {
    const count = AppState.chartDatasets.length;
    const totalCount = Object.keys(availableData).length;
    
    // Update button text
    const selectedCountSpan = document.getElementById('selectedDatasetCount');
    if (selectedCountSpan) {
        selectedCountSpan.textContent = count;
    }
    
    // Update modal counter
    const modalCountSpan = document.getElementById('modalSelectedCount');
    if (modalCountSpan) {
        modalCountSpan.textContent = count;
    }
}

// Add listener for chartDatasets changes to update modal and charts
StateManager.addListener(function (keys) {
    if (keys.indexOf("chartDatasets") >= 0) {
        updateDataCharts();
        updateDatasetModalSelection();
        updateDatasetCounts();
    }
});

// Chart initialization and interaction functions
function initializeDataChart() {
    const dataCtx = document.getElementById('dataChart');
    if (!dataCtx) return;

    document.getElementById('resetZoomBtn').addEventListener('click', function() {
        if (dataChart) {
            dataChart.resetZoom();
        }
    });

    const scales = {
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
        }
    }

    const limits = {
        x: {min: 0, max: 10}
    }
    Object.keys(availableAxises).forEach(key => {
        const axis = availableAxises[key];
        scales[key] = {
            type: 'linear',
            display: true,
            position: 'right',
            offset: true,
            title: {
                display: true,
                text: axis.text,
                color: axis.color,
                font: { weight: 'bold' }
            },
            ticks: {
                color: axis.color
            },
            grid: {
                drawOnChartArea: false,
            }
        }
        limits[key] = {min: 0, max: 10}
    });


    dataChart = new Chart(dataCtx, {
        type: 'line',
        data: {
            datasets: [
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
            scales,
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
                    limits,
                }
            },
            onClick: function(event, elements) {
                if (AppState.isDragging) return;

                const hasPowerCurvePoint = elements.some(element => element.datasetIndex === 0);

                if (!hasPowerCurvePoint) {
                    const canvasPosition = Chart.helpers.getRelativePosition(event, dataChart);
                    const dataX = dataChart.scales.x.getValueForPixel(canvasPosition.x);
                    const dataY = dataChart.scales.power.getValueForPixel(canvasPosition.y);

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
    populateDatasetModal();
    setupDatasetModalHandlers();
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
            const dataY = dataChart.scales.power.getValueForPixel(canvasPosition.y);

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
