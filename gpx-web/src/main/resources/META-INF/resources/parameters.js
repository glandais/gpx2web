
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
