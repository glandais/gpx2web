// Fonction pour charger les données JSON
async function loadData() {
    try {
        const response = await fetch('output.json'); // Assurez-vous que le fichier JSON est nommé 'data.json' et est dans le même répertoire
        const data = await response.json();
        initializeMapAndChart(data);
    } catch (error) {
        console.error('Erreur lors du chargement du fichier JSON:', error);
    }
}

// Initialisation de la carte et du graphique avec les données chargées
function initializeMapAndChart(data) {
    // Initialisation de la carte Leaflet
    const map = L.map('map').setView([48.8566, 2.3522], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Ajout d'une ligne entre les points sur la carte
    const latLngs = data.points.map(point => [point.lat, point.lon]);
    const polyline = L.polyline(latLngs, { color: 'blue' }).addTo(map);

    // Zoom sur la ligne
    map.fitBounds(polyline.getBounds());

    // Initialisation du graphique Chart.js
    const ctx = document.getElementById('myChart').getContext('2d');
    let myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.points.map(point => point.dist),
            datasets: []
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                x: {
                    type: 'linear',
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Distance'
                    }
                },
                y: {
                    type: 'linear',
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Data'
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(tooltipItem) {
                            const index = tooltipItem.dataIndex;
                            const point = data.points[index];
                            highlightPointOnLine(index);
                            return `${tooltipItem.dataset.label}: ${tooltipItem.raw}`;
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
                }
            }
        }
    });

    // Populate the select dropdowns with available keys
    const selects = ['dataSelect1', 'dataSelect2', 'dataSelect3'].map(id => document.getElementById(id));
    data.keys.forEach(key => {
        if (key !== 'lon' && key !== 'lat' && key !== 'dist') {
            selects.forEach(select => {
                const option = document.createElement('option');
                option.value = key;
                option.textContent = key;
                select.appendChild(option);
            });
        }
    });

    // Update the chart when a new data key is selected
    selects.forEach((select, index) => {
        select.addEventListener('change', () => {
            updateChart(data, myChart, selects);
        });
        // Initial update with the first available data key
        if (select.options.length > 0) {
            select.dispatchEvent(new Event('change'));
        }
    });

    // Function to update the chart with selected data
    function updateChart(data, chart, selects) {
        const selectedKeys = selects.map(select => select.value);
        chart.data.datasets = selectedKeys.map((key, index) => ({
            label: key,
            data: data.points.map(point => point[key]),
            borderColor: ['rgba(75, 192, 192, 1)', 'rgba(255, 99, 132, 1)', 'rgba(54, 162, 235, 1)'][index],
            borderWidth: 1,
            fill: false,
            yAxisID: 'y'//`y${index + 1}`
        }));
        chart.update();
    }

    // Function to highlight the point on the line
    function highlightPointOnLine(index) {
        const point = data.points[index];
        const marker = L.circleMarker([point.lat, point.lon], {
            color: 'gold',
            radius: 6
        }).addTo(map);

        // Remove previous marker if exists
        if (highlightPointOnLine.previousMarker) {
            map.removeLayer(highlightPointOnLine.previousMarker);
        }
        highlightPointOnLine.previousMarker = marker;
    }
    highlightPointOnLine.previousMarker = null;
}
