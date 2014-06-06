'use strict';

angular.module('milestogo')
    .controller('DashboardCtrl', function ($scope, ProgressService, ConfigService, activeProfile, $http, $timeout) {
        $scope.currentUser = activeProfile;
        $scope.error = null;
        $scope.data = {};

        ProgressService.progress($scope.currentUser.username).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.status = status;
            $scope.data = data;
            $scope.style = "width:" + data.percentage + "%";
        }).
            error(function (data, status, headers, config) {
                toastr.error("Unable to fetch your progress. Please try later.");
                $scope.error = {message: "Unable to fetch your progress. Please try later."};
                console.log(data);
                console.log(status);
            });


        $http.get("api/v1/dashboard/charts/distance").success(function (data, status, headers, config) {
            console.log('Got data ' + data);
            Morris.Line({
                element: 'activities-line-graph',
                data: data,
                xLabels: 'day',
                xLabelFormat: function (date) {
                    return moment(dateFormat(date.getTime()), "YYYYMMDD").format('MMM Do');
                },
                yLabelFormat: function (distance) {
                    return distance.toString() + ' ' + activeProfile.goalUnit.$name;
                },
                xkey: 'activityDate',
                ykeys: ['distance'],
                dateFormat: function (x) {
                    return moment(dateFormat(x), "YYYYMMDD").format('dddd, MMM Do YYYY');
                },
                labels: ['Distance Ran']

            });
        }).error(function (data, status, headers, config) {
            console.log(data);
        });

        var dateFormat = function (x) {
            var date = new Date(x);
            var yyyy = date.getFullYear().toString();
            var mm = (date.getMonth() + 1).toString();
            var dd = date.getDate().toString();
            return yyyy + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + (dd[1] ? dd : "0" + dd[0]);
        }

    });