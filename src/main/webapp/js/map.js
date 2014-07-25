var coordinates = [30.3781788, -5.7766974];
var zoomLevel = 3;

if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function (position) {
            var longitude = position.coords.longitude;
            var latitude = position.coords.latitude;
            coordinates = [latitude, longitude];
            zoomLevel = 6;
        }
        , function (e) {
            console.log(e);
        },
        { timeout: 45000 }
    );
}

var map = L.map('map').setView([coordinates[0], coordinates[1]], zoomLevel);
var markerLayerGroup = L.layerGroup().addTo(map);
L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'
}).addTo(map);
map.on('dragend', getMapData);
map.on('zoomend', getMapData);
map.whenReady(getMapData)


function getMapData(e) {
    var data = [
        {
            runners: 10,
            city: 'Kurukshetra',
            country: 'India',
            position: [30.3781788, 76.7766974]
        },
        {
            runners: 20,
            city: 'Boston',
            country: 'USA',
            position: [42.3584308, -71.0597732]
        }
    ];
    pinTheMap(data);
}

function pinTheMap(data) {
    map.removeLayer(markerLayerGroup);
    var markerArray = new Array(data.length)
    for (var i = 0; i < data.length; i++) {
        var cityDetails = data[i];
        var popupInformation = "Runners:" + cityDetails.runners + "</br>";
        popupInformation += "City: " + cityDetails.city + "</br>";
        popupInformation += "Country: " + cityDetails.country + "</br>";
        markerArray[i] = L.marker([cityDetails.position[0], cityDetails.position[1]]).bindPopup(popupInformation);
    }
    markerLayerGroup = L.layerGroup(markerArray).addTo(map);
}