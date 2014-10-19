var app = angular.module("miles2run-cr", ['leaflet-directive']);

var CommunityRunCtrl = function ($scope, leafletBoundsHelpers, $http, $location) {

    var communityRun = angular.copy(window.communityRun);

    angular.extend($scope, {
        defaults: {
            maxZoom: 14,
            minZoom: 2,
            tileLayer: 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
            tileLayerOptions: {
                attribution: '© <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
                opacity: 0.9,
                detectRetina: true,
                reuseTiles: true
            },
            scrollWheelZoom: false
        }
    });

    $scope.markers = new Array();

    $http.get(appContext() + 'api/v1/community_runs/' + communityRun.slug + '/profiles_group_city').then(function (res) {
        angular.forEach(res.data, function (value, key) {
            var marker = {
                lat: value.latLng[0],
                lng: value.latLng[1],
                message: 'Runners : ' + value.count + ', City: ' + value.city + ', Country: ' + value.country
            }
            $scope.markers.push(marker);
        })
    });

    function appContext() {
        if ($location.port() === 8080) {
            return "/miles2run/";
        } else {
            return "/";
        }
    }

};
app.controller('CommunityRunCtrl', CommunityRunCtrl)



