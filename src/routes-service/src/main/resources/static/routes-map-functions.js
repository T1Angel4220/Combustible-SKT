// ============================================
// Leaflet Map Functions (Gratuito, sin API key)
// ============================================

function showMapSelector(field) {
    currentLocationField = field;
    const mapContainer = document.getElementById('mapContainer');
    const mapLabel = document.getElementById('mapLabel');
    
    if (!mapContainer) return;
    
    mapLabel.textContent = `Seleccione la ubicación de ${field === 'origen' ? 'Origen' : 'Destino'} en el mapa`;
    mapContainer.style.display = 'block';
    
    // Inicializar mapa con Leaflet (centrado en Ecuador)
    const mapDiv = document.getElementById('map');
    if (!mapDiv) return;
    
    // Si el mapa ya existe, solo actualizar la vista
    if (map) {
        map.remove();
        map = null;
        mapMarker = null;
    }
    
    // Crear nuevo mapa centrado en Ecuador
    map = L.map('map').setView([-1.8312, -78.1834], 7);
    
    // Agregar capa de OpenStreetMap (gratuita)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);
    
    // Agregar marcador inicial
    mapMarker = L.marker([-1.8312, -78.1834], {
        draggable: true,
        icon: L.icon({
            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        })
    }).addTo(map);
    
    // Guardar el campo actual en el contexto del mapa
    const currentField = field;
    
    // Actualizar dirección cuando se hace clic en el mapa
    map.on('click', function(e) {
        mapMarker.setLatLng(e.latlng);
        // Pasar el campo como parámetro para asegurar que se use el correcto
        reverseGeocodeWithField(e.latlng, currentField);
    });
    
    // Actualizar dirección cuando se arrastra el marcador
    mapMarker.on('dragend', function(e) {
        // Pasar el campo como parámetro para asegurar que se use el correcto
        reverseGeocodeWithField(e.target.getLatLng(), currentField);
    });
    
    // Mostrar popup con coordenadas
    mapMarker.bindPopup('Arrastre el marcador o haga clic en el mapa').openPopup();
}

// Función auxiliar que recibe el campo explícitamente
function reverseGeocodeWithField(location, field) {
    if (!field) {
        console.error('No hay campo de ubicación especificado');
        return;
    }
    
    // Usar Nominatim (OpenStreetMap) para geocodificación inversa
    const lat = location.lat;
    const lng = location.lng;
    
    if (!lat || !lng) {
        console.error('Coordenadas inválidas:', location);
        return;
    }
    
    console.log('Geocodificando para campo:', field, 'Coordenadas:', lat, lng);
    
    // Mostrar loading
    if (mapMarker) {
        mapMarker.setOpacity(0.5);
    }
    
    // Rate limit: esperar 1 segundo entre requests (requerido por Nominatim)
    setTimeout(() => {
        fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1&accept-language=es`, {
            headers: {
                'User-Agent': 'SKT-Fuel-System/1.0'
            }
        })
            .then(response => response.json())
            .then(data => {
                // Usar el campo pasado como parámetro
                const targetField = field === 'origen' ? 
                    document.getElementById('routeOrigen') : 
                    document.getElementById('routeDestino');
                
                if (data && data.display_name) {
                    const address = data.display_name;
                    if (targetField) {
                        targetField.value = address;
                        console.log('Dirección guardada en', field, ':', address);
                    }
                    if (mapMarker) {
                        mapMarker.setOpacity(1);
                        mapMarker.setPopupContent(`<b>Ubicación seleccionada (${field === 'origen' ? 'Origen' : 'Destino'}):</b><br>${address}`).openPopup();
                    }
                } else {
                    // Si no hay dirección, mostrar coordenadas
                    const coordsText = `Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)}`;
                    if (targetField) {
                        targetField.value = coordsText;
                    }
                    if (mapMarker) {
                        mapMarker.setOpacity(1);
                        mapMarker.setPopupContent(`<b>Coordenadas (${field === 'origen' ? 'Origen' : 'Destino'}):</b><br>${coordsText}`).openPopup();
                    }
                }
            })
            .catch(error => {
                console.error('Error en geocodificación inversa:', error);
                const coordsText = `Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)}`;
                const targetField = field === 'origen' ? 
                    document.getElementById('routeOrigen') : 
                    document.getElementById('routeDestino');
                if (targetField) {
                    targetField.value = coordsText;
                }
                if (mapMarker) {
                    mapMarker.setOpacity(1);
                    mapMarker.setPopupContent(`<b>Coordenadas (${field === 'origen' ? 'Origen' : 'Destino'}):</b><br>${coordsText}`).openPopup();
                }
            });
    }, 1000);
}

// Función original mantenida para compatibilidad
function reverseGeocode(location) {
    reverseGeocodeWithField(location, currentLocationField);
}

function confirmMapLocation() {
    if (mapMarker && currentLocationField) {
        const position = mapMarker.getLatLng();
        
        // Guardar las coordenadas en los campos ocultos
        if (currentLocationField === 'origen') {
            document.getElementById('origenLat').value = position.lat;
            document.getElementById('origenLng').value = position.lng;
        } else if (currentLocationField === 'destino') {
            document.getElementById('destinoLat').value = position.lat;
            document.getElementById('destinoLng').value = position.lng;
        }
        
        // Usar la función que recibe el campo explícitamente para asegurar que se use el correcto
        reverseGeocodeWithField(position, currentLocationField);
        // Esperar un momento antes de cerrar para que la geocodificación se complete
        setTimeout(() => {
            cancelMapSelection();
        }, 1500);
    }
}

function cancelMapSelection() {
    const mapContainer = document.getElementById('mapContainer');
    if (mapContainer) {
        mapContainer.style.display = 'none';
    }
    currentLocationField = null;
}

async function calculateDistance() {
    const origenInput = document.getElementById('routeOrigen');
    const destinoInput = document.getElementById('routeDestino');
    const origenLatInput = document.getElementById('origenLat');
    const origenLngInput = document.getElementById('origenLng');
    const destinoLatInput = document.getElementById('destinoLat');
    const destinoLngInput = document.getElementById('destinoLng');
    
    if (!origenInput || !destinoInput) {
        showNotification('error', 'Error', 'No se encontraron los campos de origen y destino');
        return;
    }
    
    const origen = origenInput.value.trim();
    const destino = destinoInput.value.trim();
    const origenLat = origenLatInput ? origenLatInput.value : null;
    const origenLng = origenLngInput ? origenLngInput.value : null;
    const destinoLat = destinoLatInput ? destinoLatInput.value : null;
    const destinoLng = destinoLngInput ? destinoLngInput.value : null;
    
    if (!origen || !destino) {
        showNotification('warning', 'Advertencia', 'Por favor, ingrese origen y destino');
        return;
    }
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        
        // Construir URL con coordenadas si están disponibles
        let url = `${API_BASE_URL}/calculate-distance?origen=${encodeURIComponent(origen)}&destino=${encodeURIComponent(destino)}`;
        
        // Si tenemos coordenadas, las enviamos para mayor precisión
        if (origenLat && origenLng && destinoLat && destinoLng) {
            url += `&origenLat=${origenLat}&origenLng=${origenLng}&destinoLat=${destinoLat}&destinoLng=${destinoLng}`;
        }
        
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            if (data.error) {
                showNotification('error', 'Error', data.error);
                return;
            }
            
            document.getElementById('routeDistancia').value = data.distanciaKm.toFixed(2);
            document.getElementById('routeDuracion').value = data.duracionHoras.toFixed(2);
            
            await updateConsumoEstimado();
            
            showNotification('success', 'Éxito', 
                `Distancia calculada: ${data.distanciaKm.toFixed(2)} km, Duración: ${data.duracionHoras.toFixed(2)} horas`);
        } else {
            const errorData = await response.json().catch(() => ({ error: 'Error al calcular distancia' }));
            showNotification('error', 'Error', errorData.error || 'Error al calcular distancia');
        }
    } catch (error) {
        console.error('Error calculando distancia:', error);
        showNotification('error', 'Error', 'Error de red al calcular distancia');
    }
}

async function updateConsumoEstimado() {
    const distancia = parseFloat(document.getElementById('routeDistancia').value);
    const vehiculoId = document.getElementById('routeVehiculo').value;
    
    if (!distancia || distancia <= 0) {
        document.getElementById('routeConsumoEstimado').value = '';
        return;
    }
    
    if (!vehiculoId) {
        const tipoMaq = document.getElementById('routeTipoMaquinaria').value;
        let consumoPor100Km = 30;
        
        if (tipoMaq === 'CAMION' || tipoMaq === 'VOLQUETE') {
            consumoPor100Km = 27.5;
        } else if (tipoMaq === 'EXCAVADORA' || tipoMaq === 'CARGADOR' || tipoMaq === 'GRUA' || tipoMaq === 'MOTONIVELADORA') {
            consumoPor100Km = 40.0;
        }
        
        const consumoEstimado = (distancia / 100.0) * consumoPor100Km;
        document.getElementById('routeConsumoEstimado').value = consumoEstimado.toFixed(2);
        return;
    }
    
    try {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        const response = await fetch(`${VEHICLES_API_URL}/${vehiculoId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const vehiculo = await response.json();
            let consumoPor100Km = vehiculo.consumoPromedio || 30;
            
            if (!consumoPor100Km || consumoPor100Km <= 0) {
                const tipoMaq = vehiculo.tipoMaquinaria || document.getElementById('routeTipoMaquinaria').value;
                if (tipoMaq === 'CAMION' || tipoMaq === 'VOLQUETE') {
                    consumoPor100Km = 27.5;
                } else {
                    consumoPor100Km = 40.0;
                }
            }
            
            const consumoEstimado = (distancia / 100.0) * consumoPor100Km;
            document.getElementById('routeConsumoEstimado').value = consumoEstimado.toFixed(2);
        }
    } catch (error) {
        console.error('Error obteniendo vehículo para calcular consumo:', error);
        const consumoEstimado = (distancia / 100.0) * 30;
        document.getElementById('routeConsumoEstimado').value = consumoEstimado.toFixed(2);
    }
}

