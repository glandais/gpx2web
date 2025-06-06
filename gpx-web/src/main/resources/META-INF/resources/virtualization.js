
var virtualizationTimeout;

function initVirtualization() {
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

}


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
                keys: gpxData.keys,
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

// Export functions for global access
window.updatePowerPoint = updatePowerPoint;
window.removePowerPoint = removePowerPoint;
