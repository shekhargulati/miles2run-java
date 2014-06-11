'use strict';

angular.module('miles2run-home')
    .controller('EditGoalCtrl', function ($scope, $http, ConfigService, $routeParams, $location) {

        $http.get(ConfigService.getBaseUrl() + "goals/" + $routeParams.goalId).success(function (data) {
            $scope.goal = data;
        }).error(function (data, status) {
            toastr.error("Unable to fetch goal. Please try after sometime.");
            console.log("Error " + data);
            console.log("Status " + status)
        });

        $scope.editGoal = function () {
            console.log($scope.goal);
            $http.put(ConfigService.getBaseUrl() + "goals/" + $scope.goal.id, $scope.goal).success(function (data) {
                toastr.success("Updated goal");
                $location.path("/");
            }).error(function (data, status) {
                toastr.error("Unable to update goal. Please try after sometime.");
                console.log("Error " + data);
                console.log("Status " + status)
                $location.path("/");
            });
        };

        $scope.today = function () {
            $scope.dt = new Date();
        };
        $scope.today();

        $scope.showWeeks = true;
        $scope.toggleWeeks = function () {
            $scope.showWeeks = !$scope.showWeeks;
        };

        $scope.clear = function () {
            $scope.dt = null;
        };

        // Disable weekend selection
        $scope.disabled = function (date, mode) {
            return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        $scope.toggleMin = function () {
            $scope.minDate = ( $scope.minDate ) ? null : new Date();
        };
        $scope.toggleMin();

        $scope.open = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.opened = true;
        };

        $scope.dateOptions = {
            'year-format': "'yy'",
            'starting-day': 1
        };

        $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
        $scope.format = $scope.formats[0];
    });