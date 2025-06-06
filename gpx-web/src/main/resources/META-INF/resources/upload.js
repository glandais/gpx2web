
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
