document.addEventListener('DOMContentLoaded', function() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('gpxFile');
    const browseBtn = document.getElementById('browseBtn');
    const analyzeBtn = document.getElementById('analyzeBtn');
    const spinner = document.getElementById('spinner');
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');
    const progressSection = document.getElementById('progressSection');
    const errorSection = document.getElementById('errorSection');
    const errorMessage = document.getElementById('errorMessage');

    let selectedFile = null;

    // Clear any existing session data when starting fresh
    clearSessionData();

    // File input change handler
    fileInput.addEventListener('change', function(e) {
        if (e.target.files.length > 0) {
            handleFileSelection(e.target.files[0]);
        }
    });

    // Browse button click
    browseBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        fileInput.click();
    });

    // Upload area click (but not when clicking the browse button)
    uploadArea.addEventListener('click', function(e) {
        if (e.target !== browseBtn && !browseBtn.contains(e.target)) {
            fileInput.click();
        }
    });

    // Drag and drop handlers
    uploadArea.addEventListener('dragover', function(e) {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', function(e) {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', function(e) {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            const file = files[0];
            if (file.name.toLowerCase().endsWith('.gpx')) {
                fileInput.files = files;
                handleFileSelection(file);
            } else {
                showError('Please select a valid GPX file');
            }
        }
    });

    // Analyze button click
    analyzeBtn.addEventListener('click', function() {
        if (selectedFile) {
            analyzeGpxFile(selectedFile);
        }
    });

    function handleFileSelection(file) {
        selectedFile = file;
        
        // Show file info
        fileName.textContent = file.name;
        fileSize.textContent = formatFileSize(file.size);
        fileInfo.classList.remove('d-none');
        
        // Enable analyze button
        analyzeBtn.disabled = false;
        
        // Hide errors
        errorSection.classList.add('d-none');
    }

    async function analyzeGpxFile(file) {
        try {
            showProgress();
            
            const formData = new FormData();
            formData.append('gpxFile', file);
            
            const response = await fetch('/api/analyze', {
                method: 'POST',
                body: formData
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Analysis failed');
            }
            
            const analysisData = await response.json();
            
            // Store analysis and file data in sessionStorage
            sessionStorage.setItem('gpxAnalysis', JSON.stringify(analysisData));
            sessionStorage.setItem('selectedGpxFile', JSON.stringify({
                name: file.name,
                size: file.size,
                lastModified: file.lastModified
            }));
            
            // Store the file data
            const reader = new FileReader();
            reader.onload = function(e) {
                sessionStorage.setItem('gpxFileData', e.target.result);
                
                hideProgress();
                
                // Redirect to parameters page
                window.location.href = '/parameters';
            };
            reader.readAsDataURL(file);
            
        } catch (error) {
            console.error('Error analyzing GPX:', error);
            showError('Failed to analyze GPX file: ' + error.message);
        }
    }

    function showProgress() {
        analyzeBtn.disabled = true;
        spinner.classList.remove('d-none');
        progressSection.classList.remove('d-none');
        errorSection.classList.add('d-none');
    }

    function hideProgress() {
        analyzeBtn.disabled = false;
        spinner.classList.add('d-none');
        progressSection.classList.add('d-none');
    }

    function showError(message) {
        hideProgress();
        errorMessage.textContent = message;
        errorSection.classList.remove('d-none');
    }

    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    function clearSessionData() {
        sessionStorage.clear();
    }
});