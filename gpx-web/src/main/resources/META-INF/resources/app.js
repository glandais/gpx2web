function initializeApp() {
    initUploadHandlers();
    initParameterHandlers();
    initializeDataChart();
    initVirtualization();
    loadSavedParameters();
}

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
